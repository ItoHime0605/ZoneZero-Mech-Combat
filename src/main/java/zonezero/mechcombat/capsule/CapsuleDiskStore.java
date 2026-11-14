package zonezero.mechcombat.capsule;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;
import java.util.zip.CRC32;

/**
 * World-local atomic file writer. Minecraft NBT is stored as an opaque, compressed
 * binary blob. The network-facing capsule item MUST store only a UUID token.
 * No automatic restoration: ledger RECOVERY_REQUIRED demands world verification.
 */
public final class CapsuleDiskStore implements CapsuleLedger.Store {
    private static final int RECORD_MAGIC = 0x5A5A434C; // ZZCL
    private static final int SNAPSHOT_MAGIC = 0x5A5A4353; // ZZCS
    private static final int FORMAT = 1;
    private static final int MAX_SNAPSHOT_BYTES = 64 * 1024 * 1024;
    private final Path ledgerDir;
    private final Path archiveDir;

    public CapsuleDiskStore(Path worldDir) throws IOException {
        Path base = Objects.requireNonNull(worldDir).resolve("data/zonezero_mechcombat/capsules");
        ledgerDir = base.resolve("ledger");
        archiveDir = base.resolve("archives");
        Files.createDirectories(ledgerDir);
        Files.createDirectories(archiveDir);
    }

    /** Must be called before beginCapture. Never overwrite an existing archive. */
    public void archiveSnapshot(UUID capsuleId, byte[] compressedNbt) throws IOException {
        Objects.requireNonNull(capsuleId);
        Objects.requireNonNull(compressedNbt);
        if (compressedNbt.length == 0 || compressedNbt.length > MAX_SNAPSHOT_BYTES)
            throw new IOException("Invalid compressed Sable snapshot size");
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(compressedNbt.length + 16);
        try (DataOutputStream output = new DataOutputStream(buffer)) {
            output.writeInt(SNAPSHOT_MAGIC);
            output.writeInt(FORMAT);
            output.writeInt(compressedNbt.length);
            CRC32 crc = new CRC32();
            crc.update(compressedNbt);
            output.writeLong(crc.getValue());
            output.write(compressedNbt);
        }
        atomicWrite(archiveDir.resolve(capsuleId + ".dat"), buffer.toByteArray(), false);
    }

    public byte[] loadSnapshot(UUID capsuleId) throws IOException {
        byte[] contents = Files.readAllBytes(archiveDir.resolve(capsuleId + ".dat"));
        if (contents.length > MAX_SNAPSHOT_BYTES + 20) throw new IOException("Snapshot too large");
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(contents))) {
            if (in.readInt() != SNAPSHOT_MAGIC || in.readInt() != FORMAT) throw new IOException("Invalid snapshot header");
            int length = in.readInt();
            long checksum = in.readLong();
            if (length <= 0 || length > MAX_SNAPSHOT_BYTES || length != in.available())
                throw new IOException("Truncated/oversized Sable snapshot");
            byte[] payload = in.readNBytes(length);
            CRC32 crc = new CRC32();
            crc.update(payload);
            if (checksum != crc.getValue()) throw new IOException("Snapshot checksum mismatch");
            return payload;
        }
    }

    @Override
    public void save(CapsuleLedger.Entry entry) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(b)) {
            out.writeInt(RECORD_MAGIC);
            out.writeInt(FORMAT);
            writeUuid(out, entry.capsuleId());
            writeUuid(out, entry.ownerId());
            out.writeUTF(entry.dimension());
            out.writeInt(entry.subLevels().size());
            for (UUID id : entry.subLevels()) writeUuid(out, id);
            out.writeUTF(entry.phase().name());
            out.writeLong(entry.revision());
        }
        atomicWrite(ledgerDir.resolve(entry.capsuleId() + ".dat"), b.toByteArray(), true);
    }

    public List<CapsuleLedger.Entry> loadRecords() throws IOException {
        List<CapsuleLedger.Entry> output = new ArrayList<>();
        try (DirectoryStream<Path> files = Files.newDirectoryStream(ledgerDir, "*.dat")) {
            for (Path file : files) {
                byte[] bytes = Files.readAllBytes(file);
                try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
                    if (in.readInt() != RECORD_MAGIC || in.readInt() != FORMAT)
                        throw new IOException("Invalid record version");
                    UUID cid = readUuid(in);
                    UUID oid = readUuid(in);
                    String dimension = in.readUTF();
                    int count = in.readInt();
                    if (count < 1 || count > 128) throw new IOException("Invalid member count");
                    List<UUID> members = new ArrayList<>();
                    for (int i=0; i<count; i++) members.add(readUuid(in));
                    CapsuleLedger.Phase phase = CapsuleLedger.Phase.valueOf(in.readUTF());
                    long revision = in.readLong();
                    if (in.available() != 0 || !file.getFileName().toString().equals(cid + ".dat"))
                        throw new IOException("Record trailing data or mismatched filename");
                    output.add(new CapsuleLedger.Entry(cid, oid, dimension, members, phase, revision));
                } catch (RuntimeException e) {
                    throw new IOException("Invalid capsule ledger entry: " + file, e);
                }
            }
        }
        return List.copyOf(output);
    }

    private static void atomicWrite(Path target, byte[] bytes, boolean replace) throws IOException {
        Path temp = Files.createTempFile(target.getParent(), ".capsule-", ".tmp");
        try {
            try (FileChannel ch = FileChannel.open(temp, StandardOpenOption.WRITE)) {
                ByteBuffer b = ByteBuffer.wrap(bytes);
                while (b.hasRemaining()) ch.write(b);
                ch.force(true);
            }
            if (replace) {
                Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } else {
                // Reject existing IDs (never overwrite a preexisting machine archive).
                // ATOMIC_MOVE alone may replace on some filesystems; CREATE_NEW
                // reservation ensures the target exists before a competing writer.
                try (FileChannel ignored = FileChannel.open(target, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                    // placeholder reserved; replaced atomically below
                }
                try {
                    Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    Files.deleteIfExists(target);
                    throw e;
                }
            }
            // Best-effort directory durability (not supported by every filesystem).
            try (FileChannel dir = FileChannel.open(target.getParent(), StandardOpenOption.READ)) { dir.force(true); }
            catch (UnsupportedOperationException | IOException ignored) { }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private static void writeUuid(DataOutputStream out, UUID u) throws IOException {
        out.writeLong(u.getMostSignificantBits()); out.writeLong(u.getLeastSignificantBits());
    }
    private static UUID readUuid(DataInputStream in) throws IOException {
        return new UUID(in.readLong(), in.readLong());
    }
}
