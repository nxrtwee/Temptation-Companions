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
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
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

      dispatcher.register(
         Commands.literal("sexmod")
            .then(spawn)
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

   private static GirlSceneAction parseAction(String value) throws CommandSyntaxException {
      return GirlSceneAction.fromLegacyId(value).orElseThrow(INVALID_ACTION::create);
   }

   private static int spawnGirl(CommandSourceStack source, String character) throws CommandSyntaxException {
      ServerPlayer player = source.getPlayerOrException();
      ServerLevel level = player.level();
      EntityType<? extends SceneGirlEntity> type = ModEntities.all().get(character);
      SceneGirlEntity girl = type.create(level, EntitySpawnReason.COMMAND);
      if (girl == null) {
         return 0;
      }

      girl.snapTo(player.getX() + 1.5, player.getY(), player.getZ() + 1.5, player.getYRot(), 0.0F);
      level.addFreshEntity(girl);
      player.sendSystemMessage(Component.literal("Spawned " + girl.getName().getString() + "."));
      return 1;
   }
}
