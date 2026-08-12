package com.nxrtweetg.sexmod.item;

import com.nxrtweetg.sexmod.girls.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public final class KoboldEggItem extends Item {
   public KoboldEggItem(Properties properties) {
      super(properties);
   }

   @Override
   public InteractionResult useOn(UseOnContext context) {
      if (!context.getLevel().isClientSide()) {
         var kobold = ModEntities.KOBOLD.create(context.getLevel(), EntitySpawnReason.COMMAND);
         if (kobold != null) {
            BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
            float yaw = context.getPlayer() == null ? 0.0F : context.getPlayer().getYRot() + 180.0F;
            kobold.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0.0F);
            context.getLevel().addFreshEntity(kobold);
            Player player = context.getPlayer();
            ItemStack stack = context.getItemInHand();
            if (player == null || !player.getAbilities().instabuild) {
               stack.shrink(1);
            }
         }
      }

      return InteractionResult.SUCCESS;
   }
}
