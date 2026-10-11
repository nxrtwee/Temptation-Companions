package com.nxrtweetg.sexmod.girls.scene;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Where the player and the girl stand relative to each other in a scene, tuned in-game with the
 * scene editor.
 *
 * <p>Keys are {@code <character>.<profile>.<scene>.<field>}. Girls differ in height and proportion,
 * so each keeps her own numbers, but an untuned one reads Jenny's as her starting point rather than
 * zeroes. Keys written before characters existed carry no prefix and are read as Jenny's.
 */
public final class GirlSceneLayoutConfig {
   private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("sexmod-jenny-scene-layout.properties");
   private static final String BASE_CHARACTER = "jenny";
   /**
    * Every scene may be tuned. The scenes that ship with no offsets of their own simply start from
    * zero, which is what they were rendering at while they were locked out of the editor.
    */
   private static final Set<GirlSceneAction> TUNABLE = Set.of(GirlSceneAction.values());
   /** character -> profile -> scene -> offsets */
   private static final Map<String, Map<LayoutProfile, EnumMap<GirlSceneAction, SceneOffsets>>> OFFSETS = new HashMap<>();
   private static boolean loaded;

   private GirlSceneLayoutConfig() {
   }

   public static SceneOffsets get(String character, GirlSceneAction action, GirlScenePhase phase) {
      ensureLoaded();
      LayoutProfile profile = profileForPhase(phase);
      SceneOffsets own = profileOffsets(character, profile).get(action);
      if (own != null) {
         return own;
      }

      return profileOffsets(BASE_CHARACTER, profile).getOrDefault(action, SceneOffsets.ZERO);
   }

   public static void set(String character, GirlSceneAction action, GirlScenePhase phase, double forward, double side, double vertical) {
      if (supportsForTuning(action)) {
         ensureLoaded();
         profileOffsets(character, profileForPhase(phase)).put(action, new SceneOffsets(forward, side, vertical));
         save();
      }
   }

   public static boolean supportsForTuning(GirlSceneAction action) {
      return TUNABLE.contains(action);
   }

