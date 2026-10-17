package com.nxrtweetg.sexmod.girls.scene;

import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/**
 * Where a girl stands to start a bed scene.
 * Prioritizes beds that are reachable on foot within a reasonable room radius (<= 7 blocks),
 * preventing characters from pathing into walls or snapping across distant rooms.
 */
public final class GirlBedTable {
   /**
    * How wide, in blocks, the scan around the origin reaches.
    * Reduced to 7 blocks horizontally and 2 vertically so she picks beds in the same room
    * instead of pathing towards distant beds behind walls in other rooms.
    */
   private static final int SEARCH_HORIZONTAL_RADIUS = 20;
   private static final int SEARCH_VERTICAL_RADIUS = 5;
   private static final int MAX_PATH_NODES = 64;

   /** Probe directions, in table order: -Z end, +Z end, -X side, +X side. */
   private static final Direction[] PROBE = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
   /** The yaw each candidate looks along, which is always back towards the bed. */
   private static final float[] YAW = {0.0F, 180.0F, -90.0F, 90.0F};
   /**
    * How far past the probed block's centre each candidate sits, along the probe axis. A whole block
    * for the standing variants; the sit-on-the-edge variant stops 0.18 off the mattress instead.
    */
   private static final double BLOCK_OFFSET = 1.0;
   private static final double EDGE_OFFSET = 0.68;

   private GirlBedTable() {
   }

   /** How forgiving the surroundings check is for one scene. */
   public enum Variant {
      /**
       * Bia's doggy ({@code e0.a()}): the spot must be clear <em>and</em> the block opposite it must be
       * the other half of the same bed, so only the two ends of the bed qualify.
       */
      STRICT,
      /**
       * Jenny's doggy ({@code ex.a()}) and Bia's anal ({@code e0.b()}): the spot only has to be clear,
       * so all four sides qualify and she takes whichever is nearest - the "random" placement the bed
       * scenes are supposed to have.
       */
      LOOSE,
      /** Sit-on-the-edge scenes: the two ends again, but the spot hugs the mattress. */
      EDGE;

      /** Which line she says when nothing in range works. */
      public String failureDialogueKey() {
         return this == LOOSE ? "bedobscured" : "nobedinsight";
      }
   }

   /**
    * A resolved spot: the bed it belongs to, plus where she stands and which way she looks.
    */
   public record Anchor(BlockPos footPos, Direction facing, BlockPos bedPos, boolean alongBedAxis, Vec3 standPos, float standYaw) {
   }

   public static Optional<Anchor> find(SceneGirlEntity girl, Variant variant) {
      return find(girl.level(), girl.blockPosition(), girl.position(), variant, girl);
   }

   public static Optional<Anchor> find(Level level, BlockPos searchOrigin, Vec3 girlPos, Variant variant) {
      return find(level, searchOrigin, girlPos, variant, null);
   }

   /**
    * Finds the nearest reachable bed spot within room reach.
    * Validates pathfinding reachability so beds behind walls or in inaccessible rooms are ignored.
    */
   public static Optional<Anchor> find(Level level, BlockPos searchOrigin, Vec3 girlPos, Variant variant, SceneGirlEntity girl) {
      Anchor best = null;
      double bestMetric = Double.MAX_VALUE;

      for (BlockPos probe : BlockPos.betweenClosed(
         searchOrigin.offset(-SEARCH_HORIZONTAL_RADIUS, -SEARCH_VERTICAL_RADIUS, -SEARCH_HORIZONTAL_RADIUS),
         searchOrigin.offset(SEARCH_HORIZONTAL_RADIUS, SEARCH_VERTICAL_RADIUS, SEARCH_HORIZONTAL_RADIUS)
      )) {
         BlockState state = level.getBlockState(probe);
         if (!state.is(BlockTags.BEDS) || !(state.getBlock() instanceof BedBlock)) {
            continue;
         }

         BlockPos block = probe.immutable();
         Direction facing = state.getValue(BedBlock.FACING);
         BlockPos footPos = state.getValue(BedBlock.PART) == BedPart.FOOT ? block : block.relative(facing.getOpposite());
         if (!level.getBlockState(footPos).is(BlockTags.BEDS)) {
            continue;
         }

         for (int candidate = 0; candidate < PROBE.length; candidate++) {
            Direction dir = PROBE[candidate];
            if (!isValid(level, block, dir, variant)) {
               continue;
            }

            Vec3 standPos = standPos(block, dir, variant);
            double distSqr = standPos.distanceToSqr(girlPos);
            double metric = distSqr;

            // Validate path reachability if we have the girl entity available
            if (girl != null) {
               if (distSqr > 1.5 * 1.5) {
                  Path path = girl.getNavigation().createPath(standPos.x, standPos.y, standPos.z, 0);
                  if (path == null || !path.canReach()) {
                     // Inaccessible on foot! (Solid wall in between, no door, obstructed path)
                     continue;
                  }
                  if (path.getNodeCount() > MAX_PATH_NODES) {
                     // Too far away or winding path through other rooms
                     continue;
                  }
                  metric = path.getNodeCount();
               }
            }

            if (metric < bestMetric) {
               bestMetric = metric;
               best = new Anchor(footPos, facing, block, dir.getAxis() == facing.getAxis(), standPos, YAW[candidate]);
            }
         }
      }

      return Optional.ofNullable(best);
   }

   private static boolean isValid(Level level, BlockPos block, Direction dir, Variant variant) {
      BlockPos standBlock = block.relative(dir);
      BlockState standState = level.getBlockState(standBlock);
      // Stand position must not be a solid wall block (allows air, carpets, etc.)
      if (!standState.isAir() && standState.isSolid()) {
         return false;
      }
      // Head level must not be solid
      if (level.getBlockState(standBlock.above()).isSolid()) {
         return false;
      }

      // STRICT and EDGE additionally demand that the bed continues on the far side, which restricts
      // the spot to one of the bed's two ends.
      return variant == Variant.LOOSE || level.getBlockState(block.relative(dir.getOpposite())).is(BlockTags.BEDS);
   }

   private static Vec3 standPos(BlockPos block, Direction dir, Variant variant) {
      double offset = variant == Variant.EDGE ? EDGE_OFFSET : BLOCK_OFFSET;
      Vec3 center = Vec3.atBottomCenterOf(block);
      return new Vec3(center.x + dir.getStepX() * offset, block.getY(), center.z + dir.getStepZ() * offset);
   }

   /** Which variant a scene uses, or empty when the scene needs no bed at all. */
   public static Optional<Variant> variantFor(GirlSceneAction action) {
      return switch (action) {
         case PRONE_DOGGY, SITTING_ALT -> Optional.of(Variant.STRICT);
         case DOGGY, ANAL, ANAL_ALT -> Optional.of(Variant.LOOSE);
         case SITTING, MISSIONARY, COWGIRL, REVERSE_COWGIRL -> Optional.of(Variant.EDGE);
         default -> Optional.empty();
      };
   }
}
