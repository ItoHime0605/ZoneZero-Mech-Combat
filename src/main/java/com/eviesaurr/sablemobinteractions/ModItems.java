package com.eviesaurr.sablemobinteractions;

import com.eviesaurr.sablemobinteractions.item.RepairToolItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private ModItems() {}
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(Registries.ITEM, SableMobInteractions.MOD_ID);
    public static final DeferredHolder<Item, Item> REPAIR_TOOL =
        ITEMS.register("repair_tool", () -> new RepairToolItem(new Item.Properties().durability(128)));
}
