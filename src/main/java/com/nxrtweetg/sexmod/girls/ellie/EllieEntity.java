package com.nxrtweetg.sexmod.girls.ellie;

import software.bernie.geckolib.animation.RawAnimation;
import com.nxrtweetg.sexmod.girls.scene.GirlInteractionService;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationSet;
import com.nxrtweetg.sexmod.girls.anim.GirlSoundSet;
import com.nxrtweetg.sexmod.girls.scene.GirlCustomPhase;
import com.nxrtweetg.sexmod.girls.scene.GirlEdgeApproach;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.nxrtweetg.sexmod.util.TagValueInput;
import com.nxrtweetg.sexmod.util.TagValueOutput;

public final class EllieEntity extends SceneGirlEntity {
   private static final EntityDataAccessor<Boolean> TRACKED_HORNY = SynchedEntityData.defineId(
      EllieEntity.class, EntityDataSerializers.BOOLEAN
   );

   /** The animation names in ellie.animation.json; everything else is borrowed from Jenny. */
   private static final Set<String> OWN_ANIMATIONS = Set.of(
      "attack0", "attack1", "attack2", "backwards_walk", "bowcharge", "carry_cum", "carry_fast", "carry_intro",
      "carry_slow1", "carry_slow2", "carry_slow3", "carry_slow4", "cowgirlcum", "cowgirlfast", "cowgirlslow2",
      "cowgirlstart", "crouchidle", "crouchwalk", "dash", "downed", "eyes", "fastwalk", "fly", "fly2", "hug",
      "hugidle", "hugselected", "idle", "missionary_cum", "missionary_fast", "missionary_slow",
      "missionary_start", "new", "null", "ride", "run", "sit", "sitdown", "sitdownidle", "strip", "throwpearl",
      "walk"
   );
   /** How many takes she ships per voice family; a scene wanting more falls back to Jenny. */
   private static final Map<String, Integer> OWN_SOUNDS = Map.ofEntries(
      Map.entry("aftersessionmoan", 5), Map.entry("ahh", 10), Map.entry("bjmoan", 13),
      Map.entry("cometomommy", 2), Map.entry("giggle", 5), Map.entry("goodboy", 2),
      Map.entry("happyoh", 3), Map.entry("heavybreathing", 9), Map.entry("hmph", 4),
      Map.entry("huh", 2), Map.entry("lightbreathing", 8), Map.entry("lipsound", 10),
      Map.entry("mmm", 9), Map.entry("moan", 9), Map.entry("mommyhorny", 2),
      Map.entry("sadoh", 2), Map.entry("sigh", 2)
   );
   private static final GirlAnimationSet ANIMATIONS = new GirlAnimationSet("ellie", OWN_ANIMATIONS);
   private static final GirlSoundSet SOUNDS = new GirlSoundSet("ellie", OWN_SOUNDS);

   public EllieEntity(EntityType<? extends EllieEntity> entityType, Level world) {
      super(entityType, world, "Ellie");
   }

   @Override
   public String characterId() {
      return "ellie";
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
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(TRACKED_HORNY, false);
   }

   public boolean isHorny() {
      return this.getEntityData().get(TRACKED_HORNY);
   }

   public void setHorny(boolean horny) {
      this.getEntityData().set(TRACKED_HORNY, horny);
   }

