package com.nxrtweetg.sexmod.girls;

import com.nxrtweetg.sexmod.girls.state.GirlActivity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * "Go home": blocks movement, turns toward home, plays the pearl throw animation,
 * pauses in pose for ~1.5s, then snaps directly to home with portal particles and sound.
 */
public final class GirlGoHomeGoal extends Goal {
   private static final int TELEPORT_TICKS = 35;
   private static final int PORTAL_PARTICLES = 32;

   private final GirlEntity girl;
   private int ticks;

   public GirlGoHomeGoal(GirlEntity girl) {
      this.girl = girl;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
   }

   @Override
   public boolean canUse() {
      if (this.girl.hasActiveScene() || this.girl.isDowned() || !this.girl.coreState().isGoingHome()) {
         return false;
      }

      return this.girl.coreState().homePos() != null;
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
      this.girl.getNavigation().stop();
      this.girl.setDeltaMovement(Vec3.ZERO);

      BlockPos home = this.girl.coreState().homePos();
      if (home != null) {
         Vec3 homeCenter = Vec3.atBottomCenterOf(home);
         double dx = homeCenter.x - this.girl.getX();
         double dz = homeCenter.z - this.girl.getZ();
         float yaw = (float)(Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
         this.girl.setYRot(yaw);
         this.girl.setYHeadRot(yaw);
         this.girl.setYBodyRot(yaw);
      }

      this.girl.setActivity(GirlActivity.PEARL);
   }

   @Override
   public void stop() {
      if (this.girl.activity() == GirlActivity.PEARL) {
         this.girl.setActivity(GirlActivity.NONE);
      }
   }

   @Override
   public void tick() {
      BlockPos home = this.girl.coreState().homePos();
      if (home == null || !(this.girl.level() instanceof ServerLevel serverLevel)) {
         return;
      }

      this.girl.getNavigation().stop();
      this.girl.setDeltaMovement(Vec3.ZERO);

      Vec3 homeCenter = Vec3.atBottomCenterOf(home);
      this.girl.getLookControl().setLookAt(homeCenter.x, homeCenter.y + this.girl.getEyeHeight(), homeCenter.z);
      this.ticks++;

      if (this.ticks >= TELEPORT_TICKS) {
         this.teleportHome(serverLevel, homeCenter);
      }
   }

   private void teleportHome(ServerLevel world, Vec3 homeCenter) {
      for (int i = 0; i < PORTAL_PARTICLES; i++) {
         world.sendParticles(
            ParticleTypes.PORTAL,
            this.girl.getX(),
            this.girl.getY() + this.girl.getRandom().nextDouble() * 2.0,
            this.girl.getZ(),
            1, 0.2, 0.2, 0.2, 0.0
         );
      }
      world.playSound(null, this.girl.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 1.0F, 1.0F);

      this.girl.snapTo(homeCenter.x, homeCenter.y, homeCenter.z, this.girl.getYRot(), this.girl.getXRot());
      this.girl.setDeltaMovement(Vec3.ZERO);
      this.girl.fallDistance = 0.0;

      for (int i = 0; i < PORTAL_PARTICLES; i++) {
         world.sendParticles(
            ParticleTypes.PORTAL,
            homeCenter.x,
            homeCenter.y + this.girl.getRandom().nextDouble() * 2.0,
            homeCenter.z,
            1, 0.2, 0.2, 0.2, 0.0
         );
      }
      world.playSound(null, BlockPos.containing(homeCenter), SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 1.0F, 1.0F);

      this.girl.coreState().setGoingHome(false);
      this.girl.setActivity(GirlActivity.NONE);
      this.girl.setStayingAtHome(true);
   }
}
