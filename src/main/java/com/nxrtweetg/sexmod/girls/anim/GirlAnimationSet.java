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

   /**
    * @param namespace the character's animation namespace, i.e. the {@code X} in {@code animation.X.walk}
    * @param ownSuffixes the animation names present in that character's own file
    */
   public GirlAnimationSet(String namespace, Set<String> ownSuffixes) {
      this.namespace = namespace;

      for (GirlAnimationRole role : GirlAnimationRole.values()) {
         String owner = namespace.equals(FALLBACK_NAMESPACE) || ownSuffixes.contains(role.suffix()) ? namespace : FALLBACK_NAMESPACE;
         String name = "animation." + owner + "." + role.suffix();
         this.resolved.put(role, role.looping() ? RawAnimation.begin().thenLoop(name) : RawAnimation.begin().thenPlay(name));
      }
   }

   public String namespace() {
      return this.namespace;
   }

   public RawAnimation get(GirlAnimationRole role) {
      return this.resolved.get(role);
   }
}
