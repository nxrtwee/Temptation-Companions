package com.nxrtweetg.sexmod.girls.anim;

/**
 * One animation slot, named by what it is used for rather than by whose file it lives in. The
 * suffix is the bare name inside a character's animation json; {@link GirlAnimationSet} turns it
 * into a full {@code animation.<character>.<suffix>} id.
 */
public enum GirlAnimationRole {
   ANAL_CUM("anal_cum", true),
   ANAL_FAST("anal_fast", true),
   ANAL_POSITIONING("anal_prepare", false),
   ANAL_SLOW("anal_slow", true),
   ANAL_START("anal_start", false),
   ANAL_WAIT("anal_wait", true),
   ANAL_ALT_CUM("anal_alt_cum", true),
   ANAL_ALT_FAST("anal_alt_fast", true),
   ANAL_ALT_POSITIONING("anal_alt_prepare", false),
   ANAL_ALT_SLOW("anal_alt_slow", true),
   ANAL_ALT_START("anal_alt_start", false),
   ANAL_ALT_WAIT("anal_alt_wait", true),
   ATTACK0("attack0", false),
   ATTACK1("attack1", false),
   ATTACK2("attack2", false),
   BLOWJOB_CUM("blowjobcum", true),
   BLOWJOB_INTRO("blowjobintro", false),
   BLOWJOB_SUCK("blowjobsuck", true),
   BLOWJOB_THRUST("blowjobthrust", true),
   BOOBJOB_CUM("paizuri_cum", true),
   BOOBJOB_FAST("paizuri_fast", true),
   BOOBJOB_SLOW("paizuri_slow", true),
   BOOBJOB_START("paizuri_start", false),
   BOW("bowcharge", true),
   COWGIRL_CUM("cowgirlcum", true),
   COWGIRL_FAST("cowgirlfast", true),
   COWGIRL_SLOW("cowgirlslow2", true),
   COWGIRL_START("cowgirlstart", true),
   DEEPTHROAT_CUM("deepthroat_cum", true),
   DEEPTHROAT_FAST("deepthroat_fast", true),
   DEEPTHROAT_INTRO("deepthroat_start", false),
   DEEPTHROAT_SLOW("deepthroat_slow", true),
   DOGGY_CUM("doggycum", true),
   DOGGY_FAST("doggyfast", true),
   DOGGY_GO_ON_BED("doggygoonbed", false),
   DOGGY_SLOW("doggyslow", true),
   DOGGY_START("doggystart", false),
   DOGGY_WAIT("doggywait", true),
   DOWNED("downed", true),
   FACEFUCK_CUM("carry_cum", true),
   FACEFUCK_FAST("carry_fast", true),
   FACEFUCK_INTRO("carry_intro", true),
   FACEFUCK_SLOW("carry_slow1", true),
   FACEFUCK_SLOW2("carry_slow2", true),
   FACEFUCK_SLOW3("carry_slow3", true),
   FACEFUCK_SLOW4("carry_slow4", true),
   HAPPY_EYES("fhappy", true),
   IDLE("idle", true),
   KOBOLD_ANAL_CUM("kobold_anal_cum", false),
   KOBOLD_ANAL_FAST("kobold_anal_hard", true),
   KOBOLD_ANAL_SLOW("kobold_anal_soft", true),
   KOBOLD_ANAL_START("kobold_anal_start", false),
   KOBOLD_BLOWJOB_CUM("kobold_blowjob_cum", true),
   KOBOLD_BLOWJOB_FAST("kobold_blowjob_fast", true),
   KOBOLD_BLOWJOB_INTRO("kobold_blowjob_start", false),
   KOBOLD_BLOWJOB_SLOW("kobold_blowjob_slow", true),
   KOBOLD_BLOWJOB_SLOW_R("kobold_blowjob_slow_r", true),
   KOBOLD_BLOWJOB_SWITCH_TO_L("kobold_blowjob_switch_l", false),
   KOBOLD_BLOWJOB_SWITCH_TO_R("kobold_blowjob_switch_r", false),
   MATING_PRESS_CUM("mating_press_cum", false),
   MATING_PRESS_FAST("mating_press_hard", true),
   MATING_PRESS_SLOW("mating_press_soft", true),
   MATING_PRESS_START("mating_press_start", false),
   MISSIONARY_CUM("missionary_cum", true),
   MISSIONARY_FAST("missionary_fast", true),
   MISSIONARY_SLOW("missionary_slow", true),
   MISSIONARY_START("missionary_start", true),
   NULL("null", true),
   PAYMENT("payment", false),
   PRONE_DOGGY_CUM("prone_doggy_cum", true),
   PRONE_DOGGY_FAST("prone_doggy_hard1", true),
   PRONE_DOGGY_FAST2("prone_doggy_hard2", true),
   PRONE_DOGGY_FAST3("prone_doggy_hard3", true),
   // prone doggy ships no separate wait take: intro IS the holding loop, and insert loops as well
   // until the phase timer moves her on (original BiaEntity:628-635 builds both with looped=true)
   PRONE_DOGGY_POSITIONING("prone_doggy_intro", true),
   PRONE_DOGGY_SLOW("prone_doggy_soft", true),
   PRONE_DOGGY_START("prone_doggy_insert", true),
   REVERSE_COWGIRL_CUM("reverse_cowgirl_cum", true),
   REVERSE_COWGIRL_FAST1("reverse_cowgirl_fastc1", true),
   REVERSE_COWGIRL_FAST2("reverse_cowgirl_fastc2", true),
   REVERSE_COWGIRL_FAST3("reverse_cowgirl_fastc3", true),
   REVERSE_COWGIRL_FAST("reverse_cowgirl_fasts", true),
   REVERSE_COWGIRL_SLOW("reverse_cowgirl_slow1", true),
   REVERSE_COWGIRL_SLOW2("reverse_cowgirl_slow2", true),
   REVERSE_COWGIRL_SLOW3("reverse_cowgirl_slow3", true),
   REVERSE_COWGIRL_START("reverse_cowgirl_start", true),
   SITTING_CUM("sitting_cum", true),
   SITTING_FAST("sitting_fast", true),
   SITTING_SLOW("sitting_slow", true),
   SITTING_START("sitting_intro", false),
   SITTING_ALT_CUM("sitting_alt_cum", true),
   SITTING_ALT_FAST("sitting_alt_hard1", true),
   SITTING_ALT_FAST2("sitting_alt_hard2", true),
   SITTING_ALT_FAST3("sitting_alt_hard3", true),
   SITTING_ALT_POSITIONING("sitting_alt_intro", true),
   SITTING_ALT_SLOW("sitting_alt_soft", true),
   SITTING_ALT_START("sitting_alt_insert", true),
   STRIP("strip", false),
   THROW_PEARL("throwpearl", true),
   TOUCH_BOOBS_CUM("touch_boobs_cum", true),
   TOUCH_BOOBS_FAST("touch_boobs_fast", true),
   TOUCH_BOOBS_SLOW("touch_boobs_slow", true),
   TOUCH_BOOBS_SLOW_ALT("touch_boobs_slow1", true),
   TOUCH_BOOBS_START("touch_boobs_intro", false),
   WALK("walk", true);

   private final String suffix;
   private final boolean looping;

   GirlAnimationRole(String suffix, boolean looping) {
      this.suffix = suffix;
      this.looping = looping;
   }

   public String suffix() {
      return this.suffix;
   }

   public boolean looping() {
      return this.looping;
   }
}
