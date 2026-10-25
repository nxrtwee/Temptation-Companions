package com.nxrtweetg.sexmod.girls;

import com.nxrtweetg.sexmod.girls.state.GirlActivity;
import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Ranged half of the bodyguard behaviour: with a bow in her bow slot she keeps her distance and
 * shoots, as in the original (g_class338's BOW branch: charge for 32 ticks past 6 blocks, then a
 * 20-tick recovery). Damage comes from vanilla power scaling instead of the original's flat 4.5.
 */
public final class GirlBowAttackGoal extends Goal {
   private static final double MIN_BOW_RANGE = 6.0;
   private static final double MAX_BOW_RANGE = 20.0;
   private static final int CHARGE_TICKS = 32;
   private static final int RECOVERY_TICKS = 20;
   private static final double STRAFE_SPEED = 0.5;
   private static final float STRAFE_FLIP_CHANCE = 0.3F;

   private final GirlEntity girl;
   private LivingEntity target;
   private int charge;
   private boolean strafeRight;

   public GirlBowAttackGoal(GirlEntity girl) {
      this.girl = girl;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
   }

   @Override
   public boolean canUse() {
      if (this.girl.hasActiveScene() || this.girl.isDowned() || !this.hasBow()) {
         return false;
      }

      LivingEntity candidate = this.girl.getTarget();
      if (candidate == null || !candidate.isAlive()) {
         return false;
      }

      double distanceSqr = this.girl.distanceToSqr(candidate);
      if (distanceSqr < MIN_BOW_RANGE * MIN_BOW_RANGE || distanceSqr > MAX_BOW_RANGE * MAX_BOW_RANGE) {
         return false;
      }

      if (!this.girl.getSensing().hasLineOfSight(candidate)) {
         return false;
      }

      this.target = candidate;
      return true;
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
      this.charge = 0;
      this.girl.setActivity(GirlActivity.COMBAT_BOW);
   }

   @Override
   public void stop() {
      this.target = null;
      this.charge = 0;
      this.girl.getNavigation().stop();
      if (this.girl.activity() == GirlActivity.COMBAT_BOW) {
         this.girl.setActivity(GirlActivity.NONE);
      }
   }

   @Override
   public void tick() {
      if (this.target == null) {
         return;
      }

      // setLookAt only turns the head, so on its own she keeps firing with her back to the target.
      this.girl.lookAt(this.target, 30.0F, 30.0F);
      this.girl.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
      this.girl.getNavigation().stop();
      // Vanilla only flips the circling direction occasionally; every tick reads as a vibration.
      if (this.girl.getRandom().nextFloat() < STRAFE_FLIP_CHANCE) {
         this.strafeRight = !this.strafeRight;
      }

      this.girl.getMoveControl().strafe(0.0F, (float)(this.strafeRight ? STRAFE_SPEED : -STRAFE_SPEED));

      if (++this.charge >= CHARGE_TICKS) {
         this.shoot();
         this.charge = -RECOVERY_TICKS;
      }
   }

   private void shoot() {
      if (!(this.girl.level() instanceof ServerLevel world)) {
         return;
      }

      ItemStack bow = this.bowStack();
      float power = BowItem.getPowerForTime(CHARGE_TICKS);
      Arrow arrow = new Arrow(world, this.girl, new ItemStack(Items.ARROW), bow);
      double dx = this.target.getX() - this.girl.getX();
      double dy = this.target.getY(0.3333333333333333) - arrow.getY();
      double dz = this.target.getZ() - this.girl.getZ();
      double horizontal = Math.sqrt(dx * dx + dz * dz);
      arrow.shoot(dx, dy + horizontal * 0.2, dz, power * 3.0F, 1.0F);
      world.addFreshEntity(arrow);
      this.girl.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (this.girl.getRandom().nextFloat() * 0.4F + 0.8F));
   }

   private boolean hasBow() {
      ItemStack stack = this.bowStack();
      // The slot filter also accepts crossbows, so shooting must too or she just stands there.
      return stack.is(Items.BOW) || stack.is(Items.CROSSBOW);
   }

   private ItemStack bowStack() {
      return this.girl.equipmentContainer().getItem(com.nxrtweetg.sexmod.girls.inventory.GirlEquipmentSlot.Kind.BOW.ordinal());
   }
}
