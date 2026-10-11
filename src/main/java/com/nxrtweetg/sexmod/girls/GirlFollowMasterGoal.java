package com.nxrtweetg.sexmod.girls;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

/** Walks the girl after the player who used "Follow me", the original's "master" parameter. */
public final class GirlFollowMasterGoal extends Goal {
   private static final double START_FOLLOW_DISTANCE = 4.0;
   private static final double STOP_FOLLOW_DISTANCE = 2.0;
   private static final double WALK_SPEED = 0.5;
   private static final double SPRINT_SPEED = 0.7;
   private static final double DISTANCE_SPEEDUP_STEP = 5.0;
   private static final double DISTANCE_SPEEDUP = 0.2;
   private static final double WATER_SPEED_FACTOR = 60.0;
   private static final double MAX_SPEED = 2.0;
   private static final int REPATH_INTERVAL = 10;
   private static final int TELEPORT_ATTEMPTS = 20;
   private static final int TELEPORT_SPREAD = 10;

   private final GirlEntity girl;
   private Player master;
   private int repathCooldown;

   public GirlFollowMasterGoal(GirlEntity girl) {
      this.girl = girl;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
   }

   @Override
   public boolean canUse() {
      // Never follow while heading home or staying at home, or the teleport below would drag her back to the player.
      if (this.girl.hasActiveInteraction() || this.girl.isInteractingWithMenu() || !this.girl.coreState().hasMaster() || this.girl.coreState().isGoingHome() || this.girl.isStayingAtHome()) {
         return false;
      }

      Player player = this.girl.level().getPlayerByUUID(this.girl.coreState().masterId());
      if (player == null || player.isSpectator()) {
         return false;
      }

      this.master = player;
      return this.girl.distanceToSqr(player) > START_FOLLOW_DISTANCE * START_FOLLOW_DISTANCE;
   }

   @Override
   public boolean canContinueToUse() {
      return this.master != null
         && this.master.isAlive()
         && !this.girl.hasActiveInteraction()
         && !this.girl.isInteractingWithMenu()
         && this.girl.coreState().hasMaster()
         && !this.girl.coreState().isGoingHome()
         && !this.girl.isStayingAtHome()
         && this.girl.distanceToSqr(this.master) > STOP_FOLLOW_DISTANCE * STOP_FOLLOW_DISTANCE;
   }

   @Override
   public void stop() {
      this.master = null;
      this.girl.getNavigation().stop();
   }

   @Override
   public boolean requiresUpdateEveryTick() {
      return true;
   }

   @Override
   public void tick() {
      if (this.master == null) {
         return;
      }

      this.girl.getLookControl().setLookAt(this.master, 10.0F, this.girl.getMaxHeadXRot());

      // If tamed by lasso, max distance is 15 blocks. Otherwise follow range attribute.
      double maxDistance = this.girl.isTamedByLasso() ? 15.0 : this.girl.getAttributeValue(Attributes.FOLLOW_RANGE);
      if (this.girl.distanceToSqr(this.master) > maxDistance * maxDistance) {
         // Do not spam teleport while player is airborne/flying/falling; wait for landing
         if (!this.master.onGround() || this.master.isFallFlying() || this.master.getAbilities().flying) {
            return;
         }
         this.teleportToMaster();
         return;
      }

      double speed = this.followSpeed();
      this.girl.getNavigation().setSpeedModifier(speed);

      if (--this.repathCooldown > 0) {
         return;
      }

      this.repathCooldown = REPATH_INTERVAL;
      this.girl.getNavigation().moveTo(this.master, speed);
   }

   /** The original sped up the further behind she fell, rather than snapping to the player (f_class282.double_b). */
   private double followSpeed() {
      double speed = this.master.isSprinting() ? SPRINT_SPEED : WALK_SPEED;
      speed += Math.floor(this.girl.distanceTo(this.master) / DISTANCE_SPEEDUP_STEP) * DISTANCE_SPEEDUP;
      if (this.girl.isInWater()) {
         speed *= WATER_SPEED_FACTOR;
      }

      // The original's raw numbers assumed 1.12 movement scaling; unclamped they read as tens of
      // blocks per tick here, which trips the server's "moved too quickly" check.
      return Math.min(speed, MAX_SPEED);
   }

   private void teleportToMaster() {
      BlockPos masterPos = this.master.blockPosition();

      for (int attempt = 0; attempt < TELEPORT_ATTEMPTS; attempt++) {
         int x = masterPos.getX() + this.girl.getRandom().nextInt(TELEPORT_SPREAD);
         int z = masterPos.getZ() + this.girl.getRandom().nextInt(TELEPORT_SPREAD);
         if (this.girl.randomTeleport(x, masterPos.getY(), z, true)) {
            this.girl.getNavigation().stop();
            return;
         }
      }

      this.girl.snapTo(this.master.getX(), this.master.getY(), this.master.getZ(), this.girl.getYRot(), this.girl.getXRot());
      this.girl.getNavigation().stop();
   }
}
