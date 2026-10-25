package com.nxrtweetg.sexmod.item;

import com.nxrtweetg.sexmod.girls.ModEntities;
import com.nxrtweetg.sexmod.girls.kobold.KoboldColors;
import com.nxrtweetg.sexmod.girls.kobold.KoboldEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public final class TribeEggItem extends Item {
   private static final double[][] OFFSETS = {
      {0.0, 0.0},
      {1.2, 0.8},
      {-1.0, 1.1},
      {0.5, -1.2}
   };

   public TribeEggItem(Properties properties) {
      super(properties);
   }

   @Override
   public InteractionResult useOn(UseOnContext context) {
      if (!context.getLevel().isClientSide() && context.getLevel() instanceof ServerLevel serverLevel) {
         BlockPos center = context.getClickedPos().relative(context.getClickedFace());
         float yaw = context.getPlayer() == null ? 0.0F : context.getPlayer().getYRot() + 180.0F;
         RandomSource random = serverLevel.getRandom();
         int tribeColorIndex = KoboldColors.TribeColor.random(random).ordinal();
         int eyeColorArgb = KoboldColors.randomEyeArgb(random);

         for (double[] offset : OFFSETS) {
            KoboldEntity kobold = ModEntities.KOBOLD.create(serverLevel, null, null, MobSpawnType.COMMAND, false, false);
            if (kobold != null) {
               kobold.moveTo(center.getX() + 0.5 + offset[0], center.getY(), center.getZ() + 0.5 + offset[1], yaw, 0.0F);
               kobold.setTribeColorIndex(tribeColorIndex);
               kobold.setEyeColorArgb(eyeColorArgb);
               serverLevel.addFreshEntity(kobold);
            }
         }

         Player player = context.getPlayer();
         ItemStack stack = context.getItemInHand();
         if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
         }
      }

      return InteractionResult.SUCCESS;
   }
}
