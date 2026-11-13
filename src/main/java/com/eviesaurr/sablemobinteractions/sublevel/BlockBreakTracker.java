package com.eviesaurr.sablemobinteractions.sublevel;

import com.eviesaurr.sablemobinteractions.config.Config;
import com.eviesaurr.sablemobinteractions.health.MechHealth;
import com.eviesaurr.sablemobinteractions.health.WalkerLocator;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.BlockPos;
import java.util.UUID;

/** Converts an armor BLOCK hit to SHARED walker HP damage; never destroys blocks. */
public final class BlockBreakTracker {
    private BlockBreakTracker() { }
    public static boolean registerHit(SubLevel section, BlockPos localPos, Entity source) {
        if (source == null || !(source.level() instanceof ServerLevel level)) return false;
        var walker = WalkerLocator.resolve(level, section);
        if (walker.isEmpty()) return false;
        double attack = source instanceof LivingEntity living
            ? living.getAttributeValue(Attributes.ATTACK_DAMAGE) : 1.0;
        int damage = Math.max(1, (int) Math.ceil(Math.max(0, attack) * Config.DAMAGE_MULTIPLIER.get()));
        MechHealth.Status after = MechHealth.damage(level, walker.get(), damage);
        return after.hp() <= 0;
    }

    /** Legacy v1.0.1 callsite compatibility; shared HP is persisted in Sable. */
    public static void clearSubLevel(UUID sectionId) { }
}
