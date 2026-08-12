package com.nxrtweetg.sexmod.mixin.client;

import com.nxrtweetg.sexmod.client.GirlSceneInputController;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.CameraType;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The scene owns the view: freecam replaces the perspective toggle, so F5 must not cycle the
 * camera out from under it.
 */
@Environment(EnvType.CLIENT)
@Mixin(Options.class)
abstract class OptionsMixin {
   @Inject(method = "setCameraType", at = @At("HEAD"), cancellable = true)
   private void sexmod$keepSceneCameraType(CameraType cameraType, CallbackInfo ci) {
      if (GirlSceneInputController.shouldBlockGameplayInput()) {
         ci.cancel();
      }
   }
}
