package com.nxrtweetg.sexmod.network;

import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.girls.GirlEntity;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.scene.GirlInteractionService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class SexmodNetworking {
   private SexmodNetworking() {
   }

   public static void register() {
      PayloadTypeRegistry.playC2S().register(JennyStartInteractionPayload.ID, JennyStartInteractionPayload.CODEC);
      PayloadTypeRegistry.playC2S().register(JennySceneInputPayload.ID, JennySceneInputPayload.CODEC);
      PayloadTypeRegistry.playC2S().register(GirlCommandPayload.ID, GirlCommandPayload.CODEC);
      PayloadTypeRegistry.playC2S().register(HomeActionPayload.ID, HomeActionPayload.CODEC);
      PayloadTypeRegistry.playS2C().register(SyncHomesPayload.ID, SyncHomesPayload.CODEC);
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
      ServerPlayNetworking.registerGlobalReceiver(HomeActionPayload.ID, (payload, context) -> {
         ServerPlayer serverplayer = context.player();
         context.server().execute(() -> handleHomeAction(serverplayer, payload));
      });
   }

   private static void handleGirlCommand(ServerPlayer player, GirlCommandPayload payload) {
      if (!(player.level().getEntity(payload.entityId()) instanceof GirlEntity girl)) {
         player.displayClientMessage(Component.literal("She is no longer available."), false);
         return;
      }

      if (girl.distanceToSqr(player) > 144.0) {
         player.displayClientMessage(Component.literal("You are too far away."), false);
         return;
      }

      String name = girl.getName().getString();

      switch (payload.commandId()) {
         case "action.names.followme":
            if (girl.isStayingAtHome()) {
               girl.setStayingAtHome(false);
            }
            girl.startFollowing(player);
            sayLine(player, name, Component.translatable(girl.commandDialogueKey("followme"), player.getName().getString()));
            break;
         case "action.names.stopfollowme":
            girl.stopFollowing();
            sayLine(player, name, Component.translatable(girl.commandDialogueKey("bye")));
            break;
         case "action.names.gohome":
            girl.setStayingAtHome(true);
            if (!girl.assignedHomeName().isEmpty()) {
               com.nxrtweetg.sexmod.home.GirlHomeRegion region = com.nxrtweetg.sexmod.home.SexmodHomeData.get(player.level()).getHome(girl.assignedHomeName());
               if (region != null) {
                  girl.coreState().setHomePos(region.center());
               }
            }
            if (girl.coreState().homePos() == null) {
               sayLine(player, name, Component.translatable(girl.commandDialogueKey("nohome")));
            } else {
               girl.sendHome();
               sayLine(player, name, Component.translatable(girl.commandDialogueKey("bye2")));
            }
            break;
         case "action.names.stayhome":
            boolean staying = !girl.isStayingAtHome();
            girl.setStayingAtHome(staying);
            if (staying) {
               girl.stopFollowing();
               sayLine(player, name, Component.translatable("chat.sexmod.home.staying"));
            } else {
               sayLine(player, name, Component.translatable("chat.sexmod.home.not_staying"));
            }
            break;
         case "action.names.setnewhome":
            girl.setHomeHere();
            sayLine(player, name, Component.translatable(girl.commandDialogueKey("hihi")));
            break;
         case "action.names.equipment":
            girl.openEquipmentScreen(player);
            break;
         case "menu_open":
            girl.setInteractingPlayer(player);
            break;
         case "menu_close":
            girl.clearInteractingPlayer(player);
            break;
         case "allie_dismiss_free":
            if (girl instanceof com.nxrtweetg.sexmod.girls.allie.AllieEntity allie) {
               allie.startDismissal(false);
            }
            break;
         default:
            player.displayClientMessage(Component.literal("Unknown command."), false);
      }
   }

   private static void handleHomeAction(ServerPlayer player, HomeActionPayload payload) {
      ServerLevel level = player.level();
      com.nxrtweetg.sexmod.home.SexmodHomeData homeData = com.nxrtweetg.sexmod.home.SexmodHomeData.get(level);

      switch (payload.action()) {
         case "query" -> {
            ServerPlayNetworking.send(player, new SyncHomesPayload(homeData.getHomesList()));
         }
         case "create" -> {
            if (payload.homeName().isBlank()) {
               player.displayClientMessage(Component.translatable("chat.sexmod.home.invalid_name"), false);
               return;
            }
            com.nxrtweetg.sexmod.home.GirlHomeRegion region = com.nxrtweetg.sexmod.home.GirlHomeRegion.create(
               payload.homeName(),
               level.dimension().location().toString(),
               new net.minecraft.core.BlockPos(payload.x1(), payload.y1(), payload.z1()),
               new net.minecraft.core.BlockPos(payload.x2(), payload.y2(), payload.z2())
            );
            homeData.addHome(region);
            spawnHomeBoundaryParticles(level, region);
            broadcastHomes(level.getServer());
            player.displayClientMessage(Component.translatable("chat.sexmod.home.created", region.name()), false);
         }
         case "delete" -> {
            boolean removed = homeData.removeHome(payload.homeName());
            if (removed) {
               broadcastHomes(level.getServer());
               player.displayClientMessage(Component.translatable("chat.sexmod.home.deleted", payload.homeName()), false);
            } else {
               player.displayClientMessage(Component.translatable("chat.sexmod.home.not_found", payload.homeName()), false);
            }
         }
         case "assign" -> {
            if (level.getEntity(payload.entityId()) instanceof GirlEntity girl) {
               com.nxrtweetg.sexmod.home.GirlHomeRegion region = homeData.getHome(payload.homeName());
               if (region != null) {
                  girl.setAssignedHomeName(region.name());
                  girl.coreState().setHomePos(region.center());
                  sayLine(player, girl.getName().getString(), Component.translatable("chat.sexmod.home.assigned", region.name()));
               }
            }
         }
         case "stay" -> {
            if (level.getEntity(payload.entityId()) instanceof GirlEntity girl) {
               boolean staying = !girl.isStayingAtHome();
               girl.setStayingAtHome(staying);
               if (staying) {
                  girl.stopFollowing();
                  sayLine(player, girl.getName().getString(), Component.translatable("chat.sexmod.home.staying"));
               } else {
                  sayLine(player, girl.getName().getString(), Component.translatable("chat.sexmod.home.not_staying"));
               }
            }
         }
         case "show_boundary" -> {
            com.nxrtweetg.sexmod.home.GirlHomeRegion region = homeData.getHome(payload.homeName());
            if (region != null) {
               spawnHomeBoundaryParticles(level, region);
            }
         }
         default -> player.displayClientMessage(Component.literal("Unknown home action: " + payload.action()), false);
      }
   }

   public static void broadcastHomes(net.minecraft.server.MinecraftServer server) {
      java.util.List<com.nxrtweetg.sexmod.home.GirlHomeRegion> homes = com.nxrtweetg.sexmod.home.SexmodHomeData.get(server.overworld()).getHomesList();
      SyncHomesPayload payload = new SyncHomesPayload(homes);
      for (ServerPlayer p : server.getPlayerList().getPlayers()) {
         ServerPlayNetworking.send(p, payload);
      }
   }

   public static void spawnHomeBoundaryParticles(ServerLevel level, com.nxrtweetg.sexmod.home.GirlHomeRegion r) {
      int minX = r.minX();
      int minY = r.minY();
      int minZ = r.minZ();
      int maxX = r.maxX() + 1;
      int maxY = r.maxY() + 1;
      int maxZ = r.maxZ() + 1;

      for (double x = minX; x <= maxX; x += 1.0) {
         level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, x, minY, minZ, 1, 0, 0, 0, 0);
         level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, x, minY, maxZ, 1, 0, 0, 0, 0);
         level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, x, maxY, minZ, 1, 0, 0, 0, 0);
         level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, x, maxY, maxZ, 1, 0, 0, 0, 0);
      }
      for (double z = minZ; z <= maxZ; z += 1.0) {
         level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, minX, minY, z, 1, 0, 0, 0, 0);
         level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, maxX, minY, z, 1, 0, 0, 0, 0);
         level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, minX, maxY, z, 1, 0, 0, 0, 0);
         level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, maxX, maxY, z, 1, 0, 0, 0, 0);
      }
      for (double y = minY; y <= maxY; y += 1.0) {
         level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, minX, y, minZ, 1, 0, 0, 0, 0);
         level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, maxX, y, minZ, 1, 0, 0, 0, 0);
         level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, minX, y, maxZ, 1, 0, 0, 0, 0);
         level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, maxX, y, maxZ, 1, 0, 0, 0, 0);
      }
   }

   /** Same shape as the girls' in-world dialogue: {@code <Name> line}. */
   private static void sayLine(ServerPlayer player, String name, Component line) {
      player.displayClientMessage(Component.literal("<" + name + "> ").append(line), false);
   }

   private static void handleStartInteraction(ServerPlayer player, JennyStartInteractionPayload payload) {
      if (player.level().getEntity(payload.entityId()) instanceof SceneGirlEntity girl) {
         GirlSceneAction girlsceneaction = GirlSceneAction.fromLegacyId(payload.actionId()).orElse(null);
         if (girlsceneaction == null) {
            if (!girl.tryBeginCustomInteraction(player, payload.actionId())) {
               player.displayClientMessage(Component.literal("Unknown scene action."), false);
            }
         } else if (!girl.interceptSceneChoice(player, girlsceneaction)) {
            // she is free to answer for herself first: Bia's talk ends in a menu of scenes, and the
            // ordinary path would turn that choice down because the talk still counts as busy
            GirlInteractionService.tryBeginInteraction(player, girl, girlsceneaction);
         }
      } else {
         player.displayClientMessage(Component.literal("She is no longer available."), false);
      }
   }

   private static void handleSceneInput(ServerPlayer player, JennySceneInputPayload payload) {
      if (player.level().getEntity(payload.entityId()) instanceof SceneGirlEntity girl) {
         switch (payload.inputId()) {
            case "thrust":
               girl.applySceneInput(player, false);
               break;
            case "thrust_release":
               girl.applyPaceRelease(player);
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
               } else if (payload.inputId().startsWith(SceneGirlEntity.KEYFRAME_INPUT_PREFIX)) {
                  // the owning client reporting that its animation reached a structural keyframe;
                  // keyframes only fire client-side, so this is the server's cue to advance the beat
                  girl.applySceneKeyframe(player, payload.inputId().substring(SceneGirlEntity.KEYFRAME_INPUT_PREFIX.length()));
               } else {
                  player.displayClientMessage(Component.literal("Unknown Jenny scene input."), false);
               }
         }
      }
   }
}
