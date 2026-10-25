package com.nxrtweetg.sexmod.girls.anim;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nxrtweetg.sexmod.SexmodMod;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneKeyframe;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * How long each animation runs, in ticks, and which keyframes it carries, read from the shipped
 * animation files.
 *
 * <p>Durations the scene machine needs on the server, so this reads the jar through the classpath
 * rather than the {@code ResourceManager}: a dedicated server never loads {@code assets/}, but the
 * files are in the jar either way. Resource packs are deliberately not consulted — a pack that
 * retimes an animation would desync the server's phase clock from what it is showing.
 */
public final class GirlAnimationLengths {
   private static final String FALLBACK_NAMESPACE = "jenny";
   private static final float SECONDS_TO_TICKS = 20.0F;
   /** namespace -> animation name -> take; a namespace that failed to load maps to an empty map. */
   private static final Map<String, Map<String, Take>> TAKES = new HashMap<>();

   private GirlAnimationLengths() {
   }

   /**
    * One animation as the scene machine needs to know it: how long it runs, and the sound keyframes it
    * fires mapped to the tick they fire on. {@code ticks} is null for a loop that ships no
    * {@code animation_length}.
    */
   private record Take(Integer ticks, Map<String, Integer> keyframes) {
   }

   /**
    * The length of {@code animation.<namespace>.<suffix>}, falling back to Jenny's copy of the same
    * animation and then to {@code fallback} when neither ships it.
    */
   public static int ticks(String namespace, String suffix, int fallback) {
      Take own = take(namespace, suffix);
      return own != null && own.ticks() != null ? own.ticks() : fallback;
   }

   public static int ticks(String namespace, GirlAnimationRole role, int fallback) {
      return ticks(namespace, role.suffix(), fallback);
   }

   /**
    * How far into {@code animation.<namespace>.<suffix>} the keyframe named {@code keyframe} fires, in
    * ticks, or {@code fallback} when the take does not carry it.
    *
    * <p>This is for the hand-overs nobody is bound to report. A scene keyframe reaches the server
    * because the client that is in the scene sends it, but a pose a character plays on her own has no
    * such reporter, so the tick that owns the pose has to know the time itself.
    */
   public static int keyframeTicks(String namespace, String suffix, String keyframe, int fallback) {
      Take take = take(namespace, suffix);
      if (take == null) {
         return fallback;
      }

      Integer ticks = take.keyframes().get(keyframe);
      return ticks == null ? fallback : ticks;
   }

   /**
    * True when the take carries a keyframe the scene machine reads as {@code signal}.
    *
    * <p>The phase budgets use this to decide whether they are the authority or only a watchdog: a take
    * that names its own end hands over on that keyframe, so the budget may safely overrun it, while one
    * that does not has to end on the budget alone. Overrunning a looping take of the second kind snaps
    * her back to its first frame, which reads as her dropping out of the scene for a moment.
    */
   public static boolean endsOnKeyframe(String namespace, GirlAnimationRole role, GirlSceneKeyframe signal) {
      Take take = take(namespace, role.suffix());
      if (take == null) {
         return false;
      }

      for (String keyframe : take.keyframes().keySet()) {
         if (GirlSceneKeyframe.of(keyframe) == signal) {
            return true;
         }
      }

      return false;
   }

   /** Her own copy of the take, else Jenny's, else null. */
   private static Take take(String namespace, String suffix) {
      Take own = takes(namespace).get(suffix);
      if (own != null) {
         return own;
      }

      return namespace.equals(FALLBACK_NAMESPACE) ? null : takes(FALLBACK_NAMESPACE).get(suffix);
   }

   private static synchronized Map<String, Take> takes(String namespace) {
      return TAKES.computeIfAbsent(namespace, GirlAnimationLengths::load);
   }

   public static synchronized void reloadFromResourceManager(ResourceManager resourceManager) {
      TAKES.clear();
      for (Map.Entry<ResourceLocation, Resource> entry :
           resourceManager.listResources("geckolib/animations", id -> id.getPath().endsWith(".animation.json")).entrySet()) {
         ResourceLocation id = entry.getKey();
         if (!"sexmod".equals(id.getNamespace())) {
            continue;
         }
         String path = id.getPath();
         String[] parts = path.split("/");
         if (parts.length >= 3) {
            String characterNamespace = parts[2];
            try (InputStream input = entry.getValue().open();
                 Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
               JsonObject animations = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("animations");
               if (animations != null) {
                  Map<String, Take> takes = TAKES.computeIfAbsent(characterNamespace, GirlAnimationLengths::load);
                  for (Map.Entry<String, JsonElement> animEntry : animations.entrySet()) {
                     takes.put(suffixOf(animEntry.getKey()), new Take(readLength(animEntry.getValue()).orElse(null), readKeyframes(animEntry.getValue())));
                  }
               }
            } catch (Exception e) {
               SexmodMod.LOGGER.warn("Failed to reload animation file from resource pack: {}", id, e);
            }
         }
      }
      SexmodMod.LOGGER.info("Reloaded animation lengths from ResourceManager for {} characters", TAKES.size());
   }

   private static Map<String, Take> load(String namespace) {
      String path = "/assets/sexmod/geckolib/animations/" + namespace + "/" + namespace + ".animation.json";
      Map<String, Take> takes = new HashMap<>();

      try (InputStream input = GirlAnimationLengths.class.getResourceAsStream(path)) {
         if (input == null) {
            SexmodMod.LOGGER.warn("No animation file for '{}' at {}", namespace, path);
            return takes;
         }

         try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            JsonObject animations = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("animations");
            if (animations == null) {
               return takes;
            }

            for (Map.Entry<String, JsonElement> entry : animations.entrySet()) {
               takes.put(suffixOf(entry.getKey()), new Take(readLength(entry.getValue()).orElse(null), readKeyframes(entry.getValue())));
            }
         }
      } catch (Exception exception) {
         SexmodMod.LOGGER.warn("Failed to read animation lengths for '{}'", namespace, exception);
         return takes;
      }

      SexmodMod.LOGGER.info("Loaded {} animation takes for '{}'", takes.size(), namespace);
      return takes;
   }

   /** The take's sound keyframes, {@code effect} name to the tick it fires on. */
   private static Map<String, Integer> readKeyframes(JsonElement animation) {
      Map<String, Integer> keyframes = new HashMap<>();
      if (!animation.isJsonObject()) {
         return keyframes;
      }

      JsonElement effects = animation.getAsJsonObject().get("sound_effects");
      if (effects == null || !effects.isJsonObject()) {
         return keyframes;
      }

      for (Map.Entry<String, JsonElement> entry : effects.getAsJsonObject().entrySet()) {
         if (entry.getValue().isJsonObject()) {
            JsonElement effect = entry.getValue().getAsJsonObject().get("effect");
            if (effect != null && effect.isJsonPrimitive()) {
               // A name that appears twice in one take keeps its first firing, which is the one a
               // countdown started at the take's first frame would reach.
               keyframes.merge(effect.getAsString(), keyframeTick(entry.getKey()), Math::min);
            }
         }
      }

      return keyframes;
   }

   /** Sound keyframes are keyed by their time in seconds, as a string. */
   private static int keyframeTick(String time) {
      try {
         return Math.max(0, Math.round(Float.parseFloat(time) * SECONDS_TO_TICKS));
      } catch (NumberFormatException exception) {
         return 0;
      }
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
