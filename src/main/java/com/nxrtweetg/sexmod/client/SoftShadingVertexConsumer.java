package com.nxrtweetg.sexmod.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

/** Applies {@link GirlSoftShading} to every vertex the model emits. */
@Environment(EnvType.CLIENT)
final class SoftShadingVertexConsumer implements VertexConsumer {
   private final VertexConsumer delegate;
   private final Vec3 lightDirection;

   SoftShadingVertexConsumer(VertexConsumer delegate, Vec3 lightDirection) {
      this.delegate = delegate;
      this.lightDirection = lightDirection;
   }

   @Override
   public void addVertex(
      float x, float y, float z, int color, float u, float v, int packedOverlay, int packedLight, float normalX, float normalY, float normalZ
   ) {
      int tinted = GirlSoftShading.tint(color, normalX, normalY, normalZ, this.lightDirection);
      this.delegate.addVertex(x, y, z, tinted, u, v, packedOverlay, packedLight, normalX, normalY, normalZ);
   }

   @Override
   public VertexConsumer addVertex(float x, float y, float z) {
      return this.delegate.addVertex(x, y, z);
   }

   @Override
   public VertexConsumer setColor(int red, int green, int blue, int alpha) {
      return this.delegate.setColor(red, green, blue, alpha);
   }

   @Override
   public VertexConsumer setColor(int color) {
      return this.delegate.setColor(color);
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

   @Override
   public VertexConsumer setLineWidth(float width) {
      return this.delegate.setLineWidth(width);
   }

   @Override
   public VertexConsumer addVertex(Vector3fc position) {
      return this.delegate.addVertex(position);
   }
}
