package com.nxrtweetg.sexmod.girls.anim;

/**
 * One animation slot, named by what it is used for rather than by whose file it lives in. The
 * suffix is the bare name inside a character's animation json; {@link GirlAnimationSet} turns it
 * into a full {@code animation.<character>.<suffix>} id.
 */
public enum GirlAnimationRole {
   ANAL_CUM("anal_cum", true, 2),
   ANAL_FAST("anal_fast", true, 0),
   ANAL_POSITIONING("anal_prepare", false, 5),
   ANAL_SLOW("anal_slow", true, 2),
   ANAL_START("anal_start", false, 0),
   ANAL_WAIT("anal_wait", true, 0),
   ANAL_ALT_CUM("anal_alt_cum", true, 2),
   ANAL_ALT_FAST("anal_alt_fast", true, 0),
   ANAL_ALT_POSITIONING("anal_alt_prepare", false, 5),
   ANAL_ALT_SLOW("anal_alt_slow", true, 2),
   ANAL_ALT_START("anal_alt_start", false, 0),
   ANAL_ALT_WAIT("anal_alt_wait", true, 0),
   ATTACK0("attack0", false, 0),
   ATTACK1("attack1", false, 0),
   ATTACK2("attack2", false, 0),
   BLOWJOB_CUM("blowjobcum", true, 0),
   BLOWJOB_INTRO("blowjobintro", false, 2),
   BLOWJOB_SUCK("blowjobsuck", true, 2),
   BLOWJOB_THRUST("blowjobthrust", true, 2),
   BOOBJOB_CUM("paizuri_cum", true, 0),
   BOOBJOB_FAST("paizuri_fast", true, 0),
   BOOBJOB_SLOW("paizuri_slow", true, 0),
   BOOBJOB_START("paizuri_start", false, 0),
   BOW("bowcharge", true, 2),
   COWGIRL_CUM("cowgirlcum", true, 2),
   COWGIRL_FAST("cowgirlfast", true, 10),
   COWGIRL_SLOW("cowgirlslow2", true, 10),
   COWGIRL_START("cowgirlstart", true, 0),
   DEEPTHROAT_CUM("deepthroat_cum", true, 2),
   DEEPTHROAT_FAST("deepthroat_fast", true, 2),
   DEEPTHROAT_INTRO("deepthroat_start", false, 0),
   DEEPTHROAT_SLOW("deepthroat_slow", true, 2),
   DOGGY_CUM("doggycum", true, 2),
   DOGGY_FAST("doggyfast", true, 2),
   DOGGY_GO_ON_BED("doggygoonbed", false, 2),
   DOGGY_SLOW("doggyslow", true, 2),
   DOGGY_START("doggystart", false, 0),
   DOGGY_WAIT("doggywait", true, 0),
   DOWNED("downed", true, 7),
   FACEFUCK_CUM("carry_cum", true, 0),
   FACEFUCK_FAST("carry_fast", true, 0),
   FACEFUCK_INTRO("carry_intro", true, 0),
   FACEFUCK_SLOW("carry_slow1", true, 0),
   FACEFUCK_SLOW2("carry_slow2", true, 0),
   FACEFUCK_SLOW3("carry_slow3", true, 0),
   FACEFUCK_SLOW4("carry_slow4", true, 0),
   HAPPY_EYES("fhappy", true),
   IDLE("idle", true),
   KOBOLD_ANAL_CUM("kobold_anal_cum", false, 2),
   KOBOLD_ANAL_FAST("kobold_anal_hard", true, 0),
   KOBOLD_ANAL_SLOW("kobold_anal_soft", true, 0),
   KOBOLD_ANAL_START("kobold_anal_start", false, 0),
   KOBOLD_BLOWJOB_CUM("kobold_blowjob_cum", true),
   KOBOLD_BLOWJOB_FAST("kobold_blowjob_fast", true),
   KOBOLD_BLOWJOB_INTRO("kobold_blowjob_start", false),
   KOBOLD_BLOWJOB_SLOW("kobold_blowjob_slow", true),
   KOBOLD_BLOWJOB_SLOW_R("kobold_blowjob_slow_r", true),
   KOBOLD_BLOWJOB_SWITCH_TO_L("kobold_blowjob_switch_l", false),
   KOBOLD_BLOWJOB_SWITCH_TO_R("kobold_blowjob_switch_r", false),
   MATING_PRESS_CUM("mating_press_cum", false, 2),
   MATING_PRESS_FAST("mating_press_hard", true, 0),
   MATING_PRESS_SLOW("mating_press_soft", true, 0),
   MATING_PRESS_START("mating_press_start", false, 0),
   MISSIONARY_CUM("missionary_cum", true, 2),
   MISSIONARY_FAST("missionary_fast", true, 2),
   MISSIONARY_SLOW("missionary_slow", true, 2),
   MISSIONARY_START("missionary_start", true, 0),
   NELSON_CUM("nelson_cum", true, 0),
   NELSON_FAST("nelson_fastc", true, 0),
   NELSON_INTRO("nelson_intro", false, 0),
   NELSON_SLOW("nelson_slow", true, 0),
   NULL("null", true, 0),
   PAYMENT("payment", false, 5),
   PRONE_DOGGY_CUM("prone_doggy_cum", true, 0),
   PRONE_DOGGY_FAST("prone_doggy_hard1", true, 0),
   PRONE_DOGGY_FAST2("prone_doggy_hard2", true, 0),
   PRONE_DOGGY_FAST3("prone_doggy_hard3", true, 0),
   // prone doggy ships no separate wait take: intro IS the holding loop, and insert loops as well
   // until the phase timer moves her on (original BiaEntity:628-635 builds both with looped=true)
   PRONE_DOGGY_POSITIONING("prone_doggy_intro", true, 0),
   PRONE_DOGGY_SLOW("prone_doggy_soft", true, 0),
   PRONE_DOGGY_START("prone_doggy_insert", true, 2),
   REVERSE_COWGIRL_CUM("reverse_cowgirl_cum", true, 0),
   REVERSE_COWGIRL_FAST1("reverse_cowgirl_fastc1", true, 0),
   REVERSE_COWGIRL_FAST2("reverse_cowgirl_fastc2", true, 0),
   REVERSE_COWGIRL_FAST3("reverse_cowgirl_fastc3", true, 0),
   REVERSE_COWGIRL_FAST("reverse_cowgirl_fasts", true, 0),
   REVERSE_COWGIRL_SLOW("reverse_cowgirl_slow1", true, 0),
   REVERSE_COWGIRL_SLOW2("reverse_cowgirl_slow2", true, 0),
   REVERSE_COWGIRL_SLOW3("reverse_cowgirl_slow3", true, 0),
   REVERSE_COWGIRL_START("reverse_cowgirl_start", true, 0),
   // The bee's one scene (original CITIZEN_START/SLOW/FAST/CUM), whose blend lengths are 0/0/0/2 there.
   // sex_cum is marked looping in her file but her controller plays it once, and the controller wins.
   SEX_CUM("sex_cum", false, 2),
   SEX_FAST("sex_fast", true, 0),
   SEX_SLOW("sex_slow", true, 0),
   SEX_START("sex_start", false, 0),
   SITTING_CUM("sitting_cum", true, 5),
   SITTING_FAST("sitting_fast", true, 5),
   SITTING_SLOW("sitting_slow", true, 5),
   SITTING_START("sitting_intro", false, 0),
   SITTING_ALT_CUM("sitting_alt_cum", true),
   SITTING_ALT_FAST("sitting_alt_hard1", true),
   SITTING_ALT_FAST2("sitting_alt_hard2", true),
   SITTING_ALT_FAST3("sitting_alt_hard3", true),
   SITTING_ALT_POSITIONING("sitting_alt_intro", true),
   SITTING_ALT_SLOW("sitting_alt_soft", true),
   SITTING_ALT_START("sitting_alt_insert", true),
   // the sit-on-the-edge approach: she walks to the end of the bed, sits down once and then holds that
   // pose until he walks up (original fp.SITDOWN / fp.SITDOWNIDLE, shipped by Bia and Ellie)
   SIT_DOWN("sitdown", false, 2),
   SIT_DOWN_IDLE("sitdownidle", true, 0),
   STRIP("strip", false, 5),
   THROW_PEARL("throwpearl", true, 0),
   TOUCH_BOOBS_CUM("touch_boobs_cum", true, 2),
   TOUCH_BOOBS_FAST("touch_boobs_fast", true, 2),
   TOUCH_BOOBS_SLOW("touch_boobs_slow", true, 2),
   TOUCH_BOOBS_SLOW_ALT("touch_boobs_slow1", true, 2),
   TOUCH_BOOBS_START("touch_boobs_intro", false, 0),
   // Luna's stand-in for the sit-down pair: she ships no sitdown take, only this hold (fp.WAIT_CAT),
   // and its own file already ends it on the last frame rather than looping
   THREESOME_SLOW("threesome_slow", true, 0),
   THREESOME_FAST("threesome_fast", true, 0),
   THREESOME_CUM("threesome_cum", true, 2),
   WAIT("wait", false, 0),
   WALK("walk", true);

