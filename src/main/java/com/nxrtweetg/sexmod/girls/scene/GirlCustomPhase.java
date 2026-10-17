package com.nxrtweetg.sexmod.girls.scene;

/**
 * The phases of a custom interaction - the small machine behind the options that are not
 * {@link GirlSceneAction}s (Luna's headpat, Bia's talk).
 *
 * <p>The original ran these through the same state field as the sex scenes, with the state's
 * {@code fp} metadata deciding whether a partner was drawn and what followed. We keep them on their
 * own field so the built-in scene machine stays untouched, but the shape is the same: glide to the
 * player, play the take, and let the animation's own keyframe end it.
 */
public enum GirlCustomPhase {
   IDLE("idle"),
   /** The 40-tick glide to the point captured when the interaction started. */
   APPROACH("approach"),
   /** The take is playing. Ends on its keyframe; the measured length is only a watchdog. */
   ACTIVE("active"),
   /** She holds a looping pose - Bia after her talk, while the follow-up menu is open. */
   WAITING("waiting");

   private final String id;

   GirlCustomPhase(String id) {
      this.id = id;
   }

   public String id() {
      return this.id;
   }

   public static GirlCustomPhase fromId(String id) {
      return switch (id) {
         case "approach" -> APPROACH;
         case "active" -> ACTIVE;
         case "waiting" -> WAITING;
         default -> IDLE;
      };
   }
}
