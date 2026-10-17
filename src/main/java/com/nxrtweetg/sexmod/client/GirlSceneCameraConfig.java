package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.girls.scene.GirlScenePhase;
import com.nxrtweetg.sexmod.config.SexmodConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public final class GirlSceneCameraConfig {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("sexmod-camera.json");
   private static final String BASE_CHARACTER = "jenny";
   private static final RosterSettings BUNDLED_DEFAULTS = loadBundledDefaults();
   private static RosterSettings roster = new RosterSettings();

   private static RosterSettings loadBundledDefaults() {
      try (java.io.InputStream in = GirlSceneCameraConfig.class.getResourceAsStream("/assets/sexmod/config/default-camera.json")) {
         if (in != null) {
            try (Reader reader = new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8)) {
               RosterSettings loaded = GSON.fromJson(reader, RosterSettings.class);
               if (loaded != null) {
                  return loaded.sanitized();
               }
            }
         }
      } catch (Exception ignored) {
      }
      return null;
   }

   private GirlSceneCameraConfig() {
   }

   public static void load() {
      if (!Files.exists(CONFIG_PATH)) {
         if (BUNDLED_DEFAULTS != null) {
            roster = GSON.fromJson(GSON.toJson(BUNDLED_DEFAULTS), RosterSettings.class).sanitized();
         } else {
            roster = new RosterSettings().sanitized();
         }
         save();
         return;
      }

      try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
         RosterSettings loaded = GSON.fromJson(reader, RosterSettings.class);
         roster = loaded == null ? new RosterSettings() : loaded;
      } catch (IOException exception) {
         roster = new RosterSettings();
      }

      roster = roster.sanitized();
   }

   public static void save() {
      try {
         Files.createDirectories(CONFIG_PATH.getParent());

         try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(roster.sanitized(), writer);
         }
      } catch (IOException ioexception) {
      }
   }

   public static double distanceMultiplier(String character, GirlSceneAction action) {
      return distanceMultiplier(character, action, GirlScenePhase.ACTIVE);
   }

   public static double distanceMultiplier(String character, GirlSceneAction action, GirlScenePhase phase) {
      SceneCameraSettings s = settings(character, action, phase);
      if (SexmodConfig.isBoneCamDynamic() && s.dynamicDistanceMultiplier != null) {
         return s.dynamicDistanceMultiplier;
      }
      return s.distanceMultiplier;
   }

   public static double verticalOffset(String character, GirlSceneAction action) {
      return verticalOffset(character, action, GirlScenePhase.ACTIVE);
   }

   public static double verticalOffset(String character, GirlSceneAction action, GirlScenePhase phase) {
      SceneCameraSettings s = settings(character, action, phase);
      if (SexmodConfig.isBoneCamDynamic() && s.dynamicVerticalOffset != null) {
         return s.dynamicVerticalOffset;
      }
      return s.verticalOffset;
   }

   public static double lateralOffset(String character, GirlSceneAction action) {
      return lateralOffset(character, action, GirlScenePhase.ACTIVE);
   }

   public static double lateralOffset(String character, GirlSceneAction action, GirlScenePhase phase) {
      SceneCameraSettings s = settings(character, action, phase);
      if (SexmodConfig.isBoneCamDynamic() && s.dynamicLateralOffset != null) {
         return s.dynamicLateralOffset;
      }
      return s.lateralOffset;
   }

   public static double helperVerticalOffset(String character, GirlSceneAction action) {
      return helperVerticalOffset(character, action, GirlScenePhase.ACTIVE);
   }

   public static double helperVerticalOffset(String character, GirlSceneAction action, GirlScenePhase phase) {
      return settings(character, action, phase).helperVerticalOffset;
   }

   public static double helperScale(String character, GirlSceneAction action) {
      return helperScale(character, action, GirlScenePhase.ACTIVE);
   }

   public static double helperScale(String character, GirlSceneAction action, GirlScenePhase phase) {
      return settings(character, action, phase).helperScale;
   }

   public static double helperForwardOffset(String character, GirlSceneAction action) {
      return helperForwardOffset(character, action, GirlScenePhase.ACTIVE);
   }

   public static double helperForwardOffset(String character, GirlSceneAction action, GirlScenePhase phase) {
      return settings(character, action, phase).helperForwardOffset;
   }

   public static double helperSideOffset(String character, GirlSceneAction action) {
      return helperSideOffset(character, action, GirlScenePhase.ACTIVE);
   }

   public static double helperSideOffset(String character, GirlSceneAction action, GirlScenePhase phase) {
      return settings(character, action, phase).helperSideOffset;
   }

   /** Field of a scene's camera entry, so the editor can read and write one by name. */
   @Environment(EnvType.CLIENT)
   public enum Field {
      DISTANCE,
      VERTICAL,
      LATERAL,
      HELPER_VERTICAL,
      HELPER_FORWARD,
      HELPER_SIDE,
      HELPER_SCALE,
      GIRL_SCALE,
      /** -1 leaves the choice to {@link GirlSceneCamRig#boneByDefault}, 0 forces off, 1 forces on. */
      CAMERA_MODE,
      BONE_VERTICAL,
      BONE_FORWARD,
      BONE_SIDE;
   }

   /**
    * How a scene picks its camera: {@link CameraMode#AUTO} defers to
    * {@link GirlSceneCamRig#boneByDefault}, the other two force the choice.
    */
   @Environment(EnvType.CLIENT)
   public enum CameraMode {
      AUTO,
      LEGACY,
      BONE;
   }

   public static GirlSceneCameraConfig.CameraMode cameraMode(String character, GirlSceneAction action, GirlScenePhase phase) {
      if (SexmodConfig.isBoneCamDynamic()) {
         return GirlSceneCameraConfig.CameraMode.BONE;
      }
      return GirlSceneCameraConfig.CameraMode.LEGACY;
   }

   public static double boneVerticalOffset(String character, GirlSceneAction action, GirlScenePhase phase) {
      return settings(character, action, phase).boneVerticalOffset;
   }

   public static double boneForwardOffset(String character, GirlSceneAction action, GirlScenePhase phase) {
      return settings(character, action, phase).boneForwardOffset;
   }

   public static double boneSideOffset(String character, GirlSceneAction action, GirlScenePhase phase) {
      return settings(character, action, phase).boneSideOffset;
   }

   public static double get(String character, GirlSceneAction action, GirlScenePhase phase, Field field) {
      GirlSceneCameraConfig.SceneCameraSettings settings = settings(character, action, phase);
      return switch (field) {
         case DISTANCE -> distanceMultiplier(character, action, phase);
         case VERTICAL -> verticalOffset(character, action, phase);
         case LATERAL -> lateralOffset(character, action, phase);
         case HELPER_VERTICAL -> settings.helperVerticalOffset;
         case HELPER_FORWARD -> settings.helperForwardOffset;
         case HELPER_SIDE -> settings.helperSideOffset;
         case HELPER_SCALE -> settings.helperScale;
         case GIRL_SCALE -> settings.girlScale;
         case CAMERA_MODE -> settings.boneCamera;
         case BONE_VERTICAL -> settings.boneVerticalOffset;
         case BONE_FORWARD -> settings.boneForwardOffset;
         case BONE_SIDE -> settings.boneSideOffset;
      };
   }

   public static void set(String character, GirlSceneAction action, GirlScenePhase phase, Field field, double value) {
      GirlSceneCameraConfig.SceneCameraSettings settings = mutableSettings(character, action, phase);
      if (SexmodConfig.isBoneCamDynamic()) {
         switch (field) {
            case DISTANCE -> settings.dynamicDistanceMultiplier = value;
            case VERTICAL -> settings.dynamicVerticalOffset = value;
            case LATERAL -> settings.dynamicLateralOffset = value;
            case HELPER_VERTICAL -> settings.helperVerticalOffset = value;
            case HELPER_FORWARD -> settings.helperForwardOffset = value;
            case HELPER_SIDE -> settings.helperSideOffset = value;
            case HELPER_SCALE -> settings.helperScale = value;
            case GIRL_SCALE -> settings.girlScale = value;
            case CAMERA_MODE -> settings.boneCamera = value;
            case BONE_VERTICAL -> settings.boneVerticalOffset = value;
            case BONE_FORWARD -> settings.boneForwardOffset = value;
            case BONE_SIDE -> settings.boneSideOffset = value;
         }
      } else {
         switch (field) {
            case DISTANCE -> settings.distanceMultiplier = value;
            case VERTICAL -> settings.verticalOffset = value;
            case LATERAL -> settings.lateralOffset = value;
            case HELPER_VERTICAL -> settings.helperVerticalOffset = value;
            case HELPER_FORWARD -> settings.helperForwardOffset = value;
            case HELPER_SIDE -> settings.helperSideOffset = value;
            case HELPER_SCALE -> settings.helperScale = value;
            case GIRL_SCALE -> settings.girlScale = value;
            case CAMERA_MODE -> settings.boneCamera = value;
            case BONE_VERTICAL -> settings.boneVerticalOffset = value;
            case BONE_FORWARD -> settings.boneForwardOffset = value;
            case BONE_SIDE -> settings.boneSideOffset = value;
         }
      }

      roster = roster.sanitized();
      save();
   }

   public static double girlScale(String character, GirlSceneAction action, GirlScenePhase phase) {
      return settings(character, action, phase).girlScale;
   }

   public static double adjustGirlScale(String character, GirlSceneAction action, GirlScenePhase phase, double delta) {
      mutableSettings(character, action, phase).girlScale += delta;
      roster = roster.sanitized();
      save();
      return settings(character, action, phase).girlScale;
   }

   /** Puts both bodies back to their authored size without touching the rest of the tuning. */
   public static void resetScales(String character, GirlSceneAction action, GirlScenePhase phase) {
      GirlSceneCameraConfig.SceneCameraSettings settings = mutableSettings(character, action, phase);
      settings.girlScale = 1.0;
      settings.helperScale = 1.0;
      roster = roster.sanitized();
      save();
   }

   public static double adjustDistance(String character, GirlSceneAction action, double delta) {
      return adjustDistance(character, action, GirlScenePhase.ACTIVE, delta);
   }

   public static double adjustDistance(String character, GirlSceneAction action, GirlScenePhase phase, double delta) {
      SceneCameraSettings settings = mutableSettings(character, action, phase);
      if (SexmodConfig.isBoneCamDynamic()) {
         double base = settings.dynamicDistanceMultiplier != null ? settings.dynamicDistanceMultiplier : settings.distanceMultiplier;
         settings.dynamicDistanceMultiplier = base + delta;
      } else {
         settings.distanceMultiplier += delta;
      }
      roster = roster.sanitized();
      save();
      return distanceMultiplier(character, action, phase);
   }

   public static double adjustVerticalOffset(String character, GirlSceneAction action, double delta) {
      return adjustVerticalOffset(character, action, GirlScenePhase.ACTIVE, delta);
   }

   public static double adjustVerticalOffset(String character, GirlSceneAction action, GirlScenePhase phase, double delta) {
      SceneCameraSettings settings = mutableSettings(character, action, phase);
      if (SexmodConfig.isBoneCamDynamic()) {
         double base = settings.dynamicVerticalOffset != null ? settings.dynamicVerticalOffset : settings.verticalOffset;
         settings.dynamicVerticalOffset = base + delta;
      } else {
         settings.verticalOffset += delta;
      }
      roster = roster.sanitized();
      save();
      return verticalOffset(character, action, phase);
   }

   public static double adjustLateralOffset(String character, GirlSceneAction action, double delta) {
      return adjustLateralOffset(character, action, GirlScenePhase.ACTIVE, delta);
   }

   public static double adjustLateralOffset(String character, GirlSceneAction action, GirlScenePhase phase, double delta) {
      SceneCameraSettings settings = mutableSettings(character, action, phase);
      if (SexmodConfig.isBoneCamDynamic()) {
         double base = settings.dynamicLateralOffset != null ? settings.dynamicLateralOffset : settings.lateralOffset;
         settings.dynamicLateralOffset = base + delta;
      } else {
         settings.lateralOffset += delta;
      }
      roster = roster.sanitized();
      save();
      return lateralOffset(character, action, phase);
   }

   public static double adjustHelperVerticalOffset(String character, GirlSceneAction action, double delta) {
      return adjustHelperVerticalOffset(character, action, GirlScenePhase.ACTIVE, delta);
   }

   public static double adjustHelperVerticalOffset(String character, GirlSceneAction action, GirlScenePhase phase, double delta) {
      mutableSettings(character, action, phase).helperVerticalOffset += delta;
      roster = roster.sanitized();
      save();
      return settings(character, action, phase).helperVerticalOffset;
   }

   public static double adjustHelperScale(String character, GirlSceneAction action, double delta) {
      return adjustHelperScale(character, action, GirlScenePhase.ACTIVE, delta);
   }

   public static double adjustHelperScale(String character, GirlSceneAction action, GirlScenePhase phase, double delta) {
      mutableSettings(character, action, phase).helperScale += delta;
      roster = roster.sanitized();
      save();
      return settings(character, action, phase).helperScale;
   }

   public static double adjustHelperForwardOffset(String character, GirlSceneAction action, double delta) {
      return adjustHelperForwardOffset(character, action, GirlScenePhase.ACTIVE, delta);
   }

   public static double adjustHelperForwardOffset(String character, GirlSceneAction action, GirlScenePhase phase, double delta) {
      mutableSettings(character, action, phase).helperForwardOffset += delta;
      roster = roster.sanitized();
      save();
      return settings(character, action, phase).helperForwardOffset;
   }

   public static double adjustHelperSideOffset(String character, GirlSceneAction action, double delta) {
      return adjustHelperSideOffset(character, action, GirlScenePhase.ACTIVE, delta);
   }

   public static double adjustHelperSideOffset(String character, GirlSceneAction action, GirlScenePhase phase, double delta) {
      mutableSettings(character, action, phase).helperSideOffset += delta;
      roster = roster.sanitized();
      save();
      return settings(character, action, phase).helperSideOffset;
   }

   /**
    * Her camera for this scene, read-only. Preference order is her own edited value, then the value
    * measured for her in-game, then Jenny's — so re-tuning Jenny still propagates to every girl who
    * was never measured for that scene.
    */
   private static GirlSceneCameraConfig.SceneCameraSettings settings(String character, GirlSceneAction action, GirlScenePhase phase) {
      GirlSceneCameraConfig.CameraProfile profile = GirlSceneCameraConfig.CameraProfile.forPhase(phase);
      GirlSceneCameraConfig.CameraSettings own = roster.characters.get(character);
      GirlSceneCameraConfig.SceneCameraSettings tuned = own == null ? null : own.profileMap(profile).get(action);
      if (tuned != null) {
         return tuned;
      }

      GirlSceneCameraConfig.SceneCameraSettings measured = CameraSettings.measured(character, profile, action);
      return measured != null ? measured : roster.forCharacter(BASE_CHARACTER).forAction(action, profile);
   }

   /** The same entry, but materialised under her own name because it is about to be edited. */
   private static GirlSceneCameraConfig.SceneCameraSettings mutableSettings(String character, GirlSceneAction action, GirlScenePhase phase) {
      GirlSceneCameraConfig.CameraProfile profile = GirlSceneCameraConfig.CameraProfile.forPhase(phase);
      GirlSceneCameraConfig.CameraSettings own = roster.forCharacter(character);
      return own.profileMap(profile).computeIfAbsent(action, key -> settings(character, key, phase).copy());
   }

   @Environment(EnvType.CLIENT)
   private enum CameraProfile {
      MAIN,
      INTRO,
      PAYMENT,
      WAITING;

      private static GirlSceneCameraConfig.CameraProfile forPhase(GirlScenePhase phase) {
         if (phase == GirlScenePhase.PAYMENT) {
            return PAYMENT;
         } else if (phase == GirlScenePhase.WAITING || phase == GirlScenePhase.POSITIONING) {
            return WAITING;
         } else {
            return phase == GirlScenePhase.INTRO ? INTRO : MAIN;
         }
      }
   }

   /** The whole file: one camera set per girl. The old flat shape is migrated into Jenny's slot. */
   @Environment(EnvType.CLIENT)
   private static final class RosterSettings {
      private Map<String, GirlSceneCameraConfig.CameraSettings> characters = new LinkedHashMap<>();
      /** Pre-roster files stored these four maps at the top level. */
      private Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings> byAction;
      private Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings> mainByAction;
      private Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings> introByAction;
      private Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings> paymentByAction;

      private RosterSettings sanitized() {
         if (this.characters == null) {
            this.characters = new LinkedHashMap<>();
         }

         this.migrateLegacyFlatFile();
         this.forCharacter(BASE_CHARACTER).fillDefaults();

         for (GirlSceneCameraConfig.CameraSettings character : this.characters.values()) {
            character.sanitized();
         }

         return this;
      }

      /** Everything a pre-roster file held was Jenny's, so it moves under her name untouched. */
      private void migrateLegacyFlatFile() {
         if (this.byAction == null && this.mainByAction == null && this.introByAction == null && this.paymentByAction == null) {
            return;
         }

         GirlSceneCameraConfig.CameraSettings jenny = this.forCharacter(BASE_CHARACTER);
         jenny.adoptLegacy(this.byAction, this.mainByAction, this.introByAction, this.paymentByAction);
         this.byAction = null;
         this.mainByAction = null;
         this.introByAction = null;
         this.paymentByAction = null;
      }

      private GirlSceneCameraConfig.CameraSettings forCharacter(String character) {
         return this.characters.computeIfAbsent(character, key -> new GirlSceneCameraConfig.CameraSettings().sanitized());
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class CameraSettings {
      private Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings> byAction = new EnumMap<>(GirlSceneAction.class);
      private Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings> mainByAction = new EnumMap<>(GirlSceneAction.class);
      private Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings> introByAction = new EnumMap<>(GirlSceneAction.class);
      private Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings> paymentByAction = new EnumMap<>(GirlSceneAction.class);
      private Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings> waitingByAction = new EnumMap<>(GirlSceneAction.class);

      private GirlSceneCameraConfig.CameraSettings sanitized() {
         this.byAction = sanitizeMap(this.byAction);
         this.mainByAction = sanitizeMap(this.mainByAction);
         this.introByAction = sanitizeMap(this.introByAction);
         this.paymentByAction = sanitizeMap(this.paymentByAction);
         this.waitingByAction = sanitizeMap(this.waitingByAction);
         this.migrateLegacyMainSettings();
         sanitizeEntries(this.mainByAction);
         sanitizeEntries(this.introByAction);
         sanitizeEntries(this.paymentByAction);
         sanitizeEntries(this.waitingByAction);
         return this;
      }

      /**
       * Only the base character is filled out in full. Everyone else keeps just the scenes actually
       * tuned for her, so the rest keep reading Jenny's live values instead of a frozen copy.
       */
      private GirlSceneCameraConfig.CameraSettings fillDefaults() {
         this.sanitized();
         fillProfile(this.mainByAction, GirlSceneCameraConfig.CameraProfile.MAIN);
         fillProfile(this.introByAction, GirlSceneCameraConfig.CameraProfile.INTRO);
         fillProfile(this.paymentByAction, GirlSceneCameraConfig.CameraProfile.PAYMENT);
         fillProfile(this.waitingByAction, GirlSceneCameraConfig.CameraProfile.WAITING);
         return this;
      }

      private void adoptLegacy(
         Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings> legacy,
         Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings> main,
         Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings> intro,
         Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings> payment
      ) {
         if (legacy != null) {
            this.byAction.putAll(legacy);
         }

         if (main != null) {
            this.mainByAction.putAll(main);
         }

         if (intro != null) {
            this.introByAction.putAll(intro);
         }

         if (payment != null) {
            this.paymentByAction.putAll(payment);
         }
      }

      private GirlSceneCameraConfig.SceneCameraSettings forAction(GirlSceneAction action, GirlSceneCameraConfig.CameraProfile profile) {
         return this.profileMap(profile).computeIfAbsent(action, key -> defaults(BASE_CHARACTER, profile, key).sanitized());
      }

      private Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings> profileMap(GirlSceneCameraConfig.CameraProfile profile) {
         return switch (profile) {
            case MAIN -> this.mainByAction;
            case INTRO -> this.introByAction;
            case PAYMENT -> this.paymentByAction;
            case WAITING -> this.waitingByAction;
         };
      }

      private void migrateLegacyMainSettings() {
         if (this.mainByAction.isEmpty() && !this.byAction.isEmpty()) {
            this.mainByAction.putAll(this.byAction);
            this.byAction.clear();
         }
      }

      private static Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings> sanitizeMap(
         Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings> source
      ) {
         if (source instanceof EnumMap) {
            return source;
         }

         EnumMap<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings> enummap = new EnumMap<>(GirlSceneAction.class);
         if (source != null) {
            enummap.putAll(source);
         }

         return enummap;
      }

      /** Repairs the entries that are present, without inventing any. */
      private static void sanitizeEntries(Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings> profile) {
         for (Map.Entry<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings> entry : profile.entrySet()) {
            entry.setValue(entry.getValue().sanitized());
         }
      }

      private static void fillProfile(
         Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings> profile, GirlSceneCameraConfig.CameraProfile cameraProfile
      ) {
         for (GirlSceneAction action : GirlSceneAction.values()) {
            profile.computeIfAbsent(action, key -> defaults(BASE_CHARACTER, cameraProfile, key).sanitized());
         }
      }

      /** Her measured entry for this scene, or null when she was never tuned for it. */
      private static GirlSceneCameraConfig.SceneCameraSettings measured(
         String character, GirlSceneCameraConfig.CameraProfile profile, GirlSceneAction action
      ) {
         if (BUNDLED_DEFAULTS != null && BUNDLED_DEFAULTS.characters != null) {
            GirlSceneCameraConfig.CameraSettings charSettings = BUNDLED_DEFAULTS.characters.get(character);
            if (charSettings != null) {
               GirlSceneCameraConfig.SceneCameraSettings tuned = charSettings.profileMap(profile).get(action);
               if (tuned != null) {
                  return tuned.copy().sanitized();
               }
            }
         }

         Map<GirlSceneCameraConfig.CameraProfile, Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings>> byProfile = TUNED.get(character);
         Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings> byAction = byProfile == null ? null : byProfile.get(profile);
         GirlSceneCameraConfig.SceneCameraSettings tuned = byAction == null ? null : byAction.get(action);
         return tuned == null ? null : tuned.copy().sanitized();
      }

      /**
       * The tuned numbers, as measured in-game. Keyed by character so a girl who was tuned for a
       * scene starts from her own values; anyone else still falls back to Jenny at lookup time.
       */
      private static final Map<String, Map<GirlSceneCameraConfig.CameraProfile, Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings>>> TUNED = tuned();

      private static Map<String, Map<GirlSceneCameraConfig.CameraProfile, Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings>>> tuned() {
         Map<String, Map<GirlSceneCameraConfig.CameraProfile, Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings>>> map = new LinkedHashMap<>();

         if (BUNDLED_DEFAULTS != null && BUNDLED_DEFAULTS.characters != null) {
            for (Map.Entry<String, GirlSceneCameraConfig.CameraSettings> entry : BUNDLED_DEFAULTS.characters.entrySet()) {
               String character = entry.getKey();
               GirlSceneCameraConfig.CameraSettings cs = entry.getValue();
               for (GirlSceneCameraConfig.CameraProfile profile : GirlSceneCameraConfig.CameraProfile.values()) {
                  Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings> profMap = cs.profileMap(profile);
                  if (profMap != null) {
                     for (Map.Entry<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings> actionEntry : profMap.entrySet()) {
                        map.computeIfAbsent(character, key -> new EnumMap<>(GirlSceneCameraConfig.CameraProfile.class))
                           .computeIfAbsent(profile, key -> new EnumMap<>(GirlSceneAction.class))
                           .put(actionEntry.getKey(), actionEntry.getValue().copy().sanitized());
                     }
                  }
               }
            }
         }
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.ANAL, -2.7, -0.75, -0.05, 1.12, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.ANAL_ALT, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.BLOWJOB, 0.3, -0.7, -0.85, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.BOOBJOB, 0.1, -0.7, -0.9, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.COWGIRL, -3.5, 0.4, 0.0, -1.24, 1.0, 0.66, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.DEEPTHROAT, -1.1, 0.05, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.DOGGY, 0.7, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.FACEFUCK, -1.2, 0.2, 0.0, -4.14, 0.98, 1.0, 0.16, 0.98);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.KOBOLD_ANAL, -3.4, 1.2, 0.05, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.KOBOLD_BLOWJOB, 1.1, 0.6, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.MATING_PRESS, 0.8, 0.3, 0.0, -0.6, 1.0, 0.16, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.MISSIONARY, 0.9, -0.05, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.PRONE_DOGGY, 0.4, -0.25, 0.0, 0.0, 1.0, -1.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.REVERSE_COWGIRL, -0.3, 0.25, 0.0, 0.0, 1.0, 2.34, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.SITTING, -3.4, -0.4, -0.05, 1.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.SITTING_ALT, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.STRIP, 1.1, 0.2, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.TOUCH_BOOBS, -0.7, 0.0, 0.0, 1.0, 1.0, -17.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.ANAL, -1.85, 0.02, -0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.ANAL_ALT, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.BLOWJOB, -0.9, 0.1, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.BOOBJOB, -0.8, 0.07, 0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.COWGIRL, 0.3, 0.26, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.DEEPTHROAT, -1.05, 0.02, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.DOGGY, 0.72, -0.05, -0.01, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.FACEFUCK, -0.75, 0.34, -0.03, 0.04, 0.92, 0.06, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.KOBOLD_ANAL, -2.2, 0.82, 0.04, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.KOBOLD_BLOWJOB, -0.25, 0.52, -0.04, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.MATING_PRESS, 1.48, 1.15, 0.0, 12.0, 1.0, 19.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.MISSIONARY, 1.02, 0.0, 0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.PRONE_DOGGY, 0.66, -0.2, 0.02, -1.1, 1.0, -1.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.REVERSE_COWGIRL, -0.12, 0.41, -0.03, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.SITTING, -3.45, -0.47, -0.13, -0.02, 1.0, -0.02, 0.18, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.SITTING_ALT, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.STRIP, 0.82, 0.16, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.TOUCH_BOOBS, -0.68, -0.03, 0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.ANAL, -0.7, 0.02, -0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.ANAL_ALT, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.BLOWJOB, -1.02, 0.03, 0.01, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.BOOBJOB, -0.64, 0.07, 0.04, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.COWGIRL, -0.82, 0.12, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.DEEPTHROAT, -1.06, 0.16, -0.01, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.DOGGY, -0.66, -0.03, 0.01, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.FACEFUCK, -1.02, 0.2, -0.04, 0.04, 0.92, 0.06, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.KOBOLD_ANAL, -0.95, 0.03, 0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.KOBOLD_BLOWJOB, -0.96, 0.14, -0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.MATING_PRESS, -0.78, 0.08, 0.0, -0.6, 1.0, 0.16, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.MISSIONARY, -0.9, 0.1, -0.03, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.PRONE_DOGGY, -0.66, 0.02, -0.01, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.REVERSE_COWGIRL, -0.84, 0.09, 0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.SITTING, -0.85, 0.08, 0.03, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.SITTING_ALT, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.STRIP, -1.18, 0.09, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "jenny", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.TOUCH_BOOBS, -0.68, -0.24, 0.02, 3.56, 1.0, -23.0, 0.0, 1.0);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.ANAL, -2.7, -0.75, -0.05, 5.0, 1.0, 2.0, 0.0, 1.0);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.BLOWJOB, 0.3, -0.7, -0.85, 0.52, 1.0, 1.22, 0.0, 0.84);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.BOOBJOB, -0.2, -0.7, -0.9, 0.0, 1.0, 1.92, 0.0, 0.88);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.DEEPTHROAT, -1.1, 0.05, 0.0, -0.08, 1.0, -4.0, 0.0, 1.0);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.DOGGY, 0.7, 0.1, 0.0, 0.0, 1.0, 2.0, 0.0, 1.0);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.FACEFUCK, -1.2, 0.2, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.KOBOLD_ANAL, -3.4, 1.2, 0.05, -2.0, 1.0, -1.82, -0.02, 1.0);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.MISSIONARY, 0.9, 0.0, 0.05, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.PRONE_DOGGY, 0.4, -0.25, 0.0, 0.0, 1.0, -3.0, 0.0, 1.0);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.TOUCH_BOOBS, -0.7, 0.0, 0.0, 0.0, 1.0, -17.02, 0.0, 0.9);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.ANAL, -2.55, -0.73, -0.02, 4.0, 1.0, 0.84, 0.0, 1.0);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.BLOWJOB, -1.1, 0.0, -0.03, 0.0, 1.16, 0.0, 0.0, 0.94);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.DOGGY, 0.82, 0.1, -0.01, 0.0, 1.0, 2.0, 0.0, 0.9);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.FACEFUCK, -0.95, 0.34, -0.03, 0.04, 0.92, 0.06, 0.0, 1.0);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.PRONE_DOGGY, 0.66, -0.2, 0.02, -2.0, 1.0, -3.0, 0.0, 1.0);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.TOUCH_BOOBS, -0.68, 0.02, 0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.ANAL, -0.7, 0.12, -0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.BLOWJOB, -0.92, 0.43, 0.06, 0.0, 1.0, 0.0, 0.0, 1.02);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.BOOBJOB, -0.84, 0.07, 0.04, 0.0, 1.18, 0.0, 0.0, 1.0);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.KOBOLD_ANAL, -0.95, 0.23, 0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.MISSIONARY, -1.0, 0.2, -0.03, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.REVERSE_COWGIRL, -0.84, 0.29, 0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.SITTING, -0.85, 0.23, 0.03, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.STRIP, -1.18, 0.34, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "ellie", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.TOUCH_BOOBS, -0.78, 0.06, 0.02, 1.0, 1.0, -20.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.ANAL, -3.1, -0.05, -0.05, 0.0, 1.0, -15.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.ANAL_ALT, -4.0, 0.0, 0.0, 0.0, 1.0, -15.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.BLOWJOB, 0.3, -0.7, -0.85, 0.2, 0.96, 1.62, 0.0, 1.14);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.BOOBJOB, -0.2, -0.7, -0.9, 0.0, 1.0, 1.04, 0.0, 1.16);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.COWGIRL, -3.5, 0.4, 0.0, -1.24, 1.0, -15.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.DOGGY, 0.7, -0.05, 0.0, 0.04, 0.88, -15.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.FACEFUCK, -1.2, 0.2, 0.0, -4.0, 0.98, 0.88, 0.0, 1.18);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.MISSIONARY, 1.1, 0.0, 0.0, 0.0, 1.0, -15.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.PRONE_DOGGY, 0.5, -0.25, 0.0, 0.0, 1.0, -15.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.REVERSE_COWGIRL, -0.4, 0.3, 0.0, 0.0, 1.0, -15.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.SITTING_ALT, -0.1, 0.4, 0.0, 0.0, 1.0, -15.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.STRIP, 1.3, 0.2, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.TOUCH_BOOBS, -0.8, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.ANAL, -1.85, 0.02, -0.02, 0.0, 1.0, -15.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.ANAL_ALT, 1.0, 0.0, 0.0, 0.0, 1.0, -15.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.BLOWJOB, -1.0, 0.15, 0.0, 0.0, 1.0, 0.0, 0.0, 1.18);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.COWGIRL, -3.6, 0.51, 0.0, 0.0, 1.0, -15.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.DOGGY, 0.72, -0.05, -0.01, 0.0, 1.0, -15.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.FACEFUCK, -1.15, 0.34, -0.03, 0.04, 0.92, 0.06, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.MISSIONARY, 0.82, 0.05, 0.02, 0.0, 1.0, -15.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.PRONE_DOGGY, 0.76, -0.2, 0.02, 0.0, 1.0, -15.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.REVERSE_COWGIRL, 0.58, 0.36, 0.02, 0.0, 1.0, -15.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.SITTING_ALT, -0.3, 0.5, 0.0, 0.0, 1.0, -15.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.TOUCH_BOOBS, -0.58, 0.02, 0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.ANAL, -0.9, 0.12, -0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.ANAL_ALT, -0.9, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.BLOWJOB, -1.02, 0.08, -0.04, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.COWGIRL, -1.02, -0.08, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.DOGGY, -0.66, -0.03, 0.01, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.KOBOLD_ANAL, -0.85, 0.13, 0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.MATING_PRESS, -1.08, 0.18, 0.0, -0.6, 1.0, 0.16, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.MISSIONARY, -0.8, 0.1, -0.03, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.PRONE_DOGGY, -0.66, 0.12, -0.01, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.REVERSE_COWGIRL, -0.94, 0.14, 0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.SITTING, -0.95, 0.18, 0.03, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.SITTING_ALT, -1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.TOUCH_BOOBS, -0.78, 0.11, 0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.WAITING, GirlSceneAction.ANAL, -1.85, 0.02, -0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.WAITING, GirlSceneAction.ANAL_ALT, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.WAITING, GirlSceneAction.BLOWJOB, -1.0, 0.15, 0.0, 0.0, 1.0, 0.0, 0.0, 1.18);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.WAITING, GirlSceneAction.COWGIRL, -3.6, 0.51, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.WAITING, GirlSceneAction.DOGGY, 0.72, -0.05, -0.01, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.WAITING, GirlSceneAction.MISSIONARY, 0.82, 0.05, 0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.WAITING, GirlSceneAction.PRONE_DOGGY, 0.76, -0.2, 0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.WAITING, GirlSceneAction.REVERSE_COWGIRL, 0.58, 0.36, 0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "bia", GirlSceneCameraConfig.CameraProfile.WAITING, GirlSceneAction.SITTING_ALT, -0.3, 0.5, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "luna", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.BLOWJOB, 0.3, -0.7, -0.85, 0.22, 1.0, 0.52, 0.0, 1.22);
         seed(map, "luna", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.BOOBJOB, -0.1, -0.35, -0.85, -3.0, 0.84, 1.0, 0.18, 1.14);
         seed(map, "luna", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.DEEPTHROAT, -0.9, 0.05, 0.0, 1.0, 1.0, 0.0, 0.0, 1.14);
         seed(map, "luna", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.DOGGY, 0.6, 0.0, 0.0, 0.0, 0.94, -1.0, 0.0, 1.0);
         seed(map, "luna", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.MISSIONARY, 0.9, 0.05, 0.0, 1.0, 0.84, 3.28, -0.06, 1.0);
         seed(map, "luna", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.REVERSE_COWGIRL, 0.1, 0.35, 0.0, 0.0, 0.76, 0.0, 0.0, 1.0);
         seed(map, "luna", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.SITTING, -3.4, -0.4, -0.05, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "luna", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.TOUCH_BOOBS, -0.8, 0.15, 0.0, 0.0, 0.94, 0.0, 0.0, 1.0);
         seed(map, "luna", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.BLOWJOB, -0.9, 0.1, -0.03, 0.0, 1.0, 0.0, 0.0, 1.1);
         seed(map, "luna", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.DEEPTHROAT, 0.55, 0.17, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "luna", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.REVERSE_COWGIRL, 0.68, 0.16, 0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "luna", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.TOUCH_BOOBS, -0.68, 0.02, 0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "luna", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.BLOWJOB, -1.02, 0.13, 0.06, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "luna", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.DEEPTHROAT, -0.96, 0.16, -0.01, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "luna", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.DOGGY, -0.66, 0.07, -0.04, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "luna", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.MATING_PRESS, -0.78, 0.18, 0.0, -0.6, 1.0, 0.16, 0.0, 1.0);
         seed(map, "luna", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.MISSIONARY, -0.9, 0.2, -0.03, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "luna", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.REVERSE_COWGIRL, -0.84, 0.14, 0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "luna", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.STRIP, -1.18, 0.14, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "luna", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.TOUCH_BOOBS, -0.68, 0.01, 0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "kobold", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.KOBOLD_BLOWJOB, -0.8, 0.55, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "kobold", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.KOBOLD_BLOWJOB, -0.55, 0.22, -0.04, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "kobold", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.KOBOLD_BLOWJOB, -1.06, 0.14, -0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "allie", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.DEEPTHROAT, -1.2, 0.05, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "allie", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.REVERSE_COWGIRL, 0.1, 0.35, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "allie", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.DEEPTHROAT, -0.65, 0.17, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "allie", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.DEEPTHROAT, -1.06, 0.16, -0.01, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "allie", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.REVERSE_COWGIRL, -0.84, 0.14, 0.02, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "bee", GirlSceneCameraConfig.CameraProfile.PAYMENT, GirlSceneAction.SITTING, -0.85, 0.18, 0.03, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "slime", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.BLOWJOB, 0.3, -0.7, -0.85, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "slime", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.DOGGY, 0.7, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "goblin", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.BLOWJOB, 0.3, -0.7, -0.85, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "manglelie", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.DOGGY, 0.7, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "manglelie", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.DOGGY, 0.72, -0.05, -0.01, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "manglelie", GirlSceneCameraConfig.CameraProfile.INTRO, GirlSceneAction.PRONE_DOGGY, 0.66, -0.2, 0.02, -1.1, 1.0, -1.0, 0.0, 1.0);
         seed(map, "galath", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.BLOWJOB, 0.3, -0.7, -0.85, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "galath", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.DOGGY, 0.7, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "galath", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.DEEPTHROAT, -1.1, 0.05, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "galath", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.FACEFUCK, -1.2, 0.2, 0.0, -4.14, 1.0, 1.0, 0.16, 1.0);
         seed(map, "galath", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.BOOBJOB, 0.1, -0.7, -0.9, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "galath", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.TOUCH_BOOBS, -0.7, 0.0, 0.0, 1.0, 1.0, -17.0, 0.0, 1.0);
         seed(map, "galath", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.PRONE_DOGGY, 0.4, -0.25, 0.0, 0.0, 1.0, -1.0, 0.0, 1.0);
         seed(map, "galath", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.MISSIONARY, 0.9, -0.05, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
         seed(map, "galath", GirlSceneCameraConfig.CameraProfile.MAIN, GirlSceneAction.COWGIRL, -3.5, 0.4, 0.0, -1.24, 1.0, 0.66, 0.0, 1.0);
         return map;
      }

      private static void seed(
         Map<String, Map<GirlSceneCameraConfig.CameraProfile, Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings>>> map,
         String character,
         GirlSceneCameraConfig.CameraProfile profile,
         GirlSceneAction action,
         double distance,
         double vertical,
         double lateral,
         double helperVertical,
         double helperScale,
         double helperForward,
         double helperSide,
         double girlScale
      ) {
         GirlSceneCameraConfig.SceneCameraSettings settings = new GirlSceneCameraConfig.SceneCameraSettings(
            distance, vertical, lateral, helperVertical, helperScale, helperForward, helperSide
         );
         settings.girlScale = girlScale;
         map.computeIfAbsent(character, key -> new EnumMap<>(GirlSceneCameraConfig.CameraProfile.class))
            .computeIfAbsent(profile, key -> new EnumMap<>(GirlSceneAction.class))
            .put(action, settings);
      }

      /** A scene with no measured entry keeps the neutral camera it was rendering at before. */
      private static GirlSceneCameraConfig.SceneCameraSettings defaults(String character, GirlSceneCameraConfig.CameraProfile profile, GirlSceneAction action) {
         Map<GirlSceneCameraConfig.CameraProfile, Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings>> byProfile = TUNED.get(character);
         Map<GirlSceneAction, GirlSceneCameraConfig.SceneCameraSettings> byAction = byProfile == null ? null : byProfile.get(profile);
         GirlSceneCameraConfig.SceneCameraSettings tuned = byAction == null ? null : byAction.get(action);
         return tuned == null ? new GirlSceneCameraConfig.SceneCameraSettings() : tuned.copy();
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class SceneCameraSettings {
      private double distanceMultiplier = 1.0;
      private double verticalOffset = 0.0;
      private double lateralOffset = 0.0;
      private double helperVerticalOffset = 0.0;
      private double helperScale = 1.0;
      private double helperForwardOffset = 0.0;
      private double helperSideOffset = 0.0;
      /** Scales the girl's own {@code body} bone; the partner has {@code helperScale}. */
      private double girlScale = 1.0;
      /**
       * -1 = follow the state's own row in {@link GirlSceneCamRig}, 0 = the measured {@code Head2}
       * framing, 1 = mount the camera on the animated cam bone. Kept as a double so the editor's
       * numeric rows can write it like any other field.
       */
      private double boneCamera = 0.0;
      /** Nudges on top of the cam bone, in blocks; only read in bone mode. */
      private double boneVerticalOffset = 0.0;
      private double boneForwardOffset = 0.0;
      private double boneSideOffset = 0.0;
      /** Dynamic camera offsets (used in dynamic mode; null falls back to static values). */
      private Double dynamicDistanceMultiplier = null;
      private Double dynamicVerticalOffset = null;
      private Double dynamicLateralOffset = null;

      private SceneCameraSettings() {
      }

      private SceneCameraSettings(
         double distanceMultiplier,
         double verticalOffset,
         double lateralOffset,
         double helperVerticalOffset,
         double helperScale,
         double helperForwardOffset,
         double helperSideOffset
      ) {
         this.distanceMultiplier = distanceMultiplier;
         this.verticalOffset = verticalOffset;
         this.lateralOffset = lateralOffset;
         this.helperVerticalOffset = helperVerticalOffset;
         this.helperScale = helperScale;
         this.helperForwardOffset = helperForwardOffset;
         this.helperSideOffset = helperSideOffset;
      }

      private GirlSceneCameraConfig.SceneCameraSettings copy() {
         GirlSceneCameraConfig.SceneCameraSettings copy = new GirlSceneCameraConfig.SceneCameraSettings(
            this.distanceMultiplier,
            this.verticalOffset,
            this.lateralOffset,
            this.helperVerticalOffset,
            this.helperScale,
            this.helperForwardOffset,
            this.helperSideOffset
         );
         copy.girlScale = this.girlScale;
         copy.boneCamera = this.boneCamera;
         copy.boneVerticalOffset = this.boneVerticalOffset;
         copy.boneForwardOffset = this.boneForwardOffset;
         copy.boneSideOffset = this.boneSideOffset;
         copy.dynamicDistanceMultiplier = this.dynamicDistanceMultiplier;
         copy.dynamicVerticalOffset = this.dynamicVerticalOffset;
         copy.dynamicLateralOffset = this.dynamicLateralOffset;
         return copy;
      }

      private GirlSceneCameraConfig.SceneCameraSettings sanitized() {
         if (!Double.isFinite(this.distanceMultiplier)) {
            this.distanceMultiplier = 1.0;
         }

         if (!Double.isFinite(this.verticalOffset)) {
            this.verticalOffset = 0.0;
         }

         if (!Double.isFinite(this.lateralOffset)) {
            this.lateralOffset = 0.0;
         }

         if (!Double.isFinite(this.helperVerticalOffset)) {
            this.helperVerticalOffset = 0.0;
         }

         if (!Double.isFinite(this.helperScale)) {
            this.helperScale = 1.0;
         }

         this.helperScale = Math.max(0.2, this.helperScale);
         if (!Double.isFinite(this.girlScale)) {
            this.girlScale = 1.0;
         }

         this.girlScale = Math.max(0.2, this.girlScale);
         if (!Double.isFinite(this.helperForwardOffset)) {
            this.helperForwardOffset = 0.0;
         }

         if (!Double.isFinite(this.helperSideOffset)) {
            this.helperSideOffset = 0.0;
         }

         if (!Double.isFinite(this.boneCamera)) {
            this.boneCamera = 0.0;
         }

         if (!Double.isFinite(this.boneVerticalOffset)) {
            this.boneVerticalOffset = 0.0;
         }

         if (!Double.isFinite(this.boneForwardOffset)) {
            this.boneForwardOffset = 0.0;
         }

         if (!Double.isFinite(this.boneSideOffset)) {
            this.boneSideOffset = 0.0;
         }

         if (this.dynamicDistanceMultiplier != null && !Double.isFinite(this.dynamicDistanceMultiplier)) {
            this.dynamicDistanceMultiplier = null;
         }
         if (this.dynamicVerticalOffset != null && !Double.isFinite(this.dynamicVerticalOffset)) {
            this.dynamicVerticalOffset = null;
         }
         if (this.dynamicLateralOffset != null && !Double.isFinite(this.dynamicLateralOffset)) {
            this.dynamicLateralOffset = null;
         }

         return this;
      }
   }
}
