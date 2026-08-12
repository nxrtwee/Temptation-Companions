package com.nxrtweetg.sexmod.girls;

import com.nxrtweetg.sexmod.girls.state.GirlActivity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * "Go home": she throws an ender pearl towards her home and teleports after it, as in the original
 * (gg_class366) rather than walking the whole way.
 *
 * <p>The original stepped this from the client-side {@code pearl} sound keyframe, which meant a
 * player looking away could leave her stuck forever. Here the steps run on the server, timed to the
 * same points of the looping throwpearl animation (keyframes at 0.958s and 2.875s).
 */
public final class GirlGoHomeGoal extends Goal {
   private static final int THROW_TICK = 19;
   private static final int TELEPORT_TICK = 58;
   private static final int CYCLE_TICKS = 92;
   private static final int MAX_ATTEMPTS = 3;
   private static final double ARRIVED_DISTANCE = 2.0;
   private static final double PEARL_LANDING_TOLERANCE = 5.0;
   private static final float MAX_PEARL_SPEED = 4.0F;
   private static final int PORTAL_PARTICLES = 32;

   private final GirlEntity girl;
   private ThrownEnderpearl pearl;
   private int ticks;
   private int attempts;

   public GirlGoHomeGoal(GirlEntity girl) {
      this.girl = girl;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
   }

   @Override
   public boolean canUse() {
      if (this.girl.hasActiveScene() || this.girl.isDowned() || !this.girl.coreState().isGoingHome()) {
         return false;
      }

      BlockPos home = this.girl.coreState().homePos();
      return home != null && !this.hasArrived(home);
   }

   @Override
   public boolean canContinueToUse() {
      return this.canUse();
   }

   @Override
   public boolean requiresUpdateEveryTick() {
      return true;
   }

   @Override
   public void start() {
      this.ticks = 0;
      this.attempts = 0;
      this.pearl = null;
      this.girl.getNavigation().stop();
      this.girl.setActivity(GirlActivity.PEARL);
   }

   @Override
   public void stop() {
      this.discardPearl();
      // Only clear our own state: being downed or pulled into a scene also stops this goal, and
      // resetting the activity there would undo whatever put her in that state.
      if (this.girl.activity() == GirlActivity.PEARL) {
         this.girl.setActivity(GirlActivity.NONE);
      }

      BlockPos home = this.girl.coreState().homePos();
      if (home != null && this.hasArrived(home)) {
         this.girl.coreState().setGoingHome(false);
      }
   }

   @Override
   public void tick() {
      BlockPos home = this.girl.coreState().homePos();
      if (home == null || !(this.girl.level() instanceof ServerLevel serverLevel)) {
         return;
      }

      Vec3 homeCenter = Vec3.atBottomCenterOf(home);
      this.girl.getLookControl().setLookAt(homeCenter.x, homeCenter.y + this.girl.getEyeHeight(), homeCenter.z);
      this.ticks++;

      if (this.ticks == THROW_TICK) {
         this.throwPearl(serverLevel, homeCenter);
      } else if (this.ticks == TELEPORT_TICK) {
         if (this.pearlLandedNearHome(homeCenter)) {
            this.teleportHome(serverLevel, homeCenter);
         } else {
            this.discardPearl();
         }
      } else if (this.ticks >= CYCLE_TICKS) {
         this.ticks = 0;
         this.discardPearl();
         if (++this.attempts >= MAX_ATTEMPTS) {
            // the pearl keeps missing (walls, unloaded terrain) — go the rest of the way directly
            this.teleportHome(serverLevel, homeCenter);
         }
      }
   }

   private void throwPearl(ServerLevel world, Vec3 homeCenter) {
      Vec3 origin = this.girl.getEyePosition();
      Vec3 delta = homeCenter.subtract(origin);
      float speed = (float)Math.min(MAX_PEARL_SPEED, delta.length() * 0.1);
      // No owner: a vanilla pearl teleports whoever threw it, which would fight our own teleport.
      ThrownEnderpearl thrown = new ThrownEnderpearl(world, this.girl, new ItemStack(Items.ENDER_PEARL));
      thrown.setOwner(null);
      thrown.snapTo(origin.x, origin.y, origin.z, this.girl.getYRot(), this.girl.getXRot());
      thrown.shoot(delta.x, delta.y, delta.z, speed, 0.0F);
      world.addFreshEntity(thrown);
      this.pearl = thrown;
   }

   private boolean pearlLandedNearHome(Vec3 homeCenter) {
      return this.pearl != null && this.pearl.position().distanceTo(homeCenter) < PEARL_LANDING_TOLERANCE;
   }

   private void teleportHome(ServerLevel world, Vec3 homeCenter) {
      for (int i = 0; i < PORTAL_PARTICLES; i++) {
         world.sendParticles(
            ParticleTypes.PORTAL, this.girl.getX(), this.girl.getY() + this.girl.getRandom().nextDouble() * 2.0, this.girl.getZ(), 1, 0.2, 0.2, 0.2, 0.0
         );
      }

      this.discardPearl();
      this.girl.snapTo(homeCenter.x, homeCenter.y, homeCenter.z, this.girl.getYRot(), this.girl.getXRot());
      this.girl.setDeltaMovement(Vec3.ZERO);
      this.girl.fallDistance = 0.0;
      this.girl.coreState().setGoingHome(false);
      this.girl.setActivity(GirlActivity.NONE);
   }

   private void discardPearl() {
      if (this.pearl != null) {
         this.pearl.discard();
         this.pearl = null;
      }
   }

   private boolean hasArrived(BlockPos home) {
      // horizontal only: the home block may sit a step above or below her feet
      double dx = this.girl.getX() - (home.getX() + 0.5);
      double dz = this.girl.getZ() - (home.getZ() + 0.5);
      return dx * dx + dz * dz <= ARRIVED_DISTANCE * ARRIVED_DISTANCE;
   }
}