   /**
    * The offsets as measured in-game, per girl. A girl with no entry for a scene still falls back to
    * Jenny's at lookup time, and anything the player tunes later overwrites these on load.
    */
   private static void seedMeasuredOffsets() {
      seed("allie", LayoutProfile.MAIN, GirlSceneAction.DEEPTHROAT, 0.0, 0.0, 0.0);
      seed("allie", LayoutProfile.MAIN, GirlSceneAction.MISSIONARY, 0.0, 0.0, 0.0);
      seed("bia", LayoutProfile.INTRO, GirlSceneAction.ANAL, -15.0, 0.0, 0.0);
      seed("bia", LayoutProfile.INTRO, GirlSceneAction.ANAL_ALT, -15.0, 0.0, 0.0);
      seed("bia", LayoutProfile.INTRO, GirlSceneAction.BLOWJOB, 0.05, 0.0, 0.25);
      seed("bia", LayoutProfile.INTRO, GirlSceneAction.COWGIRL, -15.0, 0.0, 0.0);
      seed("bia", LayoutProfile.INTRO, GirlSceneAction.DOGGY, -15.0, 0.0, 0.0);
      seed("bia", LayoutProfile.INTRO, GirlSceneAction.MISSIONARY, -15.0, 0.0, 0.0);
      seed("bia", LayoutProfile.INTRO, GirlSceneAction.PRONE_DOGGY, -15.0, 0.0, 0.0);
      seed("bia", LayoutProfile.INTRO, GirlSceneAction.REVERSE_COWGIRL, -15.0, 0.0, 0.0);
      seed("bia", LayoutProfile.INTRO, GirlSceneAction.SITTING_ALT, -15.0, 0.0, 0.0);
      seed("bia", LayoutProfile.MAIN, GirlSceneAction.ANAL, -15.0, 0.0, 0.0);
      seed("bia", LayoutProfile.MAIN, GirlSceneAction.ANAL_ALT, -15.0, 0.0, 0.0);
      seed("bia", LayoutProfile.MAIN, GirlSceneAction.BLOWJOB, 0.0, 0.2, -0.05);
      seed("bia", LayoutProfile.MAIN, GirlSceneAction.COWGIRL, -15.0, 0.0, 0.0);
      seed("bia", LayoutProfile.MAIN, GirlSceneAction.DOGGY, -15.0, 0.0, 0.0);
      seed("bia", LayoutProfile.MAIN, GirlSceneAction.FACEFUCK, -0.05, 0.05, 0.0);
      seed("bia", LayoutProfile.MAIN, GirlSceneAction.MISSIONARY, -15.0, 0.0, 0.0);
      seed("bia", LayoutProfile.MAIN, GirlSceneAction.PRONE_DOGGY, -15.0, 0.0, 0.0);
      seed("bia", LayoutProfile.MAIN, GirlSceneAction.REVERSE_COWGIRL, -15.0, 0.0, 0.0);
      seed("bia", LayoutProfile.MAIN, GirlSceneAction.SITTING_ALT, -15.0, 0.0, 0.0);
      seed("bia", LayoutProfile.MAIN, GirlSceneAction.TOUCH_BOOBS, -2.3, -0.3, 0.95);
      seed("bia", LayoutProfile.PAYMENT, GirlSceneAction.BLOWJOB, 0.0, 0.0, -0.05);
      seed("bia", LayoutProfile.PAYMENT, GirlSceneAction.TOUCH_BOOBS, 0.0, -0.3, 3.65);
      seed("bia", LayoutProfile.WAITING, GirlSceneAction.ANAL, 0.0, 0.0, 0.0);
      seed("bia", LayoutProfile.WAITING, GirlSceneAction.ANAL_ALT, 0.0, 0.0, 0.0);
      seed("bia", LayoutProfile.WAITING, GirlSceneAction.BLOWJOB, 0.0, 0.0, 0.0);
      seed("bia", LayoutProfile.WAITING, GirlSceneAction.COWGIRL, 0.0, 0.0, 0.0);
      seed("bia", LayoutProfile.WAITING, GirlSceneAction.DOGGY, 0.0, 0.0, 0.0);
      seed("bia", LayoutProfile.WAITING, GirlSceneAction.MISSIONARY, 0.0, 0.0, 0.0);
      seed("bia", LayoutProfile.WAITING, GirlSceneAction.PRONE_DOGGY, 0.0, 0.0, 0.0);
      seed("bia", LayoutProfile.WAITING, GirlSceneAction.REVERSE_COWGIRL, 0.0, 0.0, 0.0);
      seed("bia", LayoutProfile.WAITING, GirlSceneAction.SITTING_ALT, 0.0, 0.0, 0.0);
      seed("ellie", LayoutProfile.INTRO, GirlSceneAction.ANAL, 0.0, 0.0, 0.0);
      seed("ellie", LayoutProfile.INTRO, GirlSceneAction.TOUCH_BOOBS, -4.0, 0.0, -3.0);
      seed("ellie", LayoutProfile.MAIN, GirlSceneAction.DOGGY, 0.0, 0.0, 0.0);
      seed("ellie", LayoutProfile.MAIN, GirlSceneAction.FACEFUCK, 0.35, -0.05, 0.0);
      seed("ellie", LayoutProfile.MAIN, GirlSceneAction.KOBOLD_ANAL, 0.0, 0.0, 0.0);
      seed("ellie", LayoutProfile.MAIN, GirlSceneAction.TOUCH_BOOBS, -20.9, -0.3, -0.45);
      seed("jenny", LayoutProfile.INTRO, GirlSceneAction.ANAL, 0.0, 0.0, 0.0);
      seed("jenny", LayoutProfile.INTRO, GirlSceneAction.COWGIRL, 0.0, 0.0, 0.0);
      seed("jenny", LayoutProfile.INTRO, GirlSceneAction.DEEPTHROAT, 0.95, 0.05, 1.0);
      seed("jenny", LayoutProfile.INTRO, GirlSceneAction.DOGGY, 0.0, 0.0, 0.0);
      seed("jenny", LayoutProfile.INTRO, GirlSceneAction.FACEFUCK, 0.4, -0.05, 0.0);
      seed("jenny", LayoutProfile.INTRO, GirlSceneAction.KOBOLD_BLOWJOB, 0.45, -0.05, -0.05);
      seed("jenny", LayoutProfile.INTRO, GirlSceneAction.MATING_PRESS, 0.0, 0.0, 0.0);
      seed("jenny", LayoutProfile.INTRO, GirlSceneAction.MISSIONARY, 0.0, 0.0, 0.0);
      seed("jenny", LayoutProfile.INTRO, GirlSceneAction.PRONE_DOGGY, 0.0, 0.0, 0.0);
      seed("jenny", LayoutProfile.INTRO, GirlSceneAction.REVERSE_COWGIRL, 0.0, 0.0, 0.0);
      seed("jenny", LayoutProfile.INTRO, GirlSceneAction.SITTING, 0.0, 0.0, 0.0);
      seed("jenny", LayoutProfile.INTRO, GirlSceneAction.TOUCH_BOOBS, -0.25, 0.0, 0.1);
      seed("jenny", LayoutProfile.MAIN, GirlSceneAction.ANAL, 0.0, 0.0, 0.0);
      seed("jenny", LayoutProfile.MAIN, GirlSceneAction.COWGIRL, 0.0, 0.0, 0.0);
      seed("jenny", LayoutProfile.MAIN, GirlSceneAction.DEEPTHROAT, 14.0, 0.0, 15.0);
      seed("jenny", LayoutProfile.MAIN, GirlSceneAction.DOGGY, 0.0, 0.0, 0.0);
      seed("jenny", LayoutProfile.MAIN, GirlSceneAction.FACEFUCK, 0.35, -0.05, 0.0);
      seed("jenny", LayoutProfile.MAIN, GirlSceneAction.KOBOLD_BLOWJOB, 0.45, -0.05, -0.05);
      seed("jenny", LayoutProfile.MAIN, GirlSceneAction.MATING_PRESS, 0.0, 0.0, 0.0);
      seed("jenny", LayoutProfile.MAIN, GirlSceneAction.MISSIONARY, 0.0, 0.0, 0.0);
      seed("jenny", LayoutProfile.MAIN, GirlSceneAction.PRONE_DOGGY, 0.0, 0.0, 0.0);
      seed("jenny", LayoutProfile.MAIN, GirlSceneAction.REVERSE_COWGIRL, 0.0, 0.0, 0.0);
      seed("jenny", LayoutProfile.MAIN, GirlSceneAction.SITTING, 0.0, 0.0, 0.0);
      seed("jenny", LayoutProfile.MAIN, GirlSceneAction.TOUCH_BOOBS, -21.35, -0.3, -0.6);
      seed("jenny", LayoutProfile.PAYMENT, GirlSceneAction.ANAL, 0.0, 0.0, 0.0);
      seed("jenny", LayoutProfile.PAYMENT, GirlSceneAction.COWGIRL, 0.0, 0.0, 0.0);
      seed("jenny", LayoutProfile.PAYMENT, GirlSceneAction.DEEPTHROAT, 0.05, 0.0, 0.0);
      seed("jenny", LayoutProfile.PAYMENT, GirlSceneAction.DOGGY, 0.0, 0.0, 0.0);
      seed("jenny", LayoutProfile.PAYMENT, GirlSceneAction.FACEFUCK, 0.4, -0.05, 0.0);
      seed("jenny", LayoutProfile.PAYMENT, GirlSceneAction.KOBOLD_BLOWJOB, 0.1, 0.0, -0.05);
      seed("jenny", LayoutProfile.PAYMENT, GirlSceneAction.MATING_PRESS, 0.0, 0.0, 0.0);
      seed("jenny", LayoutProfile.PAYMENT, GirlSceneAction.MISSIONARY, 0.0, 0.0, 0.0);
      seed("jenny", LayoutProfile.PAYMENT, GirlSceneAction.PRONE_DOGGY, 0.0, 0.0, 0.0);
      seed("jenny", LayoutProfile.PAYMENT, GirlSceneAction.REVERSE_COWGIRL, 0.0, 0.0, 0.0);
      seed("jenny", LayoutProfile.PAYMENT, GirlSceneAction.SITTING, 0.0, 0.0, 0.0);
      seed("jenny", LayoutProfile.PAYMENT, GirlSceneAction.TOUCH_BOOBS, -0.05, 0.0, 0.1);
      seed("luna", LayoutProfile.MAIN, GirlSceneAction.BLOWJOB, 0.1, 0.05, 0.2);
      seed("luna", LayoutProfile.MAIN, GirlSceneAction.BOOBJOB, 0.05, 0.1, 0.0);
      seed("luna", LayoutProfile.MAIN, GirlSceneAction.DEEPTHROAT, 12.2, -0.1, 15.45);
      seed("luna", LayoutProfile.MAIN, GirlSceneAction.FACEFUCK, 0.35, 0.0, 0.0);
      seed("luna", LayoutProfile.MAIN, GirlSceneAction.MISSIONARY, 0.0, 0.0, 0.0);
      seed("luna", LayoutProfile.MAIN, GirlSceneAction.TOUCH_BOOBS, 0.0, 0.0, 0.0);
      seed("luna", LayoutProfile.PAYMENT, GirlSceneAction.TOUCH_BOOBS, 0.0, -0.35, 1.95);
   }

