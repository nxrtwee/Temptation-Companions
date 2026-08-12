package com.nxrtweetg.sexmod.girls.scene;

import com.nxrtweetg.sexmod.girls.state.GirlFollowUpAction;
import com.nxrtweetg.sexmod.girls.state.GirlPaymentItem;
import java.util.Optional;

public enum GirlSceneAction {
   BLOWJOB("blowjob", GirlPaymentItem.EMERALD, 3, GirlFollowUpAction.BLOWJOB),
   KOBOLD_BLOWJOB("kobold_blowjob", GirlPaymentItem.EMERALD, 3, GirlFollowUpAction.KOBOLD_BLOWJOB, false),
   DEEPTHROAT("deepthroat", GirlPaymentItem.EMERALD, 3, GirlFollowUpAction.DEEPTHROAT),
   FACEFUCK("facefuck", GirlPaymentItem.EMERALD, 3, GirlFollowUpAction.FACEFUCK),
   BOOBJOB("boobjob", GirlPaymentItem.ENDER_PEARL, 2, GirlFollowUpAction.BOOBJOB),
   TOUCH_BOOBS("touch_boobs", GirlPaymentItem.ENDER_PEARL, 2, GirlFollowUpAction.TOUCH_BOOBS),
   DOGGY("doggy", GirlPaymentItem.DIAMOND, 2, GirlFollowUpAction.DOGGY),
   PRONE_DOGGY("prone_doggy", GirlPaymentItem.DIAMOND, 2, GirlFollowUpAction.PRONE_DOGGY),
   ANAL("anal", GirlPaymentItem.DIAMOND, 2, GirlFollowUpAction.ANAL),
   ANAL_ALT("anal_alt", GirlPaymentItem.DIAMOND, 2, GirlFollowUpAction.ANAL_ALT),
   KOBOLD_ANAL("kobold_anal", GirlPaymentItem.DIAMOND, 2, GirlFollowUpAction.KOBOLD_ANAL),
   SITTING("sitting", GirlPaymentItem.DIAMOND, 2, GirlFollowUpAction.SITTING),
   SITTING_ALT("sitting_alt", GirlPaymentItem.DIAMOND, 2, GirlFollowUpAction.SITTING_ALT),
   MISSIONARY("missionary", GirlPaymentItem.DIAMOND, 2, GirlFollowUpAction.MISSIONARY),
   MATING_PRESS("mating_press", GirlPaymentItem.DIAMOND, 2, GirlFollowUpAction.MATING_PRESS, false),
   REVERSE_COWGIRL("reverse_cowgirl", GirlPaymentItem.DIAMOND, 2, GirlFollowUpAction.REVERSE_COWGIRL),
   COWGIRL("cowgirl", GirlPaymentItem.DIAMOND, 2, GirlFollowUpAction.COWGIRL),
   STRIP("strip", GirlPaymentItem.GOLD, 1, GirlFollowUpAction.STRIP);

   private final String legacyId;
   private final GirlPaymentItem paymentItem;
   private final int paymentAmount;
   private final GirlFollowUpAction followUpAction;
   private final boolean enabled;

   GirlSceneAction(String legacyId, GirlPaymentItem paymentItem, int paymentAmount, GirlFollowUpAction followUpAction) {
      this(legacyId, paymentItem, paymentAmount, followUpAction, true);
   }

   GirlSceneAction(String legacyId, GirlPaymentItem paymentItem, int paymentAmount, GirlFollowUpAction followUpAction, boolean enabled) {
      this.legacyId = legacyId;
      this.paymentItem = paymentItem;
      this.paymentAmount = paymentAmount;
      this.followUpAction = followUpAction;
      this.enabled = enabled;
   }

   /**
    * Whether this scene is offered at all. Disabled scenes keep their tuning and animation wiring so
    * they can be switched back on, but never reach a menu or a running scene.
    */
   public boolean enabled() {
      return this.enabled;
   }

   public String legacyId() {
      return this.legacyId;
   }

   public GirlPaymentItem paymentItem() {
      return this.paymentItem;
   }

   public int paymentAmount() {
      return this.paymentAmount;
   }

   public GirlFollowUpAction followUpAction() {
      return this.followUpAction;
   }

   public static Optional<GirlSceneAction> fromLegacyId(String value) {
      return switch (value) {
         case "blowjob" -> Optional.of(BLOWJOB);
         case "kobold_blowjob", "koboldblowjob" -> Optional.of(KOBOLD_BLOWJOB);
         case "deepthroat", "standard2" -> Optional.of(DEEPTHROAT);
         case "facefuck", "carry" -> Optional.of(FACEFUCK);
         case "boobjob" -> Optional.of(BOOBJOB);
         case "touch_boobs", "touchboobs" -> Optional.of(TOUCH_BOOBS);
         case "doggy" -> Optional.of(DOGGY);
         case "prone_doggy", "pronedoggy" -> Optional.of(PRONE_DOGGY);
         case "anal" -> Optional.of(ANAL);
         case "anal_alt", "analalt" -> Optional.of(ANAL_ALT);
         case "kobold_anal", "koboldanal" -> Optional.of(KOBOLD_ANAL);
         case "sitting" -> Optional.of(SITTING);
         case "sitting_alt", "sittingalt" -> Optional.of(SITTING_ALT);
         case "missionary" -> Optional.of(MISSIONARY);
         case "mating_press", "matingpress" -> Optional.of(MATING_PRESS);
         case "reverse_cowgirl", "reversecowgirl" -> Optional.of(REVERSE_COWGIRL);
         case "cowgirl" -> Optional.of(COWGIRL);
         case "strip" -> Optional.of(STRIP);
         default -> Optional.empty();
      };
   }
}
