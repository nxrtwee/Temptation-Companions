package com.nxrtweetg.sexmod.network;

import com.nxrtweetg.sexmod.home.GirlHomeRegion;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server -> Client payload synchronizing the roster of player-defined homes.
 */
public record SyncHomesPayload(List<GirlHomeRegion> homes) implements CustomPacketPayload {
   public static final Type<SyncHomesPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("sexmod", "sync_homes"));

   public static final StreamCodec<ByteBuf, GirlHomeRegion> REGION_CODEC = StreamCodec.of(
      (buf, r) -> {
         ByteBufCodecs.STRING_UTF8.encode(buf, r.name());
         ByteBufCodecs.STRING_UTF8.encode(buf, r.dimension());
         ByteBufCodecs.INT.encode(buf, r.minX());
         ByteBufCodecs.INT.encode(buf, r.minY());
         ByteBufCodecs.INT.encode(buf, r.minZ());
         ByteBufCodecs.INT.encode(buf, r.maxX());
         ByteBufCodecs.INT.encode(buf, r.maxY());
         ByteBufCodecs.INT.encode(buf, r.maxZ());
      },
      buf -> new GirlHomeRegion(
         ByteBufCodecs.STRING_UTF8.decode(buf),
         ByteBufCodecs.STRING_UTF8.decode(buf),
         ByteBufCodecs.INT.decode(buf),
         ByteBufCodecs.INT.decode(buf),
         ByteBufCodecs.INT.decode(buf),
         ByteBufCodecs.INT.decode(buf),
         ByteBufCodecs.INT.decode(buf),
         ByteBufCodecs.INT.decode(buf)
      )
   );

   public static final StreamCodec<ByteBuf, SyncHomesPayload> CODEC = StreamCodec.composite(
      ByteBufCodecs.collection(ArrayList::new, REGION_CODEC),
      SyncHomesPayload::homes,
      SyncHomesPayload::new
   );

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
