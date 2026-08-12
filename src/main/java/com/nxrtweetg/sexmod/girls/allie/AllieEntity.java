package com.nxrtweetg.sexmod.girls.allie;

import com.geckolib.animation.RawAnimation;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationSet;
import com.nxrtweetg.sexmod.girls.anim.GirlSoundSet;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class AllieEntity extends SceneGirlEntity {
   private static final int SUMMON_INTRO_TICKS = 95;
   // The imported Allie animation file is not GeckoLib-5-safe yet; use Jenny's stable rig mapping
   // until the Allie-specific json is cleaned up.
   private static final GirlAnimationSet ANIMATIONS = GirlAnimationSet.JENNY;
   private static final GirlSoundSet SOUNDS = new GirlSoundSet("allie", Map.of());
   private static final EntityDataAccessor<Boolean> TRACKED_SUMMONING = SynchedEntityData.defineId(AllieEntity.class, EntityDataSerializers.BOOLEAN);
   private int summonTicks;

   public AllieEntity(EntityType<? extends AllieEntity> entityType, Level world) {
      super(entityType, world, "Allie");
   }

   @Override
   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(TRACKED_SUMMONING, false);
   }

   @Override
   public String characterId() {
      return "allie";
   }

   @Override
   protected GirlAnimationSet animations() {
      return ANIMATIONS;
   }

   @Override
   protected GirlSoundSet sounds() {
      return SOUNDS;
   }

   @Override
   protected Set<GirlSceneAction> sceneActions() {
      return EnumSet.of(GirlSceneAction.DEEPTHROAT, GirlSceneAction.REVERSE_COWGIRL);
   }

   @Override
   protected RawAnimation selectCustomMovementAnimation(boolean moving) {
      return null;
   }

   @Override
   protected boolean hasCustomSceneLock() {
      return this.isSummoning();
   }

   @Override
   protected void tickCustomServer() {
      if (!this.isSummoning()) {
         return;
      }

      this.summonTicks++;
      this.setDeltaMovement(Vec3.ZERO);
      if (this.summonTicks > SUMMON_INTRO_TICKS + 40) {
         this.summonTicks = SUMMON_INTRO_TICKS;
      }
   }

   @Override
   public void beginInteraction(net.minecraft.server.level.ServerPlayer player, GirlSceneAction action) {
      this.stopSummoning();
      super.beginInteraction(player, action);
   }

   @Override
   public void resetScene() {
      super.resetScene();
      this.stopSummoning();
   }

   public void startSummoning() {
      this.getEntityData().set(TRACKED_SUMMONING, true);
      this.summonTicks = 0;
      this.setNoGravity(true);
      this.setDeltaMovement(Vec3.ZERO);
   }

   public void stopSummoning() {
      this.getEntityData().set(TRACKED_SUMMONING, false);
      this.summonTicks = 0;
      this.setNoGravity(false);
   }

   public boolean isSummoning() {
      return this.getEntityData().get(TRACKED_SUMMONING);
   }
}
