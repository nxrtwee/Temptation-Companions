package com.nxrtweetg.sexmod.girls.scene;

import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

public final class GirlInteractionService {
   private static final int BED_SEARCH_HORIZONTAL_RADIUS = 4;
   private static final int BED_SEARCH_VERTICAL_RADIUS = 2;

   private GirlInteractionService() {
   }

   public static Optional<SceneGirlEntity> findNearestGirl(ServerPlayer player, double radius) {
      ServerLevel serverlevel = player.level();
      List<SceneGirlEntity> list = serverlevel.getEntitiesOfClass(
         SceneGirlEntity.class, player.getBoundingBox().inflate(radius), entity -> entity.isAlive()
      );
      return list.stream().min(Comparator.comparingDouble(player::distanceToSqr));
   }

   public static boolean tryBeginInteraction(ServerPlayer player, GirlSceneAction action) {
      Optional<SceneGirlEntity> optional = findNearestGirl(player, 8.0);
      if (optional.isEmpty()) {
         player.sendSystemMessage(Component.literal("No girl found nearby."));
         return false;
      } else {
         return tryBeginInteraction(player, optional.get(), action);
      }
   }

   public static boolean tryBeginInteraction(ServerPlayer player, SceneGirlEntity girl, GirlSceneAction action) {
      if (!action.enabled()) {
         player.sendSystemMessage(Component.literal("That scene is not available."));
         return false;
      }

      if (!canInteract(player, girl)) {
         return false;
      }

      String name = girl.getName().getString();
      if (requiresBed(action)) {
         Optional<GirlInteractionService.BedAnchor> optional = findNearestBedAnchor(player, girl);
         if (optional.isEmpty()) {
            player.sendSystemMessage(Component.literal("You need a nearby bed for this sex scene."));
            return false;
         }

         girl.setSceneBedAnchor(optional.get().footPos(), optional.get().facing());
      } else {
         girl.clearSceneBedAnchor();
      }

      if (!GirlPayment.charge(player, new net.minecraft.world.item.ItemStack(girl.paymentItemFor(action).item(), girl.paymentAmountFor(action)))) {
         player.sendSystemMessage(Component.literal("<" + name + "> you cannot afford that..."));
         return false;
      }

      girl.beginInteraction(player, action);
      player.sendSystemMessage(Component.literal("Started " + name + " action: " + action.legacyId()));
      return true;
   }

   public static boolean canInteract(ServerPlayer player, SceneGirlEntity girl) {
      String name = girl.getName().getString();
      if (!girl.isAlive()) {
         player.sendSystemMessage(Component.literal(name + " is no longer available."));
         return false;
      }

      if (girl.coreState().activePlayerId() != null && player.level().getPlayerByUUID(girl.coreState().activePlayerId()) == null) {
         girl.resetScene();
      }

      if (girl.coreState().activePlayerId() != null && !girl.coreState().activePlayerId().equals(player.getUUID())) {
         player.sendSystemMessage(Component.literal(name + " is already busy."));
         return false;
      }

      if (girl.coreState().activePlayerId() != null && girl.coreState().activePlayerId().equals(player.getUUID()) && girl.hasActiveScene()) {
         player.sendSystemMessage(Component.literal("Finish or reset the current scene before starting another one."));
         return false;
      }

      if (player.distanceToSqr(girl) > 64.0) {
         player.sendSystemMessage(Component.literal("You are too far away from " + name + "."));
         return false;
      }
      return true;
   }

   private static boolean requiresBed(GirlSceneAction action) {
      return action == GirlSceneAction.DOGGY
         || action == GirlSceneAction.PRONE_DOGGY
         || action == GirlSceneAction.SITTING_ALT
         || action == GirlSceneAction.ANAL
         || action == GirlSceneAction.ANAL_ALT
         || action == GirlSceneAction.SITTING
         || action == GirlSceneAction.MISSIONARY
         || action == GirlSceneAction.MATING_PRESS
         || action == GirlSceneAction.COWGIRL
         || action == GirlSceneAction.REVERSE_COWGIRL;
   }

   private static Optional<GirlInteractionService.BedAnchor> findNearestBedAnchor(ServerPlayer player, SceneGirlEntity girl) {
      ServerLevel serverlevel = player.level();
      BlockPos blockpos = player.blockPosition();
      GirlInteractionService.BedAnchor bedanchor = null;
      double d0 = Double.MAX_VALUE;

      for (BlockPos blockpos1 : BlockPos.withinManhattan(blockpos, BED_SEARCH_HORIZONTAL_RADIUS, BED_SEARCH_VERTICAL_RADIUS, BED_SEARCH_HORIZONTAL_RADIUS)) {
         BlockState blockstate = serverlevel.getBlockState(blockpos1);
         if (blockstate.is(BlockTags.BEDS) && blockstate.getBlock() instanceof BedBlock) {
            Direction direction = (Direction)blockstate.getValue(BedBlock.FACING);
            BlockPos blockpos2 = blockstate.getValue(BedBlock.PART) == BedPart.FOOT
               ? blockpos1.immutable()
               : blockpos1.relative(direction.getOpposite()).immutable();
            BlockState blockstate1 = serverlevel.getBlockState(blockpos2);
            if (blockstate1.is(BlockTags.BEDS)) {
               double d1 = player.distanceToSqr(blockpos2.getX() + 0.5, blockpos2.getY() + 0.5, blockpos2.getZ() + 0.5);
               double d2 = girl.distanceToSqr(blockpos2.getX() + 0.5, blockpos2.getY() + 0.5, blockpos2.getZ() + 0.5);
               double d3 = d1 + d2;
               if (d3 < d0) {
                  d0 = d3;
                  bedanchor = new GirlInteractionService.BedAnchor(blockpos2, direction);
               }
            }
         }
      }

      return Optional.ofNullable(bedanchor);
   }

   private record BedAnchor(BlockPos footPos, Direction facing) {
   }
}
