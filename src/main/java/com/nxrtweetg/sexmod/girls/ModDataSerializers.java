package com.nxrtweetg.sexmod.girls;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;

public final class ModDataSerializers {
   public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID = EntityDataSerializer.forValueType(
      ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC)
   );

   private ModDataSerializers() {
   }

   public static void register() {
      EntityDataSerializers.registerSerializer(OPTIONAL_UUID);
   }
}
