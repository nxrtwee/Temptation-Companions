package com.nxrtweetg.sexmod.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * 1.21.x keeps the entity pipeline's shading tame enough that the original's flat-lighting trick -
 * forcing one normal for every quad through {@code GirlEntityRenderer#createVerticesOfQuad} - is all
 * that is needed, so the render type is vanilla's own and this class only stays as the seam the
 * callers already know.
 */
@Environment(EnvType.CLIENT)
public final class GirlFlatLitRenderType {
   private GirlFlatLitRenderType() {
   }

   public static RenderType get(ResourceLocation texture) {
      return RenderType.entityCutout(texture);
   }
}
