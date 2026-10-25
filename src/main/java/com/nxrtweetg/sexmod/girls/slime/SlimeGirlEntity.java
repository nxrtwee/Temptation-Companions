package com.nxrtweetg.sexmod.girls.slime;

import software.bernie.geckolib.animation.RawAnimation;
import com.nxrtweetg.sexmod.girls.GirlFollowMasterGoal;
import com.nxrtweetg.sexmod.girls.GirlGoHomeGoal;
import com.nxrtweetg.sexmod.girls.GirlMeleeAttackGoal;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationLengths;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationSet;
import com.nxrtweetg.sexmod.girls.anim.GirlSoundSet;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.girls.scene.GirlScenePhase;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * The slime girl, {@code fn} in the original.
 *
 * <p>She is the one character who is not a shopkeeper: she has no interaction menu, no AI goals and no
 * dialogue. She hops around on a 100-tick loop, rolls once per landing for arousal, and once aroused
 * hops straight at the nearest player and starts her own scenes when he gets close enough to grab.
 * Everything below is her own tick; the scenes themselves run on the shared engine.
 *
 * <p>Left out of this port, deliberately: her pregnancy ({@code ticksUntilBirth} and the plain slime it
 * hatches into) has no counterpart on our side yet, so the two places the original tests it - the
 * arousal roll and the facing choice - are simply always "not pregnant" here.
 */
public final class SlimeGirlEntity extends SceneGirlEntity {
   /** {@code fn.T}, NBT {@code hornyLevel}: 1 undresses her, 2 hunts, 4 lies down by herself. */
   private static final EntityDataAccessor<Integer> TRACKED_HORNY_LEVEL = SynchedEntityData.defineId(
      SlimeGirlEntity.class, EntityDataSerializers.INT
   );
   /**
    * {@code fn.S}, her out-of-scene pose. The original keeps it client-side only and works it out from
    * an unsynced counter, which is why its jump take runs a fraction of a second early there; ours is
    * synced from the tick that owns the counter instead.
    */
   private static final EntityDataAccessor<String> TRACKED_POSE = SynchedEntityData.defineId(
      SlimeGirlEntity.class, EntityDataSerializers.STRING
   );

