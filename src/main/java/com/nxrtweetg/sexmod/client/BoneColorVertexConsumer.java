package com.nxrtweetg.sexmod.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Forces every vertex onto one ARGB colour, so a bone's procedural tint (kobold tribes, goblin
 * palettes) can ride through the ordinary recursive render pass without a per-bone render type.
 */
@Environment(EnvType.CLIENT)
final class BoneColorVertexConsumer implements VertexConsumer {
   private final VertexConsumer delegate;
   private final int color;

   BoneColorVertexConsumer(VertexConsumer delegate, int color) {
      this.delegate = delegate;
      this.color = color;
   }

   @Override
   public VertexConsumer addVertex(float x, float y, float z) {
      return this.delegate.addVertex(x, y, z);
   }

   @Override
   public VertexConsumer setColor(int r, int g, int b, int a) {
      return this.delegate.setColor((this.color >> 16) & 0xFF, (this.color >> 8) & 0xFF, this.color & 0xFF, (this.color >>> 24) & 0xFF);
   }

   @Override
   public VertexConsumer setUv(float u, float v) {
      return this.delegate.setUv(u, v);
   }

   @Override
   public VertexConsumer setUv1(int u, int v) {
      return this.delegate.setUv1(u, v);
   }

   @Override
   public VertexConsumer setUv2(int u, int v) {
      return this.delegate.setUv2(u, v);
   }

   @Override
   public VertexConsumer setNormal(float x, float y, float z) {
      return this.delegate.setNormal(x, y, z);
   }
}
