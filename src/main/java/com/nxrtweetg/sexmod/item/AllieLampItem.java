package com.nxrtweetg.sexmod.item;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.SingletonGeoAnimatable;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.object.PlayState;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.util.GeckoLibUtil;
import com.nxrtweetg.sexmod.client.AllieSummonClientTracker;
import com.nxrtweetg.sexmod.client.SimpleGeoItemModel;
import com.nxrtweetg.sexmod.girls.ModEntities;
import com.nxrtweetg.sexmod.girls.allie.AllieEntity;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class AllieLampItem extends Item implements GeoItem {
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

   public AllieLampItem(Properties properties) {
      super(properties);
      SingletonGeoAnimatable.registerSyncedAnimatable(this);
   }

   @Override
   public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
      controllers.add(new AnimationController<>("controller", 0, state -> PlayState.CONTINUE));
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   @Override
   public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
      consumer.accept(new GeoRenderProvider() {
         private GeoItemRenderer<AllieLampItem> renderer;

         @Override
         public GeoItemRenderer<?> getGeoItemRenderer() {
            if (this.renderer == null) {
               this.renderer = new com.nxrtweetg.sexmod.client.AllieLampRenderer();
            }
            return this.renderer;
         }
      });
   }

   @Override
   public InteractionResult use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!level.isClientSide()) {
         AllieEntity allie = ModEntities.ALLIE.create(level, EntitySpawnReason.COMMAND);
         if (allie != null) {
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
            while (pos.getY() > level.getMinY() && !level.getBlockState(pos.below()).isSolid()) {
               pos.move(0, -1, 0);
            }
            while (pos.getY() < level.getMaxY() && level.getBlockState(pos).isSolid()) {
               pos.move(0, 1, 0);
            }
            double targetY = pos.getY();
            float yaw = player.getYRot() + 180.0F;
            allie.snapTo(targetX, targetY, targetZ, yaw, 0.0F);
            allie.setYRot(yaw);
            allie.setYHeadRot(yaw);
            allie.setYBodyRot(yaw);
            while (!level.noCollision(allie) && allie.getY() < player.getY() + 3.0) {
               allie.setPos(allie.getX(), allie.getY() + 0.5, allie.getZ());
            }
            allie.setSummoner(player.getUUID());
            allie.startSummoning();
            level.addFreshEntity(allie);
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
