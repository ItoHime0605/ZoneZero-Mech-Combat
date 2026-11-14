package com.eviesaurr.sablemobinteractions.health;

/**
 * HUD color thresholds (no Minecraft dependencies):
 * green >75%; yellow >50%; orange >20%; red 0..20%.
 */
public final class MechHudColors {
    private MechHudColors() { }

    public static int index(int hp, int maxHp) {
        double fraction = MechHealthMath.ratio(hp, maxHp);
        if (fraction > 0.75) return 0;
        if (fraction > 0.50) return 1;
        if (fraction > 0.20) return 2;
        return 3;
    }
}
