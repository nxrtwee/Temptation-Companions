package com.nxrtweetg.sexmod.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * On 1.21.6..1.21.8 the flat-lit look comes from the original's own trick - forcing one
 * normal for every quad through GirlEntityRenderer#createVerticesOfQuad - so the render
 * type is vanilla's own and this class only stays as the seam the callers already know.
 */
@Environment(EnvType.CLIENT)
public final class GirlFlatLitRenderType {
   private GirlFlatLitRenderType() {
   }

   public static RenderType get(ResourceLocation texture) {
      return RenderType.entityCutout(texture);
   }
}
