package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.girls.scene.GirlScenePhase;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.config.SexmodConfig;
import com.mojang.blaze3d.platform.InputConstants.Type;
import com.nxrtweetg.sexmod.girls.GirlEntity;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.network.JennySceneInputPayload;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class GirlSceneInputController {
   private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("sexmod", "main"));
   private static final KeyMapping RESET_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.sexmod.reset", Type.KEYSYM, 88, CATEGORY));
   private static final KeyMapping CONTROL_SCREEN_KEY = KeyBindingHelper.registerKeyBinding(
      new KeyMapping("key.sexmod.scene_controls", Type.KEYSYM, 78, CATEGORY)
   );
   private static final KeyMapping SETTINGS_KEY = KeyBindingHelper.registerKeyBinding(
      new KeyMapping("key.sexmod.settings", Type.KEYSYM, GLFW.GLFW_KEY_Z, CATEGORY)
   );
   private static final KeyMapping FREECAM_TOGGLE_KEY = KeyBindingHelper.registerKeyBinding(
      new KeyMapping("key.sexmod.freecam_toggle", Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY)
   );
   private static final KeyMapping FREECAM_XZ_DOWN_KEY = KeyBindingHelper.registerKeyBinding(
      new KeyMapping("key.sexmod.freecam_speed_xz_down", Type.KEYSYM, GLFW.GLFW_KEY_LEFT_BRACKET, CATEGORY)
   );
   private static final KeyMapping FREECAM_XZ_UP_KEY = KeyBindingHelper.registerKeyBinding(
      new KeyMapping("key.sexmod.freecam_speed_xz_up", Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_BRACKET, CATEGORY)
   );
   private static final KeyMapping FREECAM_Y_DOWN_KEY = KeyBindingHelper.registerKeyBinding(
      new KeyMapping("key.sexmod.freecam_speed_y_down", Type.KEYSYM, GLFW.GLFW_KEY_SEMICOLON, CATEGORY)
   );
   private static final KeyMapping FREECAM_Y_UP_KEY = KeyBindingHelper.registerKeyBinding(
      new KeyMapping("key.sexmod.freecam_speed_y_up", Type.KEYSYM, GLFW.GLFW_KEY_APOSTROPHE, CATEGORY)
   );
   // Fade editor: KP4/KP6 = fadeOut-/+, KP2/KP8 = blackHold-/+, KP1/KP3 = fadeIn-/+, KP5 = preview
   private static final KeyMapping FADE_OUT_DOWN  = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.sexmod.fade_out_down",   Type.KEYSYM, GLFW.GLFW_KEY_KP_4, CATEGORY));
   private static final KeyMapping FADE_OUT_UP    = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.sexmod.fade_out_up",     Type.KEYSYM, GLFW.GLFW_KEY_KP_6, CATEGORY));
   private static final KeyMapping FADE_HOLD_DOWN = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.sexmod.fade_hold_down",  Type.KEYSYM, GLFW.GLFW_KEY_KP_2, CATEGORY));
   private static final KeyMapping FADE_HOLD_UP   = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.sexmod.fade_hold_up",    Type.KEYSYM, GLFW.GLFW_KEY_KP_8, CATEGORY));
   private static final KeyMapping FADE_IN_DOWN   = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.sexmod.fade_in_down",    Type.KEYSYM, GLFW.GLFW_KEY_KP_1, CATEGORY));
   private static final KeyMapping FADE_IN_UP     = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.sexmod.fade_in_up",      Type.KEYSYM, GLFW.GLFW_KEY_KP_3, CATEGORY));
   private static final KeyMapping FADE_PREVIEW   = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.sexmod.fade_preview",    Type.KEYSYM, GLFW.GLFW_KEY_KP_5, CATEGORY));
   private static boolean thrustHeld;
   private static boolean climaxHeld;
   private static int proneDoggyShiftTicks;

   private GirlSceneInputController() {
   }

   public static void register() {
      ClientTickEvents.END_CLIENT_TICK
         .register(
            (EndTick)client -> {
               while (CONTROL_SCREEN_KEY.consumeClick()) {
                  if (client.screen instanceof GirlSceneControlScreen) {
                     client.setScreen(null);
                  } else if (client.player != null && client.level != null && findOwnedGirl(client.player).isPresent()) {
                     client.setScreen(new GirlSceneControlScreen());
                  }
               }

               while (SETTINGS_KEY.consumeClick()) {
                  if (client.screen instanceof GirlSettingsScreen) {
                     client.setScreen(null);
                  } else if (client.player != null && client.level != null) {
                     client.setScreen(new GirlSettingsScreen());
                  }
               }

               while (FREECAM_TOGGLE_KEY.consumeClick()) {
                  if (client.player != null && client.level != null && findOwnedGirl(client.player).isPresent()) {
                     GirlSceneFreecam.toggle(client);
                  }
               }

               while (FREECAM_XZ_DOWN_KEY.consumeClick()) {
                  GirlSceneFreecam.adjustHorizontalSpeed(-GirlSceneFreecam.speedStep());
               }

               while (FREECAM_XZ_UP_KEY.consumeClick()) {
                  GirlSceneFreecam.adjustHorizontalSpeed(GirlSceneFreecam.speedStep());
               }

               while (FREECAM_Y_DOWN_KEY.consumeClick()) {
                  GirlSceneFreecam.adjustVerticalSpeed(-GirlSceneFreecam.speedStep());
               }

               while (FREECAM_Y_UP_KEY.consumeClick()) {
                  GirlSceneFreecam.adjustVerticalSpeed(GirlSceneFreecam.speedStep());
               }

               // Fade editor — KP4/6 fadeOut, KP2/8 blackHold, KP1/3 fadeIn, KP5 preview
               while (FADE_OUT_DOWN.consumeClick())  { SexmodConfig.setFadeDurationTicks(SexmodConfig.getFadeDurationTicks() - 1); }
               while (FADE_OUT_UP.consumeClick())    { SexmodConfig.setFadeDurationTicks(SexmodConfig.getFadeDurationTicks() + 1); }
               while (FADE_HOLD_DOWN.consumeClick()) { SexmodConfig.setFadeLeadTicks(SexmodConfig.getFadeLeadTicks() - 1); }
               while (FADE_HOLD_UP.consumeClick())   { SexmodConfig.setFadeLeadTicks(SexmodConfig.getFadeLeadTicks() + 1); }
               while (FADE_IN_DOWN.consumeClick())   { SexmodConfig.setFadeDurationTicks(SexmodConfig.getFadeDurationTicks() - 1); }
               while (FADE_IN_UP.consumeClick())     { SexmodConfig.setFadeDurationTicks(SexmodConfig.getFadeDurationTicks() + 1); }
               while (FADE_PREVIEW.consumeClick())   { GirlSceneTransitionFade.trigger(); }

               if (client.player != null
                  && client.level != null
                  && !(client.screen instanceof GirlInteractionScreen)
                  && !(client.screen instanceof GirlSceneControlScreen)
                  && !(client.screen instanceof GirlSettingsScreen)) {
                  Optional<SceneGirlEntity> optional = findOwnedGirl(client.player);
                  if (optional.isEmpty()) {
                     thrustHeld = false;
                     climaxHeld = false;
                     proneDoggyShiftTicks = 0;
                  } else {
                     SceneGirlEntity girl = optional.get();
                     int i = girl.getId();
                     // A custom interaction (headpat, talk) is not a sex scene: it locks the player in
                     // place the way the original's d3.a(false) did, but sneak and jump stay off the
                     // scene controls. Only the abort key answers.
                     if (!girl.customActionId().isEmpty()) {
                        thrustHeld = false;
                        climaxHeld = false;
                        proneDoggyShiftTicks = 0;

                        while (RESET_KEY.consumeClick()) {
                           ClientPlayNetworking.send(new JennySceneInputPayload(i, "reset"));
                        }

                        return;
                     }

                     if (girl.trackedScenePhase() == GirlScenePhase.WAITING
                        || girl.trackedScenePhase() == GirlScenePhase.APPROACH
                        || girl.trackedScenePhase() == GirlScenePhase.WALKING
                        || (girl.trackedScenePhase() == GirlScenePhase.POSITIONING && girl.waitsForPlayerApproachTracked())) {
                        // Jenny is walking over / heading to the bed; vanilla keys stay vanilla
                        thrustHeld = false;
                        climaxHeld = false;
                        proneDoggyShiftTicks = 0;

                        while (RESET_KEY.consumeClick()) {
                           ClientPlayNetworking.send(new JennySceneInputPayload(i, "reset"));
                        }
                        return;
                     }

                     if (GirlSceneFreecam.isActive()) {
                        thrustHeld = false;
                        climaxHeld = false;
                        proneDoggyShiftTicks = 0;
                        return;
                     }

                     // vanilla keys drive the scene, as in the original (d3_class161 on InputUpdateEvent)
                     boolean flag = client.options.keyShift.isDown();
                     boolean isProneDoggy = girl.trackedActiveAction().filter(a -> a == GirlSceneAction.PRONE_DOGGY).isPresent();
                     if (isProneDoggy) {
                        if (flag) {
                           if (proneDoggyShiftTicks == 0) {
                              ClientPlayNetworking.send(new JennySceneInputPayload(i, "thrust"));
                              thrustHeld = true;
                           } else if (proneDoggyShiftTicks == 1) {
                              ClientPlayNetworking.send(new JennySceneInputPayload(i, "thrust_release"));
                              thrustHeld = false;
                           }
                           proneDoggyShiftTicks++;
                           if (proneDoggyShiftTicks >= 20) {
                              proneDoggyShiftTicks = 0;
                           }
                        } else {
                           if (proneDoggyShiftTicks > 0) {
                              if (thrustHeld) {
                                 ClientPlayNetworking.send(new JennySceneInputPayload(i, "thrust_release"));
                              }
                              thrustHeld = false;
                              proneDoggyShiftTicks = 0;
                           }
                        }
                     } else {
                        if (flag && !thrustHeld) {
                           ClientPlayNetworking.send(new JennySceneInputPayload(i, "thrust"));
                        } else if (!flag && thrustHeld) {
                           ClientPlayNetworking.send(new JennySceneInputPayload(i, "thrust_release"));
                        }
                        thrustHeld = flag;
                     }
                     boolean flag1 = client.options.keyJump.isDown();
                     if (flag1 && !climaxHeld) {
                        // Space ONLY finishes the scene on a full meter (CLIMAX).
                        // Space MUST NOT reset or abort the scene! Only 'X' aborts.
                        if (GirlHornyMeter.isFull()) {
                           ClientPlayNetworking.send(new JennySceneInputPayload(i, "climax"));
                           GirlHornyMeter.markFinishing();
                        }
                     }

                     climaxHeld = flag1;

                     while (RESET_KEY.consumeClick()) {
                        ClientPlayNetworking.send(new JennySceneInputPayload(i, "reset"));
                     }
                  }
               } else {
                  thrustHeld = false;
                  climaxHeld = false;
                  proneDoggyShiftTicks = 0;
               }
            }
         );
   }

   /**
    * The original swallows the entire movement input while a scene runs (d3_class161): sneak and
    * jump are scene controls, so they must not also crouch or jump the player.
    */
   public static boolean shouldSwallowMovementInput() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player == null || minecraft.level == null) {
         return false;
      }

      if (GirlSceneFreecam.isActive()) {
         return false;
      }

      // APPROACH keeps the view free but pins the feet, so movement is still swallowed there
      return findOwnedGirl(minecraft.player)
         .map(
            jenny -> jenny.trackedScenePhase() != GirlScenePhase.WAITING
               && jenny.trackedScenePhase() != GirlScenePhase.WALKING
               && !(jenny.trackedScenePhase() == GirlScenePhase.POSITIONING && jenny.waitsForPlayerApproachTracked())
         )
         .orElse(false);
   }

   public static boolean shouldBlockGameplayInput() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player == null || minecraft.level == null) {
         return false;
      }
      return findOwnedGirl(minecraft.player)
         .map(girl -> {
            GirlScenePhase phase = girl.trackedScenePhase();
            if (phase == GirlScenePhase.WAITING
               || phase == GirlScenePhase.WALKING
               || (phase == GirlScenePhase.POSITIONING && girl.waitsForPlayerApproachTracked())) {
               return false;
            }
            return true;
         })
         .orElse(false);
   }

   public static boolean shouldHideFirstPersonHands() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player == null || minecraft.level == null) {
         return false;
      }

      // A partner-less custom interaction (Bia's talk) keeps the vanilla first-person view, so the
      // hands belong on screen. Every scene phase, and anything that draws him, still hides them.
      return findOwnedGirl(minecraft.player)
         .map(girl -> {
            GirlScenePhase phase = girl.trackedScenePhase();
            if (phase == GirlScenePhase.WAITING
               || phase == GirlScenePhase.WALKING
               || (phase == GirlScenePhase.POSITIONING && girl.waitsForPlayerApproachTracked())) {
               return false;
            }
            return phase != GirlScenePhase.IDLE || girl.shouldRenderPlayerTracked();
         })
         .orElse(false);
   }

   static Optional<SceneGirlEntity> findOwnedGirl(Player player) {
      return player.level()
         .getEntitiesOfClass(SceneGirlEntity.class, player.getBoundingBox().inflate(24.0), entity -> entity.isAlive())
         .stream()
         .filter(entity -> entity.trackedActivePlayer().map(uuid -> uuid.equals(player.getUUID())).orElse(false))
         // a custom interaction owns her with the scene phase still IDLE, so it needs its own clause -
         // without it the player keeps full control while she walks over for a headpat
         .filter(
            entity -> entity.trackedScenePhase() != GirlScenePhase.IDLE
               || entity.shouldRenderPlayerTracked()
               || !entity.customActionId().isEmpty()
         )
         .min(Comparator.comparingDouble(player::distanceToSqr));
   }

   static void sendSceneTuneInput(Minecraft client, String inputId) {
      if (client.player != null) {
         findOwnedGirl(client.player).ifPresent(jenny -> ClientPlayNetworking.send(new JennySceneInputPayload(jenny.getId(), inputId)));
      }
   }

   static void sendSceneControlInput(Minecraft client, String inputId) {
      if (client.player != null) {
         findOwnedGirl(client.player).ifPresent(jenny -> ClientPlayNetworking.send(new JennySceneInputPayload(jenny.getId(), inputId)));
      }
   }
}
