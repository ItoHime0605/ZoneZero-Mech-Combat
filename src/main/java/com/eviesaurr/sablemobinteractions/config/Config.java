package com.eviesaurr.sablemobinteractions.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** ZoneZero-specific settings; vehicle, creeper, skeleton and Enderman features removed. */
public class Config {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue ZOMBIE_SEARCH_RADIUS;
    public static final ModConfigSpec.DoubleValue ZOMBIE_ATTACK_RANGE;
    public static final ModConfigSpec.DoubleValue GENERIC_MONSTER_SEARCH_RADIUS;
    public static final ModConfigSpec.IntValue BASE_HITS_TO_BREAK;
    public static final ModConfigSpec.DoubleValue PLAYER_PRESENCE_RANGE;

    static {
        var b = new ModConfigSpec.Builder();
        b.push("zombie");
        ZOMBIE_SEARCH_RADIUS = b
                .comment("How far vanilla zombie variants search for walker armor blocks.")
                .translation("zonezero_mechcombat.configuration.zombie.searchRadius")
                .defineInRange("searchRadius", 24.0, 1.0, 64.0);
        ZOMBIE_ATTACK_RANGE = b
                .comment("How close a vanilla zombie must be to hit a walker armor block.")
                .translation("zonezero_mechcombat.configuration.zombie.attackRange")
                .defineInRange("attackRange", 2.0, 0.5, 8.0);
        b.pop();

        b.push("undead_revamped");
        GENERIC_MONSTER_SEARCH_RADIUS = b
                .comment("How far The Undead Revamped mobs search for walker structures.")
                .translation("zonezero_mechcombat.configuration.undead_revamped.searchRadius")
                .defineInRange("searchRadius", 24.0, 1.0, 64.0);
        b.pop();

        b.push("block_breaking");
        BASE_HITS_TO_BREAK = b
                .comment("Hits required to break an armor block; walker core is never broken by this prototype.")
                .translation("zonezero_mechcombat.configuration.block_breaking.baseHitsToBreak")
                .defineInRange("baseHitsToBreak", 16, 1, 200);
        b.pop();

        b.push("general");
        PLAYER_PRESENCE_RANGE = b
                .comment("A player must be near the walker. Set 0 to disable this guard.")
                .translation("zonezero_mechcombat.configuration.general.playerPresenceRange")
                .defineInRange("playerPresenceRange", 20.0, 0.0, 128.0);
        b.pop();
        SPEC = b.build();
    }
}
