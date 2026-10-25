package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.girls.scene.GirlScenePhase;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Disconnect;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Join;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public final class GirlSceneCameraController {
   private static boolean previousInvisible;
   private static boolean sceneCameraActive;
   /** Предыдущая фаза сцены для обнаружения переходов. */
   private static GirlScenePhase previousPhase = GirlScenePhase.IDLE;
   /**
    * Скин партнёра, зафиксированный при старте сцены и неизменный до её конца.
    * Резолвится один раз в tickClient когда скин уже загружен (не в рендер-треде).
    * null = скин ещё не резолвлен или сцены нет.
    */
   private static ResourceLocation lockedPartnerSkin = null;
   private static UUID lockedForPlayerUUID = null;
   /** SLIM = Alex (narrow arms), WIDE = Steve (wide arms). Locked alongside the skin. */
   private static boolean lockedPartnerIsSlim = false;
   /** Last seen fade signal counter — triggers fade when server increments it. */
   private static int lastSeenFadeSignal = -1;

   public static ResourceLocation getLockedPartnerSkin() {
      return lockedPartnerSkin;
   }

   public static boolean isLockedPartnerSlim() {
      return lockedPartnerIsSlim;
   }

   private GirlSceneCameraController() {
   }

   public static boolean isSceneCameraActive() {
      return sceneCameraActive;
   }

   public static boolean isFreecamActive() {
      return GirlSceneFreecam.isActive();
   }

   public static void register() {
      ClientTickEvents.END_CLIENT_TICK.register(GirlSceneCameraController::tickClient);
      ClientPlayConnectionEvents.DISCONNECT.register((Disconnect)(handler, client) -> forceRelease(client));
      ClientPlayConnectionEvents.JOIN.register((Join)(handler, sender, client) -> forceRelease(client));
   }

   private static void tickClient(Minecraft client) {
      LocalPlayer localplayer = client.player;
      if (localplayer != null && client.level != null) {
         Optional<SceneGirlEntity> optional = GirlSceneInputController.findOwnedGirl(localplayer);
         if (optional.isEmpty()) {
            // No scene at all — full reset including fade state.
            fullReleaseSceneCamera(client);
         } else {
            SceneGirlEntity girl = optional.get();
            GirlScenePhase girlscenephase = girl.trackedScenePhase();

            // Резолвим скин сразу как нашли девушку — до любых phase-проверок.
            // Иначе steve-слой успевает отрендериться с хардкодом на первый кадр сцены.
            if (lockedPartnerSkin == null) {
               lockedPartnerSkin = localplayer.getSkin().texture();
               lockedForPlayerUUID = localplayer.getUUID();
               lockedPartnerIsSlim = localplayer.getSkin().model() == net.minecraft.client.resources.PlayerSkin.Model.SLIM;
            }

            // Fade: сервер инкрементирует TRACKED_FADE_SIGNAL за FADE_LEAD_TICKS до snapTo.
            // Сравниваем с последним виденным — если изменился и больше, тригерим fade.
            // tick() и trigger() живут через "пешие" фазы (APPROACH/WALKING/POSITIONING/WAITING):
            // suspendSceneCamera не трогает fade, чтобы предзапущенный затем успел закрыть экран.
            int fadeSignal = girl.trackedFadeSignal();
            if (lastSeenFadeSignal < 0) {
               lastSeenFadeSignal = fadeSignal;
            } else if (fadeSignal > lastSeenFadeSignal) {
               lastSeenFadeSignal = fadeSignal;
               GirlSceneTransitionFade.trigger();
            }
            GirlSceneTransitionFade.tick();

            // Jenny walks to the bed and lies down on her own; the player keeps full vanilla control
            // until he closes the distance himself, so the scene camera stays off for those phases
            if (girlscenephase == GirlScenePhase.WAITING
               || girlscenephase == GirlScenePhase.WALKING
               || (girlscenephase == GirlScenePhase.POSITIONING && girl.waitsForPlayerApproachTracked())) {
               suspendSceneCamera(client);
               if (girlscenephase == GirlScenePhase.WAITING && GirlSceneTransitionFade.isActive()) {
                  lockMovement(client, localplayer);
               }
            } else if (girlscenephase == GirlScenePhase.APPROACH) {
               // she is walking up to us: movement is locked but the view stays free, so backing the
               // camera away still makes her follow
               suspendSceneCamera(client);
               lockMovement(client, localplayer);
            } else if (girl.shouldRenderPlayerTracked()) {
               if (!sceneCameraActive) {
                  previousInvisible = localplayer.isInvisible();
                  GirlSceneFreeLook.reset();
                  sceneCameraActive = true;
               }

               // the meter only rides along with the interactive part of a scene
               boolean interactive = girlscenephase == GirlScenePhase.ACTIVE || girlscenephase == GirlScenePhase.CLIMAX;
               if (interactive) {
                  GirlHornyMeter.show(true);
                  GirlHornyMeter.tickAutoFill(girl, client.getTimer().getGameTimeDeltaTicks());
               }

               localplayer.setInvisible(true);
               lockMovement(client, localplayer);
               GirlSceneFreecam.tick(client);
            } else {
               suspendSceneCamera(client);
            }

            previousPhase = girlscenephase;
         }
      } else {
         fullReleaseSceneCamera(client);
      }
   }

   /**
    * Full release: tears down camera, fade, skin lock and signal counter.
    * Called when there is no active scene at all.
    */
   private static void fullReleaseSceneCamera(Minecraft client) {
      GirlHornyMeter.hide();
      GirlSceneTransitionFade.reset();
      previousPhase = GirlScenePhase.IDLE;
      lastSeenFadeSignal = -1;
      lockedPartnerSkin = null;
      lockedForPlayerUUID = null;
      lockedPartnerIsSlim = false;
      if (sceneCameraActive && client.player != null) {
         client.player.setInvisible(previousInvisible);
         GirlSceneCameraState.clear();
         GirlSceneFreeLook.reset();
         GirlSceneFreecam.clear();
         sceneCameraActive = false;
      } else {
         sceneCameraActive = false;
         GirlSceneCameraState.clear();
         GirlSceneFreecam.clear();
      }
   }

   /**
    * Suspend: deactivates the scene camera while the scene is still running (approach / walking /
    * positioning / waiting phases).  Does NOT reset the fade or the signal counter so that a fade
    * triggered before the phase transition survives through those "feet-on-ground" phases and is
    * already running when the camera snaps back on.
    */
   private static void suspendSceneCamera(Minecraft client) {
      GirlHornyMeter.hide();
      if (sceneCameraActive && client.player != null) {
         client.player.setInvisible(previousInvisible);
         GirlSceneCameraState.clear();
         GirlSceneFreeLook.reset();
         GirlSceneFreecam.clear();
         sceneCameraActive = false;
      } else {
         sceneCameraActive = false;
         GirlSceneCameraState.clear();
         GirlSceneFreecam.clear();
      }
   }

   public static void forceRelease(Minecraft client) {
      GirlHornyMeter.hide();
      GirlSceneTransitionFade.reset();
      previousPhase = GirlScenePhase.IDLE;
      lockedPartnerSkin = null;
      lockedForPlayerUUID = null;
      lockedPartnerIsSlim = false;
      if (client.player != null) {
         client.player.setInvisible(false);
      }

      clearSceneActionKeys(client);
      previousInvisible = false;
      GirlSceneCameraState.clear();
      GirlSceneFreeLook.reset();
      GirlSceneFreecam.clear();
      sceneCameraActive = false;
   }

   private static void lockMovement(Minecraft client, LocalPlayer player) {
      if (!GirlSceneFreecam.isActive()) {
         clearMovementKeys(client);
      } else {
         clearSceneActionKeys(client);
      }

      player.setDeltaMovement(Vec3.ZERO);
   }

   /**
    * Suppresses walking but deliberately leaves sneak and jump alone: they drive the scene now, and
    * forcing them down=false would swallow a held key until the next physical key event.
    */
   private static void clearMovementKeys(Minecraft client) {
      client.options.keyUp.setDown(false);
      client.options.keyDown.setDown(false);
      client.options.keyLeft.setDown(false);
      client.options.keyRight.setDown(false);
      client.options.keySprint.setDown(false);
      clearSceneActionKeys(client);
   }

   /** Inventory and offhand swap stay vanilla; the scene only owns the world-interaction keys. */
   private static void clearSceneActionKeys(Minecraft client) {
      client.options.keyAttack.setDown(false);
      client.options.keyUse.setDown(false);
      client.options.keyDrop.setDown(false);
   }
}
