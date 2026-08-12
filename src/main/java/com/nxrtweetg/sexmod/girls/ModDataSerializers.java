package com.nxrtweetg.sexmod.girls;

import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;

public final class ModDataSerializers {
   public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID = EntityDataSerializer.forValueType(
      ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC)
   );

   private ModDataSerializers() {
   }

   public static void register() {
      FabricEntityDataRegistry.register(Identifier.fromNamespaceAndPath("sexmod", "optional_uuid"), OPTIONAL_UUID);
   }
}
