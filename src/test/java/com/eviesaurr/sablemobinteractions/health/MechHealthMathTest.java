package com.eviesaurr.sablemobinteractions.health;

/** Run this class with javac/java; no Minecraft dependencies. */
public final class MechHealthMathTest {
    public static void main(String[] args) {
        eq(MechHealthMath.maxHp(0), 1);
        eq(MechHealthMath.scaledMaxHp(100, 4.0), 400);
        eq(MechHealthMath.scaledMaxHp(0, 4.0), 4);
        eq(MechHealthMath.scaledMaxHp(100000, 4.0), 200000);
        assertTrue(MechHealthMath.attackable(1));
        assertTrue(!MechHealthMath.canTakeSharedHit(105, 100, 6));
        assertTrue(MechHealthMath.canTakeSharedHit(106, 100, 6));
        assertTrue(MechHealthMath.canTakeSharedHit(101, 100, 0));
        assertTrue(MechHealthMath.canTakeSharedHit(1, 999, 6));
        assertTrue(!MechHealthMath.attackable(0));
        assertTrue(MechHealthMath.attackable(MechHealthMath.repair(0,400,30)));
        eq(MechHealthMath.damage(300, 300, 40), 260);
        eq(MechHealthMath.damage(10, 300, 90), 0);
        eq(MechHealthMath.repair(0, 300, 30), 30);
        eq(MechHealthMath.repair(290, 300, 30), 300);
        eq(MechHealthMath.resize(120, 300, 500), 200);
        eq(MechHealthMath.resize(0, 300, 500), 0);
        assertTrue(!MechHealthMath.sparks(150, 300));
        assertTrue(MechHealthMath.sparks(149, 300));
        assertTrue(!MechHealthMath.slowed(60, 300));
        assertTrue(MechHealthMath.slowed(59, 300));
        assertTrue(MechHealthMath.disabled(0));
        // HUD colors: green >75%, yellow >50%, orange >20%, red <=20%.
        eq(MechHudColors.index(100,100), 0);
        eq(MechHudColors.index(76,100), 0);
        eq(MechHudColors.index(75,100), 1);
        eq(MechHudColors.index(51,100), 1);
        eq(MechHudColors.index(50,100), 2);
        eq(MechHudColors.index(21,100), 2);
        eq(MechHudColors.index(20,100), 3);
        eq(MechHudColors.index(0,100), 3);
        // A stationary foot or a teleport/time skip cannot deal stomp damage.
        assertTrue(!LegImpactMath.isMoving(0.01, 0.12));
        assertTrue(LegImpactMath.isMoving(0.04, 0.12));
        assertTrue(!LegImpactMath.isValidStep(0.25, 0.12, 2));
        assertTrue(LegImpactMath.isValidStep(0.25, 0.12, 1));
        assertTrue(!LegImpactMath.canHit(110, 100, 20));
        assertTrue(LegImpactMath.canHit(120, 100, 20));
        System.out.println("MechHealthMath + HUD colors + leg stomp thresholds: ALL TESTS PASSED");
    }
    private static void eq(int a, int b) { if (a != b) throw new AssertionError(a+" != "+b); }
    private static void assertTrue(boolean yes) { if (!yes) throw new AssertionError(); }
}
