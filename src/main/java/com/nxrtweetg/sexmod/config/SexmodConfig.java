package com.nxrtweetg.sexmod.config;

import com.nxrtweetg.sexmod.SexmodMod;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;

public final class SexmodConfig {
   private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("sexmod-settings.properties");

   public static final List<String> ALL_CHARACTERS = List.of(
      "jenny", "ellie", "bia", "slime", "bee", "luna", "goblin", "kobold", "allie", "galath", "manglelie"
   );

   public static final Set<GirlSceneAction> UNIVERSAL_SCENES = Collections.unmodifiableSet(EnumSet.of(
      GirlSceneAction.BLOWJOB,
      GirlSceneAction.KOBOLD_BLOWJOB,
      GirlSceneAction.DEEPTHROAT,
      GirlSceneAction.FACEFUCK,
      GirlSceneAction.BOOBJOB,
      GirlSceneAction.TOUCH_BOOBS,
      GirlSceneAction.DOGGY,
      GirlSceneAction.PRONE_DOGGY,
      GirlSceneAction.ANAL,
      GirlSceneAction.ANAL_ALT,
      GirlSceneAction.KOBOLD_ANAL,
      GirlSceneAction.SITTING,
      GirlSceneAction.SITTING_ALT,
      GirlSceneAction.MISSIONARY,
      GirlSceneAction.MATING_PRESS,
      GirlSceneAction.REVERSE_COWGIRL,
      GirlSceneAction.COWGIRL,
      GirlSceneAction.NELSON,
      GirlSceneAction.STRIP
   ));

   private static boolean sceneFadeEnabled = true;
   private static boolean allScenesEnabled = false;
   private static boolean boneCamDynamic = false;
   private static int fadeDurationTicks = 10;
   private static int fadeLeadTicks = 16;

   private static final Map<String, Float> BREAST_SCALES = new HashMap<>();
   private static final Map<String, Float> BOOTY_SCALES = new HashMap<>();
   private static final Map<String, Float> GIRL_SCALES = new HashMap<>();

   private static boolean loaded = false;

   static {
      load();
   }

   private SexmodConfig() {
   }

   public static synchronized void ensureLoaded() {
      if (!loaded) {
         load();
      }
   }

