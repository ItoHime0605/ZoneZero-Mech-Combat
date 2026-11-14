package com.eviesaurr.sablemobinteractions.network;

import com.eviesaurr.sablemobinteractions.SableMobInteractions;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server-authoritative HP for the walker the player is actively piloting. */
public record MechHudStatusPayload(int hp, int maxHp) implements CustomPacketPayload {
    public static final Type<MechHudStatusPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(SableMobInteractions.MOD_ID, "mech_hud_status"));

    public static final StreamCodec<ByteBuf, MechHudStatusPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MechHudStatusPayload::hp,
            ByteBufCodecs.VAR_INT, MechHudStatusPayload::maxHp,
            MechHudStatusPayload::new
        );

    @Override
    public Type<MechHudStatusPayload> type() { return TYPE; }
}
