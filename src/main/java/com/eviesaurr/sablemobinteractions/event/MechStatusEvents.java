package com.eviesaurr.sablemobinteractions.event;

import com.eviesaurr.sablemobinteractions.SableMobInteractions;
import com.eviesaurr.sablemobinteractions.health.MechHealth;
import com.eviesaurr.sablemobinteractions.health.MechHealthMath;
import com.eviesaurr.sablemobinteractions.health.WalkerLocator;
import dev.letitwalk.content.WalkerCoreBlockEntity;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Server-side visual feedback on the moving walker torso. */
@EventBusSubscriber(modid = SableMobInteractions.MOD_ID)
public final class MechStatusEvents {
    private MechStatusEvents() {}

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.getGameTime() % 10 != 0) return; // 2 feedback updates per second
        var container = SubLevelContainer.getContainer(level);
        if (container == null) return;
        for (SubLevel s : container.getAllSubLevels()) {
            if (!(s instanceof ServerSubLevel body) || body.isRemoved()) continue;
            for (var actor : body.getPlot().getBlockEntityActors()) {
                if (!(actor instanceof WalkerCoreBlockEntity core) || !core.isAssembled()) continue;
                var status = MechHealth.get(level, new WalkerLocator.Walker(body, core));
                if (!MechHealthMath.sparks(status.hp(), status.maxHp())) continue;

                var box = body.boundingBox();
                double x = (box.minX() + box.maxX()) * 0.5;
                double y = (box.minY() + box.maxY()) * 0.5;
                double z = (box.minZ() + box.maxZ()) * 0.5;
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z,
                    3, 0.8, 0.6, 0.8, 0.04);
                if (status.hp() <= 0) {
                    level.sendParticles(ParticleTypes.SMOKE, x, y, z,
                        2, 0.5, 0.5, 0.5, 0.02);
                }
            }
        }
    }
}
