package com.nxrtweetg.sexmod.girls.manglelie;

import com.geckolib.animation.RawAnimation;
import com.nxrtweetg.sexmod.girls.ModDataSerializers;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationRole;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationSet;
import com.nxrtweetg.sexmod.girls.anim.GirlSoundSet;
import com.nxrtweetg.sexmod.girls.galath.GalathEntity;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.girls.scene.GirlScenePhase;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ManglelieEntity extends SceneGirlEntity {
   private static final Set<String> OWN_ANIMATIONS = Set.of(
      "angry_face", "bed_slow", "idle", "running", "scared_run", "sit_on_galath", "sit_on_galath2", "walk",
      "double_holding_slow", "double_holding_soft", "double_holding_hard", "double_holding_back", "double_holding_cum"
   );
   private static final Map<GirlAnimationRole, String> ANIMATION_ALIASES = Map.of(
      GirlAnimationRole.THREESOME_SLOW, "shared.double_holding_slow",
      GirlAnimationRole.THREESOME_FAST, "shared.double_holding_soft",
      GirlAnimationRole.THREESOME_CUM, "shared.double_holding_cum"
   );
   private static final GirlAnimationSet ANIMATIONS = new GirlAnimationSet("manglelie", OWN_ANIMATIONS, ANIMATION_ALIASES);
   private static final GirlSoundSet SOUNDS = new GirlSoundSet("manglelie", Map.of());

   private static final EntityDataAccessor<Optional<UUID>> TRACKED_LINKED_GALATH = SynchedEntityData.defineId(
      ManglelieEntity.class, ModDataSerializers.OPTIONAL_UUID
   );

   public ManglelieEntity(EntityType<? extends ManglelieEntity> entityType, Level world) {
      super(entityType, world, "Manglelie");
   }

   @Override
   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(TRACKED_LINKED_GALATH, Optional.empty());
   }

   @Override
   public String characterId() {
      return "manglelie";
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
   public boolean hasInteractionMenu() {
      return this.isTamedByLasso() || com.nxrtweetg.sexmod.config.SexmodConfig.isAllScenesEnabled();
   }

   @Override
   protected Set<GirlSceneAction> sceneActions() {
      return Set.of();
   }

   @Override
   public boolean hasPaymentPhase(GirlSceneAction action) {
      return false;
   }

   @Override
   public boolean hasCompanionCommands() {
      return true;
   }

   public GalathEntity getLinkedGalath() {
      return null;
   }

   public void linkGalath(GalathEntity galath) {
   }

   public void unlinkGalath() {
   }

   public boolean isRidingGalath() {
      return false;
   }

   @Override
   public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
      return super.hurtServer(level, source, amount);
   }

   @Override
   public void resetScene() {
      super.resetScene();
   }

   @Override
   public void tick() {
      super.tick();
   }

   @Override
   protected RawAnimation selectCustomMovementAnimation(boolean moving) {
      double dx = this.getX() - this.xOld;
      double dz = this.getZ() - this.zOld;
      boolean actuallyMoving = (dx * dx + dz * dz) > 0.0004;
      return actuallyMoving ? RawAnimation.begin().thenLoop("animation.manglelie.walk") : RawAnimation.begin().thenLoop("animation.manglelie.idle");
   }
}
