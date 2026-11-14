package com.eviesaurr.sablemobinteractions.health;

/** Run this class with javac/java; no Minecraft dependencies. */
public final class MechHealthMathTest {
    public static void main(String[] args) {
        eq(MechHealthMath.maxHp(0), 1);
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
        System.out.println("MechHealthMath: ALL TESTS PASSED");
    }
    private static void eq(int a, int b) { if (a != b) throw new AssertionError(a+" != "+b); }
    private static void assertTrue(boolean yes) { if (!yes) throw new AssertionError(); }
}
