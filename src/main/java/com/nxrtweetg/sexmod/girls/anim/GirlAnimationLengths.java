package com.nxrtweetg.sexmod.girls.anim;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nxrtweetg.sexmod.SexmodMod;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * How long each animation runs, in ticks, read from the shipped animation files.
 *
 * <p>Durations the scene machine needs on the server, so this reads the jar through the classpath
 * rather than the {@code ResourceManager}: a dedicated server never loads {@code assets/}, but the
 * files are in the jar either way. Resource packs are deliberately not consulted — a pack that
 * retimes an animation would desync the server's phase clock from what it is showing.
 */
public final class GirlAnimationLengths {
   private static final String FALLBACK_NAMESPACE = "jenny";
   private static final float SECONDS_TO_TICKS = 20.0F;
   /** namespace -> animation name -> ticks; a namespace that failed to load maps to an empty map. */
   private static final Map<String, Map<String, Integer>> LENGTHS = new HashMap<>();

   private GirlAnimationLengths() {
   }

   /**
    * The length of {@code animation.<namespace>.<suffix>}, falling back to Jenny's copy of the same
    * animation and then to {@code fallback} when neither ships it.
    */
   public static int ticks(String namespace, String suffix, int fallback) {
      Integer own = lengths(namespace).get(suffix);
      if (own != null) {
         return own;
      }

      Integer borrowed = namespace.equals(FALLBACK_NAMESPACE) ? null : lengths(FALLBACK_NAMESPACE).get(suffix);
      return borrowed != null ? borrowed : fallback;
   }

   public static int ticks(String namespace, GirlAnimationRole role, int fallback) {
      return ticks(namespace, role.suffix(), fallback);
   }

   private static synchronized Map<String, Integer> lengths(String namespace) {
      return LENGTHS.computeIfAbsent(namespace, GirlAnimationLengths::load);
   }

   private static Map<String, Integer> load(String namespace) {
      String path = "/assets/sexmod/geckolib/animations/" + namespace + "/" + namespace + ".animation.json";
      Map<String, Integer> lengths = new HashMap<>();

      try (InputStream input = GirlAnimationLengths.class.getResourceAsStream(path)) {
         if (input == null) {
            SexmodMod.LOGGER.warn("No animation file for '{}' at {}", namespace, path);
            return lengths;
         }

         try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            JsonObject animations = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("animations");
            if (animations == null) {
               return lengths;
            }

            for (Map.Entry<String, JsonElement> entry : animations.entrySet()) {
               readLength(entry.getValue()).ifPresent(ticks -> lengths.put(suffixOf(entry.getKey()), ticks));
            }
         }
      } catch (Exception exception) {
         SexmodMod.LOGGER.warn("Failed to read animation lengths for '{}'", namespace, exception);
         return lengths;
      }

      SexmodMod.LOGGER.info("Loaded {} animation lengths for '{}'", lengths.size(), namespace);
      return lengths;
   }

   /** A looping animation may omit {@code animation_length}, in which case it has no fixed duration. */
   private static Optional<Integer> readLength(JsonElement animation) {
      if (!animation.isJsonObject()) {
         return Optional.empty();
      }

      JsonElement length = animation.getAsJsonObject().get("animation_length");
      if (length == null || !length.isJsonPrimitive()) {
         return Optional.empty();
      }

      int ticks = Math.round(length.getAsFloat() * SECONDS_TO_TICKS);
      return ticks > 0 ? Optional.of(ticks) : Optional.empty();
   }

   /** Keys are stored as {@code animation.<namespace>.<suffix>}; roles are keyed by suffix alone. */
   private static String suffixOf(String key) {
      int lastDot = key.lastIndexOf(46);
      return lastDot < 0 ? key : key.substring(lastDot + 1);
   }
}