   /** {@code fn.M}: ground ticks between hops. */
   private static final int JUMP_PERIOD_TICKS = 100;
   /** {@code fn.b()}: the yaw is picked 15 ticks before the hop and the take starts 10 ticks before it. */
   private static final int FACING_CHOICE_TICK = 85;
   private static final int JUMP_TAKE_TICK = 90;
   /** {@code fn.L}: one roll per landing. */
   private static final float AROUSAL_CHANCE = 0.1F;
   /** The roll is consumed half a hop later, so the arousal never lands on the take's first frame. */
   private static final int AROUSAL_APPLY_TICK = 50;
   /** {@code fn.Q}: how hard she throws herself forward. */
   private static final double JUMP_SPEED = 0.7;
   /** {@code fn.W}: {@code func_175134_bD}, a good deal higher than a vanilla mob's 0.42. */
   private static final float JUMP_POWER = 0.9F;
   /** {@code fn.e()}: she only aims at a player she can see this far off. */
   private static final double HUNT_RANGE = 30.0;
   /** {@code fn.a()}: and only grabs one who is right on top of her. */
   private static final double GRAB_RANGE = 1.0;
   /** {@code hornyLevel} thresholds, in the order {@code fn.a()} tests them. */
   private static final int HORNY_HUNTING = 2;
   private static final int HORNY_UNPROMPTED = 4;
   /**
    * {@code animation.slime.undress} runs 1.04s and drops the dressed model on its {@code undress}
    * keyframe at 0.92s; the pose is held to the end of the take either way.
    */
   private static final int UNDRESS_REVEAL_TICKS = 18;
   private static final int UNDRESS_FALLBACK_TICKS = 21;
   /**
    * Her two jump takes hand over to the next pose on their own keyframes rather than at their end:
    * {@code jumpstart} fires {@code jumpStartDone} at 2.16s of 3.36, {@code jumpend} fires
    * {@code jumpEndDone} at 2.2 of 2.36. These are the fallbacks for when the file cannot be read.
    */
   private static final int JUMP_START_HANDOVER_TICKS = 43;
   private static final int JUMP_END_HANDOVER_TICKS = 44;
   /**
    * How long the impulse gets to lift her before the hop is written off. {@code Entity.move} only reports
    * ground contact for downward movement, so two ticks of it after a launch means she never left the
    * floor at all - boxed in, under a ceiling, or in a fluid.
    */
   private static final int LAUNCH_GRACE_TICKS = 2;
   /**
    * How long she has to have been off the ground for a ground contact to count as a landing. Vanilla
    * drops {@code onGround} for a single tick on all sorts of nothing - a player walking into her, a
    * slab edge, a step up - and the original cannot tell the difference because there the landing test
    * lives on the client and the hop lives on the server, so a false landing costs it only one
    * mistimed squash take. Ours share a counter: a false landing there also throws the ground count
    * away, which cancels the hop the take has already started playing. Three ticks is longer than any
    * bump and shorter than the fall off a single block.
    */
   private static final int LANDING_MIN_AIR_TICKS = 3;
   /**
    * How far off she will look for somebody to lie down in front of, and how long she waits for him.
    * The original's {@code T >= 4} branch goes down with nobody bound at all and picks up whoever walks
    * into her later; ours has to name him up front, because our waiting phase is built around knowing
    * whose scene it is. That makes the choice a commitment, so it is kept to somebody who is plausibly
    * on his way over, and dropped again if he never arrives - otherwise a player who never even saw her
    * would be locked out of every other girl for the rest of the session.
    */
   private static final double UNPROMPTED_AUDIENCE_RANGE = 12.0;
   private static final int UNPROMPTED_WAIT_TICKS = 600;
   private static final double UNPROMPTED_ABANDON_RANGE = 24.0;

   /** The animation names in slime.animation.json; everything else is borrowed from Jenny. */
   private static final Set<String> OWN_ANIMATIONS = Set.of(
      "attack0", "attack1", "attack2", "backwards_walk", "blowjobcum", "blowjobsuck", "blowjobthrust",
      "bowcharge", "doggycum", "doggyfast", "doggygoonbed", "doggyslow", "doggystart", "doggywait",
      "dress", "fhappy", "fly", "fly2", "idle", "jumpair", "jumpend", "jumpstart", "null", "run",
      "sit", "strip", "undress", "walk"
   );
   private static final GirlAnimationSet ANIMATIONS = new GirlAnimationSet("slime", OWN_ANIMATIONS);
   /** She ships no voice of her own, so every line falls back to Jenny. */
   private static final GirlSoundSet SOUNDS = new GirlSoundSet("slime", Map.of());

   /** {@code fn.N}: ticks spent on the ground since the last landing. */
   private int groundTicks;
   /** {@code fn.K}: whether she was on the ground last tick, so a landing can be spotted. */
   private boolean wasOnGround = true;
   /** {@code fn.V}: the result of this landing's roll, latched until the next one. */
   private boolean arousalRoll;
   /** {@code fn.R}: the yaw the next hop will take, chosen 15 ticks in advance. */
   private float plannedJumpYaw;
   /** How far into her undressing take she is; 0 unless {@link Pose#UNDRESS} owns her. */
   private int undressTicks;
   /** How far into the current jump take she is, for the hand-over its own keyframe performs. */
   private int poseTicks;
   /** Whether a hop has been fired and still owes a landing. */
   private boolean hopOwed;
   /** Ticks the owed hop has spent on the ground since its impulse, i.e. going nowhere. */
   private int launchTicks;
   /** Ticks since she last touched the ground, so a bump can be told from a fall. */
   private int airTicks;
   /** Ticks she has been lying in wait for a player she chose herself. */
   private int unpromptedWaitTicks;

