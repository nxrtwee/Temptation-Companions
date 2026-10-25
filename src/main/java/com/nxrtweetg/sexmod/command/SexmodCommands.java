package com.nxrtweetg.sexmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.nxrtweetg.sexmod.girls.ModEntities;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.scene.GirlInteractionService;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.home.GirlHomeRegion;
import com.nxrtweetg.sexmod.home.SexmodHomeData;
import com.nxrtweetg.sexmod.network.SexmodNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;

public final class SexmodCommands {
   private static final SimpleCommandExceptionType INVALID_ACTION = new SimpleCommandExceptionType(
      Component.literal(
         "Unknown action. Use blowjob, kobold_blowjob, deepthroat, facefuck, boobjob, touch_boobs, doggy, prone_doggy, anal, kobold_anal, sitting, missionary, mating_press, cowgirl, reverse_cowgirl, or strip."
      )
   );

   private SexmodCommands() {
   }

   public static void register() {
      CommandRegistrationCallback.EVENT.register((CommandRegistrationCallback)(dispatcher, registryAccess, environment) -> register(dispatcher));
   }

   private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      LiteralArgumentBuilder<CommandSourceStack> spawn = Commands.literal("spawn");

      for (String character : ModEntities.all().keySet()) {
         spawn = spawn.then(Commands.literal(character).executes(context -> spawnGirl((CommandSourceStack)context.getSource(), character)));
      }

      spawn = spawn.then(Commands.argument("custom_name", StringArgumentType.string())
         .suggests((context, builder) -> {
            for (String name : com.nxrtweetg.sexmod.girls.custom.TCEditorLoader.getAvailableNames()) {
               builder.suggest(name);
            }
            return builder.buildFuture();
         })
         .executes(context -> {
            String name = StringArgumentType.getString(context, "custom_name");
            return spawnCustomGirl(context.getSource(), name);
         }));

      LiteralArgumentBuilder<CommandSourceStack> structure = Commands.literal("structure");
      for (String structName : java.util.Arrays.asList("jenny", "ellie", "bia", "luna", "goblin")) {
         structure = structure.then(Commands.literal(structName).executes(context -> {
            CommandSourceStack source = (CommandSourceStack)context.getSource();
            ServerPlayer player = source.getPlayerOrException();
            ServerLevel level = source.getLevel();
            net.minecraft.core.BlockPos pos = player.blockPosition();
            com.nxrtweetg.sexmod.worldgen.SexmodWorldGen.StructureDef def = com.nxrtweetg.sexmod.worldgen.SexmodWorldGen.getDefByName(structName);
            boolean ok;
            if (def != null) {
               ok = com.nxrtweetg.sexmod.worldgen.SexmodWorldGen.placeStructure(level, pos, def, net.minecraft.world.level.block.Rotation.NONE);
            } else {
               ok = com.nxrtweetg.sexmod.worldgen.SexmodWorldGen.placeStructureDirect(level, pos, structName, net.minecraft.world.level.block.Rotation.NONE);
            }
            if (ok) {
               source.sendSuccess(() -> Component.literal("Placed structure " + structName + " at " + pos.toShortString()), true);
               return 1;
            } else {
               source.sendFailure(Component.literal("Failed to place structure " + structName));
               return 0;
            }
         }));
      }

      LiteralArgumentBuilder<CommandSourceStack> locateCmd = Commands.literal("locate");
      LiteralArgumentBuilder<CommandSourceStack> locateStruct = Commands.literal("structure");
      for (String structName : java.util.Arrays.asList("jenny", "ellie", "bia", "luna", "goblin")) {
         locateStruct = locateStruct.then(Commands.literal(structName).executes(context -> locateSexmodStructure(context.getSource(), structName)));
         locateStruct = locateStruct.then(Commands.literal("sexmod:" + structName).executes(context -> locateSexmodStructure(context.getSource(), structName)));
      }
      locateCmd = locateCmd.then(locateStruct);

      com.mojang.brigadier.tree.CommandNode<CommandSourceStack> locateNode = dispatcher.getRoot().getChild("locate");
      if (locateNode != null) {
         com.mojang.brigadier.tree.CommandNode<CommandSourceStack> vanillaStructNode = locateNode.getChild("structure");
         if (vanillaStructNode != null) {
            for (String structName : java.util.Arrays.asList("jenny", "ellie", "bia", "luna", "goblin")) {
               vanillaStructNode.addChild(Commands.literal(structName).executes(context -> locateSexmodStructure(context.getSource(), structName)).build());
               vanillaStructNode.addChild(Commands.literal("sexmod:" + structName).executes(context -> locateSexmodStructure(context.getSource(), structName)).build());
            }
         }
      }

