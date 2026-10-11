package com.nxrtweetg.sexmod.girls.luna;

import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationSet;
import com.nxrtweetg.sexmod.girls.anim.GirlSoundSet;
import com.nxrtweetg.sexmod.girls.scene.GirlCustomPhase;
import com.nxrtweetg.sexmod.girls.scene.GirlEdgeApproach;
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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class LunaEntity extends SceneGirlEntity {
   /** Her one custom interaction; the shared machine in {@link SceneGirlEntity} runs it. */
   private static final String HEAD_PAT = "headpat";
   // animation.cat.head_pat is 8.12 s x 20 ticks/s = 162 ticks, and its resetGirl keyframe sits at 8.04 s.
   // The length is only the watchdog: the keyframe is what normally ends the take.
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
   private boolean clientIdle2;

   public LunaEntity(EntityType<? extends LunaEntity> entityType, Level world) {
      super(entityType, world, "Luna");
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
    * She ships no sit-down take, only the standing hold (original fp.WAIT_CAT), so she walks to the
    * end of the bed and waits there on her feet until he closes the distance.
    */
   @Override
   protected GirlEdgeApproach edgeApproach() {
      return GirlEdgeApproach.HOLD_ONLY;
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

   /** {@code eb.b(EntityPlayer)}: a headpat is the one row of hers that is not a scene. */
   @Override
   protected List<InteractionSection> extraInteractionSections() {
      return List.of(this.row(this.option("headpat", "action.names.headpat", ItemStack.EMPTY)));
   }

   @Override
   protected RawAnimation selectCustomMovementAnimation(boolean moving) {
      if (!this.customActionId().isEmpty()) {
         return this.customPhase() == GirlCustomPhase.APPROACH ? this.selectNamedAnimation("walk", true) : this.selectNamedAnimation("null", true);
      }

      if (this.level().isClientSide() && !moving && this.resolvedActivity() == GirlActivity.NONE && this.clientIdle2) {
         return this.selectNamedAnimation("idle2", true);
      }

      return null;
   }

   @Override
   protected RawAnimation selectCustomActionAnimation() {
      return HEAD_PAT.equals(this.customActionId()) && this.customPhase() == GirlCustomPhase.ACTIVE
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
   protected boolean customShowsPartner(String actionId) {
      return HEAD_PAT.equals(actionId);
   }

   /**
    * The pat is an in-place bind, not a walk-up: {@code ed}'s dispatch is the two lines
    * {@code b(fp.HEAD_PAT); b(uUID);}, and the second reaches {@code ei.b(UUID)}, which places the
    * player itself instead of sending her the approach packet the talk-style interactions use.
    */
   @Override
   protected boolean customApproachesPlayer(String actionId) {
      return false;
   }

   /**
    * {@code ei.b(UUID)} puts him a block out along her axis and {@code j(UUID)} pulls him 0.35 back,
    * which is the gap {@code animation.cat.head_pat} is posed around.
    */
   @Override
   protected double customPartnerDistance(String actionId) {
      return HEAD_PAT.equals(actionId) ? 0.65 : 0.0;
   }

   @Override
   protected int customDurationTicks(String actionId, GirlCustomPhase phase) {
      return HEAD_PAT.equals(actionId) && phase == GirlCustomPhase.ACTIVE ? HEAD_PAT_DURATION_TICKS : 0;
   }

   /** {@code resetGirl} at 8.04 s in animation.cat.head_pat is what ends the pat in the original. */
   @Override
   protected boolean isCustomStructuralKeyframe(String keyframe) {
      return "resetGirl".equals(keyframe);
   }

   @Override
   protected boolean applyCustomSceneKeyframe(ServerPlayer player, String keyframe) {
      if ("resetGirl".equals(keyframe)) {
         this.stopCustomInteraction();
         return true;
      }

      return false;
   }

   @Override
   public boolean tryBeginCustomInteraction(ServerPlayer player, String actionId) {
      if (!HEAD_PAT.equals(actionId)) {
         return false;
      }

      Optional<InteractionOption> option = this.interactionOption(actionId);
      if (option.isPresent() && !GirlPayment.canAfford(player, option.get().price())) {
         return false;
      }

      if (!GirlInteractionService.canInteract(player, this)) {
         return true;
      }

      this.beginCustomInteraction(player, HEAD_PAT);
      player.sendSystemMessage(Component.literal("Started " + this.getName().getString() + " action: " + HEAD_PAT));
      return true;
   }
}
