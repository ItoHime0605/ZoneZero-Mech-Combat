package com.eviesaurr.sablemobinteractions.health;

import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.server.level.ServerLevel;

/**
 * Used by ALL zombie AI paths and existing target dummies.
 * Wrecks at exactly zero HP are untargetable until repaired above zero.
 */
public final class MechTargeting {
    private MechTargeting() {}

    public static boolean isAttackable(ServerLevel level, SubLevel section) {
        if (section == null || section.isRemoved()) return false;
        var owner = WalkerLocator.resolve(level, section);
        return owner.isPresent()
            && MechHealthMath.attackable(MechHealth.get(level, owner.get()).hp());
    }
}