      LiteralArgumentBuilder<CommandSourceStack> sethome = Commands.literal("sethome")
         .then(Commands.argument("pos1", BlockPosArgument.blockPos())
            .then(Commands.argument("pos2", BlockPosArgument.blockPos())
               .then(Commands.argument("name", StringArgumentType.word())
                  .executes(context -> {
                     CommandSourceStack source = context.getSource();
                     ServerPlayer player = source.getPlayerOrException();
                     BlockPos pos1 = BlockPosArgument.getBlockPos(context, "pos1");
                     BlockPos pos2 = BlockPosArgument.getBlockPos(context, "pos2");
                     String name = StringArgumentType.getString(context, "name");
                     GirlHomeRegion region = GirlHomeRegion.create(name, player.level().dimension().location().toString(), pos1, pos2);
                     SexmodHomeData.get(player.serverLevel()).addHome(region);
                     SexmodNetworking.spawnHomeBoundaryParticles(player.serverLevel(), region);
                     SexmodNetworking.broadcastHomes(source.getServer());
                     source.sendSuccess(() -> Component.literal("Home '" + name + "' created [" + region.sizeX() + "x" + region.sizeY() + "x" + region.sizeZ() + "]"), true);
                     return 1;
                  }))))
         .then(Commands.argument("coord1", StringArgumentType.word())
            .then(Commands.argument("coord2", StringArgumentType.word())
               .then(Commands.argument("name", StringArgumentType.word())
                  .executes(context -> {
                     CommandSourceStack source = context.getSource();
                     ServerPlayer player = source.getPlayerOrException();
                     String c1 = StringArgumentType.getString(context, "coord1");
                     String c2 = StringArgumentType.getString(context, "coord2");
                     String name = StringArgumentType.getString(context, "name");
                     BlockPos pos1 = parseCoordString(c1, player.blockPosition());
                     BlockPos pos2 = parseCoordString(c2, player.blockPosition());
                     GirlHomeRegion region = GirlHomeRegion.create(name, player.level().dimension().location().toString(), pos1, pos2);
                     SexmodHomeData.get(player.serverLevel()).addHome(region);
                     SexmodNetworking.spawnHomeBoundaryParticles(player.serverLevel(), region);
                     SexmodNetworking.broadcastHomes(source.getServer());
                     source.sendSuccess(() -> Component.literal("Home '" + name + "' created [" + region.sizeX() + "x" + region.sizeY() + "x" + region.sizeZ() + "]"), true);
                     return 1;
                  }))));

      LiteralArgumentBuilder<CommandSourceStack> delhome = Commands.literal("delhome")
         .then(Commands.argument("name", StringArgumentType.word())
            .executes(context -> {
               CommandSourceStack source = context.getSource();
               String name = StringArgumentType.getString(context, "name");
               boolean ok = SexmodHomeData.get(source.getLevel()).removeHome(name);
               if (ok) {
                  SexmodNetworking.broadcastHomes(source.getServer());
                  source.sendSuccess(() -> Component.literal("Home '" + name + "' deleted."), true);
                  return 1;
               } else {
                  source.sendFailure(Component.literal("Home '" + name + "' not found."));
                  return 0;
               }
            }));

      LiteralArgumentBuilder<CommandSourceStack> listhomes = Commands.literal("listhomes")
         .executes(context -> {
            CommandSourceStack source = context.getSource();
            java.util.Collection<GirlHomeRegion> list = SexmodHomeData.get(source.getLevel()).getAllHomes();
            if (list.isEmpty()) {
               source.sendSuccess(() -> Component.literal("No homes defined."), false);
            } else {
               source.sendSuccess(() -> Component.literal("Homes (" + list.size() + "):"), false);
               for (GirlHomeRegion h : list) {
                  source.sendSuccess(() -> Component.literal(" - " + h.name() + ": center " + h.center().toShortString() + " [" + h.sizeX() + "x" + h.sizeY() + "x" + h.sizeZ() + "]"), false);
               }
            }
            return 1;
         });

      LiteralArgumentBuilder<CommandSourceStack> showhome = Commands.literal("showhome")
         .then(Commands.argument("name", StringArgumentType.word())
            .executes(context -> {
               CommandSourceStack source = context.getSource();
               String name = StringArgumentType.getString(context, "name");
               GirlHomeRegion h = SexmodHomeData.get(source.getLevel()).getHome(name);
               if (h != null) {
                  SexmodNetworking.spawnHomeBoundaryParticles(source.getLevel(), h);
                  source.sendSuccess(() -> Component.literal("Showing boundary for '" + name + "'"), false);
                  return 1;
               } else {
                  source.sendFailure(Component.literal("Home '" + name + "' not found."));
                  return 0;
               }
            }));

