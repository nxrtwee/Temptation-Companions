package com.nxrtweetg.sexmod.girls.anim;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.processing.AnimationController;

/**
 * On GeckoLib 5.2 the transition source is the saved bone-snapshot of the pose that is on screen -
 * the GeckoLib 3 behaviour the original was written against - so the blend fix the 5.4+ builds carry
 * (handing the held pose back as the transition source) has nothing to do here and the controller is
 * only kept as the shared construction seam.
 */
public class GirlAnimationController<T extends GeoAnimatable> extends AnimationController<T> {
   public GirlAnimationController(String name, int transitionTicks, AnimationStateHandler<T> handler) {
      super(name, transitionTicks, handler);
   }
}
