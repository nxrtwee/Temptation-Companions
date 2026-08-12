package com.nxrtweetg.sexmod.girls.state;

import java.util.UUID;
import net.minecraft.core.BlockPos;

public final class GirlCoreState {
   private final GirlAnimationState animationState = GirlAnimationState.createDefault();
   private int currentModel = 1;
   private String girlName = "girl";
   private boolean shouldBeAtTarget;
   private boolean shouldBeAtTargetYaw;
   private boolean shouldGetDamage = true;
   private boolean playerIsInPosition;
   private boolean playerIsThrusting;
   private boolean playerIsCumming;
   private boolean isForPreloading;
   private double walkSpeed = 0.35;
   private float targetYaw;
   private float playerYaw;
   private int paymentItemsAmount;
   private GirlPaymentItem paymentItem = GirlPaymentItem.DIAMOND;
   private GirlFollowUpAction followUpAction = GirlFollowUpAction.NONE;
   private UUID activePlayerId;
   /** Player she follows around, the original's "master" data parameter. */
   private UUID masterId;
   private BlockPos homePos;
   private boolean goingHome;

   public GirlAnimationState animationState() {
      return this.animationState;
   }

   public int currentModel() {
      return this.currentModel;
   }

   public void setCurrentModel(int currentModel) {
      this.currentModel = currentModel;
   }

   public String girlName() {
      return this.girlName;
   }

   public void setGirlName(String girlName) {
      this.girlName = girlName;
   }

   public boolean shouldBeAtTarget() {
      return this.shouldBeAtTarget;
   }

   public void setShouldBeAtTarget(boolean shouldBeAtTarget) {
      this.shouldBeAtTarget = shouldBeAtTarget;
   }

   public boolean shouldBeAtTargetYaw() {
      return this.shouldBeAtTargetYaw;
   }

   public void setShouldBeAtTargetYaw(boolean shouldBeAtTargetYaw) {
      this.shouldBeAtTargetYaw = shouldBeAtTargetYaw;
   }

   public boolean shouldGetDamage() {
      return this.shouldGetDamage;
   }

   public void setShouldGetDamage(boolean shouldGetDamage) {
      this.shouldGetDamage = shouldGetDamage;
   }

   public boolean playerIsInPosition() {
      return this.playerIsInPosition;
   }

   public void setPlayerIsInPosition(boolean playerIsInPosition) {
      this.playerIsInPosition = playerIsInPosition;
   }

   public boolean playerIsThrusting() {
      return this.playerIsThrusting;
   }

   public void setPlayerIsThrusting(boolean playerIsThrusting) {
      this.playerIsThrusting = playerIsThrusting;
   }

   public boolean playerIsCumming() {
      return this.playerIsCumming;
   }

   public void setPlayerIsCumming(boolean playerIsCumming) {
      this.playerIsCumming = playerIsCumming;
   }

   public boolean isForPreloading() {
      return this.isForPreloading;
   }

   public void setForPreloading(boolean forPreloading) {
      this.isForPreloading = forPreloading;
   }

   public double walkSpeed() {
      return this.walkSpeed;
   }

   public void setWalkSpeed(double walkSpeed) {
      this.walkSpeed = walkSpeed;
   }

   public float targetYaw() {
      return this.targetYaw;
   }

   public void setTargetYaw(float targetYaw) {
      this.targetYaw = targetYaw;
   }

   public float playerYaw() {
      return this.playerYaw;
   }

   public void setPlayerYaw(float playerYaw) {
      this.playerYaw = playerYaw;
   }

   public int paymentItemsAmount() {
      return this.paymentItemsAmount;
   }

   public void setPaymentItemsAmount(int paymentItemsAmount) {
      this.paymentItemsAmount = paymentItemsAmount;
   }

   public GirlPaymentItem paymentItem() {
      return this.paymentItem;
   }

   public void setPaymentItem(GirlPaymentItem paymentItem) {
      this.paymentItem = paymentItem;
   }

   public GirlFollowUpAction followUpAction() {
      return this.followUpAction;
   }

   public void setFollowUpAction(GirlFollowUpAction followUpAction) {
      this.followUpAction = followUpAction;
   }

   public UUID activePlayerId() {
      return this.activePlayerId;
   }

   public void setActivePlayerId(UUID activePlayerId) {
      this.activePlayerId = activePlayerId;
   }

   public UUID masterId() {
      return this.masterId;
   }

   public void setMasterId(UUID masterId) {
      this.masterId = masterId;
   }

   public boolean hasMaster() {
      return this.masterId != null;
   }

   public BlockPos homePos() {
      return this.homePos;
   }

   public void setHomePos(BlockPos homePos) {
      this.homePos = homePos;
   }

   public boolean isGoingHome() {
      return this.goingHome;
   }

   public void setGoingHome(boolean goingHome) {
      this.goingHome = goingHome;
   }

   public void clearInteraction() {
      this.playerIsInPosition = false;
      this.playerIsThrusting = false;
      this.playerIsCumming = false;
      this.activePlayerId = null;
      this.followUpAction = GirlFollowUpAction.NONE;
      this.animationState.set(GirlAnimationParameter.IS_DOING_ACTION, false);
      this.animationState.set(GirlAnimationParameter.AUTO_HEAD, true);
      this.animationState.set(GirlAnimationParameter.AUTO_EYES, true);
      this.animationState.set(GirlAnimationParameter.MOVEMENT_ACTIVE, true);
      this.animationState.set(GirlAnimationParameter.PLAYER_SHOULD_BE_RENDERED, false);
   }
}
