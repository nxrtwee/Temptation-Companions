package com.nxrtweetg.sexmod.effect;

import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Tells a companion entity that a horny potion landed on her.
 * Does not apply to players.
 */
final class HornyEffect extends MobEffect {
   HornyEffect() {
      super(MobEffectCategory.BENEFICIAL, ModEffects.HORNY_COLOR);
   }

   @Override
   public void onEffectStarted(LivingEntity entity, int amplifier) {
      if (entity.level().isClientSide()) {
         return;
      }

      if (entity instanceof SceneGirlEntity girl) {
         girl.applyHornyPotion();
      } else if (entity instanceof Player player) {
         player.removeEffect(ModEffects.HORNY);
      }
   }
}
