package com.nxrtweetg.sexmod.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * 1.21.11 has neither the normal-forcing hook GeckoLib 5.2 exposes nor a pipeline clone that
 * behaves like 26.x's NO_CARDINAL_LIGHTING variant, so the flat-lit look is out of reach here
 * and the vanilla cutout type is handed back. The seam stays so callers do not change.
 */
@Environment(EnvType.CLIENT)
public final class GirlFlatLitRenderType {
   private GirlFlatLitRenderType() {
   }

   public static net.minecraft.client.renderer.rendertype.RenderType get(Identifier texture) {
      return RenderTypes.entityCutout(texture);
   }
}