   @Override
   public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
      TagValueOutput output = TagValueOutput.wrap(tag, this.registryAccess());
      super.addAdditionalSaveData(tag);
      output.putBoolean("Horny", this.isHorny());
   }

   @Override
   public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
      TagValueInput input = TagValueInput.wrap(tag, this.registryAccess());
      super.readAdditionalSaveData(tag);
      this.setHorny(input.getBooleanOr("Horny", false));
   }

   @Override
   public boolean hasPaymentPhase(GirlSceneAction action) {
      return false;
   }

   @Override
   public int paymentAmountFor(GirlSceneAction action) {
      return 0;
   }

   /** She ships both takes of the sit-on-the-edge wait (original fp.SITDOWN / fp.SITDOWNIDLE). */
   @Override
   protected GirlEdgeApproach edgeApproach() {
      return GirlEdgeApproach.SIT_DOWN;
   }

   @Override
   public void applyHornyPotion() {
      if (this.hasActiveScene() || !this.customActionId().isEmpty()) {
         return;
      }
      this.setHorny(true);
      if (this.level() instanceof ServerLevel serverLevel) {
         Player nearest = serverLevel.getNearestPlayer(this, 10.0);
         if (nearest instanceof ServerPlayer player && !player.isSpectator()) {
            this.beginCustomInteraction(player, "dash");
         }
      }
   }

   @Override
   protected boolean customApproachesPlayer(String actionId) {
      return false;
   }

   @Override
   protected boolean customShowsPartner(String actionId) {
      return "hug".equals(actionId);
   }

   @Override
   protected double customPartnerDistance(String actionId) {
      return "hug".equals(actionId) ? 0.5 : 0.0;
   }

   @Override
   protected int customDurationTicks(String actionId, GirlCustomPhase phase) {
      if (phase != GirlCustomPhase.ACTIVE) {
         return 0;
      }
      return switch (actionId) {
         case "dash" -> 16;
         case "hug" -> 150;
         default -> 0;
      };
   }

   @Override
   protected RawAnimation selectCustomActionAnimation() {
      String actionId = this.customActionId();
      GirlCustomPhase phase = this.customPhase();
      if ("dash".equals(actionId)) {
         return this.selectNamedAnimation("dash", true);
      }
      if ("hug".equals(actionId)) {
         return switch (phase) {
            case ACTIVE -> this.selectNamedAnimation("hug", true);
            case WAITING -> this.selectNamedAnimation("hugidle", true);
            default -> null;
         };
      }
      return null;
   }

   @Override
   protected RawAnimation selectCustomMovementAnimation(boolean moving) {
      if (this.customActionId().isEmpty()) {
         return null;
      }
      return this.selectNamedAnimation("null", true);
   }

   @Override
   protected boolean isCustomStructuralKeyframe(String keyframe) {
      return switch (keyframe) {
         case "dashDone", "hugDone" -> true;
         default -> false;
      };
   }

   @Override
   protected boolean applyCustomSceneKeyframe(ServerPlayer player, String keyframe) {
      switch (keyframe) {
         case "dashDone":
            if ("dash".equals(this.customActionId())) {
               this.switchCustomInteraction(player, "hug", GirlCustomPhase.ACTIVE);
               return true;
            }
            return false;
         case "hugDone":
            if ("hug".equals(this.customActionId())) {
               this.advanceCustomPhase(player, GirlCustomPhase.WAITING);
               return true;
            }
            return false;
         default:
            return false;
      }
   }

   @Override
   protected void onCustomWatchdogExpired(ServerPlayer player) {
      String actionId = this.customActionId();
      if ("dash".equals(actionId)) {
         this.switchCustomInteraction(player, "hug", GirlCustomPhase.ACTIVE);
      } else if ("hug".equals(actionId)) {
         this.advanceCustomPhase(player, GirlCustomPhase.WAITING);
      } else {
         super.onCustomWatchdogExpired(player);
      }
   }

   @Override
   public boolean interceptSceneChoice(ServerPlayer player, GirlSceneAction action) {
      if (!"hug".equals(this.customActionId()) || this.customPhase() != GirlCustomPhase.WAITING) {
         return false;
      }
      if (this.coreState().activePlayerId() == null || !this.coreState().activePlayerId().equals(player.getUUID())) {
         return false;
      }
      if (!this.effectiveSceneActions().contains(action)) {
         return false;
      }
      this.stopCustomInteraction();
      GirlInteractionService.tryBeginInteraction(player, this, action);
      return true;
   }

   @Override
   protected boolean handleCustomSoundKeyframe(String keyframe) {
      switch (keyframe) {
         case "hugMSG2":
            this.sayClientLine("Hmm...");
            this.playSoundIndex(this.sounds().voice("hmph", 4), 3, 6.0F);
            return true;
         case "hugMSG3":
            this.sayClientLine("Hey!");
            this.playSoundIndex(this.sounds().voice("huh", 2), 1, 1.0F);
            return true;
         case "hugMSG4":
            this.sayDialogue("mommyhorny");
            this.playSoundVariant(this.sounds().voice("mommyhorny", 2), 0.5F);
            return true;
         case "hugMSG5":
            this.sayDialogue("whattodo");
            this.playSoundIndex(this.sounds().voice("huh", 2), 1, 6.0F);
            return true;
         case "hugselectedMSG1":
            this.sayDialogue("iknow");
            this.playSoundIndex(this.sounds().voice("hmph", 4), 3, 6.0F);
            return true;
         case "hugselectedMSG2":
            this.sayDialogue("followmedarling");
            this.playSoundIndex(this.sounds().voice("giggle", 5), 3, 6.0F);
            return true;
         case "sitdownMSG1":
            this.sayDialogue("cometomommy");
            this.playSoundVariant(this.sounds().voice("cometomommy", 2), 0.5F);
            return true;
         default:
            return false;
      }
   }

   @Override
   protected Set<GirlSceneAction> sceneActions() {
      if (this.isHorny()) {
         return EnumSet.of(GirlSceneAction.COWGIRL, GirlSceneAction.MISSIONARY);
      }
      return this.coreState().currentModel() != 0
         ? EnumSet.of(GirlSceneAction.FACEFUCK, GirlSceneAction.STRIP)
         : EnumSet.of(GirlSceneAction.STRIP);
   }
}
