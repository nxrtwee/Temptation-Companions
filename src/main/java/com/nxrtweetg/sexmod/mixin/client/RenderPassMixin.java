package com.nxrtweetg.sexmod.mixin.client;

import com.mojang.renderpearl.frontend.FrontendRenderPass;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Modern Minecraft's FrontendRenderPass.enableScissor throws IllegalArgumentException if width <= 0 or height <= 0.
 * In GUI rendering, sliding widgets or off-screen marquee text can produce empty/negative scissor boxes when
 * clipped to window bounds. This mixin gracefully disables scissor instead of crashing the client.
 */
@Environment(EnvType.CLIENT)
@Mixin(FrontendRenderPass.class)
abstract class RenderPassMixin {
   @Shadow
   public abstract void disableScissor();

   @Inject(method = "enableScissor", at = @At("HEAD"), cancellable = true)
   private void sexmod$preventZeroScissorCrash(int x, int y, int width, int height, CallbackInfo ci) {
      if (width <= 0 || height <= 0) {
         this.disableScissor();
         ci.cancel();
      }
   }
}
