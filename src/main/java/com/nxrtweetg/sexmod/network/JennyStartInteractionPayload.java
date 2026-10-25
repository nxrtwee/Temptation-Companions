package com.nxrtweetg.sexmod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

public record JennyStartInteractionPayload(int entityId, String actionId) implements CustomPacketPayload {
   public static final Type<JennyStartInteractionPayload> ID = new Type(ResourceLocation.fromNamespaceAndPath("sexmod", "jenny_start_interaction"));
   public static final StreamCodec<RegistryFriendlyByteBuf, JennyStartInteractionPayload> CODEC = StreamCodec.composite(
      ByteBufCodecs.INT,
      JennyStartInteractionPayload::entityId,
      ByteBufCodecs.STRING_UTF8,
      JennyStartInteractionPayload::actionId,
      JennyStartInteractionPayload::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
