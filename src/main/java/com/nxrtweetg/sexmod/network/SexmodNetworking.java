package com.nxrtweetg.sexmod.network;

import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.girls.GirlEntity;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.scene.GirlInteractionService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class SexmodNetworking {
   private SexmodNetworking() {
   }

   public static void register() {
      PayloadTypeRegistry.serverboundPlay().register(JennyStartInteractionPayload.ID, JennyStartInteractionPayload.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(JennySceneInputPayload.ID, JennySceneInputPayload.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(GirlCommandPayload.ID, GirlCommandPayload.CODEC);
      ServerPlayNetworking.registerGlobalReceiver(JennyStartInteractionPayload.ID, (payload, context) -> {
         ServerPlayer serverplayer = context.player();
         context.server().execute(() -> handleStartInteraction(serverplayer, payload));
      });
      ServerPlayNetworking.registerGlobalReceiver(JennySceneInputPayload.ID, (payload, context) -> {
         ServerPlayer serverplayer = context.player();
         context.server().execute(() -> handleSceneInput(serverplayer, payload));
      });
      ServerPlayNetworking.registerGlobalReceiver(GirlCommandPayload.ID, (payload, context) -> {
         ServerPlayer serverplayer = context.player();
         context.server().execute(() -> handleGirlCommand(serverplayer, payload));
      });
   }

   private static void handleGirlCommand(ServerPlayer player, GirlCommandPayload payload) {
      if (!(player.level().getEntity(payload.entityId()) instanceof GirlEntity girl)) {
         player.sendSystemMessage(Component.literal("She is no longer available."));
         return;
      }

      if (girl.distanceToSqr(player) > 144.0) {
         player.sendSystemMessage(Component.literal("You are too far away."));
         return;
      }

      String name = girl.getName().getString();

      switch (payload.commandId()) {
         case "action.names.followme":
            girl.startFollowing(player);
            sayLine(player, name, Component.translatable("jenny.dialogue.followme", player.getName().getString()));
            break;
         case "action.names.stopfollowme":
            girl.stopFollowing();
            sayLine(player, name, Component.translatable("jenny.dialogue.bye"));
            break;
         case "action.names.gohome":
            if (girl.coreState().homePos() == null) {
               sayLine(player, name, Component.translatable("jenny.dialogue.nohome"));
            } else {
               girl.sendHome();
               sayLine(player, name, Component.translatable("jenny.dialogue.bye2"));
            }
            break;
         case "action.names.setnewhome":
            girl.setHomeHere();
            sayLine(player, name, Component.translatable("jenny.dialogue.hihi"));
            break;
         case "action.names.equipment":
            girl.openEquipmentScreen(player);
            break;
         default:
            player.sendSystemMessage(Component.literal("Unknown command."));
      }
   }

   /** Same shape as the girls' in-world dialogue: {@code <Name> line}. */
   private static void sayLine(ServerPlayer player, String name, Component line) {
      player.sendSystemMessage(Component.literal("<" + name + "> ").append(line));
   }

   private static void handleStartInteraction(ServerPlayer player, JennyStartInteractionPayload payload) {
      if (player.level().getEntity(payload.entityId()) instanceof SceneGirlEntity girl) {
         GirlSceneAction girlsceneaction = GirlSceneAction.fromLegacyId(payload.actionId()).orElse(null);
         if (girlsceneaction == null) {
            if (!girl.tryBeginCustomInteraction(player, payload.actionId())) {
               player.sendSystemMessage(Component.literal("Unknown scene action."));
            }
         } else {
            GirlInteractionService.tryBeginInteraction(player, girl, girlsceneaction);
         }
      } else {
         player.sendSystemMessage(Component.literal("She is no longer available."));
      }
   }

   private static void handleSceneInput(ServerPlayer player, JennySceneInputPayload payload) {
      if (player.level().getEntity(payload.entityId()) instanceof SceneGirlEntity girl) {
         switch (payload.inputId()) {
            case "thrust":
               girl.applySceneInput(player, false);
               break;
            case "climax":
               girl.applySceneInput(player, true);
               break;
            case "reset":
               girl.forceResetFromOwner(player);
               break;
            case "toggle_freeze":
               girl.toggleSceneFreeze(player);
               break;
            case "scene_forward":
            case "scene_back":
            case "scene_left":
            case "scene_right":
            case "scene_up":
            case "scene_down":
               girl.applySceneTuneInput(player, payload.inputId());
               break;
            default:
               // typed values from the editor arrive as "scene_set:<axis>:<value>"
               if (payload.inputId().startsWith("scene_set:")) {
                  girl.applySceneTuneInput(player, payload.inputId());
               } else {
                  player.sendSystemMessage(Component.literal("Unknown Jenny scene input."));
               }
         }
      }
   }
}
