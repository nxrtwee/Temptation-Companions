package com.nxrtweetg.sexmod.girls.anim;

import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Full action-animation inventory from the shipped GeckoLib files.
 *
 * <p>The runtime scene state machine still picks one primary role per phase in
 * {@link GirlSceneAnimations}, but future ports often need to know every scene-only animation a
 * character can borrow or override. This catalog keeps those grouped by action, including alternate
 * loop variants that are not selected yet by the generic controller.
 */
public final class GirlActionAnimationCatalog {
   private static final Map<GirlSceneAction, List<GirlAnimationRole>> ACTION_ROLES = build();

   private GirlActionAnimationCatalog() {
   }

   public static List<GirlAnimationRole> rolesFor(GirlSceneAction action) {
      return ACTION_ROLES.getOrDefault(action, List.of());
   }

   public static Map<GirlSceneAction, List<GirlAnimationRole>> allRoles() {
      return ACTION_ROLES;
   }

   private static Map<GirlSceneAction, List<GirlAnimationRole>> build() {
      Map<GirlSceneAction, List<GirlAnimationRole>> roles = new EnumMap<>(GirlSceneAction.class);
      roles.put(GirlSceneAction.BLOWJOB, List.of(GirlAnimationRole.BLOWJOB_INTRO, GirlAnimationRole.BLOWJOB_SUCK, GirlAnimationRole.BLOWJOB_THRUST, GirlAnimationRole.BLOWJOB_CUM));
      roles.put(
         GirlSceneAction.FACEFUCK,
         List.of(
            GirlAnimationRole.FACEFUCK_INTRO,
            GirlAnimationRole.FACEFUCK_SLOW,
            GirlAnimationRole.FACEFUCK_SLOW2,
            GirlAnimationRole.FACEFUCK_SLOW3,
            GirlAnimationRole.FACEFUCK_SLOW4,
            GirlAnimationRole.FACEFUCK_FAST,
            GirlAnimationRole.FACEFUCK_CUM
         )
      );
      roles.put(
         GirlSceneAction.KOBOLD_BLOWJOB,
         List.of(
            GirlAnimationRole.KOBOLD_BLOWJOB_INTRO,
            GirlAnimationRole.KOBOLD_BLOWJOB_SLOW,
            GirlAnimationRole.KOBOLD_BLOWJOB_SLOW_R,
            GirlAnimationRole.KOBOLD_BLOWJOB_SWITCH_TO_L,
            GirlAnimationRole.KOBOLD_BLOWJOB_SWITCH_TO_R,
            GirlAnimationRole.KOBOLD_BLOWJOB_FAST,
            GirlAnimationRole.KOBOLD_BLOWJOB_CUM
         )
      );
      roles.put(
         GirlSceneAction.BOOBJOB,
         List.of(
            GirlAnimationRole.BOOBJOB_START,
            GirlAnimationRole.BOOBJOB_SLOW,
            GirlAnimationRole.BOOBJOB_FAST,
            GirlAnimationRole.BOOBJOB_CUM
         )
      );
      roles.put(
         GirlSceneAction.TOUCH_BOOBS,
         List.of(
            GirlAnimationRole.TOUCH_BOOBS_START,
            GirlAnimationRole.TOUCH_BOOBS_SLOW,
            GirlAnimationRole.TOUCH_BOOBS_SLOW_ALT,
            GirlAnimationRole.TOUCH_BOOBS_FAST,
            GirlAnimationRole.TOUCH_BOOBS_CUM
         )
      );
      roles.put(
         GirlSceneAction.DOGGY,
         List.of(
            GirlAnimationRole.DOGGY_GO_ON_BED,
            GirlAnimationRole.DOGGY_WAIT,
            GirlAnimationRole.DOGGY_START,
            GirlAnimationRole.DOGGY_SLOW,
            GirlAnimationRole.DOGGY_FAST,
            GirlAnimationRole.DOGGY_CUM
         )
      );
      roles.put(
         GirlSceneAction.PRONE_DOGGY,
         List.of(
            GirlAnimationRole.PRONE_DOGGY_POSITIONING,
            GirlAnimationRole.PRONE_DOGGY_START,
            GirlAnimationRole.PRONE_DOGGY_SLOW,
            GirlAnimationRole.PRONE_DOGGY_FAST,
            GirlAnimationRole.PRONE_DOGGY_FAST2,
            GirlAnimationRole.PRONE_DOGGY_FAST3,
            GirlAnimationRole.PRONE_DOGGY_CUM
         )
      );
      roles.put(
         GirlSceneAction.ANAL,
         List.of(
            GirlAnimationRole.ANAL_POSITIONING,
            GirlAnimationRole.ANAL_START,
            GirlAnimationRole.ANAL_WAIT,
            GirlAnimationRole.ANAL_SLOW,
            GirlAnimationRole.ANAL_FAST,
            GirlAnimationRole.ANAL_CUM
         )
      );
      roles.put(
         GirlSceneAction.KOBOLD_ANAL,
         List.of(
            GirlAnimationRole.KOBOLD_ANAL_START,
            GirlAnimationRole.KOBOLD_ANAL_SLOW,
            GirlAnimationRole.KOBOLD_ANAL_FAST,
            GirlAnimationRole.KOBOLD_ANAL_CUM
         )
      );
      roles.put(
         GirlSceneAction.SITTING,
         List.of(
            GirlAnimationRole.SITTING_START,
            GirlAnimationRole.SITTING_SLOW,
            GirlAnimationRole.SITTING_FAST,
            GirlAnimationRole.SITTING_CUM
         )
      );
      roles.put(
         GirlSceneAction.MISSIONARY,
         List.of(
            GirlAnimationRole.MISSIONARY_START,
            GirlAnimationRole.MISSIONARY_SLOW,
            GirlAnimationRole.MISSIONARY_FAST,
            GirlAnimationRole.MISSIONARY_CUM
         )
      );
      roles.put(
         GirlSceneAction.MATING_PRESS,
         List.of(
            GirlAnimationRole.MATING_PRESS_START,
            GirlAnimationRole.MATING_PRESS_SLOW,
            GirlAnimationRole.MATING_PRESS_FAST,
            GirlAnimationRole.MATING_PRESS_CUM
         )
      );
      roles.put(
         GirlSceneAction.COWGIRL,
         List.of(
            GirlAnimationRole.COWGIRL_START,
            GirlAnimationRole.COWGIRL_SLOW,
            GirlAnimationRole.COWGIRL_FAST,
            GirlAnimationRole.COWGIRL_CUM
         )
      );
      roles.put(
         GirlSceneAction.REVERSE_COWGIRL,
         List.of(
            GirlAnimationRole.REVERSE_COWGIRL_START,
            GirlAnimationRole.REVERSE_COWGIRL_SLOW,
            GirlAnimationRole.REVERSE_COWGIRL_SLOW2,
            GirlAnimationRole.REVERSE_COWGIRL_SLOW3,
            GirlAnimationRole.REVERSE_COWGIRL_FAST,
            GirlAnimationRole.REVERSE_COWGIRL_FAST1,
            GirlAnimationRole.REVERSE_COWGIRL_FAST2,
            GirlAnimationRole.REVERSE_COWGIRL_FAST3,
            GirlAnimationRole.REVERSE_COWGIRL_CUM
         )
      );
      roles.put(GirlSceneAction.STRIP, List.of(GirlAnimationRole.STRIP));
      return Map.copyOf(roles);
   }
}
