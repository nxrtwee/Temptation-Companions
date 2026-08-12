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
 * Per-scene body proportions, tuned in-game. Keys are {@code <character>.<scene>.<field>}; keys
 * without a character prefix predate the roster and are read as Jenny's. An untuned girl inherits
 * Jenny's numbers rather than starting from 1.0.
 */
public final class GirlSceneBodyConfig {
   private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("sexmod-jenny-scene-body.properties");
   private static final String BASE_CHARACTER = "jenny";
   private static final Map<String, EnumMap<GirlSceneAction, BodySettings>> SETTINGS = new HashMap<>();
   private static boolean loaded;

   private GirlSceneBodyConfig() {
   }

   public static BodySettings get(String character, GirlSceneAction action) {
      ensureLoaded();
      BodySettings own = settings(character).get(action);
      if (own != null) {
         return own;
      }

      return settings(BASE_CHARACTER).getOrDefault(action, BodySettings.DEFAULT);
   }

   public static double breastScale(String character, GirlSceneAction action) {
      return get(character, action).breastScale();
   }

   public static double bootyScale(String character, GirlSceneAction action) {
      return get(character, action).bootyScale();
   }

   public static double adjustBreastScale(String character, GirlSceneAction action, double delta) {
      BodySettings current = get(character, action);
      BodySettings next = new BodySettings(clampScale(current.breastScale() + delta), current.bootyScale());
      settings(character).put(action, next);
      save();
      return next.breastScale();
   }

   public static double adjustBootyScale(String character, GirlSceneAction action, double delta) {
      BodySettings current = get(character, action);
      BodySettings next = new BodySettings(current.breastScale(), clampScale(current.bootyScale() + delta));
      settings(character).put(action, next);
      save();
      return next.bootyScale();
   }

   public static double setBreastScale(String character, GirlSceneAction action, double value) {
      BodySettings current = get(character, action);
      BodySettings next = new BodySettings(clampScale(value), current.bootyScale());
      settings(character).put(action, next);
      save();
      return next.breastScale();
   }

   public static double setBootyScale(String character, GirlSceneAction action, double value) {
      BodySettings current = get(character, action);
      BodySettings next = new BodySettings(current.breastScale(), clampScale(value));
      settings(character).put(action, next);
      save();
      return next.bootyScale();
   }

   /** The proportions as measured in-game; a scene left at Jenny's value is simply absent. */
   private static void seedMeasuredScales() {
      seed("bia", GirlSceneAction.MISSIONARY, 1.15, 0.55);
      seed("bia", GirlSceneAction.TOUCH_BOOBS, 1.2, 1.0);
      seed("ellie", GirlSceneAction.TOUCH_BOOBS, 1.0, 1.0);
      seed("jenny", GirlSceneAction.REVERSE_COWGIRL, 1.0, 1.1);
      seed("jenny", GirlSceneAction.SITTING, 1.0, 1.15);
      seed("jenny", GirlSceneAction.TOUCH_BOOBS, 1.1, 1.0);
      seed("luna", GirlSceneAction.BOOBJOB, 1.6, 1.0);
      seed("luna", GirlSceneAction.MISSIONARY, 1.15, 1.0);
      seed("luna", GirlSceneAction.TOUCH_BOOBS, 1.2, 1.0);
   }

   private static void seed(String character, GirlSceneAction action, double breasts, double booty) {
      settings(character).put(action, new BodySettings(clampScale(breasts), clampScale(booty)));
   }

   private static EnumMap<GirlSceneAction, BodySettings> settings(String character) {
      return SETTINGS.computeIfAbsent(character, key -> new EnumMap<>(GirlSceneAction.class));
   }

   private static void ensureLoaded() {
      if (loaded) {
         return;
      }

      loaded = true;
      seedMeasuredScales();
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
         for (GirlSceneAction action : GirlSceneAction.values()) {
            load(properties, character, action);
         }
      }
   }

   /** The prefixes present in the file, plus Jenny for the unprefixed keys older versions wrote. */
   private static Set<String> characterNames(Properties properties) {
      Set<String> names = new LinkedHashSet<>();
      names.add(BASE_CHARACTER);

      for (String key : properties.stringPropertyNames()) {
         String[] parts = key.split("\\.");
         if (parts.length == 3) {
            names.add(parts[0]);
         }
      }

      return names;
   }

   private static void load(Properties properties, String character, GirlSceneAction action) {
      String prefix = character + "." + action.legacyId();
      if (!has(properties, prefix)) {
         if (!character.equals(BASE_CHARACTER)) {
            return;
         }

         prefix = action.legacyId();
         if (!has(properties, prefix)) {
            return;
         }
      }

      settings(character)
         .put(
            action,
            new BodySettings(
               clampScale(parse(properties.getProperty(prefix + ".breasts"), 1.0)), clampScale(parse(properties.getProperty(prefix + ".booty"), 1.0))
            )
         );
   }

   private static boolean has(Properties properties, String prefix) {
      return properties.containsKey(prefix + ".breasts") || properties.containsKey(prefix + ".booty");
   }

   private static double parse(String value, double fallback) {
      if (value == null || value.isBlank()) {
         return fallback;
      }

      try {
         return Double.parseDouble(value);
      } catch (NumberFormatException exception) {
         return fallback;
      }
   }

   private static void save() {
      Properties properties = new Properties();

      for (Map.Entry<String, EnumMap<GirlSceneAction, BodySettings>> character : SETTINGS.entrySet()) {
         for (Map.Entry<GirlSceneAction, BodySettings> scene : character.getValue().entrySet()) {
            String prefix = character.getKey() + "." + scene.getKey().legacyId();
            properties.setProperty(prefix + ".breasts", Double.toString(scene.getValue().breastScale()));
            properties.setProperty(prefix + ".booty", Double.toString(scene.getValue().bootyScale()));
         }
      }

      try {
         Files.createDirectories(CONFIG_PATH.getParent());

         try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
            properties.store(output, "Scene body tuning, keyed by character");
         }
      } catch (IOException exception) {
      }
   }

   private static double clampScale(double value) {
      return Math.max(0.5, Math.min(2.0, value));
   }

   public record BodySettings(double breastScale, double bootyScale) {
      private static final BodySettings DEFAULT = new BodySettings(1.0, 1.0);
   }
}
