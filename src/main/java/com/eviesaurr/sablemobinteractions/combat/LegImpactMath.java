package com.eviesaurr.sablemobinteractions.combat;

/** Pure Java movement and per-target impact-cooldown boundaries. */
public final class LegImpactMath {
    private LegImpactMath() {}

    public static boolean isMoving(double distanceSquared, double minMovement) {
        return distanceSquared >= minMovement * minMovement;
    }

    public static boolean isValidStep(double distanceSquared, double minMovement, long elapsedTicks) {
        return elapsedTicks == 1 && isMoving(distanceSquared, minMovement) && distanceSquared <= 6.25;
    }

    public static boolean canHit(long now, long previousTick, int cooldown) {
        return now < previousTick || now - previousTick >= cooldown;
    }
}
