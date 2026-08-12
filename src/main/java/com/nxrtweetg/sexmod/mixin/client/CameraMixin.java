package com.nxrtweetg.sexmod.mixin.client;

import com.nxrtweetg.sexmod.client.GirlSceneCameraController;
import com.nxrtweetg.sexmod.client.GirlSceneCameraState;
import com.nxrtweetg.sexmod.client.GirlSceneFreecam;
import com.nxrtweetg.sexmod.client.GirlSceneFreeLook;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Camera.class)
abstract class CameraMixin {
   @Shadow
   private Vec3 position;

   @Shadow
   protected abstract void setPosition(Vec3 position);

   @Shadow
   protected abstract void setRotation(float yRot, float xRot);

   @Shadow
   public abstract Entity entity();

   @Shadow
   public abstract float yRot();

   @Shadow
   public abstract float xRot();

   // Must land before Camera#update builds the view matrix and calls prepareCullFrustum, since the
   // frustum is prepared from the position field: overriding on TAIL left culling computed around
   // the player, which showed up as sliced-away geometry once the scene camera moved away from him.
   @Inject(
      method = "update",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/Camera;getViewRotationMatrix(Lorg/joml/Matrix4f;)Lorg/joml/Matrix4f;",
         shift = Shift.BEFORE
      )
   )
   private void sexmod$anchorJennySceneCamera(DeltaTracker deltaTracker, CallbackInfo ci) {
      // the renderer refreshes the anchor every frame while the controller clears it per tick, so
      // without this gate the view alternates between the scene anchor and the player's own eyes
      if (!GirlSceneCameraController.isSceneCameraActive()) {
         return;
      }

      Entity focusedEntity = this.entity();
      if (focusedEntity != null) {
         GirlSceneFreecam.activePosition().or(GirlSceneCameraState::activeAnchor).ifPresent(this::setPosition);
         // the player entity stays pinned by the scene, so free-look lives on the camera alone.
         // Yaw wraps freely; pitch is clamped once here, against the combined value.
         this.setRotation(
            Mth.wrapDegrees(this.yRot() + GirlSceneFreeLook.yawOffset()), Mth.clamp(this.xRot() + GirlSceneFreeLook.pitchOffset(), -90.0F, 90.0F)
         );
      }
   }
}
