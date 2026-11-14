package com.eviesaurr.sablemobinteractions.client;

import com.eviesaurr.sablemobinteractions.SableMobInteractions;
import com.eviesaurr.sablemobinteractions.health.MechHudColors;
import dev.letitwalk.client.PilotInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** A compact potion-effect-style indicator, no large lower-left HP bar. */
public final class MechHudOverlay {
    private static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath(SableMobInteractions.MOD_ID, "mech_hp_indicator");
    private static final ResourceLocation ICONS =
        ResourceLocation.fromNamespaceAndPath(SableMobInteractions.MOD_ID, "textures/gui/mech_health_icons.png");

    private MechHudOverlay() {}

    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.MOB_EFFECTS, ID, MechHudOverlay::render);
    }

    private static void render(GuiGraphics gui, DeltaTracker tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui ||
                !PilotInput.isActive() || !ClientHudStatus.isFresh()) return;

        final int hp = ClientHudStatus.hp();
        final int max = ClientHudStatus.maxHp();
        final int sprite = MechHudColors.index(hp, max);
        final int x = gui.guiWidth() - 27;
        final int y = 55; // Just below the first row of vanilla potion effects.

        gui.blit(ICONS, x, y, sprite * 24, 0, 24, 24, 96, 24);

        // Short exact percentage, no persistent health bar.
        int pct = (int) Math.round(100.0 * hp / Math.max(1, max));
        String label = pct + "%";
        gui.drawString(mc.font, label, x - mc.font.width(label) - 4, y + 8, 0xEBF0F1, true);
    }
}
