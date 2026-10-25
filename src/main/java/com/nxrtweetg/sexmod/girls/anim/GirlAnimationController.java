package com.nxrtweetg.sexmod.girls.anim;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.state.AnimationPoint;

/**
 * A controller that blends out of the pose that is on screen, the way GeckoLib 3 did.
 *
 * <p>GeckoLib 5 keeps the blend source in {@code transitionFromPoint}, and
 * {@link AnimationController#setAnimation} only fills it in while the outgoing take is still running:
 * {@code transitionFromPoint = getCurrentTimelineTime() > 0 ? animationPoint : null}. A take that has
 * played out parks its timeline time at a negative sentinel, so the swap out of a finished take gets no
 * blend source at all - and {@code AnimationProcessor.findResetPointValue} then eases out of the
 * model's bind pose instead, which is her standing upright. That is the flash the player sees as her
 * dropping out of the scene for a fraction of a second: it lands on every take that plays and holds
 * (the positioning takes, and a beat take whose own {@code *Done} went past) and never on a looping one,
 * which is why it only showed up in some scenes.
 *
 * <p>GeckoLib 3, which the original was written against, blended from the live bone snapshots and so
 * had nothing to lose here. Handing the held pose back as the blend source restores that: the finished
 * take's last frame is exactly what the transition should start from.
 *
 * <p>A deliberately dropped controller is left alone. {@link AnimationController#reset()} clears the
 * pose along with everything else, and a beat that restarts on its own re-entry keyframe is meant to be
 * instant anyway - it asks for a zero-length blend in the same frame.
 */
public class GirlAnimationController<T extends GeoAnimatable> extends AnimationController<T> {
   public GirlAnimationController(String name, int transitionTicks, AnimationStateHandler<T> handler) {
      super(name, transitionTicks, handler);
   }

   @Override
   public void setAnimation(RawAnimation animation) {
      RawAnimation previous = this.currentRawAnimation;
      AnimationPoint onScreen = this.animationPoint;
      super.setAnimation(animation);
      if (this.currentRawAnimation != previous && this.transitionFromPoint == null && onScreen != null) {
         this.transitionFromPoint = onScreen;
      }
   }

   @Override
   protected void initializeNewAnimation(T animatable, software.bernie.geckolib.renderer.base.GeoRenderState renderState, software.bernie.geckolib.model.GeoModel<T> model, double animTime, int transitionTicks) {
      try {
         super.initializeNewAnimation(animatable, renderState, model, animTime, transitionTicks);
      } catch (Exception e) {
         this.reset();
      }
   }
}


