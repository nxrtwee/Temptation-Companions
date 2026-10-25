package com.nxrtweetg.sexmod.item;

import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;
import com.nxrtweetg.sexmod.client.SimpleGeoItemModel;
import com.nxrtweetg.sexmod.girls.kobold.KoboldEntity;
import java.util.function.Consumer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class DragonStaffItem extends Item implements GeoItem, software.bernie.geckolib.animatable.client.GeoRenderProvider {
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

   public DragonStaffItem(Properties properties) {
      super(properties);
      SingletonGeoAnimatable.registerSyncedAnimatable(this);
   }

   @Override
   public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
      controllers.add(new AnimationController<>(this, "controller", 0, state -> PlayState.CONTINUE));
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   @Override
   public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
      consumer.accept(new GeoRenderProvider() {
         private GeoItemRenderer<DragonStaffItem> renderer;

         @Override
         public GeoItemRenderer<?> getGeoItemRenderer() {
            if (this.renderer == null) {
               GeoItemRenderer<DragonStaffItem> r = new GeoItemRenderer<>(new SimpleGeoItemModel<>(
                  ResourceLocation.fromNamespaceAndPath("sexmod", "kobold/staff"),
                  ResourceLocation.fromNamespaceAndPath("sexmod", "textures/entity/kobold/staff.png"),
                  ResourceLocation.fromNamespaceAndPath("sexmod", "kobold/staff")
               ));
               r.addRenderLayer(new com.nxrtweetg.sexmod.client.DragonStaffCrystalLayer(r));
               this.renderer = r;
            }
            return this.renderer;
         }
      });
   }

   @Override
   public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
      if (target instanceof KoboldEntity kobold) {
         Level level = player.level();
         if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            if (!kobold.coreState().hasMaster()) {
               kobold.startFollowing(player);
               serverLevel.sendParticles(ParticleTypes.HEART,
                  kobold.getX(), kobold.getY() + 1.2, kobold.getZ(),
                  7, 0.3, 0.3, 0.3, 0.05);
               level.playSound(null, kobold.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
            } else {
               serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                  kobold.getX(), kobold.getY() + 1.2, kobold.getZ(),
                  5, 0.3, 0.3, 0.3, 0.05);
            }
         }
         return InteractionResult.SUCCESS;
      }
      return InteractionResult.PASS;
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltipComponents, TooltipFlag flag) {
      tooltipComponents.add(Component.translatable("item.sexmod.dragon_staff.desc"));
   }
}
