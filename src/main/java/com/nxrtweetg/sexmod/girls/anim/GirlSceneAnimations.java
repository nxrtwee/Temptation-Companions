package com.nxrtweetg.sexmod.girls.anim;

import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.girls.scene.GirlScenePhase;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Which animation each scene plays in each phase. This was a 360-line switch; the shape was
 * always a table, and every character needs the same one, so it lives here as data.
 */
public final class GirlSceneAnimations {
   private static final Map<GirlSceneAction, Map<GirlScenePhase, Entry>> TABLE = build();
   /**
    * Scenes whose active beat ships more than one take. Index 0 repeats what {@link #role} returns
    * so a variant maps straight onto a list position; the rest are the alternates that used to sit
    * unused in the animation file.
    */
   private static final Map<GirlSceneAction, List<GirlAnimationRole>> FAST_VARIANTS = fastVariants();
   private static final Map<GirlSceneAction, List<GirlAnimationRole>> SLOW_VARIANTS = slowVariants();

   private GirlSceneAnimations() {
   }

   /** The pair a phase resolves to; {@code fast} is null when the phase does not branch on thrusting. */
   private record Entry(GirlAnimationRole slow, GirlAnimationRole fast) {
   }

   /**
    * Returns null when the scene handles the phase itself (kobold blowjob's side-switching loop),
    * which no flat table can express.
    */
   public static GirlAnimationRole role(GirlSceneAction action, GirlScenePhase phase, boolean thrusting) {
      Map<GirlScenePhase, Entry> phases = TABLE.get(action);
      if (phases == null) {
         return GirlAnimationRole.NULL;
      }

      Entry entry = phases.get(phase);
      if (entry == null) {
         return GirlAnimationRole.NULL;
      }

      return thrusting && entry.fast() != null ? entry.fast() : entry.slow();
   }

   /** The alternate takes of one beat, or an empty list when the scene ships a single take. */
   public static List<GirlAnimationRole> variants(GirlSceneAction action, GirlScenePhase phase, boolean thrusting) {
      if (phase != GirlScenePhase.ACTIVE) {
         return List.of();
      }

      List<GirlAnimationRole> takes = (thrusting ? FAST_VARIANTS : SLOW_VARIANTS).get(action);
      return takes != null ? takes : List.of();
   }

   /**
    * The take to play for a variant, counting from 1, falling back to the flat lookup when the
    * scene has no alternates or the variant is out of range.
    */
   public static GirlAnimationRole role(GirlSceneAction action, GirlScenePhase phase, boolean thrusting, int variant) {
      List<GirlAnimationRole> takes = variants(action, phase, thrusting);
      return variant >= 1 && variant <= takes.size() ? takes.get(variant - 1) : role(action, phase, thrusting);
   }

   /**
    * How many takes the scene's active beat has, counting the widest of its slow and fast sets. One
    * variant index drives both, so a scene whose sets differ in size falls back to the flat lookup
    * on the indices the shorter set lacks.
    */
   public static int variantCount(GirlSceneAction action) {
      return Math.max(sizeOf(SLOW_VARIANTS.get(action)), sizeOf(FAST_VARIANTS.get(action)));
   }

   private static int sizeOf(List<GirlAnimationRole> takes) {
      return takes != null ? takes.size() : 0;
   }

   private static Map<GirlSceneAction, List<GirlAnimationRole>> fastVariants() {
      Map<GirlSceneAction, List<GirlAnimationRole>> variants = new EnumMap<>(GirlSceneAction.class);
      variants.put(
         GirlSceneAction.PRONE_DOGGY,
         List.of(GirlAnimationRole.PRONE_DOGGY_FAST, GirlAnimationRole.PRONE_DOGGY_FAST2, GirlAnimationRole.PRONE_DOGGY_FAST3)
      );
      variants.put(
         GirlSceneAction.SITTING_ALT,
         List.of(GirlAnimationRole.SITTING_ALT_FAST, GirlAnimationRole.SITTING_ALT_FAST2, GirlAnimationRole.SITTING_ALT_FAST3)
      );
      variants.put(
         GirlSceneAction.REVERSE_COWGIRL,
         List.of(
            GirlAnimationRole.REVERSE_COWGIRL_FAST,
            GirlAnimationRole.REVERSE_COWGIRL_FAST1,
            GirlAnimationRole.REVERSE_COWGIRL_FAST2,
            GirlAnimationRole.REVERSE_COWGIRL_FAST3
         )
      );
      return variants;
   }

