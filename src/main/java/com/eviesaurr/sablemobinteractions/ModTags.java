package com.eviesaurr.sablemobinteractions;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags {
    /** A Sable structure is attackable only if it contains a Let It Walk! walker core. */
    public static final TagKey<Block> SHIP_ESSENTIAL_BLOCKS = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(SableMobInteractions.MOD_ID, "ship_essential_blocks")
    );
}
