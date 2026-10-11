package com.nxrtweetg.sexmod.girls.bee;

import com.geckolib.animation.RawAnimation;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationRole;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationSet;
import com.nxrtweetg.sexmod.girls.anim.GirlSoundSet;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.girls.scene.GirlScenePhase;
import com.nxrtweetg.sexmod.girls.state.GirlActivity;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SpellParticleOption;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The bee, {@code fz} in the original.
 *
 * <p>She is the one companion who is not a shopkeeper and not a stranger either: she sells nothing and
 * refuses every request ({@code fz.a(String, UUID)} is empty), but she flies, she can be tamed, she can
 * be given a satchel to carry, and she follows the player who tamed her. What she does have is a slow
 * arousal counter; when it fills she comes looking for you herself, and the scene she starts is the one
 * chance to tame her.
 *
 * <p>Her potion shortcut is live: {@code sexmod:horny} lands on her like any other status effect and
 * {@link #applyHornyPotion()} is what the effect calls, exactly as on the slime.
 */
public final class BeeEntity extends SceneGirlEntity {
   /** {@code fz.M}, NBT {@code isTamed}. */
   private static final EntityDataAccessor<Boolean> TRACKED_TAMED = SynchedEntityData.defineId(BeeEntity.class, EntityDataSerializers.BOOLEAN);
   /** {@code em.K}, NBT {@code hasChest}: declared on the original's base girl, ours alone needs it. */
   private static final EntityDataAccessor<Boolean> TRACKED_HAS_CHEST = SynchedEntityData.defineId(
      BeeEntity.class, EntityDataSerializers.BOOLEAN
   );

   /** {@code fz.N}: where her arousal starts, and {@code fz.O}, where it has to get to. */
   private static final float AROUSAL_START = 3200.0F;
   private static final float AROUSAL_THRESHOLD = 4800.0F;
   /** {@code fz.Q}: how far she will look for someone once she is ready. */
   private static final double SEARCH_RANGE = 10.0;
   /** {@code fz.c()}: and how close he has to be before she stops flying and starts. */
   private static final double GRAB_RANGE = 1.5;
   /** What the potion sets {@code N} to: far past the threshold, so it survives a whole scene. */
   private static final float POTION_AROUSAL = 6.9420184E7F;
   /** {@code fz.b()}: she will not climb more than three blocks above whatever is under her. */
   private static final double ALTITUDE_LIMIT = 3.0;
   /** {@code fz.func_70071_h_}: how hard a descent is braked while she is still waiting to fill. */
   private static final double HOVER_DAMPING = 0.4;
   /** {@code fz.a()}: the taming counter's four gates, in the order it tests them. */
   private static final int TAMED_HEART_TICKS = 40;
   private static final int TAMING_ROLL_TICK = 200;
   private static final int TAMING_REACTION_END = 250;
   /** {@code em.L}: a single chest's worth, which is what her satchel screen shows. */
   private static final int SATCHEL_SIZE = 27;
   /** The id the interaction menu sends back when the satchel row is clicked. */
   private static final String SATCHEL_ACTION = "bee_satchel";

   /** Her own takes; the rest fall back to Jenny, whose rig hers is built on. */
   private static final Set<String> OWN_ANIMATIONS = Set.of(
      "attack0", "attack1", "attack2", "bowcharge", "idle", "idle_has_chest", "null", "sex_cum",
      "sex_fast", "sex_slow", "sex_start", "throw_pearl"
   );
   /**
    * Her pearl take is spelled {@code throw_pearl} where Jenny's is {@code throwpearl}, so the role is
    * aliased rather than left to fall back onto Jenny's file.
    */
   private static final GirlAnimationSet ANIMATIONS = new GirlAnimationSet(
      "bee", OWN_ANIMATIONS, Map.of(GirlAnimationRole.THROW_PEARL, "throw_pearl")
   );
   /** She ships no voice of her own: she never speaks, and her scene is scored by its sound keyframes. */
   private static final GirlSoundSet SOUNDS = new GirlSoundSet("bee", Map.of());

   /** {@code fz.N}: her arousal, counted up one per tick while nothing else has her. */
   private float arousal = AROUSAL_START;
   /** {@code fz.P}: the taming counter, dormant at 0 and armed by the climax of her own scene. */
   private int tamingTicks;
   /** {@code em.L}: the satchel, which only exists once someone has given her a chest. */
   private final SimpleContainer satchel = new SimpleContainer(SATCHEL_SIZE);

   public BeeEntity(EntityType<? extends BeeEntity> entityType, Level world) {
      super(entityType, world, "Bee");
      this.moveControl = new FlyingMoveControl<>(this, 20, true);
   }

   @Override
   public String characterId() {
      return "bee";
   }

   @Override
   protected GirlAnimationSet animations() {
      return ANIMATIONS;
   }

   @Override
   protected GirlSoundSet sounds() {
      return SOUNDS;
   }

   /**
    * She sells nothing. {@code fz.a(String, UUID)} is an empty override of the scene-request entry point,
    * so every menu row and every command that would start a scene has nothing to offer for her; the one
    * scene she has, she starts herself.
    */
   @Override
   protected Set<GirlSceneAction> sceneActions() {
      return EnumSet.noneOf(GirlSceneAction.class);
   }

   /** {@code fz.i()}: her name plate sits a little lower than a girl's, because she is shorter. */
   @Override
   public double nameplateYOffset() {
      return -0.1;
   }
   /**
    * {@code fz.func_184651_r()}: panic, swim, watch you, wander the air. Her priority-0 goal in the
    * original is {@code h}, a follow-master goal, and our {@link com.nxrtweetg.sexmod.girls.GirlFollowMasterGoal}
    * is the same thing, so the pair of companion goals comes from the shared set and the rest is hers.
    */
   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new com.nxrtweetg.sexmod.girls.GirlFollowMasterGoal(this));
      this.goalSelector.addGoal(1, new com.nxrtweetg.sexmod.girls.GirlGoHomeGoal(this));
      this.goalSelector.addGoal(1, new PanicGoal(this, 1.25));
      this.goalSelector.addGoal(1, new FloatGoal(this));
      this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
      this.goalSelector.addGoal(3, new WaterAvoidingRandomFlyingGoal(this, 1.0));
   }

   /** {@code fz.func_175447_b}: she flies, ignores doors she cannot open, and floats rather than sinks. */
   @Override
   protected PathNavigation createNavigation(Level world) {
      FlyingPathNavigation navigation = new FlyingPathNavigation(this, world);
      navigation.setCanOpenDoors(false);
      navigation.getNodeEvaluator().setCanPassDoors(true);
      navigation.setCanFloat(true);
      return navigation;
   }

   /** {@code fz.func_180430_e} is empty: she is airborne by nature and never takes a landing badly. */
   @Override
   public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
      return false;
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(TRACKED_TAMED, false);
      builder.define(TRACKED_HAS_CHEST, false);
   }

   public boolean isTamed() {
      return this.getEntityData().get(TRACKED_TAMED);
   }

   private void setTamed(boolean tamed) {
      this.getEntityData().set(TRACKED_TAMED, tamed);
   }

   /** Read by the renderer every frame: her satchel bone only exists while this is true. */
   public boolean hasChest() {
      return this.getEntityData().get(TRACKED_HAS_CHEST);
   }

   private void setHasChest(boolean hasChest) {
      this.getEntityData().set(TRACKED_HAS_CHEST, hasChest);
   }

   /**
    * {@code fz.func_184645_a}: an untamed bee has no handler at all, and a tamed one only answers a right
    * click - so before she is tamed there is nothing to open and nothing to give.
    */
   @Override
   public boolean hasInteractionMenu() {
      return this.isTamed() || this.isTamedByLasso() || com.nxrtweetg.sexmod.config.SexmodConfig.isAllScenesEnabled();
   }

   /**
    * The chest is a gift, not a menu row, so the click has to reach the server. Every other click on a
    * tamed bee is still swallowed by the menu hook.
    */
   @Override
   public boolean defersInteractionToServer(ItemStack held) {
      return super.defersInteractionToServer(held) || (this.isTamed() && !this.hasChest() && held.is(Items.CHEST));
   }

   /** She has no voice, so all five companion commands read as things she is seen to do. */
   @Override
   public String commandDialogueKey(String suffix) {
      return "bee.dialogue." + suffix;
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack held = player.getItemInHand(hand);
      if (this.isTamed() && !this.hasChest() && held.is(Items.CHEST)) {
         this.setHasChest(true);
         held.shrink(1);
         this.playSound(net.minecraft.sounds.SoundEvents.CHEST_OPEN, 0.7F, 1.2F);
         return InteractionResult.SUCCESS;
      }

      return super.mobInteract(player, hand);
   }
   /** {@code ch.java}'s chest icon: one extra row on the menu, and only once she is carrying one. */
   @Override
   protected List<InteractionSection> extraInteractionSections() {
      if (!this.hasChest()) {
         return List.of();
      }

      return List.of(this.row(this.option(SATCHEL_ACTION, "action.names.satchel", ItemStack.EMPTY)));
   }

   /**
    * The satchel is a plain chest inventory, so it goes through the vanilla menu rather than the girls'
    * typed equipment screen - which means no client-side screen registration of its own.
    */
   @Override
   public boolean tryBeginCustomInteraction(ServerPlayer player, String actionId) {
      if (!SATCHEL_ACTION.equals(actionId)) {
         return false;
      }

      if (this.hasChest()) {
         player.openMenu(
            new SimpleMenuProvider(
               (containerId, inventory, opener) -> ChestMenu.threeRows(containerId, inventory, this.satchel), this.getName()
            )
         );
      }

      return true;
   }

   /** Whatever she was carrying spills with her equipment, so a chest is never lost to a bad landing. */
   @Override
   protected void dropCustomDeathLoot(ServerLevel world, DamageSource source, boolean hitByPlayer) {
      super.dropCustomDeathLoot(world, source, hitByPlayer);
      if (this.hasChest()) {
         this.spawnAtLocation(world, new ItemStack(Items.CHEST));
      }

      for (int slot = 0; slot < this.satchel.getContainerSize(); slot++) {
         ItemStack stack = this.satchel.removeItemNoUpdate(slot);
         if (!stack.isEmpty()) {
            this.spawnAtLocation(world, stack);
         }
      }
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput output) {
      super.addAdditionalSaveData(output);
      output.putBoolean("isTamed", this.isTamed());
      output.putBoolean("hasChest", this.hasChest());
      output.putFloat("arousal", this.arousal);
      output.putInt("tamingTicks", this.tamingTicks);
      ValueOutput satchelOutput = output.child("Satchel");

      for (int slot = 0; slot < this.satchel.getContainerSize(); slot++) {
         ItemStack stack = this.satchel.getItem(slot);
         if (!stack.isEmpty()) {
            satchelOutput.store(String.valueOf(slot), ItemStack.CODEC, stack);
         }
      }
   }

   @Override
   protected void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      this.setTamed(input.getBooleanOr("isTamed", false));
      this.setHasChest(input.getBooleanOr("hasChest", false));
      this.arousal = input.getFloatOr("arousal", AROUSAL_START);
      this.tamingTicks = input.getIntOr("tamingTicks", 0);
      this.satchel.clearContent();
      ValueInput satchelInput = input.childOrEmpty("Satchel");

      for (int slot = 0; slot < this.satchel.getContainerSize(); slot++) {
         int index = slot;
         satchelInput.read(String.valueOf(slot), ItemStack.CODEC).ifPresent(stack -> this.satchel.setItem(index, stack));
      }
   }

   /**
    * The potion's shortcut into her arousal ({@code fz.func_70619_bc}): it pushes the counter so far past
    * the threshold that she comes for you now and stays willing afterwards. Refused while a scene already
    * has her, which is the original's {@code ae() == null} test.
    */
   @Override
   public void applyHornyPotion() {
      if (this.arousal < AROUSAL_THRESHOLD && this.coreState().activePlayerId() == null) {
         this.arousal = POTION_AROUSAL;
      }
   }
   @Override
   public void tick() {
      super.tick();

      // fz.func_70071_h_: a bee who is still filling up hovers rather than drops, so her descent is braked
      // to a drift. Once she is ready she falls normally, because she is about to land on somebody.
      if (this.arousal < AROUSAL_THRESHOLD && !this.onGround() && this.getDeltaMovement().y < 0.0) {
         Vec3 movement = this.getDeltaMovement();
         this.setDeltaMovement(movement.x, movement.y * HOVER_DAMPING, movement.z);
      }

      this.applyAltitudeCap();
      if (this.level() instanceof ServerLevel serverLevel) {
         // fz.func_70619_bc's order: the arousal hunt first, then the taming counter, which the climax of
         // the scene the hunt started is what arms.
         this.tickArousal(serverLevel);
         if (this.trackedScenePhase() == GirlScenePhase.CLIMAX) {
            this.tamingTicks = Math.max(1, this.tamingTicks);
         }

         this.tickTaming(serverLevel);
      }
   }

   /**
    * {@code fz.b()}: a downward ray from where she is, and any upward motion is cut the moment she is more
    * than three blocks above what it hits. It is what keeps her at flower height instead of drifting into
    * the clouds on the wander goal.
    */
   private void applyAltitudeCap() {
      if (this.getDeltaMovement().y <= 0.0) {
         return;
      }

      Vec3 from = this.position();
      BlockHitResult hit = this.level()
         .clip(new ClipContext(from, new Vec3(from.x, this.level().getMinY(), from.z), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
      if (this.getY() - hit.getBlockPos().getY() > ALTITUDE_LIMIT) {
         Vec3 movement = this.getDeltaMovement();
         this.setDeltaMovement(movement.x, 0.0, movement.z);
      }
   }

   /**
    * {@code fz.c()}: the whole of her initiative, and the guard order matters. She only counts up while
    * she is nobody's - not in a scene, not following a master - and once she is full she flies at the
    * nearest free player until she is close enough to start.
    */
   private void tickArousal(ServerLevel world) {
      if (this.coreState().activePlayerId() != null || this.trackedHasMaster()) {
         return;
      }

      this.arousal++;
      if (this.arousal < AROUSAL_THRESHOLD) {
         return;
      }

      Player nearest = world.getNearestPlayer(this, SEARCH_RANGE);
      if (!(nearest instanceof ServerPlayer player) || isBoundToAnyGirl(player)) {
         return;
      }

      if (this.distanceTo(player) >= GRAB_RANGE) {
         this.getNavigation().stop();
         this.getNavigation().moveTo(player, 1.0);
         return;
      }

      this.arousal = 0.0F;
      this.getNavigation().stop();
      // She lands where she is and takes him with her: her scene has no bed, no offer and no walk over,
      // and its intro take is the whole of the lead-in.
      float facing = Mth.wrapDegrees(player.getYRot() - 180.0F);
      this.setYRot(facing);
      this.setYBodyRot(facing);
      this.setYHeadRot(facing);
      this.setSceneFloorAnchor();
      this.beginSelfStartedScene(player, GirlSceneAction.BEE_SEX, GirlScenePhase.INTRO);
   }

   /** The same box-around-the-player check the slime uses, for the same reason. */
   private static boolean isBoundToAnyGirl(Player player) {
      return !player.level()
         .getEntitiesOfClass(
            SceneGirlEntity.class, player.getBoundingBox().inflate(16.0), girl -> player.getUUID().equals(girl.coreState().activePlayerId())
         )
         .isEmpty();
   }
   /**
    * {@code fz.a()}: the taming machine, dormant until the climax of her own scene arms it and then
    * running on its own for as long as it takes.
    *
    * <p>Untamed, she spends 200 ticks in a cloud of magic, and on tick 200 exactly the coin is flipped. A
    * win ends the machine on the spot, because the next tick comes in tamed and past the 40-tick gate; a
    * loss leaves her counting out fifty ticks of angry villager before she gives up. Tamed and armed
    * again by a later scene, she simply hearts for those forty ticks. The heavy magic burst at the end is
    * unconditional in the original and fires every tick the counter is running.
    */
   private void tickTaming(ServerLevel world) {
      if (this.tamingTicks == 0) {
         return;
      }

      this.tamingTicks++;
      if (this.isTamed()) {
         if (this.tamingTicks >= TAMED_HEART_TICKS) {
            this.tamingTicks = 0;
         } else {
            this.emitParticles(world, ParticleTypes.HEART, 1);
         }
      } else if (this.tamingTicks < TAMING_ROLL_TICK) {
         this.emitParticles(world, spellParticle(), 1);
      } else if (this.tamingTicks == TAMING_ROLL_TICK) {
         this.setTamed(this.getRandom().nextBoolean());
      } else if (this.tamingTicks < TAMING_REACTION_END) {
         this.emitParticles(world, this.isTamed() ? ParticleTypes.HEART : ParticleTypes.ANGRY_VILLAGER, 3);
      } else {
         this.tamingTicks = 0;
      }

      this.emitParticles(world, spellParticle(), 10);
   }

   /** 1.12's {@code SPELL} is the plain white swirl, which modern {@code effect} needs told its colour. */
   private static ParticleOptions spellParticle() {
      return SpellParticleOption.create(ParticleTypes.EFFECT, 0xFFFFFF, 1.0F);
   }

   /**
    * Position, spread and speed are the original's exactly. The one thing that is not is the audience: it
    * hand-picked every player within 15 blocks, where the modern broadcast picks its own radius.
    */
   private void emitParticles(ServerLevel world, ParticleOptions particle, int count) {
      world.sendParticles(particle, this.getX(), this.getY() + 0.3, this.getZ(), count, 0.2, 0.3, 0.2, 0.25);
   }

   /**
    * She has no walk, run or fly take of her own, so leaving the movement controller to the shared table
    * would play Jenny's walk on her rig. Her idles are the whole of her locomotion, and the one that
    * carries the satchel is what keeps that bone in step with the flag the renderer reads.
    *
    * <p>The two whole-body poses the shared path yields to - the pearl throw that sends her home, and
    * lying downed - have to be yielded to here as well, or her idle plays over the top of them.
    */
   @Override
   protected RawAnimation selectCustomMovementAnimation(boolean moving) {
      if (this.isBusyTracked()
         || !this.customActionId().isEmpty()
         || this.activity() == GirlActivity.PEARL
         || this.activity() == GirlActivity.DOWNED) {
         return this.selectNamedAnimation("null", true);
      }

      return this.selectNamedAnimation(this.hasChest() ? "idle_has_chest" : "idle", true);
   }

   /**
    * Her scene's own score. The three {@code MSG1} cues are hers alone, and the arousal meter is written
    * from them because her takes carry no meter keyframes of their own; everything structural - the
    * {@code Done} and {@code Ready} names, the blackout, the pearl - has to fall through to the shared
    * table, so it must answer false here.
    */
   @Override
   protected boolean handleCustomSoundKeyframe(String keyframe) {
      switch (keyframe) {
         case "resetCumPercentage":
            // Her intro take has no openSexUI cue, so this is where the meter appears as well as resets.
            com.nxrtweetg.sexmod.client.GirlHornyMeter.show(true);
            com.nxrtweetg.sexmod.client.GirlHornyMeter.reset();
            return true;
         case "sex_startMSG1":
            this.playSoundVariant("misc.pounding.pounding", 36, 1.0F);
            this.addMeter(0.02);
            return true;
         case "sex_fastMSG1":
            this.playSoundVariant("misc.pounding.pounding", 36, 1.0F);
            this.addMeter(0.04);
            return true;
         case "sex_cumMSG1":
            this.playSoundAt("misc.cuminflation.cuminflation0", 2.0F);
            this.playSoundVariant("misc.pounding.pounding", 36, 1.0F);
            return true;
         default:
            return false;
      }
   }

   /** Only the client whose scene this is may fill the meter; a bystander watching must not. */
   private void addMeter(double amount) {
      if (this.isSceneOwnedByLocalPlayer()) {
         com.nxrtweetg.sexmod.client.GirlHornyMeter.add(amount);
      }
   }
}
