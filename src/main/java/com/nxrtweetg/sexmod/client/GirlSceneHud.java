package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.girls.scene.GirlScenePhase;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneLayoutConfig;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public final class GirlSceneHud {
   private GirlSceneHud() {
   }

   public static void register() {
      HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("sexmod", "scene_hud"), GirlSceneHud::render);
   }

   private static void render(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player != null && minecraft.level != null && !minecraft.gui.hud.isHidden()) {
         GirlHornyMeterHud.render(context, tickCounter);

         // Плавное затемнение при смене фаз — использует субтиковую интерполяцию для 60+ FPS плавности
         float fadeAlpha = GirlSceneTransitionFade.getAlpha(tickCounter.getGameTimeDeltaPartialTick(false));
         if (fadeAlpha > 0.0F) {
            int alpha = (int)(fadeAlpha * 255.0F);
            int color = (alpha << 24); // чёрный с вычисленной прозрачностью
            context.fill(0, 0, context.guiWidth(), context.guiHeight(), color);
         }

         Optional<SceneGirlEntity> optional = findOwnedGirl(minecraft.player);
         if (!optional.isEmpty()) {
            SceneGirlEntity girl = optional.get();
            GirlScenePhase girlscenephase = girl.trackedScenePhase();
            GirlSceneAction girlsceneaction = girl.trackedActiveAction().orElse(null);
            boolean tunable = girl.trackedActiveAction().filter(GirlSceneLayoutConfig::supportsForTuning).isPresent();
            int right = context.guiWidth() - 4;
            int y = 4;

            String tuning = tunable
               ? String.format(
                  "Scene %s [N]  F %+.2f  S %+.2f  H %+.2f",
                  phaseLabel(sceneConfigPhase(girlscenephase)),
                  girl.trackedSceneTuneForward(),
                  girl.trackedSceneTuneSide(),
                  girl.trackedSceneTuneVertical()
               )
               : String.format(
                  "Camera %s [N]  Dist %.1fx  Height %+.2f  Side %+.2f",
                  phaseLabel(cameraConfigPhase(girlscenephase)),
                  girlsceneaction == null ? 1.0 : GirlSceneCameraConfig.distanceMultiplier(girl.characterId(), girlsceneaction, cameraConfigPhase(girlscenephase)),
                  girlsceneaction == null ? 0.0 : GirlSceneCameraConfig.verticalOffset(girl.characterId(), girlsceneaction, cameraConfigPhase(girlscenephase)),
                  girlsceneaction == null ? 0.0 : GirlSceneCameraConfig.lateralOffset(girl.characterId(), girlsceneaction, cameraConfigPhase(girlscenephase))
               );
            drawRight(context, minecraft, tuning, right, y, -6311229);
            drawRight(
               context,
               minecraft,
               String.format(
                  "Freecam [G]  Move WASD/Shift/Space  Speed %.2f/%.2f [[ ] ; ']",
                  GirlSceneFreecam.horizontalSpeed(),
                  GirlSceneFreecam.verticalSpeed()
               ),
               right,
               y + 10,
               -8947849
            );

            if (girl.isSceneFrozenTracked()) {
               drawRight(context, minecraft, "[Frozen]", right, y + 20, -2695449);
            }
         }
      }
   }

   private static void drawRight(GuiGraphicsExtractor context, Minecraft minecraft, String text, int right, int y, int color) {
      Component component = Component.literal(text);
      context.text(minecraft.font, component, right - minecraft.font.width(component), y, color);
   }

   static Optional<SceneGirlEntity> findOwnedGirl(Player player) {
      return player.level()
         .getEntitiesOfClass(SceneGirlEntity.class, player.getBoundingBox().inflate(24.0), entity -> entity.isAlive())
         .stream()
         .filter(entity -> entity.trackedActivePlayer().map(uuid -> uuid.equals(player.getUUID())).orElse(false))
         .min(Comparator.comparingDouble(player::distanceToSqr));
   }

   private static GirlScenePhase cameraConfigPhase(GirlScenePhase phase) {
      return switch (phase) {
         case PAYMENT -> GirlScenePhase.PAYMENT;
         case INTRO -> GirlScenePhase.INTRO;
         default -> GirlScenePhase.ACTIVE;
      };
   }

   private static GirlScenePhase sceneConfigPhase(GirlScenePhase phase) {
      return switch (phase) {
         case PAYMENT -> GirlScenePhase.PAYMENT;
         case INTRO -> GirlScenePhase.INTRO;
         default -> GirlScenePhase.ACTIVE;
      };
   }

   private static String phaseLabel(GirlScenePhase phase) {
      return switch (phase) {
         case PAYMENT -> "Payment";
         case INTRO -> "Intro";
         default -> "Main";
      };
   }
}
