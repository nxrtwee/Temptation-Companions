package com.nxrtweetg.sexmod.girls.scene;

import java.util.HashMap;
import java.util.Map;

/**
 * What a structural keyframe means for the scene machine.
 *
 * <p>The original had no scene timers at all: every transition hung off a sound keyframe in the
 * animation file, so a beat could never be cut short and never overran. Keyframes only fire on the
 * client, so the owning client reports the structural ones and the server uses them as the authority
 * for the transition. The per-phase tick budgets stay behind as a watchdog, for animations that ship
 * no such keyframe and for a client that stops reporting.
 *
 * <p>Names come straight from the animation files, spelling inconsistencies included - the same beat
 * is {@code cowgirlcumDone} in one file and {@code cowgirl_cumDone} in another.
 */
public enum GirlSceneKeyframe {
   /**
    * The take's re-entry point: {@code *Ready}, and the {@code doggyReset}/{@code doggySwitch}/
    * {@code fastSwitch} trio that spell the same thing.
    *
    * <p>While the pace key is held the beat restarts here and the rest of the take is deliberately
    * never played - that is what makes a thrust land on the key rather than a take's length later. A
    * key that is up by then lets the take run on to its own {@link #BEAT_END} instead, which is the
    * only way back to the slow loop.
    */
   BEAT_HOLD,
   /** {@code *fastDone}: the beat played out with the key up. Drops back to the slow loop. */
   BEAT_END,
   /** {@code *startDone}, {@code *introDone}: the intro played out, the beat can start. */
   INTRO_END,
   /** {@code doggyGoOnBedDone}, {@code anal_prepareDone}: she is in place and now waits for the player. */
   POSITIONING_END,
   /** {@code paymentDone}: she has pocketed the payment. */
   PAYMENT_END,
   /** {@code *cumDone}: the scene is over. */
   CLIMAX_END,
   /** {@code becomeNude}: the moment the stripped model swaps in. */
   STRIP_REVEAL,
   /** {@code stripDone}: the strip animation finished. */
   STRIP_END,
   /** Cameras, fades, UI toggles, held items. These never leave the client. */
   CLIENT_ONLY;

   private static final Map<String, GirlSceneKeyframe> TABLE = build();

   /** The meaning of one keyframe, or null when it carries no structure (dialogue and sound cues). */
   public static GirlSceneKeyframe of(String keyframe) {
      return TABLE.get(keyframe);
   }

   /** True when the server has to hear about this keyframe for the scene to move on. */
   public boolean needsServer() {
      return this != CLIENT_ONLY;
   }

   private static Map<String, GirlSceneKeyframe> build() {
      Map<String, GirlSceneKeyframe> table = new HashMap<>();

      put(table, INTRO_END, "bjiDone", "blowjobStartDone", "deepthroat_startDone", "paizuri_startDone",
         "touch_boobs_introDone", "doggystartDone", "anal_startDone", "analStartDone", "sitting_introDone",
         "missionary_startDone", "mating_press_startDone", "cowgirlStartDone", "sex_startDone");
      // The three names below are re-entry points that do not follow the *Ready spelling, which is why
      // they used to fall through this table as "no structure at all" - and with no re-entry signal
      // those beats had nothing but their tick budget, so a thrust took a whole take to land. They sit
      // in: prone_doggy_hard1/2/3 and sitting_alt_hard1/2/3 (doggyReset), prone_doggy_soft and
      // sitting_alt_soft (doggySwitch, the entry into the beat from the slow loop), and every reverse
      // cowgirl fast take (fastSwitch, which is the only structural keyframe those takes carry).
      put(table, BEAT_HOLD, "bjtReady", "paizuriReady", "doggyfastReady", "missionary_fastReady",
         "mating_press_softReady", "mating_press_hardReady", "cowgirlfastReady", "sex_fastReady",
         "doggySwitch", "fastSwitch");
      put(table, BEAT_END, "fastDone", "blowjobFastDone", "bjtDone", "deepthroat_fastDone", "paizuri_fastDone",
         "touch_boobs_slowDone", "carry_slowDone", "carry_fastDone", "doggyfastDone", "anal_fastDone",
         "sitting_fastDone", "missionary_fastDone", "mating_press_hardDone", "cowgirlfastDone",
         // analDone ends kobold_anal_hard, whose whole length it sits at. It fires again a fifth of the
         // way into kobold_anal_cum, where it is a cue and not an end: reading it as the climax's end
         // cut that scene off at 27 of its 102 ticks.
         "cowgirlfastdomDone", "sex_fastDone", "analDone");
      put(table, CLIMAX_END, "bjcDone", "blowjobCumDone", "deepthroat_cumDone", "paizuri_cumDone", "touch_boobs_cumDone",
         "carry_cumDone", "doggyCumDone", "doggy_cumDone", "anal_cumDone", "analCumDone",
         "missionary_cumDone", "mating_press_cumDone", "cowgirlcumDone", "cowgirl_cumDone", "sex_cumDone");
      // sitdownDone is the sit-on-the-edge positioning take's own end; without it those five scenes
      // handed over on the watchdog instead, a fifth of a second after she had finished sitting.
      put(table, POSITIONING_END, "doggyGoOnBedDone", "anal_prepareDone", "sitdownDone");
      put(table, PAYMENT_END, "paymentDone");
      put(table, STRIP_REVEAL, "becomeNude");
      put(table, STRIP_END, "stripDone");
      put(table, CLIENT_ONLY, "switch", "endSwitch", "idle", "drawItems", "removeItems", "startStrip",
         "renderEgg", "pearl", "attackDone", "paizuri_startStep", "sitting_fastTp", "cowgirlSlowDone",
         "openSexUI", "openSexUi", "sexUI", "sexUiOn", "boobjob_camera", "carry_introCam", "carry_introCam2",
         "matingCam", "mating_cum_cam", "analStartCam", "bjcBlackScreen", "blackScreen", "blackscreen",
         "doggyReset");

      return table;
   }

   private static void put(Map<String, GirlSceneKeyframe> table, GirlSceneKeyframe signal, String... names) {
      for (String name : names) {
         table.put(name, signal);
      }
   }
}
