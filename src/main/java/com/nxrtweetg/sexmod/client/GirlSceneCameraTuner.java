package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.girls.scene.GirlScenePhase;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneBodyConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public final class GirlSceneCameraTuner {
   private GirlSceneCameraTuner() {
   }

   public static void register() {
   }

   public static void updateDistance(Minecraft client, double delta) {
      SceneGirlEntity girl = currentGirl(client);
      GirlSceneAction girlsceneaction = girl == null ? null : girl.trackedActiveAction().orElse(null);
      if (girlsceneaction != null) {
         GirlScenePhase girlscenephase = activeCameraPhase(girl);
         double d0 = GirlSceneCameraConfig.adjustDistance(girl.characterId(), girlsceneaction, girlscenephase, delta);
         if (client.player != null) {
            client.player
               .displayClientMessage(Component.literal(String.format("%s %s camera distance: %.1fx", girlsceneaction.legacyId(), phaseLabel(girlscenephase), d0)), true);
         }
      }
   }

   public static void updateVertical(Minecraft client, double delta) {
      SceneGirlEntity girl = currentGirl(client);
      GirlSceneAction girlsceneaction = girl == null ? null : girl.trackedActiveAction().orElse(null);
      if (girlsceneaction != null) {
         GirlScenePhase girlscenephase = activeCameraPhase(girl);
         double d0 = GirlSceneCameraConfig.adjustVerticalOffset(girl.characterId(), girlsceneaction, girlscenephase, delta);
         if (client.player != null) {
            client.player
               .displayClientMessage(Component.literal(String.format("%s %s camera height: %+.2f", girlsceneaction.legacyId(), phaseLabel(girlscenephase), d0)), true);
         }
      }
   }

   public static void updateLateral(Minecraft client, double delta) {
      SceneGirlEntity girl = currentGirl(client);
      GirlSceneAction girlsceneaction = girl == null ? null : girl.trackedActiveAction().orElse(null);
      if (girlsceneaction != null) {
         GirlScenePhase girlscenephase = activeCameraPhase(girl);
         double d0 = GirlSceneCameraConfig.adjustLateralOffset(girl.characterId(), girlsceneaction, girlscenephase, delta);
         if (client.player != null) {
            client.player
               .displayClientMessage(Component.literal(String.format("%s %s camera side offset: %+.2f", girlsceneaction.legacyId(), phaseLabel(girlscenephase), d0)), true);
         }
      }
   }

   public static void updateHelperVertical(Minecraft client, double delta) {
      SceneGirlEntity girl = currentGirl(client);
      GirlSceneAction girlsceneaction = girl == null ? null : girl.trackedActiveAction().orElse(null);
      if (girlsceneaction != null) {
         GirlScenePhase girlscenephase = activeCameraPhase(girl);
         double d0 = GirlSceneCameraConfig.adjustHelperVerticalOffset(girl.characterId(), girlsceneaction, girlscenephase, delta);
         if (client.player != null) {
            client.player
               .displayClientMessage(Component.literal(String.format("%s %s helper height: %+.2f", girlsceneaction.legacyId(), phaseLabel(girlscenephase), d0)), true);
         }
      }
   }

   public static void updateHelperScale(Minecraft client, double delta) {
      SceneGirlEntity girl = currentGirl(client);
      GirlSceneAction girlsceneaction = girl == null ? null : girl.trackedActiveAction().orElse(null);
      if (girlsceneaction != null) {
         GirlScenePhase girlscenephase = activeCameraPhase(girl);
         double d0 = GirlSceneCameraConfig.adjustHelperScale(girl.characterId(), girlsceneaction, girlscenephase, delta);
         if (client.player != null) {
            client.player
               .displayClientMessage(Component.literal(String.format("%s %s partner scale: %.2f", girlsceneaction.legacyId(), phaseLabel(girlscenephase), d0)), true);
         }
      }
   }

   public static void updateGirlScale(Minecraft client, double delta) {
      SceneGirlEntity girl = currentGirl(client);
      GirlSceneAction girlsceneaction = girl == null ? null : girl.trackedActiveAction().orElse(null);
      if (girlsceneaction != null) {
         GirlScenePhase girlscenephase = activeCameraPhase(girl);
         double d0 = GirlSceneCameraConfig.adjustGirlScale(girl.characterId(), girlsceneaction, girlscenephase, delta);
         if (client.player != null) {
            client.player
               .displayClientMessage(Component.literal(String.format("%s %s girl scale: %.2f", girlsceneaction.legacyId(), phaseLabel(girlscenephase), d0)), true);
         }
      }
   }

   public static void resetScales(Minecraft client) {
      SceneGirlEntity girl = currentGirl(client);
      GirlSceneAction girlsceneaction = girl == null ? null : girl.trackedActiveAction().orElse(null);
      if (girlsceneaction != null) {
         GirlSceneCameraConfig.resetScales(girl.characterId(), girlsceneaction, activeCameraPhase(girl));
         if (client.player != null) {
            client.player.displayClientMessage(Component.literal("Scale reset to 1.00"), true);
         }
      }
   }

   public static void updateHelperForward(Minecraft client, double delta) {
      SceneGirlEntity girl = currentGirl(client);
      GirlSceneAction girlsceneaction = girl == null ? null : girl.trackedActiveAction().orElse(null);
      if (girlsceneaction != null) {
         GirlScenePhase girlscenephase = activeCameraPhase(girl);
         double d0 = GirlSceneCameraConfig.adjustHelperForwardOffset(girl.characterId(), girlsceneaction, girlscenephase, delta);
         if (client.player != null) {
            client.player
               .displayClientMessage(Component.literal(String.format("%s %s helper forward: %+.2f", girlsceneaction.legacyId(), phaseLabel(girlscenephase), d0)), true);
         }
      }
   }

   public static void updateHelperSide(Minecraft client, double delta) {
      SceneGirlEntity girl = currentGirl(client);
      GirlSceneAction girlsceneaction = girl == null ? null : girl.trackedActiveAction().orElse(null);
      if (girlsceneaction != null) {
         GirlScenePhase girlscenephase = activeCameraPhase(girl);
         double d0 = GirlSceneCameraConfig.adjustHelperSideOffset(girl.characterId(), girlsceneaction, girlscenephase, delta);
         if (client.player != null) {
            client.player
               .displayClientMessage(Component.literal(String.format("%s %s helper side: %+.2f", girlsceneaction.legacyId(), phaseLabel(girlscenephase), d0)), true);
         }
      }
   }

   public static void updateBreastScale(Minecraft client, double delta) {
      SceneGirlEntity girl = currentGirl(client);
      GirlSceneAction girlsceneaction = girl == null ? null : girl.trackedActiveAction().orElse(null);
      if (girlsceneaction != null) {
         double d0 = GirlSceneBodyConfig.adjustBreastScale(girl.characterId(), girlsceneaction, delta);
         if (client.player != null) {
            client.player.displayClientMessage(Component.literal(String.format("%s breasts: %.2f", girlsceneaction.legacyId(), d0)), true);
         }
      }
   }

   public static void updateBootyScale(Minecraft client, double delta) {
      SceneGirlEntity girl = currentGirl(client);
      GirlSceneAction girlsceneaction = girl == null ? null : girl.trackedActiveAction().orElse(null);
      if (girlsceneaction != null) {
         double d0 = GirlSceneBodyConfig.adjustBootyScale(girl.characterId(), girlsceneaction, delta);
         if (client.player != null) {
            client.player.displayClientMessage(Component.literal(String.format("%s booty: %.2f", girlsceneaction.legacyId(), d0)), true);
         }
      }
   }

   /** Writes a camera field outright, for the editor's typed values. */
   public static void setCameraField(Minecraft client, GirlSceneCameraConfig.Field field, double value) {
      SceneGirlEntity girl = currentGirl(client);
      GirlSceneAction action = girl == null ? null : girl.trackedActiveAction().orElse(null);
      if (action != null) {
         GirlSceneCameraConfig.set(girl.characterId(), action, activeCameraPhase(girl), field, value);
      }
   }

   public static double cameraField(Minecraft client, GirlSceneCameraConfig.Field field) {
      SceneGirlEntity girl = currentGirl(client);
      GirlSceneAction action = girl == null ? null : girl.trackedActiveAction().orElse(null);
      return action == null ? 0.0 : GirlSceneCameraConfig.get(girl.characterId(), action, activeCameraPhase(girl), field);
   }

   public static void setBreastScale(Minecraft client, double value) {
      SceneGirlEntity girl = currentGirl(client);
      GirlSceneAction action = girl == null ? null : girl.trackedActiveAction().orElse(null);
      if (action != null) {
         GirlSceneBodyConfig.setBreastScale(girl.characterId(), action, value);
      }
   }

   public static void setBootyScale(Minecraft client, double value) {
      SceneGirlEntity girl = currentGirl(client);
      GirlSceneAction action = girl == null ? null : girl.trackedActiveAction().orElse(null);
      if (action != null) {
         GirlSceneBodyConfig.setBootyScale(girl.characterId(), action, value);
      }
   }

   public static double breastScale(Minecraft client) {
      SceneGirlEntity girl = currentGirl(client);
      GirlSceneAction action = girl == null ? null : girl.trackedActiveAction().orElse(null);
      return action == null ? 1.0 : GirlSceneBodyConfig.breastScale(girl.characterId(), action);
   }

   public static double bootyScale(Minecraft client) {
      SceneGirlEntity girl = currentGirl(client);
      GirlSceneAction action = girl == null ? null : girl.trackedActiveAction().orElse(null);
      return action == null ? 1.0 : GirlSceneBodyConfig.bootyScale(girl.characterId(), action);
   }

   private static SceneGirlEntity currentGirl(Minecraft client) {
      return client.player == null ? null : GirlSceneInputController.findOwnedGirl(client.player).orElse(null);
   }

   private static GirlSceneAction currentAction(Minecraft client) {
      SceneGirlEntity girl = currentGirl(client);
      return girl == null ? null : girl.trackedActiveAction().orElse(null);
   }

   private static GirlScenePhase activeCameraPhase(SceneGirlEntity jenny) {
      GirlScenePhase girlscenephase = jenny.trackedScenePhase();

      return switch (girlscenephase) {
         case PAYMENT -> GirlScenePhase.PAYMENT;
         case INTRO -> GirlScenePhase.INTRO;
         default -> GirlScenePhase.ACTIVE;
      };
   }

   private static String phaseLabel(GirlScenePhase phase) {
      return switch (phase) {
         case PAYMENT -> "payment";
         case INTRO -> "intro";
         default -> "main";
      };
   }
}
