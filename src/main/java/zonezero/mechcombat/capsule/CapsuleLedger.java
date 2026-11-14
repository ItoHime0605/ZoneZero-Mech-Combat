package zonezero.mechcombat.capsule;

import java.util.*;

/**
 * The authoritative one-time-use capsule ledger. This is server-side logic only;
 * callers MUST persist the returned Snapshot BEFORE changing physical Sable state.
 * An inventory item holds a capsule UUID, never a copy of the machine's blocks.
 *
 * This ledger deliberately refuses recovery without inspecting the actual world:
 * guessing whether Sable completed a removal/reconstruction risks duplication.
 */
public final class CapsuleLedger {
    public enum Phase { PACKING, PACKED, DEPLOYING, DEPLOYED, RECOVERY_REQUIRED }

    public record Entry(UUID capsuleId, UUID ownerId, String dimension,
                        List<UUID> subLevels, Phase phase, long revision) {
        public Entry {
            Objects.requireNonNull(capsuleId, "capsuleId");
            Objects.requireNonNull(ownerId, "ownerId");
            Objects.requireNonNull(dimension, "dimension");
            Objects.requireNonNull(subLevels, "subLevels");
            Objects.requireNonNull(phase, "phase");
            if (dimension.isBlank()) throw new IllegalArgumentException("Blank dimension");
            if (subLevels.isEmpty() || subLevels.stream().anyMatch(Objects::isNull)
                || new HashSet<>(subLevels).size() != subLevels.size())
                throw new IllegalArgumentException("Missing or duplicate Sable sublevel UUID");
            if (revision < 0) throw new IllegalArgumentException("Invalid revision");
            subLevels = List.copyOf(subLevels);
        }
        Entry advance(Phase target) {
            return new Entry(capsuleId, ownerId, dimension, subLevels, target, revision + 1);
        }
    }

    /** Persistence layer must write snapshots/ledger atomically and durably. */
    public interface Store {
        void save(Entry next) throws Exception;
    }

    public static class CapsuleException extends IllegalStateException {
        public CapsuleException(String message) { super(message); }
    }

    private final Store store;
    private final Map<UUID, Entry> entries = new HashMap<>();
    private final Map<UUID, UUID> reservedSubLevels = new HashMap<>();

    public CapsuleLedger(Store store, Collection<Entry> recovered) {
        this.store = Objects.requireNonNull(store);
        for (Entry entry : recovered) {
            if (entries.putIfAbsent(entry.capsuleId(), entry) != null)
                throw new CapsuleException("Duplicate capsule ID in saved ledger");
            // Released records are tombstones. They cannot be reused.
            if (entry.phase() == Phase.DEPLOYED) continue;
            for (UUID shipId : entry.subLevels()) {
                UUID old = reservedSubLevels.putIfAbsent(shipId, entry.capsuleId());
                if (old != null) throw new CapsuleException("Ship already reserved: " + shipId);
            }
        }
    }

    /** Call only AFTER an immutable multi-sublevel snapshot is durably archived. */
    public synchronized Entry beginCapture(UUID capsuleId, UUID ownerId,
                                           String dimension, Collection<UUID> subLevels) throws Exception {
        Entry e = new Entry(capsuleId, ownerId, dimension, List.copyOf(subLevels), Phase.PACKING, 0);
        if (entries.containsKey(capsuleId)) throw new CapsuleException("Capsule ID already used");
        for (UUID id : e.subLevels())
            if (reservedSubLevels.containsKey(id)) throw new CapsuleException("Mech already in a capsule transaction");
        store.save(e); // persistence must succeed before anything can be removed
        entries.put(capsuleId, e);
        e.subLevels().forEach(id -> reservedSubLevels.put(id, capsuleId));
        return e;
    }

    /** Only call after all related Sable sublevels are confirmed absent. */
    public synchronized Entry finishCapture(UUID capsuleId) throws Exception {
        return transition(capsuleId, Phase.PACKING, Phase.PACKED);
    }

    /** Only one copy of a filled capsule can begin a deployment. */
    public synchronized Entry beginDeploy(UUID capsuleId, UUID actorId) throws Exception {
        Entry e = require(capsuleId);
        if (!e.ownerId().equals(actorId)) throw new CapsuleException("Not the capsule owner");
        return transition(capsuleId, Phase.PACKED, Phase.DEPLOYING);
    }

    /** Call only after all Sable structures and their relationships are restored. */
    public synchronized Entry finishDeploy(UUID capsuleId) throws Exception {
        Entry e = transition(capsuleId, Phase.DEPLOYING, Phase.DEPLOYED);
        for (UUID id : e.subLevels()) reservedSubLevels.remove(id, capsuleId);
        return e;
    }

    /** If a world or persistence operation fails, quarantine the record. */
    public synchronized Entry quarantine(UUID capsuleId) throws Exception {
        Entry e = require(capsuleId);
        if (e.phase() == Phase.DEPLOYED) throw new CapsuleException("Already deployed");
        Entry changed = e.advance(Phase.RECOVERY_REQUIRED);
        store.save(changed);
        entries.put(capsuleId, changed);
        return changed;
    }

    /** Only an admin reconcile operation may reset a pre-deployment failure. */
    public synchronized Entry resetUndeployed(UUID capsuleId, boolean verifiedNoActiveSublevels) throws Exception {
        if (!verifiedNoActiveSublevels) throw new CapsuleException("World verification required");
        return transition(capsuleId, Phase.RECOVERY_REQUIRED, Phase.PACKED);
    }

    public synchronized Optional<Entry> find(UUID capsuleId) {
        return Optional.ofNullable(entries.get(capsuleId));
    }

    public synchronized boolean canDeploy(UUID capsuleId, UUID actorId) {
        Entry e = entries.get(capsuleId);
        return e != null && e.phase() == Phase.PACKED && e.ownerId().equals(actorId);
    }

    public synchronized List<Entry> snapshot() { return List.copyOf(entries.values()); }

    private Entry transition(UUID capsuleId, Phase expected, Phase next) throws Exception {
        Entry current = require(capsuleId);
        if (current.phase() != expected)
            throw new CapsuleException("Invalid capsule transition: " + current.phase() + " -> " + next);
        Entry changed = current.advance(next);
        store.save(changed); // fail closed; leave previous state in memory on I/O error
        entries.put(capsuleId, changed);
        return changed;
    }

    private Entry require(UUID id) {
        Entry e = entries.get(id);
        if (e == null) throw new CapsuleException("Unknown capsule; never trust item NBT alone");
        return e;
    }
}
