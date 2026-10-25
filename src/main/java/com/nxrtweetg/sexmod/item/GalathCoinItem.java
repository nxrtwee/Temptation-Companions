package com.nxrtweetg.sexmod.item;

import com.nxrtweetg.sexmod.girls.ModEntities;
import com.nxrtweetg.sexmod.girls.galath.GalathEntity;
import com.nxrtweetg.sexmod.girls.manglelie.ManglelieEntity;
import java.util.function.Consumer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class GalathCoinItem extends Item {
   public GalathCoinItem(Properties properties) {
      super(properties);
   }

   @Override
   public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
      if (target instanceof GalathEntity galath) {
         Level level = player.level();
         if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            ManglelieEntity mang = galath.getLinkedManglelie();
            if (mang != null) {
               serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                  mang.getX(), mang.getY() + 0.5, mang.getZ(),
                  30, 0.3, 0.4, 0.3, 0.05);
               mang.discard();
            }
            serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
               galath.getX(), galath.getY() + 1.0, galath.getZ(),
               60, 0.4, 0.8, 0.4, 0.05);
            level.playSound(null, galath.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            galath.discard();
         }
         return InteractionResult.SUCCESS;
      }
      return InteractionResult.PASS;
   }

   @Override
   public InteractionResult use(Level level, Player player, InteractionHand hand) {
      if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
         GalathEntity existing = null;
         for (GalathEntity candidate : serverLevel.getEntitiesOfClass(GalathEntity.class, player.getBoundingBox().inflate(64.0))) {
            existing = candidate;
            break;
         }

         Vec3 look = player.getLookAngle();
         double horizLen = Math.sqrt(look.x * look.x + look.z * look.z);
         double nx = horizLen > 1e-4 ? look.x / horizLen : 0.0;
         double nz = horizLen > 1e-4 ? look.z / horizLen : 1.0;
         double targetX = player.getX() + nx * 2.0;
         double targetZ = player.getZ() + nz * 2.0;

         net.minecraft.core.BlockPos.MutableBlockPos pos = new net.minecraft.core.BlockPos.MutableBlockPos(
            net.minecraft.util.Mth.floor(targetX),
            net.minecraft.util.Mth.floor(player.getY() + 1.0),
            net.minecraft.util.Mth.floor(targetZ)
         );
         while (pos.getY() > serverLevel.getMinY() && !serverLevel.getBlockState(pos.below()).isSolid()) {
            pos.move(0, -1, 0);
         }
         while (pos.getY() < serverLevel.getMaxY() && serverLevel.getBlockState(pos).isSolid()) {
            pos.move(0, 1, 0);
         }
         double targetY = pos.getY();
         float yaw = player.getYRot() + 180.0F;

         if (existing != null) {
            existing.teleportTo(targetX, targetY, targetZ);
            existing.setYRot(yaw);
            existing.setYBodyRot(yaw);
            existing.setYHeadRot(yaw);
            while (!serverLevel.noCollision(existing) && existing.getY() < player.getY() + 3.0) {
               existing.setPos(existing.getX(), existing.getY() + 0.5, existing.getZ());
            }
            ManglelieEntity mang = existing.getLinkedManglelie();
            if (mang != null) {
               mang.teleportTo(existing.getX() + 1.0, existing.getY(), existing.getZ());
               mang.setYRot(yaw);
               mang.setYBodyRot(yaw);
               mang.setYHeadRot(yaw);
            }
            serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
               targetX, targetY + 1.0, targetZ,
               40, 0.3, 0.6, 0.3, 0.05);
            level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.2F);
         } else {
            GalathEntity galath = ModEntities.GALATH.create(serverLevel, EntitySpawnReason.COMMAND);
            if (galath != null) {
               galath.snapTo(targetX, targetY, targetZ, yaw, 0.0F);
               galath.setYRot(yaw);
               galath.setYBodyRot(yaw);
               galath.setYHeadRot(yaw);
               while (!serverLevel.noCollision(galath) && galath.getY() < player.getY() + 3.0) {
                  galath.setPos(galath.getX(), galath.getY() + 0.5, galath.getZ());
               }
               serverLevel.addFreshEntity(galath);

               // Also summon Manglelie nearby on the ground
               ManglelieEntity mang = ModEntities.MANGLELIE.create(serverLevel, EntitySpawnReason.COMMAND);
               if (mang != null) {
                  mang.snapTo(galath.getX() + 1.0, galath.getY(), galath.getZ(), yaw, 0.0F);
                  mang.setYRot(yaw);
                  mang.setYBodyRot(yaw);
                  mang.setYHeadRot(yaw);
                  while (!serverLevel.noCollision(mang) && mang.getY() < player.getY() + 3.0) {
                     mang.setPos(mang.getX(), mang.getY() + 0.5, mang.getZ());
                  }
                  serverLevel.addFreshEntity(mang);
               }

               serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                  targetX, targetY + 1.0, targetZ,
                  80, 0.5, 0.8, 0.5, 0.05);
               level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.8F);
            }
         }
      }
      return InteractionResult.SUCCESS;
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipAdder, TooltipFlag flag) {
      tooltipAdder.accept(Component.translatable("item.sexmod.galath_coin.desc"));
   }
}