   public static synchronized void load() {
      loaded = true;
      if (!Files.exists(CONFIG_PATH)) {
         return;
      }

      Properties props = new Properties();
      try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
         props.load(in);
         sceneFadeEnabled = Boolean.parseBoolean(props.getProperty("sceneFadeEnabled", "true"));
         allScenesEnabled = Boolean.parseBoolean(props.getProperty("allScenesEnabled", "false"));
         boneCamDynamic = Boolean.parseBoolean(props.getProperty("boneCamDynamic", "false"));
         fadeDurationTicks = clampFadeDuration(Integer.parseInt(props.getProperty("fadeDurationTicks", "10")));
         fadeLeadTicks = clampFadeLead(Integer.parseInt(props.getProperty("fadeLeadTicks", "16")));

         for (String charId : ALL_CHARACTERS) {
            String bKey = "scale." + charId + ".breasts";
            if (props.containsKey(bKey)) {
               BREAST_SCALES.put(charId, clampScale(Float.parseFloat(props.getProperty(bKey))));
            }
            String btKey = "scale." + charId + ".booty";
            if (props.containsKey(btKey)) {
               BOOTY_SCALES.put(charId, clampScale(Float.parseFloat(props.getProperty(btKey))));
            }
            String gKey = "scale." + charId + ".girl";
            if (props.containsKey(gKey)) {
               GIRL_SCALES.put(charId, clampScale(Float.parseFloat(props.getProperty(gKey))));
            }
         }
      } catch (Exception e) {
         SexmodMod.LOGGER.warn("Failed to load sexmod-settings.properties: {}", e.getMessage());
      }
   }

   public static synchronized void save() {
      Properties props = new Properties();
      props.setProperty("sceneFadeEnabled", Boolean.toString(sceneFadeEnabled));
      props.setProperty("allScenesEnabled", Boolean.toString(allScenesEnabled));
      props.setProperty("boneCamDynamic", Boolean.toString(boneCamDynamic));
      props.setProperty("fadeDurationTicks", Integer.toString(fadeDurationTicks));
      props.setProperty("fadeLeadTicks", Integer.toString(fadeLeadTicks));

      for (String charId : ALL_CHARACTERS) {
         float b = getBreastScale(charId);
         if (b != 1.0F) {
            props.setProperty("scale." + charId + ".breasts", String.format(java.util.Locale.ROOT, "%.2f", b));
         }
         float bt = getBootyScale(charId);
         if (bt != 1.0F) {
            props.setProperty("scale." + charId + ".booty", String.format(java.util.Locale.ROOT, "%.2f", bt));
         }
         float g = getGirlScale(charId);
         if (g != 1.0F) {
            props.setProperty("scale." + charId + ".girl", String.format(java.util.Locale.ROOT, "%.2f", g));
         }
      }

      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
            props.store(out, "SexMod Global Configuration");
         }
      } catch (Exception e) {
         SexmodMod.LOGGER.warn("Failed to save sexmod-settings.properties: {}", e.getMessage());
      }
   }

   public static boolean isSceneFadeEnabled() {
      ensureLoaded();
      return sceneFadeEnabled;
   }

   public static void setSceneFadeEnabled(boolean enabled) {
      ensureLoaded();
      sceneFadeEnabled = enabled;
      save();
   }

   public static int getFadeDurationTicks() {
      ensureLoaded();
      return fadeDurationTicks;
   }

   public static void setFadeDurationTicks(int ticks) {
      ensureLoaded();
      fadeDurationTicks = clampFadeDuration(ticks);
      save();
   }

   public static int getFadeLeadTicks() {
      ensureLoaded();
      return fadeLeadTicks;
   }

   public static void setFadeLeadTicks(int ticks) {
      ensureLoaded();
      fadeLeadTicks = clampFadeLead(ticks);
      save();
   }

   public static int clampFadeDuration(int val) {
      return Math.max(4, Math.min(20, val));
   }

   public static int clampFadeLead(int val) {
      return Math.max(6, Math.min(26, val));
   }

   public static boolean isAllScenesEnabled() {
      ensureLoaded();
      return allScenesEnabled;
   }

   public static void setAllScenesEnabled(boolean enabled) {
      ensureLoaded();
      allScenesEnabled = enabled;
      save();
   }

   public static boolean isBoneCamDynamic() {
      ensureLoaded();
      return boneCamDynamic;
   }

   public static void setBoneCamDynamic(boolean dynamic) {
      ensureLoaded();
      boneCamDynamic = dynamic;
      save();
   }

   public static float getBreastScale(String character) {
      ensureLoaded();
      return BREAST_SCALES.getOrDefault(character.toLowerCase(), 1.0F);
   }

   public static void setBreastScale(String character, float scale) {
      ensureLoaded();
      BREAST_SCALES.put(character.toLowerCase(), clampScale(scale));
      save();
   }

   public static float getBootyScale(String character) {
      ensureLoaded();
      return BOOTY_SCALES.getOrDefault(character.toLowerCase(), 1.0F);
   }

   public static void setBootyScale(String character, float scale) {
      ensureLoaded();
      BOOTY_SCALES.put(character.toLowerCase(), clampScale(scale));
      save();
   }

   public static float getGirlScale(String character) {
      ensureLoaded();
      return GIRL_SCALES.getOrDefault(character.toLowerCase(), 1.0F);
   }

   public static void setGirlScale(String character, float scale) {
      ensureLoaded();
      GIRL_SCALES.put(character.toLowerCase(), clampScale(scale));
      save();
   }

   public static void resetCharacter(String character) {
      ensureLoaded();
      String key = character.toLowerCase();
      BREAST_SCALES.remove(key);
      BOOTY_SCALES.remove(key);
      GIRL_SCALES.remove(key);
      save();
   }

   public static void resetAll() {
      ensureLoaded();
      BREAST_SCALES.clear();
      BOOTY_SCALES.clear();
      GIRL_SCALES.clear();
      save();
   }

   public static float clampScale(float val) {
      return Math.round(Math.max(0.1F, Math.min(3.0F, val)) * 100.0F) / 100.0F;
   }
}
