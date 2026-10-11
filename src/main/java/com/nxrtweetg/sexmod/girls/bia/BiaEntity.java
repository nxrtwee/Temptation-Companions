package com.nxrtweetg.sexmod.girls.bia;

import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationSet;
import com.nxrtweetg.sexmod.girls.anim.GirlSoundSet;
import com.nxrtweetg.sexmod.girls.scene.GirlCustomPhase;
import com.nxrtweetg.sexmod.girls.scene.GirlEdgeApproach;
import com.nxrtweetg.sexmod.girls.scene.GirlInteractionService;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.geckolib.animation.RawAnimation;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class BiaEntity extends SceneGirlEntity {
   /** Her custom interactions. {@code TALK_RESPONSE} is the second half of {@code TALK}. */
   private static final String HEAD_PAT = "headpat";
   private static final String TALK = "talk";
   private static final String TALK_RESPONSE = "talk_response";
   /** Watchdogs only - each take ends on its own {@code *Done} keyframe. Lengths from bia.animation.json. */
   private static final int HEAD_PAT_DURATION_TICKS = 154;
   private static final int TALK_DURATION_TICKS = 47;
   private static final int TALK_RESPONSE_DURATION_TICKS = 68;
   /**
    * What the talk offers, the original's two first ({@code e0.b} reopened the GUI with
    * {@code {anal, doggy}}), then the two scenes only she ships. Her roster's last entry, {@code STRIP},
    * is reachable from the "all scenes" row the talk ends on.
    */
   private static final List<GirlSceneAction> TALK_SCENES = List.of(
      GirlSceneAction.ANAL, GirlSceneAction.PRONE_DOGGY, GirlSceneAction.ANAL_ALT, GirlSceneAction.SITTING_ALT
   );
   /** The animation names in bia.animation.json; everything else is borrowed from Jenny. */
   private static final Set<String> OWN_ANIMATIONS = Set.of(
      "anal_cum", "anal_fast", "anal_prepare", "anal_slow", "anal_start", "anal_wait", "attack0", "attack1",
      "attack2", "backwards_walk", "bowcharge", "downed", "fastwalk", "fhappy", "fly", "fly2", "headpat", "idle",
      "null", "ride", "run", "sit", "sitdown",
      "sitdownidle", "strip", "talk_horny", "talk_horny2", "talk_idle", "talk_idle2", "talk_response",
      "throwpearl", "walk", "wave", "wave_idle",
      // Only she ships these: Jenny has no copy to fall back to, so they must be listed here.
      "sitting_alt_intro", "sitting_alt_insert", "sitting_alt_soft", "sitting_alt_hard1",
      "sitting_alt_hard2", "sitting_alt_hard3", "sitting_alt_cum",
      "anal_alt_prepare", "anal_alt_wait", "anal_alt_start", "anal_alt_slow", "anal_alt_fast", "anal_alt_cum"
   );
   /** How many takes she ships per voice family; a scene wanting more falls back to Jenny. */
   private static final Map<String, Integer> OWN_SOUNDS = Map.of(
      "ahh", 8, "bjmoan", 5, "breath", 4, "giggle", 3, "hey", 4, "huh", 3, "mmm", 8
   );
   /** Where she lacks a family, she speaks it with one of her own. */
   private static final Map<String, String> SOUND_SUBSTITUTES = Map.of(
      "moan", "ahh",
      "lipsound", "mmm",
      "heavybreathing", "breath",
      "lightbreathing", "breath",
      "aftersessionmoan", "breath",
      "hmph", "huh",
      "sadoh", "huh",
      "sigh", "breath"
   );
   private static final GirlAnimationSet ANIMATIONS = new GirlAnimationSet("bia", OWN_ANIMATIONS);
   private static final GirlSoundSet SOUNDS = new GirlSoundSet("bia", OWN_SOUNDS, SOUND_SUBSTITUTES);
   /** The scene she was asked for at the end of the talk, played out once she has answered. */
   private GirlSceneAction pendingTalkAction;

   public BiaEntity(EntityType<? extends BiaEntity> entityType, Level world) {
      super(entityType, world, "Bia");
   }

   @Override
   public String characterId() {
      return "bia";
   }

   @Override
   protected GirlAnimationSet animations() {
      return ANIMATIONS;
   }

   @Override
   protected GirlSoundSet sounds() {
      return SOUNDS;
   }

   /** She ships both takes of the sit-on-the-edge wait (original fp.SITDOWN / fp.SITDOWNIDLE). */
   @Override
   protected GirlEdgeApproach edgeApproach() {
      return GirlEdgeApproach.SIT_DOWN;
   }

   @Override
   protected boolean handleCustomSoundKeyframe(String keyframe) {
      switch (keyframe) {
         case "talk_hornyMSG1":
            this.sayDialogue("heya");
            this.playSoundVariant(this.sounds().voice("hey", 4), 1.0F);
            return true;
         case "talk_hornyMSG2":
            // MSG2/MSG3 sit in the long talk_horny take, which nothing plays; kept so a swap back works
            this.sayDialogue("horny");
            this.playSoundIndex(this.sounds().voice("giggle", 3), 2, 1.0F);
            return true;
         case "talk_hornyMSG3":
            this.sayDialogue("so");
            this.playSoundIndex(this.sounds().voice("breath", 4), 0, 1.0F);
            return true;
         case "talk_hornyMSG4":
            this.sayDialogue("fun");
            this.playSoundIndex(this.sounds().voice("huh", 3), 0, 1.0F);
            return true;
         case "talk_responseMSG1":
            this.sayDialogue("huh");
            this.playSoundIndex(this.sounds().voice("huh", 3), 2, 1.0F);
            return true;
         case "talk_responseMSG2":
            this.sayDialogue("iuhm");
            this.playSoundIndex(this.sounds().voice("breath", 4), 1, 1.0F);
            return true;
         case "talk_responseMSG3":
            this.sayDialogue("yes");
            this.playSoundIndex(this.sounds().voice("giggle", 3), 0, 1.0F);
            return true;
         case "headpatMSG1":
            this.sayDialogue("headpats");
            this.playSoundIndex(this.sounds().voice("breath", 4), 0, 1.0F);
            return true;
         case "headpatMSG2":
            this.sayDialogue("hmm");
            this.playSoundIndex(this.sounds().voice("mmm", 8), 0, 1.0F);
            return true;
         case "headpatMSG3":
            this.sayDialogue("huh2");
            this.playSoundIndex(this.sounds().voice("huh", 3), 0, 1.0F);
            return true;
         case "headpatMSG4":
            this.sayDialogue("thankyou");
            this.playSoundIndex(this.sounds().voice("giggle", 3), 1, 1.0F);
            return true;
         case "sitdownMSG1":
            this.sayClientLine("come here big boy~");
            this.playSoundVariant(this.sounds().voice("breath", 4), 1.0F);
            return true;
         default:
            return false;
      }
   }

   @Override
   protected Set<GirlSceneAction> sceneActions() {
      return EnumSet.of(
         GirlSceneAction.ANAL, GirlSceneAction.ANAL_ALT, GirlSceneAction.PRONE_DOGGY, GirlSceneAction.SITTING_ALT, GirlSceneAction.STRIP
      );
   }

   /** {@code e0.b(EntityPlayer)}: her top level is talk and a headpat, and nothing else of her own. */
   @Override
   protected List<InteractionSection> extraInteractionSections() {
      return List.of(
         this.row(this.option(TALK, "action.names.talk", ItemStack.EMPTY)), this.row(this.option(HEAD_PAT, "action.names.headpat", ItemStack.EMPTY))
      );
   }

   /**
    * While she is waiting on an answer the menu is the answer: the talk ends in a list of scenes rather
    * than a submenu, which is what the original's second GUI showed - flat, one row per scene, the way
    * {@code e0.b} reopened it with {@code {anal, doggy}}.
    *
    * <p>Her top level is only her own rows, because reaching a scene past the talk is what the talk is
    * for: the shared board's roster row sat next to the talk and offered the same scenes without her
    * ever saying a word, which is the conflict with the old menu values. The roster is still reachable,
    * as the last row of the talk.
    */
   @Override
   protected List<InteractionSection> buildInteractionSections() {
      if (TALK.equals(this.customActionId()) && this.customPhase() == GirlCustomPhase.WAITING) {
         return this.talkSections();
      }

      List<InteractionSection> rows = List.copyOf(this.extraInteractionSections());
      return rows.isEmpty() ? super.buildInteractionSections() : rows;
   }

   private List<InteractionSection> talkSections() {
      List<InteractionSection> rows = new java.util.ArrayList<>();

      for (GirlSceneAction action : this.effectiveSceneActions()) {
         if (action.enabled()) {
            rows.add(this.row(action));
         }
      }

      return rows.isEmpty() ? super.buildInteractionSections() : rows;
   }

   @Override
   protected RawAnimation selectCustomActionAnimation() {
      String actionId = this.customActionId();
      GirlCustomPhase phase = this.customPhase();
      if (HEAD_PAT.equals(actionId) && phase == GirlCustomPhase.ACTIVE) {
         return this.selectNamedAnimation("headpat", true);
      }

      if (TALK.equals(actionId)) {
         // talk_horny2 while she says her piece, talk_idle2 while she waits on an answer - the wait
         // has to keep moving, which is why it is a looping take and not a frozen pose. The longer
         // talk_horny/talk_idle takes are in the asset but the original plays neither.
         return switch (phase) {
            case ACTIVE -> this.selectNamedAnimation("talk_horny2", true);
            case WAITING -> this.selectNamedAnimation("talk_idle2", true);
            default -> null;
         };
      }

      return TALK_RESPONSE.equals(actionId) && phase == GirlCustomPhase.ACTIVE ? this.selectNamedAnimation("talk_response", true) : null;
   }

   @Override
   protected RawAnimation selectCustomMovementAnimation(boolean moving) {
      if (this.customActionId().isEmpty()) {
         return null;
      }

      return this.customPhase() == GirlCustomPhase.APPROACH ? this.selectNamedAnimation("walk", true) : this.selectNamedAnimation("null", true);
   }

   /** Only the headpat needs him drawn; she talks to the camera. */
   @Override
   protected boolean customShowsPartner(String actionId) {
      return HEAD_PAT.equals(actionId);
   }

   @Override
   protected int customDurationTicks(String actionId, GirlCustomPhase phase) {
      if (phase != GirlCustomPhase.ACTIVE) {
         // the wait has no clock at all: it ends when he picks something
         return 0;
      }

      return switch (actionId) {
         case HEAD_PAT -> HEAD_PAT_DURATION_TICKS;
         case TALK -> TALK_DURATION_TICKS;
         case TALK_RESPONSE -> TALK_RESPONSE_DURATION_TICKS;
         default -> 0;
      };
   }

   @Override
   protected boolean isCustomStructuralKeyframe(String keyframe) {
      return switch (keyframe) {
         case "headpatDone", "talk_hornyDone", "talk_responseDone" -> true;
         default -> false;
      };
   }

   @Override
   protected boolean applyCustomSceneKeyframe(ServerPlayer player, String keyframe) {
      switch (keyframe) {
         case "headpatDone":
            if (HEAD_PAT.equals(this.customActionId())) {
               this.stopCustomInteraction();
               return true;
            }

            return false;
         case "talk_hornyDone":
            if (TALK.equals(this.customActionId()) && this.customPhase() == GirlCustomPhase.ACTIVE) {
               this.advanceCustomPhase(player, GirlCustomPhase.WAITING);
               return true;
            }

            return false;
         case "talk_responseDone":
            if (TALK_RESPONSE.equals(this.customActionId())) {
               this.startPendingTalkScene(player);
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
      if (TALK.equals(actionId)) {
         this.advanceCustomPhase(player, GirlCustomPhase.WAITING);
      } else if (TALK_RESPONSE.equals(actionId)) {
         this.startPendingTalkScene(player);
      } else {
         super.onCustomWatchdogExpired(player);
      }
   }

   @Override
   public boolean tryBeginCustomInteraction(ServerPlayer player, String actionId) {
      if (!HEAD_PAT.equals(actionId) && !TALK.equals(actionId)) {
         return false;
      }

      if (!GirlInteractionService.canInteract(player, this)) {
         return true;
      }

      this.pendingTalkAction = null;
      this.beginCustomInteraction(player, actionId);
      player.sendSystemMessage(Component.literal("Started " + this.getName().getString() + " action: " + actionId));
      return true;
   }

   /** The talk's follow-up menu: she answers first, then the scene she was asked for starts. */
   @Override
   public boolean interceptSceneChoice(ServerPlayer player, GirlSceneAction action) {
      if (!TALK.equals(this.customActionId()) || this.customPhase() != GirlCustomPhase.WAITING) {
         return false;
      }

      if (this.coreState().activePlayerId() == null || !this.coreState().activePlayerId().equals(player.getUUID())) {
         return false;
      }

      // any scene of hers, not just the rows the talk lists by name: the "all scenes" row it ends on
      // reaches the rest of her roster, and those have to be answered the same way rather than fall
      // through to the ordinary path, which would turn them down while the talk still counts as busy
      if (!this.effectiveSceneActions().contains(action)) {
         return false;
      }

      this.pendingTalkAction = action;
      this.switchCustomInteraction(player, TALK_RESPONSE, GirlCustomPhase.ACTIVE);
      return true;
   }

   private void startPendingTalkScene(ServerPlayer player) {
      GirlSceneAction action = this.pendingTalkAction;
      this.pendingTalkAction = null;
      this.stopCustomInteraction();
      if (action != null) {
         GirlInteractionService.tryBeginInteraction(player, this, action);
      }
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
   protected double customPartnerDistance(String actionId) {
      return HEAD_PAT.equals(actionId) ? 0.65 : 0.0;
   }
}
