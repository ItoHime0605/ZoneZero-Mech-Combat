package com.eviesaurr.sablemobinteractions.health;

import com.eviesaurr.sablemobinteractions.config.Config;
import dev.letitwalk.walker.LegRuntime;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunkSection;
import java.util.HashSet;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One shared HP pool per walker body; every foot/leg segment contributes its
 * actual material blocks to maximum HP. Data stored on Sable sublevel user tag.
 */
public final class MechHealth {
    private static final String TAG = "ZoneZeroMechCombat";
    private static final Map<UUID, Long> LAST_SCAN = new ConcurrentHashMap<>();
    public record Status(int hp, int maxHp) {
        public double fraction() { return MechHealthMath.ratio(hp, maxHp); }
    }
    private MechHealth() {}

    private static CompoundTag root(ServerSubLevel body) {
        CompoundTag data = body.getUserDataTag();
        if (data == null) {
            data = new CompoundTag();
            body.setUserDataTag(data);
        }
        return data;
    }

    public static Status get(ServerLevel level, WalkerLocator.Walker walker) {
        var root = root(walker.body());
        CompoundTag tag = root.getCompound(TAG);
        UUID id = walker.body().getUniqueId();
        long now = level.getGameTime();
        boolean newMech = !tag.contains("MaxHP");
        if (newMech || now - LAST_SCAN.getOrDefault(id, Long.MIN_VALUE / 2) >= Config.RECALC_TICKS.get()) {
            int newMax = calculateMax(level, walker);
            int oldMax = tag.getInt("MaxHP");
            int hp = newMech ? newMax : MechHealthMath.resize(tag.getInt("HP"), oldMax, newMax);
            tag.putInt("MaxHP", newMax);
            tag.putInt("HP", hp);
            root.put(TAG, tag);
            LAST_SCAN.put(id, now);
        }
        return new Status(tag.getInt("HP"), tag.getInt("MaxHP"));
    }

    private static int calculateMax(ServerLevel level, WalkerLocator.Walker walker) {
        var container = SubLevelContainer.getContainer(level);
        if (container == null) return 1;
        var segments = new HashSet<UUID>();
        for (LegRuntime leg : walker.core().legs) {
            if (leg.built != null) segments.addAll(leg.built.segments);
        }

        // If no leg sublevel is currently loaded, retain a basic HP allowance per foot.
        int sum = 0;
        int counted = 0;
        Map<String, Integer> configured = new HashMap<>();
        for (String row : Config.MATERIAL_HP.get()) {
            int sep = row.lastIndexOf('=');
            if (sep <= 0) continue;
            try { configured.put(row.substring(0, sep).trim(), Math.max(0, Integer.parseInt(row.substring(sep + 1).trim()))); }
            catch (NumberFormatException ignored) { }
        }
        for (UUID sectionId : segments) {
            SubLevel s = container.getSubLevel(sectionId);
            if (!(s instanceof ServerSubLevel part) || part.isRemoved()) continue;
            for (var holder : part.getPlot().getLoadedChunks()) {
                var chunk = holder.getChunk();
                if (chunk == null) continue;
                LevelChunkSection[] sections = chunk.getSections();
                int minY = chunk.getMinBuildHeight();
                for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
                    var states = sections[sectionIndex];
                    if (states == null || states.hasOnlyAir()) continue;
                    for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
                        var state = states.getBlockState(x, y, z);
                        if (state.isAir()) continue;
                        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                        String key = id.toString();
                        Integer exact = configured.get(key);
                        if (exact != null) { sum += exact; counted++; continue; }
                        // Only solid fallback blocks provide structural HP.
                        if (state.blocksMotion()) {
                            String name = id.getPath();
                            int weight = name.contains("tungsten") ? 110
                                : name.contains("titanium") ? 85
                                : name.contains("steel") ? 55
                                : name.contains("iron") ? 38
                                : name.contains("copper") ? 22
                                : name.contains("wood") || name.contains("plank") ? 6
                                : Config.DEFAULT_BLOCK_HP.get();
                            sum += Math.max(0, weight);
                            counted++;
                        }
                    }
                }
            }
        }
        if (counted == 0) sum = Math.max(1, walker.core().legs.size()) * Config.FALLBACK_FOOT_HP.get();
        return MechHealthMath.scaledMaxHp(sum, Config.MAX_HP_MULTIPLIER.get());
    }

    private static Status write(WalkerLocator.Walker walker, int hp, int max) {
        CompoundTag root = root(walker.body());
        CompoundTag tag = root.getCompound(TAG);
        tag.putInt("HP", Math.max(0, Math.min(max, hp)));
        tag.putInt("MaxHP", max);
        root.put(TAG, tag);
        return new Status(tag.getInt("HP"), max);
    }

    public static Status damage(ServerLevel level, WalkerLocator.Walker walker, int amount) {
        Status old = get(level, walker);
        if (!MechHealthMath.attackable(old.hp()) || amount <= 0) return old;
        CompoundTag data = root(walker.body());
        CompoundTag tag = data.getCompound(TAG);
        long now = level.getGameTime();
        if (tag.contains("LastDamageTick")
                && !MechHealthMath.canTakeSharedHit(now, tag.getLong("LastDamageTick"),
                    Config.SHARED_HIT_GRACE_TICKS.get())) {
            return old;
        }
        tag.putLong("LastDamageTick", now);
        data.put(TAG, tag);
        return write(walker, MechHealthMath.damage(old.hp(), old.maxHp(), amount), old.maxHp());
    }

    public static Status repair(ServerLevel level, WalkerLocator.Walker walker, int amount) {
        Status old = get(level, walker);
        return write(walker, MechHealthMath.repair(old.hp(), old.maxHp(), amount), old.maxHp());
    }
}
