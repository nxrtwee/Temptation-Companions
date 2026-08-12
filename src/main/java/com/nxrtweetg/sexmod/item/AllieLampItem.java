package com.nxrtweetg.sexmod.item;

import com.nxrtweetg.sexmod.client.AllieSummonClientTracker;
import com.nxrtweetg.sexmod.girls.ModEntities;
import com.nxrtweetg.sexmod.girls.allie.AllieEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class AllieLampItem extends Item {
   public AllieLampItem(Properties properties) {
      super(properties);
   }

   @Override
   public InteractionResult use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!level.isClientSide()) {
         AllieEntity allie = ModEntities.ALLIE.create(level, EntitySpawnReason.COMMAND);
         if (allie != null) {
            Vec3 spawn = player.position().add(player.getLookAngle().normalize().scale(2.0));
            float yaw = player.getYRot() + 180.0F;
            allie.snapTo(spawn.x, spawn.y, spawn.z, yaw, 0.0F);
            allie.startSummoning();
            level.addFreshEntity(allie);
            if (!player.getAbilities().instabuild) {
               stack.shrink(1);
            }
         }
      } else {
         queueAutoOpen();
      }

      return InteractionResult.SUCCESS;
   }

   @Environment(EnvType.CLIENT)
   private static void queueAutoOpen() {
      AllieSummonClientTracker.queueOpen();
   }
}