      dispatcher.register(
         Commands.literal("sexmod")
            .then(spawn)
            .then(structure)
            .then(locateCmd)
            .then(sethome)
            .then(delhome)
            .then(listhomes)
            .then(showhome)
            .then(Commands.literal("scene").then(Commands.argument("action", StringArgumentType.word()).suggests((context, builder) -> {
               for (GirlSceneAction action : GirlSceneAction.values()) {
                  if (action.enabled()) {
                     builder.suggest(action.legacyId());
                  }
               }

               return builder.buildFuture();
            }).executes(context -> {
               CommandSourceStack source = (CommandSourceStack)context.getSource();
               GirlSceneAction action = parseAction(StringArgumentType.getString(context, "action"));
               return GirlInteractionService.tryBeginInteraction(source.getPlayerOrException(), action) ? 1 : 0;
            })))
      );
   }

   private static BlockPos parseCoordString(String str, BlockPos fallback) {
      try {
         String[] parts = str.split("[_,~]");
         if (parts.length == 3) {
            int x = parts[0].isEmpty() ? fallback.getX() : Integer.parseInt(parts[0]);
            int y = parts[1].isEmpty() ? fallback.getY() : Integer.parseInt(parts[1]);
            int z = parts[2].isEmpty() ? fallback.getZ() : Integer.parseInt(parts[2]);
            return new BlockPos(x, y, z);
         }
      } catch (Exception ignored) {
      }
      return fallback;
   }

   private static GirlSceneAction parseAction(String value) throws CommandSyntaxException {
      return GirlSceneAction.fromLegacyId(value).orElseThrow(INVALID_ACTION::create);
   }


   private static int spawnGirl(CommandSourceStack source, String character) throws CommandSyntaxException {
      ServerPlayer player = source.getPlayerOrException();
      ServerLevel level = player.serverLevel();
      EntityType<? extends SceneGirlEntity> type = ModEntities.all().get(character);
      SceneGirlEntity girl = type.create(level, null, null, MobSpawnType.COMMAND, false, false);
      if (girl == null) {
         return 0;
      }

      if (girl instanceof com.nxrtweetg.sexmod.girls.custom.CustomGirlEntity customGirl) {
         java.util.Set<String> names = com.nxrtweetg.sexmod.girls.custom.TCEditorLoader.getAvailableNames();
         if (!names.isEmpty()) {
            customGirl.setCustomId(names.iterator().next());
         }
      }

      girl.moveTo(player.getX() + 1.5, player.getY(), player.getZ() + 1.5, player.getYRot(), 0.0F);
      level.addFreshEntity(girl);
      player.displayClientMessage(Component.literal("Spawned " + girl.getName().getString() + "."), false);
      return 1;
   }

   private static int spawnCustomGirl(CommandSourceStack source, String characterName) throws CommandSyntaxException {
      ServerPlayer player = source.getPlayerOrException();
      ServerLevel level = player.serverLevel();
      com.nxrtweetg.sexmod.girls.custom.CustomGirlEntity girl = ModEntities.CUSTOM_GIRL.create(level, null, null, MobSpawnType.COMMAND, false, false);
      if (girl == null) {
         return 0;
      }

      girl.setCustomId(characterName);
      girl.moveTo(player.getX() + 1.5, player.getY(), player.getZ() + 1.5, player.getYRot(), 0.0F);
      level.addFreshEntity(girl);
      player.displayClientMessage(Component.literal("Spawned custom companion " + girl.getName().getString() + "."), false);
      return 1;
   }

   private static int locateSexmodStructure(CommandSourceStack source, String structName) throws CommandSyntaxException {
      ServerPlayer player = source.getPlayerOrException();
      ServerLevel level = source.getLevel();
      net.minecraft.core.BlockPos targetPos = com.nxrtweetg.sexmod.worldgen.SexmodWorldGen.findNearestStructure(level, player.blockPosition(), structName);
      if (targetPos != null) {
         int dist = (int) Math.round(Math.sqrt(player.blockPosition().distSqr(targetPos)));
         String tpCmd = "/tp @s " + targetPos.getX() + " ~ " + targetPos.getZ();
         Component coords = Component.literal("[" + targetPos.getX() + ", ~, " + targetPos.getZ() + "]")
            .withStyle(style -> style
               .withColor(net.minecraft.ChatFormatting.GREEN)
               .withClickEvent(new net.minecraft.network.chat.ClickEvent(net.minecraft.network.chat.ClickEvent.Action.SUGGEST_COMMAND, tpCmd))
               .withHoverEvent(new net.minecraft.network.chat.HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.literal("Click to teleport"))));

         source.sendSuccess(() -> Component.literal("The nearest " + structName + " is at ").append(coords).append(" (" + dist + " blocks away)"), false);
         return 1;
      } else {
         source.sendFailure(Component.literal("Could not find nearby structure '" + structName + "'"));
         return 0;
      }
   }
}
