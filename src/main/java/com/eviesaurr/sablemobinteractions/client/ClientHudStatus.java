package com.eviesaurr.sablemobinteractions.client;

import net.minecraft.client.Minecraft;

/** Client-only snapshot; expires if pilot controls stop sending packets. */
public final class ClientHudStatus {
    private static int hp = 0;
    private static int maxHp = 0;
    private static long lastUpdateNs = 0L;

    private ClientHudStatus() {}

    public static void receive(int value, int maximum) {
        maxHp = Math.max(1, maximum);
        hp = Math.max(0, Math.min(value, maxHp));
        lastUpdateNs = System.nanoTime();
    }

    public static boolean isFresh() {
        return lastUpdateNs != 0L && System.nanoTime() - lastUpdateNs < 2_500_000_000L;
    }

    public static int hp() { return hp; }
    public static int maxHp() { return maxHp; }
}
