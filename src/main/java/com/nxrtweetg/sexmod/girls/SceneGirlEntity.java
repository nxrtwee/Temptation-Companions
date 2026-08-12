package com.nxrtweetg.sexmod.girls;

import com.nxrtweetg.sexmod.girls.scene.GirlSceneLayoutConfig;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationLengths;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationRole;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationSet;
import com.nxrtweetg.sexmod.girls.anim.GirlSceneAnimations;
import com.nxrtweetg.sexmod.girls.anim.GirlSoundSet;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneStarter;
import com.nxrtweetg.sexmod.girls.scene.GirlScenePhase;
import com.nxrtweetg.sexmod.girls.state.GirlActivity;
import com.nxrtweetg.sexmod.girls.state.GirlAnimationParameter;
import com.nxrtweetg.sexmod.girls.state.GirlFollowUpAction;
import com.nxrtweetg.sexmod.girls.state.GirlPaymentItem;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.locale.Language;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animatable.manager.AnimatableManager.ControllerRegistrar;
import com.geckolib.animation.state.KeyFrameEvent;
import com.geckolib.cache.animation.keyframeevent.SoundKeyframeData;
import com.geckolib.util.GeckoLibUtil;

public abstract class SceneGirlEntity extends GirlEntity implements GeoEntity {
   private static final int PAYMENT_AUTO_FINISH_TICKS = 184;
   /** The original glides her to the player over this many ticks rather than pathfinding to him. */
   private static final int APPROACH_GLIDE_TICKS = 40;
   /** How many ticks before a snapTo transition the server fires the fade signal. */
   private static final int FADE_LEAD_TICKS = 20;
   private static final int STRIP_MODEL_REVEAL_TICKS = 96;
   private static final int STRIP_AUTO_FINISH_TICKS = 100;
   private static final EntityDataAccessor<String> TRACKED_ACTIVE_ACTION = SynchedEntityData.defineId(SceneGirlEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<String> TRACKED_PAYMENT_ITEM = SynchedEntityData.defineId(SceneGirlEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<Integer> TRACKED_PAYMENT_AMOUNT = SynchedEntityData.defineId(SceneGirlEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<String> TRACKED_FOLLOW_UP = SynchedEntityData.defineId(SceneGirlEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<Boolean> TRACKED_BUSY = SynchedEntityData.defineId(SceneGirlEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Optional<UUID>> TRACKED_ACTIVE_PLAYER = SynchedEntityData.defineId(
      SceneGirlEntity.class, ModDataSerializers.OPTIONAL_UUID
   );
   private static final EntityDataAccessor<String> TRACKED_SCENE_PHASE = SynchedEntityData.defineId(SceneGirlEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<Integer> TRACKED_MODEL_VARIANT = SynchedEntityData.defineId(SceneGirlEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> TRACKED_RENDER_PLAYER = SynchedEntityData.defineId(SceneGirlEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> TRACKED_THRUSTING = SynchedEntityData.defineId(SceneGirlEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> TRACKED_INTRO_ACTIVE = SynchedEntityData.defineId(SceneGirlEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> TRACKED_SCENE_TUNE_FORWARD = SynchedEntityData.defineId(SceneGirlEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> TRACKED_SCENE_TUNE_SIDE = SynchedEntityData.defineId(SceneGirlEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> TRACKED_SCENE_TUNE_VERTICAL = SynchedEntityData.defineId(SceneGirlEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> TRACKED_SCENE_FROZEN = SynchedEntityData.defineId(SceneGirlEntity.class, EntityDataSerializers.BOOLEAN);
   /** Monotonically incrementing counter. Each increment signals the client to trigger a
    *  fade overlay. The client compares against its last seen value — if it changed, fade.
    *  Using an INT instead of a boolean so a set/sync/clear in one tick is never lost
    *  (SynchedEntityData sends the final value per tick, so a boolean would always arrive as false). */
   private static final EntityDataAccessor<Integer> TRACKED_FADE_SIGNAL = SynchedEntityData.defineId(SceneGirlEntity.class, EntityDataSerializers.INT);
   /** The yaw the scene was laid out with, so the camera frames it the same way every run. */
   private static final EntityDataAccessor<Float> TRACKED_SCENE_YAW = SynchedEntityData.defineId(SceneGirlEntity.class, EntityDataSerializers.FLOAT);
   /** Which of prone doggy's three hard variants is currently playing (1..3). */
   private static final EntityDataAccessor<Integer> TRACKED_HARD_VARIANT = SynchedEntityData.defineId(SceneGirlEntity.class, EntityDataSerializers.INT);
   private GirlSceneAction activeAction;
   private GirlScenePhase scenePhase = GirlScenePhase.IDLE;
   private int climaxTicks;
   private int sceneTicks;
   private int thrustTicks;
   /** Prone doggy cycles hard1..hard3 and never plays the same one twice in a row. */
   private int hardVariant = 1;
   private int stripTargetModel = 0;
   private GirlSceneAction queuedActionAfterStrip;
   private BlockPos sceneBedFootPos;
   private Direction sceneBedFacing;
   private double sceneTuneForward;
   private double sceneTuneSide;
   private double sceneTuneVertical;
   private boolean sceneFrozen;
   /** Incremented by server to signal an imminent snapTo transition; client triggers fade on change. */
   private int fadeSignalCounter;
   /** True once the fade signal has been fired during the current WAITING phase. */
   private boolean waitingFadeFired;
   private boolean spawnStateInitialized;
   private final Map<String, Integer> lastSoundVariants = new HashMap<>();
   private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
   private static final GirlAnimationRole[] ATTACK_ROLES = {
      GirlAnimationRole.ATTACK0, GirlAnimationRole.ATTACK1, GirlAnimationRole.ATTACK2
   };

   protected SceneGirlEntity(EntityType<? extends SceneGirlEntity> entityType, Level world, String displayName) {
      super(entityType, world);
      this.coreState().setGirlName(displayName);
      this.setCustomName(Component.literal(displayName));
      this.setCustomNameVisible(true);
   }

   /** Identifies the girl in resource paths and in the per-character tuning configs. */
   public abstract String characterId();

   /** Which animation file this girl reads from, and what she borrows from Jenny. */
   protected abstract GirlAnimationSet animations();

   /** Which voice folder she uses. */
   protected abstract GirlSoundSet sounds();

   /** Her own dialogue lines where they exist, Jenny's wording otherwise. */
   public String dialogueKey(String key) {
      String own = this.characterId() + ".dialogue." + key;
      return Language.getInstance().has(own) ? own : "jenny.dialogue." + key;
   }

   /** The base sound id for one voice family, so the client can speak in her voice too. */
   public String voice(String family, int variants) {
      return this.sounds().voice(family, variants).baseId();
   }

   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(TRACKED_ACTIVE_ACTION, "");
      builder.define(TRACKED_PAYMENT_ITEM, GirlPaymentItem.DIAMOND.name());
      builder.define(TRACKED_PAYMENT_AMOUNT, 0);
      builder.define(TRACKED_FOLLOW_UP, GirlFollowUpAction.NONE.name());
      builder.define(TRACKED_BUSY, false);
      builder.define(TRACKED_ACTIVE_PLAYER, Optional.empty());
      builder.define(TRACKED_SCENE_PHASE, GirlScenePhase.IDLE.id());
      builder.define(TRACKED_MODEL_VARIANT, 1);
      builder.define(TRACKED_RENDER_PLAYER, false);
      builder.define(TRACKED_THRUSTING, false);
      builder.define(TRACKED_INTRO_ACTIVE, false);
      builder.define(TRACKED_SCENE_TUNE_FORWARD, 0);
      builder.define(TRACKED_SCENE_TUNE_SIDE, 0);
      builder.define(TRACKED_SCENE_TUNE_VERTICAL, 0);
      builder.define(TRACKED_SCENE_FROZEN, false);
      builder.define(TRACKED_FADE_SIGNAL, 0);
      builder.define(TRACKED_SCENE_YAW, 0.0F);
      builder.define(TRACKED_HARD_VARIANT, 1);
   }

   public void beginInteraction(ServerPlayer player, GirlSceneAction action) {
      this.activeAction = action;
      this.scenePhase = GirlScenePhase.APPROACH;
      this.climaxTicks = 0;
      this.sceneTicks = 0;
      this.thrustTicks = 0;
      this.sceneFrozen = false;
      this.waitingFadeFired = false;
      this.queuedActionAfterStrip = null;
      this.loadSceneTune(action);
      this.coreState().setShouldGetDamage(false);
      this.stripTargetModel = action == GirlSceneAction.STRIP ? (this.coreState().currentModel() == 0 ? 1 : 0) : this.coreState().currentModel();
      GirlSceneStarter.beginOffer(this, player, action);
      // No fade signal here: APPROACH has no snapTo, the player should see Jenny walk up.
      // The first fade fires at PAYMENT tick (184 - FADE_LEAD_TICKS) before the real snap.
      this.applyActionAnimationFlags(action, this.scenePhase);
      this.syncTrackedState();
   }

   public boolean hasActiveScene() {
      return this.hasBuiltInScene() || this.hasCustomSceneLock();
   }

   public void setSceneBedAnchor(BlockPos footPos, Direction facing) {
      this.sceneBedFootPos = footPos;
      this.sceneBedFacing = facing;
   }

   public void clearSceneBedAnchor() {
      this.sceneBedFootPos = null;
      this.sceneBedFacing = null;
   }

   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      return InteractionResult.SUCCESS;
   }

   public Optional<GirlSceneAction> trackedActiveAction() {
      String s = (String)this.getEntityData().get(TRACKED_ACTIVE_ACTION);
      return s.isEmpty()
         ? Optional.empty()
         : Optional.of(GirlSceneAction.fromLegacyId(s).orElseThrow(() -> new IllegalStateException("Unknown tracked Jenny action: " + s)));
   }

   public GirlPaymentItem trackedPaymentItem() {
      return GirlPaymentItem.valueOf((String)this.getEntityData().get(TRACKED_PAYMENT_ITEM));
   }

   public int trackedPaymentAmount() {
      return (Integer)this.getEntityData().get(TRACKED_PAYMENT_AMOUNT);
   }

   public GirlFollowUpAction trackedFollowUpAction() {
      return GirlFollowUpAction.valueOf((String)this.getEntityData().get(TRACKED_FOLLOW_UP));
   }

   public boolean isBusyTracked() {
      return (Boolean)this.getEntityData().get(TRACKED_BUSY);
   }

   public Optional<UUID> trackedActivePlayer() {
      return (Optional<UUID>)this.getEntityData().get(TRACKED_ACTIVE_PLAYER);
   }

   public GirlScenePhase trackedScenePhase() {
      return GirlScenePhase.fromId((String)this.getEntityData().get(TRACKED_SCENE_PHASE));
   }

   /** Returns the current fade signal counter. The client triggers a fade whenever this value changes. */
   public int trackedFadeSignal() {
      return (Integer)this.getEntityData().get(TRACKED_FADE_SIGNAL);
   }

   public int trackedModelVariant() {
      return (Integer)this.getEntityData().get(TRACKED_MODEL_VARIANT);
   }

   public boolean shouldRenderPlayerTracked() {
      return (Boolean)this.getEntityData().get(TRACKED_RENDER_PLAYER);
   }

   public boolean isThrustingTracked() {
      return (Boolean)this.getEntityData().get(TRACKED_THRUSTING);
   }

   public boolean isIntroActiveTracked() {
      return (Boolean)this.getEntityData().get(TRACKED_INTRO_ACTIVE);
   }

   public double trackedSceneTuneForward() {
      return ((Integer)this.getEntityData().get(TRACKED_SCENE_TUNE_FORWARD)).intValue() / 100.0;
   }

   public double trackedSceneTuneSide() {
      return ((Integer)this.getEntityData().get(TRACKED_SCENE_TUNE_SIDE)).intValue() / 100.0;
   }

   public double trackedSceneTuneVertical() {
      return ((Integer)this.getEntityData().get(TRACKED_SCENE_TUNE_VERTICAL)).intValue() / 100.0;
   }

   public boolean isSceneFrozenTracked() {
      return (Boolean)this.getEntityData().get(TRACKED_SCENE_FROZEN);
   }

   /** Orientation the scene was laid out with; the camera frames against this, not the player's view. */
   public float trackedSceneYaw() {
      return (Float)this.getEntityData().get(TRACKED_SCENE_YAW);
   }

   @Override
   public void resetScene() {
      super.resetScene();
      this.setNoGravity(false);
      this.activeAction = null;
      this.scenePhase = GirlScenePhase.IDLE;
      this.climaxTicks = 0;
      this.sceneTicks = 0;
      this.thrustTicks = 0;
      this.sceneFrozen = false;
      this.queuedActionAfterStrip = null;
      this.clearSceneBedAnchor();
      this.sceneTuneForward = 0.0;
      this.sceneTuneSide = 0.0;
      this.sceneTuneVertical = 0.0;
      this.coreState().setShouldGetDamage(true);
      this.syncTrackedState();
   }

   public void die(DamageSource damageSource) {
      this.resetScene();
      super.die(damageSource);
   }

   public void remove(RemovalReason reason) {
      if (this.hasActiveScene()) {
         this.resetScene();
      }

      super.remove(reason);
   }

   @Override
   public void tick() {
      boolean builtInScene = this.hasBuiltInScene();
      boolean customScene = this.hasCustomSceneLock();
      boolean flag = builtInScene || customScene;
      // she walks to the bed on her own feet, so AI and navigation must survive that phase; the
      // APPROACH glide moves her by setPos and needs AI off but its position left alone
      boolean walking = builtInScene && this.isWalkingSceneParticipant() || customScene && this.isCustomWalkingParticipant();
      boolean gliding = builtInScene && this.scenePhase == GirlScenePhase.APPROACH;
      this.setNoAi(flag && !walking);
      super.tick();
      if (!this.spawnStateInitialized) {
         this.ensureNeutralSpawnState();
      }

      if (flag && !walking && !gliding) {
         this.getNavigation().stop();
         this.setDeltaMovement(0.0, 0.0, 0.0);
         this.setZza(0.0F);
         this.setXxa(0.0F);
      }

      // Scene phases switch gravity off to pin her on the bed, and vanilla persists NoGravity to NBT.
      // If the world saves mid-scene (or a scene ends by an unusual path) she reloads weightless, and
      // then never touches the ground - which silently kills navigation, since GroundPathNavigation
      // refuses to build any path unless onGround() is true.
      if (!flag && this.isNoGravity()) {
         this.setNoGravity(false);
      }

      // A scene always outranks combat or a pearl throw.
      if (flag && this.activity() != GirlActivity.NONE) {
         this.setActivity(GirlActivity.NONE);
      }

      if (!this.level().isClientSide()) {
         if (builtInScene) {
            this.tickServerScene();
         } else if (customScene) {
            this.tickCustomServer();
         }
      }
   }

   /** Phases in which Jenny moves under her own navigation instead of being pinned by the scene. */
   private boolean isWalkingSceneParticipant() {
      return this.scenePhase == GirlScenePhase.WALKING;
   }

   /**
    * Vanilla re-enables the goal control flags every 5 ticks, which lets the wander goal grab the
    * MOVE flag and clear the path the scene just set. The original removed those goals outright for
    * the duration of a scene ({@code tasks.removeTask(this.z)}).
    */
   @Override
   protected void updateControlFlags() {
      if (this.hasActiveScene()) {
         this.goalSelector.setControlFlag(Goal.Flag.MOVE, false);
         this.goalSelector.setControlFlag(Goal.Flag.JUMP, false);
         this.goalSelector.setControlFlag(Goal.Flag.LOOK, false);
      } else {
         super.updateControlFlags();
      }
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput output) {
      super.addAdditionalSaveData(output);
      output.putString("JennyTrackedActiveAction", this.activeAction == null ? "" : this.activeAction.legacyId());
      output.putString("JennyScenePhase", this.scenePhase.id());
      output.putBoolean("JennySceneFrozen", this.sceneFrozen);
      output.putString("JennyQueuedActionAfterStrip", this.queuedActionAfterStrip == null ? "" : this.queuedActionAfterStrip.legacyId());
      output.putInt("JennyStripTargetModel", this.stripTargetModel);
      output.putInt("JennyCurrentModel", this.coreState().currentModel());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      input.getString("JennyTrackedActiveAction")
         .ifPresent(s -> this.activeAction = s.isEmpty() ? null : GirlSceneAction.fromLegacyId(s).orElse(null));
      input.getString("JennyScenePhase").ifPresent(s -> this.scenePhase = GirlScenePhase.fromId(s));
      this.sceneFrozen = input.getBooleanOr("JennySceneFrozen", this.sceneFrozen);
      input.getString("JennyQueuedActionAfterStrip")
         .ifPresent(s -> this.queuedActionAfterStrip = s.isEmpty() ? null : GirlSceneAction.fromLegacyId(s).orElse(null));
      this.stripTargetModel = input.getIntOr("JennyStripTargetModel", this.stripTargetModel);
      input.getInt("JennyCurrentModel").ifPresent(value -> this.coreState().setCurrentModel(value));

      if (this.loadedWithPersistedSceneState()) {
         this.clearPersistedSceneState();
      } else if (this.activeAction == null && this.scenePhase == GirlScenePhase.IDLE && this.coreState().activePlayerId() == null) {
         this.coreState().setCurrentModel(1);
      }

      this.syncTrackedState();
   }

   public void applySceneInput(ServerPlayer player, boolean climaxRequest) {
      if (this.coreState().activePlayerId() == null || !this.coreState().activePlayerId().equals(player.getUUID())) {
         player.sendSystemMessage(Component.literal("You are not controlling this Jenny scene."));
      } else if (this.activeAction == null || this.scenePhase == GirlScenePhase.IDLE) {
         player.sendSystemMessage(Component.literal("Jenny is not in an active scene."));
      } else if (this.scenePhase == GirlScenePhase.POSITIONING) {
         player.sendSystemMessage(Component.literal("Jenny is still getting into position."));
      } else if (this.scenePhase == GirlScenePhase.PAYMENT) {
         player.sendSystemMessage(Component.literal("Jenny is still finishing the payment scene."));
      } else if (this.scenePhase == GirlScenePhase.INTRO && !this.introWaitsForThrust()) {
         player.sendSystemMessage(Component.literal("Jenny is still in the intro animation."));
      } else if (this.activeAction == GirlSceneAction.STRIP) {
         player.sendSystemMessage(Component.literal("Strip plays automatically."));
      } else if (climaxRequest) {
         // the client only sends this on a full arousal meter, matching the original's jump-to-finish
         this.scenePhase = GirlScenePhase.CLIMAX;
         this.climaxTicks = 30;
         this.coreState().setPlayerIsCumming(true);
         this.applyActionAnimationFlags(this.activeAction, GirlScenePhase.CLIMAX);
         this.syncTrackedState();
      } else if (this.scenePhase != GirlScenePhase.CLIMAX) {
         // prone doggy's intro waits for this very thrust, so it must not be gated behind the intro
         if (!this.isServerIntroActive() || this.introWaitsForThrust()) {
            // prone doggy rolls a fresh hard take on entry, then rides its own 34-tick budget back
            // down to soft (original PRONE_DOGGY_HARD(..., 34, PRONE_DOGGY_SOFT), Action.java:140).
            // Re-entering while it still runs would restart the take mid-beat and eat a thrust.
            if (this.introWaitsForThrust() && this.thrustTicks > 0) {
               return;
            }

            // Never the take that just played (original BiaEntity.ag(), :270-278). Only between
            // beats: rolling while one still runs would swap the animation mid-motion.
            if (this.scenePhase == GirlScenePhase.ACTIVE && this.thrustTicks <= 0) {
               this.rollHardVariant();
            }

            this.coreState().setPlayerIsThrusting(true);
            this.thrustTicks = Math.max(
               this.thrustTicks,
               this.scenePhase == GirlScenePhase.INTRO ? this.introAnimationTicks() : this.thrustDurationTicks()
            );
            if (this.activeAction == GirlSceneAction.STRIP) {
               this.coreState().setCurrentModel(0);
            }

            this.applyActionAnimationFlags(this.activeAction, this.scenePhase);
            this.syncTrackedState();
         }
      }
   }

   public void forceResetFromOwner(ServerPlayer player) {
      if (this.coreState().activePlayerId() != null && this.coreState().activePlayerId().equals(player.getUUID())) {
         this.resetScene();
         player.sendSystemMessage(Component.literal("Jenny scene reset."));
      } else {
         player.sendSystemMessage(Component.literal("You are not controlling this Jenny scene."));
      }
   }

   public void applySceneTuneInput(ServerPlayer player, String inputId) {
      if (this.coreState().activePlayerId() != null && this.coreState().activePlayerId().equals(player.getUUID())) {
         if (GirlSceneLayoutConfig.supportsForTuning(this.activeAction)) {
            double d0 = 0.05;
            switch (inputId) {
               case "scene_forward":
                  this.sceneTuneForward += d0;
                  break;
               case "scene_back":
                  this.sceneTuneForward -= d0;
                  break;
               case "scene_left":
                  this.sceneTuneSide -= d0;
                  break;
               case "scene_right":
                  this.sceneTuneSide += d0;
                  break;
               case "scene_up":
                  this.sceneTuneVertical += d0;
                  break;
               case "scene_down":
                  this.sceneTuneVertical -= d0;
                  break;
               default:
                  // the editor's typed values arrive as "scene_set:<axis>:<value>"
                  if (!this.applyTypedSceneTune(inputId)) {
                     return;
                  }
            }

            GirlSceneLayoutConfig.set(this.characterId(), this.activeAction, this.sceneTuneProfilePhase(), this.sceneTuneForward, this.sceneTuneSide, this.sceneTuneVertical);
            this.prepareSceneLayout(player, this.activeAction);
            this.alignSceneParticipants(player);
            this.syncTrackedState();
         }
      }
   }

   /** Parses the editor's {@code scene_set:<axis>:<value>}; false means "not one of ours". */
   private boolean applyTypedSceneTune(String inputId) {
      String[] parts = inputId.split(":");
      if (parts.length != 3 || !parts[0].equals("scene_set")) {
         return false;
      }

      double value;
      try {
         value = Double.parseDouble(parts[2]);
      } catch (NumberFormatException exception) {
         return false;
      }

      if (!Double.isFinite(value)) {
         return false;
      }

      switch (parts[1]) {
         case "forward" -> this.sceneTuneForward = value;
         case "side" -> this.sceneTuneSide = value;
         case "vertical" -> this.sceneTuneVertical = value;
         default -> {
            return false;
         }
      }

      return true;
   }

   public void toggleSceneFreeze(ServerPlayer player) {
      if (this.coreState().activePlayerId() != null && this.coreState().activePlayerId().equals(player.getUUID())) {
         if (this.activeAction != null && this.scenePhase != GirlScenePhase.IDLE) {
            this.sceneFrozen = !this.sceneFrozen;
            this.syncTrackedState();
            player.sendSystemMessage(Component.literal(this.sceneFrozen ? "Jenny scene frozen." : "Jenny scene resumed."));
         }
      }
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController<SceneGirlEntity>("eyes", 5, state -> state.setAndContinue(this.selectEyesAnimation()))
            .setSoundKeyframeHandler(this::handleSoundKeyframe)
      );
      controllers.add(
         new AnimationController<SceneGirlEntity>("movement", 5, state -> state.setAndContinue(this.selectMovementAnimation(state.isMoving())))
            .setSoundKeyframeHandler(this::handleSoundKeyframe)
      );
      controllers.add(
         new AnimationController<SceneGirlEntity>("combat", 3, state -> state.setAndContinue(this.selectCombatAnimation()))
            .setSoundKeyframeHandler(this::handleSoundKeyframe)
      );
      controllers.add(
         new AnimationController<SceneGirlEntity>("action", 2, state -> state.setAndContinue(this.selectActionAnimation()))
            .setSoundKeyframeHandler(this::handleSoundKeyframe)
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.animationCache;
   }

   private void tickServerScene() {
      if (this.coreState().activePlayerId() != null) {
         // she needs her legs while walking to the bed, and the APPROACH glide drives her position
         // directly, so neither may be zeroed here
         boolean selfMoving = this.isWalkingSceneParticipant() || this.scenePhase == GirlScenePhase.APPROACH;
         if (!selfMoving) {
            this.getNavigation().stop();
            this.setDeltaMovement(0.0, 0.0, 0.0);
         }
         ServerPlayer serverplayer = this.level() instanceof ServerLevel serverLevel
            ? serverLevel.getServer().getPlayerList().getPlayer(this.coreState().activePlayerId())
            : null;
         if (serverplayer == null || !serverplayer.isAlive()) {
            this.resetScene();
         } else if (this.sceneFrozen) {
            this.alignSceneParticipants(serverplayer);
            this.applyActionAnimationFlags(this.activeAction, this.scenePhase);
            this.syncTrackedState();
         } else if (this.scenePhase == GirlScenePhase.APPROACH) {
            this.sceneTicks++;
            float playerYaw = serverplayer.getYRot();
            Vec3 meetingPoint = serverplayer.position().add(horizontalForward(playerYaw));
            double dx = this.getX() - meetingPoint.x;
            double dz = this.getZ() - meetingPoint.z;
            double dist2 = dx * dx + dz * dz;
            // No fade during APPROACH — the player should see Jenny walk up.
            // First fade fires in PAYMENT at tick (184 - FADE_LEAD_TICKS).
            boolean arriving = dist2 <= 0.01 || this.sceneTicks > APPROACH_GLIDE_TICKS;
            if (arriving) {
               this.getNavigation().stop();
               this.setNoGravity(false);
               this.coreState().setTargetYaw(Mth.wrapDegrees(playerYaw + 180.0F));
               this.holdSceneYaw();
               // Fire fade signal for APPROACH → PAYMENT transition; no snapTo yet but the
               // signal must arrive early so the payment animation plays behind a black screen.
               this.fadeSignalCounter++;
               this.scenePhase = GirlScenePhase.PAYMENT;
               this.sceneTicks = 0;
            } else {
               int remaining = APPROACH_GLIDE_TICKS - this.sceneTicks;
               Vec3 step = remaining <= 0 ? meetingPoint : this.position().add(meetingPoint.subtract(this.position()).scale(1.0 / remaining));
               float travelYaw = (float)(Mth.atan2(meetingPoint.z - this.getZ(), meetingPoint.x - this.getX()) * 180.0 / Math.PI) - 90.0F;
               this.setYRot(travelYaw);
               this.setYBodyRot(travelYaw);
               this.setYHeadRot(travelYaw);
               this.setPos(step.x, step.y, step.z);
               this.setDeltaMovement(Vec3.ZERO);
            }

            this.applyActionAnimationFlags(this.activeAction, this.scenePhase);
            this.syncTrackedState();
         } else if (this.scenePhase == GirlScenePhase.PAYMENT) {
            this.sceneTicks++;
            // Fire fade exactly once at the lead tick before the transition
            if (this.sceneTicks == 184 - FADE_LEAD_TICKS) {
               this.fadeSignalCounter++;
               this.syncTrackedState();
            }
            if (this.sceneTicks >= 184) {
               this.sceneTicks = 0;
               this.scenePhase = this.nextScenePhaseAfterPayment();
               this.loadSceneTune(this.activeAction);
               if (this.scenePhase != GirlScenePhase.WALKING) {
                  this.prepareSceneLayout(serverplayer, this.activeAction);
               }
               this.sceneTicks = 0;
               this.applyActionAnimationFlags(this.activeAction, this.scenePhase);
               this.syncTrackedState();
            }
         } else if (this.scenePhase == GirlScenePhase.CLIMAX) {
            if (--this.climaxTicks <= 0) {
               serverplayer.sendSystemMessage(Component.literal("Jenny scene finished: " + this.activeAction.legacyId()));
               this.resetScene();
            } else {
               this.alignSceneParticipants(serverplayer);
               this.syncTrackedState();
            }
         } else if (this.scenePhase == GirlScenePhase.WAITING) {
            this.applyActionAnimationFlags(this.activeAction, this.scenePhase);
            this.holdSceneYaw();
            this.getNavigation().stop();
            if (this.sceneBedFootPos != null) {
               this.setDeltaMovement(Vec3.ZERO);
               this.setNoGravity(true);
            }
            Vec3 approachTarget = this.sceneBedFootPos != null ? this.requireSceneBedCenter() : this.position();
            double px = serverplayer.getX() - approachTarget.x;
            double pz = serverplayer.getZ() - approachTarget.z;
            // Fire fade once when player enters ~3-block range, not every tick
            double playerDist2 = px * px + pz * pz;
            if (!this.waitingFadeFired && playerDist2 <= 9.0) {
               this.waitingFadeFired = true;
               this.fadeSignalCounter++;
               this.syncTrackedState();
            }
            if (playerDist2 <= 4.0) {
               this.scenePhase = this.requiresIntroPhase() ? GirlScenePhase.INTRO : GirlScenePhase.ACTIVE;
               this.sceneTicks = 0;
               this.loadSceneTune(this.activeAction);
               this.prepareSceneLayout(serverplayer, this.activeAction);
               this.alignSceneParticipants(serverplayer);
               this.applyActionAnimationFlags(this.activeAction, this.scenePhase);
            }

            this.syncTrackedState();
         } else if (this.scenePhase == GirlScenePhase.WALKING) {
            // Jenny walks to the bed on her own feet; the lying-down animation only starts once she
            // is there, otherwise it gets cut off mid-way and she snaps back to the default pose
            this.sceneTicks++;
            Vec3 bedTarget = this.requireSceneBedCenter();
            // horizontal only: the bed centre sits above floor level, so a 3D check never matches
            double dx = this.getX() - bedTarget.x;
            double dz = this.getZ() - bedTarget.z;
            if (dx * dx + dz * dz <= 1.0 || this.sceneTicks > 200) {
               this.getNavigation().stop();
               this.scenePhase = GirlScenePhase.POSITIONING;
               this.sceneTicks = 0;
               this.loadSceneTune(this.activeAction);
               this.prepareSceneLayout(serverplayer, this.activeAction);
            } else {
               this.getLookControl().setLookAt(bedTarget.x, this.getEyeY(), bedTarget.z);
               // the original re-issues the path on a fixed schedule instead of polling isDone(),
               // because a path that fails to build leaves the navigation "done" every tick
               if (this.sceneTicks == 1 || this.sceneTicks == 60 || this.sceneTicks == 120) {
                  this.getNavigation().stop();
                  this.getNavigation().moveTo(bedTarget.x, bedTarget.y, bedTarget.z, 0.35);
               }
            }

            this.applyActionAnimationFlags(this.activeAction, this.scenePhase);
            this.syncTrackedState();
         } else if (this.scenePhase == GirlScenePhase.POSITIONING && this.waitsForPlayerApproach()) {
            // she is on the bed now: play doggygoonbed to the end, then hold the pose and wait
            this.sceneTicks++;
            this.holdSceneYaw();
            this.setDeltaMovement(Vec3.ZERO);
            this.setNoGravity(true);
            int posTotal = this.requiredPositioningTicks();
            if (this.sceneTicks == posTotal - FADE_LEAD_TICKS && posTotal > FADE_LEAD_TICKS) {
               this.fadeSignalCounter++;
               this.syncTrackedState();
            }
            if (this.sceneTicks >= posTotal) {
               this.scenePhase = this.nextScenePhaseAfterPositioning();
               this.sceneTicks = 0;
               this.loadSceneTune(this.activeAction);
               this.prepareSceneLayout(serverplayer, this.activeAction);
            }

            this.applyActionAnimationFlags(this.activeAction, this.scenePhase);
            this.syncTrackedState();
         } else {
            this.alignSceneParticipants(serverplayer);
            if (this.scenePhase == GirlScenePhase.POSITIONING) {
               this.sceneTicks++;
               int posTotal = this.requiredPositioningTicks();
               if (this.sceneTicks == posTotal - FADE_LEAD_TICKS && posTotal > FADE_LEAD_TICKS) {
                  this.fadeSignalCounter++;
                  this.syncTrackedState();
               }
               if (this.sceneTicks >= posTotal) {
                  this.scenePhase = this.nextScenePhaseAfterPositioning();
                  this.sceneTicks = 0;
                  this.loadSceneTune(this.activeAction);
                  this.prepareSceneLayout(serverplayer, this.activeAction);
                  this.alignSceneParticipants(serverplayer);
                  this.applyActionAnimationFlags(this.activeAction, this.scenePhase);
                  this.syncTrackedState();
               }
            } else if (this.scenePhase == GirlScenePhase.INTRO) {
               if (this.introWaitsForThrust() && this.thrustTicks <= 0) {
                  this.holdSceneYaw();
                  this.syncTrackedState();
                  return;
               }

               this.sceneTicks++;
               int introTotal = this.introAnimationTicks();
               if (this.sceneTicks == introTotal - FADE_LEAD_TICKS && introTotal > FADE_LEAD_TICKS) {
                  this.fadeSignalCounter++;
                  this.syncTrackedState();
               }
               if (this.sceneTicks >= introTotal) {
                  this.scenePhase = GirlScenePhase.ACTIVE;
                  this.sceneTicks = 0;
                  this.thrustTicks = 0;
                  this.coreState().setPlayerIsThrusting(false);
                  this.loadSceneTune(this.activeAction);
                  this.prepareSceneLayout(serverplayer, this.activeAction);
                  this.alignSceneParticipants(serverplayer);
                  this.applyActionAnimationFlags(this.activeAction, this.scenePhase);
                  this.syncTrackedState();
               }
            } else if (this.activeAction == GirlSceneAction.STRIP) {
               this.sceneTicks++;
               if (this.sceneTicks >= 96 && this.coreState().currentModel() != this.stripTargetModel) {
                  this.coreState().setCurrentModel(this.stripTargetModel);
                  this.syncTrackedState();
               }

               if (this.sceneTicks >= 100) {
                  if (this.queuedActionAfterStrip != null) {
                     this.activeAction = this.queuedActionAfterStrip;
                     this.queuedActionAfterStrip = null;
                     this.scenePhase = GirlScenePhase.POSITIONING;
                     this.sceneTicks = 0;
                     this.loadSceneTune(this.activeAction);
                     this.prepareSceneLayout(serverplayer, this.activeAction);
                     this.applyActionAnimationFlags(this.activeAction, this.scenePhase);
                     this.syncTrackedState();
                  } else {
                     serverplayer.sendSystemMessage(Component.literal("Jenny scene finished: " + this.activeAction.legacyId()));
                     this.resetScene();
                  }
               }
            } else {
               if (this.thrustTicks > 0) {
                  this.thrustTicks--;
               }

               if (this.coreState().playerIsThrusting() && this.thrustTicks <= 0) {
                  this.coreState().setPlayerIsThrusting(false);
               }

               if (this.scenePhase == GirlScenePhase.ACTIVE) {
                  this.applyActionAnimationFlags(this.activeAction, this.scenePhase);
                  this.syncTrackedState();
               }
            }
         }
         // no cleanup needed here — fadeSignalCounter is monotonic, waitingFadeFired reset at scene start
      }
   }


   private int introAnimationTicks() {
      return switch (this.activeAction) {
         case BLOWJOB -> 986;
         case KOBOLD_BLOWJOB -> 226;
         case DEEPTHROAT -> 64;
         case FACEFUCK -> 194;
         case BOOBJOB -> 173;
         case TOUCH_BOOBS -> 130;
         case DOGGY -> 196;
         // the intro loop itself is open-ended; this budget covers the insert take that the first
         // thrust starts (original PRONE_DOGGY_INSERT(..., 42, PRONE_DOGGY_SOFT))
         case PRONE_DOGGY -> 42;
         // her alternate take shares the shape: the intro loops, the insert take is 44 ticks
         case SITTING_ALT -> 44;
         case ANAL -> 58;
         case ANAL_ALT -> 57;
         case KOBOLD_ANAL -> 43;
         case SITTING -> 72;
         case MISSIONARY -> 72;
         case MATING_PRESS -> 54;
         case REVERSE_COWGIRL -> 89;
         case COWGIRL -> 320;
         case STRIP -> 0;
      };
   }

   private boolean requiresIntroPhase() {
      return switch (this.activeAction) {
         case BLOWJOB, KOBOLD_BLOWJOB, DEEPTHROAT, FACEFUCK, BOOBJOB, TOUCH_BOOBS, DOGGY, PRONE_DOGGY, ANAL, ANAL_ALT, KOBOLD_ANAL, SITTING, SITTING_ALT, MISSIONARY, MATING_PRESS, REVERSE_COWGIRL, COWGIRL -> true;
         case STRIP -> false;
      };
   }

   /**
    * How long one beat runs. The take has to play out: cutting it short drops her back to the slow
    * loop mid-motion, which is what made most scenes look torn (cowgirl's take is 68 ticks against
    * the 16 that used to be assumed here). The length comes from the animation itself so every
    * character times to her own take rather than to Jenny's.
    */
   private int thrustDurationTicks() {
      GirlAnimationRole role = GirlSceneAnimations.role(this.activeAction, this.scenePhase, true, this.hardVariant);
      return GirlAnimationLengths.ticks(this.animations().namespace(), role, this.fallbackThrustDurationTicks());
   }

   /** Used only when neither she nor Jenny ships the take; keeps the pre-measurement behaviour. */
   private int fallbackThrustDurationTicks() {
      return switch (this.activeAction) {
         case BLOWJOB -> 18;
         case KOBOLD_BLOWJOB -> 16;
         case DEEPTHROAT -> 16;
         case FACEFUCK -> 16;
         case BOOBJOB -> 16;
         case TOUCH_BOOBS -> 16;
         case DOGGY -> 16;
         case PRONE_DOGGY -> 34;
         case SITTING_ALT -> 36;
         case ANAL -> 16;
         case ANAL_ALT -> 18;
         case KOBOLD_ANAL -> 16;
         case SITTING -> 16;
         case MISSIONARY -> 16;
         case MATING_PRESS -> 30;
         case REVERSE_COWGIRL -> 16;
         case COWGIRL -> 16;
         case STRIP -> 0;
      };
   }

   private GirlScenePhase nextScenePhaseAfterPayment() {
      if (this.shouldAutoStripBeforeTouchBoobs()) {
         this.queuedActionAfterStrip = GirlSceneAction.TOUCH_BOOBS;
         this.stripTargetModel = 0;
         this.activeAction = GirlSceneAction.STRIP;
         return GirlScenePhase.ACTIVE;
      } else if (this.activeAction == GirlSceneAction.STRIP) {
         return GirlScenePhase.ACTIVE;
      } else if (this.requiredPositioningTicks() <= 0) {
         return this.requiresIntroPhase() ? GirlScenePhase.INTRO : GirlScenePhase.ACTIVE;
      } else if (this.waitsForPlayerApproach()) {
         // walk over first, then lie down: doggygoonbed must not play while she is still moving
         return GirlScenePhase.WALKING;
      } else {
         return GirlScenePhase.POSITIONING;
      }
   }

   private GirlScenePhase nextScenePhaseAfterPositioning() {      if (this.waitsForPlayerApproach()) {
         return GirlScenePhase.WAITING;
      }

      return this.requiresIntroPhase() ? GirlScenePhase.INTRO : GirlScenePhase.ACTIVE;
   }

   /**
    * Picks a new take for the beat about to play, never the one that just played (original
    * BiaEntity.ag()). Scenes ship a different number of takes — three for prone doggy, four for
    * facefuck — so the count comes from the table rather than a constant.
    */
   private void rollHardVariant() {
      int takes = GirlSceneAnimations.variantCount(this.activeAction);
      if (takes < 2) {
         this.hardVariant = 1;
         return;
      }

      int previous = this.hardVariant;
      int next = previous;
      while (next == previous) {
         next = this.random.nextInt(takes) + 1;
      }

      this.hardVariant = next;
   }

   /** Client reads the synced variant; the server owns the roll. */
   private int resolvedHardVariant() {
      return this.level().isClientSide() ? this.getEntityData().get(TRACKED_HARD_VARIANT) : this.hardVariant;
   }

   /**
    * Bed scenes let Jenny climb on first and then hold, so the player closes the distance himself
    * instead of being teleported (original: STARTDOGGY -> WAITDOGGY -> DOGGYSTART).
    *
    * <p>Only scenes that actually ship a holding animation can do this: doggy and prone doggy have
    * their own, anal reuses {@code anal_wait}. The rest would freeze on a pose they do not own.
    */
   private static boolean waitsForPlayerApproach(GirlSceneAction action) {
      return action == GirlSceneAction.DOGGY
         || action == GirlSceneAction.PRONE_DOGGY
         || action == GirlSceneAction.SITTING_ALT
         || action == GirlSceneAction.ANAL
         || action == GirlSceneAction.ANAL_ALT;
   }

   /**
    * Scenes whose intro loops until the player thrusts, rather than running on a clock. The thrust
    * both ends the wait and starts the insert take, so the intro budget only begins counting then.
    */
   private boolean introWaitsForThrust() {
      return this.activeAction == GirlSceneAction.PRONE_DOGGY || this.activeAction == GirlSceneAction.SITTING_ALT;
   }

   private boolean waitsForPlayerApproach() {
      return waitsForPlayerApproach(this.activeAction);
   }

   /** Client-side counterpart of {@link #waitsForPlayerApproach()}, driven by the tracked action. */
   public boolean waitsForPlayerApproachTracked() {
      return this.trackedActiveAction().filter(SceneGirlEntity::waitsForPlayerApproach).isPresent();
   }

   private boolean shouldAutoStripBeforeTouchBoobs() {
      return this.activeAction == GirlSceneAction.TOUCH_BOOBS && this.coreState().currentModel() != 0;
   }

   private void applyActionAnimationFlags(GirlSceneAction action, GirlScenePhase phase) {
      this.coreState().animationState().set(GirlAnimationParameter.IS_DOING_ACTION, phase != GirlScenePhase.IDLE);
      this.coreState().animationState().set(GirlAnimationParameter.MOVEMENT_ACTIVE, phase == GirlScenePhase.IDLE);
      this.coreState().animationState().set(GirlAnimationParameter.START_PAYMENT, phase == GirlScenePhase.PAYMENT);
      this.coreState().animationState().set(GirlAnimationParameter.START_STRIP, false);
      this.coreState().animationState().set(GirlAnimationParameter.START_BLOWJOB, false);
      this.coreState().animationState().set(GirlAnimationParameter.SUCK_BLOWJOB, false);
      this.coreState().animationState().set(GirlAnimationParameter.CUM_BLOWJOB, false);
      this.coreState().animationState().set(GirlAnimationParameter.THRUST_BLOWJOB, false);
      this.coreState().animationState().set(GirlAnimationParameter.START_BOOBJOB, false);
      this.coreState().animationState().set(GirlAnimationParameter.SLOW_BOOBJOB, false);
      this.coreState().animationState().set(GirlAnimationParameter.FAST_BOOBJOB, false);
      this.coreState().animationState().set(GirlAnimationParameter.CUM_BOOBJOB, false);
      this.coreState().animationState().set(GirlAnimationParameter.START_DOGGY, false);
      this.coreState().animationState().set(GirlAnimationParameter.WAIT_DOGGY, false);
      this.coreState().animationState().set(GirlAnimationParameter.DOGGY_START, false);
      this.coreState().animationState().set(GirlAnimationParameter.DOGGY_SLOW, false);
      this.coreState().animationState().set(GirlAnimationParameter.DOGGY_FAST, false);
      this.coreState().animationState().set(GirlAnimationParameter.DOGGY_CUM, false);
      this.coreState().animationState().set(GirlAnimationParameter.PRONE_DOGGY_INTRO, false);
      this.coreState().animationState().set(GirlAnimationParameter.PRONE_DOGGY_SOFT, false);
      this.coreState().animationState().set(GirlAnimationParameter.PRONE_DOGGY_HARD, false);
      this.coreState().animationState().set(GirlAnimationParameter.PRONE_DOGGY_CUM, false);
      switch (action) {
         case BLOWJOB:
            this.coreState().animationState().set(GirlAnimationParameter.PLAYER_SHOULD_BE_RENDERED, true);
            this.coreState().animationState().set(GirlAnimationParameter.START_BLOWJOB, phase == GirlScenePhase.INTRO);
            this.coreState()
               .animationState()
               .set(GirlAnimationParameter.SUCK_BLOWJOB, (phase == GirlScenePhase.ACTIVE) && this.thrustTicks <= 0);
            this.coreState().animationState().set(GirlAnimationParameter.CUM_BLOWJOB, phase == GirlScenePhase.CLIMAX);
            this.coreState()
               .animationState()
               .set(
                  GirlAnimationParameter.THRUST_BLOWJOB, (phase == GirlScenePhase.ACTIVE) && this.thrustTicks > 0
               );
            break;
         case KOBOLD_BLOWJOB:
            this.coreState().animationState().set(GirlAnimationParameter.PLAYER_SHOULD_BE_RENDERED, true);
            this.coreState().animationState().set(GirlAnimationParameter.START_BLOWJOB, phase == GirlScenePhase.INTRO);
            this.coreState()
               .animationState()
               .set(GirlAnimationParameter.SUCK_BLOWJOB, (phase == GirlScenePhase.ACTIVE) && this.thrustTicks <= 0);
            this.coreState().animationState().set(GirlAnimationParameter.CUM_BLOWJOB, phase == GirlScenePhase.CLIMAX);
            this.coreState()
               .animationState()
               .set(
                  GirlAnimationParameter.THRUST_BLOWJOB, (phase == GirlScenePhase.ACTIVE) && this.thrustTicks > 0
               );
            break;
         case DEEPTHROAT:
            this.coreState().animationState().set(GirlAnimationParameter.PLAYER_SHOULD_BE_RENDERED, true);
            this.coreState().animationState().set(GirlAnimationParameter.START_BLOWJOB, phase == GirlScenePhase.INTRO);
            this.coreState()
               .animationState()
               .set(GirlAnimationParameter.SUCK_BLOWJOB, (phase == GirlScenePhase.ACTIVE) && this.thrustTicks <= 0);
            this.coreState().animationState().set(GirlAnimationParameter.CUM_BLOWJOB, phase == GirlScenePhase.CLIMAX);
            this.coreState()
               .animationState()
               .set(
                  GirlAnimationParameter.THRUST_BLOWJOB, (phase == GirlScenePhase.ACTIVE) && this.thrustTicks > 0
               );
            break;
         case FACEFUCK:
            this.coreState().animationState().set(GirlAnimationParameter.PLAYER_SHOULD_BE_RENDERED, true);
            this.coreState().animationState().set(GirlAnimationParameter.START_BLOWJOB, phase == GirlScenePhase.INTRO);
            this.coreState()
               .animationState()
               .set(GirlAnimationParameter.SUCK_BLOWJOB, (phase == GirlScenePhase.ACTIVE) && this.thrustTicks <= 0);
            this.coreState().animationState().set(GirlAnimationParameter.CUM_BLOWJOB, phase == GirlScenePhase.CLIMAX);
            this.coreState()
               .animationState()
               .set(
                  GirlAnimationParameter.THRUST_BLOWJOB, (phase == GirlScenePhase.ACTIVE) && this.thrustTicks > 0
               );
            break;
         case BOOBJOB:
         case TOUCH_BOOBS:
            this.coreState().animationState().set(GirlAnimationParameter.PLAYER_SHOULD_BE_RENDERED, true);
            this.coreState().animationState().set(GirlAnimationParameter.START_BOOBJOB, phase == GirlScenePhase.INTRO);
            this.coreState()
               .animationState()
               .set(GirlAnimationParameter.SLOW_BOOBJOB, (phase == GirlScenePhase.ACTIVE) && this.thrustTicks <= 0);
            this.coreState()
               .animationState()
               .set(GirlAnimationParameter.FAST_BOOBJOB, (phase == GirlScenePhase.ACTIVE) && this.thrustTicks > 0);
            this.coreState().animationState().set(GirlAnimationParameter.CUM_BOOBJOB, phase == GirlScenePhase.CLIMAX);
            break;
         case DOGGY:
         case ANAL:
         case ANAL_ALT:
         case SITTING:
         case MISSIONARY:
         case MATING_PRESS:
         case REVERSE_COWGIRL:
         case COWGIRL:
            // original Action.hasPlayer: while Jenny walks over, heads to the bed or waits there is
            // no partner in the animation yet, so the "steve" bone must stay hidden (GirlModel.java:199)
            this.coreState()
               .animationState()
               .set(
                  GirlAnimationParameter.PLAYER_SHOULD_BE_RENDERED,
                  phase != GirlScenePhase.APPROACH
                     && phase != GirlScenePhase.WALKING
                     && phase != GirlScenePhase.POSITIONING
                     && phase != GirlScenePhase.WAITING
               );
            this.coreState().animationState().set(GirlAnimationParameter.START_DOGGY, phase == GirlScenePhase.POSITIONING);
            this.coreState()
               .animationState()
               .set(GirlAnimationParameter.DOGGY_SLOW, (phase == GirlScenePhase.ACTIVE) && this.thrustTicks <= 0);
            this.coreState().animationState().set(GirlAnimationParameter.DOGGY_START, phase == GirlScenePhase.INTRO);
            this.coreState()
               .animationState()
               .set(GirlAnimationParameter.DOGGY_FAST, (phase == GirlScenePhase.ACTIVE) && this.thrustTicks > 0);
            this.coreState().animationState().set(GirlAnimationParameter.DOGGY_CUM, phase == GirlScenePhase.CLIMAX);
            this.coreState().animationState().set(GirlAnimationParameter.WAIT_DOGGY, phase == GirlScenePhase.WAITING);
            break;
         case PRONE_DOGGY:
         case SITTING_ALT:
            // she borrows anal's prepare/wait takes, then runs her own INTRO -> SOFT <-> HARD -> CUM
            // chain (original: anal_prepare -> ANAL_WAIT -> PRONE_DOGGY_INTRO, BiaEntity:244-262)
            this.coreState()
               .animationState()
               .set(
                  GirlAnimationParameter.PLAYER_SHOULD_BE_RENDERED,
                  phase != GirlScenePhase.APPROACH
                     && phase != GirlScenePhase.WALKING
                     && phase != GirlScenePhase.POSITIONING
                     && phase != GirlScenePhase.WAITING
               );
            this.coreState().animationState().set(GirlAnimationParameter.PRONE_DOGGY_INTRO, phase == GirlScenePhase.INTRO);
            this.coreState()
               .animationState()
               .set(GirlAnimationParameter.PRONE_DOGGY_SOFT, (phase == GirlScenePhase.ACTIVE) && this.thrustTicks <= 0);
            this.coreState()
               .animationState()
               .set(GirlAnimationParameter.PRONE_DOGGY_HARD, (phase == GirlScenePhase.ACTIVE) && this.thrustTicks > 0);
            this.coreState().animationState().set(GirlAnimationParameter.PRONE_DOGGY_CUM, phase == GirlScenePhase.CLIMAX);
            break;
         case KOBOLD_ANAL:
            this.coreState().animationState().set(GirlAnimationParameter.PLAYER_SHOULD_BE_RENDERED, true);
            this.coreState().animationState().set(GirlAnimationParameter.START_DOGGY, false);
            this.coreState().animationState().set(GirlAnimationParameter.DOGGY_START, phase == GirlScenePhase.INTRO);
            this.coreState()
               .animationState()
               .set(GirlAnimationParameter.DOGGY_SLOW, (phase == GirlScenePhase.ACTIVE) && this.thrustTicks <= 0);
            this.coreState()
               .animationState()
               .set(GirlAnimationParameter.DOGGY_FAST, (phase == GirlScenePhase.ACTIVE) && this.thrustTicks > 0);
            this.coreState().animationState().set(GirlAnimationParameter.DOGGY_CUM, phase == GirlScenePhase.CLIMAX);
            this.coreState().animationState().set(GirlAnimationParameter.WAIT_DOGGY, false);
            break;
         case STRIP:
            this.coreState().animationState().set(GirlAnimationParameter.START_STRIP, phase == GirlScenePhase.ACTIVE);
            this.coreState().animationState().set(GirlAnimationParameter.PLAYER_SHOULD_BE_RENDERED, phase == GirlScenePhase.PAYMENT);
      }

      // original Action.hasPlayer (GirlModel.java:199): the partner only exists once the scene proper
      // is running. Before that she is still walking over or being paid, so the "steve" bone must stay
      // hidden regardless of which action was picked - the cases above hardcode true for the non-bed ones.
      if (phase == GirlScenePhase.APPROACH || phase == GirlScenePhase.WALKING || phase == GirlScenePhase.WAITING) {
         this.coreState().animationState().set(GirlAnimationParameter.PLAYER_SHOULD_BE_RENDERED, false);
      }
   }

   /** Every animation goes through here, so a character's own file is preferred over Jenny's. */
   private RawAnimation animation(GirlAnimationRole role) {
      return this.animations().get(role);
   }

   protected RawAnimation selectNamedAnimation(String animationName, boolean looping) {
      String owner = this.animations().namespace();
      String id = "animation." + owner + "." + animationName;
      return looping ? RawAnimation.begin().thenLoop(id) : RawAnimation.begin().thenPlay(id);
   }

   protected RawAnimation selectCustomMovementAnimation(boolean moving) {
      return null;
   }

   protected RawAnimation selectCustomActionAnimation() {
      return null;
   }

   protected boolean handleCustomSoundKeyframe(String keyframe) {
      return false;
   }

   protected boolean hasCustomSceneLock() {
      return false;
   }

   protected boolean isCustomWalkingParticipant() {
      return false;
   }

   protected void tickCustomServer() {
   }

   /**
    * For custom (non-scene) interactions that still need the partner visible.
    * Writes both the animation-state flag and the synced data accessor directly,
    * since {@code syncTrackedState()} is reserved for the main scene tick.
    */
   protected void setRenderPlayer(boolean visible) {
      this.coreState().animationState().set(GirlAnimationParameter.PLAYER_SHOULD_BE_RENDERED, visible);
      this.getEntityData().set(TRACKED_RENDER_PLAYER, visible);
   }

   public boolean tryBeginCustomInteraction(ServerPlayer player, String actionId) {
      return false;
   }

   protected List<InteractionSection> buildInteractionSections() {
      List<InteractionSection> sections = new ArrayList<>();
      List<InteractionOption> allOptions = this.optionsFor(this.allSceneActions());
      if (!allOptions.isEmpty()) {
         sections.add(this.section("action.section.all", ItemStack.EMPTY, allOptions));
      }

      sections.addAll(this.extraInteractionSections());
      Set<GirlSceneAction> personalActions = this.sceneActions();
      this.addSceneSection(sections, "action.section.oral", personalActions, GirlSceneAction.BLOWJOB, GirlSceneAction.DEEPTHROAT, GirlSceneAction.FACEFUCK, GirlSceneAction.KOBOLD_BLOWJOB);
      this.addSceneSection(sections, "action.section.foreplay", personalActions, GirlSceneAction.BOOBJOB, GirlSceneAction.TOUCH_BOOBS);
      this.addSceneSection(
         sections,
         "action.section.sex",
         personalActions,
         GirlSceneAction.DOGGY,
         GirlSceneAction.PRONE_DOGGY,
         GirlSceneAction.MISSIONARY,
         GirlSceneAction.COWGIRL,
         GirlSceneAction.REVERSE_COWGIRL,
         GirlSceneAction.SITTING,
         GirlSceneAction.SITTING_ALT,
         GirlSceneAction.MATING_PRESS,
         GirlSceneAction.ANAL,
         GirlSceneAction.ANAL_ALT,
         GirlSceneAction.KOBOLD_ANAL
      );
      this.addSceneSection(sections, "action.section.tease", personalActions, GirlSceneAction.STRIP);
      return sections;
   }

   protected Set<GirlSceneAction> sceneActions() {
      return this.allSceneActions();
   }

   protected List<InteractionSection> extraInteractionSections() {
      return List.of();
   }

   protected Set<GirlSceneAction> allSceneActions() {
      return EnumSet.allOf(GirlSceneAction.class);
   }

   private void addSceneSection(List<InteractionSection> sections, String labelKey, Set<GirlSceneAction> enabledActions, GirlSceneAction... actions) {
      List<InteractionOption> options = new ArrayList<>();

      for (GirlSceneAction action : actions) {
         if (action.enabled() && enabledActions.contains(action)) {
            options.add(this.option(action, this.labelKey(action)));
         }
      }

      if (!options.isEmpty()) {
         sections.add(this.section(labelKey, options.get(0).price(), options));
      }
   }

   private List<InteractionOption> optionsFor(Set<GirlSceneAction> actions) {
      List<InteractionOption> options = new ArrayList<>();

      for (GirlSceneAction action : GirlSceneAction.values()) {
         if (action.enabled() && actions.contains(action)) {
            options.add(this.option(action, this.labelKey(action)));
         }
      }

      return options;
   }

   private String labelKey(GirlSceneAction action) {
      return switch (action) {
         case BLOWJOB -> "action.names.blowjob";
         case KOBOLD_BLOWJOB -> "action.names.kobold_blowjob";
         case DEEPTHROAT -> "action.names.deepthroat";
         case FACEFUCK -> "action.names.facefuck";
         case BOOBJOB -> "action.names.boobjob";
         case TOUCH_BOOBS -> "action.names.touchboobs";
         case DOGGY -> "action.names.doggy";
         case PRONE_DOGGY -> "action.names.prone_doggy";
         case SITTING_ALT -> "action.names.sitting_alt";
         case ANAL -> "action.names.anal";
         case ANAL_ALT -> "action.names.anal_alt";
         case KOBOLD_ANAL -> "action.names.kobold_anal";
         case SITTING -> "action.names.sitting";
         case MISSIONARY -> "action.names.missionary";
         case MATING_PRESS -> "action.names.mating_press";
         case REVERSE_COWGIRL -> "action.names.reverse_cowgirl";
         case COWGIRL -> "action.names.cowgirl";
         case STRIP -> "action.names.strip";
      };
   }

   public final List<InteractionSection> interactionSections() {
      return this.buildInteractionSections();
   }

   public final Optional<InteractionOption> interactionOption(String actionId) {
      for (InteractionSection section : this.interactionSections()) {
         for (InteractionOption option : section.options()) {
            if (option.actionId().equals(actionId)) {
               return Optional.of(option);
            }
         }
      }

      return Optional.empty();
   }

   protected final InteractionOption option(GirlSceneAction action, String labelKey) {
      return new InteractionOption(action.legacyId(), labelKey, this.priceFor(action));
   }

   /**
    * Override to substitute a character-specific price for a given scene action.
    * Default returns the action's global payment item and amount.
    */
   protected ItemStack priceFor(GirlSceneAction action) {
      return new ItemStack(this.paymentItemFor(action).item(), this.paymentAmountFor(action));
   }

   public GirlPaymentItem paymentItemFor(GirlSceneAction action) {
      return action.paymentItem();
   }

   public int paymentAmountFor(GirlSceneAction action) {
      return action.paymentAmount();
   }

   protected final InteractionOption option(String actionId, String labelKey, ItemStack price) {
      return new InteractionOption(actionId, labelKey, price.copy());
   }

   protected final InteractionSection section(String labelKey, GirlSceneAction previewAction, List<InteractionOption> options) {
      return new InteractionSection(labelKey, this.priceFor(previewAction), List.copyOf(options));
   }

   protected final InteractionSection section(String labelKey, ItemStack previewPrice, List<InteractionOption> options) {
      return new InteractionSection(labelKey, previewPrice.copy(), List.copyOf(options));
   }

   private RawAnimation selectMovementAnimation(boolean moving) {
      RawAnimation custom = this.selectCustomMovementAnimation(moving);
      if (custom != null) {
         return custom;
      }

      if (this.resolvedScenePhase() == GirlScenePhase.IDLE
         && this.resolvedActiveAction() == null
         && this.allowsWalkAnimation()
         && this.coreState().animationState().get(GirlAnimationParameter.MOVEMENT_ACTIVE)) {
         return this.animation(moving ? GirlAnimationRole.WALK : GirlAnimationRole.IDLE);
      } else {
         return this.animation(GirlAnimationRole.NULL);
      }
   }

   /** Downed and pearl-throwing are whole-body poses; letting the legs keep walking looks broken. */
   private boolean allowsWalkAnimation() {
      GirlActivity activity = this.resolvedActivity();
      return activity != GirlActivity.DOWNED && activity != GirlActivity.PEARL;
   }

   private RawAnimation selectCombatAnimation() {
      if (this.resolvedScenePhase() != GirlScenePhase.IDLE || this.resolvedActiveAction() != null) {
         return this.animation(GirlAnimationRole.NULL);
      }

      return switch (this.resolvedActivity()) {
         case COMBAT_MELEE -> this.animation(ATTACK_ROLES[Math.floorMod(this.attackVariant(), ATTACK_ROLES.length)]);
         case COMBAT_BOW -> this.animation(GirlAnimationRole.BOW);
         case PEARL -> this.animation(GirlAnimationRole.THROW_PEARL);
         case DOWNED -> this.animation(GirlAnimationRole.DOWNED);
         case NONE -> this.animation(GirlAnimationRole.NULL);
      };
   }

   private RawAnimation selectEyesAnimation() {
      return this.animation(this.coreState().animationState().get(GirlAnimationParameter.AUTO_EYES) ? GirlAnimationRole.HAPPY_EYES : GirlAnimationRole.NULL);
   }

   private RawAnimation selectKoboldBlowjobLoopAnimation(boolean thrusting) {
      if (thrusting) {
         return this.animation(GirlAnimationRole.KOBOLD_BLOWJOB_FAST);
      } else {
         int i = Math.floorMod(this.sceneTicks, 200);
         if (i < 50) {
            return this.animation(GirlAnimationRole.KOBOLD_BLOWJOB_SLOW);
         } else if (i < 100) {
            return this.animation(GirlAnimationRole.KOBOLD_BLOWJOB_SWITCH_TO_R);
         } else {
            return this.animation(i < 150 ? GirlAnimationRole.KOBOLD_BLOWJOB_SLOW_R : GirlAnimationRole.KOBOLD_BLOWJOB_SWITCH_TO_L);
         }
      }
   }

   public double koboldBlowjobVisualSideOffset() {
      if (this.activeAction != GirlSceneAction.KOBOLD_BLOWJOB) {
         return 0.0;
      } else if (this.scenePhase != GirlScenePhase.ACTIVE) {
         return 0.0;
      } else if (this.coreState().playerIsThrusting()) {
         return 0.0;
      } else {
         int i = Math.floorMod(this.sceneTicks, 80);
         if (i < 20) {
            return -0.2;
         } else if (i < 40) {
            return Mth.lerp((i - 20) / 20.0, -0.2, 0.2);
         } else {
            return i < 60 ? 0.2 : Mth.lerp((i - 60) / 20.0, 0.2, -0.2);
         }
      }
   }

   private RawAnimation selectActionAnimation() {
      RawAnimation custom = this.selectCustomActionAnimation();
      if (custom != null) {
         return custom;
      }

      GirlSceneAction girlsceneaction = this.resolvedActiveAction();
      GirlScenePhase girlscenephase = this.resolvedScenePhase();
      boolean flag = this.resolvedThrusting();
      if (girlsceneaction == null || girlscenephase == GirlScenePhase.IDLE || !this.isBusyTracked()) {
         return this.animation(GirlAnimationRole.NULL);
      }

      if (girlscenephase == GirlScenePhase.PAYMENT) {
         return this.animation(GirlAnimationRole.PAYMENT);
      }

      // The one phase a flat table cannot describe: the loop alternates sides on a timer.
      if (girlsceneaction == GirlSceneAction.KOBOLD_BLOWJOB && girlscenephase == GirlScenePhase.ACTIVE) {
         return this.selectKoboldBlowjobLoopAnimation(flag);
      }

      // scenes that ship several takes of the same beat rotate through them instead of looping one
      GirlAnimationRole variant = GirlSceneAnimations.role(girlsceneaction, girlscenephase, flag, this.resolvedHardVariant());
      return this.animation(variant);
   }

   private void handleSoundKeyframe(KeyFrameEvent<SceneGirlEntity, SoundKeyframeData> event) {
      if (this.level().isClientSide()) {
         String s = event.keyframeData().getSound();
         if (this.handleCustomSoundKeyframe(s)) {
            return;
         }

         switch (s) {
            case "stripMSG1":
               this.sayDialogue("hihi");
               this.playSoundVariant(this.sounds().voice("giggle", 5), 1.0F);
               break;
            case "paymentMSG1":
               this.sayDialogue("huh");
               this.playSoundIndex(this.sounds().voice("huh", 2), 1, 1.0F);
               break;
            case "paymentMSG2":
               this.playSoundIndex("misc.plob.plob", 0, 0.5F);
               this.sayPlayerRequest();
               break;
            case "paymentMSG3":
               this.sayDialogue("hehe");
               this.playSoundVariant(this.sounds().voice("giggle", 5), 1.0F);
               break;
            case "paymentMSG4":
               this.playSoundIndex("misc.plob.plob", 0, 0.25F);
               break;
            case "bjiMSG1":
               this.sayDialogue("blowjobtext1");
               this.playSoundIndex(this.sounds().voice("mmm", 9), 8, 1.0F);
               break;
            case "bjiMSG2":
               this.sayDialogue("blowjobtext2");
               this.playSoundIndex(this.sounds().voice("lightbreathing", 9), 8, 1.0F);
               break;
            case "bjiMSG3":
               this.sayDialogue("blowjobtext3");
               this.playSoundIndex(this.sounds().voice("aftersessionmoan", 1), 0, 1.0F);
               break;
            case "bjiMSG4":
               this.playSoundIndex("misc.belljingle.belljingle", 0, 1.0F);
               break;
            case "bjiMSG5":
               this.sayDialogue("blowjobtext4");
               this.playSoundIndex(this.sounds().voice("hmph", 2), 1, 0.5F);
               break;
            case "bjiMSG6":
               this.sayDialogue("blowjobtext5");
               this.playSoundIndex(this.sounds().voice("lightbreathing", 9), 8, 1.0F);
               break;
            case "bjiMSG7":
               this.sayDialogue("blowjobtext6");
               this.playSoundIndex(this.sounds().voice("giggle", 5), 4, 1.0F);
               break;
            case "bjiMSG8":
               this.sayPlayerLine("blowjobtext7");
               this.playSoundIndex("misc.plob.plob", 0, 0.5F);
               break;
            case "bjiMSG9":
               this.sayDialogue("blowjobtext8");
               this.playSoundIndex(this.sounds().voice("giggle", 3), 2, 1.0F);
               break;
            case "bjiMSG11":
               this.playSoundVariant(this.sounds().voice("lipsound", 10), 1.0F);
               com.nxrtweetg.sexmod.client.GirlHornyMeter.add(0.02);
               break;
            case "bjiMSG12":
               if (this.random.nextInt(5) == 0) {
                  this.playSoundVariant(this.sounds().voice("bjmoan", 13), 1.0F);
               }

               this.playSoundVariant(this.sounds().voice("lipsound", 10), 1.0F);
               com.nxrtweetg.sexmod.client.GirlHornyMeter.add(0.02);
               break;
            case "bjtMSG1":
            case "paizuriSlowMSG1":
               this.playSoundVariant(this.sounds().voice("mmm", 9), 1.0F);
               this.playSoundVariant(this.sounds().voice("lipsound", 10), 1.0F);
               com.nxrtweetg.sexmod.client.GirlHornyMeter.add(0.04);
               break;
            case "blowjobStartMSG1":
            case "blowjobStartMSG2":
            case "paizuriStartMSG1":
               this.playSoundVariant(this.sounds().voice("mmm", 9), 1.0F);
               break;
            case "blowjobStartbreath":
               this.playSoundVariant(this.sounds().voice("lightbreathing", 12), 1.0F);
               break;
            case "deepthroat_slowMSG1":
            case "deepthroat_fastMSG1":
               this.playSoundVariant(this.sounds().voice("lipsound", 10), 1.0F);
               this.playSoundVariant(this.sounds().voice("bjmoan", 13), 1.0F);
               break;
            case "switch":
            case "endSwitch":
            case "blowjobFastDone":
            case "carry_introCam":
            case "carry_introCam2":
            case "carry_slowDone":
            case "carry_fastDone":
            case "carry_cumDone":
            case "openSexUI":
            case "openSexUi":
            case "sexUI":
            case "sexUiOn":
            case "matingCam":
            case "mating_cum_cam":
            case "mating_press_softReady":
            case "mating_press_hardReady":
            case "mating_press_hardDone":
            case "analStartCam":
            case "analStartDone":
            case "analCumDone":
            case "idle":
            case "drawItems":
            case "removeItems":
            case "startStrip":
            case "becomeNude":
            case "stripDone":
            case "paymentDone":
            case "bjiDone":
            case "bjtReady":
            case "bjtDone":
            case "paizuriReady":
            case "paizuri_fastDone":
            case "paizuri_cumDone":
            case "doggyGoOnBedDone":
            case "doggystartDone":
            case "doggyfastReady":
            case "doggyfastDone":
            case "doggyCumDone":
            case "bjcBlackScreen":
            case "paizuri_startDone":
            case "boobjob_camera":
            case "attackDone":
            case "pearl":
            case "paizuri_startStep":
            case "missionary_startDone":
            case "missionary_fastReady":
            case "missionary_fastDone":
            case "missionary_cumDone":
            case "mating_press_startDone":
            case "mating_press_cumDone":
            case "renderEgg":
            case "cowgirlStartDone":
            case "cowgirlfastReady":
            case "cowgirlfastDone":
            case "cowgirlfastdomDone":
            case "cowgirlcumDone":
            case "cowgirl_cumDone":
            case "cowgirlSlowDone":
            case "blackScreen":
            case "blackscreen":
            case "anal_prepareDone":
            case "anal_startDone":
            case "anal_fastDone":
            case "anal_cumDone":
            case "analDone":
            case "sitting_introDone":
            case "sitting_fastTp":
            case "sitting_fastDone":
            case "touch_boobs_introDone":
            case "touch_boobs_slowDone":
            case "touch_boobs_cumDone":
            case "blowjobStartDone":
            case "blowjobCumDone":
            case "deepthroat_startDone":
            case "deepthroat_fastDone":
            case "deepthroat_cumDone":
            case "doggy_cumDone":
            case "fastDone":
            default:
               break;
            case "paizuriFastMSG1":
               this.playSoundVariant(this.sounds().voice("ahh", 10), 1.0F);
               com.nxrtweetg.sexmod.client.GirlHornyMeter.add(0.04);
               break;
            case "paizuri_cumStart":
               this.playSoundVariant("misc.cuminflation.cuminflation", 1, 2.0F);
               break;
            case "deepthroat_cumMSG1":
               this.playSoundVariant("misc.cuminflation.cuminflation", 1, 1.6F);
               this.playSoundVariant(this.sounds().voice("heavybreathing", 8), 1.0F);
               break;
            case "bjmoan":
               this.playSoundVariant(this.sounds().voice("bjmoan", 13), 1.0F);
               break;
            case "lipsound":
               this.playSoundVariant(this.sounds().voice("lipsound", 10), 1.0F);
               break;
            case "bjcMSG1":
            case "bjcMSG2":
            case "bjcMSG3":
            case "bjcMSG4":
            case "bjcMSG5":
            case "bjcMSG6":
            case "bjcMSG7":
               this.playSoundVariant(this.sounds().voice("heavybreathing", 8), 1.0F);
               break;
            case "touch_boobs_cumMSG1":
               this.playSoundVariant("misc.cuminflation.cuminflation", 1, 2.0F);
               break;
            case "touch_boobs_cumMSG3":
               this.playSoundVariant(this.sounds().voice("heavybreathing", 8), 1.0F);
               break;
            case "touch_boobsMSG1":
               this.playSoundVariant("misc.touch.touch", 2, 1.0F);
               this.playSoundVariant(this.sounds().voice("moan", 8), 1.0F);
               break;
            case "carry_introMSG1":
            case "carry_introMSG2":
               this.playSoundVariant("misc.touch.touch", 2, 1.0F);
               this.playSoundVariant(this.sounds().voice("moan", 8), 1.0F);
               break;
            case "doggyGoOnBedMSG1":
            case "bedRustle":
               this.playSoundVariant("misc.bedrustle.bedrustle", 2, 1.0F);
               break;
            case "doggyGoOnBedMSG2":
               this.playSoundVariant(this.sounds().voice("lightbreathing", 12), 1.0F);
               break;
            case "doggyGoOnBedMSG3":
               this.playSoundVariant(this.sounds().voice("giggle", 5), 1.0F);
               break;
            case "doggyGoOnBedMSG4":
               this.playSoundVariant("misc.slap.slap", 2, 0.75F);
               break;
            case "doggystartMSG1":
            case "doggystartMSG2":
               this.playSoundVariant("misc.touch.touch", 2, 1.0F);
               break;
            case "doggystartMSG3":
               this.playSoundVariant("misc.bedrustle.bedrustle", 2, 0.5F);
               break;
            case "doggystartMSG4":
               this.playSoundVariant("misc.inserts.inserts", 5, 1.0F);
               this.playSoundVariant(this.sounds().voice("mmm", 9), 1.0F);
               break;
            case "doggystartMSG5":
            case "doggyslowMSG1":
            case "doggyfastMSG1":
               this.playSoundVariant("misc.pounding.pounding", 36, s.equals("doggyfastMSG1") ? 0.75F : 0.33F);
               this.playSoundVariant(this.sounds().voice("moan", 8), 1.0F);
               com.nxrtweetg.sexmod.client.GirlHornyMeter.add(s.equals("doggyfastMSG1") ? 0.02 : 0.00666);
               break;
            case "analSoft":
               this.playSoundVariant("misc.pounding.pounding", 36, 0.33F);
               break;
            case "analHard":
               this.playSoundVariant("misc.pounding.pounding", 36, 0.75F);
               break;
            case "analFastRapid":
               this.playSoundVariant("misc.pounding.pounding", 36, 0.9F);
               break;
            case "analHardMSG1":
               this.playSoundVariant(this.sounds().voice("moan", 8), 1.0F);
               break;
            case "breath":
               this.playSoundVariant(this.sounds().voice("lightbreathing", 12), 0.5F);
               break;
            case "orgasm":
               this.playSoundVariant(this.sounds().voice("heavybreathing", 8), 1.0F);
               break;
            case "cumLoud":
               this.playSoundVariant("misc.cuminflation.cuminflation", 1, 2.0F);
               break;
            case "cumQuiet":
               this.playSoundVariant("misc.cuminflation.cuminflation", 1, 1.0F);
               break;
            case "cumMsg":
               this.playSoundVariant(this.sounds().voice("heavybreathing", 8), 1.0F);
               break;
            case "pound":
               this.playSoundVariant("misc.pounding.pounding", 36, 0.5F);
               break;
            case "moanMating":
               this.playSoundVariant(this.sounds().voice("moan", 8), 1.0F);
               break;
            case "doggyMoan":
               this.playSoundVariant(this.sounds().voice("moan", 8), 1.0F);
               break;
            case "doggySwitch":
            case "doggyReset":
               this.playSoundVariant("misc.inserts.inserts", 5, 1.0F);
               break;
            case "orgasm1":
            case "orgasm2":
               this.playSoundVariant(this.sounds().voice("heavybreathing", 8), 1.0F);
               break;
            case "doggyslowMSG2":
               this.playSoundVariant(this.sounds().voice("lightbreathing", 12), 0.5F);
               break;
            case "doggycumMSG1":
               this.playSoundVariant("misc.cuminflation.cuminflation", 1, 2.0F);
               this.playSoundVariant("misc.pounding.pounding", 36, 2.0F);
               this.playSoundVariant(this.sounds().voice("moan", 8), 1.0F);
               break;
            case "doggycumMSG2":
            case "doggycumMSG3":
            case "doggycumMSG4":
            case "doggycumMSG5":
               this.playSoundVariant(this.sounds().voice("heavybreathing", 8), 1.0F);
               break;
            case "missionary_slowMSG1":
            case "missionary_fastMSG1":
               this.playSoundVariant("misc.pounding.pounding", 36, s.equals("missionary_fastMSG1") ? 0.75F : 0.33F);
               this.playSoundVariant(this.sounds().voice("moan", 8), 1.0F);
               break;
            case "missionary_cumMSG1":
               this.playSoundVariant("misc.cuminflation.cuminflation", 1, 2.0F);
               this.playSoundVariant(this.sounds().voice("heavybreathing", 8), 1.0F);
               break;
            case "missionary_cumMSG2":
               this.playSoundVariant(this.sounds().voice("heavybreathing", 8), 1.0F);
               break;
            case "anal_prepareMSG1":
            case "anal_prepareMSG2":
            case "anal_startMSG1":
            case "anal_startMSG2":
               this.playSoundVariant("misc.touch.touch", 2, 1.0F);
               this.playSoundVariant(this.sounds().voice("moan", 8), 1.0F);
               break;
            case "anal_slowMSG1":
               this.playSoundVariant("misc.pounding.pounding", 36, 0.33F);
               this.playSoundVariant(this.sounds().voice("moan", 8), 1.0F);
               break;
            case "anal_fastMSG1":
               this.playSoundVariant("misc.pounding.pounding", 36, 0.75F);
               this.playSoundVariant(this.sounds().voice("moan", 8), 1.0F);
               break;
            case "anal_cumMSG2":
               this.playSoundVariant(this.sounds().voice("heavybreathing", 8), 1.0F);
               break;
            case "anal_cumBlackScreen":
               this.playSoundVariant("misc.cuminflation.cuminflation", 1, 2.0F);
               break;
            case "sitting_slowMSG1":
               this.playSoundVariant("misc.pounding.pounding", 36, 0.33F);
               this.playSoundVariant(this.sounds().voice("moan", 8), 1.0F);
               break;
            case "sitting_fastMSG1":
               this.playSoundVariant("misc.pounding.pounding", 36, 0.75F);
               this.playSoundVariant(this.sounds().voice("moan", 8), 1.0F);
               break;
            case "sitting_cumMSG1":
               this.playSoundVariant("misc.cuminflation.cuminflation", 1, 2.0F);
               this.playSoundVariant(this.sounds().voice("heavybreathing", 8), 1.0F);
               break;
            case "touch":
               this.playSoundVariant("misc.touch.touch", 2, 1.0F);
               break;
            case "jump":
               this.playSoundVariant("misc.touch.touch", 2, 0.85F);
               break;
            case "giggle":
               this.playSoundVariant(this.sounds().voice("giggle", 5), 1.0F);
               break;
            case "pounding":
               this.playSoundVariant("misc.pounding.pounding", 36, 0.5F);
               break;
            case "moan":
            case "slowMoan":
            case "fastMoan":
            case "aftermoan":
               this.playSoundVariant(this.sounds().voice("moan", 8), 1.0F);
               break;
            case "moanOrNya":
               if (this.random.nextBoolean()) {
                  this.playSoundVariant(this.sounds().voice("moan", 8), 1.0F);
               } else {
                  this.playSoundVariant(this.sounds().voice("ahh", 10), 1.0F);
               }
               break;
            case "mmm":
               this.playSoundVariant(this.sounds().voice("mmm", 9), 1.0F);
               break;
            case "hehe":
               this.playSoundVariant(this.sounds().voice("giggle", 5), 1.0F);
               break;
            case "singing":
               this.playSoundVariant(this.sounds().voice("happyoh", 3), 1.0F);
               break;
            case "horninya":
            case "horninya2":
               this.playSoundVariant(this.sounds().voice("ahh", 10), 1.0F);
               break;
            case "slide":
            case "fastSwitch":
               this.playSoundVariant("misc.inserts.inserts", 5, 1.0F);
               break;
            case "cum":
               this.playSoundVariant("misc.cuminflation.cuminflation", 1, 2.0F);
               break;
            case "addCumSlow":
               this.playSoundVariant("misc.touch.touch", 2, 0.75F);
               this.playSoundVariant(this.sounds().voice("lightbreathing", 12), 0.75F);
               break;
            case "addCumFast":
               this.playSoundVariant("misc.touch.touch", 2, 1.0F);
               this.playSoundVariant(this.sounds().voice("moan", 8), 1.0F);
               break;
            case "cowgirlStartMSG0":
               this.playSoundVariant("misc.bedrustle.bedrustle", 2, 1.0F);
               break;
            case "cowgirlStartMSG1":
            case "cowgirlStartMSG2":
               this.playSoundVariant("misc.touch.touch", 2, 1.0F);
               this.playSoundVariant(this.sounds().voice("moan", 8), 1.0F);
               break;
            case "cowgirlfastMSG1":
               this.playSoundVariant("misc.pounding.pounding", 36, 0.75F);
               this.playSoundVariant(this.sounds().voice("moan", 8), 1.0F);
               break;
            case "cowgirlcumMSG1":
               this.playSoundVariant("misc.cuminflation.cuminflation", 1, 2.0F);
               break;
            case "cowgirlcumMSG2":
            case "cowgirlcumMSG3":
            case "cowgirlcumMSG4":
            case "cowgirlcumMSG5":
            case "cowgirlcumMSG6":
               this.playSoundVariant(this.sounds().voice("heavybreathing", 8), 1.0F);
               break;
            case "attackSound":
               this.playSoundVariant("misc.slap.slap", 2, 1.0F);
               break;
         }
      }
   }

   /** Prints a Jenny line as "&lt;Jenny&gt; text", matching the original chat format. */
   protected void sayDialogue(String key) {
      this.broadcastLine(String.format("<%s> %s", this.getName().getString(), I18n.get(this.dialogueKey(key))));
   }

   /** Prints a literal line client-side, used where the original spoke a non-i18n string. */
   protected void sayClientLine(String line) {
      if (this.level().isClientSide()) {
         net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
         if (minecraft.player != null) {
            minecraft.player.sendSystemMessage(net.minecraft.network.chat.Component.literal("<" + this.getName().getString() + "> " + line));
         }
      }
   }

   /** Prints a line attributed to the player, used where the original voiced the request. */
   private void sayPlayerLine(String key) {
      Player player = Minecraft.getInstance().player;
      if (player != null) {
         this.broadcastLine(String.format("<%s> %s", player.getName().getString(), I18n.get(this.dialogueKey(key))));
      }
   }

   /** The player asking for the currently queued action. */
   protected void sayPlayerRequest() {
      // Reached only from the sound keyframes, which fire client-side, where activeAction is never set.
      GirlSceneAction action = this.resolvedActiveAction();
      String key = action == null
         ? "givesex"
         : switch (action) {
            case STRIP -> "showBobsandveganapls";
            case BLOWJOB, DEEPTHROAT, FACEFUCK, KOBOLD_BLOWJOB -> "giveblowjob";
            case BOOBJOB, TOUCH_BOOBS -> "givebooba";
            default -> "givesex";
         };
      this.sayPlayerLine(key);
   }

   private void broadcastLine(String line) {
      Player player = Minecraft.getInstance().player;
      if (player != null && player.distanceToSqr(this) <= 1600.0) {
         player.sendSystemMessage(Component.literal(line));
      }
   }

   protected void playSoundVariant(GirlSoundSet.Voice voice, float volume) {
      if (voice.variants() > 0) {
         int index = this.random.nextInt(voice.variants());

         for (int attempt = 0; attempt < 10 && voice.variants() > 1 && index == this.lastSoundVariants.getOrDefault(voice.baseId(), -1); attempt++) {
            index = this.random.nextInt(voice.variants());
         }

         this.lastSoundVariants.put(voice.baseId(), index);
         this.playSoundAt(voice.id(index), volume);
      }
   }

   /** The shared effect banks under {@code misc}, which every girl uses the same way. */
   protected void playSoundVariant(String baseId, int variants, float volume) {
      this.playSoundVariant(new GirlSoundSet.Voice(baseId, variants), volume);
   }

   /** Plays one specific variant, for the keyframes where the original hardcoded an index. */
   protected void playSoundIndex(GirlSoundSet.Voice voice, int index, float volume) {
      this.playSoundAt(voice.id(index), volume);
   }

   protected void playSoundIndex(String baseId, int index, float volume) {
      this.playSoundAt(baseId + index, volume);
   }

   protected void playSoundAt(String soundId, float volume) {
      if (this.level().isClientSide()) {
         SoundEvent soundevent = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("sexmod", soundId));
         this.level().playLocalSound(this.blockPosition(), soundevent, SoundSource.NEUTRAL, volume, 1.0F, false);
      }
   }

   private void ensureNeutralSpawnState() {
      this.spawnStateInitialized = true;
      if (this.activeAction == null && this.scenePhase == GirlScenePhase.IDLE && this.coreState().activePlayerId() == null) {
            this.climaxTicks = 0;
         this.sceneTicks = 0;
         this.thrustTicks = 0;
            this.coreState().setPaymentItemsAmount(0);
         this.coreState().setFollowUpAction(GirlFollowUpAction.NONE);
         this.coreState().setPlayerIsThrusting(false);
         this.coreState().setPlayerIsCumming(false);
         this.coreState().animationState().set(GirlAnimationParameter.AUTO_HEAD, true);
         this.coreState().animationState().set(GirlAnimationParameter.AUTO_EYES, true);
         this.coreState().animationState().set(GirlAnimationParameter.MOVEMENT_ACTIVE, true);
         this.coreState().animationState().set(GirlAnimationParameter.PLAYER_SHOULD_BE_RENDERED, false);
         this.applyActionAnimationFlags(GirlSceneAction.STRIP, GirlScenePhase.IDLE);
         this.syncTrackedState();
      }
   }

   private boolean loadedWithPersistedSceneState() {
      return this.activeAction != null
         || this.scenePhase != GirlScenePhase.IDLE
         || this.coreState().activePlayerId() != null
         || this.coreState().playerIsInPosition()
         || this.coreState().playerIsThrusting()
         || this.coreState().playerIsCumming()
         || this.coreState().animationState().get(GirlAnimationParameter.PLAYER_SHOULD_BE_RENDERED)
         || this.coreState().animationState().get(GirlAnimationParameter.IS_DOING_ACTION);
   }

   private void clearPersistedSceneState() {
      this.activeAction = null;
      this.scenePhase = GirlScenePhase.IDLE;
      // she may have been saved mid-scene while pinned weightless on a bed
      this.setNoGravity(false);
      this.climaxTicks = 0;
      this.sceneTicks = 0;
      this.thrustTicks = 0;
      this.sceneFrozen = false;
      this.stripTargetModel = 1;
      this.queuedActionAfterStrip = null;
      this.coreState().clearInteraction();
      this.coreState().setShouldGetDamage(true);
      this.coreState().setPaymentItemsAmount(0);
      this.coreState().setPlayerYaw(0.0F);
      this.coreState().setTargetYaw(0.0F);
      this.coreState().setCurrentModel(1);
      this.applyActionAnimationFlags(GirlSceneAction.STRIP, GirlScenePhase.IDLE);
   }

   protected void syncTrackedState() {
      this.getEntityData().set(TRACKED_ACTIVE_ACTION, this.activeAction == null ? "" : this.activeAction.legacyId());
      this.getEntityData().set(TRACKED_PAYMENT_ITEM, this.coreState().paymentItem().name());
      this.getEntityData().set(TRACKED_PAYMENT_AMOUNT, this.coreState().paymentItemsAmount());
      this.getEntityData().set(TRACKED_FOLLOW_UP, this.coreState().followUpAction().name());
      this.getEntityData().set(TRACKED_BUSY, this.coreState().activePlayerId() != null);
      this.getEntityData().set(TRACKED_ACTIVE_PLAYER, Optional.ofNullable(this.coreState().activePlayerId()));
      this.getEntityData().set(TRACKED_SCENE_PHASE, this.scenePhase.id());
      this.getEntityData().set(TRACKED_MODEL_VARIANT, this.coreState().currentModel());
      this.getEntityData().set(TRACKED_RENDER_PLAYER, this.coreState().animationState().get(GirlAnimationParameter.PLAYER_SHOULD_BE_RENDERED));
      this.getEntityData().set(TRACKED_THRUSTING, this.coreState().playerIsThrusting() && this.thrustTicks > 0);
      this.getEntityData().set(TRACKED_INTRO_ACTIVE, this.isServerIntroActive());
      this.getEntityData().set(TRACKED_SCENE_TUNE_FORWARD, (int)Math.round(this.sceneTuneForward * 100.0));
      this.getEntityData().set(TRACKED_SCENE_TUNE_SIDE, (int)Math.round(this.sceneTuneSide * 100.0));
      this.getEntityData().set(TRACKED_SCENE_TUNE_VERTICAL, (int)Math.round(this.sceneTuneVertical * 100.0));
      this.getEntityData().set(TRACKED_SCENE_FROZEN, this.sceneFrozen);
      this.getEntityData().set(TRACKED_FADE_SIGNAL, this.fadeSignalCounter);
      // the scene is framed against Jenny's own facing, which prepareSceneLayout pinned
      this.getEntityData().set(TRACKED_SCENE_YAW, this.coreState().targetYaw());
      this.getEntityData().set(TRACKED_HARD_VARIANT, this.hardVariant);
   }

   private void loadSceneTune(GirlSceneAction action) {
      if (!GirlSceneLayoutConfig.supportsForTuning(action)) {
         this.sceneTuneForward = 0.0;
         this.sceneTuneSide = 0.0;
         this.sceneTuneVertical = 0.0;
      } else {
         GirlSceneLayoutConfig.SceneOffsets jennyscenelayoutconfig$sceneoffsets = GirlSceneLayoutConfig.get(this.characterId(), action, this.sceneTuneProfilePhase());
         this.sceneTuneForward = jennyscenelayoutconfig$sceneoffsets.forward();
         this.sceneTuneSide = jennyscenelayoutconfig$sceneoffsets.side();
         this.sceneTuneVertical = jennyscenelayoutconfig$sceneoffsets.vertical();
      }
   }

   private GirlScenePhase sceneTuneProfilePhase() {
      return switch (this.scenePhase) {
         case PAYMENT -> GirlScenePhase.PAYMENT;
         case INTRO -> GirlScenePhase.INTRO;
         default -> GirlScenePhase.ACTIVE;
      };
   }

   private void prepareSceneLayout(ServerPlayer player, GirlSceneAction action) {
      switch (action) {
         case BLOWJOB:
         case KOBOLD_BLOWJOB:
         case DEEPTHROAT:
         case FACEFUCK:
         case BOOBJOB:
         case TOUCH_BOOBS:
         case STRIP:
            float f2 = player.getYRot();

            double d1 = switch (action) {
               case FACEFUCK -> 0.48;
               case BOOBJOB -> 0.72;
               case TOUCH_BOOBS -> 0.72;
               default -> 0.95;
            };
            // DEEPTHROAT/KOBOLD_BLOWJOB/TOUCH_BOOBS shift the body bone on the client instead, so
            // moving the entity as well would apply the tuning twice
            boolean movesBodyBone = action == GirlSceneAction.DEEPTHROAT
               || action == GirlSceneAction.KOBOLD_BLOWJOB
               || action == GirlSceneAction.TOUCH_BOOBS;
            double tuneForward = movesBodyBone ? 0.0 : this.sceneTuneForward;
            double tuneSide = movesBodyBone ? 0.0 : this.sceneTuneSide;
            double tuneVertical = movesBodyBone ? 0.0 : this.sceneTuneVertical;
            Vec3 vec328 = horizontalRight(f2);
            Vec3 vec33 = player.position().add(horizontalForward(f2).scale(d1 + tuneForward)).add(vec328.scale(tuneSide));
            float f10 = Mth.wrapDegrees(f2 + 180.0F);
            this.snapTo(vec33.x, player.getY() + tuneVertical, vec33.z, f10, 0.0F);
            this.coreState().setTargetYaw(f10);
            this.coreState().setPlayerYaw(f2);
            break;
         case DOGGY:
            Vec3 vec311 = this.requireSceneBedCenter();
            float f9 = Mth.wrapDegrees(directionYaw(this.requireSceneBedFacing()));
            Vec3 vec320 = horizontalRight(f9);
            Vec3 vec327 = horizontalForward(f9).scale(0.52 + this.sceneTuneForward).add(vec320.scale(-0.22 + this.sceneTuneSide));
            double d8 = vec311.y + 0.12 + this.sceneTuneVertical;
            this.snapTo(vec311.x + vec327.x, d8, vec311.z + vec327.z, f9, 0.0F);
            this.coreState().setTargetYaw(f9);
            this.coreState().setPlayerYaw(f9);
            break;
         case PRONE_DOGGY:
            Vec3 vec310 = this.requireSceneBedCenter();
            float f8 = Mth.wrapDegrees(directionYaw(this.requireSceneBedFacing()));
            Vec3 vec319 = horizontalRight(f8);
            Vec3 vec326 = horizontalForward(f8).scale(0.52 + this.sceneTuneForward).add(vec319.scale(-0.22 + this.sceneTuneSide));
            double d7 = vec310.y + 0.12 + this.sceneTuneVertical;
            this.snapTo(vec310.x + vec326.x, d7, vec310.z + vec326.z, f8, 0.0F);
            this.coreState().setTargetYaw(f8);
            this.coreState().setPlayerYaw(f8);
            break;
         case SITTING_ALT:
            Vec3 altBedCenter = this.requireSceneBedCenter();
            float altYaw = Mth.wrapDegrees(directionYaw(this.requireSceneBedFacing()));
            Vec3 altRight = horizontalRight(altYaw);
            Vec3 altOffset = horizontalForward(altYaw).scale(0.52 + this.sceneTuneForward).add(altRight.scale(-0.22 + this.sceneTuneSide));
            double altY = altBedCenter.y + 0.12 + this.sceneTuneVertical;
            this.snapTo(altBedCenter.x + altOffset.x, altY, altBedCenter.z + altOffset.z, altYaw, 0.0F);
            this.coreState().setTargetYaw(altYaw);
            this.coreState().setPlayerYaw(altYaw);
            break;
         case ANAL:
            Vec3 vec39 = this.requireSceneBedCenter();
            float f7 = Mth.wrapDegrees(directionYaw(this.requireSceneBedFacing()));
            Vec3 vec318 = horizontalRight(f7);
            Vec3 vec325 = horizontalForward(f7).scale(0.44 + this.sceneTuneForward).add(vec318.scale(-0.1 + this.sceneTuneSide));
            double d6 = vec39.y + 0.26 + this.sceneTuneVertical;
            this.snapTo(vec39.x + vec325.x, d6, vec39.z + vec325.z, f7, 0.0F);
            this.coreState().setTargetYaw(f7);
            this.coreState().setPlayerYaw(f7);
            break;
         case ANAL_ALT:
            Vec3 analAltBedCenter = this.requireSceneBedCenter();
            float analAltYaw = Mth.wrapDegrees(directionYaw(this.requireSceneBedFacing()));
            Vec3 analAltRight = horizontalRight(analAltYaw);
            Vec3 analAltOffset = horizontalForward(analAltYaw).scale(0.44 + this.sceneTuneForward).add(analAltRight.scale(-0.1 + this.sceneTuneSide));
            double analAltY = analAltBedCenter.y + 0.26 + this.sceneTuneVertical;
            this.snapTo(analAltBedCenter.x + analAltOffset.x, analAltY, analAltBedCenter.z + analAltOffset.z, analAltYaw, 0.0F);
            this.coreState().setTargetYaw(analAltYaw);
            this.coreState().setPlayerYaw(analAltYaw);
            break;
         case KOBOLD_ANAL:
            float f1 = player.getYRot();
            Vec3 vec312 = horizontalRight(f1);
            Vec3 vec317 = player.position()
               .add(horizontalForward(f1).scale(0.42 + this.sceneTuneForward))
               .add(vec312.scale(this.sceneTuneSide));
            this.snapTo(vec317.x, player.getY() + this.sceneTuneVertical, vec317.z, f1, 0.0F);
            this.coreState().setTargetYaw(f1);
            this.coreState().setPlayerYaw(f1);
            break;
         case SITTING:
            Vec3 vec38 = this.requireSceneBedCenter();
            float f6 = Mth.wrapDegrees(directionYaw(this.requireSceneBedFacing()));
            Vec3 vec316 = horizontalRight(f6);
            Vec3 vec324 = horizontalForward(f6).scale(0.34 + this.sceneTuneForward).add(vec316.scale(-0.12 + this.sceneTuneSide));
            double d5 = vec38.y + 0.08 + this.sceneTuneVertical;
            this.snapTo(vec38.x + vec324.x, d5, vec38.z + vec324.z, f6, 0.0F);
            this.coreState().setTargetYaw(f6);
            this.coreState().setPlayerYaw(Mth.wrapDegrees(f6 + 180.0F));
            break;
         case MISSIONARY:
            Vec3 vec37 = this.requireSceneBedCenter();
            float f5 = Mth.wrapDegrees(directionYaw(this.requireSceneBedFacing()));
            Vec3 vec315 = horizontalRight(f5);
            Vec3 vec323 = horizontalForward(f5).scale(0.56 + this.sceneTuneForward).add(vec315.scale(-0.18 + this.sceneTuneSide));
            double d4 = vec37.y + 0.1 + this.sceneTuneVertical;
            Vec3 vec34 = new Vec3(vec37.x + vec323.x, d4, vec37.z + vec323.z);
            this.snapTo(vec34.x, d4, vec34.z, f5, 0.0F);
            this.coreState().setTargetYaw(f5);
            this.coreState().setPlayerYaw(Mth.wrapDegrees(f5 + 180.0F));
            break;
         case MATING_PRESS:
            Vec3 vec36 = this.requireSceneBedCenter();
            float f4 = Mth.wrapDegrees(directionYaw(this.requireSceneBedFacing()));
            Vec3 vec314 = horizontalRight(f4);
            Vec3 vec322 = horizontalForward(f4).scale(0.4 + this.sceneTuneForward).add(vec314.scale(-0.1 + this.sceneTuneSide));
            double d3 = vec36.y + 0.14 + this.sceneTuneVertical;
            this.snapTo(vec36.x + vec322.x, d3, vec36.z + vec322.z, f4, 0.0F);
            this.coreState().setTargetYaw(f4);
            this.coreState().setPlayerYaw(f4);
            break;
         case REVERSE_COWGIRL:
            Vec3 vec35 = this.requireSceneBedCenter();
            float f3 = Mth.wrapDegrees(directionYaw(this.requireSceneBedFacing()));
            Vec3 vec313 = horizontalRight(f3);
            // the body bone carries this scene's tuning on the client, so the entity itself stays put
            Vec3 vec321 = horizontalForward(f3).scale(0.34).add(vec313.scale(-0.12));
            double d2 = vec35.y + 0.08;
            this.snapTo(vec35.x + vec321.x, d2, vec35.z + vec321.z, f3, 0.0F);
            this.coreState().setTargetYaw(f3);
            this.coreState().setPlayerYaw(Mth.wrapDegrees(f3 + 180.0F));
            break;
         case COWGIRL:
            Vec3 vec3 = this.requireSceneBedCenter();
            float f = Mth.wrapDegrees(directionYaw(this.requireSceneBedFacing()));
            Vec3 vec31 = horizontalRight(f);
            Vec3 vec32 = horizontalForward(f).scale(0.34 + this.sceneTuneForward).add(vec31.scale(-0.12 + this.sceneTuneSide));
            double d0 = vec3.y + 0.08 + this.sceneTuneVertical;
            this.snapTo(vec3.x + vec32.x, d0, vec3.z + vec32.z, f, 0.0F);
            this.coreState().setTargetYaw(f);
            this.coreState().setPlayerYaw(Mth.wrapDegrees(f + 180.0F));
      }
   }

   /** Pins Jenny to the yaw the scene chose, overriding the vanilla look controller. */
   private void holdSceneYaw() {
      float yaw = this.coreState().targetYaw();
      this.setYRot(yaw);
      this.setYBodyRot(yaw);
      this.setYHeadRot(yaw);
      this.yRotO = yaw;
      this.yBodyRotO = yaw;
      this.yHeadRotO = yaw;
      this.getLookControl().setLookAt(this.getX() + horizontalForward(yaw).x, this.getEyeY(), this.getZ() + horizontalForward(yaw).z);
   }

   private void alignSceneParticipants(ServerPlayer player) {
      float f = this.coreState().targetYaw();
      this.setYRot(f);
      this.setYBodyRot(f);
      this.setYHeadRot(f);

      Vec3 vec3 = switch (this.activeAction) {
         case BLOWJOB -> this.position().add(horizontalForward(f).scale(0.92));
         case KOBOLD_BLOWJOB -> this.position().add(horizontalForward(f).scale(0.92));
         case DEEPTHROAT -> this.position().add(horizontalForward(f).scale(0.92));
         case FACEFUCK -> this.position().add(horizontalForward(f).scale(0.48));
         case BOOBJOB -> this.position().add(horizontalForward(f).scale(0.72));
         case TOUCH_BOOBS -> this.position().add(horizontalForward(f).scale(0.72));
         case DOGGY -> {
            Vec3 vec39 = this.requireSceneBedCenter();
            Vec3 vec316 = horizontalRight(f);
            yield new Vec3(vec39.x + vec316.x * 1.05, this.sceneBedFootPos.getY(), vec39.z + vec316.z * 1.05);
         }
         case PRONE_DOGGY -> {
            Vec3 vec38 = this.requireSceneBedCenter();
            Vec3 vec315 = horizontalRight(f);
            yield new Vec3(vec38.x + vec315.x * 1.05, this.sceneBedFootPos.getY(), vec38.z + vec315.z * 1.05);
         }
         case SITTING_ALT -> {
            Vec3 altCenter = this.requireSceneBedCenter();
            Vec3 altSide = horizontalRight(f);
            yield new Vec3(altCenter.x + altSide.x * 1.05, this.sceneBedFootPos.getY(), altCenter.z + altSide.z * 1.05);
         }
         case ANAL -> {
            Vec3 vec37 = this.requireSceneBedCenter();
            Vec3 vec314 = horizontalRight(f);
            yield new Vec3(vec37.x + vec314.x * 1.0, this.sceneBedFootPos.getY(), vec37.z + vec314.z * 1.0);
         }
         case ANAL_ALT -> {
            Vec3 analAltCenter = this.requireSceneBedCenter();
            Vec3 analAltSide = horizontalRight(f);
            yield new Vec3(analAltCenter.x + analAltSide.x * 1.0, this.sceneBedFootPos.getY(), analAltCenter.z + analAltSide.z * 1.0);
         }
         case KOBOLD_ANAL -> this.position().add(horizontalForward(f).scale(-0.1));
         case SITTING -> {
            Vec3 vec36 = this.requireSceneBedCenter();
            Vec3 vec313 = horizontalRight(f);
            yield new Vec3(vec36.x + vec313.x * 1.1, this.sceneBedFootPos.getY(), vec36.z + vec313.z * 1.1);
         }
         case MISSIONARY -> {
            Vec3 vec35 = this.requireSceneBedCenter();
            Vec3 vec312 = horizontalRight(f);
            yield new Vec3(vec35.x + vec312.x * 1.1, this.sceneBedFootPos.getY(), vec35.z + vec312.z * 1.1);
         }
         case MATING_PRESS -> {
            Vec3 vec34 = this.requireSceneBedCenter();
            Vec3 vec311 = horizontalRight(f);
            yield new Vec3(vec34.x + vec311.x * 1.0, this.sceneBedFootPos.getY(), vec34.z + vec311.z * 1.0);
         }
         case REVERSE_COWGIRL -> {
            Vec3 vec33 = this.requireSceneBedCenter();
            Vec3 vec310 = horizontalRight(f);
            yield new Vec3(vec33.x + vec310.x * 1.1, this.sceneBedFootPos.getY(), vec33.z + vec310.z * 1.1);
         }
         case COWGIRL -> {
            Vec3 vec31 = this.requireSceneBedCenter();
            Vec3 vec32 = horizontalRight(f);
            yield new Vec3(vec31.x + vec32.x * 1.1, this.sceneBedFootPos.getY(), vec31.z + vec32.z * 1.1);
         }
         case STRIP -> null;
      };
      if (vec3 != null) {
         float f1 = switch (this.activeAction) {
            case BLOWJOB -> Mth.wrapDegrees(f + 180.0F);
            case KOBOLD_BLOWJOB -> Mth.wrapDegrees(f + 180.0F);
            case DEEPTHROAT -> Mth.wrapDegrees(f + 180.0F);
            case FACEFUCK -> Mth.wrapDegrees(f + 180.0F);
            case BOOBJOB -> Mth.wrapDegrees(f + 180.0F);
            case TOUCH_BOOBS -> Mth.wrapDegrees(f + 180.0F);
            case DOGGY, PRONE_DOGGY, ANAL, ANAL_ALT, MISSIONARY, MATING_PRESS, REVERSE_COWGIRL, COWGIRL -> Mth.wrapDegrees(f + 90.0F);
            case KOBOLD_ANAL -> f;
            case SITTING, SITTING_ALT -> Mth.wrapDegrees(f + 90.0F);
            case STRIP -> player.getYRot();
         };
         player.teleportTo(vec3.x, vec3.y, vec3.z);
         player.setYRot(f1);
         player.setYBodyRot(f1);
         player.setYHeadRot(f1);
         player.setXRot(0.0F);
         player.setDeltaMovement(0.0, 0.0, 0.0);
         this.coreState().setPlayerYaw(f1);
         this.coreState().setPlayerIsInPosition(true);
      }
   }

   private int requiredPositioningTicks() {
      return switch (this.activeAction) {
         case BLOWJOB -> 0;
         case KOBOLD_BLOWJOB -> 0;
         case DEEPTHROAT -> 0;
         case FACEFUCK -> 0;
         case BOOBJOB -> 0;
         case TOUCH_BOOBS -> 0;
         case DOGGY -> 130;
         // shares anal's lying-down take, so it needs anal's runtime, not the old 38 ticks that
         // belonged to the much shorter prone_doggy_intro
         case PRONE_DOGGY -> 111;
         case SITTING_ALT -> 111;
         case ANAL -> 111;
         case ANAL_ALT -> 110;
         case KOBOLD_ANAL -> 0;
         case SITTING -> 0;
         case MISSIONARY -> 0;
         case MATING_PRESS -> 0;
         case REVERSE_COWGIRL -> 0;
         case COWGIRL -> 0;
         case STRIP -> 0;
      };
   }

   private Vec3 requireSceneBedCenter() {
      if (this.sceneBedFootPos != null && this.sceneBedFacing != null) {
         Vec3 vec3 = Vec3.atBottomCenterOf(this.sceneBedFootPos).add(0.0, 0.5625, 0.0);
         Vec3 vec31 = vec3.add(Vec3.atLowerCornerOf(this.sceneBedFacing.getUnitVec3i()));
         return vec3.lerp(vec31, 0.5);
      } else {
         throw new IllegalStateException("Expected a bed anchor for action " + this.activeAction);
      }
   }

   private Direction requireSceneBedFacing() {
      if (this.sceneBedFacing == null) {
         throw new IllegalStateException("Expected a bed facing for action " + this.activeAction);
      } else {
         return this.sceneBedFacing;
      }
   }

   private static float directionYaw(Direction direction) {
      return switch (direction) {
         case SOUTH -> 0.0F;
         case WEST -> 90.0F;
         case NORTH -> 180.0F;
         case EAST -> -90.0F;
         default -> 0.0F;
      };
   }

   private static Vec3 horizontalForward(float yaw) {
      double d0 = Math.toRadians(yaw);
      return new Vec3(-Math.sin(d0), 0.0, Math.cos(d0));
   }

   private static Vec3 horizontalRight(float yaw) {
      Vec3 vec3 = horizontalForward(yaw);
      return new Vec3(vec3.z, 0.0, -vec3.x);
   }

   private GirlSceneAction resolvedActiveAction() {
      return this.level().isClientSide() ? this.trackedActiveAction().orElse(null) : this.activeAction;
   }

   private GirlScenePhase resolvedScenePhase() {
      return this.level().isClientSide() ? this.trackedScenePhase() : this.scenePhase;
   }

   private boolean resolvedThrusting() {
      return this.level().isClientSide() ? this.isThrustingTracked() : this.thrustTicks > 0;
   }

   private boolean resolvedIntroActive() {
      return this.level().isClientSide() ? this.isIntroActiveTracked() : this.isServerIntroActive();
   }

   private boolean isServerIntroActive() {
      return this.activeAction != null && this.scenePhase == GirlScenePhase.INTRO;
   }

   private boolean hasBuiltInScene() {
      return this.activeAction != null || this.scenePhase != GirlScenePhase.IDLE || this.coreState().activePlayerId() != null && !this.hasCustomSceneLock();
   }

   public record InteractionOption(String actionId, String labelKey, ItemStack price) {
   }

   public record InteractionSection(String labelKey, ItemStack pricePreview, List<InteractionOption> options) {
   }
}