   /**
    * What the action controller blended with before the per-take numbers were recovered. Roles with
    * no counterpart in the original - our own alternative stagings, the kobold blowjob, and the two
    * roles only the movement and eyes controllers ever play - keep it.
    */
   public static final int DEFAULT_TRANSITION_TICKS = 2;

   private static final java.util.Map<String, GirlAnimationRole> BY_SUFFIX = buildSuffixIndex();

   /**
    * Takes that belong to one character rather than to a shared role, with the blend length their
    * state carried in the original. Bia's talk and both head pats never became roles because nobody
    * else plays them, but they still want their own timing.
    */
   private static final java.util.Map<String, Integer> BESPOKE_TRANSITION_TICKS = java.util.Map.of(
      "talk_horny2", 5,     // TALK_HORNY
      "talk_horny", 5,      // the long take the morph plays
      "talk_idle2", 0,      // TALK_IDLE
      "talk_idle", 0,
      "talk_response", 2,   // TALK_RESPONSE
      "headpat", 0,         // HEAD_PAT, Bia's spelling
      "head_pat", 0         // HEAD_PAT, Luna's spelling
   );

   private final String suffix;
   private final boolean looping;
   private final int transitionTicks;

   GirlAnimationRole(String suffix, boolean looping) {
      this(suffix, looping, DEFAULT_TRANSITION_TICKS);
   }

