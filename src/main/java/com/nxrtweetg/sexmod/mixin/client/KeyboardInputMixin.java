package com.nxrtweetg.sexmod.mixin.client;

import com.nxrtweetg.sexmod.client.GirlSceneInputController;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The original swallows the whole movement input while a scene runs (d3_class161 on
 * InputUpdateEvent): sneak and jump drive the scene, so they must not also make the player
 * crouch or hop.
 */
@Environment(EnvType.CLIENT)
@Mixin(KeyboardInput.class)
abstract class KeyboardInputMixin extends ClientInput {
   @Inject(method = "tick", at = @At("TAIL"))
   private void sexmod$swallowSceneInput(CallbackInfo ci) {
      if (GirlSceneInputController.shouldSwallowMovementInput()) {
         this.keyPresses = net.minecraft.world.entity.player.Input.EMPTY;
         this.leftImpulse = 0.0F;
         this.forwardImpulse = 0.0F;
      }
   }
}
