package com.nxrtweetg.sexmod.girls.galath;

import software.bernie.geckolib.animation.RawAnimation;
import com.nxrtweetg.sexmod.girls.ModDataSerializers;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationRole;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationSet;
import com.nxrtweetg.sexmod.girls.anim.GirlSoundSet;
import com.nxrtweetg.sexmod.girls.manglelie.ManglelieEntity;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.girls.scene.GirlScenePhase;
import com.nxrtweetg.sexmod.item.ModItems;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class GalathEntity extends SceneGirlEntity {
   private static final Set<String> OWN_ANIMATIONS = Set.of(
      "attack", "attack0", "attack1", "attack2", "backwards_walk", "blink", "boost", "bowcharge",
      "controlled_flight", "corrupt_cum", "corrupt_hard", "corrupt_intro", "corrupt_slow", "corrupt_soft",
      "crouchidle", "crouchwalk", "desummon", "desummon_standing", "double_holding_hard1", "give_coin",
      "hug_mang", "idle", "idle_flying", "knocked_out", "knocked_out_stand_up", "knockout_air", "masterbate",
      "masterbating_sitting", "masterbating_sitting_cum", "null", "pussy_licking", "pussy_licking_back",
      "pussy_licking_forward", "rape0", "rape1", "rape2", "rape_charge", "rape_cum", "rape_cum_idle",
      "rape_intro", "rape_prepare", "run", "running", "sit", "strip", "summon", "summon_skeleton",
      "summon_skeletonMirrored", "walk"
   );
   private static final Map<String, Integer> OWN_SOUNDS = Map.ofEntries(
      Map.entry("aaa", 2), Map.entry("ahh", 8), Map.entry("breathing", 7),
      Map.entry("dialog", 6), Map.entry("giggle", 4), Map.entry("hmph", 3),
      Map.entry("huh", 3), Map.entry("lightcharge", 5), Map.entry("moan", 8),
      Map.entry("orgasm", 5), Map.entry("strongcharge", 4), Map.entry("uuh", 7)
   );
   private static final Map<String, String> SOUND_SUBSTITUTES = Map.of(
      "lightbreathing", "breathing",
      "heavybreathing", "breathing",
      "aftersessionmoan", "moan",
      "bjmoan", "moan"
   );
   private static final Map<GirlAnimationRole, String> ANIMATION_ALIASES = Map.ofEntries(
      Map.entry(GirlAnimationRole.COWGIRL_START, "galath.rape_intro"),
      Map.entry(GirlAnimationRole.COWGIRL_SLOW, "galath.rape0"),
      Map.entry(GirlAnimationRole.COWGIRL_FAST, "galath.rape1"),
      Map.entry(GirlAnimationRole.COWGIRL_CUM, "galath.rape_cum"),
      Map.entry(GirlAnimationRole.ANAL_START, "galath.corrupt_intro"),
      Map.entry(GirlAnimationRole.ANAL_SLOW, "galath.corrupt_slow"),
      Map.entry(GirlAnimationRole.ANAL_FAST, "galath.corrupt_soft"),
      Map.entry(GirlAnimationRole.ANAL_CUM, "galath.corrupt_cum"),
      Map.entry(GirlAnimationRole.THREESOME_SLOW, "galath.pussy_licking"),
      Map.entry(GirlAnimationRole.THREESOME_FAST, "galath.masterbating_sitting"),
      Map.entry(GirlAnimationRole.THREESOME_CUM, "galath.masterbating_sitting_cum")
   );
   private static final GirlAnimationSet ANIMATIONS = new GirlAnimationSet("galath", OWN_ANIMATIONS, ANIMATION_ALIASES);
   private static final GirlSoundSet SOUNDS = new GirlSoundSet("galath", OWN_SOUNDS, SOUND_SUBSTITUTES);

   private static final EntityDataAccessor<Optional<UUID>> TRACKED_LINKED_MANGLELIE = SynchedEntityData.defineId(
      GalathEntity.class, ModDataSerializers.OPTIONAL_UUID
   );
   private static final EntityDataAccessor<Boolean> TRACKED_KNOCKED_OUT = SynchedEntityData.defineId(
      GalathEntity.class, EntityDataSerializers.BOOLEAN
   );
   private static final EntityDataAccessor<Boolean> TRACKED_HUGGING_MANG = SynchedEntityData.defineId(
      GalathEntity.class, EntityDataSerializers.BOOLEAN
   );

   private int hugTicks;
   private int giveCoinTicks;

   public GalathEntity(EntityType<? extends GalathEntity> entityType, Level world) {
      super(entityType, world, "Galath");
   }

   @Override
   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(TRACKED_LINKED_MANGLELIE, Optional.empty());
      builder.define(TRACKED_KNOCKED_OUT, false);
      builder.define(TRACKED_HUGGING_MANG, false);
   }

   @Override
   public String characterId() {
      return "galath";
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

   public double getMangRideHeightOffset() {
      return 2.0875;
   }

   public boolean isKnockedOut() {
      return false;
   }

   public void setKnockedOut(boolean knockedOut) {
   }

   public boolean isHuggingMang() {
      return false;
   }

   public ManglelieEntity getLinkedManglelie() {
      return null;
   }

   public void linkManglelie(ManglelieEntity mang) {
   }

   public void unlinkManglelie() {
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      return InteractionResult.PASS;
   }

   @Override
   public void tick() {
      super.tick();
   }

   @Override
   protected RawAnimation selectCustomMovementAnimation(boolean moving) {
      return super.selectCustomMovementAnimation(moving);
   }
}
