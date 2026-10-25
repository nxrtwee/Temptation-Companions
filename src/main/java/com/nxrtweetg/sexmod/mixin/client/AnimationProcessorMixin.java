package com.nxrtweetg.sexmod.mixin.client;

import java.util.Map;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import software.bernie.geckolib.animatable.processing.AnimationProcessor;
import software.bernie.geckolib.animatable.processing.AnimationState;
import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.cache.object.GeoBone;

/**
 * GeckoLib 5.2 carries poses across frames: bones the current animation does not mention are never
 * rewritten, and the BoneSnapshot offset machinery saves the full bone pose each tick only to feed
 * it back into transition blends - which made every scene transition grow exponentially (the Head2
 * anchor exploded to +/-thousands of blocks within twelve frames). The 5.4+ line removed that whole
 * mechanism; this restores the "clean slate every frame" contract here by resetting every bone to
 * its model pose before the tick writes the animated values, so per-frame rig edits and transition
 * snapshots always start from fresh state.
 */
@Environment(EnvType.CLIENT)
@Mixin(value = AnimationProcessor.class, remap = false)
public abstract class AnimationProcessorMixin {
   @Shadow(remap = false)
   private Map<String, GeoBone> bones;

   @Inject(method = "tickAnimation", at = @At("HEAD"), remap = false)
   private void sexmod$resetBonesToModelPose(AnimationState<?> state, CallbackInfo ci) {
      for (GeoBone bone : this.bones.values()) {
         BoneSnapshot initial = bone.getInitialSnapshot();
         if (initial == null) {
            continue;
         }
         bone.setPosX(initial.getOffsetX());
         bone.setPosY(initial.getOffsetY());
         bone.setPosZ(initial.getOffsetZ());
         bone.setRotX(initial.getRotX());
         bone.setRotY(initial.getRotY());
         bone.setRotZ(initial.getRotZ());
         bone.setScaleX(initial.getScaleX());
         bone.setScaleY(initial.getScaleY());
         bone.setScaleZ(initial.getScaleZ());
      }
   }
}
