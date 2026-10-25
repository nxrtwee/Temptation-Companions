package com.nxrtweetg.sexmod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client -> Server payload executing home management actions (create, assign, delete, stay, query).
 */
public record HomeActionPayload(
   String action,
   int entityId,
   String homeName,
   int x1,
   int y1,
   int z1,
   int x2,
   int y2,
   int z2
) implements CustomPacketPayload {
   public static final Type<HomeActionPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath("sexmod", "home_action"));

   public static final StreamCodec<ByteBuf, HomeActionPayload> CODEC = StreamCodec.of(
      (buf, p) -> {
         ByteBufCodecs.STRING_UTF8.encode(buf, p.action());
         ByteBufCodecs.INT.encode(buf, p.entityId());
         ByteBufCodecs.STRING_UTF8.encode(buf, p.homeName());
         ByteBufCodecs.INT.encode(buf, p.x1());
         ByteBufCodecs.INT.encode(buf, p.y1());
         ByteBufCodecs.INT.encode(buf, p.z1());
         ByteBufCodecs.INT.encode(buf, p.x2());
         ByteBufCodecs.INT.encode(buf, p.y2());
         ByteBufCodecs.INT.encode(buf, p.z2());
      },
      buf -> new HomeActionPayload(
         ByteBufCodecs.STRING_UTF8.decode(buf),
         ByteBufCodecs.INT.decode(buf),
         ByteBufCodecs.STRING_UTF8.decode(buf),
         ByteBufCodecs.INT.decode(buf),
         ByteBufCodecs.INT.decode(buf),
         ByteBufCodecs.INT.decode(buf),
         ByteBufCodecs.INT.decode(buf),
         ByteBufCodecs.INT.decode(buf),
         ByteBufCodecs.INT.decode(buf)
      )
   );

   public static HomeActionPayload createHome(String name, int x1, int y1, int z1, int x2, int y2, int z2) {
      return new HomeActionPayload("create", -1, name, x1, y1, z1, x2, y2, z2);
   }

   public static HomeActionPayload assignHome(int entityId, String name) {
      return new HomeActionPayload("assign", entityId, name, 0, 0, 0, 0, 0, 0);
   }

   public static HomeActionPayload deleteHome(String name) {
      return new HomeActionPayload("delete", -1, name, 0, 0, 0, 0, 0, 0);
   }

   public static HomeActionPayload stayHome(int entityId) {
      return new HomeActionPayload("stay", entityId, "", 0, 0, 0, 0, 0, 0);
   }

   public static HomeActionPayload query() {
      return new HomeActionPayload("query", -1, "", 0, 0, 0, 0, 0, 0);
   }

   public static HomeActionPayload showBoundary(String name) {
      return new HomeActionPayload("show_boundary", -1, name, 0, 0, 0, 0, 0, 0);
   }

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
