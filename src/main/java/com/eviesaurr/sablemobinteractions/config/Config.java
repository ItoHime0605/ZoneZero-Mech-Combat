package com.eviesaurr.sablemobinteractions.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.List;

/** All values are configurable in zonezero_mechcombat-common.toml. */
public final class Config {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue ZOMBIE_SEARCH_RADIUS;
    public static final ModConfigSpec.DoubleValue ZOMBIE_ATTACK_RANGE;
    public static final ModConfigSpec.BooleanValue LEG_STOMP_ENABLED;
    public static final ModConfigSpec.DoubleValue LEG_STOMP_DAMAGE;
    public static final ModConfigSpec.DoubleValue LEG_STOMP_KNOCKBACK;
    public static final ModConfigSpec.DoubleValue LEG_STOMP_RADIUS;
    public static final ModConfigSpec.DoubleValue LEG_STOMP_MIN_MOVEMENT;
    public static final ModConfigSpec.IntValue LEG_STOMP_TARGET_COOLDOWN;
    public static final ModConfigSpec.DoubleValue GENERIC_MONSTER_SEARCH_RADIUS;
    public static final ModConfigSpec.DoubleValue PLAYER_PRESENCE_RANGE;
    public static final ModConfigSpec.DoubleValue DAMAGE_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue INCOMING_DAMAGE_SCALE;
    public static final ModConfigSpec.IntValue MAX_DAMAGE_PER_HIT;
    public static final ModConfigSpec.IntValue SHARED_HIT_GRACE_TICKS;
    public static final ModConfigSpec.DoubleValue MAX_HP_MULTIPLIER;
    public static final ModConfigSpec.IntValue ZOMBIE_ATTACK_COOLDOWN_TICKS;
    public static final ModConfigSpec.DoubleValue SLOW_MULTIPLIER;
    public static final ModConfigSpec.IntValue DEFAULT_BLOCK_HP;
    public static final ModConfigSpec.IntValue FALLBACK_FOOT_HP;
    public static final ModConfigSpec.IntValue RECALC_TICKS;
    public static final ModConfigSpec.IntValue REPAIR_AMOUNT;
    public static final ModConfigSpec.IntValue REPAIR_RANGE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MATERIAL_HP;

    static {
        var b = new ModConfigSpec.Builder();
        b.push("zombie");
        ZOMBIE_SEARCH_RADIUS = b.comment("Vanilla zombie mech search radius.").defineInRange("searchRadius",24.0,1.0,64.0);
        ZOMBIE_ATTACK_RANGE = b.comment("Vanilla zombie melee reach against walker armor.").defineInRange("attackRange",2.0,0.5,8.0);
        ZOMBIE_ATTACK_COOLDOWN_TICKS = b.comment("Ticks between individual zombie strikes (20 = 1 second).").defineInRange("attackCooldownTicks",20,10,100);
        b.pop();

        b.push("undead_revamped");
        GENERIC_MONSTER_SEARCH_RADIUS = b.comment("Undead Revamped mob search radius.").defineInRange("searchRadius",24.0,1.0,64.0);
        b.pop();

        b.push("health");
        DEFAULT_BLOCK_HP = b.comment("HP of each solid leg block without an explicit material entry.").defineInRange("defaultBlockHp",8,0,1000);
        MAX_HP_MULTIPLIER = b.comment("Multiply total material-based walker HP while preserving saved HP percentage.").defineInRange("maxHpMultiplier",4.0,1.0,20.0);
        FALLBACK_FOOT_HP = b.comment("HP per leg if Sable leg sublevels are not currently available.").defineInRange("fallbackFootHp",60,1,10000);
        MATERIAL_HP = b.comment("Each entry uses registry_id=hp. This is a per BLOCK contribution, not a one-time bonus.")
            .defineList("materialHp", () -> List.of(
                "let_it_walk:leg_foot=60",
                "let_it_walk:jet_foot=90",
                "let_it_walk:spiked_foot=80",
                "let_it_walk:climbing_foot=80",
                "let_it_walk:leg_hinge=35",
                "let_it_walk:leg_swivel=35",
                "minecraft:iron_block=40",
                "minecraft:copper_block=25",
                "minecraft:diamond_block=70",
                "minecraft:netherite_block=150",
                "minecraft:obsidian=55",
                "create:industrial_iron_block=45",
                "create:brass_block=35"
            ), value -> value instanceof String);
        DAMAGE_MULTIPLIER = b.comment("Legacy damage multiplier, kept for existing 0.2.1 configs.").defineInRange("damageMultiplier",1.0,0.05,20.0);
        INCOMING_DAMAGE_SCALE = b.comment("Additional reduction applied even if damageMultiplier was already saved at 1.0.").defineInRange("incomingDamageScale",0.6,0.05,1.0);
        MAX_DAMAGE_PER_HIT = b.comment("Hard upper limit of HP lost per single monster hit.").defineInRange("maxDamagePerHit",15,1,1000);
        SHARED_HIT_GRACE_TICKS = b.comment("Entire walker takes at most one hit in this many ticks, even against a zombie swarm; 6 ticks = 0.3 sec.").defineInRange("sharedHitGraceTicks",6,0,40);
        SLOW_MULTIPLIER = b.comment("Speed multiplier below 20 percent HP; 0.0 means stopped.").defineInRange("slowMultiplier",0.45,0.0,1.0);
        RECALC_TICKS = b.comment("How often loaded leg part materials are recounted.").defineInRange("rescanTicks",200,20,12000);
        REPAIR_AMOUNT = b.comment("HP restored by each successful right click.").defineInRange("repairAmount",30,1,10000);
        REPAIR_RANGE = b.comment("Maximum repair radius from walker's body in blocks.").defineInRange("repairRange",6,1,16);
        b.pop();

        b.push("leg_stomp");
        LEG_STOMP_ENABLED = b.comment("Only an actively swinging foot can hurt hostile monsters; stationary feet never hurt mobs.").define("enabled",true);
        LEG_STOMP_DAMAGE = b.comment("Damage in HP per foot impact; 4 HP = two hearts.").defineInRange("damage",4.0,0.0,100.0);
        LEG_STOMP_KNOCKBACK = b.comment("Horizontal knockback applied on an actual foot hit.").defineInRange("knockback",0.6,0.0,3.0);
        LEG_STOMP_RADIUS = b.comment("Physical collision radius (blocks) of a moving foot.").defineInRange("radius",0.85,0.25,2.0);
        LEG_STOMP_MIN_MOVEMENT = b.comment("Minimum foot movement per tick required to count as a stomp.").defineInRange("minFootMovement",0.12,0.01,1.5);
        LEG_STOMP_TARGET_COOLDOWN = b.comment("Per-walker and per-target impact cooldown in ticks; 20 ticks = 1 second.").defineInRange("targetCooldownTicks",20,1,100);
        b.pop();

        b.push("general");
        PLAYER_PRESENCE_RANGE = b.comment("No player nearby means walker cannot be targeted.").defineInRange("playerPresenceRange",20.0,0.0,128.0);
        b.pop();
        SPEC = b.build();
    }
}
