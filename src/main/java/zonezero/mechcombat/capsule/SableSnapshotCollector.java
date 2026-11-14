package zonezero.mechcombat.capsule;

import com.eviesaurr.sablemobinteractions.health.WalkerLocator;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.storage.serialization.SubLevelSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * ALPHA / SOURCE ONLY. Non-mutating capture of the entire physics-connected
 * walker. Sable handles chunk sections, block entities, inventory NBT, fluid/
 * block scheduled ticks and user_data through ServerLevelPlot.save().
 *
 * This snapshot is NOT an inventory item and MUST NOT be removed from the
 * world until an atomic durable archive has been written and verified.
 * WARNING: The restore side needs reserved/remapped Sable plot coordinates,
 * full dependency restoration and real-world collision checks.
 */
public final class SableSnapshotCollector {
    private SableSnapshotCollector() {}

    public record Snapshot(UUID rootId, List<UUID> memberIds, CompoundTag nbt) { }

    public static Snapshot collect(WalkerLocator.Walker walker) {
        var core = walker.core();
        if (!core.isAssembled() || !core.parked || core.flying || core.swimming)
            throw new IllegalStateException("Walker must be fully assembled, parked, and grounded");
        if (!core.carried.isEmpty())
            throw new IllegalStateException("Drop carried external cargo before packing");

        Collection<SubLevel> connected = SubLevelHelper.getConnectedChain(walker.body());
        if (connected.isEmpty() || !connected.contains(walker.body()))
            throw new IllegalStateException("Walker body is absent from Sable connected chain");
        if (connected.size() > 128)
            throw new IllegalStateException("Safety limit: unexpectedly large connected structure");

        List<ServerSubLevel> members = new ArrayList<>();
        Set<UUID> unique = new HashSet<>();
        for (SubLevel part : connected) {
            if (!(part instanceof ServerSubLevel serverPart) || serverPart.isRemoved())
                throw new IllegalStateException("Unloaded/removed member cannot be safely snapshotted");
            if (!unique.add(serverPart.getUniqueId()))
                throw new IllegalStateException("Duplicate Sable sublevel UUID");
            RigidBodyHandle handle = RigidBodyHandle.of(serverPart);
            if (handle != null) {
                Vector3d lv = handle.getLinearVelocity(new Vector3d());
                Vector3d av = handle.getAngularVelocity(new Vector3d());
                if (lv.lengthSquared() > 0.0004 || av.lengthSquared() > 0.0004)
                    throw new IllegalStateException("Mech is still moving; stop it before packing");
            }
            members.add(serverPart);
        }

        List<UUID> ids = members.stream().map(ServerSubLevel::getUniqueId).toList();
        CompoundTag root = new CompoundTag();
        root.putInt("format", 1);
        root.putUUID("body", walker.body().getUniqueId());
        ListTag parts = new ListTag();
        for (ServerSubLevel member : members) {
            // Include known connected peers for load ordering. The final loader
            // must restore dependencies and constraints in a verified order.
            var archive = SubLevelSerializer.toData(member, ids);
            parts.add(archive.fullTag().copy());
        }
        root.put("sublevels", parts);
        return new Snapshot(walker.body().getUniqueId(), List.copyOf(ids), root);
    }
}
