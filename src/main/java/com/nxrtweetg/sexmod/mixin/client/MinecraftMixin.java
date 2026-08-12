package com.nxrtweetg.sexmod.mixin.client;

import com.nxrtweetg.sexmod.client.GirlSceneInputController;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Scene inputs own attack/use while a scene runs. Cancelling all of {@code handleKeybinds} would
 * also swallow the camera toggle, inventory and hotbar, which vanilla drives from that same method,
 * so only the world-interaction entry points are blocked.
 */
@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
abstract class MinecraftMixin {
   @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
   private void sexmod$blockSceneAttack(CallbackInfoReturnable<Boolean> cir) {
      if (GirlSceneInputController.shouldBlockGameplayInput()) {
         cir.setReturnValue(false);
      }
   }

   @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
   private void sexmod$blockSceneContinueAttack(boolean leftClickHeld, CallbackInfo ci) {
      if (GirlSceneInputController.shouldBlockGameplayInput()) {
         ci.cancel();
      }
   }

   @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
   private void sexmod$blockSceneUseItem(CallbackInfo ci) {
      if (GirlSceneInputController.shouldBlockGameplayInput()) {
         ci.cancel();
      }
   }

   @Inject(method = "pickBlockOrEntity", at = @At("HEAD"), cancellable = true)
   private void sexmod$blockScenePickBlock(CallbackInfo ci) {
      if (GirlSceneInputController.shouldBlockGameplayInput()) {
         ci.cancel();
      }
   }
}
