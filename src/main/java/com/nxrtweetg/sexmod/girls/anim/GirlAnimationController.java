package com.nxrtweetg.sexmod.girls.anim;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationController.AnimationStateHandler;

/**
 * The shared construction seam. GeckoLib 4 already blends out of the saved bone-snapshot of the pose
 * on screen - the GeckoLib 3 behaviour the original was written against - so the 5.4+ blend fix has
 * nothing to do here.
 */
public class GirlAnimationController<T extends GeoAnimatable> extends AnimationController<T> {
   public GirlAnimationController(T animatable, String name, int transitionTicks, AnimationStateHandler<T> handler) {
      super(animatable, name, transitionTicks, handler);
   }
}
