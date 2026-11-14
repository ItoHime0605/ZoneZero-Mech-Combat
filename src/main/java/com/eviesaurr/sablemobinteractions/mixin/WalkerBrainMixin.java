package com.eviesaurr.sablemobinteractions.mixin;

import com.eviesaurr.sablemobinteractions.config.Config;
import com.eviesaurr.sablemobinteractions.health.MechHealth;
import com.eviesaurr.sablemobinteractions.health.MechHealthMath;
import com.eviesaurr.sablemobinteractions.health.WalkerLocator;
import dev.letitwalk.content.WalkerCoreBlockEntity;
import dev.letitwalk.walker.WalkerBrain;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies speed reduction per walker without editing global gear speeds.
 * HEAD guard also prevents motion controls when shared HP is zero.
 * Target field and method signatures checked against Let It Walk! 0.1.5 JAR.
 */
@Mixin(value = WalkerBrain.class, remap = false)
public abstract class WalkerBrainMixin {
    @Shadow(remap = false) @Final public static double[] GEAR_SPEED;

    @Inject(method = "tick", at = @At("HEAD"), remap = false)
    private static void zonezero$disableBrokenWalker(
            WalkerCoreBlockEntity core, ServerSubLevel body, CallbackInfo callback) {
        if (!core.isAssembled()) return;
        ServerLevel level = body.getLevel();
        var status = MechHealth.get(level, new WalkerLocator.Walker(body, core));
        if (!MechHealthMath.disabled(status.hp())) return;
        core.input = 0;
        core.latchedInput = 0;
        core.throttle = 0;
        core.strafe = 0;
        core.parked = true;
        core.flying = false;
        core.flightKeyHeld = false;
        core.dashKeyHeld = false;
        core.dashCarry = 0;
        core.jetPower = 0;
        core.targetVelocity.zero();
    }

    @Redirect(
        method = "plan",
        at = @At(value = "FIELD",
            target = "Ldev/letitwalk/walker/WalkerBrain;GEAR_SPEED:[D",
            remap = false),
        remap = false
    )
    private static double[] zonezero$localGearSpeed(
            WalkerCoreBlockEntity core, ServerSubLevel body, Pose3dc pose) {
        if (!core.isAssembled()) return GEAR_SPEED;
        ServerLevel level = body.getLevel();
        var status = MechHealth.get(level, new WalkerLocator.Walker(body, core));
        if (!MechHealthMath.slowed(status.hp(), status.maxHp())) return GEAR_SPEED;
        double factor = MechHealthMath.disabled(status.hp()) ? 0.0 : Config.SLOW_MULTIPLIER.get();
        double[] modified = GEAR_SPEED.clone();
        for (int i = 0; i < modified.length; i++) modified[i] *= factor;
        return modified;
    }
}
