package com.nxrtweetg.sexmod.girls;

import com.nxrtweetg.sexmod.girls.inventory.GirlEquipmentMenuProvider;
import com.nxrtweetg.sexmod.girls.inventory.GirlEquipmentSlot;
import com.nxrtweetg.sexmod.girls.state.GirlActivity;
import com.nxrtweetg.sexmod.girls.state.GirlAnimationParameter;
import com.nxrtweetg.sexmod.girls.state.GirlCoreState;
import com.nxrtweetg.sexmod.girls.state.GirlFollowUpAction;
import com.nxrtweetg.sexmod.girls.state.GirlPaymentItem;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class GirlEntity extends PathfinderMob {
   private static final double DEFAULT_WALK_SPEED = 0.35;
   private static final EntityDataAccessor<String> TRACKED_ACTIVITY = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<Integer> TRACKED_ATTACK_VARIANT = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.INT);
   /** The menu hides "follow me" or "stop follow me" depending on this, so the client needs it too. */
   private static final EntityDataAccessor<Boolean> TRACKED_HAS_MASTER = SynchedEntityData.defineId(GirlEntity.class, EntityDataSerializers.BOOLEAN);
   private final GirlCoreState coreState = new GirlCoreState();
   private final SimpleContainer equipment = new SimpleContainer(GirlEquipmentSlot.Kind.COUNT);
   private GirlActivity activity = GirlActivity.NONE;

   protected GirlEntity(EntityType<? extends PathfinderMob> entityType, Level world) {
      super(entityType, world);
      this.coreState.setWalkSpeed(0.35);
   }

   @Override
   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(TRACKED_ACTIVITY, GirlActivity.NONE.id());
      builder.define(TRACKED_ATTACK_VARIANT, 0);
      builder.define(TRACKED_HAS_MASTER, false);
   }

   public SimpleContainer equipmentContainer() {
      return this.equipment;
   }

   /**
    * Projects the six typed equipment slots onto the vanilla ones. This one override is what makes
    * the equipment actually mean something: vanilla then handles armour value, enchantments,
    * {@code doHurtTarget} damage and the equipment sync packet on its own.
    */
   @Override
   public ItemStack getItemBySlot(EquipmentSlot slot) {
      GirlEquipmentSlot.Kind kind = kindForVanillaSlot(slot);
      return kind == null ? ItemStack.EMPTY : this.equipment.getItem(kind.ordinal());
   }

   @Override
   public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
      GirlEquipmentSlot.Kind kind = kindForVanillaSlot(slot);
      if (kind == null) {
         super.setItemSlot(slot, stack);
      } else {
         this.equipment.setItem(kind.ordinal(), stack);
      }
   }

   private static GirlEquipmentSlot.Kind kindForVanillaSlot(EquipmentSlot slot) {
      return switch (slot) {
         case MAINHAND -> GirlEquipmentSlot.Kind.WEAPON;
         case OFFHAND -> GirlEquipmentSlot.Kind.BOW;
         case HEAD -> GirlEquipmentSlot.Kind.HELMET;
         case CHEST -> GirlEquipmentSlot.Kind.CHESTPLATE;
         case LEGS -> GirlEquipmentSlot.Kind.LEGGINGS;
         case FEET -> GirlEquipmentSlot.Kind.BOOTS;
         default -> null;
      };
   }

   /**
    * Her gear is the player's, kept in the container and dropped whole on death. Vanilla's per-slot
    * drop roll would otherwise duplicate it, since it reads through the projection above.
    */
   @Override
   protected void dropCustomDeathLoot(ServerLevel world, DamageSource source, boolean hitByPlayer) {
      for (int slot = 0; slot < this.equipment.getContainerSize(); slot++) {
         ItemStack stack = this.equipment.removeItemNoUpdate(slot);
         if (!stack.isEmpty()) {
            this.spawnAtLocation(world, stack);
         }
      }
   }

   /** Nothing may shove items into the typed slots past the screen's filters. */
   @Override
   protected boolean canDispenserEquipIntoSlot(EquipmentSlot slot) {
      return false;
   }

   @Override
   public boolean canHoldItem(ItemStack stack) {
      return false;
   }

   public GirlActivity activity() {
      return this.activity;
   }

   /** Client-side readers get the synced value; the server keeps the field authoritative. */
   public GirlActivity resolvedActivity() {
      return this.level().isClientSide() ? GirlActivity.fromId(this.entityData.get(TRACKED_ACTIVITY)) : this.activity;
   }

   /** Scenes always win: nothing may drive her into a combat or pearl state mid-scene. */
   public void setActivity(GirlActivity next) {
      if (next != GirlActivity.NONE && this.hasActiveScene()) {
         return;
      }

      this.activity = next;
      this.entityData.set(TRACKED_ACTIVITY, next.id());
   }

   public int attackVariant() {
      return this.entityData.get(TRACKED_ATTACK_VARIANT);
   }

   public void cycleAttackVariant() {
      this.entityData.set(TRACKED_ATTACK_VARIANT, (this.attackVariant() + 1) % 3);
   }

   /** Leaving the downed state must never be blocked, or she stays invulnerable forever. */
   public void reviveFromDowned() {
      this.activity = GirlActivity.NONE;
      this.entityData.set(TRACKED_ACTIVITY, GirlActivity.NONE.id());
   }

   public boolean isDowned() {
      return this.activity == GirlActivity.DOWNED;
   }

   /**
    * Whether a scene owns the entity. Overridden by SceneGirlEntity, which additionally tracks a phase
    * and an action of its own; the base class only knows about the interaction owner.
    */
   public boolean hasActiveScene() {
      return this.hasActiveInteraction();
   }

   /** Opens the equipment screen; the original routed this through a Forge GUI handler. */
   public void openEquipmentScreen(ServerPlayer player) {
      player.openMenu(new GirlEquipmentMenuProvider(this));
   }

   public static AttributeSupplier.Builder createGirlAttributes() {
      return LivingEntity.createLivingAttributes()
         .add(Attributes.MAX_HEALTH, 15.0)
         .add(Attributes.FOLLOW_RANGE, 30.0)
         .add(Attributes.MOVEMENT_SPEED, 0.5)
         .add(Attributes.ATTACK_DAMAGE, 1.0)
         .add(Attributes.ATTACK_SPEED, 4.0);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new GirlGoHomeGoal(this));
      this.goalSelector.addGoal(2, new GirlBowAttackGoal(this));
      this.goalSelector.addGoal(2, new GirlMeleeAttackGoal(this));
      this.goalSelector.addGoal(3, new GirlFollowMasterGoal(this));
      this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.35));
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 3.0F));
      this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

      this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
      this.targetSelector.addGoal(2, new GirlMasterHurtTargetGoal(this));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false, this::isValidCombatTarget));
   }

   /**
    * The original also required the target to be near the master, so a bodyguard does not wander off
    * after something she merely noticed (g_class338: within 20 of herself, within 15 of the master).
    */
   private boolean isValidCombatTarget(LivingEntity candidate, ServerLevel world) {
      if (candidate instanceof GirlEntity || candidate instanceof Creeper || this.hasActiveScene() || this.isDowned()) {
         return false;
      }

      if (this.distanceToSqr(candidate) > 20.0 * 20.0) {
         return false;
      }

      Player master = this.master();
      return master == null || candidate.distanceToSqr(master) < 15.0 * 15.0;
   }

   public Player master() {
      return this.coreState.hasMaster() ? this.level().getPlayerByUUID(this.coreState.masterId()) : null;
   }

   /** The follow/home commands from the girl menu; the original kept these on the "master" parameter. */
   public void startFollowing(Player player) {
      this.coreState.setMasterId(player.getUUID());
      this.coreState.setGoingHome(false);
      this.syncHasMaster();
   }

   public void stopFollowing() {
      this.coreState.setMasterId(null);
      this.syncHasMaster();
   }

   public void sendHome() {
      this.coreState.setMasterId(null);
      this.coreState.setGoingHome(this.coreState.homePos() != null);
      this.syncHasMaster();
   }

   public void setHomeHere() {
      this.coreState.setHomePos(this.blockPosition());
      this.coreState.setGoingHome(false);
   }

   private void syncHasMaster() {
      this.entityData.set(TRACKED_HAS_MASTER, this.coreState.hasMaster());
   }

   public boolean trackedHasMaster() {
      return this.entityData.get(TRACKED_HAS_MASTER);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput output) {
      super.addAdditionalSaveData(output);
      output.putInt("CurrentModel", this.coreState.currentModel());
      output.putString("GirlName", this.coreState.girlName());
      output.putBoolean("ShouldBeAtTarget", this.coreState.shouldBeAtTarget());
      output.putBoolean("ShouldBeAtTargetYaw", this.coreState.shouldBeAtTargetYaw());
      output.putBoolean("ShouldGetDamage", this.coreState.shouldGetDamage());
      output.putBoolean("PlayerIsInPosition", this.coreState.playerIsInPosition());
      output.putBoolean("PlayerIsThrusting", this.coreState.playerIsThrusting());
      output.putBoolean("PlayerIsCumming", this.coreState.playerIsCumming());
      output.putBoolean("IsForPreloading", this.coreState.isForPreloading());
      output.putDouble("WalkSpeed", this.coreState.walkSpeed());
      output.putFloat("TargetYaw", this.coreState.targetYaw());
      output.putFloat("PlayerYaw", this.coreState.playerYaw());
      output.putInt("PaymentItemsAmount", this.coreState.paymentItemsAmount());
      output.putString("PaymentItem", this.coreState.paymentItem().name());
      output.putString("FollowUpAction", this.coreState.followUpAction().name());
      output.storeNullable("ActivePlayerId", UUIDUtil.CODEC, this.coreState.activePlayerId());
      output.storeNullable("MasterId", UUIDUtil.CODEC, this.coreState.masterId());
      output.storeNullable("HomePos", BlockPos.CODEC, this.coreState.homePos());
      output.putBoolean("GoingHome", this.coreState.isGoingHome());
      output.putString("Activity", this.activity.id());

      ValueOutput equipmentOutput = output.child("Equipment");

      for (int slot = 0; slot < this.equipment.getContainerSize(); slot++) {
         ItemStack stack = this.equipment.getItem(slot);
         if (!stack.isEmpty()) {
            // stored per slot index: the slots are typed, so their order must survive a reload
            equipmentOutput.store(String.valueOf(slot), ItemStack.CODEC, stack);
         }
      }

      ValueOutput animationOutput = output.child("AnimationState");

      for (Entry<GirlAnimationParameter, Boolean> entry : this.coreState.animationState().snapshot().entrySet()) {
         animationOutput.putBoolean(entry.getKey().name(), entry.getValue());
      }
   }

   @Override
   protected void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      this.coreState.setCurrentModel(input.getIntOr("CurrentModel", this.coreState.currentModel()));
      input.getString("GirlName").ifPresent(this.coreState::setGirlName);
      this.coreState.setShouldBeAtTarget(input.getBooleanOr("ShouldBeAtTarget", false));
      this.coreState.setShouldBeAtTargetYaw(input.getBooleanOr("ShouldBeAtTargetYaw", false));
      this.coreState.setShouldGetDamage(input.getBooleanOr("ShouldGetDamage", true));
      this.coreState.setPlayerIsInPosition(input.getBooleanOr("PlayerIsInPosition", false));
      this.coreState.setPlayerIsThrusting(input.getBooleanOr("PlayerIsThrusting", false));
      this.coreState.setPlayerIsCumming(input.getBooleanOr("PlayerIsCumming", false));
      this.coreState.setForPreloading(input.getBooleanOr("IsForPreloading", false));
      this.coreState.setWalkSpeed(input.getDoubleOr("WalkSpeed", DEFAULT_WALK_SPEED));
      this.coreState.setTargetYaw(input.getFloatOr("TargetYaw", 0.0F));
      this.coreState.setPlayerYaw(input.getFloatOr("PlayerYaw", 0.0F));
      this.coreState.setPaymentItemsAmount(input.getIntOr("PaymentItemsAmount", 0));
      input.getString("PaymentItem").ifPresent(name -> this.coreState.setPaymentItem(GirlPaymentItem.valueOf(name)));
      input.getString("FollowUpAction").ifPresent(name -> this.coreState.setFollowUpAction(GirlFollowUpAction.valueOf(name)));
      this.coreState.setActivePlayerId(input.read("ActivePlayerId", UUIDUtil.CODEC).orElse(null));
      this.coreState.setMasterId(input.read("MasterId", UUIDUtil.CODEC).orElse(null));
      this.syncHasMaster();
      this.coreState.setHomePos(input.read("HomePos", BlockPos.CODEC).orElse(null));
      this.coreState.setGoingHome(input.getBooleanOr("GoingHome", false));
      // A pearl throw cannot be resumed across a reload, so it never survives one. Assigned directly:
      // setActivity refuses anything but NONE while a scene is loaded, which would drop a saved
      // DOWNED state and leave her standing at 1 HP.
      GirlActivity loadedActivity = GirlActivity.fromId(input.getStringOr("Activity", GirlActivity.NONE.id()));
      this.activity = loadedActivity == GirlActivity.PEARL ? GirlActivity.NONE : loadedActivity;
      this.entityData.set(TRACKED_ACTIVITY, this.activity.id());
      this.equipment.clearContent();
      ValueInput equipmentInput = input.childOrEmpty("Equipment");

      for (int slot = 0; slot < this.equipment.getContainerSize(); slot++) {
         int index = slot;
         equipmentInput.read(String.valueOf(slot), ItemStack.CODEC).ifPresent(stack -> this.equipment.setItem(index, stack));
      }

      ValueInput animationInput = input.childOrEmpty("AnimationState");

      for (GirlAnimationParameter girlanimationparameter : GirlAnimationParameter.values()) {
         this.coreState
            .animationState()
            .set(
               girlanimationparameter,
               animationInput.getBooleanOr(girlanimationparameter.name(), this.coreState.animationState().get(girlanimationparameter))
            );
      }
   }

   public boolean removeWhenFarAway(double distanceSquared) {
      return false;
   }

   public boolean isPersistenceRequired() {
      return true;
   }

   public GirlCoreState coreState() {
      return this.coreState;
   }

   public void setActivePlayer(UUID playerId) {
      this.coreState.setActivePlayerId(playerId);
   }

   public void setFollowUpAction(GirlFollowUpAction action) {
      this.coreState.setFollowUpAction(action);
   }

   public void resetScene() {
      this.coreState.clearInteraction();
   }

   /**
    * A girl with a master goes down instead of dying, as in the original (g_class338's hurt handler
    * clamped the killing blow and set the downed flag). Damage that bypasses invulnerability still
    * gets through, so /kill and the void are not defeated by it.
    */
   public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
      if (this.hasActiveInteraction() || !this.coreState.shouldGetDamage()) {
         return false;
      }

      if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
         return super.hurtServer(world, source, amount);
      }

      if (this.isDowned()) {
         return false;
      }

      if (amount >= this.getHealth() && this.coreState.hasMaster()) {
         this.setActivity(GirlActivity.DOWNED);
         this.setHealth(1.0F);
         this.setTarget(null);
         this.getNavigation().stop();
         return true;
      }

      return super.hurtServer(world, source, amount);
   }

   public boolean hasActiveInteraction() {
      return this.coreState.activePlayerId() != null;
   }

   public void tick() {
      super.tick();
      if (this.level() instanceof ServerLevel serverlevel) {
         if (this.coreState.isForPreloading() && this.tickCount > 20) {
            this.discard();
            this.onPreloadFinished(serverlevel);
            return;
         }

         this.tickRegeneration(serverlevel);
      }
   }

   /**
    * Matches the original: heals every 80 ticks and only then shows heart particles. A downed girl
    * recovers faster while nothing hostile is nearby, and stands back up at full health. Lives here
    * rather than on SceneGirlEntity so no subclass can be left downed and invulnerable forever.
    */
   private void tickRegeneration(ServerLevel world) {
      // Losing her master while downed would otherwise strand her: invulnerable with no way back up.
      if (this.isDowned() && !this.coreState.hasMaster()) {
         this.reviveFromDowned();
      }

      if (this.tickCount % 80 != 0 || this.getHealth() == this.getMaxHealth()) {
         return;
      }

      float healed = 1.0F;
      if (this.isDowned()) {
         boolean threatened = !world.getEntitiesOfClass(Monster.class, this.getBoundingBox().inflate(7.0, 1.0, 7.0)).isEmpty();
         healed = threatened ? 1.0F : 4.0F;
      }

      this.heal(healed);
      world.sendParticles(
         ParticleTypes.HEART, this.getX(), this.getY() + 1.0 + this.random.nextDouble(), this.getZ(), 1, 1.0, 1.0, 1.0, this.random.nextGaussian()
      );

      if (this.isDowned() && this.getHealth() >= this.getMaxHealth()) {
         this.reviveFromDowned();
      }
   }

   protected void onPreloadFinished(ServerLevel serverWorld) {
   }

   public boolean canSpawnForPortScaffold(ServerLevel world, EntitySpawnReason reason) {
      return true;
   }
}
