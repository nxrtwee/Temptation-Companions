package com.nxrtweetg.sexmod.mixin.client;

import com.nxrtweetg.sexmod.client.GirlSceneInputController;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The scene camera replaces the player's presence entirely, so first-person hands and held items
 * are hidden until the scene ends. Scenes force third person, where vanilla skips this call
 * anyway; the mixin covers the player toggling back to first person mid-scene.
 */
@Environment(EnvType.CLIENT)
@Mixin(FirstPersonHandsAndItemsRenderer.class)
abstract class ItemInHandRendererMixin {
   @Inject(method = "submitHandsWithItems", at = @At("HEAD"), cancellable = true)
   private void sexmod$hideHandsDuringScenes(CallbackInfo ci) {
      if (GirlSceneInputController.shouldHideFirstPersonHands()) {
         ci.cancel();
      }
   }
}
