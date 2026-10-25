package com.nxrtweetg.sexmod.item;

import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public final class GirlSpawnEggItem extends Item {
   private final EntityType<? extends SceneGirlEntity> entityType;

   public GirlSpawnEggItem(EntityType<? extends SceneGirlEntity> entityType, Properties properties) {
      super(properties);
      this.entityType = entityType;
   }

   @Override
   public InteractionResult useOn(UseOnContext context) {
      if (!context.getLevel().isClientSide()) {
         var girl = this.entityType.create(context.getLevel(), EntitySpawnReason.SPAWN_ITEM_USE);
         if (girl != null) {
            BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
            float yaw = context.getPlayer() == null ? 0.0F : context.getPlayer().getYRot() + 180.0F;
            girl.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0.0F);
            girl.setPersistenceRequired();
            context.getLevel().addFreshEntity(girl);
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
