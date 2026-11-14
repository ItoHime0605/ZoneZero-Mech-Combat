package com.eviesaurr.sablemobinteractions.health;

import dev.letitwalk.content.WalkerCoreBlockEntity;
import dev.letitwalk.walker.LegRuntime;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import java.util.Optional;
import java.util.UUID;

/** Resolves a leg/foot sublevel to the common walker core (not just the torso plot). */
public final class WalkerLocator {
    public record Walker(ServerSubLevel body, WalkerCoreBlockEntity core) { }
    private WalkerLocator() {}

    public static Optional<Walker> resolve(ServerLevel level, SubLevel hitSection) {
        var container = SubLevelContainer.getContainer(level);
        if (container == null || hitSection == null) return Optional.empty();
        UUID hitId = hitSection.getUniqueId();
        for (SubLevel section : container.getAllSubLevels()) {
            if (!(section instanceof ServerSubLevel body) || body.isRemoved()) continue;
            for (var actor : body.getPlot().getBlockEntityActors()) {
                if (!(actor instanceof WalkerCoreBlockEntity core) || !core.isAssembled()) continue;
                if (body.getUniqueId().equals(hitId) || containsLegSection(core, hitId)) {
                    return Optional.of(new Walker(body, core));
                }
            }
        }
        return Optional.empty();
    }

    /** Exact walker being piloted (never substitute another nearby vehicle). */
    public static Optional<Walker> byCore(ServerLevel level, WalkerCoreBlockEntity target) {
        if (target == null || !target.isAssembled()) return Optional.empty();
        var container = SubLevelContainer.getContainer(level);
        if (container == null) return Optional.empty();
        for (SubLevel section : container.getAllSubLevels()) {
            if (!(section instanceof ServerSubLevel body) || body.isRemoved()) continue;
            for (var actor : body.getPlot().getBlockEntityActors()) {
                if (actor == target) return Optional.of(new Walker(body, target));
            }
        }
        return Optional.empty();
    }

    public static boolean containsLegSection(WalkerCoreBlockEntity core, UUID sectionId) {
        for (LegRuntime leg : core.legs) {
            if (leg.built != null && leg.built.segments.contains(sectionId)) return true;
        }
        return false;
    }

    /** Returns nearest assembled walker inside a world-space interaction radius. */
    public static Optional<Walker> nearest(ServerLevel level, Player player, double radius) {
        var container = SubLevelContainer.getContainer(level);
        if (container == null) return Optional.empty();
        Walker best = null;
        double bestSq = radius * radius;
        for (SubLevel section : container.getAllSubLevels()) {
            if (!(section instanceof ServerSubLevel body) || body.isRemoved()) continue;
            var bounds = body.boundingBox();
            double x = Math.max(bounds.minX(), Math.min(player.getX(), bounds.maxX()));
            double y = Math.max(bounds.minY(), Math.min(player.getY(), bounds.maxY()));
            double z = Math.max(bounds.minZ(), Math.min(player.getZ(), bounds.maxZ()));
            double sq = player.distanceToSqr(x, y, z);
            if (sq > bestSq) continue;
            // Any leg or foot can be repaired, not only the torso body.
            Optional<Walker> owner = resolve(level, body);
            if (owner.isPresent()) {
                best = owner.get();
                bestSq = sq;
            }
        }
        return Optional.ofNullable(best);
    }
}
