package com.nxrtweetg.sexmod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

/** Universal girl-menu commands: follow me, stop following, go home, set new home, equipment. */
public record GirlCommandPayload(int entityId, String commandId) implements CustomPacketPayload {
   public static final Type<GirlCommandPayload> ID = new Type(ResourceLocation.fromNamespaceAndPath("sexmod", "girl_command"));
   public static final StreamCodec<RegistryFriendlyByteBuf, GirlCommandPayload> CODEC = StreamCodec.composite(
      ByteBufCodecs.INT, GirlCommandPayload::entityId, ByteBufCodecs.STRING_UTF8, GirlCommandPayload::commandId, GirlCommandPayload::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