   private static Map<GirlSceneAction, List<GirlAnimationRole>> slowVariants() {
      Map<GirlSceneAction, List<GirlAnimationRole>> variants = new EnumMap<>(GirlSceneAction.class);
      variants.put(
         GirlSceneAction.FACEFUCK,
         List.of(
            GirlAnimationRole.FACEFUCK_SLOW,
            GirlAnimationRole.FACEFUCK_SLOW2,
            GirlAnimationRole.FACEFUCK_SLOW3,
            GirlAnimationRole.FACEFUCK_SLOW4
         )
      );
      variants.put(
         GirlSceneAction.REVERSE_COWGIRL,
         List.of(GirlAnimationRole.REVERSE_COWGIRL_SLOW, GirlAnimationRole.REVERSE_COWGIRL_SLOW2, GirlAnimationRole.REVERSE_COWGIRL_SLOW3)
      );
      return variants;
   }

   private static Map<GirlSceneAction, Map<GirlScenePhase, Entry>> build() {
      Map<GirlSceneAction, Map<GirlScenePhase, Entry>> table = new EnumMap<>(GirlSceneAction.class);

      Map<GirlScenePhase, Entry> blowjob = new EnumMap<>(GirlScenePhase.class);
      blowjob.put(GirlScenePhase.APPROACH, new Entry(GirlAnimationRole.WALK, null));
      blowjob.put(GirlScenePhase.WALKING, new Entry(GirlAnimationRole.WALK, null));
      blowjob.put(GirlScenePhase.PAYMENT, new Entry(GirlAnimationRole.PAYMENT, null));
      blowjob.put(GirlScenePhase.POSITIONING, new Entry(GirlAnimationRole.NULL, null));
      blowjob.put(GirlScenePhase.WAITING, new Entry(GirlAnimationRole.NULL, null));
      blowjob.put(GirlScenePhase.INTRO, new Entry(GirlAnimationRole.BLOWJOB_INTRO, null));
      blowjob.put(GirlScenePhase.ACTIVE, new Entry(GirlAnimationRole.BLOWJOB_SUCK, GirlAnimationRole.BLOWJOB_THRUST));
      blowjob.put(GirlScenePhase.CLIMAX, new Entry(GirlAnimationRole.BLOWJOB_CUM, null));
      blowjob.put(GirlScenePhase.IDLE, new Entry(GirlAnimationRole.NULL, null));
      table.put(GirlSceneAction.BLOWJOB, blowjob);

      Map<GirlScenePhase, Entry> kobold_blowjob = new EnumMap<>(GirlScenePhase.class);
      kobold_blowjob.put(GirlScenePhase.APPROACH, new Entry(GirlAnimationRole.WALK, null));
      kobold_blowjob.put(GirlScenePhase.WALKING, new Entry(GirlAnimationRole.WALK, null));
      kobold_blowjob.put(GirlScenePhase.PAYMENT, new Entry(GirlAnimationRole.PAYMENT, null));
      kobold_blowjob.put(GirlScenePhase.POSITIONING, new Entry(GirlAnimationRole.NULL, null));
      kobold_blowjob.put(GirlScenePhase.WAITING, new Entry(GirlAnimationRole.NULL, null));
      kobold_blowjob.put(GirlScenePhase.INTRO, new Entry(GirlAnimationRole.KOBOLD_BLOWJOB_INTRO, null));
      kobold_blowjob.put(GirlScenePhase.CLIMAX, new Entry(GirlAnimationRole.KOBOLD_BLOWJOB_CUM, null));
      kobold_blowjob.put(GirlScenePhase.IDLE, new Entry(GirlAnimationRole.NULL, null));
      table.put(GirlSceneAction.KOBOLD_BLOWJOB, kobold_blowjob);

      Map<GirlScenePhase, Entry> deepthroat = new EnumMap<>(GirlScenePhase.class);
      deepthroat.put(GirlScenePhase.APPROACH, new Entry(GirlAnimationRole.WALK, null));
      deepthroat.put(GirlScenePhase.WALKING, new Entry(GirlAnimationRole.WALK, null));
      deepthroat.put(GirlScenePhase.PAYMENT, new Entry(GirlAnimationRole.PAYMENT, null));
      deepthroat.put(GirlScenePhase.POSITIONING, new Entry(GirlAnimationRole.NULL, null));
      deepthroat.put(GirlScenePhase.WAITING, new Entry(GirlAnimationRole.NULL, null));
      deepthroat.put(GirlScenePhase.INTRO, new Entry(GirlAnimationRole.DEEPTHROAT_INTRO, null));
      deepthroat.put(GirlScenePhase.ACTIVE, new Entry(GirlAnimationRole.DEEPTHROAT_SLOW, GirlAnimationRole.DEEPTHROAT_FAST));
      deepthroat.put(GirlScenePhase.CLIMAX, new Entry(GirlAnimationRole.DEEPTHROAT_CUM, null));
      deepthroat.put(GirlScenePhase.IDLE, new Entry(GirlAnimationRole.NULL, null));
      table.put(GirlSceneAction.DEEPTHROAT, deepthroat);

      Map<GirlScenePhase, Entry> facefuck = new EnumMap<>(GirlScenePhase.class);
      facefuck.put(GirlScenePhase.APPROACH, new Entry(GirlAnimationRole.WALK, null));
      facefuck.put(GirlScenePhase.WALKING, new Entry(GirlAnimationRole.WALK, null));
      facefuck.put(GirlScenePhase.PAYMENT, new Entry(GirlAnimationRole.PAYMENT, null));
      facefuck.put(GirlScenePhase.POSITIONING, new Entry(GirlAnimationRole.NULL, null));
      facefuck.put(GirlScenePhase.WAITING, new Entry(GirlAnimationRole.NULL, null));
      facefuck.put(GirlScenePhase.INTRO, new Entry(GirlAnimationRole.FACEFUCK_INTRO, null));
      facefuck.put(GirlScenePhase.ACTIVE, new Entry(GirlAnimationRole.FACEFUCK_SLOW, GirlAnimationRole.FACEFUCK_FAST));
      facefuck.put(GirlScenePhase.CLIMAX, new Entry(GirlAnimationRole.FACEFUCK_CUM, null));
      facefuck.put(GirlScenePhase.IDLE, new Entry(GirlAnimationRole.NULL, null));
      table.put(GirlSceneAction.FACEFUCK, facefuck);

      Map<GirlScenePhase, Entry> boobjob = new EnumMap<>(GirlScenePhase.class);
      boobjob.put(GirlScenePhase.APPROACH, new Entry(GirlAnimationRole.WALK, null));
      boobjob.put(GirlScenePhase.WALKING, new Entry(GirlAnimationRole.WALK, null));
      boobjob.put(GirlScenePhase.PAYMENT, new Entry(GirlAnimationRole.PAYMENT, null));
      boobjob.put(GirlScenePhase.POSITIONING, new Entry(GirlAnimationRole.NULL, null));
      boobjob.put(GirlScenePhase.WAITING, new Entry(GirlAnimationRole.NULL, null));
      boobjob.put(GirlScenePhase.INTRO, new Entry(GirlAnimationRole.BOOBJOB_START, null));
      boobjob.put(GirlScenePhase.ACTIVE, new Entry(GirlAnimationRole.BOOBJOB_SLOW, GirlAnimationRole.BOOBJOB_FAST));
      boobjob.put(GirlScenePhase.CLIMAX, new Entry(GirlAnimationRole.BOOBJOB_CUM, null));
      boobjob.put(GirlScenePhase.IDLE, new Entry(GirlAnimationRole.NULL, null));
      table.put(GirlSceneAction.BOOBJOB, boobjob);

      Map<GirlScenePhase, Entry> touch_boobs = new EnumMap<>(GirlScenePhase.class);
      touch_boobs.put(GirlScenePhase.APPROACH, new Entry(GirlAnimationRole.WALK, null));
      touch_boobs.put(GirlScenePhase.WALKING, new Entry(GirlAnimationRole.WALK, null));
      touch_boobs.put(GirlScenePhase.PAYMENT, new Entry(GirlAnimationRole.PAYMENT, null));
      touch_boobs.put(GirlScenePhase.POSITIONING, new Entry(GirlAnimationRole.NULL, null));
      touch_boobs.put(GirlScenePhase.WAITING, new Entry(GirlAnimationRole.NULL, null));
      touch_boobs.put(GirlScenePhase.INTRO, new Entry(GirlAnimationRole.TOUCH_BOOBS_START, null));
      touch_boobs.put(GirlScenePhase.ACTIVE, new Entry(GirlAnimationRole.TOUCH_BOOBS_SLOW, GirlAnimationRole.TOUCH_BOOBS_FAST));
      touch_boobs.put(GirlScenePhase.CLIMAX, new Entry(GirlAnimationRole.TOUCH_BOOBS_CUM, null));
      touch_boobs.put(GirlScenePhase.IDLE, new Entry(GirlAnimationRole.NULL, null));
      table.put(GirlSceneAction.TOUCH_BOOBS, touch_boobs);

      Map<GirlScenePhase, Entry> doggy = new EnumMap<>(GirlScenePhase.class);
      doggy.put(GirlScenePhase.APPROACH, new Entry(GirlAnimationRole.WALK, null));
      doggy.put(GirlScenePhase.WALKING, new Entry(GirlAnimationRole.WALK, null));
      doggy.put(GirlScenePhase.PAYMENT, new Entry(GirlAnimationRole.PAYMENT, null));
      doggy.put(GirlScenePhase.POSITIONING, new Entry(GirlAnimationRole.DOGGY_GO_ON_BED, null));
      doggy.put(GirlScenePhase.WAITING, new Entry(GirlAnimationRole.DOGGY_WAIT, null));
      doggy.put(GirlScenePhase.INTRO, new Entry(GirlAnimationRole.DOGGY_START, null));
      doggy.put(GirlScenePhase.ACTIVE, new Entry(GirlAnimationRole.DOGGY_SLOW, GirlAnimationRole.DOGGY_FAST));
      doggy.put(GirlScenePhase.CLIMAX, new Entry(GirlAnimationRole.DOGGY_CUM, null));
      doggy.put(GirlScenePhase.IDLE, new Entry(GirlAnimationRole.NULL, null));
      table.put(GirlSceneAction.DOGGY, doggy);

      Map<GirlScenePhase, Entry> prone_doggy = new EnumMap<>(GirlScenePhase.class);
      prone_doggy.put(GirlScenePhase.APPROACH, new Entry(GirlAnimationRole.WALK, null));
      prone_doggy.put(GirlScenePhase.WALKING, new Entry(GirlAnimationRole.WALK, null));
      prone_doggy.put(GirlScenePhase.PAYMENT, new Entry(GirlAnimationRole.PAYMENT, null));
      // she shares anal's lying-down and holding takes: prone_doggy ships no wait animation of its
      // own, and prone_doggy_intro is already the scene proper with the partner in it
      // (original: anal_prepare -> anal_prepareDone -> ANAL_WAIT -> PRONE_DOGGY_INTRO, BiaEntity:244-262)
      prone_doggy.put(GirlScenePhase.POSITIONING, new Entry(GirlAnimationRole.ANAL_POSITIONING, null));
      prone_doggy.put(GirlScenePhase.WAITING, new Entry(GirlAnimationRole.ANAL_WAIT, null));
      // intro loops until the player thrusts, then insert plays once and hands over to soft
      // (original: PRONE_DOGGY_INTRO -thrust-> PRONE_DOGGY_INSERT(42) -> PRONE_DOGGY_SOFT)
      prone_doggy.put(GirlScenePhase.INTRO, new Entry(GirlAnimationRole.PRONE_DOGGY_POSITIONING, GirlAnimationRole.PRONE_DOGGY_START));
      prone_doggy.put(GirlScenePhase.ACTIVE, new Entry(GirlAnimationRole.PRONE_DOGGY_SLOW, GirlAnimationRole.PRONE_DOGGY_FAST));
      prone_doggy.put(GirlScenePhase.CLIMAX, new Entry(GirlAnimationRole.PRONE_DOGGY_CUM, null));
      prone_doggy.put(GirlScenePhase.IDLE, new Entry(GirlAnimationRole.NULL, null));
      table.put(GirlSceneAction.PRONE_DOGGY, prone_doggy);

      // Bia's other take on the same beat, kept from a 1.12.2 pack: she lies flat with the partner
      // above her instead of upright, and it runs the same intro -> insert -> soft/hard shape.
      Map<GirlScenePhase, Entry> sitting_alt = new EnumMap<>(GirlScenePhase.class);
      sitting_alt.put(GirlScenePhase.APPROACH, new Entry(GirlAnimationRole.WALK, null));
      sitting_alt.put(GirlScenePhase.WALKING, new Entry(GirlAnimationRole.WALK, null));
      sitting_alt.put(GirlScenePhase.PAYMENT, new Entry(GirlAnimationRole.PAYMENT, null));
      sitting_alt.put(GirlScenePhase.POSITIONING, new Entry(GirlAnimationRole.ANAL_POSITIONING, null));
      sitting_alt.put(GirlScenePhase.WAITING, new Entry(GirlAnimationRole.ANAL_WAIT, null));
      sitting_alt.put(GirlScenePhase.INTRO, new Entry(GirlAnimationRole.SITTING_ALT_POSITIONING, GirlAnimationRole.SITTING_ALT_START));
      sitting_alt.put(GirlScenePhase.ACTIVE, new Entry(GirlAnimationRole.SITTING_ALT_SLOW, GirlAnimationRole.SITTING_ALT_FAST));
      sitting_alt.put(GirlScenePhase.CLIMAX, new Entry(GirlAnimationRole.SITTING_ALT_CUM, null));
      sitting_alt.put(GirlScenePhase.IDLE, new Entry(GirlAnimationRole.NULL, null));
      table.put(GirlSceneAction.SITTING_ALT, sitting_alt);

      Map<GirlScenePhase, Entry> anal = new EnumMap<>(GirlScenePhase.class);
      anal.put(GirlScenePhase.APPROACH, new Entry(GirlAnimationRole.WALK, null));
      anal.put(GirlScenePhase.WALKING, new Entry(GirlAnimationRole.WALK, null));
      anal.put(GirlScenePhase.PAYMENT, new Entry(GirlAnimationRole.PAYMENT, null));
      anal.put(GirlScenePhase.POSITIONING, new Entry(GirlAnimationRole.ANAL_POSITIONING, null));
      anal.put(GirlScenePhase.WAITING, new Entry(GirlAnimationRole.ANAL_WAIT, null));
      anal.put(GirlScenePhase.INTRO, new Entry(GirlAnimationRole.ANAL_START, null));
      anal.put(GirlScenePhase.ACTIVE, new Entry(GirlAnimationRole.ANAL_SLOW, GirlAnimationRole.ANAL_FAST));
      anal.put(GirlScenePhase.CLIMAX, new Entry(GirlAnimationRole.ANAL_CUM, null));
      anal.put(GirlScenePhase.IDLE, new Entry(GirlAnimationRole.NULL, null));
      table.put(GirlSceneAction.ANAL, anal);

      // Her 1.12.2 take on the same scene: a different pose throughout, but the same phase shape
      // and the same runtimes, so it maps onto anal's structure one for one.
      Map<GirlScenePhase, Entry> anal_alt = new EnumMap<>(GirlScenePhase.class);
      anal_alt.put(GirlScenePhase.APPROACH, new Entry(GirlAnimationRole.WALK, null));
      anal_alt.put(GirlScenePhase.WALKING, new Entry(GirlAnimationRole.WALK, null));
      anal_alt.put(GirlScenePhase.PAYMENT, new Entry(GirlAnimationRole.PAYMENT, null));
      anal_alt.put(GirlScenePhase.POSITIONING, new Entry(GirlAnimationRole.ANAL_ALT_POSITIONING, null));
      anal_alt.put(GirlScenePhase.WAITING, new Entry(GirlAnimationRole.ANAL_ALT_WAIT, null));
      anal_alt.put(GirlScenePhase.INTRO, new Entry(GirlAnimationRole.ANAL_ALT_START, null));
      anal_alt.put(GirlScenePhase.ACTIVE, new Entry(GirlAnimationRole.ANAL_ALT_SLOW, GirlAnimationRole.ANAL_ALT_FAST));
      anal_alt.put(GirlScenePhase.CLIMAX, new Entry(GirlAnimationRole.ANAL_ALT_CUM, null));
      anal_alt.put(GirlScenePhase.IDLE, new Entry(GirlAnimationRole.NULL, null));
      table.put(GirlSceneAction.ANAL_ALT, anal_alt);

      Map<GirlScenePhase, Entry> kobold_anal = new EnumMap<>(GirlScenePhase.class);
      kobold_anal.put(GirlScenePhase.APPROACH, new Entry(GirlAnimationRole.WALK, null));
      kobold_anal.put(GirlScenePhase.WALKING, new Entry(GirlAnimationRole.WALK, null));
      kobold_anal.put(GirlScenePhase.PAYMENT, new Entry(GirlAnimationRole.PAYMENT, null));
      kobold_anal.put(GirlScenePhase.POSITIONING, new Entry(GirlAnimationRole.NULL, null));
      kobold_anal.put(GirlScenePhase.WAITING, new Entry(GirlAnimationRole.NULL, null));
      kobold_anal.put(GirlScenePhase.INTRO, new Entry(GirlAnimationRole.KOBOLD_ANAL_START, null));
      kobold_anal.put(GirlScenePhase.ACTIVE, new Entry(GirlAnimationRole.KOBOLD_ANAL_SLOW, GirlAnimationRole.KOBOLD_ANAL_FAST));
      kobold_anal.put(GirlScenePhase.CLIMAX, new Entry(GirlAnimationRole.KOBOLD_ANAL_CUM, null));
      kobold_anal.put(GirlScenePhase.IDLE, new Entry(GirlAnimationRole.NULL, null));
      table.put(GirlSceneAction.KOBOLD_ANAL, kobold_anal);

      Map<GirlScenePhase, Entry> sitting = new EnumMap<>(GirlScenePhase.class);
      sitting.put(GirlScenePhase.APPROACH, new Entry(GirlAnimationRole.WALK, null));
      sitting.put(GirlScenePhase.WALKING, new Entry(GirlAnimationRole.WALK, null));
      sitting.put(GirlScenePhase.PAYMENT, new Entry(GirlAnimationRole.PAYMENT, null));
      sitting.put(GirlScenePhase.POSITIONING, new Entry(GirlAnimationRole.NULL, null));
      sitting.put(GirlScenePhase.WAITING, new Entry(GirlAnimationRole.NULL, null));
      sitting.put(GirlScenePhase.INTRO, new Entry(GirlAnimationRole.SITTING_START, null));
      sitting.put(GirlScenePhase.ACTIVE, new Entry(GirlAnimationRole.SITTING_SLOW, GirlAnimationRole.SITTING_FAST));
      sitting.put(GirlScenePhase.CLIMAX, new Entry(GirlAnimationRole.SITTING_CUM, null));
      sitting.put(GirlScenePhase.IDLE, new Entry(GirlAnimationRole.NULL, null));
      table.put(GirlSceneAction.SITTING, sitting);

      Map<GirlScenePhase, Entry> missionary = new EnumMap<>(GirlScenePhase.class);
      missionary.put(GirlScenePhase.APPROACH, new Entry(GirlAnimationRole.WALK, null));
      missionary.put(GirlScenePhase.WALKING, new Entry(GirlAnimationRole.WALK, null));
      missionary.put(GirlScenePhase.PAYMENT, new Entry(GirlAnimationRole.PAYMENT, null));
      missionary.put(GirlScenePhase.POSITIONING, new Entry(GirlAnimationRole.NULL, null));
      missionary.put(GirlScenePhase.WAITING, new Entry(GirlAnimationRole.NULL, null));
      missionary.put(GirlScenePhase.INTRO, new Entry(GirlAnimationRole.MISSIONARY_START, null));
      missionary.put(GirlScenePhase.ACTIVE, new Entry(GirlAnimationRole.MISSIONARY_SLOW, GirlAnimationRole.MISSIONARY_FAST));
      missionary.put(GirlScenePhase.CLIMAX, new Entry(GirlAnimationRole.MISSIONARY_CUM, null));
      missionary.put(GirlScenePhase.IDLE, new Entry(GirlAnimationRole.NULL, null));
      table.put(GirlSceneAction.MISSIONARY, missionary);

      Map<GirlScenePhase, Entry> mating_press = new EnumMap<>(GirlScenePhase.class);
      mating_press.put(GirlScenePhase.APPROACH, new Entry(GirlAnimationRole.WALK, null));
      mating_press.put(GirlScenePhase.WALKING, new Entry(GirlAnimationRole.WALK, null));
      mating_press.put(GirlScenePhase.PAYMENT, new Entry(GirlAnimationRole.PAYMENT, null));
      mating_press.put(GirlScenePhase.POSITIONING, new Entry(GirlAnimationRole.NULL, null));
      mating_press.put(GirlScenePhase.WAITING, new Entry(GirlAnimationRole.NULL, null));
      mating_press.put(GirlScenePhase.INTRO, new Entry(GirlAnimationRole.MATING_PRESS_START, null));
      mating_press.put(GirlScenePhase.ACTIVE, new Entry(GirlAnimationRole.MATING_PRESS_SLOW, GirlAnimationRole.MATING_PRESS_FAST));
      mating_press.put(GirlScenePhase.CLIMAX, new Entry(GirlAnimationRole.MATING_PRESS_CUM, null));
      mating_press.put(GirlScenePhase.IDLE, new Entry(GirlAnimationRole.NULL, null));
      table.put(GirlSceneAction.MATING_PRESS, mating_press);

      Map<GirlScenePhase, Entry> reverse_cowgirl = new EnumMap<>(GirlScenePhase.class);
      reverse_cowgirl.put(GirlScenePhase.APPROACH, new Entry(GirlAnimationRole.WALK, null));
      reverse_cowgirl.put(GirlScenePhase.WALKING, new Entry(GirlAnimationRole.WALK, null));
      reverse_cowgirl.put(GirlScenePhase.PAYMENT, new Entry(GirlAnimationRole.PAYMENT, null));
      reverse_cowgirl.put(GirlScenePhase.POSITIONING, new Entry(GirlAnimationRole.NULL, null));
      reverse_cowgirl.put(GirlScenePhase.WAITING, new Entry(GirlAnimationRole.NULL, null));
      reverse_cowgirl.put(GirlScenePhase.INTRO, new Entry(GirlAnimationRole.REVERSE_COWGIRL_START, null));
      reverse_cowgirl.put(GirlScenePhase.ACTIVE, new Entry(GirlAnimationRole.REVERSE_COWGIRL_SLOW, GirlAnimationRole.REVERSE_COWGIRL_FAST));
      reverse_cowgirl.put(GirlScenePhase.CLIMAX, new Entry(GirlAnimationRole.REVERSE_COWGIRL_CUM, null));
      reverse_cowgirl.put(GirlScenePhase.IDLE, new Entry(GirlAnimationRole.NULL, null));
      table.put(GirlSceneAction.REVERSE_COWGIRL, reverse_cowgirl);

      Map<GirlScenePhase, Entry> cowgirl = new EnumMap<>(GirlScenePhase.class);
      cowgirl.put(GirlScenePhase.APPROACH, new Entry(GirlAnimationRole.WALK, null));
      cowgirl.put(GirlScenePhase.WALKING, new Entry(GirlAnimationRole.WALK, null));
      cowgirl.put(GirlScenePhase.PAYMENT, new Entry(GirlAnimationRole.PAYMENT, null));
      cowgirl.put(GirlScenePhase.POSITIONING, new Entry(GirlAnimationRole.NULL, null));
      cowgirl.put(GirlScenePhase.WAITING, new Entry(GirlAnimationRole.NULL, null));
      cowgirl.put(GirlScenePhase.INTRO, new Entry(GirlAnimationRole.COWGIRL_START, null));
      cowgirl.put(GirlScenePhase.ACTIVE, new Entry(GirlAnimationRole.COWGIRL_SLOW, GirlAnimationRole.COWGIRL_FAST));
      cowgirl.put(GirlScenePhase.CLIMAX, new Entry(GirlAnimationRole.COWGIRL_CUM, null));
      cowgirl.put(GirlScenePhase.IDLE, new Entry(GirlAnimationRole.NULL, null));
      table.put(GirlSceneAction.COWGIRL, cowgirl);
      // Strip: APPROACH and PAYMENT use walk, ACTIVE onwards uses the strip animation
      Map<GirlScenePhase, Entry> strip = new EnumMap<>(GirlScenePhase.class);
      strip.put(GirlScenePhase.APPROACH,    new Entry(GirlAnimationRole.WALK, null));
      strip.put(GirlScenePhase.PAYMENT,     new Entry(GirlAnimationRole.WALK, null));
      strip.put(GirlScenePhase.WALKING,     new Entry(GirlAnimationRole.WALK, null));
      strip.put(GirlScenePhase.POSITIONING, new Entry(GirlAnimationRole.NULL, null));
      strip.put(GirlScenePhase.WAITING,     new Entry(GirlAnimationRole.NULL, null));
      strip.put(GirlScenePhase.INTRO,       new Entry(GirlAnimationRole.STRIP, null));
      strip.put(GirlScenePhase.ACTIVE,      new Entry(GirlAnimationRole.STRIP, null));
      strip.put(GirlScenePhase.CLIMAX,      new Entry(GirlAnimationRole.STRIP, null));
      strip.put(GirlScenePhase.IDLE,        new Entry(GirlAnimationRole.NULL, null));
      table.put(GirlSceneAction.STRIP, strip);

      return table;
   }

   private static Map<GirlScenePhase, Entry> all(GirlAnimationRole role) {
      Map<GirlScenePhase, Entry> phases = new EnumMap<>(GirlScenePhase.class);

      for (GirlScenePhase phase : GirlScenePhase.values()) {
         phases.put(phase, new Entry(role, null));
      }

      return phases;
   }
}
