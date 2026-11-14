package com.eviesaurr.sablemobinteractions.network;

import com.eviesaurr.sablemobinteractions.client.ClientHudStatus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ModNetworking {
    private ModNetworking() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(
            MechHudStatusPayload.TYPE,
            MechHudStatusPayload.STREAM_CODEC,
            ModNetworking::handleOnClient
        );
    }

    private static void handleOnClient(MechHudStatusPayload payload, IPayloadContext context) {
        // The playToClient handler runs only on the logical client.
        context.enqueueWork(() -> ClientHudStatus.receive(payload.hp(), payload.maxHp()));
    }
}