   private static void seed(String character, LayoutProfile profile, GirlSceneAction action, double forward, double side, double vertical) {
      profileOffsets(character, profile).put(action, new SceneOffsets(forward, side, vertical));
   }

   private static EnumMap<GirlSceneAction, SceneOffsets> profileOffsets(String character, LayoutProfile profile) {
      return OFFSETS.computeIfAbsent(character, key -> new EnumMap<>(LayoutProfile.class))
         .computeIfAbsent(profile, key -> new EnumMap<>(GirlSceneAction.class));
   }

   private static void ensureLoaded() {
      if (loaded) {
         return;
      }

      loaded = true;
      seedMeasuredOffsets();

      if (!Files.exists(CONFIG_PATH)) {
         return;
      }

      Properties properties = new Properties();

      try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
         properties.load(input);
      } catch (IOException exception) {
         return;
      }

      for (String character : characterNames(properties)) {
         for (LayoutProfile profile : LayoutProfile.values()) {
            for (GirlSceneAction action : TUNABLE) {
               load(properties, character, profile, action);
            }
         }
      }
   }

   /** The prefixes present in the file, plus Jenny for the unprefixed keys older versions wrote. */
   private static Set<String> characterNames(Properties properties) {
      Set<String> names = new LinkedHashSet<>();
      names.add(BASE_CHARACTER);

      for (String key : properties.stringPropertyNames()) {
         String[] parts = key.split("\\.");
         if (parts.length == 4) {
            names.add(parts[0]);
         }
      }

      return names;
   }

   private static void load(Properties properties, String character, LayoutProfile profile, GirlSceneAction action) {
      String prefix = keyPrefix(character, profile, action);
      if (!has(properties, prefix)) {
         // pre-character layouts: "<profile>.<scene>", and older still, a bare "<scene>" for MAIN
         if (!character.equals(BASE_CHARACTER)) {
            return;
         }

         prefix = profile.key + "." + action.legacyId();
         if (!has(properties, prefix)) {
            prefix = action.legacyId();
            if (profile != LayoutProfile.MAIN || !has(properties, prefix)) {
               return;
            }
         }
      }

      profileOffsets(character, profile)
         .put(
            action,
            new SceneOffsets(
               parse(properties.getProperty(prefix + ".forward")),
               parse(properties.getProperty(prefix + ".side")),
               parse(properties.getProperty(prefix + ".vertical"))
            )
         );
   }

   private static boolean has(Properties properties, String prefix) {
      return properties.containsKey(prefix + ".forward") || properties.containsKey(prefix + ".side") || properties.containsKey(prefix + ".vertical");
   }

   private static double parse(String value) {
      if (value == null || value.isBlank()) {
         return 0.0;
      }

      try {
         return Double.parseDouble(value);
      } catch (NumberFormatException exception) {
         return 0.0;
      }
   }

   private static void save() {
      Properties properties = new Properties();

      for (Map.Entry<String, Map<LayoutProfile, EnumMap<GirlSceneAction, SceneOffsets>>> character : OFFSETS.entrySet()) {
         for (Map.Entry<LayoutProfile, EnumMap<GirlSceneAction, SceneOffsets>> profile : character.getValue().entrySet()) {
            for (Map.Entry<GirlSceneAction, SceneOffsets> scene : profile.getValue().entrySet()) {
               String prefix = keyPrefix(character.getKey(), profile.getKey(), scene.getKey());
               properties.setProperty(prefix + ".forward", Double.toString(scene.getValue().forward()));
               properties.setProperty(prefix + ".side", Double.toString(scene.getValue().side()));
               properties.setProperty(prefix + ".vertical", Double.toString(scene.getValue().vertical()));
            }
         }
      }

      try {
         Files.createDirectories(CONFIG_PATH.getParent());

         try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
            properties.store(output, "Scene layout tuning, keyed by character");
         }
      } catch (IOException exception) {
      }
   }

   private static String keyPrefix(String character, LayoutProfile profile, GirlSceneAction action) {
      return character + "." + profile.key + "." + action.legacyId();
   }

   private static LayoutProfile profileForPhase(GirlScenePhase phase) {
      if (phase == GirlScenePhase.PAYMENT) {
         return LayoutProfile.PAYMENT;
      }
      if (phase == GirlScenePhase.WAITING || phase == GirlScenePhase.POSITIONING) {
         return LayoutProfile.WAITING;
      }

      return phase == GirlScenePhase.INTRO ? LayoutProfile.INTRO : LayoutProfile.MAIN;
   }

   private enum LayoutProfile {
      MAIN("main"),
      INTRO("intro"),
      PAYMENT("payment"),
      WAITING("waiting");

      private final String key;

      LayoutProfile(String key) {
         this.key = key;
      }
   }

   public record SceneOffsets(double forward, double side, double vertical) {
      private static final SceneOffsets ZERO = new SceneOffsets(0.0, 0.0, 0.0);
   }
}
