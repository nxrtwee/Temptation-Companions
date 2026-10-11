package com.nxrtweetg.sexmod.mixin.client;

import com.geckolib.animation.state.AnimationPoint;
import com.geckolib.cache.animation.BoneAnimation;
import com.geckolib.cache.animation.Keyframe;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(AnimationPoint.class)
abstract class AnimationPointMixin {
   @Inject(method = "getCurrentAndNextKeyframe", at = @At("HEAD"), cancellable = true)
   private void sexmod$fixKeyframeClamping(
      int boneIndex,
      AnimationPoint.Transform transform,
      AnimationPoint.Axis axis,
      int offset,
      CallbackInfoReturnable<Pair<Keyframe, Keyframe>> cir
   ) {
      AnimationPoint self = (AnimationPoint) (Object) this;
      BoneAnimation boneAnimation = self.animation().boneAnimations()[boneIndex];
      Keyframe[] keyframes = axis.keyframes(transform.keyframeStack(boneAnimation));
      int keyframePoint = self.keyFramePoints()[boneIndex][transform.index][axis.index];
      if (keyframes.length == 0 || keyframePoint == -2) {
         cir.setReturnValue(null);
         return;
      }
      int maxIndex = keyframes.length - 1;
      int kf1Index = Mth.clamp(keyframePoint + offset, 0, maxIndex);
      int kf2Index = Mth.clamp(keyframePoint + offset + 1, 0, maxIndex);
      cir.setReturnValue(Pair.of(keyframes[kf1Index], keyframes[kf2Index]));
   }
}
