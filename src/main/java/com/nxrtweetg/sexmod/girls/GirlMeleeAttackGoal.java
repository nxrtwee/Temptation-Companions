package com.nxrtweetg.sexmod.girls;

import com.nxrtweetg.sexmod.girls.state.GirlActivity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

/**
 * Melee half of the bodyguard behaviour. Vanilla's goal already handles pathing and the attack
 * cooldown from ATTACK_SPEED; this only adds the scene bail-out and the animation state, and lets
 * {@code doHurtTarget} compute damage from the equipped weapon (the original hand-rolled both).
 */
public final class GirlMeleeAttackGoal extends MeleeAttackGoal {
   private static final double SPEED = 0.7;

   private final GirlEntity girl;

   public GirlMeleeAttackGoal(GirlEntity girl) {
      super(girl, SPEED, true);
      this.girl = girl;
   }

   @Override
   public boolean canUse() {
      return !this.girl.hasActiveScene() && !this.girl.isDowned() && super.canUse();
   }

   @Override
   public boolean canContinueToUse() {
      return !this.girl.hasActiveScene() && !this.girl.isDowned() && super.canContinueToUse();
   }

   @Override
   public void start() {
      super.start();
      this.girl.setActivity(GirlActivity.COMBAT_MELEE);
   }

   @Override
   public void stop() {
      super.stop();
      if (this.girl.activity() == GirlActivity.COMBAT_MELEE) {
         this.girl.setActivity(GirlActivity.NONE);
      }
   }

   @Override
   protected void checkAndPerformAttack(LivingEntity target) {
      boolean willSwing = this.isTimeToAttack() && this.canPerformAttack(target);
      super.checkAndPerformAttack(target);
      if (willSwing) {
         // Server-authoritative, unlike the original's client keyframe counter.
         this.girl.cycleAttackVariant();
      }
   }
}