   /**
    * {@code fn$a}. Every pose but the two transient jump takes loops; those two hand over to the next
    * pose on a keyframe of their own, which is why the original's enum has a separate airborne entry.
    */
   public enum Pose {
      IDLE("idle", true),
      JUMP_START("jumpstart", false),
      JUMP_AIR("jumpair", true),
      JUMP_END("jumpend", false),
      UNDRESS("undress", false);

      private final String animation;
      private final boolean looping;

      Pose(String animation, boolean looping) {
         this.animation = animation;
         this.looping = looping;
      }

      public String animation() {
         return this.animation;
      }

      public boolean looping() {
         return this.looping;
      }

      static Pose byName(String name) {
         for (Pose pose : values()) {
            if (pose.name().equals(name)) {
               return pose;
            }
         }

         return IDLE;
      }
   }

   public SlimeGirlEntity(EntityType<? extends SlimeGirlEntity> entityType, Level world) {
      super(entityType, world, "Slime");
   }

   @Override
   public String characterId() {
      return "slime";
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
      return EnumSet.of(GirlSceneAction.BLOWJOB, GirlSceneAction.DOGGY, GirlSceneAction.STRIP);
   }
   /**
    * {@code fn.func_184651_r()} is empty: she has no goals at all, not even the float goal, so nothing
    * steers her but the hop loop below.
    */
   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new GirlGoHomeGoal(this));
      this.goalSelector.addGoal(2, new GirlMeleeAttackGoal(this));
      this.goalSelector.addGoal(3, new GirlFollowMasterGoal(this));
   }

   /** {@code fn} implements neither {@code func_184645_a} nor {@code b(EntityPlayer)}: no menu, no talk. */
   @Override
   public boolean hasInteractionMenu() {
      return this.isTamedByLasso() || com.nxrtweetg.sexmod.config.SexmodConfig.isAllScenesEnabled();
   }

   /** {@code fn.t()} returns false unconditionally - her name plate is never drawn unless tamed. */
   @Override
   public boolean shouldShowName() {
      return this.isTamedByLasso() || this.hasCustomName();
   }

   /** {@code fn.func_180430_e} is empty, so a hop that lands badly costs her nothing. */
   @Override
   public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
      return false;
   }

   /** {@code fn.func_175134_bD()}. */
   @Override
   protected float getJumpPower() {
      return JUMP_POWER;
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(TRACKED_HORNY_LEVEL, 0);
      builder.define(TRACKED_POSE, Pose.IDLE.name());
   }

   public int hornyLevel() {
      return this.getEntityData().get(TRACKED_HORNY_LEVEL);
   }

   public void setHornyLevel(int level) {
      this.getEntityData().set(TRACKED_HORNY_LEVEL, Math.max(0, level));
   }

   public Pose slimePose() {
      return Pose.byName(this.getEntityData().get(TRACKED_POSE));
   }

   private void setSlimePose(Pose pose) {
      if (this.slimePose() != pose) {
         this.poseTicks = 0;
      }

      this.getEntityData().set(TRACKED_POSE, pose.name());
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput output) {
      super.addAdditionalSaveData(output);
      output.putInt("hornyLevel", this.hornyLevel());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      this.setHornyLevel(input.getIntOr("hornyLevel", 0));
      // The shared loader re-dresses an idle girl, because for everyone else the dressed model is the
      // resting state. Hers is not: once she has undressed she stays undressed ({@code fn.g()} only ever
      // sets the outfit at spawn), so a reload must not put her clothes back on.
      if (this.hornyLevel() > 0) {
         this.coreState().setCurrentModel(0);
         this.syncTrackedState();
      }
   }

   /**
    * The horny potion's shortcut into her ladder ({@code fn.func_70619_bc}): straight to hunting, and
    * out of her clothes if she is still in them. The original also tests {@code S == IDLE} there, but
    * {@code S} is only ever assigned on the client, so on the server that test is always true - it is
    * not a condition, and reproducing it would be reproducing a decompilation artefact.
    */
   @Override
   public void applyHornyPotion() {
      this.setHornyLevel(Math.max(HORNY_HUNTING, this.hornyLevel()));
      if (this.coreState().currentModel() == 1 && this.slimePose() != Pose.UNDRESS) {
         this.beginUndress();
      }
   }

   /**
    * {@code fn.b(fp.UNDRESS)}: a pose she plays out on the spot, not a scene. The shared keyframe road is
    * closed to it because that one needs a player to report the keyframe, and nobody is bound to her
    * here, so the take is timed off its own length instead.
    */
   private void beginUndress() {
      this.undressTicks = 0;
      this.setSlimePose(Pose.UNDRESS);
   }

   /** Whether her own tick owns her, i.e. the original's {@code y() == fp.NULL}. */
   private boolean isSelfDirected() {
      return !this.hasActiveScene() && this.slimePose() != Pose.UNDRESS;
   }
   @Override
   public void tick() {
      super.tick();
      if (this.level() instanceof ServerLevel serverLevel) {
         // The original's order, which matters: the grab is an AI task and so runs before the hop loop,
         // and the hop loop only runs while nothing else owns her.
         this.tickArousal(serverLevel);
         if (this.slimePose() == Pose.UNDRESS) {
            this.tickUndress();
         } else if (!this.hasActiveScene()) {
            if (!this.coreState().hasMaster() && !this.coreState().isGoingHome()) {
               this.tickJumpLoop();
            }
         } else {
            this.tickUnpromptedWait(serverLevel);
         }

         if (this.hornyLevel() >= HORNY_HUNTING && this.tickCount % 10 == 0) {
            serverLevel.sendParticles(
               ParticleTypes.HEART, this.getX(), this.getY() + 1.0 + this.random.nextDouble(), this.getZ(), 1, 1.0, 1.0, 1.0, this.random.nextGaussian()
            );
         }
      }
   }

   private void tickUndress() {
      this.undressTicks++;
      if (this.undressTicks == UNDRESS_REVEAL_TICKS && this.coreState().currentModel() != 0) {
         this.coreState().setCurrentModel(0);
         this.syncTrackedState();
      }

      if (this.undressTicks >= GirlAnimationLengths.ticks("slime", "undress", UNDRESS_FALLBACK_TICKS)) {
         this.undressTicks = 0;
         this.setSlimePose(Pose.IDLE);
      }
   }

   /**
    * {@code fn.b()}: the hop. The counter only advances while she is on the ground, so the loop is
    * "five seconds standing, then a jump", and the landing restarts it.
    */
   private void tickJumpLoop() {
      boolean onGround = this.onGround();
      // How long she has just spent in the air, read before this tick's contact clears it.
      int airborneFor = this.airTicks;
      this.tickJumpTakeHandover();
      if (this.groundTicks == FACING_CHOICE_TICK) {
         this.plannedJumpYaw = this.chooseJumpYaw();
      }

      if (this.groundTicks == JUMP_TAKE_TICK) {
         this.setSlimePose(Pose.JUMP_START);
      }

      if (this.groundTicks == JUMP_PERIOD_TICKS) {
         this.hop();
      }

      // A hop the impulse could not lift - a ceiling over her, a block against her, water around her -
      // otherwise leaves the take running over a slime who is standing perfectly still, and the take's own
      // hand-over then puts her in the looping airborne pose on the ground. The original cannot show this
      // because there the pose belongs to the client while the impulse belongs to the server, so the two
      // are never checked against each other; here one tick owns both, so it can notice and stand her back up.
      if (this.hopOwed && onGround && ++this.launchTicks > LAUNCH_GRACE_TICKS) {
         this.hopOwed = false;
         this.setSlimePose(Pose.IDLE);
      }

      // The roll happens on the landing tick and is then latched: one 10% chance per hop, not per tick.
      // A single tick of lost ground contact is not a landing, though: it is a player walking into her or a
      // step she scuffed, and treating it as one would throw away the ground count - which between tick 90
      // and tick 100 means cancelling the hop her take is already playing, and showing the squash instead.
      if (!this.wasOnGround && onGround && (this.hopOwed || airborneFor >= LANDING_MIN_AIR_TICKS)) {
         this.hopOwed = false;
         this.setSlimePose(Pose.JUMP_END);
         this.groundTicks = 0;
         this.arousalRoll = this.getRandom().nextFloat() < AROUSAL_CHANCE;
      }

      if (this.arousalRoll && this.groundTicks == AROUSAL_APPLY_TICK) {
         this.setHornyLevel(this.hornyLevel() + 1);
         if (this.hornyLevel() == 1) {
            this.beginUndress();
         }
      }

      if (onGround) {
         this.airTicks = 0;
         this.groundTicks++;
      } else {
         this.airTicks++;
      }

      this.wasOnGround = onGround;
   }

   /**
    * The half of her pose machine the hop loop does not drive. Her jump takes do not simply run out:
    * {@code jumpstart} names its own hand-over at 2.16s of 3.36 ({@code jumpStartDone} -> JUMP_AIR) and
    * {@code jumpend} at 2.2 of 2.36 ({@code jumpEndDone} -> IDLE), and the original reads both off the
    * animation as keyframes. It can, because there the pose is a client-side field and the client is
    * the one playing the take; ours is tracked data owned by this tick, so the times are read out of
    * the animation file instead and counted here.
    *
    * <p>Without the hand-over she never reaches her idle take at all: the landing squash is 2.36s
    * against the five seconds she spends standing, so it plays through and starts over, and a landing
    * animation replays in the middle of her standing still.
    */
   private void tickJumpTakeHandover() {
      Pose pose = this.slimePose();
      // The airborne take loops, so anything that leaves it behind on the floor leaves it there for good:
      // her landing is what normally ends it, and a hop that never happened has no landing to give.
      if (pose == Pose.JUMP_AIR && this.onGround() && !this.hopOwed) {
         this.setSlimePose(Pose.IDLE);
         return;
      }

      if (pose != Pose.JUMP_START && pose != Pose.JUMP_END) {
         return;
      }

      this.poseTicks++;
      if (pose == Pose.JUMP_START) {
         if (this.poseTicks >= GirlAnimationLengths.keyframeTicks("slime", "jumpstart", "jumpStartDone", JUMP_START_HANDOVER_TICKS)) {
            this.setSlimePose(Pose.JUMP_AIR);
         }
      } else if (this.poseTicks >= GirlAnimationLengths.keyframeTicks("slime", "jumpend", "jumpEndDone", JUMP_END_HANDOVER_TICKS)) {
         this.setSlimePose(Pose.IDLE);
      }
   }

   /** {@code fn.h()}: the jump itself keeps the vanilla vertical impulse and adds her own run-up. */
   private void hop() {
      this.setDeltaMovement(Vec3.ZERO);
      this.jumpFromGround();
      float yaw = Mth.wrapDegrees(this.plannedJumpYaw);
      this.setYRot(yaw);
      this.setYBodyRot(yaw);
      this.setYHeadRot(yaw);
      Vec3 forward = horizontalForward(yaw);
      this.setDeltaMovement(forward.x * JUMP_SPEED, this.getDeltaMovement().y, forward.z * JUMP_SPEED);
      // ServerEntity only broadcasts a position every updateInterval ticks unless something asks for it
      // sooner, so without this the client can be up to two ticks into the take before she visibly leaves
      // the ground. The pose change at tick 90 gets its own sync for free, being tracked data; the launch
      // itself changes nothing but her motion, which is exactly what this flag is for.
      this.hasImpulse = true;
      this.groundTicks = 0;
      this.hopOwed = true;
      this.launchTicks = 0;
   }

   /**
    * {@code fn.e()}: where the next hop goes. Anywhere at all, unless she is aroused and there is a free
    * player in range - then straight at him.
    */
   private float chooseJumpYaw() {
      if (this.hornyLevel() < HORNY_HUNTING) {
         return this.getRandom().nextFloat() * 360.0F;
      }

      Player player = this.level().getNearestPlayer(this, HUNT_RANGE);
      if (player == null || isBoundToAnyGirl(player)) {
         return this.getRandom().nextFloat() * 360.0F;
      }

      return (float)(Mth.atan2(this.getZ() - player.getZ(), this.getX() - player.getX()) * 180.0 / Math.PI) + 90.0F;
   }
   /**
    * {@code fn.a()}: what her arousal does once it is high enough. At 2 she can grab a player who walks
    * into her; at 4 she stops waiting for one and lies down where she stands.
    */
   private void tickArousal(ServerLevel world) {
      int horny = this.hornyLevel();
      if (horny < HORNY_HUNTING) {
         return;
      }

      boolean selfDirected = this.isSelfDirected();
      if (horny >= HORNY_UNPROMPTED && this.onGround() && selfDirected) {
         ServerPlayer audience = this.nearestFreePlayer(world, UNPROMPTED_AUDIENCE_RANGE);
         // The original starts the take with nobody bound and picks the player up later, out of the same
         // grab this method ends with. Our WAITING phase is that wait, and it needs to know whose scene it
         // is from the first tick, so she looks for him now instead - the pose and the hold are the same.
         if (audience != null) {
            this.unpromptedWaitTicks = 0;
            this.setSceneFloorAnchor();
            this.beginSelfStartedScene(audience, GirlSceneAction.DOGGY, GirlScenePhase.POSITIONING);
         }

         return;
      }

      // Deviation: the original does not test the state here, so a second player could be pulled into a
      // scene that was already running and take it over. Ours leaves a running scene alone.
      if (!selfDirected) {
         return;
      }

      ServerPlayer caught = this.nearestFreePlayer(world, GRAB_RANGE);
      if (caught != null && caught.onGround()) {
         this.setSceneFloorAnchor();
         this.beginSelfStartedScene(caught, GirlSceneAction.BLOWJOB, GirlScenePhase.ACTIVE);
      }
   }

   /**
    * The other side of the choice {@link #tickArousal} makes for her: she named a player before he had
    * agreed to anything, and until her scene ends he counts as taken - no other girl will look at him.
    * The original never has to answer for this, because it binds nobody until somebody is already
    * standing on top of her. So the commitment gets a limit: he has half a minute to come over, and
    * wandering off ends it sooner. {@link #resetScene} also drops her arousal, so she goes back to
    * hopping and can work her way up to trying again.
    */
   private void tickUnpromptedWait(ServerLevel world) {
      GirlScenePhase phase = this.trackedScenePhase();
      if (phase != GirlScenePhase.POSITIONING && phase != GirlScenePhase.WAITING) {
         this.unpromptedWaitTicks = 0;
         return;
      }

      UUID partnerId = this.coreState().activePlayerId();
      ServerPlayer partner = partnerId == null ? null : world.getServer().getPlayerList().getPlayer(partnerId);
      if (partner == null || partner.distanceToSqr(this) > UNPROMPTED_ABANDON_RANGE * UNPROMPTED_ABANDON_RANGE) {
         this.resetScene();
         return;
      }

      if (++this.unpromptedWaitTicks > UNPROMPTED_WAIT_TICKS) {
         this.resetScene();
      }
   }

   /** The nearest player in range who is not already in somebody's scene, or null. */
   private ServerPlayer nearestFreePlayer(ServerLevel world, double range) {
      Player player = world.getNearestPlayer(this, range);
      return player instanceof ServerPlayer serverPlayer && !isBoundToAnyGirl(serverPlayer) ? serverPlayer : null;
   }

   /**
    * {@code fn.d(EntityPlayer)}, which walks the whole loaded entity list looking for the girl whose scene
    * this player is in. A scene pins him within a block of her, so a box around him finds her just as
    * surely and does not touch every entity in the world twice a second.
    */
   private static boolean isBoundToAnyGirl(Player player) {
      return !player.level()
         .getEntitiesOfClass(
            SceneGirlEntity.class, player.getBoundingBox().inflate(16.0), girl -> player.getUUID().equals(girl.coreState().activePlayerId())
         )
         .isEmpty();
   }

   /**
    * The original gives her no movement controller at all, so her legs never play walk or idle: whatever
    * the action controller is doing is the whole of her. Returning the empty take here is how that reads
    * on our four-controller rig.
    */
   @Override
   protected RawAnimation selectCustomMovementAnimation(boolean moving) {
      return this.selectNamedAnimation("null", true);
   }

   /**
    * Her out-of-scene poses. The two jump takes play once and hold, because each one is handed over
    * before its end by {@link #tickJumpTakeHandover}; the resting poses loop.
    */
   @Override
   protected RawAnimation selectCustomActionAnimation() {
      if (this.sceneOwnsHer()) {
         return null;
      }

      Pose pose = this.slimePose();
      return this.selectNamedAnimation(pose.animation(), pose.looping());
   }

   /**
    * Whether a scene owns her, asked in a way that answers the same on both sides: the phase fields are
    * server-only, but the bound player and the custom-interaction id are both tracked.
    */
   private boolean sceneOwnsHer() {
      return this.isBusyTracked() || !this.customActionId().isEmpty();
   }

   @Override
   public boolean hasPaymentPhase(GirlSceneAction action) {
      return false;
   }

   @Override
   public int paymentAmountFor(GirlSceneAction action) {
      return 0;
   }

   @Override
   protected void playSoundVariant(GirlSoundSet.Voice voice, float volume) {
      if (voice.baseId().startsWith("misc.")) {
         super.playSoundVariant(voice, volume);
      }
   }

   @Override
   protected void playSoundIndex(GirlSoundSet.Voice voice, int index, float volume) {
      if (voice.baseId().startsWith("misc.")) {
         super.playSoundIndex(voice, index, volume);
      }
   }

   private void playSlimeSound(SoundEvent sound, float volume, float pitch) {
      if (this.level().isClientSide()) {
         this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), sound, SoundSource.NEUTRAL, volume, pitch, false);
      } else {
         this.level().playSound(null, this.getX(), this.getY(), this.getZ(), sound, SoundSource.NEUTRAL, volume, pitch);
      }
   }

   @Override
   protected void sayDialogue(String key) {
      // Slime never speaks
   }

   @Override
   protected void sayPlayerRequest() {
      // Slime has no dialogue requests
   }

   /** Authentic 1.12.2 sound mappings for Slime (fn.class): uses vanilla slime sounds and misc FX. */
   @Override
   protected boolean handleCustomSoundKeyframe(String keyframe) {
      switch (keyframe) {
         case "jumpStart" -> {
            this.playSlimeSound(SoundEvents.SLIME_JUMP, 1.0F, 1.0F);
            return true;
         }
         case "jumpEndSound" -> {
            this.playSlimeSound(SoundEvents.SLIME_SQUISH, 1.0F, 1.0F);
            return true;
         }
         case "bjiMSG11" -> {
            this.playSlimeSound(SoundEvents.SLIME_SQUISH, 0.5F, 1.0F);
            com.nxrtweetg.sexmod.client.GirlHornyMeter.add(0.02);
            return true;
         }
         case "bjiMSG12" -> {
            this.playSlimeSound(this.random.nextBoolean() ? SoundEvents.SLIME_JUMP : SoundEvents.SLIME_SQUISH, 0.5F, 1.0F);
            com.nxrtweetg.sexmod.client.GirlHornyMeter.add(0.02);
            return true;
         }
         case "bjtMSG1" -> {
            this.playSlimeSound(SoundEvents.SLIME_HURT, 1.0F, 1.0F);
            this.playSlimeSound(SoundEvents.SLIME_ATTACK, 1.0F, 1.0F);
            com.nxrtweetg.sexmod.client.GirlHornyMeter.add(0.04);
            return true;
         }
         case "bjcMSG1", "bjcMSG2" -> {
            this.playSlimeSound(SoundEvents.SLIME_JUMP, 1.0F, 1.0F);
            return true;
         }
         case "doggyGoOnBedMSG1" -> {
            this.playSlimeSound(SoundEvents.SLIME_SQUISH, 1.0F, 1.0F);
            return true;
         }
         case "doggystartMSG1", "doggystartMSG2" -> {
            this.playSoundVariant("misc.touch.touch", 2, 1.0F);
            return true;
         }
         case "doggystartMSG3" -> {
            this.playSlimeSound(SoundEvents.SLIME_SQUISH, 0.25F, 1.0F);
            return true;
         }
         case "doggystartMSG4" -> {
            this.playSoundVariant("misc.inserts.inserts", 5, 1.5F);
            return true;
         }
         case "doggystartMSG5" -> {
            this.playSoundVariant("misc.pounding.pounding", 36, 0.33F);
            this.playSlimeSound(SoundEvents.SLIME_HURT, 1.0F, 1.0F);
            return true;
         }
         case "doggyslowMSG1" -> {
            this.playSoundVariant("misc.pounding.pounding", 36, 0.33F);
            int r = this.random.nextInt(3);
            if (r == 0) {
               this.playSlimeSound(SoundEvents.SLIME_HURT, 1.0F, 1.0F);
            } else if (r == 1) {
               this.playSlimeSound(SoundEvents.SLIME_JUMP, 1.0F, 1.0F);
            } else {
               this.playSlimeSound(SoundEvents.SLIME_SQUISH, 1.0F, 1.0F);
            }
            com.nxrtweetg.sexmod.client.GirlHornyMeter.add(0.00666);
            return true;
         }
         case "doggyslowMSG2" -> {
            this.playSlimeSound(SoundEvents.SLIME_HURT, 1.0F, 1.0F);
            return true;
         }
         case "doggyfastMSG1" -> {
            this.playSoundVariant("misc.pounding.pounding", 36, 0.75F);
            int r = this.random.nextInt(3);
            if (r == 0) {
               this.playSlimeSound(SoundEvents.SLIME_HURT, 1.0F, 1.0F);
            } else if (r == 1) {
               this.playSlimeSound(SoundEvents.SLIME_JUMP, 1.0F, 1.0F);
            } else {
               this.playSlimeSound(SoundEvents.SLIME_SQUISH, 1.0F, 1.0F);
            }
            com.nxrtweetg.sexmod.client.GirlHornyMeter.add(0.02);
            return true;
         }
         case "doggycumMSG1" -> {
            this.playSoundVariant("misc.cuminflation.cuminflation", 1, 4.0F);
            this.playSoundVariant("misc.pounding.pounding", 36, 2.0F);
            this.playSlimeSound(SoundEvents.SLIME_ATTACK, 1.0F, 1.0F);
            return true;
         }
         default -> {
            if (keyframe.startsWith("bjiMSG") || keyframe.startsWith("doggycumMSG")
               || keyframe.equals("jumpStartDone") || keyframe.equals("jumpEndDone") || keyframe.equals("undress")) {
               return true;
            }
            return false;
         }
      }
   }

   /**
    * Deviation: {@code fn} never resets {@code hornyLevel}, so a slime who has reached 4 lies straight back
    * down the tick her last scene ended and can never be left alone again. Dropping it here keeps
    * everything the ladder is for - she still hunts, still grabs, still starts her own scenes - and gives
    * her a cool-down between them.
    */
   @Override
   public void resetScene() {
      super.resetScene();
      this.setHornyLevel(0);
      this.groundTicks = 0;
      this.arousalRoll = false;
      this.undressTicks = 0;
      this.hopOwed = false;
      this.launchTicks = 0;
      this.airTicks = 0;
      this.unpromptedWaitTicks = 0;
      // Her scenes can begin in mid-hop - the grab only asks the player to be on the ground, not her - and
      // a stale "she was airborne" would then read the first tick back on her own feet as a landing and
      // play the squash take out of nowhere.
      this.wasOnGround = this.onGround();
      this.setSlimePose(Pose.IDLE);
   }
}
