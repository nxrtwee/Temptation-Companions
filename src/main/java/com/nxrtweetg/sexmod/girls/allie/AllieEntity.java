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
   private static final Set<String> OWN_ANIMATIONS = Set.of(
      "attack0", "attack1", "attack2", "blink", "bowcharge", "deepthroat_cum", "deepthroat_fast",
      "deepthroat_normal_prepare", "deepthroat_prepare", "deepthroat_slow", "deepthroat_start",
      "null", "reverse_cowgirl_cum", "reverse_cowgirl_fastc1", "reverse_cowgirl_fastc2",
      "reverse_cowgirl_fastc3", "reverse_cowgirl_fasts", "reverse_cowgirl_slow1",
      "reverse_cowgirl_slow2", "reverse_cowgirl_slow3", "reverse_cowgirl_start",
      "rich", "rich_normal", "summon", "summon_normal", "summon_normal_wait",
      "summon_sand", "summon_wait", "tail"
   );
   private static final GirlAnimationSet ANIMATIONS = new GirlAnimationSet("allie", OWN_ANIMATIONS);
   private static final Map<String, Integer> OWN_SOUNDS = Map.ofEntries(
      Map.entry("aftersessionmoan", 4),
      Map.entry("ahh", 10),
      Map.entry("bjmoan", 14),
      Map.entry("giggle", 5),
      Map.entry("happyoh", 3),
      Map.entry("heavybreathing", 8),
      Map.entry("hmph", 5),
      Map.entry("huh", 2),
      Map.entry("lightbreathing", 11),
      Map.entry("lipsound", 14),
      Map.entry("mmm", 10),
      Map.entry("moan", 8),
      Map.entry("sadoh", 2),
      Map.entry("scawy", 3),
      Map.entry("sigh", 2)
   );
   private static final GirlSoundSet SOUNDS = new GirlSoundSet("allie", OWN_SOUNDS, Map.of());
   private static final EntityDataAccessor<Boolean> TRACKED_SUMMONING = SynchedEntityData.defineId(AllieEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> TRACKED_DISMISSING = SynchedEntityData.defineId(AllieEntity.class, EntityDataSerializers.BOOLEAN);
   private int summonTicks;
   private int dismissTicks;
   private java.util.UUID summonerPlayerUuid = null;
   private boolean hadActiveScene = false;

   public AllieEntity(EntityType<? extends AllieEntity> entityType, Level world) {
      super(entityType, world, "Allie");
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.FloatGoal(this));
      this.goalSelector.addGoal(6, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, net.minecraft.world.entity.player.Player.class, 3.0F));
      this.goalSelector.addGoal(7, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(this));
   }

   @Override
   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(TRACKED_SUMMONING, false);
      builder.define(TRACKED_DISMISSING, false);
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
   public boolean hasPaymentPhase(GirlSceneAction action) {
      return false;
   }

   @Override
   public boolean hasCompanionCommands() {
      return true;
   }

   @Override
   public boolean requiresBed(GirlSceneAction action) {
      if (action == GirlSceneAction.REVERSE_COWGIRL) {
         return false;
      }
      return super.requiresBed(action);
   }

   @Override
   protected boolean handleCustomSoundKeyframe(String keyframe) {
      switch (keyframe) {
         case "summonMSG1" -> {
            this.sayDialogue("summon1");
            this.playSoundVariant("girls.allie.scawy.scawy", 3, 0.5F);
            return true;
         }
         case "summonMSG2" -> {
            this.sayDialogue("summon2");
            this.playSoundVariant("girls.allie.giggle.giggle", 5, 1.0F);
            return true;
         }
         case "summonMSG3" -> {
            this.sayDialogue("summon3");
            return true;
         }
         case "summonMSG4" -> {
            this.sayDialogue("summon4");
            this.playSoundVariant("girls.allie.lightbreathing.lightbreathing", 11, 1.0F);
            return true;
         }
         case "summonMSG5" -> {
            this.sayDialogue("summon5");
            this.playSoundVariant("girls.allie.hmph.hmph", 5, 1.0F);
            return true;
         }
         case "summonMSG6" -> {
            this.sayDialogue("summon6");
            this.playSoundVariant("girls.allie.giggle.giggle", 5, 1.0F);
            return true;
         }
         case "summonMSG7" -> {
            this.sayDialogue("summon7");
            return true;
         }
         case "summonMSG8" -> {
            this.sayDialogue("summon8");
            this.playSoundVariant("girls.allie.huh.huh", 2, 1.0F);
            return true;
         }
         case "summonDone", "summon_normalDone" -> {
            this.summonTicks = SUMMON_INTRO_TICKS;
            return true;
         }
         case "summon_normalMSG1" -> {
            this.playSoundVariant("girls.allie.giggle.giggle", 5, 1.0F);
            return true;
         }
         case "deepthroat_prepareMSG1" -> {
            this.sayDialogue("hihi");
            this.playSoundVariant("girls.allie.giggle.giggle", 5, 1.0F);
            return true;
         }
         case "deepthroat_prepareMSG2" -> {
            this.sayDialogue("boys");
            this.playSoundVariant("girls.allie.sigh.sigh", 2, 1.0F);
            return true;
         }
         case "deepthroat_fastMSG1" -> {
            this.playSoundVariant("girls.allie.bjmoan.bjmoan", 14, 1.0F);
            return true;
         }
         case "deepthroat_slowMSG1" -> {
            if (this.random.nextFloat() > 0.33F) {
               this.playSoundVariant("girls.allie.lipsound.lipsound", 14, 1.0F);
            } else {
               this.playSoundVariant("girls.allie.bjmoan.bjmoan", 14, 1.0F);
            }
            return true;
         }
         case "deepthroat_cumMSG1" -> {
            this.playSoundVariant("girls.allie.moan.moan", 8, 1.0F);
            this.playSoundVariant("girls.allie.lipsound.lipsound", 14, 1.0F);
            this.playSoundVariant("misc.cuminflation.cuminflation", 1, 1.5F);
            return true;
         }
         default -> {
            return false;
         }
      }
   }

   @Override
   protected RawAnimation selectCustomMovementAnimation(boolean moving) {
      if (this.isDismissing() || this.isSummoning()) {
         return this.animation(com.nxrtweetg.sexmod.girls.anim.GirlAnimationRole.NULL);
      }
      return RawAnimation.begin().thenLoop("animation.allie.tail");
   }

   @Override
   protected RawAnimation selectCustomActionAnimation() {
      if (this.isDismissing()) {
         return RawAnimation.begin().thenPlayAndHold("animation.allie.summon_normal_wait");
      }
      if (this.isSummoning()) {
         boolean sand = this.level().getBlockState(this.blockPosition().below()).is(net.minecraft.tags.BlockTags.SAND);
         if (this.summonTicks >= SUMMON_INTRO_TICKS) {
            return RawAnimation.begin().thenLoop(sand ? "animation.allie.summon_wait" : "animation.allie.summon_normal_wait");
         }
         return RawAnimation.begin().thenPlayAndHold(sand ? "animation.allie.summon_sand" : "animation.allie.summon_normal");
      }
      return null;
   }

   @Override
   protected boolean hasCustomSceneLock() {
      return this.isSummoning() || this.isDismissing();
   }

   @Override
   public void tick() {
      super.tick();
      if (this.isDismissing()) {
         this.dismissTicks++;
         this.setDeltaMovement(Vec3.ZERO);
         if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(
               net.minecraft.core.particles.ParticleTypes.PORTAL,
               this.getX(), this.getY() + 0.5 + this.random.nextDouble() * 0.8, this.getZ(),
               4, 0.2, 0.3, 0.2, 0.0
            );
            serverLevel.sendParticles(
               net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL,
               this.getX(), this.getY() + 0.5 + this.random.nextDouble() * 0.8, this.getZ(),
               2, 0.2, 0.3, 0.2, 0.0
            );
         }
         if (this.dismissTicks >= 30) {
            this.discard();
         }
         return;
      }

      if (this.isSummoning()) {
         this.summonTicks++;
         this.setDeltaMovement(Vec3.ZERO);
         if (this.summonTicks > SUMMON_INTRO_TICKS + 40) {
            this.summonTicks = SUMMON_INTRO_TICKS;
         }
         if (!this.level().isClientSide() && !this.hadActiveScene && !this.hasActiveScene()) {
            net.minecraft.world.entity.player.Player summoner = this.summonerPlayerUuid != null ? this.level().getPlayerByUUID(this.summonerPlayerUuid) : null;
            if (summoner == null || summoner.distanceToSqr(this) > 100.0) {
               this.startDismissal(false);
            }
         }
      } else {
         this.summonTicks = 0;
      }
   }

   @Override
   public void beginInteraction(net.minecraft.server.level.ServerPlayer player, GirlSceneAction action) {
      this.hadActiveScene = true;
      this.stopSummoning();
      super.beginInteraction(player, action);
   }

   @Override
   public boolean tryBeginCustomInteraction(net.minecraft.server.level.ServerPlayer player, String actionId) {
      this.hadActiveScene = true;
      this.stopSummoning();
      return super.tryBeginCustomInteraction(player, actionId);
   }

   @Override
   public void resetScene() {
      boolean wasActive = this.hadActiveScene || this.hasActiveScene();
      super.resetScene();
      this.stopSummoning();
      if (wasActive && !this.level().isClientSide()) {
         this.startDismissal(true);
      }
   }

   public void setSummoner(java.util.UUID uuid) {
      this.summonerPlayerUuid = uuid;
   }

   public java.util.UUID summoner() {
      return this.summonerPlayerUuid;
   }

   public boolean isDismissing() {
      return this.getEntityData().get(TRACKED_DISMISSING);
   }

   public float getDismissScale(float partialTick) {
      if (!this.isDismissing()) {
         return 1.0F;
      }
      float t = this.dismissTicks + partialTick;
      return Math.max(0.0F, 1.0F - (t / 30.0F));
   }

   public void startDismissal(boolean consumeLamp) {
      if (this.isDismissing() || this.isRemoved()) {
         return;
      }
      this.stopSummoning();
      this.getEntityData().set(TRACKED_DISMISSING, true);
      this.dismissTicks = 0;
      this.setDeltaMovement(Vec3.ZERO);

      if (consumeLamp && !this.level().isClientSide()) {
         net.minecraft.world.entity.player.Player player = this.summonerPlayerUuid != null ? this.level().getPlayerByUUID(this.summonerPlayerUuid) : null;
         if (player == null && this.trackedActivePlayer().isPresent()) {
            player = this.level().getPlayerByUUID(this.trackedActivePlayer().get());
         }
         if (player != null && !player.getAbilities().instabuild) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
               net.minecraft.world.item.ItemStack stack = player.getInventory().getItem(i);
               if (stack.is(com.nxrtweetg.sexmod.item.ModItems.ALLIES_LAMP)) {
                  stack.shrink(1);
                  break;
               }
            }
         }
      }

      this.level().playSound(null, this.blockPosition(), net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.2F);
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

   @Override
   protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
      super.addAdditionalSaveData(output);
      if (this.summonerPlayerUuid != null) {
         output.putString("AllieSummoner", this.summonerPlayerUuid.toString());
      }
      output.putBoolean("AllieHadActiveScene", this.hadActiveScene);
   }

   @Override
   protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
      super.readAdditionalSaveData(input);
      input.getString("AllieSummoner").ifPresent(s -> {
         try {
            this.summonerPlayerUuid = java.util.UUID.fromString(s);
         } catch (Exception ignored) {
         }
      });
      this.hadActiveScene = input.getBooleanOr("AllieHadActiveScene", false);
   }
}
