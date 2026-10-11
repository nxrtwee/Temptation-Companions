package com.nxrtweetg.sexmod.girls.anim;

import com.geckolib.animation.RawAnimation;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Resolves an animation role to a concrete {@code animation.<character>.<suffix>} id for one girl.
 *
 * <p>Only the animations a character actually ships are looked up under her own namespace; anything
 * else falls back to Jenny's file, whose set is a superset because the scenes of every other girl
 * were retargeted onto her rig. GeckoLib skips animation channels whose bone the model lacks, so a
 * borrowed animation degrades to "that bone does not move" rather than failing.
 */
public final class GirlAnimationSet {
   private static final String FALLBACK_NAMESPACE = "jenny";
   /** Jenny's file holds every role, since the other girls' scenes were retargeted onto her rig. */
   public static final GirlAnimationSet JENNY = new GirlAnimationSet(FALLBACK_NAMESPACE, Set.of());

   private final String namespace;
   private final Map<GirlAnimationRole, RawAnimation> resolved = new EnumMap<>(GirlAnimationRole.class);
   private final Set<GirlAnimationRole> ownRoles = java.util.EnumSet.noneOf(GirlAnimationRole.class);

   /**
    * @param namespace the character's animation namespace, i.e. the {@code X} in {@code animation.X.walk}
    * @param ownSuffixes the animation names present in that character's own file
    */
   public GirlAnimationSet(String namespace, Set<String> ownSuffixes) {
      this(namespace, ownSuffixes, Map.of());
   }

   /**
    * @param namespace the character's animation namespace, i.e. the {@code X} in {@code animation.X.walk}
    * @param ownSuffixes the animation names present in that character's own file
    * @param aliases roles this character spells differently from the role's own suffix. The bee's pearl
    *     throw is {@code throw_pearl} where everyone else's is {@code throwpearl}, and renaming the role
    *     would take the take away from the four who spell it the other way.
    */
   public GirlAnimationSet(String namespace, Set<String> ownSuffixes, Map<GirlAnimationRole, String> aliases) {
      this.namespace = namespace;

      for (GirlAnimationRole role : GirlAnimationRole.values()) {
         String alias = aliases.get(role);
         boolean own = (namespace.equals(FALLBACK_NAMESPACE) && !isJennyMissing(role)) || alias != null || ownSuffixes.contains(role.suffix());
         if (own) {
            this.ownRoles.add(role);
         }
         String owner = own ? namespace : fallbackOwnerFor(role);
         String suffix = own && alias != null ? alias : role.suffix();
         String name;
         if (suffix.startsWith("animation.")) {
            name = suffix;
         } else if (suffix.startsWith("shared.")) {
            name = "animation." + suffix;
         } else {
            name = "animation." + owner + "." + suffix;
         }
         // A take that plays once holds its last frame instead of ending. GeckoLib 5 blends a finished
         // take back towards the model's bind pose over its own transition length (AnimationProcessor
         // .findResetPointValue, reached because a completed take has no transition source), which read
         // as her standing up out of the scene for a fraction of a second every time a phase waited on
         // the server for its hand-over. Holding the pose is also what the original did: its controller
         // kept the last frame until the next state arrived.
         this.resolved.put(role, role.looping() ? RawAnimation.begin().thenLoop(name) : RawAnimation.begin().thenPlayAndHold(name));
      }
   }

   public String namespace() {
      return this.namespace;
   }

   public RawAnimation get(GirlAnimationRole role) {
      return this.resolved.get(role);
   }

   public boolean hasOwn(GirlAnimationRole role) {
      return this.ownRoles.contains(role);
   }

   private static String fallbackOwnerFor(GirlAnimationRole role) {
      return switch (role) {
         case SIT_DOWN, SIT_DOWN_IDLE,
              ANAL_ALT_CUM, ANAL_ALT_FAST, ANAL_ALT_POSITIONING, ANAL_ALT_SLOW, ANAL_ALT_START, ANAL_ALT_WAIT,
              SITTING_ALT_CUM, SITTING_ALT_FAST, SITTING_ALT_FAST2, SITTING_ALT_FAST3, SITTING_ALT_POSITIONING, SITTING_ALT_SLOW, SITTING_ALT_START
              -> "bia";
         case NELSON_CUM, NELSON_FAST, NELSON_INTRO, NELSON_SLOW
              -> "goblin";
         case SEX_CUM, SEX_FAST, SEX_SLOW, SEX_START
              -> "bee";
         case WAIT
              -> "cat";
         default -> FALLBACK_NAMESPACE;
      };
   }

   private static boolean isJennyMissing(GirlAnimationRole role) {
      return switch (role) {
         case SIT_DOWN, SIT_DOWN_IDLE,
              ANAL_ALT_CUM, ANAL_ALT_FAST, ANAL_ALT_POSITIONING, ANAL_ALT_SLOW, ANAL_ALT_START, ANAL_ALT_WAIT,
              SITTING_ALT_CUM, SITTING_ALT_FAST, SITTING_ALT_FAST2, SITTING_ALT_FAST3, SITTING_ALT_POSITIONING, SITTING_ALT_SLOW, SITTING_ALT_START,
              NELSON_CUM, NELSON_FAST, NELSON_INTRO, NELSON_SLOW,
              SEX_CUM, SEX_FAST, SEX_SLOW, SEX_START,
              WAIT
              -> true;
         default -> false;
      };
   }
}

