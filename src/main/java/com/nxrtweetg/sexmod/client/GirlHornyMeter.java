package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.girls.scene.GirlScenePhase;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * The scene arousal meter of the 1.12.2 original ({@code ds_class200}). Purely client-side state:
 * animation keyframes feed it, and a full meter is what unlocks the climax input.
 */
@Environment(EnvType.CLIENT)
public final class GirlHornyMeter {
   private static boolean visible;
   private static boolean showThrustButton;
   private static boolean finishing;
   private static double fill;
   private static double displayedFill;
   private static float slideIn;
   private static float finishSlide;

   private GirlHornyMeter() {
   }

   public static void show(boolean withThrustButton) {
      if (!visible) {
         reset();
         visible = true;
         showThrustButton = withThrustButton;
      }
   }

   public static void hide() {
      reset();
      visible = false;
      showThrustButton = true;
   }

   public static boolean isVisible() {
      return visible;
   }

   public static boolean isFull() {
      return fill >= 1.0;
   }

   public static boolean isFinishing() {
      return finishing;
   }

   public static void markFinishing() {
      finishing = true;
   }

   public static void add(double amount) {
      fill = Math.min(1.0, fill + amount);
   }

   /**
    * Some ported scenes still lack per-animation horny-meter keyframes, so while they are in their
    * interactive loop we advance the bar from tracked scene state the same way the original meter
    * felt: slow movement fills gradually, thrusting fills faster.
    */
   public static void tickAutoFill(SceneGirlEntity girl, float deltaTicks) {
      if (!visible || finishing || deltaTicks <= 0.0F || girl.trackedScenePhase() != GirlScenePhase.ACTIVE) {
         return;
      }

      GirlSceneAction action = girl.trackedActiveAction().orElse(null);
      if (action == null) {
         return;
      }

      double perTick = switch (action) {
         case PRONE_DOGGY -> girl.isThrustingTracked() ? 0.005 : 0.0025;
         case SITTING_ALT -> girl.isThrustingTracked() ? 0.005 : 0.0025;
         case ANAL, ANAL_ALT -> girl.isThrustingTracked() ? 0.0045 : 0.0022;
         case KOBOLD_ANAL -> girl.isThrustingTracked() ? 0.004 : 0.002;
         case SITTING, MISSIONARY, REVERSE_COWGIRL, COWGIRL, THREESOME -> girl.isThrustingTracked() ? 0.0045 : 0.0024;
         case MATING_PRESS -> girl.isThrustingTracked() ? 0.0055 : 0.0028;
         case NELSON -> girl.isThrustingTracked() ? 0.0045 : 0.0024;
         case FACEFUCK -> girl.isThrustingTracked() ? 0.0045 : 0.0024;
         case DEEPTHROAT -> girl.isThrustingTracked() ? 0.004 : 0.0022;
         case TOUCH_BOOBS -> girl.isThrustingTracked() ? 0.0035 : 0.0018;
         // The bee's own take carries the original's meter keyframes (0.02 slow, 0.04 fast), so nothing
         // has to stand in for them here.
         case BLOWJOB, KOBOLD_BLOWJOB, BOOBJOB, DOGGY, BEE_SEX, STRIP, HEAD_PAT -> 0.0;
      };
      if (perTick > 0.0) {
         add(perTick * deltaTicks);
      }
   }

   /** Clears the fill without hiding the meter, as the original did between phases. */
   public static void reset() {
      fill = 0.0;
      finishing = false;
   }

   public static double fill() {
      return fill;
   }

   public static boolean showThrustButton() {
      return showThrustButton;
   }

   public static double displayedFill() {
      return displayedFill;
   }

   public static void setDisplayedFill(double value) {
      displayedFill = value;
   }

   public static float slideIn() {
      return slideIn;
   }

   public static void setSlideIn(float value) {
      slideIn = value;
   }

   public static float finishSlide() {
      return finishSlide;
   }

   public static void setFinishSlide(float value) {
      finishSlide = value;
   }

   public static void resetAnimation() {
      slideIn = 0.0F;
      finishSlide = 0.0F;
      displayedFill = 0.0;
   }
}
