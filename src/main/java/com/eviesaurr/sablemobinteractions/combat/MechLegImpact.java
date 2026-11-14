package com.eviesaurr.sablemobinteractions.combat;

import com.eviesaurr.sablemobinteractions.config.Config;
import com.eviesaurr.sablemobinteractions.health.MechHealth;
import com.eviesaurr.sablemobinteractions.health.MechHealthMath;
import com.eviesaurr.sablemobinteractions.health.WalkerLocator;
import dev.letitwalk.walker.LegRuntime;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Uses actual walking foot trajectories; avoids generic body collision damage.
 * Runs inside WalkerBrain.tick TAIL on the logical server.
 */
public final class MechLegImpact {
    private record Sample(Vec3 position, long tick) {}
    private static final Map<UUID, Map<Integer, Sample>> SAMPLES = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<UUID, Long>> TARGET_HITS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_SEEN = new ConcurrentHashMap<>();

    private MechLegImpact() {}

    public static void onWalkerTick(ServerLevel level, WalkerLocator.Walker walker) {
        if (!Config.LEG_STOMP_ENABLED.get()) return;
        var core = walker.core();
        UUID mechId = walker.body().getUniqueId();
        long now = level.getGameTime();
        LAST_SEEN.put(mechId, now);

        // If disabled, the whole walker is inert, including every foot.
        if (!MechHealthMath.attackable(MechHealth.get(level, walker).hp())) {
            SAMPLES.remove(mechId);
            return;
        }

        Map<Integer, Sample> feet = SAMPLES.computeIfAbsent(mechId, id -> new ConcurrentHashMap<>());
        Map<UUID, Long> hits = TARGET_HITS.computeIfAbsent(mechId, id -> new ConcurrentHashMap<>());
        int index = 0;
        for (LegRuntime leg : core.legs) {
            int legIndex = index++;
            if (leg.arm) continue;
            Vec3 nowPos = leg.footWorld((double) now);
            if (nowPos == null || !Double.isFinite(nowPos.x) || !Double.isFinite(nowPos.y)
                    || !Double.isFinite(nowPos.z)) continue;

            Sample old = feet.put(legIndex, new Sample(nowPos, now));
            if (old == null || leg.state != LegRuntime.State.SWING) continue;
            Vec3 prev = old.position();
            double squared = nowPos.distanceToSqr(prev);
            if (!LegImpactMath.isValidStep(squared, Config.LEG_STOMP_MIN_MOVEMENT.get(),
                    now - old.tick())) continue;

            double radius = Config.LEG_STOMP_RADIUS.get();
            AABB swept = new AABB(
                    Math.min(prev.x, nowPos.x) - radius,
                    Math.min(prev.y, nowPos.y) - radius,
                    Math.min(prev.z, nowPos.z) - radius,
                    Math.max(prev.x, nowPos.x) + radius,
                    Math.max(prev.y, nowPos.y) + radius + 0.8,
                    Math.max(prev.z, nowPos.z) + radius
            );
            Vec3 motion = nowPos.subtract(prev);
            double lengthSq = motion.lengthSqr();

            for (Monster target : level.getEntitiesOfClass(Monster.class, swept, Monster::isAlive)) {
                Long last = hits.get(target.getUUID());
                if (last != null && !LegImpactMath.canHit(now, last,
                        Config.LEG_STOMP_TARGET_COOLDOWN.get())) continue;

                Vec3 centre = target.position().add(0,
                        Math.min(0.8, target.getBbHeight() * 0.5), 0);
                double t = Math.max(0.0, Math.min(1.0,
                        centre.subtract(prev).dot(motion) / Math.max(1.0e-9, lengthSq)));
                Vec3 contact = prev.add(motion.scale(t));
                double reach = radius + target.getBbWidth() * 0.5;
                if (centre.distanceToSqr(contact) > reach * reach) continue;

                if (!target.hurt(level.damageSources().generic(), Config.LEG_STOMP_DAMAGE.get().floatValue())) continue;
                hits.put(target.getUUID(), now);

                double deltaX = target.getX() - contact.x;
                double deltaZ = target.getZ() - contact.z;
                if (deltaX * deltaX + deltaZ * deltaZ < 1.0e-4) {
                    deltaX = motion.x;
                    deltaZ = motion.z;
                }
                // knockback() subtracts the supplied direction internally:
                // pass foot -> mob inverted, so the mob moves AWAY from the foot.
                target.knockback(Config.LEG_STOMP_KNOCKBACK.get(), -deltaX, -deltaZ);
            }
        }

        // Bound cache size on servers with frequently rebuilt mechs and despawned mobs.
        if (now % 400 == 0) {
            hits.entrySet().removeIf(e -> now - e.getValue() > 400);
            LAST_SEEN.entrySet().removeIf(e -> {
                if (now - e.getValue() <= 800) return false;
                SAMPLES.remove(e.getKey());
                TARGET_HITS.remove(e.getKey());
                return true;
            });
        }
    }
}
