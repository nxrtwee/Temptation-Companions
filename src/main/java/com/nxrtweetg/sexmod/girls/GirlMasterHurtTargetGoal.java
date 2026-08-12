package com.nxrtweetg.sexmod.girls;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

/**
 * Makes her defend her master: she picks up whatever hurt him, or whatever he attacked recently.
 * Vanilla's OwnerHurt*TargetGoal pair only accepts TamableAnimal, so this reproduces it against the
 * mod's own "master" UUID, including vanilla's timestamp de-duplication so a single attack does not
 * re-trigger the goal every tick for the whole 140-tick window (the window is the original's).
 */
public final class GirlMasterHurtTargetGoal extends TargetGoal {
   private static final int RECENT_ATTACK_TICKS = 140;

   private final GirlEntity girl;
   private LivingEntity pendingTarget;
   private int timestamp;

   public GirlMasterHurtTargetGoal(GirlEntity girl) {
      super(girl, false);
      this.girl = girl;
      this.setFlags(EnumSet.of(Flag.TARGET));
   }

   @Override
   public boolean canUse() {
      if (this.girl.hasActiveScene() || this.girl.isDowned()) {
         return false;
      }

      Player master = this.girl.master();
      if (master == null) {
         return false;
      }

      LivingEntity candidate = null;
      int candidateTimestamp = 0;

      if (master.getLastHurtByMob() != null && master.tickCount - master.getLastHurtByMobTimestamp() < RECENT_ATTACK_TICKS) {
         candidate = master.getLastHurtByMob();
         candidateTimestamp = master.getLastHurtByMobTimestamp();
      } else if (master.getLastHurtMob() != null && master.tickCount - master.getLastHurtMobTimestamp() < RECENT_ATTACK_TICKS) {
         candidate = master.getLastHurtMob();
         candidateTimestamp = master.getLastHurtMobTimestamp();
      }

      // Same attack as last time: let whatever she is already fighting keep her attention.
      if (candidate == null || candidateTimestamp == this.timestamp) {
         return false;
      }

      if (candidate instanceof GirlEntity || candidate instanceof Player || candidate == master) {
         return false;
      }

      this.pendingTarget = candidate;
      this.timestamp = candidateTimestamp;
      return this.canAttack(candidate, TargetingConditions.DEFAULT);
   }

   @Override
   public void start() {
      this.mob.setTarget(this.pendingTarget);
      this.targetMob = this.pendingTarget;
      super.start();
   }
}
