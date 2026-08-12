package com.nxrtweetg.sexmod.girls.anim;

import java.util.Map;

/**
 * Picks the sound folder for one girl. Every voice line lives at {@code girls.<character>.<family>},
 * but the roster is uneven: Bia ships seven of the twelve families the scenes ask for, and even a
 * family she does have may hold fewer takes than the scene asks for.
 *
 * <p>The original has no fallback at all — each girl carries her own keyframe switch naming only
 * the families she owns, and where she lacks one she uses a neighbouring family of her own (Bia
 * moans with {@code ahh}/{@code mmm}, original BiaEntity {@code doggyMoan}). We keep the shared
 * switch, so the same intent is expressed here: use her own take when it exists, else a substitute
 * from her own voice, and only borrow Jenny when she has nothing that fits.
 */
public final class GirlSoundSet {
   private static final String FALLBACK_CHARACTER = "jenny";

   /** What Jenny ships, so a borrowed line never asks for a take that is not on disk. */
   private static final Map<String, Integer> JENNY_VARIANTS = Map.ofEntries(
      Map.entry("aftersessionmoan", 5), Map.entry("ahh", 10), Map.entry("bjmoan", 13),
      Map.entry("giggle", 5), Map.entry("happyoh", 3), Map.entry("heavybreathing", 8),
      Map.entry("hmph", 4), Map.entry("huh", 2), Map.entry("lightbreathing", 12),
      Map.entry("lipsound", 10), Map.entry("mmm", 9), Map.entry("moan", 8),
      Map.entry("sadoh", 2), Map.entry("sigh", 2)
   );

   public static final GirlSoundSet JENNY = new GirlSoundSet(FALLBACK_CHARACTER, JENNY_VARIANTS, Map.of());

   private final String character;
   private final Map<String, Integer> ownVariants;
   private final Map<String, String> substitutes;

   /**
    * One resolved line: where it lives and how many takes are actually on disk, so the caller picks
    * an index that exists rather than the count the scene wished for.
    */
   public record Voice(String baseId, int variants) {
      public String id(int index) {
         return this.baseId + Math.max(0, Math.min(index, this.variants - 1));
      }
   }

   /**
    * @param character her folder under {@code assets/sexmod/sounds/girls}
    * @param ownVariants how many takes she ships per family, e.g. {@code ahh -> 8}
    */
   public GirlSoundSet(String character, Map<String, Integer> ownVariants) {
      this(character, ownVariants, Map.of());
   }

   /**
    * @param character her folder under {@code assets/sexmod/sounds/girls}
    * @param ownVariants how many takes she ships per family, e.g. {@code ahh -> 8}
    * @param substitutes family she lacks -> family of her own to speak it with
    */
   public GirlSoundSet(String character, Map<String, Integer> ownVariants, Map<String, String> substitutes) {
      this.character = character;
      this.ownVariants = ownVariants;
      this.substitutes = substitutes;
   }

   /**
    * Resolves a family to a real folder.
    *
    * @param requested how many takes the scene would like to choose between; the result is capped to
    *     what she actually ships, so eight of her own takes beat borrowing Jenny's twelve
    */
   public Voice voice(String family, int requested) {
      int own = this.ownVariants.getOrDefault(family, 0);
      if (own > 0) {
         return voice(this.character, family, Math.min(requested, own));
      }

      String substitute = this.substitutes.get(family);
      int substituteVariants = substitute == null ? 0 : this.ownVariants.getOrDefault(substitute, 0);
      if (substituteVariants > 0) {
         return voice(this.character, substitute, Math.min(requested, substituteVariants));
      }

      return voice(FALLBACK_CHARACTER, family, Math.min(requested, JENNY_VARIANTS.getOrDefault(family, 0)));
   }

   private static Voice voice(String character, String family, int variants) {
      return new Voice("girls." + character + "." + family + "." + family, variants);
   }
}