   GirlAnimationRole(String suffix, boolean looping, int transitionTicks) {
      this.suffix = suffix;
      this.looping = looping;
      this.transitionTicks = transitionTicks;
   }

   public String suffix() {
      return this.suffix;
   }

   public boolean looping() {
      return this.looping;
   }

   /**
    * How many ticks the action controller should blend over when it switches to this take, from the
    * original's per-state {@code transitionTick}. Zero means snap - the intros and climaxes want to
    * start on their own first frame, while the long slow loops want a visible blend.
    */
   public int transitionTicks() {
      return this.transitionTicks;
   }

   /**
    * The role that owns a take, resolved from a full {@code animation.<character>.<suffix>} id, or
    * {@code null} for a take no role names - a character's own bespoke animation, for instance.
    */
   public static GirlAnimationRole bySuffixOf(String animationId) {
      return animationId == null ? null : BY_SUFFIX.get(suffixOf(animationId));
   }

   /**
    * The blend length for any take, named by a role or not, so the action controller has one place to
    * ask. Bespoke takes carry their own state's number; anything the original never had blends over
    * {@link #DEFAULT_TRANSITION_TICKS}.
    */
   public static int transitionTicksFor(String animationId) {
      if (animationId == null) {
         return DEFAULT_TRANSITION_TICKS;
      }

      String suffix = suffixOf(animationId);
      GirlAnimationRole role = BY_SUFFIX.get(suffix);
      return role != null ? role.transitionTicks : BESPOKE_TRANSITION_TICKS.getOrDefault(suffix, DEFAULT_TRANSITION_TICKS);
   }

   private static String suffixOf(String animationId) {
      int dot = animationId.lastIndexOf('.');
      return dot < 0 ? animationId : animationId.substring(dot + 1);
   }

   private static java.util.Map<String, GirlAnimationRole> buildSuffixIndex() {
      java.util.Map<String, GirlAnimationRole> map = new java.util.HashMap<>();
      for (GirlAnimationRole role : values()) {
         // first writer wins; the few suffixes two roles share carry the same timing anyway
         map.putIfAbsent(role.suffix, role);
      }

      return java.util.Map.copyOf(map);
   }
}
