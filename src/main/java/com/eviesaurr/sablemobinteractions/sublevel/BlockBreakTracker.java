package com.eviesaurr.sablemobinteractions.sublevel;

import com.eviesaurr.sablemobinteractions.config.Config;
import com.eviesaurr.sablemobinteractions.ModTags;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prototype from upstream v1.0.1: each physical armor block is damaged by hits.
 * Shared mech HP is NOT implemented in this revision.
 */
public class BlockBreakTracker {
    private static final Map<UUID, Map<BlockPos, Integer>> hitCounts = new ConcurrentHashMap<>();

    public static boolean registerHit(SubLevel subLevel, BlockPos localPos, Entity source) {
        if (subLevel == null || subLevel.isRemoved() || source == null) return false;
        var subLevelHits = hitCounts.computeIfAbsent(subLevel.getUniqueId(), id -> new ConcurrentHashMap<>());
        int hits = subLevelHits.merge(localPos, 1, Integer::sum);
        BlockPos rawPos = SubLevelInteraction.toRawBlockPos(subLevel, localPos);
        // Never destroy walker cores in the prototype: core failure needs a proper HP system.
        if (source.level().getBlockState(rawPos).is(ModTags.SHIP_ESSENTIAL_BLOCKS)) {
            subLevelHits.remove(localPos);
            return false;
        }
        int hitsNeeded = Config.BASE_HITS_TO_BREAK.get();
        int stage = Math.min(9, (hits * 10) / hitsNeeded);
        source.level().destroyBlockProgress(source.getId(), rawPos, stage);
        if (hits >= hitsNeeded) {
            source.level().destroyBlock(rawPos, true, source);
            subLevelHits.remove(localPos);
            return true;
        }
        return false;
    }

    public static void clearSubLevel(UUID subLevelId) {
        hitCounts.remove(subLevelId);
    }
}
