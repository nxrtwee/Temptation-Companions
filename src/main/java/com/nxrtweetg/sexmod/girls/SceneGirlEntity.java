package com.nxrtweetg.sexmod.girls;

import com.nxrtweetg.sexmod.girls.scene.GirlSceneLayoutConfig;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationController;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationLengths;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationRole;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationSet;
import com.nxrtweetg.sexmod.girls.anim.GirlSceneAnimations;
import com.nxrtweetg.sexmod.girls.anim.GirlSoundSet;
import com.nxrtweetg.sexmod.girls.scene.GirlBedTable;
import com.nxrtweetg.sexmod.girls.scene.GirlCustomPhase;
import com.nxrtweetg.sexmod.girls.scene.GirlEdgeApproach;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneKeyframe;
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
   /** How far forward the original walks the player when an approach starts (the {@code dc} handler). */
   private static final double APPROACH_PLAYER_NUDGE = 0.35;
   /**
    * How many ticks before a camera cut the server fires the fade signal.
    *
    * <p>Only the two cuts where the scene camera switches on or off get a fade now. The original has no
    * blackout at all: it teleports the player once and the camera never moves again, so every fade we
    * used to run mid-scene was covering a teleport of our own. Those teleports are gone - the layout is
    * built off the same stored origin she walked to - so the fades that hid them were the only thing
    * left to see.
    */
   private int fadeLeadTicks() {
      return com.nxrtweetg.sexmod.config.SexmodConfig.getFadeLeadTicks();
   }
   /**
    * The original's hardcoded sit-down length ({@code el.q(): al = 109}), used only if a character's
    * own take carries no {@code animation_length}.
    */
   private static final int SIT_DOWN_FALLBACK_TICKS = 109;
   private static final int STRIP_MODEL_REVEAL_TICKS = 96;
   private static final int STRIP_AUTO_FINISH_TICKS = 100;
   /** Only for a climax take that carries no {@code animation_length}; every shipped one does. */
   private static final int CLIMAX_FALLBACK_TICKS = 30;
   /**
    * How long past a phase's measured length the server waits for the animation's own keyframe before
    * giving up and advancing on the clock. The keyframe is meant to win; this only rescues a scene
    * whose animation ships no such keyframe, or whose owner stopped reporting.
    */
   private static final int KEYFRAME_WATCHDOG_TICKS = 100;
   /** Arrives once she is within 1.6 blocks of the spot beside the bed. */
   private static final double ARRIVAL_DISTANCE_SQR = 2.56;
   /** And gives up on the walk after this long ({@code ad > 200}). */
   private static final int WALK_BAIL_TICKS = 200;
   /**
    * Ours: how many times a stalled walk is re-pathed before she hands over where she stands. The
    * original re-issues the path twice on a fixed schedule and otherwise waits out the full ten seconds;
    * ground navigation here reports the walk finished the moment it runs out of path, so the same two
    * attempts are spent as soon as that happens instead. Without this she stood at the bed playing the
    * walk take until the bail tick, which is the "walking on the spot" this fixes.
    */
   private static final int WALK_STALL_REPATHS = 2;
   /** Long enough for the first path to be issued and taken up, so the walk cannot end on its own start. */
   private static final int WALK_STALL_GRACE_TICKS = 5;
   /**
    * The two ticks the original re-issues the path on ({@code ad != 60 && ad != 120} guards the only
    * other {@code func_75492_a} of the walk). The first one goes out when the phase starts, and nothing
    * touches navigation in between.
    */
   private static final int[] WALK_REPATH_TICKS = {30, 60, 90, 120};
   private static final double WALK_TO_BED_SPEED = 0.35;
   /** Prefix the owning client sends a structural keyframe under, on the shared scene-input channel. */
   public static final String KEYFRAME_INPUT_PREFIX = "kf:";
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
   /**
    * The original's {@code N()}: the beat is meant to start on the frame the pace changes, so it zeroed
    * its own tick offset and relayed a "reset your controller" packet to every viewer. Same trick as the
    * fade counter - a monotonic int, because the client has to notice a bump that happens twice in a row
    * and because re-playing the <em>same</em> take is invisible to {@code setAndContinue}.
    */
   private static final EntityDataAccessor<Integer> TRACKED_BEAT_RESET = SynchedEntityData.defineId(SceneGirlEntity.class, EntityDataSerializers.INT);
   /** Which custom (non-{@link GirlSceneAction}) interaction owns her, or "" for none. */
   private static final EntityDataAccessor<String> TRACKED_CUSTOM_ACTION = SynchedEntityData.defineId(SceneGirlEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<String> TRACKED_CUSTOM_PHASE = SynchedEntityData.defineId(SceneGirlEntity.class, EntityDataSerializers.STRING);
   protected GirlSceneAction activeAction;
   protected GirlScenePhase scenePhase = GirlScenePhase.IDLE;
   protected int climaxTicks;
   protected int sceneTicks;
   protected int thrustTicks;
   /** How many rescue paths the walk to the bed has spent, see {@link #repathToBed}. */
   private int walkRepathAttempts;
   /** Prone doggy cycles hard1..hard3 and never plays the same one twice in a row. */
   protected int hardVariant = 1;
   private int stripTargetModel = 0;
   private GirlSceneAction queuedActionAfterStrip;
   private BlockPos sceneBedFootPos;
   private Direction sceneBedFacing;
   /**
    * The half of the bed the table probed her spot against, and whether that spot is at one of the
    * bed's ends. Neither steers the layout any more - {@link #requireSceneBedOrigin()} measures from the
    * spot itself - but they stay for the dialogue and diagnostics that ask which bed she picked.
    */
   private BlockPos sceneBedPos;
   private boolean sceneBedAlongAxis;
   /** Where the bed table put her: the spot she walks to, and the rotation the layout is built on. */
   private Vec3 sceneBedStandPos;
   private float sceneBedStandYaw;
   /**
    * The stand-in for a bed anchor when a scene is played on the floor: the surface the pose is laid out
    * on, captured once when the scene starts so nothing drifts as she is moved onto it.
    *
    * <p>The original has no such thing because the girls who take the player to bed always look one up
    * first, while the ones who start their own scenes ({@code fn} storing {@code func_174791_d()} before
    * STARTDOGGY) simply play them where they stand. Both roads meet at the same three questions -
    * {@link #requireSceneBedOrigin()}, {@link #requireSceneBedYaw()} and {@link #sceneSurfaceY()} - so
    * the floor answers them instead of the mattress.
    */
   private Vec3 sceneFloorCenter;
   private float sceneFloorYaw;
   /** The meeting point the approach glide was aimed at, captured once when the scene started. */
   private Vec3 sceneApproachTarget;
   private double sceneTuneForward;
   private double sceneTuneSide;
   private double sceneTuneVertical;
   private boolean sceneFrozen;
   /** Incremented by server to signal an imminent snapTo transition; client triggers fade on change. */
   private int fadeSignalCounter;
   /** Incremented whenever the beat has to restart from frame 0 on the viewers. */
   private int beatResetCounter;
   /**
    * Whether the player is holding the pace key right now, as opposed to whether a take is still
    * running. The original read the key at the {@code *Ready} keyframe to decide on another take, so
    * releasing it lets the current beat play out instead of cutting it off mid-motion.
    */
   protected boolean paceHeld;
   private boolean approachFadeFired;
   private boolean paymentFadeFired;
   private boolean waitingFadeFired;
   private boolean introFadeFired;
   private boolean climaxFadeFired;
   private boolean customApproachFadeFired;
   /**
    * Ticks since that fade started. The original bound the player and then waited a fixed 20 ticks
    * before it moved anybody ({@code t(): af = 20} -> {@code u(): U()}), which is what keeps the screen
    * black over the teleport; a distance ring alone lets a player who is already at the bed see her
    * jump into the pose.
    */
   private int waitingFadeTicks;
   /** Client-side only: the last {@link #TRACKED_BEAT_RESET} value the action controller acted on. */
   private int lastSeenBeatReset;
   /** Server-side counter for the custom-interaction machine, in the same role as {@code sceneTicks}. */
   private int customTicks;
   /** The point the custom-interaction glide was aimed at, captured once when it started. */
   private Vec3 customApproachTarget;
   /**
    * Where the partner is held for a custom interaction that places him itself, captured once at the
    * bind. Not saved: a reload mid-interaction re-derives it from her own position.
    */
   private Vec3 customPartnerAnchor;
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
      builder.define(TRACKED_BEAT_RESET, 0);
      builder.define(TRACKED_CUSTOM_ACTION, "");
      builder.define(TRACKED_CUSTOM_PHASE, GirlCustomPhase.IDLE.id());
   }

   public void beginInteraction(ServerPlayer player, GirlSceneAction action) {
      this.activeAction = action;
      this.climaxTicks = 0;
      this.sceneTicks = 0;
      this.thrustTicks = 0;
      this.paceHeld = false;
      this.sceneFrozen = false;
      this.approachFadeFired = false;
      this.paymentFadeFired = false;
      this.waitingFadeFired = false;
      this.introFadeFired = false;
      this.climaxFadeFired = false;
      this.customApproachFadeFired = false;
      this.waitingFadeTicks = 0;
      this.queuedActionAfterStrip = null;
      this.loadSceneTune(action);
      this.coreState().setShouldGetDamage(false);
      this.stripTargetModel = action == GirlSceneAction.STRIP ? (this.coreState().currentModel() == 0 ? 1 : 0) : this.coreState().currentModel();
      GirlSceneStarter.beginOffer(this, player, action);

      if (this.shouldSkipApproachToBed(action)) {
         this.scenePhase = GirlScenePhase.WALKING;
         this.sceneApproachTarget = null;
         this.repathToBed(this.requireSceneBedStandPos(), true);
      } else {
         this.scenePhase = GirlScenePhase.APPROACH;
         this.sceneApproachTarget = player.position().add(horizontalForward(player.getYRot()));
         this.startApproachFacing(player);
      }

      this.applyActionAnimationFlags(action, this.scenePhase);
      this.syncTrackedState();
   }

   /**
    * Whether this scene skips the preliminary approach to the player and walks straight to the bed.
    * Only bed scenes can skip approaching the player: standing/kneeling scenes (blowjob, boobjob, etc.)
    * require the approach to establish the relative positioning in front of the player.
    * For bed scenes, if the character has no payment phase or if the scene is an "all scenes" extension,
    * walking up to the player just to immediately turn around and walk to the bed is redundant.
    */
   public boolean shouldSkipApproachToBed(GirlSceneAction action) {
      if (!this.requiresBed(action)) {
         return false;
      }
      return !this.hasPaymentPhase(action) || !this.sceneActions().contains(action);
   }

   public boolean hasActiveScene() {
      return this.hasBuiltInScene() || this.hasCustomSceneLock();
   }

   /**
    * What the horny potion does to her. Most of the roster ignores it - the original only ever polls the
    * effect from the slime's, the bee's and the kobold's own AI ticks - so the base is a no-op and the
    * ones who care override it.
    */
   public void applyHornyPotion() {
   }

   public void setSceneBedAnchor(GirlBedTable.Anchor anchor) {
      this.sceneBedFootPos = anchor.footPos();
      this.sceneBedFacing = anchor.facing();
      this.sceneBedPos = anchor.bedPos();
      this.sceneBedAlongAxis = anchor.alongBedAxis();
      this.sceneBedStandPos = anchor.standPos();
      this.sceneBedStandYaw = anchor.standYaw();
      this.sceneFloorCenter = null;
      this.sceneFloorYaw = 0.0F;
   }

   /**
    * Lays a bed scene out on the ground where she is standing instead of on a mattress, for a girl who
    * starts her own scenes and never looks for a bed ({@code fn.b()} stores her position and goes down
    * on the spot). The centre is captured once, so the every-tick re-alignment cannot walk it away.
    */
   public void setSceneFloorAnchor() {
      this.clearSceneBedAnchor();
      this.sceneFloorCenter = this.position();
      this.sceneFloorYaw = Mth.wrapDegrees(this.getYRot());
   }

   public void clearSceneBedAnchor() {
      this.sceneBedFootPos = null;
      this.sceneBedFacing = null;
      this.sceneBedPos = null;
      this.sceneBedAlongAxis = false;
      this.sceneBedStandPos = null;
      this.sceneBedStandYaw = 0.0F;
      this.sceneFloorCenter = null;
      this.sceneFloorYaw = 0.0F;
   }

   /** Whether the scene has a surface to lay itself out on, be it a bed or the floor she is standing on. */
   private boolean hasSceneAnchor() {
      return this.sceneBedFootPos != null || this.sceneFloorCenter != null;
   }

   /**
    * Whether right-clicking her opens the interaction menu at all. The girls who sell scenes do; one who
    * starts her own has no menu in the original either - {@code fn} implements neither
    * {@code func_184645_a} nor {@code b(EntityPlayer)}, so a right click on her does nothing.
    */
   public boolean hasInteractionMenu() {
      return true;
   }

   /**
    * Whether a click carrying this stack should skip the client-side menu and reach {@code mobInteract}
    * on the server. The menu hook answers SUCCESS for every click on a girl who has a menu, so a gift
    * that is meant to be handed over - the bee's chest - would otherwise never be seen by the server.
    */
   public boolean defersInteractionToServer(ItemStack held) {
      if (held.is(com.nxrtweetg.sexmod.item.ModItems.LASSO) && !com.nxrtweetg.sexmod.item.LassoItem.hasGirl(held)) {
         if (this instanceof com.nxrtweetg.sexmod.girls.allie.AllieEntity
               || this instanceof com.nxrtweetg.sexmod.girls.bee.BeeEntity
               || this instanceof com.nxrtweetg.sexmod.girls.slime.SlimeGirlEntity) {
            return false;
         }
         return true;
      }
      if (held.is(com.nxrtweetg.sexmod.item.ModItems.GALATH_COIN)) {
         return true;
      }
      return false;
   }

   /**
    * A constant nudge for the floating name, in blocks, for characters whose model does not fill the
    * hitbox the way Jenny's does. The scene-specific offsets stay in the renderer; this is the
    * character's own resting correction.
    */
   public double nameplateYOffset() {
      return 0.0;
   }

   /**
    * Starts a scene she decided on herself: no offer, no payment, no walk over and no approach glide.
    * The phase is the caller's because the entry point differs per scene - the original's grab goes
    * straight to the looping take of the blowjob, while her own doggy begins at the pose that leads
    * into the wait.
    */
   protected void beginSelfStartedScene(ServerPlayer player, GirlSceneAction action, GirlScenePhase phase) {
      this.activeAction = action;
      this.scenePhase = phase;
      this.climaxTicks = 0;
      this.sceneTicks = 0;
      this.thrustTicks = 0;
      this.paceHeld = false;
      this.sceneFrozen = false;
      this.waitingFadeFired = false;
      this.waitingFadeTicks = 0;
      this.climaxFadeFired = false;
      this.queuedActionAfterStrip = null;
      this.sceneApproachTarget = null;
      this.loadSceneTune(action);
      this.coreState().setShouldGetDamage(false);
      this.stripTargetModel = this.coreState().currentModel();
      GirlSceneStarter.beginOffer(this, player, action);
      this.prepareSceneLayout(player, action);
      // WAITING poses her and leaves him to close the distance himself, and so does the positioning take
      // that leads into it - the original's STARTDOGGY goes down on the spot with nobody bound yet. Every
      // other phase has them both in place from the first tick.
      if (phase != GirlScenePhase.WAITING && !(phase == GirlScenePhase.POSITIONING && this.waitsForPlayerApproach(action))) {
         this.alignSceneParticipants(player);
      }

      // she teleports onto her own layout, and the client should not see the jump
      this.fadeSignalCounter++;
      this.applyActionAnimationFlags(action, phase);
      this.syncTrackedState();
   }

   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack held = player.getItemInHand(hand);
      if (this.defersInteractionToServer(held)) {
         return InteractionResult.PASS;
      }
      return InteractionResult.SUCCESS;
   }

   public Optional<GirlSceneAction> trackedActiveAction() {
      String s = (String)this.getEntityData().get(TRACKED_ACTIVE_ACTION);
      if (!s.isEmpty()) {
         return GirlSceneAction.fromLegacyId(s);
      }
      String custom = (String)this.getEntityData().get(TRACKED_CUSTOM_ACTION);
      if ("headpat".equals(custom)) {
         return Optional.of(GirlSceneAction.HEAD_PAT);
      }
      return Optional.empty();
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

   public GirlScenePhase scenePhase() {
      return this.scenePhase;
   }

   public int thrustTicks() {
      return this.thrustTicks;
   }

   public int climaxTicks() {
      return this.climaxTicks;
   }

   public int hardVariant() {
      return this.hardVariant;
   }

   public boolean isPaceHeld() {
      return this.paceHeld;
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
      // before the super call, which forgets who she was busy with - the partner's gravity is restored
      // through that id
      this.releaseCustomPartner();
      super.resetScene();
      this.setNoGravity(false);
      this.activeAction = null;
      this.scenePhase = GirlScenePhase.IDLE;
      this.climaxTicks = 0;
      this.sceneTicks = 0;
      this.thrustTicks = 0;
      this.approachFadeFired = false;
      this.paymentFadeFired = false;
      this.waitingFadeFired = false;
      this.introFadeFired = false;
      this.climaxFadeFired = false;
      this.customApproachFadeFired = false;
      this.waitingFadeTicks = 0;
      this.queuedActionAfterStrip = null;
      this.clearSceneBedAnchor();
      this.sceneTuneForward = 0.0;
      this.sceneTuneSide = 0.0;
      this.sceneTuneVertical = 0.0;
      // a reset is unconditional: whichever of the two machines owns her, she goes back to idle
      this.getEntityData().set(TRACKED_CUSTOM_ACTION, "");
      this.setCustomPhase(GirlCustomPhase.IDLE);
      this.customApproachTarget = null;
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
         // Nothing to do for the name plate here. It used to be driven from this tick by toggling
         // setCustomNameVisible, which could never work: the flag only matters for a girl who has a
         // custom name, and a naturally spawned one has none, so every girl went through her whole life
         // with no label at all. The rule lives in GirlEntityRenderer.shouldShowName now, off the phase
         // that is already synced to the client.
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
      output.putString("GirlCustomAction", this.customActionId());
      output.putString("GirlCustomPhase", this.customPhase().id());
      if (this.sceneBedFootPos != null) {
         output.storeNullable("SceneBedFootPos", BlockPos.CODEC, this.sceneBedFootPos);
      }
      if (this.sceneBedPos != null) {
         output.storeNullable("SceneBedPos", BlockPos.CODEC, this.sceneBedPos);
      }
      if (this.sceneBedFacing != null) {
         output.putString("SceneBedFacing", this.sceneBedFacing.getName());
      }
      output.putBoolean("SceneBedAlongAxis", this.sceneBedAlongAxis);
      if (this.sceneBedStandPos != null) {
         output.putDouble("SceneBedStandX", this.sceneBedStandPos.x);
         output.putDouble("SceneBedStandY", this.sceneBedStandPos.y);
         output.putDouble("SceneBedStandZ", this.sceneBedStandPos.z);
      }
      output.putFloat("SceneBedStandYaw", this.sceneBedStandYaw);
      if (this.sceneFloorCenter != null) {
         output.putDouble("SceneFloorCenterX", this.sceneFloorCenter.x);
         output.putDouble("SceneFloorCenterY", this.sceneFloorCenter.y);
         output.putDouble("SceneFloorCenterZ", this.sceneFloorCenter.z);
      }
      output.putFloat("SceneFloorYaw", this.sceneFloorYaw);
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
      this.getEntityData().set(TRACKED_CUSTOM_ACTION, input.getStringOr("GirlCustomAction", ""));
      this.setCustomPhase(GirlCustomPhase.fromId(input.getStringOr("GirlCustomPhase", GirlCustomPhase.IDLE.id())));

      this.sceneBedFootPos = input.read("SceneBedFootPos", BlockPos.CODEC).orElse(null);
      this.sceneBedPos = input.read("SceneBedPos", BlockPos.CODEC).orElse(null);
      input.getString("SceneBedFacing").ifPresent(name -> this.sceneBedFacing = Direction.byName(name));
      this.sceneBedAlongAxis = input.getBooleanOr("SceneBedAlongAxis", false);
      double standX = input.getDoubleOr("SceneBedStandX", Double.NaN);
      if (!Double.isNaN(standX)) {
         this.sceneBedStandPos = new Vec3(
            standX,
            input.getDoubleOr("SceneBedStandY", 0.0),
            input.getDoubleOr("SceneBedStandZ", 0.0)
         );
      }
      this.sceneBedStandYaw = input.getFloatOr("SceneBedStandYaw", 0.0F);
      double floorX = input.getDoubleOr("SceneFloorCenterX", Double.NaN);
      if (!Double.isNaN(floorX)) {
         this.sceneFloorCenter = new Vec3(
            floorX,
            input.getDoubleOr("SceneFloorCenterY", 0.0),
            input.getDoubleOr("SceneFloorCenterZ", 0.0)
         );
      }
      this.sceneFloorYaw = input.getFloatOr("SceneFloorYaw", 0.0F);

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
         this.climaxTicks = this.climaxWatchdogTicks();
         this.coreState().setPlayerIsCumming(true);
         this.applyActionAnimationFlags(this.activeAction, GirlScenePhase.CLIMAX);
         this.syncTrackedState();
      } else if (this.scenePhase != GirlScenePhase.CLIMAX) {
         // prone doggy's intro waits for this very thrust, so it must not be gated behind the intro
         if (!this.isServerIntroActive() || this.introWaitsForThrust()) {
            // The pace key is down. That fact alone is what the original's keyframe handlers read live
            // ({@code if (n() && d3.d)}), so it has to be recorded before any of the state work below is
            // skipped - otherwise a key pressed again during a running beat stays invisible to the cue
            // that would have re-entered it, and prone doggy sits out the rest of the take.
            this.paceHeld = true;
            // prone doggy rolls a fresh hard take on entry, then rides its own 34-tick budget back
            // down to soft (original PRONE_DOGGY_HARD(..., 34, PRONE_DOGGY_SOFT), Action.java:140).
            // Re-entering while it still runs would restart the take mid-beat and eat a thrust; the
            // original's pace key is a no-op there too (e0.c(fp) has no successor for the soft loop or
            // the hard take, and em.b(fp) returns early for the state it is already in).
            if (this.introWaitsForThrust() && this.thrustTicks > 0) {
               return;
            }

            // Never the take that just played (original BiaEntity.ag(), :270-278). Only between
            // beats: rolling while one still runs would swap the animation mid-motion.
            boolean startsNewBeat = this.thrustTicks <= 0;
            if (this.scenePhase == GirlScenePhase.ACTIVE && startsNewBeat) {
               this.rollHardVariant(false);
            }

            this.coreState().setPlayerIsThrusting(true);
            if (startsNewBeat) {
               this.thrustTicks = this.scenePhase == GirlScenePhase.INTRO ? this.introAnimationTicks() : this.thrustDurationTicks();
               if (this.scenePhase == GirlScenePhase.INTRO) {
                  this.sceneTicks = 0;
                  this.introFadeFired = false;
               }
               this.beatResetCounter++;
            }
            if (this.activeAction == GirlSceneAction.STRIP) {
               this.coreState().setCurrentModel(0);
            }

            this.applyActionAnimationFlags(this.activeAction, this.scenePhase);
            this.syncTrackedState();
         }
      }
   }

   /**
    * The pace key went up. The take that is already running plays to its own end keyframe; only the
    * decision to start another one is withdrawn, so a released key never cuts a beat off mid-motion.
    */
   public void applyPaceRelease(ServerPlayer player) {
      if (this.coreState().activePlayerId() != null && this.coreState().activePlayerId().equals(player.getUUID())) {
         this.paceHeld = false;
      }
   }

   public void forceResetFromOwner(ServerPlayer player) {
      if (this.coreState().activePlayerId() != null && this.coreState().activePlayerId().equals(player.getUUID())) {
         this.fadeSignalCounter++;
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
      // Every one of these blends, and every one of them plays takes that hold on their last frame, so
      // they all want GirlAnimationController's "blend out of the pose that is on screen" fix rather
      // than GeckoLib's bind pose.
      controllers.add(
         new GirlAnimationController<SceneGirlEntity>("eyes", 5, state -> state.setAndContinue(this.selectEyesAnimation()))
            .setSoundKeyframeHandler(this::handleSoundKeyframe)
      );
      controllers.add(
         new GirlAnimationController<SceneGirlEntity>("movement", 5, state -> {
            boolean moving = state.isMoving();
            if (moving) {
               double dx = this.getX() - this.xo;
               double dz = this.getZ() - this.zo;
               double displacement = Math.sqrt(dx * dx + dz * dz);
               double speed = Math.clamp(displacement / 0.12, 0.6, 3.0);
               state.controller().setAnimationSpeed(speed);
            } else {
               state.controller().setAnimationSpeed(1.0);
            }
            return state.setAndContinue(this.selectMovementAnimation(moving));
         })
            .setSoundKeyframeHandler(this::handleSoundKeyframe)
      );
      controllers.add(
         new GirlAnimationController<SceneGirlEntity>("combat", 3, state -> state.setAndContinue(this.selectCombatAnimation()))
            .setSoundKeyframeHandler(this::handleSoundKeyframe)
      );
      controllers.add(
         new GirlAnimationController<SceneGirlEntity>("action", GirlAnimationRole.DEFAULT_TRANSITION_TICKS, state -> {
            boolean flag = this.consumeBeatReset(state.controller());
            RawAnimation rawanimation = this.selectActionAnimation();
            this.applyTakeTransition(state.controller(), rawanimation, flag);
            state.controller().setAnimationSpeed(
               (this.activeAction == GirlSceneAction.PRONE_DOGGY && this.resolvedThrusting()) ? 1.35 : 1.0
            );
            return state.setAndContinue(rawanimation);
         })
            .setSoundKeyframeHandler(this::handleSoundKeyframe)
      );
   }

   /**
    * Gives the action controller the blend length the take asks for, the way the original set
    * {@code transitionLengthTicks} from the scene state every frame. A flat value made the intros and
    * climaxes ease in when they are meant to snap, and cut the long loops over too fast.
    *
    * @param restarted whether the beat reset just dropped the controller. A blend needs a pose to blend
    *     out of, and a dropped controller has none: GeckoLib then eases out of the model's bind pose
    *     instead ({@code AnimationProcessor.findResetPointValue}), which showed up as her snapping
    *     upright for two or three frames on every single thrust. A restarted beat is meant to be
    *     instant anyway, so it gets no blend at all.
    */
   private void applyTakeTransition(AnimationController<SceneGirlEntity> controller, RawAnimation animation, boolean restarted) {
      if (animation == null || animation.getAnimationStages().isEmpty()) {
         return;
      }

      int ticks = restarted ? 0 : GirlAnimationRole.transitionTicksFor(animation.getAnimationStages().getFirst().animationName());
      if (controller.getTransitionTicks() != ticks) {
         controller.setTransitionTicks(ticks);
      }
   }

   /**
    * The viewer side of the original's {@code N()}/ResetController pair. Dropping the controller makes
    * the next {@code setAndContinue} treat the take as brand new, so it starts on frame 0 even when it
    * is the same animation that was already playing.
    *
    * @return whether the controller was dropped this frame, so the caller can suppress its blend.
    */
   private boolean consumeBeatReset(AnimationController<SceneGirlEntity> controller) {
      int signal = this.getEntityData().get(TRACKED_BEAT_RESET);
      if (signal == this.lastSeenBeatReset) {
         return false;
      } else {
         this.lastSeenBeatReset = signal;
         controller.reset();
         return true;
      }
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
            // the stored point, not a fresh one: a target that keeps moving with him is why she used to
            // trail behind and only ever arrive on the 40-tick timeout
            if (this.sceneApproachTarget == null) {
               this.sceneApproachTarget = serverplayer.position().add(horizontalForward(playerYaw));
            }

            Vec3 meetingPoint = this.sceneApproachTarget;
            double dx = this.getX() - meetingPoint.x;
            double dz = this.getZ() - meetingPoint.z;
            double dist2 = dx * dx + dz * dz;
            if (!this.approachFadeFired && this.sceneTicks >= APPROACH_GLIDE_TICKS - this.fadeLeadTicks()) {
               this.approachFadeFired = true;
               this.fadeSignalCounter++;
               this.syncTrackedState();
            }

            boolean arriving = dist2 <= 0.01 || this.sceneTicks >= APPROACH_GLIDE_TICKS;
            if (arriving) {
               this.getNavigation().stop();
               this.setNoGravity(false);
               this.sceneApproachTarget = null;
               this.coreState().setTargetYaw(Mth.wrapDegrees(playerYaw + 180.0F));
               this.holdSceneYaw();
               if (!this.hasPaymentPhase(this.activeAction)) {
                  this.finishPaymentPhase(serverplayer);
               } else {
                  this.scenePhase = GirlScenePhase.PAYMENT;
                  this.sceneTicks = 0;
               }
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
            int payTotal = this.paymentTicks();
            if (!this.paymentFadeFired && this.sceneTicks >= payTotal - this.fadeLeadTicks()) {
               this.paymentFadeFired = true;
               this.fadeSignalCounter++;
               this.syncTrackedState();
            }
            if (this.sceneTicks >= payTotal) {
               this.finishPaymentPhase(serverplayer);
            }
         } else if (this.scenePhase == GirlScenePhase.CLIMAX) {
            if (!this.climaxFadeFired && this.climaxTicks <= this.fadeLeadTicks()) {
               this.climaxFadeFired = true;
               this.fadeSignalCounter++;
               this.syncTrackedState();
            }
            if (--this.climaxTicks <= 0) {
               this.finishScene(serverplayer);
            } else {
               this.alignSceneParticipants(serverplayer);
               this.syncTrackedState();
            }
         } else if (this.scenePhase == GirlScenePhase.WAITING) {
            this.applyActionAnimationFlags(this.activeAction, this.scenePhase);
            this.holdSceneYaw();
            this.getNavigation().stop();
            if (this.hasSceneAnchor()) {
               this.setDeltaMovement(Vec3.ZERO);
               this.setNoGravity(true);
            }
            Vec3 approachTarget = this.hasSceneAnchor() ? this.requireSceneBedOrigin() : this.position();
            double px = serverplayer.getX() - approachTarget.x;
            double pz = serverplayer.getZ() - approachTarget.z;
            double playerDist2 = px * px + pz * pz;
            if (!this.waitingFadeFired && playerDist2 <= 6.25) {
               this.waitingFadeFired = true;
               this.waitingFadeTicks = 0;
               this.fadeSignalCounter++;
               serverplayer.setDeltaMovement(Vec3.ZERO);
               this.syncTrackedState();
            } else if (this.waitingFadeFired) {
               this.waitingFadeTicks++;
               serverplayer.setDeltaMovement(Vec3.ZERO);
               if (this.waitingFadeTicks >= this.fadeLeadTicks()) {
                  this.scenePhase = this.requiresIntroPhase() ? GirlScenePhase.INTRO : GirlScenePhase.ACTIVE;
                  this.sceneTicks = 0;
                  this.thrustTicks = 0;
                  this.introFadeFired = false;
                  this.loadSceneTune(this.activeAction);
                  this.prepareSceneLayout(serverplayer, this.activeAction);
                  this.alignSceneParticipants(serverplayer);
                  this.applyActionAnimationFlags(this.activeAction, this.scenePhase);
               }
            }

            this.syncTrackedState();
         } else if (this.scenePhase == GirlScenePhase.WALKING) {
            // She walks to the bed on her own feet; the lying-down animation only starts once she is
            // there, otherwise it gets cut off mid-way and she snaps back to the default pose. The
            // target is the spot the bed table picked - a whole block beside the bed, so it is
            // reachable on foot. Pathing at the mattress itself never completes, which is what left
            // her treading water in front of the bed.
            this.sceneTicks++;
            if (this.sceneTicks > 2400) {
               this.resetScene();
               return;
            }
            Vec3 bedTarget = this.requireSceneBedStandPos();
            // horizontal only: the bed centre sits above floor level, so a 3D check never matches
            double dx = this.getX() - bedTarget.x;
            double dz = this.getZ() - bedTarget.z;
            if (this.hasArrivedAtBed(dx * dx + dz * dz)) {
               this.getNavigation().stop();
               this.sceneTicks = 0;
               if (this.sitsDownToWait(this.activeAction)) {
                  // she sits on the bed and stays there until he walks up, so she takes the
                  // spot the bed table picked rather than the scene's own offset (original el.q():
                  // c(vec3d) + b(n2) immediately before SITDOWN). A character with no sit-down take
                  // skips straight to holding.
                  this.snapToBedAnchor();
                  this.scenePhase = this.requiredPositioningTicks() > 0 ? GirlScenePhase.POSITIONING : GirlScenePhase.WAITING;
               } else {
                  this.scenePhase = GirlScenePhase.POSITIONING;
                  this.prepareSceneLayout(serverplayer, this.activeAction);
               }
               this.loadSceneTune(this.activeAction);
            } else {
               this.getLookControl().setLookAt(bedTarget.x, this.getEyeY(), bedTarget.z);
               this.repathToBed(bedTarget, false);
            }

            this.applyActionAnimationFlags(this.activeAction, this.scenePhase);
            this.syncTrackedState();
         } else if (this.scenePhase == GirlScenePhase.POSITIONING && this.waitsForPlayerApproach()) {
            // she is on the bed now: play doggygoonbed to the end, then hold the pose and wait
            this.sceneTicks++;
            this.holdSceneYaw();
            this.setDeltaMovement(Vec3.ZERO);
            this.setNoGravity(true);
            // No fade here any more: positioning ends where it started, on the stored origin, and the
            // camera stays on across the transition, so there is nothing left to cover. The blackout
            // that used to run here is what the player still saw as a "visible transition".
            if (this.sceneTicks >= this.positioningWatchdogTicks()) {
               this.finishPositioningPhase(serverplayer);
            }

            this.applyActionAnimationFlags(this.activeAction, this.scenePhase);
            this.syncTrackedState();
         } else {
            this.alignSceneParticipants(serverplayer);
            if (this.scenePhase == GirlScenePhase.POSITIONING) {
               this.sceneTicks++;
               if (this.sceneTicks >= this.positioningWatchdogTicks()) {
                  this.finishPositioningPhase(serverplayer);
               }
            } else if (this.scenePhase == GirlScenePhase.INTRO) {
               if (this.introWaitsForThrust() && this.thrustTicks <= 0) {
                  this.holdSceneYaw();
                  this.syncTrackedState();
                  return;
               }

               this.sceneTicks++;
               int introTotal = this.introAnimationTicks();
               if (!this.introFadeFired && (introTotal <= this.fadeLeadTicks() || this.sceneTicks >= introTotal - this.fadeLeadTicks())) {
                  this.introFadeFired = true;
                  this.fadeSignalCounter++;
                  this.syncTrackedState();
               }

               if (this.sceneTicks >= introTotal) {
                  this.finishIntroPhase(serverplayer);
               }
            } else if (this.activeAction == GirlSceneAction.STRIP) {
               this.sceneTicks++;
               if (this.sceneTicks >= STRIP_MODEL_REVEAL_TICKS && this.coreState().currentModel() != this.stripTargetModel) {
                  this.coreState().setCurrentModel(this.stripTargetModel);
                  this.syncTrackedState();
               }

               if (this.sceneTicks >= this.stripWatchdogTicks()) {
                  this.finishStripPhase(serverplayer);
               }
            } else {
               if (this.thrustTicks > 0) {
                  this.thrustTicks--;
               }

               if (this.coreState().playerIsThrusting() && this.thrustTicks <= 0) {
                  // A beat take that names a re-entry or an end keyframe is paced by that keyframe. One
                  // that names neither ends here, on the countdown, and has to do the chaining itself:
                  // otherwise a held key drops her back to the slow loop until the client's next repeat
                  // arrives, so every thrust is separated by a few frames of the wrong take.
                  if (this.scenePhase == GirlScenePhase.ACTIVE && this.paceHeld && !this.beatEndsOnKeyframe()) {
                     this.rollHardVariant(true);
                     this.thrustTicks = this.thrustDurationTicks();
                     this.beatResetCounter++;
                  } else {
                     this.coreState().setPlayerIsThrusting(false);
                  }
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

   /**
    * The transitions out of each phase, factored out of {@link #tickServerScene}. Both authorities call
    * these: the animation's own keyframe by way of {@link #applySceneKeyframe}, and the per-phase tick
    * budget as a watchdog when that keyframe never arrives. They are idempotent in practice because
    * every caller checks the phase first and each one moves the phase on.
    */
   /**
    * Points navigation at the spot beside the bed, once when the walk starts and then only on the two
    * rescue ticks the original uses.
    *
    * <p>Asking for a path every tick is what had her marching on the spot for the first couple of
    * seconds: a fresh {@code moveTo} rebuilds the path from where she is standing, so a target one
    * block away is re-approached from scratch every tick and she never gets to finish the step. The
    * displacement stays tiny but it is displacement, and the walk take runs on displacement (see
    * {@code GirlEntityRenderer.isDisplacing}), so the walk kept playing over it.
    */
   private void repathToBed(Vec3 bedTarget, boolean starting) {
      boolean shouldRepath = starting;

      if (!shouldRepath && this.sceneTicks % 60 == 0) {
         shouldRepath = true;
      }

      if (shouldRepath) {
         this.getNavigation().moveTo(bedTarget.x, bedTarget.y, bedTarget.z, WALK_TO_BED_SPEED);
      }
   }

   private void finishPaymentPhase(ServerPlayer player) {
      this.sceneTicks = 0;
      this.scenePhase = this.nextScenePhaseAfterPayment();
      this.loadSceneTune(this.activeAction);
      if (this.scenePhase == GirlScenePhase.WALKING) {
         this.repathToBed(this.requireSceneBedStandPos(), true);
      } else {
         this.prepareSceneLayout(player, this.activeAction);
      }

      this.applyActionAnimationFlags(this.activeAction, this.scenePhase);
      this.syncTrackedState();
   }

   private void finishPositioningPhase(ServerPlayer player) {
      this.scenePhase = this.nextScenePhaseAfterPositioning();
      this.sceneTicks = 0;
      this.loadSceneTune(this.activeAction);
      // WAITING means she holds the pose until he walks up, so she must not be dragged into place yet;
      // nextScenePhaseAfterPositioning() returns it exactly when waitsForPlayerApproach() does. The
      // scenes she waits for him sitting go one step further: their waiting pose belongs to the bed
      // anchor rather than to the scene layout, so she keeps the spot she walked to for the whole wait.
      if (this.scenePhase != GirlScenePhase.WAITING || !this.sitsDownToWait(this.activeAction)) {
         this.prepareSceneLayout(player, this.activeAction);
      }

      if (this.scenePhase != GirlScenePhase.WAITING) {
         this.alignSceneParticipants(player);
      }

      this.applyActionAnimationFlags(this.activeAction, this.scenePhase);
      this.syncTrackedState();
   }

   private void finishIntroPhase(ServerPlayer player) {
      this.scenePhase = GirlScenePhase.ACTIVE;
      this.sceneTicks = 0;
      this.thrustTicks = 0;
      this.coreState().setPlayerIsThrusting(false);
      this.loadSceneTune(this.activeAction);
      this.prepareSceneLayout(player, this.activeAction);
      this.alignSceneParticipants(player);
      this.applyActionAnimationFlags(this.activeAction, this.scenePhase);
      this.syncTrackedState();
   }

   private void finishStripPhase(ServerPlayer player) {
      if (this.queuedActionAfterStrip == null) {
         this.finishScene(player);
      } else {
         this.activeAction = this.queuedActionAfterStrip;
         this.queuedActionAfterStrip = null;
         this.scenePhase = this.requiredPositioningTicks() <= 0
            ? (this.requiresIntroPhase() ? GirlScenePhase.INTRO : GirlScenePhase.ACTIVE)
            : GirlScenePhase.POSITIONING;
         this.sceneTicks = 0;
         this.loadSceneTune(this.activeAction);
         this.prepareSceneLayout(player, this.activeAction);
         this.alignSceneParticipants(player);
         this.applyActionAnimationFlags(this.activeAction, this.scenePhase);
         this.syncTrackedState();
      }
   }

   private void finishScene(ServerPlayer player) {
      player.sendSystemMessage(Component.literal(this.getName().getString() + " scene finished: " + this.activeAction.legacyId()));
      this.resetScene();
   }


   /**
    * How long the intro take runs, measured from the character's own file. The old table was Jenny's
    * numbers rounded by hand and was short for two of them - sitting's intro is 118 ticks against the
    * 72 assumed here, touch boobs' 215 against 130 - so those scenes were cut off mid-intro, and every
    * character whose take is a different length was cut off with them.
    *
    * <p>The thrust-waiting scenes budget the insert take rather than the loop they wait in, because
    * that is the take this counts: the intro only starts counting once the thrust has begun.
    */
   private int introAnimationTicks() {
      return this.phaseTakeTicks(GirlScenePhase.INTRO, this.introWaitsForThrust(), this.fallbackIntroAnimationTicks());
   }

   /** Used only when neither she nor Jenny ships the take; keeps the pre-measurement behaviour. */
   private int fallbackIntroAnimationTicks() {
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
         // sex_start runs 8.24s and ends on its own sex_startDone; this is only the watchdog
         case BEE_SEX -> 165;
         case NELSON -> 41;
         case STRIP, HEAD_PAT, THREESOME -> 0;
      };
   }

   private int introWatchdogTicks() {
      return this.introAnimationTicks()
         + this.watchdogGraceTicks(GirlScenePhase.INTRO, this.introWaitsForThrust(), GirlSceneKeyframe.INTRO_END);
   }

   private boolean requiresIntroPhase() {
      return switch (this.activeAction) {
         case BLOWJOB, KOBOLD_BLOWJOB, DEEPTHROAT, FACEFUCK, BOOBJOB, TOUCH_BOOBS, DOGGY, PRONE_DOGGY, ANAL, ANAL_ALT, KOBOLD_ANAL, SITTING, SITTING_ALT, MISSIONARY, MATING_PRESS, REVERSE_COWGIRL, COWGIRL, BEE_SEX, NELSON -> true;
         case STRIP, HEAD_PAT, THREESOME -> false;
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
      int ticks = GirlAnimationLengths.ticks(this.animations().namespace(), role, this.fallbackThrustDurationTicks());
      if (this.activeAction == GirlSceneAction.PRONE_DOGGY) {
         return 20;
      }
      // A beat whose take names no keyframe ends on this countdown alone, so it carries the take's blend
      // for the same reason the phase watchdogs do (see watchdogGraceTicks): the blend is a stage in
      // front of the animation, so the take is still that many ticks from its last frame when a clock
      // measured from the bare length runs out. A keyframe-paced beat needs none - its cue lands well
      // inside the take, and a re-entered take is given no blend at all (applyTakeTransition).
      return this.endsOnBeatKeyframe(role) ? ticks : ticks + this.blendTicks(role);
   }

   /** Whether a beat take carries either of the two cues that pace it, so the countdown is only a watchdog. */
   private boolean endsOnBeatKeyframe(GirlAnimationRole role) {
      if (role == null || role == GirlAnimationRole.NULL) {
         return false;
      }

      String namespace = this.animations().namespace();
      return GirlAnimationLengths.endsOnKeyframe(namespace, role, GirlSceneKeyframe.BEAT_END)
         || GirlAnimationLengths.endsOnKeyframe(namespace, role, GirlSceneKeyframe.BEAT_HOLD);
   }

   private int blendTicks(GirlAnimationRole role) {
      return role == null || role == GirlAnimationRole.NULL ? 0 : role.transitionTicks();
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
         case BEE_SEX -> 34;
         case NELSON -> 31;
         case THREESOME -> 16;
         case STRIP, HEAD_PAT -> 0;
      };
   }

   /**
    * The measured length of the take a phase plays, falling back to {@code fallback} when the phase
    * names no take of its own or the character ships neither it nor Jenny's copy of it.
    */
   private int phaseTakeTicks(GirlScenePhase phase, boolean thrusting, int fallback) {
      return this.takeTicks(GirlSceneAnimations.role(this.activeAction, phase, thrusting), fallback);
   }

   private int takeTicks(GirlAnimationRole role, int fallback) {
      return role == null || role == GirlAnimationRole.NULL
         ? fallback
         : GirlAnimationLengths.ticks(this.animations().namespace(), role, fallback);
   }

   /**
    * How long past its measured length a phase may keep waiting for the animation's own keyframe.
    *
    * <p>The keyframe is the authority and the clock is only a watchdog, so a take that names its own end
    * gets the full grace. A take that does not name it has nothing to wait for, and waiting anyway runs
    * it past its last frame: a looping one then snaps back to its first, which is her dropping out of
    * the pose into a standing one for a fifth of a second before the phase finally turns over. Three
    * takes are in that position - the reverse cowgirl intro and both insert takes - and they are exactly
    * the scenes that were coming apart at the seams between phases.
    *
    * <p>Either way the blend the take asks for is added on top, because the phase clock starts when the
    * state changes and the take does not: GeckoLib puts a transition stage of {@code transitionTicks}
    * <em>in front</em> of the animation on the controller's timeline ({@code AnimationTimeline.create}),
    * so a take entered with a five-tick blend still has its last quarter second to play when a clock
    * measured from its bare length runs out. That is the last of the early cuts across the phases.
    */
   private int watchdogGraceTicks(GirlScenePhase phase, boolean thrusting, GirlSceneKeyframe endSignal) {
      return this.watchdogGraceTicks(GirlSceneAnimations.role(this.activeAction, phase, thrusting), endSignal);
   }

   private int watchdogGraceTicks(GirlAnimationRole role, GirlSceneKeyframe endSignal) {
      if (role == null || role == GirlAnimationRole.NULL) {
         return KEYFRAME_WATCHDOG_TICKS;
      }

      int blend = this.blendTicks(role);
      return GirlAnimationLengths.endsOnKeyframe(this.animations().namespace(), role, endSignal)
         ? KEYFRAME_WATCHDOG_TICKS + blend
         : blend;
   }

   /**
    * Whether the beat that is running names its own end, so the keyframe is the authority that carries
    * one beat into the next while the pace is held.
    *
    * <p>Prone doggy's hard takes do name it: {@code doggySwitch} enters the hard beat from the soft loop
    * and {@code doggyReset} ends it, which is exactly how the original paced that state - its pace key
    * had no successor for {@code PRONE_DOGGY_SOFT} at all ({@code e0.c(fp)}), so the cues were the only
    * way in and out. Takes that name neither fall back to the countdown below, or the beat would be
    * followed by the slow loop until the client's repeat comes round.
    */
   private boolean beatEndsOnKeyframe() {
      return this.endsOnBeatKeyframe(GirlSceneAnimations.role(this.activeAction, GirlScenePhase.ACTIVE, true, this.hardVariant));
   }

   /** Her payment take, measured: Jenny's runs 186 ticks, not the 184 that used to be assumed. */
   private int paymentTicks() {
      return this.takeTicks(GirlAnimationRole.PAYMENT, PAYMENT_AUTO_FINISH_TICKS);
   }

   private int paymentWatchdogTicks() {
      return this.paymentTicks() + this.watchdogGraceTicks(GirlAnimationRole.PAYMENT, GirlSceneKeyframe.PAYMENT_END);
   }

   private int stripTicks() {
      return this.takeTicks(GirlAnimationRole.STRIP, STRIP_AUTO_FINISH_TICKS);
   }

   private int stripWatchdogTicks() {
      return this.stripTicks() + this.watchdogGraceTicks(GirlAnimationRole.STRIP, GirlSceneKeyframe.STRIP_END);
   }

   /**
    * How long the climax runs. This was a flat 30 ticks for every scene, which is shorter than all but
    * two of the takes and is the single biggest reason scenes ended early: doggy's climax is 134 ticks,
    * the cowgirl's 307, and both were being cut a second and a half in. Every climax take ships its own
    * {@code *cumDone}, so the keyframe still ends it and this only has to stop being the thing that does.
    */
   private int climaxWatchdogTicks() {
      return this.phaseTakeTicks(GirlScenePhase.CLIMAX, false, CLIMAX_FALLBACK_TICKS)
         + this.watchdogGraceTicks(GirlScenePhase.CLIMAX, false, GirlSceneKeyframe.CLIMAX_END);
   }

   private GirlScenePhase nextScenePhaseAfterPayment() {
      if (this.shouldAutoStripBeforeBreastScene()) {
         this.queuedActionAfterStrip = this.activeAction;
         this.stripTargetModel = 0;
         this.activeAction = GirlSceneAction.STRIP;
         return GirlScenePhase.ACTIVE;
      } else if (this.activeAction == GirlSceneAction.STRIP) {
         return GirlScenePhase.ACTIVE;
      } else if (this.waitsForPlayerApproach()) {
         // walk over first, then lie down: doggygoonbed must not play while she is still moving. This
         // has to be asked before the zero-length check below, because a character who ships no
         // sit-down take still has to make the walk - she just holds instead of sitting down.
         return GirlScenePhase.WALKING;
      } else if (this.requiredPositioningTicks() <= 0) {
         return this.requiresIntroPhase() ? GirlScenePhase.INTRO : GirlScenePhase.ACTIVE;
      } else {
         return GirlScenePhase.POSITIONING;
      }
   }

   private GirlScenePhase nextScenePhaseAfterPositioning() {
      if (this.waitsForPlayerApproach()) {
         return GirlScenePhase.WAITING;
      }

      return this.requiresIntroPhase() ? GirlScenePhase.INTRO : GirlScenePhase.ACTIVE;
   }

   /**
    * Picks a new take for the beat about to play, never the one that just played (original
    * BiaEntity.ag()). Scenes ship a different number of takes — three for prone doggy, four for
    * facefuck — so the count comes from the table rather than a constant.
    */
   /**
    * Picks the take for the next beat, never the one that just played.
    *
    * @param reentry whether a beat is already running. The distinction only matters for a scene whose
    *     first take is an entry rather than a loop: the original enters reverse cowgirl's beat on
    *     {@code REVERSE_COWGIRL_FAST_START} and from then on rolls only among the three
    *     {@code FAST_CONTINUES} takes, so the entry motion is never seen again mid-beat.
    */
   private void rollHardVariant(boolean reentry) {
      int takes = GirlSceneAnimations.variantCount(this.activeAction);
      if (takes < 2) {
         this.hardVariant = 1;
         return;
      }

      int first = GirlSceneAnimations.beatEntersOnFirstTake(this.activeAction) && takes > 2 ? 2 : 1;
      if (!reentry) {
         this.hardVariant = first > 1 ? 1 : this.random.nextInt(takes) + 1;
         return;
      }

      int span = takes - first + 1;
      int previous = this.hardVariant;
      int next = previous;
      while (next == previous) {
         next = first + this.random.nextInt(span);
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
    * <p>Only scenes that actually ship a holding animation can do this: doggy has its own, anal reuses
    * {@code anal_wait}, and prone doggy waits sitting on the bed for a character who ships that pair.
    * The rest would freeze on a pose they do not own - which is why the five sit-on-the-edge scenes
    * join them only for a character who ships a take to wait in, see {@link #edgeApproach()}.
    */
   private boolean waitsForPlayerApproach(GirlSceneAction action) {
      if (!this.requiresBed(action)) {
         return false;
      }
      return action == GirlSceneAction.DOGGY
         || action == GirlSceneAction.PRONE_DOGGY
         || action == GirlSceneAction.SITTING_ALT
         || action == GirlSceneAction.ANAL
         || action == GirlSceneAction.ANAL_ALT
         || this.sitsDownToWait(action);
   }

   /**
    * How she waits at the bed when a sit-on-the-edge scene starts, or null when she ships no take for
    * it and the scene begins where the player stands, as it always has here. The original walks her to
    * the bed, plays her sit-down take once and then holds until he closes in (Ellie's
    * {@code q() -> j() -> t() -> u()}); Luna owns only the hold and Jenny neither, so this belongs to
    * the character rather than to the scene.
    */
   protected GirlEdgeApproach edgeApproach() {
      return GirlEdgeApproach.SIT_DOWN;
   }

   /**
    * True when this scene's walk-up wait is spent sitting on the bed, which needs the character to ship
    * a take for it.
    *
    * <p>Two families qualify. The five sit-on-the-edge scenes use the bed table's edge spot and sit
    * there by construction. Prone doggy joins them without the edge geometry: the original dispatches
    * its menu entry straight into the same pair of takes ({@code e0.a("doggy") -> b(fp.SITDOWN)}), then
    * holds in {@code SITDOWNIDLE} until the player is within a block and only there sets
    * {@code PRONE_DOGGY_INTRO}, moving both participants onto the mattress. Its bed spot stays the
    * strict full-block one, so this cannot be read off the bed variant.
    */
   private boolean sitsDownToWait(GirlSceneAction action) {
      if (this.edgeApproach() == null) {
         return false;
      } else if (action == GirlSceneAction.PRONE_DOGGY || action == GirlSceneAction.SITTING_ALT) {
         return true;
      }

      return GirlBedTable.variantFor(action).filter(variant -> variant == GirlBedTable.Variant.EDGE).isPresent();
   }

   public boolean requiresBed(GirlSceneAction action) {
      return GirlBedTable.variantFor(action).isPresent();
   }

   /**
    * Which take covers the walk-up wait of a scene she waits for him sitting on the bed. The shared
    * table cannot answer this: the same scene is a sit-down for Bia and Ellie and a standing hold for
    * Luna.
    */
   private GirlAnimationRole edgeApproachRole(GirlSceneAction action, GirlScenePhase phase) {
      if (!this.sitsDownToWait(action)) {
         return null;
      }

      GirlEdgeApproach approach = this.edgeApproach();
      return switch (phase) {
         case POSITIONING -> approach.sitDown();
         case WAITING -> approach.hold();
         default -> null;
      };
   }

   /**
    * How long her sit-down take runs, measured from her own file rather than taken from the original's
    * hardcoded 109, so a character whose take is a different length still hands over on its last frame.
    * Zero when she has no sit-down take at all: Luna walks to the bed and holds without one.
    */
   private int sitDownTicks() {
      GirlEdgeApproach approach = this.edgeApproach();
      if (approach == null || approach.sitDown() == null) {
         return 0;
      }

      if (this.animations().hasOwn(approach.sitDown())) {
         return GirlAnimationLengths.ticks(this.animations().namespace(), approach.sitDown(), SIT_DOWN_FALLBACK_TICKS);
      }
      String preferredOwner = (this.activeAction == GirlSceneAction.COWGIRL || this.activeAction == GirlSceneAction.MISSIONARY) ? "ellie" : "bia";
      return GirlAnimationLengths.ticks(preferredOwner, approach.sitDown(), SIT_DOWN_FALLBACK_TICKS);
   }

   private RawAnimation resolveEdgeApproachAnimation(GirlSceneAction action, GirlAnimationRole role) {
      if (this.animations().hasOwn(role)) {
         return this.animation(role);
      }
      String preferredOwner;
      if (action == GirlSceneAction.COWGIRL || action == GirlSceneAction.MISSIONARY) {
         preferredOwner = "ellie";
      } else if (action == GirlSceneAction.SITTING || action == GirlSceneAction.SITTING_ALT || action == GirlSceneAction.PRONE_DOGGY || action == GirlSceneAction.REVERSE_COWGIRL) {
         preferredOwner = "bia";
      } else {
         preferredOwner = "ellie";
      }
      String id = "animation." + preferredOwner + "." + role.suffix();
      return role.looping() ? RawAnimation.begin().thenLoop(id) : RawAnimation.begin().thenPlayAndHold(id);
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
      return this.trackedActiveAction().filter(this::waitsForPlayerApproach).isPresent();
   }

   private boolean shouldAutoStripBeforeBreastScene() {
      return (this.activeAction == GirlSceneAction.TOUCH_BOOBS || this.activeAction == GirlSceneAction.BOOBJOB)
         && this.coreState().currentModel() != 0;
   }

   protected void applyActionAnimationFlags(GirlSceneAction action, GirlScenePhase phase) {
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
         case MATING_PRESS:
            this.coreState().animationState().set(GirlAnimationParameter.PLAYER_SHOULD_BE_RENDERED, phase != GirlScenePhase.APPROACH);
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
            break;
         case BEE_SEX:
         case NELSON:
         case THREESOME:
            // Her scene has no shared animation parameters of its own - the take is chosen straight from
            // the phase in GirlSceneAnimations - but the partner bone still has to exist for every phase
            // of it, and the scene camera and the arousal meter hang off that same flag.
            this.coreState().animationState().set(GirlAnimationParameter.PLAYER_SHOULD_BE_RENDERED, true);
            break;
         default:
            break;
      }

      // original Action.hasPlayer (GirlModel.java:199): the partner only exists once the scene proper
      // is running. Before that she is still walking over or being paid, so the "steve" bone must stay
      // hidden regardless of which action was picked - the cases above hardcode true for the non-bed ones.
      if (phase == GirlScenePhase.APPROACH || phase == GirlScenePhase.WALKING || phase == GirlScenePhase.WAITING) {
         this.coreState().animationState().set(GirlAnimationParameter.PLAYER_SHOULD_BE_RENDERED, false);
      }
   }

   /** Every animation goes through here, so a character's own file is preferred over Jenny's. */
   protected RawAnimation animation(GirlAnimationRole role) {
      return this.animations().get(role);
   }

   protected RawAnimation selectNamedAnimation(String animationName, boolean looping) {
      String owner = this.animations().namespace();
      String id = "animation." + owner + "." + animationName;
      // Holds the last frame rather than easing back to the bind pose, for the reason in GirlAnimationSet.
      return looping ? RawAnimation.begin().thenLoop(id) : RawAnimation.begin().thenPlayAndHold(id);
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
      return !this.customActionId().isEmpty();
   }

   protected boolean isCustomWalkingParticipant() {
      return false;
   }

   /** The id of the custom interaction that owns her, or "" when none does. Valid on both sides. */
   public final String customActionId() {
      return this.getEntityData().get(TRACKED_CUSTOM_ACTION);
   }

   /** Where that interaction has got to. Valid on both sides. */
   public final GirlCustomPhase customPhase() {
      return GirlCustomPhase.fromId(this.getEntityData().get(TRACKED_CUSTOM_PHASE));
   }

   protected final void setCustomPhase(GirlCustomPhase phase) {
      this.getEntityData().set(TRACKED_CUSTOM_PHASE, phase.id());
      this.customTicks = 0;
   }

   /**
    * Starts a custom interaction. Mirrors the original's {@code dc} approach packet: bind the partner,
    * drop navigation, and capture the meeting point <em>once</em> so the glide has a fixed destination
    * instead of chasing him.
    */
   protected final void beginCustomInteraction(ServerPlayer player, String actionId) {
      this.stopCustomInteraction();
      this.getEntityData().set(TRACKED_CUSTOM_ACTION, actionId);
      this.coreState().setActivePlayerId(player.getUUID());
      this.coreState().setShouldGetDamage(false);
      this.getNavigation().stop();
      this.setNoGravity(false);
      this.customApproachTarget = player.position().add(horizontalForward(player.getYRot()));
      boolean approaching = this.customApproachesPlayer(actionId);
      this.setCustomPhase(approaching ? GirlCustomPhase.APPROACH : GirlCustomPhase.ACTIVE);
      if (approaching) {
         this.startApproachFacing(player);
      } else {
         this.fadeSignalCounter++;
         this.setRenderPlayer(this.customShowsPartner(actionId));
         this.bindCustomPartner(player, actionId);
         this.onCustomPhaseEntered(actionId, GirlCustomPhase.ACTIVE, player);
      }

      this.syncTrackedState();
   }

   protected final void stopCustomInteraction() {
      if (this.customShowsPartner(this.customActionId())) {
         this.fadeSignalCounter++;
      }
      this.releaseCustomPartner();
      this.getEntityData().set(TRACKED_CUSTOM_ACTION, "");
      this.setCustomPhase(GirlCustomPhase.IDLE);
      this.customApproachTarget = null;
      this.setRenderPlayer(false);
      this.setNoGravity(false);
      this.coreState().setShouldGetDamage(true);
      this.coreState().clearInteraction();
      this.syncTrackedState();
   }

   /** Moves a custom interaction into its next phase, resetting the tick counter and the watchdog. */
   protected final void advanceCustomPhase(ServerPlayer player, GirlCustomPhase phase) {
      String actionId = this.customActionId();
      this.setCustomPhase(phase);
      this.setRenderPlayer(phase != GirlCustomPhase.IDLE && this.customShowsPartner(actionId));
      if (phase == GirlCustomPhase.ACTIVE) {
         if (!this.customApproachFadeFired) {
            this.fadeSignalCounter++;
         }
         this.bindCustomPartner(player, actionId);
      }
      this.onCustomPhaseEntered(actionId, phase, player);
      this.syncTrackedState();
   }

   /**
    * Swaps a running custom interaction for another take without releasing her - the second half of a
    * conversation, where the id names the animation and the phase says where the machine is.
    */
   protected final void switchCustomInteraction(ServerPlayer player, String actionId, GirlCustomPhase phase) {
      this.getEntityData().set(TRACKED_CUSTOM_ACTION, actionId);
      this.advanceCustomPhase(player, phase);
   }

   /**
    * She was offered a scene while a custom interaction still owns her - Bia's talk ends in a menu of
    * them. Return true to take the choice over; the ordinary path would refuse it, since she is busy.
    */
   public boolean interceptSceneChoice(ServerPlayer player, GirlSceneAction action) {
      return false;
   }

   /** Whether this interaction opens with the walk-up glide. */
   protected boolean customApproachesPlayer(String actionId) {
      return true;
   }

   /** Whether the partner model is drawn while this interaction runs. */
   protected boolean customShowsPartner(String actionId) {
      return false;
   }

   /**
    * How far in front of her the partner is held while this interaction runs, or 0 to leave him where
    * he stands.
    *
    * <p>The original binds him with {@code ei.b(UUID)}: it locks his movement, puts him a block along
    * her own axis, then {@code j(UUID)} pulls him 0.35 back towards her, so every interaction of that
    * family ends up with the pair 0.65 apart and facing each other. That is the number a character
    * returns here.
    */
   protected double customPartnerDistance(String actionId) {
      return 0.0;
   }

   /**
    * Puts the partner where the take expects him, instantly, the way {@code ei.b(UUID)} does.
    *
    * <p>Three deliberate deviations from the original. It snaps <em>her</em> to world yaw 0 and him to
    * 180 before measuring, so the pair always end up axis-aligned no matter which way either of them
    * was facing; we turn her towards whoever clicked her and measure the same offset off that, which is
    * the same geometry in her local frame - the frame the take is animated in - while moving him the
    * shortest distance. It gives him {@code noClip}, which we leave alone: he is only moved 0.65 blocks
    * towards a girl who is herself standing in the open, and a player with collisions off is a player
    * who can end up inside the floor if the interaction ends the wrong way. And it gives him
    * {@code noGravity}, which we also leave alone, because that flag is saved in his player file: a
    * disconnect mid-interaction would leave him weightless for good. He is put down at her own feet
    * height, so ordinary gravity holds him there just as well, the way the bed scenes already hold him.
    */
   private void bindCustomPartner(ServerPlayer player, String actionId) {
      double distance = this.customPartnerDistance(actionId);
      this.customPartnerAnchor = null;
      if (distance > 0.0) {
         // she turns to whoever clicked her rather than to a world axis, so he is placed along the ray
         // he already stands on and barely moves
         double dx = player.getX() - this.getX();
         double dz = player.getZ() - this.getZ();
         float yaw = dx * dx + dz * dz > 1.0E-4
            ? (float)(Mth.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F
            : this.getYRot();
         this.setYRot(yaw);
         this.setYBodyRot(yaw);
         this.setYHeadRot(yaw);
         this.yRotO = yaw;
         this.yBodyRotO = yaw;
         this.yHeadRotO = yaw;
         this.coreState().setTargetYaw(yaw);
         Vec3 spot = this.position().add(horizontalForward(yaw).scale(distance));
         float facing = Mth.wrapDegrees(yaw + 180.0F);
         this.customPartnerAnchor = new Vec3(spot.x, this.getY(), spot.z);
         player.setDeltaMovement(Vec3.ZERO);
         // the rotation has to travel with the teleport: he is being put behind his own back, and a
         // server-side setYRot alone would leave his camera pointing the way it already was
         player.teleportTo((ServerLevel)player.level(), spot.x, this.getY(), spot.z, Set.of(), facing, 0.0F, true);
         player.setYBodyRot(facing);
         player.setYHeadRot(facing);
         this.coreState().setPlayerYaw(facing);
         this.coreState().setPlayerIsInPosition(true);
      }
   }

   /**
    * Holds him on the spot the bind chose. His movement keys are already dead - the client's scene
    * camera locks them for any interaction that draws him - so this only has to answer a shove or a
    * slope, and it re-sends a position only once he has really drifted, rather than teleporting him
    * every tick and rubber-banding his client for the whole take.
    */
   private void holdCustomPartner(ServerPlayer player) {
      Vec3 anchor = this.customPartnerAnchor;
      if (anchor != null) {
         player.setDeltaMovement(Vec3.ZERO);
         if (player.position().distanceToSqr(anchor) > 0.04) {
            player.teleportTo(anchor.x, anchor.y, anchor.z);
         }
      }
   }

   /** Lets the partner go. Safe to call when nothing was ever bound. */
   private void releaseCustomPartner() {
      if (this.customPartnerAnchor != null) {
         this.customPartnerAnchor = null;
         if (this.level() instanceof ServerLevel serverLevel && this.coreState().activePlayerId() != null) {
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(this.coreState().activePlayerId());
            if (player != null) {
               player.setDeltaMovement(Vec3.ZERO);
            }
         }
      }
   }

   /**
    * The measured length of the take, in ticks, used only as a watchdog: the animation's own keyframe
    * is what normally ends the phase. 0 means the phase has no clock at all (a looping wait).
    */
   protected int customDurationTicks(String actionId, GirlCustomPhase phase) {
      return 0;
   }

   /** Hook for whatever a character wants to do the moment a custom phase begins. */
   protected void onCustomPhaseEntered(String actionId, GirlCustomPhase phase, ServerPlayer player) {
   }

   /**
    * What to do when the watchdog fires - the keyframe that should have ended this phase never came.
    * The default drops the interaction, which is right for a one-shot take.
    */
   protected void onCustomWatchdogExpired(ServerPlayer player) {
      this.stopCustomInteraction();
   }

   /**
    * A structural keyframe belonging to this character's own vocabulary, reported by the owning client.
    * Return true once it has been handled.
    */
   protected boolean applyCustomSceneKeyframe(ServerPlayer player, String keyframe) {
      return false;
   }

   /**
    * Which of her own keyframe names the server needs to hear about. The shared
    * {@link GirlSceneKeyframe} table only covers the sex scenes, so custom interactions declare theirs
    * here and the client relays them under the same prefix.
    */
   protected boolean isCustomStructuralKeyframe(String keyframe) {
      return false;
   }

   /**
    * The custom-interaction tick, the counterpart of {@link #tickServerScene()}. The glide, the
    * partner flag and the watchdog live here; everything character-specific goes through the hooks.
    */
   protected void tickCustomServer() {
      String actionId = this.customActionId();
      if (actionId.isEmpty()) {
         return;
      }

      ServerPlayer player = this.level() instanceof ServerLevel serverLevel && this.coreState().activePlayerId() != null
         ? serverLevel.getServer().getPlayerList().getPlayer(this.coreState().activePlayerId())
         : null;
      if (player == null || !player.isAlive() || player.distanceToSqr(this) > 64.0) {
         this.stopCustomInteraction();
         return;
      }

      GirlCustomPhase phase = this.customPhase();
      if (phase == GirlCustomPhase.APPROACH) {
         this.tickCustomApproach(player);
         return;
      }

      this.setRenderPlayer(this.customShowsPartner(actionId));
      this.holdCustomPartner(player);
      this.customTicks++;
      int budget = this.customDurationTicks(actionId, phase);
      if (budget > 0 && this.customTicks >= budget + KEYFRAME_WATCHDOG_TICKS) {
         this.onCustomWatchdogExpired(player);
      }
   }

   /**
    * The other half of the original's approach start ({@code dc$a.j}): the moment she is sent walking
    * it stops the player where he stands, steps him a third of a block forward and turns her to face
    * his back, so the pair already look at each other when the glide lands. The meeting point is
    * captured from his position <em>before</em> this runs, which is why the original ends up with them
    * 0.65 blocks apart rather than a whole block.
    */
   private void startApproachFacing(ServerPlayer player) {
      float playerYaw = player.getYRot();
      this.coreState().setTargetYaw(Mth.wrapDegrees(playerYaw + 180.0F));
      player.setDeltaMovement(Vec3.ZERO);
      Vec3 step = horizontalForward(playerYaw).scale(APPROACH_PLAYER_NUDGE);
      // the original teleports unconditionally; a step into a wall would only be shoved back out on the
      // next tick, so skip it rather than clip him through one
      if (player.level().noCollision(player, player.getBoundingBox().move(step))) {
         Vec3 target = player.position().add(step);
         player.teleportTo(target.x, target.y, target.z);
      }
   }

   /** The same 40-tick linear glide the scenes use, aimed at the point stored when it started. */

   private void tickCustomApproach(ServerPlayer player) {
      this.customTicks++;
      float playerYaw = player.getYRot();
      if (this.customApproachTarget == null) {
         this.customApproachTarget = player.position().add(horizontalForward(playerYaw));
      }

      Vec3 meetingPoint = this.customApproachTarget;
      double dx = this.getX() - meetingPoint.x;
      double dz = this.getZ() - meetingPoint.z;

      if (!this.customApproachFadeFired && this.customShowsPartner(this.customActionId()) && this.customTicks >= APPROACH_GLIDE_TICKS - this.fadeLeadTicks()) {
         this.customApproachFadeFired = true;
         this.fadeSignalCounter++;
         this.syncTrackedState();
      }

      if (dx * dx + dz * dz <= 0.01 || this.customTicks >= APPROACH_GLIDE_TICKS) {
         this.getNavigation().stop();
         this.setNoGravity(false);
         this.customApproachTarget = null;
         float facing = Mth.wrapDegrees(playerYaw + 180.0F);
         this.setYRot(facing);
         this.setYBodyRot(facing);
         this.setYHeadRot(facing);
         this.coreState().setTargetYaw(facing);
         this.advanceCustomPhase(player, GirlCustomPhase.ACTIVE);
      } else {
         int remaining = APPROACH_GLIDE_TICKS - this.customTicks;
         Vec3 step = remaining <= 0 ? meetingPoint : this.position().add(meetingPoint.subtract(this.position()).scale(1.0 / remaining));
         float travelYaw = (float)(Mth.atan2(meetingPoint.z - this.getZ(), meetingPoint.x - this.getX()) * 180.0 / Math.PI) - 90.0F;
         this.setYRot(travelYaw);
         this.setYBodyRot(travelYaw);
         this.setYHeadRot(travelYaw);
         this.setPos(step.x, step.y, step.z);
         this.setDeltaMovement(Vec3.ZERO);
      }
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

   /**
    * The rows of her menu, top level first.
    *
    * <p>Flat, like the original's: the rows are the handful of things this character in particular
    * answers to - Bia's talk and headpat, Luna's headpat, the bee's satchel - and then one row that
    * opens her whole scene roster. The original had no such row, because there the roster *was* the
    * menu: every character named her three or four scenes by hand and reached them through her own
    * talk. Ours keeps the list reachable in one place instead of sorting the scenes into oral,
    * foreplay, sex and tease sections, which no character in the original ever showed and which
    * duplicated the rows her talk already offers.
    */
   public Set<GirlSceneAction> effectiveSceneActions() {
      if (com.nxrtweetg.sexmod.config.SexmodConfig.isAllScenesEnabled() && this.hasInteractionMenu()) {
         return com.nxrtweetg.sexmod.config.SexmodConfig.UNIVERSAL_SCENES;
      }
      return this.sceneActions();
   }

   protected List<InteractionSection> buildInteractionSections() {
      List<InteractionSection> sections = new ArrayList<>(this.extraInteractionSections());
      for (InteractionOption option : this.optionsFor(this.effectiveSceneActions())) {
         sections.add(this.row(option));
      }
      return sections;
   }

   /**
    * The row that opens her whole scene roster, or nothing if she sells no scenes.
    *
    * <p>Her own roster, not every scene in the game: a girl who was given a shorter list never offered
    * the rest, and the "all" section was the one place that leaked them back in. A character who moves
    * this row somewhere else - Bia hangs it off the end of her talk - reuses it from there.
    */
   protected final List<InteractionSection> allScenesSection() {
      List<InteractionOption> allOptions = this.optionsFor(this.effectiveSceneActions());
      return allOptions.isEmpty() ? List.of() : List.of(this.section("action.section.all", ItemStack.EMPTY, allOptions));
   }

   protected Set<GirlSceneAction> sceneActions() {
      return this.allSceneActions();
   }

   /** Rows this character answers to that are hers alone, shown above her scene roster. */
   protected List<InteractionSection> extraInteractionSections() {
      return List.of();
   }

   protected Set<GirlSceneAction> allSceneActions() {
      return EnumSet.allOf(GirlSceneAction.class);
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

   protected final String labelKey(GirlSceneAction action) {
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
         case BEE_SEX -> "action.names.bee_sex";
         case NELSON -> "action.names.nelson";
         case THREESOME -> "action.names.threesome";
         case STRIP -> "action.names.strip";
         case HEAD_PAT -> "action.names.headpat";
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
   public boolean hasPaymentPhase(GirlSceneAction action) {
      return this.sceneActions().contains(action);
   }

   protected ItemStack priceFor(GirlSceneAction action) {
      if (!this.hasPaymentPhase(action)) {
         return ItemStack.EMPTY;
      }
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
      return new InteractionSection(labelKey, this.priceFor(previewAction), List.copyOf(options), null);
   }

   protected final InteractionSection section(String labelKey, ItemStack previewPrice, List<InteractionOption> options) {
      return new InteractionSection(labelKey, previewPrice.copy(), List.copyOf(options), null);
   }

   /** A top-level row that starts its action on the first click instead of opening a submenu. */
   protected final InteractionSection row(InteractionOption option) {
      return new InteractionSection(option.labelKey(), option.price(), List.of(option), option.actionId());
   }

   /** The same, for a scene she offers by name rather than through her roster. */
   protected final InteractionSection row(GirlSceneAction action) {
      return this.row(this.option(action, this.labelKey(action)));
   }

   private RawAnimation selectMovementAnimation(boolean moving) {
      RawAnimation custom = this.selectCustomMovementAnimation(moving);
      if (custom != null) {
         return custom;
      }

      if (this.walkAnimationAllowed()) {
         return this.animation(moving ? GirlAnimationRole.WALK : GirlAnimationRole.IDLE);
      } else {
         return this.animation(GirlAnimationRole.NULL);
      }
   }

   /**
    * Whether the ordinary walk/idle pair drives her right now.
    *
    * <p>It does outside a scene, and it does for the one phase of a scene she covers ground under her
    * own navigation. The original never gave that walk a scene state - {@code r()} clears the state
    * before the bed approach starts - which is what makes it fall back on the same predicate the idle
    * roaming uses, and what keeps her standing rather than marching when the path cannot be built.
    */
   private boolean walkAnimationAllowed() {
      if (!this.allowsWalkAnimation()) {
         return false;
      }

      if (this.resolvedScenePhase() == GirlScenePhase.WALKING) {
         return true;
      }

      return this.resolvedScenePhase() == GirlScenePhase.IDLE
         && this.resolvedActiveAction() == null
         && this.coreState().animationState().get(GirlAnimationParameter.MOVEMENT_ACTIVE);
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

      // The walk to the bed carries no scene state at all in the original: U() calls r(), which sets the
      // state to null before the approach starts, so she walks over on the ordinary movement animation.
      // Looping the walk take from here instead made the phase march on the spot whenever the path
      // stalled - the take plays for as long as the phase lasts, whether she is moving or not. The
      // movement controller owns the take for this phase because it is the one that knows.
      if (girlscenephase == GirlScenePhase.WALKING) {
         return this.animation(GirlAnimationRole.NULL);
      }

      // The sit-on-the-edge wait is the one pair of takes the scene table cannot name, because which
      // one a character owns is a property of the character (sitdown/sitdownidle, or a bare hold).
      GirlAnimationRole edge = this.edgeApproachRole(girlsceneaction, girlscenephase);
      if (edge != null) {
         return this.resolveEdgeApproachAnimation(girlsceneaction, edge);
      }

      // The one phase a flat table cannot describe: the loop alternates sides on a timer.
      if (girlsceneaction == GirlSceneAction.KOBOLD_BLOWJOB && girlscenephase == GirlScenePhase.ACTIVE) {
         return this.selectKoboldBlowjobLoopAnimation(flag);
      }

      // scenes that ship several takes of the same beat rotate through them instead of looping one
      GirlAnimationRole variant = GirlSceneAnimations.role(girlsceneaction, girlscenephase, flag, this.resolvedHardVariant());
      return this.animation(variant);
   }

   /**
    * Reports a structural keyframe to the server, which owns the scene machine.
    *
    * <p>Keyframes fire on every client that renders her, so only the client whose player owns the
    * scene is allowed to speak for it - otherwise a bystander would advance the beat, or it would
    * advance once per viewer. Cameras, fades and UI toggles never leave the client.
    */
   private void reportStructuralKeyframe(String keyframe) {
      GirlSceneKeyframe signal = GirlSceneKeyframe.of(keyframe);
      if (signal == null) {
         // not part of the shared scene vocabulary; a custom interaction may still own it
         if (this.isCustomStructuralKeyframe(keyframe) && this.isSceneOwnedByLocalPlayer()) {
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
               new com.nxrtweetg.sexmod.network.JennySceneInputPayload(this.getId(), KEYFRAME_INPUT_PREFIX + keyframe)
            );
         }

         return;
      }

      if (!signal.needsServer()) {
         this.handleClientKeyframe(keyframe);
         return;
      }

      if (this.isSceneOwnedByLocalPlayer()) {
         net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
            new com.nxrtweetg.sexmod.network.JennySceneInputPayload(this.getId(), KEYFRAME_INPUT_PREFIX + keyframe)
         );
      }
   }

   /** The keyframes that only ever change what the local player sees. */
   private void handleClientKeyframe(String keyframe) {
      switch (keyframe) {
         // the original showed the arousal meter from a keyframe rather than on scene entry, so it
         // appears with the first beat instead of during the walk-up
         case "openSexUI", "openSexUi", "sexUI", "sexUiOn" -> com.nxrtweetg.sexmod.client.GirlHornyMeter.show(true);
         default -> {
         }
      }
   }

   protected boolean isSceneOwnedByLocalPlayer() {
      Player player = Minecraft.getInstance().player;
      return player != null && this.trackedActivePlayer().filter(player.getUUID()::equals).isPresent();
   }

   /**
    * A structural keyframe the owning client reached. The keyframe is the authority for the
    * transition; the phase tick budgets only step in when one never arrives.
    */
   public void applySceneKeyframe(ServerPlayer player, String keyframe) {
      if (this.sceneFrozen) {
         return;
      }

      if (this.coreState().activePlayerId() == null || !this.coreState().activePlayerId().equals(player.getUUID())) {
         return;
      }

      // A custom interaction gets first refusal: its keyframes are her own, not the shared scene set.
      if (!this.customActionId().isEmpty() && this.applyCustomSceneKeyframe(player, keyframe)) {
         return;
      }

      if (this.activeAction == null || this.scenePhase == GirlScenePhase.IDLE) {
         return;
      }

      GirlSceneKeyframe signal = GirlSceneKeyframe.of(keyframe);
      if (signal == null || !signal.needsServer()) {
         return;
      }

      switch (signal) {
         case PAYMENT_END -> {
            if (this.scenePhase == GirlScenePhase.PAYMENT) {
               this.finishPaymentPhase(player);
            }
         }
         case POSITIONING_END -> {
            if (this.scenePhase == GirlScenePhase.POSITIONING) {
               this.finishPositioningPhase(player);
            }
         }
         case INTRO_END -> {
            if (this.scenePhase == GirlScenePhase.INTRO && !(this.introWaitsForThrust() && this.thrustTicks <= 0)) {
               this.finishIntroPhase(player);
            }
         }
         case BEAT_HOLD -> {
            // The original's re-entry point, and it really does re-enter: at doggyfastReady it runs
            // {@code if (isBoundPartner() && paceKeyDown) { resetControllerAndTellServer(); }}, where the
            // reset zeroes the action controller's tick offset. The rest of the take is never played -
            // doggyfast reads Ready at 0.52s of 1.08 and prone doggy's doggyReset at 0.625 of 2.04 - so a
            // held key beats at the Ready interval and not at the take's length. Its own *Done is the
            // release path: it can only be reached by a take that was not restarted, and it is what
            // hands her back to the slow loop.
            //
            // A Ready inside a slow take (mating_press_soft, and doggySwitch in prone_doggy_soft and
            // sitting_alt_soft) is the same check where the beat has not started yet, so it starts one.
            if (this.scenePhase == GirlScenePhase.ACTIVE && this.paceHeld) {
               boolean reentry = this.coreState().playerIsThrusting() && this.thrustTicks > 0;
               this.coreState().setPlayerIsThrusting(true);
               this.rollHardVariant(reentry);
               // The length stays as a watchdog only: the next Ready lands well inside it, so it is what
               // ends the beat when the client stops reporting, not what paces it.
               this.thrustTicks = this.thrustDurationTicks();
               this.beatResetCounter++;
               this.applyActionAnimationFlags(this.activeAction, this.scenePhase);
               this.syncTrackedState();
            }
         }
         case BEAT_END -> {
            // The take played out, which means the key was up when its Ready went past. A key that came
            // back down in between still starts the next take from here, rather than waiting for the
            // watchdog.
            if (this.scenePhase == GirlScenePhase.ACTIVE) {
               if (this.paceHeld) {
                  // The next take is picked before it is measured, so a scene whose takes differ in
                  // length gets the budget of the one that is about to play rather than the last one's.
                  this.rollHardVariant(true);
                  this.thrustTicks = this.thrustDurationTicks();
               } else {
                  this.thrustTicks = 0;
                  this.coreState().setPlayerIsThrusting(false);
               }

               this.beatResetCounter++;
               this.applyActionAnimationFlags(this.activeAction, this.scenePhase);
               this.syncTrackedState();
            }
         }
         case STRIP_REVEAL -> {
            if (this.activeAction == GirlSceneAction.STRIP && this.coreState().currentModel() != this.stripTargetModel) {
               this.coreState().setCurrentModel(this.stripTargetModel);
               this.syncTrackedState();
            }
         }
         case STRIP_END -> {
            if (this.activeAction == GirlSceneAction.STRIP) {
               this.finishStripPhase(player);
            }
         }
         case CLIMAX_END -> {
            if (this.scenePhase == GirlScenePhase.CLIMAX) {
               if (!this.climaxFadeFired) {
                  this.climaxFadeFired = true;
                  this.fadeSignalCounter++;
                  this.climaxTicks = this.fadeLeadTicks();
                  this.syncTrackedState();
               } else if (this.climaxTicks <= 0) {
                  this.finishScene(player);
               }
            }
         }
         default -> {
         }
      }
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
            default:
               // Structural keyframes: phase transitions, pace holds, cameras, fades. The table says
               // which of them the server has to hear about; anything unlisted is a dialogue or sound
               // cue already handled above and stays a no-op here.
               this.reportStructuralKeyframe(s);
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
         this.playSoundAt(voice.id(index), volume, this.voicePitch());
      }
   }

   /** The shared effect banks under {@code misc}, which every girl uses the same way. */
   protected void playSoundVariant(String baseId, int variants, float volume) {
      this.playSoundVariant(new GirlSoundSet.Voice(baseId, variants), volume);
   }

   /** Plays one specific variant, for the keyframes where the original hardcoded an index. */
   protected void playSoundIndex(GirlSoundSet.Voice voice, int index, float volume) {
      if (voice.variants() > 0) {
         this.playSoundAt(voice.id(index), volume, this.voicePitch());
      }
   }

   protected void playSoundIndex(String baseId, int index, float volume) {
      this.playSoundAt(baseId + index, volume);
   }

   /**
    * How her voice is pitched. One for everyone but the kobold, whose {@code ff.b(SoundEvent, float)}
    * pitches hers by how small she is; the shared {@code misc} banks are left alone either way.
    */
   protected float voicePitch() {
      return 1.0F;
   }

   protected void playSoundAt(String soundId, float volume) {
      this.playSoundAt(soundId, volume, 1.0F);
   }

   protected void playSoundAt(String soundId, float volume, float pitch) {
      if (this.level().isClientSide()) {
         SoundEvent soundevent = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("sexmod", soundId));
         this.level().playLocalSound(this.blockPosition(), soundevent, SoundSource.NEUTRAL, volume, pitch, false);
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
         || !this.customActionId().isEmpty()
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
      this.paceHeld = false;
      this.sceneFrozen = false;
      this.stripTargetModel = 1;
      this.queuedActionAfterStrip = null;
      this.sceneApproachTarget = null;
      this.getEntityData().set(TRACKED_CUSTOM_ACTION, "");
      this.setCustomPhase(GirlCustomPhase.IDLE);
      this.customApproachTarget = null;
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
      this.getEntityData().set(TRACKED_BEAT_RESET, this.beatResetCounter);
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
         case WAITING, POSITIONING -> GirlScenePhase.WAITING;
         default -> GirlScenePhase.ACTIVE;
      };
   }

   private void prepareSceneLayout(ServerPlayer player, GirlSceneAction action) {
      if (!this.requiresBed(action)) {
         if (action == GirlSceneAction.KOBOLD_ANAL) {
            float f1 = player.getYRot();
            Vec3 vec317 = player.position().add(horizontalForward(f1).scale(0.42));
            this.snapTo(vec317.x, player.getY(), vec317.z, f1, 0.0F);
            this.coreState().setTargetYaw(f1);
            this.coreState().setPlayerYaw(f1);
            return;
         }

         if (action == GirlSceneAction.THREESOME) {
            float f = player.getYRot();
            this.snapTo(player.getX(), player.getY(), player.getZ(), f, 0.0F);
            this.coreState().setTargetYaw(f);
            this.coreState().setPlayerYaw(f);
            return;
         }

         if (action == GirlSceneAction.COWGIRL) {
            float f = player.getYRot();
            float fYaw = Mth.wrapDegrees(f + 180.0F);
            this.snapTo(player.getX(), player.getY(), player.getZ(), fYaw, 0.0F);
            this.coreState().setTargetYaw(fYaw);
            this.coreState().setPlayerYaw(f);
            return;
         }

         float f2 = player.getYRot();

         double d1 = switch (action) {
            case FACEFUCK -> 0.48;
            case BOOBJOB -> 0.72;
            case TOUCH_BOOBS -> 0.72;
            // original fz.c(): c(aa()), and aa() is the player's position one block along his own yaw
            case BEE_SEX -> 1.0;
            case MATING_PRESS, NELSON -> 0.8;
            default -> 0.95;
         };
         // Scene tuning shifts the body bone on the client for all actions, so
         // moving the entity as well would apply the tuning twice and decouple positions
         boolean movesBodyBone = true;
         double tuneForward = movesBodyBone ? 0.0 : this.sceneTuneForward;
         double tuneSide = movesBodyBone ? 0.0 : this.sceneTuneSide;
         double tuneVertical = movesBodyBone ? 0.0 : this.sceneTuneVertical;
         Vec3 vec328 = horizontalRight(f2);
         Vec3 vec33 = player.position().add(horizontalForward(f2).scale(d1 + tuneForward)).add(vec328.scale(tuneSide));
         float f10 = Mth.wrapDegrees(f2 + 180.0F);
         this.snapTo(vec33.x, player.getY() + tuneVertical, vec33.z, f10, 0.0F);
         this.coreState().setTargetYaw(f10);
         this.coreState().setPlayerYaw(f2);
         return;
      }

      this.placeBedPose(action);
   }

   /**
    * Puts her at the scene's origin, facing the bed, and leaves the rest to the take. Everything that
    * used to sit here - a per-scene forward/side/vertical nudge off the mattress top - was our own
    * compensation for measuring from the wrong point; see {@link #requireSceneBedOrigin()}. The live
    * tuning stays, as a delta on top of the origin, so a scene that still needs a hair of adjustment
    * can have it without the base geometry drifting from the original's.
    */
   private void placeBedPose(GirlSceneAction action) {
      Vec3 origin = this.requireSceneBedOrigin();
      float yaw = this.requireSceneBedYaw();
      // Scene tuning shifts the body bone on the client, so the server entity stays at the canonical origin
      this.snapTo(origin.x, origin.y, origin.z, yaw, 0.0F);
      this.coreState().setTargetYaw(yaw);
      this.coreState().setPlayerYaw(facesGirlAcrossBed(action) ? Mth.wrapDegrees(yaw + 180.0F) : yaw);
   }

   /** The bed scenes that put him in front of her looking back, rather than behind her looking the same way. */
   private static boolean facesGirlAcrossBed(GirlSceneAction action) {
      return switch (action) {
         case SITTING, MISSIONARY, REVERSE_COWGIRL, COWGIRL -> true;
         default -> false;
      };
   }

   /**
    * Where the partner goes for a bed scene. The original moves him exactly once, measured off her
    * stored origin and her stored yaw ({@code em.a(side, up, forward, yawOffset, pitch)}): doggy and its
    * neighbours use {@code a(0, 0, 0.4, 0, 60)}, which is 0.4 blocks along her own facing so he ends up
    * just behind the body the take has carried onto the mattress, and the sit-on-the-edge scenes use
    * Luna's {@code a(0, -0.075, -0.7109375, 0, 0)} against a yaw flipped to face her, which lands him
    * 0.71 in front of her instead.
    */
   private Vec3 bedPartnerSpot(float yaw) {
      boolean acrossBed = facesGirlAcrossBed(this.activeAction);
      Vec3 spot = this.requireSceneBedOrigin().add(horizontalForward(yaw).scale(acrossBed ? 0.7109375 : 0.4));
      return new Vec3(spot.x, this.sceneSurfaceY() + (acrossBed ? -0.075 : 0.0), spot.z);
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

   /**
    * Puts her exactly on the spot the bed table picked, facing the way the table says, so the
    * sit-on-the-edge wait happens where she stopped walking rather than in the scene's own pose
    * (original {@code el.q()}: {@code c(vec3d)} then {@code b(n2)} immediately before SITDOWN).
    */
   private void snapToBedAnchor() {
      Vec3 stand = this.requireSceneBedStandPos();
      float yaw = this.requireSceneBedYaw();
      this.snapTo(stand.x, stand.y, stand.z, yaw, 0.0F);
      this.coreState().setTargetYaw(yaw);
      this.coreState().setPlayerYaw(Mth.wrapDegrees(yaw + 180.0F));
      this.holdSceneYaw();
   }

   protected void alignSceneParticipants(ServerPlayer player) {
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
         // The original nudges the player 0.2 forward along his own yaw while she stands a block ahead
         // of him, so the pair end up 0.8 apart - measured back from her, that is 0.8 along her facing.
         case BEE_SEX -> this.position().add(horizontalForward(f).scale(0.8));
         case NELSON -> this.position().add(horizontalForward(f).scale(0.8));
         case DOGGY -> this.requiresBed(this.activeAction) ? this.bedPartnerSpot(f) : this.position().add(horizontalForward(f).scale(0.8));
         case PRONE_DOGGY -> this.requiresBed(this.activeAction) ? this.bedPartnerSpot(f) : this.position().add(horizontalForward(f).scale(0.8));
         case SITTING_ALT -> this.requiresBed(this.activeAction) ? this.bedPartnerSpot(f) : this.position().add(horizontalForward(f).scale(0.8));
         case ANAL -> this.requiresBed(this.activeAction) ? this.bedPartnerSpot(f) : this.position().add(horizontalForward(f).scale(0.8));
         case ANAL_ALT -> this.requiresBed(this.activeAction) ? this.bedPartnerSpot(f) : this.position().add(horizontalForward(f).scale(0.8));
         case KOBOLD_ANAL -> this.position().add(horizontalForward(f).scale(-0.1));
         case SITTING -> this.requiresBed(this.activeAction) ? this.bedPartnerSpot(f) : this.position().add(horizontalForward(f).scale(0.8));
         case MISSIONARY -> this.requiresBed(this.activeAction) ? this.bedPartnerSpot(f) : this.position().add(horizontalForward(f).scale(0.8));
         case MATING_PRESS -> this.position().add(horizontalForward(f).scale(0.8));
         case REVERSE_COWGIRL -> this.requiresBed(this.activeAction) ? this.bedPartnerSpot(f) : this.position().add(horizontalForward(f).scale(0.8));
         case COWGIRL -> this.requiresBed(this.activeAction) ? this.bedPartnerSpot(f) : this.position();
         case THREESOME -> this.position();
         case STRIP, HEAD_PAT -> null;
      };
      if (vec3 != null) {
         float f1 = switch (this.activeAction) {
            case BLOWJOB -> Mth.wrapDegrees(f + 180.0F);
            case KOBOLD_BLOWJOB -> Mth.wrapDegrees(f + 180.0F);
            case DEEPTHROAT -> Mth.wrapDegrees(f + 180.0F);
            case FACEFUCK -> Mth.wrapDegrees(f + 180.0F);
            case BOOBJOB -> Mth.wrapDegrees(f + 180.0F);
            case TOUCH_BOOBS -> Mth.wrapDegrees(f + 180.0F);
            case BEE_SEX -> Mth.wrapDegrees(f + 180.0F);
            case MATING_PRESS -> Mth.wrapDegrees(f + 180.0F);
            case NELSON -> Mth.wrapDegrees(f + 180.0F);
            // He keeps her yaw behind her and takes the flipped one when the scene sits him in front of
            // her, exactly the two cases the original's `a(..., yawOffset, ...)` covers.
            case DOGGY, PRONE_DOGGY, ANAL, ANAL_ALT, SITTING_ALT -> f;
            case KOBOLD_ANAL -> f;
            case SITTING, MISSIONARY, REVERSE_COWGIRL, COWGIRL -> Mth.wrapDegrees(f + 180.0F);
            case THREESOME -> f;
            case STRIP, HEAD_PAT -> player.getYRot();
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
      // A scene she waits for him sitting measures her own sit-down take, whichever scene it is, and
      // hands straight over to the hold when she ships none (Luna).
      if (this.sitsDownToWait(this.activeAction)) {
         return this.sitDownTicks();
      }

      return switch (this.activeAction) {
         case BLOWJOB, KOBOLD_BLOWJOB, DEEPTHROAT, FACEFUCK, BOOBJOB, TOUCH_BOOBS, MATING_PRESS, KOBOLD_ANAL, BEE_SEX, NELSON, STRIP, HEAD_PAT, THREESOME -> 0;
         // her walk onto the bed, whose length is the character's own take: Jenny's is 130 ticks, but the
         // slime plays hers on the floor in 34
         case DOGGY -> this.phaseTakeTicks(GirlScenePhase.POSITIONING, false, 130);
         // these three share anal's lying-down take, so they need anal's runtime, not the old 38 ticks
         // that belonged to the much shorter prone_doggy_intro
         case PRONE_DOGGY, SITTING_ALT, ANAL -> this.phaseTakeTicks(GirlScenePhase.POSITIONING, false, 111);
         case ANAL_ALT -> this.phaseTakeTicks(GirlScenePhase.POSITIONING, false, 110);
         // the sit-on-the-edge scenes: positioning is her sit-down take, so its length comes from
         // whichever take the character ships, and is 0 for one who only holds (Luna) or has neither
         case SITTING, MISSIONARY, COWGIRL -> this.sitDownTicks();
         case REVERSE_COWGIRL -> this.requiresBed(this.activeAction) ? this.sitDownTicks() : 0;
      };
   }

   private int positioningWatchdogTicks() {
      return this.requiredPositioningTicks() + this.watchdogGraceTicks(this.positioningRole(), GirlSceneKeyframe.POSITIONING_END);
   }

   /**
    * The take positioning is measuring. When she waits for him sitting that is a property of the
    * character rather than of the scene, so it comes from her edge approach and not from the table.
    */
   private GirlAnimationRole positioningRole() {
      if (this.sitsDownToWait(this.activeAction)) {
         GirlEdgeApproach approach = this.edgeApproach();
         return approach != null ? approach.sitDown() : null;
      }

      return GirlSceneAnimations.role(this.activeAction, GirlScenePhase.POSITIONING, false);
   }

   /**
    * The one point a bed scene is measured from: the spot beside the bed she walked to, on the floor
    * she walked on, facing the bed.
    *
    * <p>The original stores exactly that point once ({@code em.c(Vec3d)}) and then pins her to it every
    * AI tick for the rest of the scene ({@code func_70080_a(o(), I(), 0)}) - it never moves her onto the
    * mattress at all. The climb is the take's own {@code body} translation: Jenny's doggy holds
    * {@code (0, 3.98, -8.3)} model units, which is 0.52 forward and 0.25 up. Measuring the pose from the
    * mattress instead applied that displacement a second time, which is what pushed her out to the far
    * edge of the bed and left her floating above it, and for a bed entered from one of its ends the
    * mattress midpoint put the pose origin on the seam between the two halves.
    *
    * <p>Every candidate the bed table returns is a whole block off a single bed block (0.68 for the
    * sit-on-the-edge variant), so measuring from here also makes the seam unreachable by construction -
    * the guard the parity list asks for.
    */
   private Vec3 requireSceneBedOrigin() {
      if (this.sceneBedStandPos != null) {
         return this.sceneBedStandPos;
      } else if (this.sceneFloorCenter != null) {
         return this.sceneFloorCenter;
      } else {
         return this.position();
      }
   }

   /**
    * The height the partner is put at: the base of the bed block under a bed scene, the floor she is on
    * without one. That is the same level {@link #requireSceneBedOrigin()} leaves her at, because the
    * original stands them both on the floor beside the bed and lets the takes do the climbing.
    */
   private double sceneSurfaceY() {
      if (this.sceneBedFootPos != null) {
         return this.sceneBedFootPos.getY();
      }

      return this.sceneFloorCenter != null ? this.sceneFloorCenter.y : this.getY();
   }

   private Direction requireSceneBedFacing() {
      if (this.sceneBedFacing == null) {
         return this.getDirection();
      } else {
         return this.sceneBedFacing;
      }
   }

   /**
    * The rotation every bed scene is laid out with. It comes from the bed table's candidate rather
    * than from the bed's own facing, so she enters from whichever end she was actually standing at;
    * the layout offsets are all measured along this yaw, so the whole pose rotates with it.
    */
   private float requireSceneBedYaw() {
      if (this.sceneBedStandPos != null) {
         return Mth.wrapDegrees(this.sceneBedStandYaw);
      }

      if (this.sceneFloorCenter != null) {
         return this.sceneFloorYaw;
      }

      if (this.sceneBedFacing != null) {
         return Mth.wrapDegrees(directionYaw(this.sceneBedFacing));
      }

      return this.getYRot();
   }

   /** The walkable spot beside the bed. Never the mattress itself, which ground navigation cannot reach. */
   private Vec3 requireSceneBedStandPos() {
      if (this.sceneBedStandPos != null) {
         return this.sceneBedStandPos;
      } else if (this.sceneFloorCenter != null) {
         return this.sceneFloorCenter;
      } else {
         return this.position();
      }
   }

   /**
    * Whether the walk to the bed is over. The original arrives inside 0.6 of the spot it picked and
    * gives up after 200 ticks ({@code ex.func_70619_bc()}: {@code dist < 0.6 || ad > 200}); the middle
    * clause is ours, for a walk that cannot be finished at all.
    *
    * <p>It used to accept any distance under 1.5 blocks the moment navigation reported itself done,
    * which is a state a path that ran out short of the spot reaches while she is still two blocks away -
    * and then nothing else in the phase could end it, so she stood at the bed playing the walk take for
    * the whole ten seconds before the bail tick. Ground navigation running out of path is now answered
    * with a fresh one, twice, the way the original re-paths twice; when those are spent she hands over
    * where she stands, and the layout puts her on the bed regardless.
    */
   private boolean hasArrivedAtBed(double horizontalDistanceSqr) {
      if (horizontalDistanceSqr <= ARRIVAL_DISTANCE_SQR) {
         return true;
      }
      return horizontalDistanceSqr <= 3.24 && (this.getNavigation().isDone() || this.getDeltaMovement().horizontalDistanceSqr() < 1e-4);
   }

   /** Navigation has nothing left to walk, and has had long enough to have started. */
   private boolean walkStalled() {
      return this.sceneTicks >= WALK_STALL_GRACE_TICKS && this.getNavigation().isDone();
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

   protected static Vec3 horizontalForward(float yaw) {
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

   /**
    * One row of the top level of her menu.
    *
    * <p>The original's menu was flat: every row sent its own action, and a row that led somewhere led
    * there by having the entity reopen the menu with a different list of rows. So a row that fires
    * straight away carries its action in {@code directActionId} and repeats it as its only option;
    * {@code null} there means the row opens a submenu built from {@code options} instead, which is how
    * the one row the original does not have - the port's "every scene she knows" list - is shown.
    */
   public record InteractionSection(String labelKey, ItemStack pricePreview, List<InteractionOption> options, String directActionId) {
      public Optional<InteractionOption> directOption() {
         return this.directActionId() == null ? Optional.empty() : this.options().stream().filter(option -> option.actionId().equals(this.directActionId())).findFirst();
      }
   }
}
