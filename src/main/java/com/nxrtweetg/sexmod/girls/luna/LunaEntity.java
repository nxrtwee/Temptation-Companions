package com.nxrtweetg.sexmod.girls.luna;

import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationSet;
import com.nxrtweetg.sexmod.girls.anim.GirlSoundSet;
import com.nxrtweetg.sexmod.girls.scene.GirlInteractionService;
import com.nxrtweetg.sexmod.girls.scene.GirlPayment;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.girls.state.GirlActivity;
import com.geckolib.animation.RawAnimation;
import java.util.EnumSet;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public final class LunaEntity extends SceneGirlEntity {
   private static final int HEAD_PAT_APPROACH_TICKS = 40;
   // animation.cat.head_pat is 8.12 s × 20 ticks/s = 162 ticks
   private static final int HEAD_PAT_DURATION_TICKS = 162;
   /** The animation names in cat.animation.json; everything else is borrowed from Jenny. */
   private static final Set<String> OWN_ANIMATIONS = Set.of(
      "attack0", "attack1", "attack2", "backwards_walk", "blink", "bowcharge", "downed", "eat_fishing",
      "fastwalk", "fly", "fly2", "head_pat", "idle", "idle_fishing", "idle2", "null", "payment", "run",
      "sit", "sitting_cum", "sitting_fast", "sitting_intro", "sitting_slow", "start_fishing", "throw_away",
      "throwpearl", "touch_boobs_cum", "touch_boobs_fast", "touch_boobs_intro", "touch_boobs_slow",
      "touch_boobs_slow1", "vid", "wait", "walk"
   );
   /** Luna ships a large custom voice pack, so scene lines stay on her own folder. */
   private static final Map<String, Integer> OWN_SOUNDS = Map.ofEntries(
      Map.entry("ahh", 18), Map.entry("cutenya", 12), Map.entry("giggle", 15),
      Map.entry("happyoh", 8), Map.entry("hmph", 6), Map.entry("horninya", 10),
      Map.entry("huh", 5), Map.entry("lightbreathing", 25), Map.entry("mmm", 8),
      Map.entry("moan", 10), Map.entry("ouu", 13), Map.entry("owo", 8),
      Map.entry("sadoh", 7), Map.entry("sigh", 8), Map.entry("singing", 8)
   );
   /** Where she lacks a family, she speaks it with one of her own. */
   private static final Map<String, String> SOUND_SUBSTITUTES = Map.of(
      "heavybreathing", "lightbreathing",
      "lipsound", "mmm",
      "bjmoan", "horninya",
      "aftersessionmoan", "moan"
   );
   private static final GirlAnimationSet ANIMATIONS = new GirlAnimationSet("cat", OWN_ANIMATIONS);
   private static final GirlSoundSet SOUNDS = new GirlSoundSet("luna", OWN_SOUNDS, SOUND_SUBSTITUTES);
   private static final EntityDataAccessor<String> TRACKED_CUSTOM_ACTION = SynchedEntityData.defineId(LunaEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<String> TRACKED_CUSTOM_PHASE = SynchedEntityData.defineId(LunaEntity.class, EntityDataSerializers.STRING);
   private int customTicks;
   private boolean clientIdle2;

   public LunaEntity(EntityType<? extends LunaEntity> entityType, Level world) {
      super(entityType, world, "Luna");
   }

   @Override
   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(TRACKED_CUSTOM_ACTION, LunaCustomAction.NONE.id());
      builder.define(TRACKED_CUSTOM_PHASE, LunaCustomPhase.IDLE.id());
   }

   @Override
   public String characterId() {
      return "luna";
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
      return EnumSet.of(GirlSceneAction.TOUCH_BOOBS, GirlSceneAction.SITTING);
   }

   /**
    * Original: sex = 3x cod, touch_boobs = 2x salmon (Items.FISH damage 0 / damage 1 in 1.12).
    * In 26.2 cod and salmon are separate items.
    */
   @Override
   protected net.minecraft.world.item.ItemStack priceFor(GirlSceneAction action) {
      return switch (action) {
         case TOUCH_BOOBS -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SALMON, 2);
         case SITTING -> new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COD, 3);
         default -> super.priceFor(action);
      };
   }

   @Override
   public com.nxrtweetg.sexmod.girls.state.GirlPaymentItem paymentItemFor(GirlSceneAction action) {
      return switch (action) {
         case TOUCH_BOOBS -> com.nxrtweetg.sexmod.girls.state.GirlPaymentItem.SALMON;
         case SITTING -> com.nxrtweetg.sexmod.girls.state.GirlPaymentItem.COD;
         default -> super.paymentItemFor(action);
      };
   }

   @Override
   public int paymentAmountFor(GirlSceneAction action) {
      return switch (action) {
         case TOUCH_BOOBS -> 2;
         case SITTING -> 3;
         default -> super.paymentAmountFor(action);
      };
   }

   @Override
   protected List<InteractionSection> extraInteractionSections() {
      return List.of(this.section("action.section.affection", ItemStack.EMPTY, List.of(this.option("headpat", "action.names.headpat", ItemStack.EMPTY))));
   }

   @Override
   protected RawAnimation selectCustomMovementAnimation(boolean moving) {
      LunaCustomAction action = this.customAction();
      if (action != LunaCustomAction.NONE) {
         return this.customPhase() == LunaCustomPhase.APPROACH ? this.selectNamedAnimation("walk", true) : this.selectNamedAnimation("null", true);
      }

      if (this.level().isClientSide() && !moving && this.resolvedActivity() == GirlActivity.NONE && this.clientIdle2) {
         return this.selectNamedAnimation("idle2", true);
      }

      return null;
   }

   @Override
   protected RawAnimation selectCustomActionAnimation() {
      return this.customAction() == LunaCustomAction.HEAD_PAT && this.customPhase() == LunaCustomPhase.ACTIVE
         ? this.selectNamedAnimation("head_pat", true)
         : null;
   }

   @Override
   protected boolean handleCustomSoundKeyframe(String keyframe) {
      switch (keyframe) {
         case "idleDone":
            this.clientIdle2 = this.random.nextInt(10) == 0;
            return true;
         case "idle2Done":
            this.clientIdle2 = false;
            return true;
         case "headpatMSG1":
            this.sayClientLine("huh?~");
            this.playSoundIndex(this.sounds().voice("huh", 5), 1, 1.0F);
            return true;
         case "headpatMSG2":
            this.playSoundVariant(this.sounds().voice("mmm", 8), 1.0F);
            return true;
         case "headpatMSG3":
            this.sayClientLine("nya~");
            this.playSoundIndex(this.sounds().voice("horninya", 10), 0, 1.0F);
            return true;
         default:
            return false;
      }
   }

   @Override
   protected boolean hasCustomSceneLock() {
      return this.customAction() != LunaCustomAction.NONE;
   }

   @Override
   protected void tickCustomServer() {
      LunaCustomAction action = this.customAction();
      if (action == LunaCustomAction.NONE) {
         return;
      }

      ServerPlayer player = this.level() instanceof ServerLevel serverLevel && this.coreState().activePlayerId() != null
         ? serverLevel.getServer().getPlayerList().getPlayer(this.coreState().activePlayerId())
         : null;
      if (player == null || !player.isAlive() || player.distanceToSqr(this) > 64.0) {
         this.stopCustomInteraction();
         return;
      }

      LunaCustomPhase phase = this.customPhase();
      if (action == LunaCustomAction.HEAD_PAT && phase == LunaCustomPhase.APPROACH) {
         this.tickHeadPatApproach(player);
         return;
      }

      // Partner visible in ACTIVE but not while approaching
      this.setRenderPlayer(action == LunaCustomAction.HEAD_PAT && phase == LunaCustomPhase.ACTIVE);

      this.customTicks++;
      if (action == LunaCustomAction.HEAD_PAT && phase == LunaCustomPhase.ACTIVE && this.customTicks >= HEAD_PAT_DURATION_TICKS) {
         this.stopCustomInteraction();
      }
   }

   @Override
   public boolean tryBeginCustomInteraction(ServerPlayer player, String actionId) {
      if (!"headpat".equals(actionId)) {
         return false;
      }

      Optional<InteractionOption> option = this.interactionOption(actionId);
      if (option.isPresent() && !GirlPayment.canAfford(player, option.get().price())) {
         return false;
      }

      if (!GirlInteractionService.canInteract(player, this)) {
         return true;
      }

      this.beginHeadPat(player);
      player.sendSystemMessage(Component.literal("Started " + this.getName().getString() + " action: headpat"));
      return true;
   }

   @Override
   public void resetScene() {
      super.resetScene();
      this.stopCustomInteraction();
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput output) {
      super.addAdditionalSaveData(output);
      output.putString("LunaCustomAction", this.customAction().id());
      output.putString("LunaCustomPhase", this.customPhase().id());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput input) {
      super.readAdditionalSaveData(input);
      this.setCustomAction(LunaCustomAction.fromId(input.getStringOr("LunaCustomAction", LunaCustomAction.NONE.id())));
      this.setCustomPhase(LunaCustomPhase.fromId(input.getStringOr("LunaCustomPhase", LunaCustomPhase.IDLE.id())));
      this.customTicks = 0;
   }

   private void beginHeadPat(ServerPlayer player) {
      this.stopCustomInteraction();
      this.setCustomAction(LunaCustomAction.HEAD_PAT);
      this.setCustomPhase(LunaCustomPhase.APPROACH);
      this.customTicks = 0;
      this.coreState().setActivePlayerId(player.getUUID());
      this.getNavigation().stop();
      this.setNoGravity(false);
      this.syncTrackedState();
   }

   private void stopCustomInteraction() {
      this.setCustomAction(LunaCustomAction.NONE);
      this.setCustomPhase(LunaCustomPhase.IDLE);
      this.customTicks = 0;
      this.setRenderPlayer(false);
      this.coreState().clearInteraction();
      this.setNoGravity(false);
      this.syncTrackedState();
   }

   private void tickHeadPatApproach(ServerPlayer player) {
      this.customTicks++;
      float playerYaw = player.getYRot();
      Vec3 meetingPoint = player.position().add(horizontalForward(playerYaw));
      double dx = this.getX() - meetingPoint.x;
      double dz = this.getZ() - meetingPoint.z;
      if (dx * dx + dz * dz <= 0.01 || this.customTicks > HEAD_PAT_APPROACH_TICKS) {
         this.getNavigation().stop();
         this.setNoGravity(false);
         float facing = Mth.wrapDegrees(playerYaw + 180.0F);
         this.setYRot(facing);
         this.setYBodyRot(facing);
         this.setYHeadRot(facing);
         this.coreState().setTargetYaw(facing);
         this.setCustomPhase(LunaCustomPhase.ACTIVE);
         this.customTicks = 0;
      } else {
         int remaining = HEAD_PAT_APPROACH_TICKS - this.customTicks;
         Vec3 step = remaining <= 0 ? meetingPoint : this.position().add(meetingPoint.subtract(this.position()).scale(1.0 / remaining));
         float travelYaw = (float)(Mth.atan2(meetingPoint.z - this.getZ(), meetingPoint.x - this.getX()) * 180.0 / Math.PI) - 90.0F;
         this.setYRot(travelYaw);
         this.setYBodyRot(travelYaw);
         this.setYHeadRot(travelYaw);
         this.setPos(step.x, step.y, step.z);
         this.setDeltaMovement(Vec3.ZERO);
      }
   }

   private static Vec3 horizontalForward(float yaw) {
      float radians = (yaw + 90.0F) * (float)(Math.PI / 180.0);
      return new Vec3(Math.cos(radians), 0.0, Math.sin(radians));
   }

   private LunaCustomAction customAction() {
      return LunaCustomAction.fromId(this.getEntityData().get(TRACKED_CUSTOM_ACTION));
   }

   private void setCustomAction(LunaCustomAction action) {
      this.getEntityData().set(TRACKED_CUSTOM_ACTION, action.id());
   }

   private LunaCustomPhase customPhase() {
      return LunaCustomPhase.fromId(this.getEntityData().get(TRACKED_CUSTOM_PHASE));
   }

   private void setCustomPhase(LunaCustomPhase phase) {
      this.getEntityData().set(TRACKED_CUSTOM_PHASE, phase.id());
   }

   private enum LunaCustomAction {
      NONE(""),
      HEAD_PAT("head_pat");

      private final String id;

      LunaCustomAction(String id) {
         this.id = id;
      }

      public String id() {
         return this.id;
      }

      public static LunaCustomAction fromId(String id) {
         return "head_pat".equals(id) ? HEAD_PAT : NONE;
      }
   }

   private enum LunaCustomPhase {
      IDLE("idle"),
      APPROACH("approach"),
      ACTIVE("active");

      private final String id;

      LunaCustomPhase(String id) {
         this.id = id;
      }

      public String id() {
         return this.id;
      }

      public static LunaCustomPhase fromId(String id) {
         return switch (id) {
            case "approach" -> APPROACH;
            case "active" -> ACTIVE;
            default -> IDLE;
         };
      }
   }
}
