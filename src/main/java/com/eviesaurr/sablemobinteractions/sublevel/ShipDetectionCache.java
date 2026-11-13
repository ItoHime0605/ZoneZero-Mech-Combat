package com.eviesaurr.sablemobinteractions.sublevel;

import com.eviesaurr.sablemobinteractions.health.WalkerLocator;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.server.level.ServerLevel;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Attack only sublevels belonging to an actual assembled Let It Walk! mech. */
public class ShipDetectionCache {
    private static final int LIFETIME_TICKS = 40;
    private record Entry(boolean attackable, long tick) {}
    private static final Map<UUID, Entry> CACHE = new ConcurrentHashMap<>();
    public static boolean isShip(ServerLevel level, SubLevel section, long now) {
        if (section == null || section.isRemoved()) return false;
        Entry previous = CACHE.get(section.getUniqueId());
        if (previous != null && now - previous.tick < LIFETIME_TICKS) return previous.attackable;
        boolean valid = WalkerLocator.resolve(level, section).isPresent();
        CACHE.put(section.getUniqueId(), new Entry(valid, now));
        return valid;
    }
    public static void clearSubLevel(UUID id) { CACHE.remove(id); }
}
