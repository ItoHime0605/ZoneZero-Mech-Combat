package com.eviesaurr.sablemobinteractions.item;

import com.eviesaurr.sablemobinteractions.config.Config;
import com.eviesaurr.sablemobinteractions.health.MechHealth;
import com.eviesaurr.sablemobinteractions.health.WalkerLocator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Handheld tool: right click a walker (or right click in air within range). */
public final class RepairToolItem extends Item {
    public RepairToolItem(Properties properties) { super(properties); }

    private boolean repairNearby(Level level, Player player, ItemStack stack, InteractionHand hand) {
        if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer sp)) return false;
        if (sp.getCooldowns().isOnCooldown(this)) return true;
        var nearby = WalkerLocator.nearest(server, sp, Config.REPAIR_RANGE.get());
        if (nearby.isEmpty()) return false;
        var walker = nearby.get();
        var status = MechHealth.get(server, walker);
        if (status.hp() >= status.maxHp()) {
            sp.displayClientMessage(Component.translatable("message.zonezero_mechcombat.full"), true);
            return true;
        }
        var repaired = MechHealth.repair(server, walker, Config.REPAIR_AMOUNT.get());
        if (status.hp() == 0 && repaired.hp() > 0) {
            // Restored mechs are controllable again after emergency repair.
            walker.core().parked = false;
            walker.core().syncToClients();
        }
        if (!sp.getAbilities().instabuild) {
            stack.hurtAndBreak(1, sp, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        }
        sp.getCooldowns().addCooldown(this, 20);
        sp.displayClientMessage(Component.translatable(
            "message.zonezero_mechcombat.repaired", repaired.hp(), repaired.maxHp()), true);
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var p = context.getPlayer();
        if (p == null) return InteractionResult.PASS;
        if (context.getLevel().isClientSide()) return InteractionResult.SUCCESS;
        return repairNearby(context.getLevel(), p, context.getItemInHand(), context.getHand())
            ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.success(stack);
        return repairNearby(level, player, stack, hand)
            ? InteractionResultHolder.success(stack) : InteractionResultHolder.pass(stack);
    }
}
