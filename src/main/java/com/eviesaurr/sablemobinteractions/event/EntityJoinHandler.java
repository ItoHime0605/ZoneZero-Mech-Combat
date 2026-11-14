package com.eviesaurr.sablemobinteractions.event;

import com.eviesaurr.sablemobinteractions.SableMobInteractions;
import com.eviesaurr.sablemobinteractions.config.Config;
import com.eviesaurr.sablemobinteractions.goal.SubLevelMeleeAttackGoal;
import com.eviesaurr.sablemobinteractions.goal.SubLevelNativeTargetGoal;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * Only zombie-family mobs and The Undead Revamped mobs may attack walker mechs.
 * Player/animal/other monster targeting is unchanged. No enemy vehicle AI.
 */
@EventBusSubscriber(modid = SableMobInteractions.MOD_ID)
public class EntityJoinHandler {
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof Zombie zombie) {
            zombie.goalSelector.addGoal(2, new SubLevelMeleeAttackGoal(
                    zombie, Config.ZOMBIE_SEARCH_RADIUS.get(), Config.ZOMBIE_ATTACK_RANGE.get()));
        } else if (event.getEntity() instanceof Monster monster) {
            ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(monster.getType());
            if ("undead_revamp2".equals(mobId.getNamespace())) {
                monster.goalSelector.addGoal(2, new SubLevelNativeTargetGoal(
                        monster, Config.GENERIC_MONSTER_SEARCH_RADIUS.get()));
            }
        }
    }
}
