package com.eviesaurr.sablemobinteractions.health;

/** Pure Java damage and repair arithmetic, independent of Minecraft runtime. */
public final class MechHealthMath {
    private MechHealthMath() {}

    public static int maxHp(int sum) { return Math.max(1, Math.min(200000, sum)); }

    /** Material HP scaling with saturation at the supported maximum. */
    public static int scaledMaxHp(int baseHp, double multiplier) {
        double scaled = Math.ceil(Math.max(1, baseHp) * Math.max(1.0, multiplier));
        return (int) Math.max(1, Math.min(200000, scaled));
    }

    /** Broken walkers cannot be selected, chased, or attacked. */
    public static boolean attackable(int hp) { return hp > 0; }

    /** True when a hit can register after the previous shared-walker hit. */
    public static boolean canTakeSharedHit(long now, long last, int grace) {
        return grace <= 0 || now < last || now - last >= grace;
    }

    public static int damage(int hp, int maxHp, int amount) {
        return Math.max(0, Math.min(maxHp, hp) - Math.max(0, amount));
    }

    public static int repair(int hp, int maxHp, int amount) {
        return Math.min(maxHp, Math.max(0, hp) + Math.max(0, amount));
    }

    /** Rebuilding with new materials preserves percentage: rebuilding is not a free repair. */
    public static int resize(int hp, int oldMax, int newMax) {
        if (oldMax <= 0) return newMax;
        if (hp <= 0) return 0;
        return Math.max(1, Math.min(newMax, (int) Math.round((double) hp * newMax / oldMax)));
    }

    public static double ratio(int hp, int max) {
        return max <= 0 ? 0.0 : Math.max(0.0, Math.min(1.0, (double) hp / max));
    }

    public static boolean sparks(int hp, int max) { return ratio(hp, max) < 0.50; }

    public static boolean slowed(int hp, int max) { return ratio(hp, max) < 0.20; }

    public static boolean disabled(int hp) { return hp <= 0; }
}
