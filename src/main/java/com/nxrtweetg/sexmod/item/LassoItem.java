package com.nxrtweetg.sexmod.item;

import com.nxrtweetg.sexmod.girls.GirlEntity;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;

/**
 * Item that captures any companion into a lasso upon right-click.
 * Right-clicking a block releases the companion, taming them, binding them to the player,
 * and conferring immortality.
 */
public final class LassoItem extends Item {
   public static final String TAG_HAS_GIRL = "HasGirl";
   public static final String TAG_ENTITY_TYPE = "EntityType";
   public static final String TAG_GIRL_NAME = "GirlName";
   public static final String TAG_ENTITY_DATA = "EntityData";

   public LassoItem(Properties properties) {
      super(properties.stacksTo(1));
   }

   public static boolean hasGirl(ItemStack stack) {
      CustomData data = stack.get(DataComponents.CUSTOM_DATA);
      if (data != null && !data.isEmpty()) {
         return data.copyTag().getBooleanOr(TAG_HAS_GIRL, false);
      }
      return false;
   }

   public static String getGirlName(ItemStack stack) {
      CustomData data = stack.get(DataComponents.CUSTOM_DATA);
      if (data != null && !data.isEmpty()) {
         return data.copyTag().getStringOr(TAG_GIRL_NAME, "");
      }
      return "";
   }

   public static void storeGirl(ItemStack stack, GirlEntity girl) {
      girl.setStayingAtHome(false);
      TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, girl.level().registryAccess());
      girl.saveWithoutId(output);
      CompoundTag entityTag = output.buildResult();

      CompoundTag tag = new CompoundTag();
      tag.putBoolean(TAG_HAS_GIRL, true);
      tag.putString(TAG_ENTITY_TYPE, BuiltInRegistries.ENTITY_TYPE.getKey(girl.getType()).toString());
      tag.putString(TAG_GIRL_NAME, girl.getName().getString());
      tag.put(TAG_ENTITY_DATA, entityTag);

      CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
   }

   public static void returnGirlToPlayerLasso(GirlEntity girl, ServerPlayer player) {
      girl.setStayingAtHome(false);
      ItemStack emptyLasso = ItemStack.EMPTY;
      for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
         ItemStack invStack = player.getInventory().getItem(i);
         if (invStack.is(ModItems.LASSO) && !hasGirl(invStack)) {
            emptyLasso = invStack;
            break;
         }
      }

      ItemStack filledLasso = new ItemStack(ModItems.LASSO);
      storeGirl(filledLasso, girl);

      if (!emptyLasso.isEmpty()) {
         emptyLasso.shrink(1);
         if (!player.getInventory().add(filledLasso)) {
            player.drop(filledLasso, false);
         }
      } else {
         if (!player.getInventory().add(filledLasso)) {
            player.drop(filledLasso, false);
         }
      }

      player.level().playSound(null, player.blockPosition(), SoundEvents.LEAD_TIED, SoundSource.PLAYERS, 1.0F, 1.2F);
      player.sendSystemMessage(
         Component.translatable("chat.sexmod.lasso.returned", girl.getName().getString()).withStyle(ChatFormatting.YELLOW)
      );

      girl.discard();
   }

   @Override
   public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
      if (!(target instanceof GirlEntity girl)) {
         return InteractionResult.PASS;
      }

      if (girl instanceof com.nxrtweetg.sexmod.girls.allie.AllieEntity
            || girl instanceof com.nxrtweetg.sexmod.girls.bee.BeeEntity
            || girl instanceof com.nxrtweetg.sexmod.girls.slime.SlimeGirlEntity) {
         return InteractionResult.PASS;
      }

      if (hasGirl(stack)) {
         return InteractionResult.PASS;
      }

      Level level = player.level();
      if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
         storeGirl(stack, girl);

         serverLevel.sendParticles(ParticleTypes.HEART,
            girl.getX(), girl.getY() + 1.0, girl.getZ(),
            12, 0.3, 0.4, 0.3, 0.05);
         level.playSound(null, girl.blockPosition(), SoundEvents.LEAD_TIED, SoundSource.PLAYERS, 1.0F, 1.2F);

         girl.discard();
      }

      return InteractionResult.SUCCESS;
   }

   @Override
   public InteractionResult useOn(UseOnContext context) {
      ItemStack stack = context.getItemInHand();
      if (!hasGirl(stack)) {
         return InteractionResult.PASS;
      }

      Player player = context.getPlayer();
      Level level = context.getLevel();

      if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
         CustomData data = stack.get(DataComponents.CUSTOM_DATA);
         if (data == null || data.isEmpty()) {
            return InteractionResult.PASS;
         }

         CompoundTag tag = data.copyTag();
         String typeId = tag.getStringOr(TAG_ENTITY_TYPE, "");
         CompoundTag entityTag = tag.getCompoundOrEmpty(TAG_ENTITY_DATA);

         EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(Identifier.parse(typeId))
            .map(Holder.Reference::value)
            .orElse(null);

         if (type != null) {
            Entity spawned = type.create(serverLevel, EntitySpawnReason.TRIGGERED);
            if (spawned instanceof GirlEntity girl) {
               if (!entityTag.isEmpty()) {
                  ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, serverLevel.registryAccess(), entityTag);
                  girl.load(input);
               }

               BlockPos clicked = context.getClickedPos();
               Direction face = context.getClickedFace();
               BlockPos spawnPos = clicked.relative(face);
               girl.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
               float yaw = player != null ? player.getYRot() + 180.0F : 0.0F;
               girl.setYRot(yaw);
               girl.setYBodyRot(yaw);
               girl.setYHeadRot(yaw);

               girl.setPersistenceRequired();
               girl.setStayingAtHome(false);
               if (player != null) {
                  girl.startFollowing(player);
               }
               girl.setTamedByLasso(true);

               serverLevel.addFreshEntity(girl);

               serverLevel.sendParticles(ParticleTypes.CLOUD,
                  girl.getX(), girl.getY() + 0.5, girl.getZ(),
                  15, 0.3, 0.4, 0.3, 0.05);
               level.playSound(null, spawnPos, SoundEvents.LEAD_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);

               // Reset lasso to empty
               stack.remove(DataComponents.CUSTOM_DATA);
            }
         }
      }

      return InteractionResult.SUCCESS;
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipAdder, TooltipFlag flag) {
      if (hasGirl(stack)) {
         String name = getGirlName(stack);
         tooltipAdder.accept(Component.translatable("item.sexmod.lasso.contains", name).withStyle(ChatFormatting.GOLD));
         tooltipAdder.accept(Component.translatable("item.sexmod.lasso.release_hint").withStyle(ChatFormatting.GRAY));
      } else {
         tooltipAdder.accept(Component.translatable("item.sexmod.lasso.empty").withStyle(ChatFormatting.GRAY));
      }
   }
}
