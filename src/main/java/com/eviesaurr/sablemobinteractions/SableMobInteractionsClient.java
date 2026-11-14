package com.eviesaurr.sablemobinteractions;

import com.eviesaurr.sablemobinteractions.client.MechHudOverlay;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = SableMobInteractions.MOD_ID, dist = Dist.CLIENT)

@EventBusSubscriber(modid = SableMobInteractions.MOD_ID, value = Dist.CLIENT)
public class SableMobInteractionsClient {
    public SableMobInteractionsClient(IEventBus modEventBus, ModContainer container) {

        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(MechHudOverlay::registerLayers);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        SableMobInteractions.LOGGER.info("ZoneZero Mech Combat client setup complete");
    }
}