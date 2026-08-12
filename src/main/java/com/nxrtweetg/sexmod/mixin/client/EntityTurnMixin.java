package com.nxrtweetg.sexmod.mixin.client;

import com.nxrtweetg.sexmod.client.GirlSceneCameraController;
import com.nxrtweetg.sexmod.client.GirlSceneFreeLook;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * During a scene the mouse drives the camera instead of the player: turning the entity would rotate
 * the scene itself, since both the camera anchor and Jenny's facing follow the player's yaw.
 */
@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
abstract class EntityTurnMixin {
   @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
   private void sexmod$divertLookToCamera(double yawDelta, double pitchDelta, CallbackInfo ci) {
      if (GirlSceneCameraController.isSceneCameraActive() && (Object)this == Minecraft.getInstance().player) {
         GirlSceneFreeLook.applyMouseDelta(yawDelta, pitchDelta);
         ci.cancel();
      }
   }
}
