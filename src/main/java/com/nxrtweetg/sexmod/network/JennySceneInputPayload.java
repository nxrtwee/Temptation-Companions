package com.nxrtweetg.sexmod.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record JennySceneInputPayload(int entityId, String inputId) implements CustomPacketPayload {
   public static final Type<JennySceneInputPayload> ID = new Type(Identifier.fromNamespaceAndPath("sexmod", "jenny_scene_input"));
   public static final StreamCodec<RegistryFriendlyByteBuf, JennySceneInputPayload> CODEC = StreamCodec.composite(
      ByteBufCodecs.INT, JennySceneInputPayload::entityId, ByteBufCodecs.STRING_UTF8, JennySceneInputPayload::inputId, JennySceneInputPayload::new
   );

   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
