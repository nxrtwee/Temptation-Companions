package com.nxrtweetg.sexmod.home;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

/**
 * Represents a defined cuboid territory for a home region.
 */
public record GirlHomeRegion(
   String name,
   String dimension,
   int minX,
   int minY,
   int minZ,
   int maxX,
   int maxY,
   int maxZ
) {
   public static final Codec<GirlHomeRegion> CODEC = RecordCodecBuilder.create(instance ->
      instance.group(
         Codec.STRING.fieldOf("name").forGetter(GirlHomeRegion::name),
         Codec.STRING.fieldOf("dimension").forGetter(GirlHomeRegion::dimension),
         Codec.INT.fieldOf("minX").forGetter(GirlHomeRegion::minX),
         Codec.INT.fieldOf("minY").forGetter(GirlHomeRegion::minY),
         Codec.INT.fieldOf("minZ").forGetter(GirlHomeRegion::minZ),
         Codec.INT.fieldOf("maxX").forGetter(GirlHomeRegion::maxX),
         Codec.INT.fieldOf("maxY").forGetter(GirlHomeRegion::maxY),
         Codec.INT.fieldOf("maxZ").forGetter(GirlHomeRegion::maxZ)
      ).apply(instance, GirlHomeRegion::new)
   );

   public static GirlHomeRegion create(String name, String dimension, BlockPos pos1, BlockPos pos2) {
      return new GirlHomeRegion(
         name,
         dimension,
         Math.min(pos1.getX(), pos2.getX()),
         Math.min(pos1.getY(), pos2.getY()),
         Math.min(pos1.getZ(), pos2.getZ()),
         Math.max(pos1.getX(), pos2.getX()),
         Math.max(pos1.getY(), pos2.getY()),
         Math.max(pos1.getZ(), pos2.getZ())
      );
   }

   public BlockPos center() {
      return new BlockPos(
         (minX + maxX) / 2,
         minY,
         (minZ + maxZ) / 2
      );
   }

   public int sizeX() {
      return maxX - minX + 1;
   }

   public int sizeY() {
      return maxY - minY + 1;
   }

   public int sizeZ() {
      return maxZ - minZ + 1;
   }

   public boolean contains(BlockPos pos) {
      return pos.getX() >= minX && pos.getX() <= maxX
          && pos.getY() >= minY && pos.getY() <= maxY
          && pos.getZ() >= minZ && pos.getZ() <= maxZ;
   }

   public boolean contains(double x, double y, double z) {
      return x >= minX && x <= maxX + 1.0
          && y >= minY && y <= maxY + 1.0
          && z >= minZ && z <= maxZ + 1.0;
   }
}
