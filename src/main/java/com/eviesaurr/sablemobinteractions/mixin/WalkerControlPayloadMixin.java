package com.eviesaurr.sablemobinteractions.mixin;

import com.eviesaurr.sablemobinteractions.health.MechHealth;
import com.eviesaurr.sablemobinteractions.health.WalkerLocator;
import com.eviesaurr.sablemobinteractions.network.MechHudStatusPayload;
import dev.letitwalk.content.WalkerCoreBlockEntity;
import dev.letitwalk.network.WalkerControlPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Sends HP for the exact walker receiving the player's validated pilot input. */
@Mixin(value = WalkerControlPayload.class, remap = false)
public abstract class WalkerControlPayloadMixin {
    @Inject(method = "drive", at = @At("TAIL"), remap = false)
    private static void zonezero$sendMechHud(
            WalkerControlPayload controls, ServerPlayer player, WalkerCoreBlockEntity core,
            CallbackInfo ci) {
        if (core == null || !core.isAssembled()) return;
        if (player.serverLevel().getGameTime() % 5 != 0) return;

        WalkerLocator.byCore(player.serverLevel(), core).ifPresent(walker -> {
            var health = MechHealth.get(player.serverLevel(), walker);
            PacketDistributor.sendToPlayer(player,
                new MechHudStatusPayload(health.hp(), health.maxHp()));
        });
    }
}
