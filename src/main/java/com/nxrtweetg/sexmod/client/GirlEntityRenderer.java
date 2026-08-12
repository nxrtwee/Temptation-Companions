package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.girls.scene.GirlScenePhase;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import com.nxrtweetg.sexmod.girls.inventory.GirlArmorMaterial;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneBodyConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public final class GirlEntityRenderer extends GeoEntityRenderer<SceneGirlEntity, LivingEntityRenderState> {
   private static final double FACEFUCK_STEVE_RENDER_OFFSET_Y = -0.15;
   private static final double MATING_PRESS_STEVE_RENDER_OFFSET_Y = 0.56;
   private static final double MATING_PRESS_STEVE_RENDER_OFFSET_Z = 0.18;
   private static final double FACEFUCK_NAMEPLATE_Y_OFFSET = 0.4;
   private static final float FACEFUCK_EYELASH_SCALE_Y = 1.1925F;
   private static final float FACEFUCK_BROW_L_ROT_Z = (float)Math.toRadians(-0.75);
   private static final float FACEFUCK_BROW_R_ROT_Z = (float)Math.toRadians(-2.0);
   private static final Vec3 DEFAULT_LIGHT_DIRECTION = new Vec3(0.2, 0.8, 0.0).normalize();
   /** Armour bone groups and the flesh they cover, from the original's JennyModel. */
   private static final String[] HELMET_BONES = {"armorHelmet"};
   private static final String[] HELMET_FLESH = {};
   private static final String[] CHESTPLATE_BONES = {"armorShoulderR", "armorShoulderL", "armorChest", "armorBoobs"};
   private static final String[] CHESTPLATE_FLESH = {"boobsFlesh", "upperBodyL", "upperBodyR"};
   private static final String[] LEGGINGS_BONES = {
      "armorBootyR", "armorBootyL", "armorPantsLowL", "armorPantsLowR", "armorPantsUpR", "armorPantsUpL", "armorHip"
   };
   private static final String[] LEGGINGS_FLESH = {"vagina", "curvesL", "curvesR", "kneeL", "kneeR"};
   private static final String[] BOOTS_BONES = {"armorShoesL", "armorShoesR"};
   private static final String[] BOOTS_FLESH = {};
   /** Slime sits on a blob of herself, and only while the doggy scene is running. */
   private static final String[] SLIME_BLOB_BONES = {"bedSlime", "bedSlimeLayer"};
   /** Rolled back: the recreated 1.12.2 tinting still reads wrong in-game. Flip to re-enable. */
   private static final boolean SOFT_SHADING_ENABLED = false;

   public GirlEntityRenderer(Context context, GirlGeoModel model) {
      super(context, model);
      this.shadowRadius = 0.45F;
      this.withRenderLayer(new GirlPaymentGeoLayer(context, this));
      this.withRenderLayer(new GirlWeaponGeoLayer(context, this));
      this.withRenderLayer(new GirlSteveGeoLayer(this));
   }

   @Override
   public LivingEntityRenderState createRenderState(SceneGirlEntity animatable, Void relatedObject) {
      return new LivingEntityRenderState();
   }

   /**
    * During a scene the camera sits inside the model and faces away from it, so the entity's own
    * bounding box falls behind the near plane and vanilla culls it. The original mod set
    * {@code ignoreFrustumCheck} for the same reason.
    */
   @Override
   protected boolean affectedByCulling(SceneGirlEntity entity) {
      return !entity.shouldRenderPlayerTracked();
   }

   @Override
   public void addRenderData(SceneGirlEntity animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
      super.addRenderData(animatable, relatedObject, renderState, partialTick);
      renderState.addGeckolibData(GirlRenderData.CHARACTER, animatable.characterId());
      GirlSceneAction action = animatable.trackedActiveAction().orElse(null);
      if (action != null) {
         renderState.addGeckolibData(GirlRenderData.ACTIVE_ACTION, action);
      }

      renderState.addGeckolibData(GirlRenderData.SCENE_PHASE, animatable.trackedScenePhase());
      renderState.addGeckolibData(GirlRenderData.RENDER_PLAYER, animatable.shouldRenderPlayerTracked());
      int variant = animatable.trackedModelVariant();
      renderState.addGeckolibData(
         GirlRenderData.MODEL_VARIANT, variant != 0 && variant != 1 ? animatable.coreState().currentModel() : variant
      );
      renderState.addGeckolibData(GirlRenderData.PAYMENT_AMOUNT, animatable.trackedPaymentAmount());
      renderState.addGeckolibData(GirlRenderData.PAYMENT_ITEM, animatable.trackedPaymentItem());
      renderState.addGeckolibData(GirlRenderData.TUNE_FORWARD, animatable.trackedSceneTuneForward());
      renderState.addGeckolibData(GirlRenderData.TUNE_SIDE, animatable.trackedSceneTuneSide());
      renderState.addGeckolibData(GirlRenderData.TUNE_VERTICAL, animatable.trackedSceneTuneVertical());
      renderState.addGeckolibData(GirlRenderData.KOBOLD_SIDE_OFFSET, animatable.koboldBlowjobVisualSideOffset());
      renderState.addGeckolibData(GirlRenderData.CAMERA_YAW, resolveCameraYaw(animatable));
      renderState.addGeckolibData(GirlRenderData.LIGHT_DIRECTION, GirlSoftShading.resolveLightDirection(animatable, partialTick));
      renderState.addGeckolibData(GirlRenderData.HELMET_MATERIAL, GirlArmorMaterial.indexFor(animatable.getItemBySlot(EquipmentSlot.HEAD)));
      renderState.addGeckolibData(GirlRenderData.CHESTPLATE_MATERIAL, GirlArmorMaterial.indexFor(animatable.getItemBySlot(EquipmentSlot.CHEST)));
      renderState.addGeckolibData(GirlRenderData.LEGGINGS_MATERIAL, GirlArmorMaterial.indexFor(animatable.getItemBySlot(EquipmentSlot.LEGS)));
      renderState.addGeckolibData(GirlRenderData.BOOTS_MATERIAL, GirlArmorMaterial.indexFor(animatable.getItemBySlot(EquipmentSlot.FEET)));
      // Скин партнёра резолвится один раз за сцену в tickClient (GirlSceneCameraController),
      // а не каждый кадр — это единственный способ избежать мерцания при загрузке текстуры.
      Identifier partnerSkin = GirlSceneCameraController.getLockedPartnerSkin();
      if (partnerSkin != null) {
         renderState.addGeckolibData(GirlRenderData.PARTNER_SKIN, partnerSkin);
         renderState.addGeckolibData(GirlRenderData.PARTNER_IS_SLIM, GirlSceneCameraController.isLockedPartnerSlim());
      }
   }

   @Override
   protected void extractNameTags(SceneGirlEntity entity, LivingEntityRenderState renderState, float partialTick) {
      boolean facefuck = entity.trackedActiveAction().filter(action -> action == GirlSceneAction.FACEFUCK).isPresent();
      this.extractNameTags(entity, renderState, partialTick, 0.0, facefuck ? FACEFUCK_NAMEPLATE_Y_OFFSET : 0.0);
   }

   @Override
   public void adjustModelBonesForRender(RenderPassInfo<LivingEntityRenderState> renderPassInfo, BoneSnapshots snapshots) {
      super.adjustModelBonesForRender(renderPassInfo, snapshots);
      LivingEntityRenderState renderState = renderPassInfo.renderState();
      GirlSceneAction action = renderState.getGeckolibData(GirlRenderData.ACTIVE_ACTION);
      GirlScenePhase phase = renderState.getOrDefaultGeckolibData(GirlRenderData.SCENE_PHASE, GirlScenePhase.IDLE);
      boolean renderPlayer = renderState.getOrDefaultGeckolibData(GirlRenderData.RENDER_PLAYER, false);
      String character = renderState.getOrDefaultGeckolibData(GirlRenderData.CHARACTER, "jenny");

      applyPlayerBoneVisibility(snapshots);
      applyPaymentBoneVisibility(snapshots, renderState);
      applyArmorBoneVisibility(snapshots, renderState);
      applySceneBodyScale(snapshots, character, action);
      applyGirlScale(snapshots, character, action, phase);
      // Скрываем неправильный вариант рук партнёра (Alex vs Steve) чтобы не было z-fighting
      boolean partnerIsSlim = renderState.getOrDefaultGeckolibData(GirlRenderData.PARTNER_IS_SLIM, false);
      applyPartnerArmModel(snapshots, partnerIsSlim);

      if (action == GirlSceneAction.FACEFUCK && (phase == GirlScenePhase.ACTIVE)) {
         applyFacefuckFacePose(snapshots);
      }

      boolean blushActions = action == GirlSceneAction.KOBOLD_BLOWJOB || action == GirlSceneAction.MATING_PRESS;
      if (blushActions && (phase == GirlScenePhase.ACTIVE || phase == GirlScenePhase.CLIMAX)) {
         snapshots.ifPresent("blush", bone -> bone.setTranslation(0.0F, 0.0F, -1.0F));
      }

      if (action == GirlSceneAction.MATING_PRESS && phase != GirlScenePhase.PAYMENT) {
         snapshots.ifPresent("LeftLeg", bone -> bone.setRotation(0.0F, 0.0F, 0.0F));
         snapshots.ifPresent("RightLeg", bone -> bone.setRotation(0.0F, 0.0F, 0.0F));
      }

      applyBodySceneTune(snapshots, renderState, action);
      applyHelperTransform(snapshots, renderState, character, action, phase);
      applySlimeRig(snapshots, character, action);

      if (renderPlayer && action != null) {
         registerCameraAnchor(renderPassInfo, renderState, character, action, phase);
      }
   }

   @Override
   public void submitRenderTasks(
      RenderPassInfo<LivingEntityRenderState> renderPassInfo, OrderedSubmitNodeCollector renderTasks, RenderType renderType
   ) {
      if (renderType == null || renderPassInfo.model().isMissingno() || !SOFT_SHADING_ENABLED) {
         super.submitRenderTasks(renderPassInfo, renderTasks, renderType);
         return;
      }

      int packedLight = renderPassInfo.packedLight();
      int packedOverlay = renderPassInfo.packedOverlay();
      int renderColor = renderPassInfo.renderColor();
      Vec3 lightDirection = renderPassInfo.renderState().getOrDefaultGeckolibData(GirlRenderData.LIGHT_DIRECTION, DEFAULT_LIGHT_DIRECTION);
      renderTasks.submitCustomGeometry(renderPassInfo.poseStack(), renderType, (pose, vertexConsumer) -> {
         PoseStack poseStack = renderPassInfo.poseStack();
         poseStack.pushPose();
         poseStack.last().set(pose);
         VertexConsumer softShaded = new SoftShadingVertexConsumer(vertexConsumer, lightDirection);
         renderPassInfo.renderPosed(() -> renderPassInfo.model().render(renderPassInfo, softShaded, packedLight, packedOverlay, renderColor));
         poseStack.popPose();
      });
   }

   /**
    * The partner is never drawn by the main pass: his bones sample a different atlas, so
    * {@link GirlSteveGeoLayer} draws that subtree separately. Hiding the whole subtree here is what
    * keeps him out of the girl's own texture pass.
    *
    * <p>Within the steve subtree each limb ships both a SLIM (Alex) and a WIDE (Steve) variant.
    * Both would overlap and z-fight, so we hide whichever variant does not match the active player's
    * skin model — read from {@link GirlRenderData#PARTNER_IS_SLIM}.
    */
   private static void applyPlayerBoneVisibility(BoneSnapshots snapshots) {
      snapshots.ifPresent("steve", bone -> bone.skipRender(true).skipChildrenRender(true));
      // cock/ballL/ballR/bone123/bone99 are children of Torso2 inside steve.
      // skipChildrenRender on steve already hides them in the main pass, but
      // GirlSteveGeoLayer restores steve's flags and calls positionAndRender — which
      // recurses into these children. Hide them explicitly so the skin pass skips them
      // and they can be drawn separately with a solid flesh colour.
      snapshots.ifPresent("cock",    b -> b.skipRender(true).skipChildrenRender(true));
      snapshots.ifPresent("ballL",   b -> b.skipRender(true).skipChildrenRender(true));
      snapshots.ifPresent("ballR",   b -> b.skipRender(true).skipChildrenRender(true));
      snapshots.ifPresent("bone123", b -> b.skipRender(true).skipChildrenRender(true));
      snapshots.ifPresent("bone99",  b -> b.skipRender(true).skipChildrenRender(true));
   }

   /**
    * Hides the arm-model variant that does not match the player's skin (Alex vs Steve).
    * Must be called inside {@code renderPosed} in {@link GirlSteveGeoLayer} after the
    * steve bone is revealed, so snapshots are the animated ones.
    *
    * <p>Bone pairs in the rig:
    * <pre>
    *   steveLeftArm  → leftArmAlex   (hide when WIDE)
    *                 → leftArmSteve  (hide when SLIM)
    *   LeftLowerArm  → leftLowerArmAlex   (hide when WIDE)
    *                 → leftLowerArmSteve  (hide when SLIM)
    *   (mirror for right arm)
    * </pre>
    */
   static void applyPartnerArmModel(BoneSnapshots snapshots, boolean isSlim) {
      // SLIM = Alex: hide Steve variants, show Alex variants
      // WIDE = Steve: hide Alex variants, show Steve variants
      String hideLeft       = isSlim ? "leftArmSteve"      : "leftArmAlex";
      String hideLowerLeft  = isSlim ? "leftLowerArmSteve" : "leftLowerArmAlex";
      String hideRight      = isSlim ? "rightArmSteve"     : "rightArmAlex";
      String hideLowerRight = isSlim ? "rightLowerArmSteve": "rightLowerArmAlex";
      snapshots.ifPresent(hideLeft,       b -> b.skipRender(true).skipChildrenRender(true));
      snapshots.ifPresent(hideLowerLeft,  b -> b.skipRender(true).skipChildrenRender(true));
      snapshots.ifPresent(hideRight,      b -> b.skipRender(true).skipChildrenRender(true));
      snapshots.ifPresent(hideLowerRight, b -> b.skipRender(true).skipChildrenRender(true));
   }

   /**
    * Slime's rig needs two things the shared code cannot express, both from the original's
    * SlimeModel: the blob she sits on only exists during the doggy scene, and her hat is a top-level
    * bone that has to be driven off the head by hand.
    */
   private static void applySlimeRig(BoneSnapshots snapshots, String character, GirlSceneAction action) {
      if (!"slime".equals(character)) {
         return;
      }

      // SlimeModel.actionsWithSlime -- the blob is part of the doggy scene and nothing else
      boolean showBlob = action == GirlSceneAction.DOGGY;
      for (String name : SLIME_BLOB_BONES) {
         snapshots.ifPresent(name, bone -> bone.skipRender(!showBlob).skipChildrenRender(!showBlob));
      }

      // SlimeModel.a(new String[]{"head"}, "hat") -- "hat" hangs off "dress", not off the head, so
      // the animation never moves it. Copy the head's pose onto it every frame.
      snapshots.get("head")
         .ifPresent(
            head -> snapshots.ifPresent(
               "hat",
               hat -> hat.setRotation(head.getRotX(), head.getRotY(), head.getRotZ())
                  .setTranslation(head.getTranslateX(), head.getTranslateY(), head.getTranslateZ())
            )
         );
   }

   private static void applyPaymentBoneVisibility(BoneSnapshots snapshots, LivingEntityRenderState renderState) {
      int amount = renderState.getOrDefaultGeckolibData(GirlRenderData.PAYMENT_AMOUNT, 0);
      snapshots.ifPresent("items", bone -> bone.skipRender(amount <= 0).skipChildrenRender(amount <= 0));

      // the baked per-currency cubes are superseded by GirlPaymentGeoLayer, which draws the real item
      for (var paymentItem : com.nxrtweetg.sexmod.girls.state.GirlPaymentItem.values()) {
         String prefix = paymentItem.name().toLowerCase();

         for (int i = 1; i <= 3; i++) {
            snapshots.ifPresent(prefix + i, bone -> bone.skipRender(true).skipChildrenRender(true));
         }
      }
   }

   /**
    * Shows one material's worth of armour bones per slot and hides the other three, then hides the
    * bare body underneath. The original paired each armour bone group with the flesh it covers
    * (GirlModel.c/f/h/e); the flesh bones it listed that our model does not have are simply skipped.
    */
   private static void applyArmorBoneVisibility(BoneSnapshots snapshots, LivingEntityRenderState renderState) {
      applyArmorSlot(snapshots, renderState, GirlRenderData.HELMET_MATERIAL, HELMET_BONES, HELMET_FLESH);
      applyArmorSlot(snapshots, renderState, GirlRenderData.CHESTPLATE_MATERIAL, CHESTPLATE_BONES, CHESTPLATE_FLESH);
      applyArmorSlot(snapshots, renderState, GirlRenderData.LEGGINGS_MATERIAL, LEGGINGS_BONES, LEGGINGS_FLESH);
      applyArmorSlot(snapshots, renderState, GirlRenderData.BOOTS_MATERIAL, BOOTS_BONES, BOOTS_FLESH);
   }

   private static void applyArmorSlot(
      BoneSnapshots snapshots,
      LivingEntityRenderState renderState,
      com.geckolib.constant.dataticket.DataTicket<Integer> ticket,
      String[] armorBones,
      String[] fleshBones
   ) {
      int materialIndex = renderState.getOrDefaultGeckolibData(ticket, -1);
      GirlArmorMaterial[] materials = GirlArmorMaterial.values();
      boolean armorDrawn = false;

      for (String bone : armorBones) {
         for (int i = 0; i < materials.length; i++) {
            boolean hidden = i != materialIndex;
            String variant = bone + "_" + materials[i].suffix();
            if (!hidden && snapshots.get(variant).isPresent()) {
               armorDrawn = true;
            }

            snapshots.ifPresent(variant, snapshot -> snapshot.skipRender(hidden).skipChildrenRender(hidden));
         }

         // Ellie and Bia also keep the un-suffixed container, whose children are armour pieces the
         // material duplication left behind (Jenny has no such bone). Without this they draw the
         // diamond look no matter what is equipped.
         boolean slotEmpty = materialIndex < 0;
         snapshots.ifPresent(bone, snapshot -> snapshot.skipRender(slotEmpty).skipChildrenRender(slotEmpty));
      }

      // The nude model has no armour bones at all, so hiding the flesh there would just delete her body.
      for (String bone : fleshBones) {
         boolean covered = armorDrawn;
         snapshots.ifPresent(bone, snapshot -> snapshot.skipRender(covered));
      }
   }

   private static void applyFacefuckFacePose(BoneSnapshots snapshots) {
      snapshots.ifPresent("closedL", bone -> bone.setTranslateZ(-1.0F));
      snapshots.ifPresent("openL", bone -> bone.setTranslateZ(1.0F));
      snapshots.ifPresent("closedR", bone -> bone.setTranslateZ(-1.0F));
      snapshots.ifPresent("openR", bone -> bone.setTranslateZ(1.0F));
      snapshots.ifPresent("eyeLashL", bone -> bone.setTranslation(0.1F, -0.125F, 0.0F).setScale(1.0F, FACEFUCK_EYELASH_SCALE_Y, 1.0F));
      snapshots.ifPresent("eyeLashR", bone -> bone.setTranslation(0.0F, -0.32F, 0.0F).setScale(1.0F, FACEFUCK_EYELASH_SCALE_Y, 1.0F));
      snapshots.ifPresent("browL", bone -> bone.setRotZ(FACEFUCK_BROW_L_ROT_Z).setTranslation(0.1F, -0.125F, 0.0F));
      snapshots.ifPresent("browR", bone -> bone.setRotZ(FACEFUCK_BROW_R_ROT_Z).setTranslation(-0.025F, -0.05F, 0.0F));
   }

   private static void applySceneBodyScale(BoneSnapshots snapshots, String character, GirlSceneAction action) {
      float breastScale = action == null ? 1.0F : (float)GirlSceneBodyConfig.breastScale(character, action);
      float bootyScale = action == null ? 1.0F : (float)GirlSceneBodyConfig.bootyScale(character, action);

      snapshots.ifPresent("boobs", bone -> bone.setScale(breastScale, breastScale, breastScale));

      snapshots.ifPresent("booty", bone -> bone.setScale(bootyScale, bootyScale, bootyScale));
   }

   /**
    * Her overall size, on her root bone only. The partner lives under {@code steve} and is scaled
    * separately in {@link #applyHelperTransform}, so the two can be sized independently. Multiplied
    * into whatever the animation set, since nearly every animation drives this bone.
    */
   private static void applyGirlScale(BoneSnapshots snapshots, String character, GirlSceneAction action, GirlScenePhase phase) {
      if (action == null) {
         return;
      }

      float scale = (float)GirlSceneCameraConfig.girlScale(character, action, cameraConfigPhase(phase));
      if (scale != 1.0F) {
         bodyRoot(snapshots).ifPresent(bone -> bone.setScale(bone.getScaleX() * scale, bone.getScaleY() * scale, bone.getScaleZ() * scale));
         // `items` is a root bone (not under `body`), so body-scale doesn't propagate to it.
         // Manually scale its translation so payment items land at the correct world height
         // for each character (Bia×0.84, Ellie×1.17, etc.).
         snapshots.ifPresent("items", bone ->
            bone.setTranslation(
               bone.getTranslateX() * scale,
               bone.getTranslateY() * scale,
               bone.getTranslateZ() * scale
            )
         );
      }
   }

   /**
    * The bone everything of hers hangs off. Slime's rig calls it {@code dd} instead of {@code body},
    * and the original picked between the two the same way (GirlModel.java:224).
    */
   private static Optional<BoneSnapshot> bodyRoot(BoneSnapshots snapshots) {
      Optional<BoneSnapshot> body = snapshots.get("body");
      return body.isPresent() ? body : snapshots.get("dd");
   }

   private static void applyBodySceneTune(BoneSnapshots snapshots, LivingEntityRenderState renderState, GirlSceneAction action) {
      if (action == GirlSceneAction.DEEPTHROAT
         || action == GirlSceneAction.KOBOLD_BLOWJOB
         || action == GirlSceneAction.REVERSE_COWGIRL
         || action == GirlSceneAction.TOUCH_BOOBS) {
         double side = renderState.getOrDefaultGeckolibData(GirlRenderData.TUNE_SIDE, 0.0);
         if (action == GirlSceneAction.KOBOLD_BLOWJOB) {
            side += renderState.getOrDefaultGeckolibData(GirlRenderData.KOBOLD_SIDE_OFFSET, 0.0);
         }

         double vertical = renderState.getOrDefaultGeckolibData(GirlRenderData.TUNE_VERTICAL, 0.0);
         double forward = renderState.getOrDefaultGeckolibData(GirlRenderData.TUNE_FORWARD, 0.0);
         float sideOffset = (float)side;
         bodyRoot(snapshots)
            .ifPresent(
               bone -> bone.setTranslation(
                  bone.getTranslateX() + sideOffset, bone.getTranslateY() + (float)vertical, bone.getTranslateZ() + (float)forward
               )
            );
      }
   }

   /**
    * Places the partner. Every scene can be tuned; the two scenes that were authored with a built-in
    * offset keep it as their starting point, so their existing tuning still lands in the same place.
    *
    * <p>The offsets are added to whatever the animation put on the bone rather than replacing it:
    * doggy and friends animate {@code steve}'s own position, and overwriting it froze the partner in
    * place.
    */
   private static void applyHelperTransform(
      BoneSnapshots snapshots, LivingEntityRenderState renderState, String character, GirlSceneAction action, GirlScenePhase phase
   ) {
      if (action == null || (action == GirlSceneAction.MATING_PRESS && phase == GirlScenePhase.PAYMENT)) {
         return;
      }

      GirlScenePhase configPhase = cameraConfigPhase(phase);
      double baseVertical = switch (action) {
         case FACEFUCK -> FACEFUCK_STEVE_RENDER_OFFSET_Y;
         case MATING_PRESS -> MATING_PRESS_STEVE_RENDER_OFFSET_Y;
         default -> 0.0;
      };
      double baseForward = action == GirlSceneAction.MATING_PRESS ? MATING_PRESS_STEVE_RENDER_OFFSET_Z : 0.0;
      float vertical = (float)(baseVertical + GirlSceneCameraConfig.helperVerticalOffset(character, action, configPhase));
      float forward = (float)(baseForward + GirlSceneCameraConfig.helperForwardOffset(character, action, configPhase));
      float side = (float)GirlSceneCameraConfig.helperSideOffset(character, action, configPhase);
      float scale = (float)GirlSceneCameraConfig.helperScale(character, action, configPhase);

      snapshots.ifPresent("steve", bone -> {
         bone.setTranslation(bone.getTranslateX() + side, bone.getTranslateY() + vertical, bone.getTranslateZ() + forward)
            .setScale(bone.getScaleX() * scale, bone.getScaleY() * scale, bone.getScaleZ() * scale);
         if (action == GirlSceneAction.MATING_PRESS) {
            bone.setRotX(bone.getRotX() + (float)Math.toRadians(-90.0));
         }
      });
   }

   private static void registerCameraAnchor(
      RenderPassInfo<LivingEntityRenderState> renderPassInfo, LivingEntityRenderState renderState, String character, GirlSceneAction action, GirlScenePhase phase
   ) {
      renderPassInfo.addBonePositionListener("Head2", (worldPos, rotation, scale) -> {
         if (worldPos != null) {
            float yaw = renderState.getOrDefaultGeckolibData(GirlRenderData.CAMERA_YAW, 0.0F);
            Vec3 forward = horizontalForward(yaw);
            Vec3 right = horizontalRight(yaw);
            GirlScenePhase configPhase = cameraConfigPhase(phase);

            double distance = switch (action) {
               case BLOWJOB, KOBOLD_BLOWJOB, DEEPTHROAT -> 0.34;
               case FACEFUCK -> 0.3;
               case BOOBJOB, TOUCH_BOOBS -> 0.46;
               case KOBOLD_ANAL, SITTING, SITTING_ALT, REVERSE_COWGIRL, COWGIRL -> 0.42;
               case DOGGY, PRONE_DOGGY -> 0.52;
               case ANAL, ANAL_ALT -> 0.46;
               case MISSIONARY -> 0.36;
               case MATING_PRESS -> 0.4;
               case STRIP -> 0.28;
            } * GirlSceneCameraConfig.distanceMultiplier(character, action, configPhase);

            double vertical = switch (action) {
               case BLOWJOB, KOBOLD_BLOWJOB, DEEPTHROAT -> 0.02;
               case FACEFUCK -> 0.04;
               case BOOBJOB, TOUCH_BOOBS, KOBOLD_ANAL, ANAL, ANAL_ALT -> 0.1;
               case SITTING, SITTING_ALT, MATING_PRESS, REVERSE_COWGIRL, COWGIRL -> 0.08;
               case DOGGY, PRONE_DOGGY -> 0.12;
               case MISSIONARY -> 0.06;
               case STRIP -> 0.0;
            } + GirlSceneCameraConfig.verticalOffset(character, action, configPhase);

            double lateral = GirlSceneCameraConfig.lateralOffset(character, action, configPhase);
            GirlSceneCameraState.update(worldPos.add(forward.scale(distance)).add(right.scale(lateral)).add(0.0, vertical, 0.0));
         }
      });
   }

   private static Vec3 horizontalForward(float yaw) {
      double radians = Math.toRadians(yaw);
      return new Vec3(-Math.sin(radians), 0.0, Math.cos(radians));
   }

   private static Vec3 horizontalRight(float yaw) {
      double radians = Math.toRadians(yaw);
      return new Vec3(Math.cos(radians), 0.0, Math.sin(radians));
   }

   /**
    * The camera anchor must be built from the scene's own orientation, not from where the player
    * happened to be looking: the free-look offsets are applied later, on the camera alone. Using the
    * live player yaw made every playthrough frame the scene differently, so the tuning never held.
    */
   private static float resolveCameraYaw(SceneGirlEntity animatable) {
      return animatable.trackedSceneYaw();
   }

   private static GirlScenePhase cameraConfigPhase(GirlScenePhase phase) {
      return switch (phase) {
         case PAYMENT -> GirlScenePhase.PAYMENT;
         case INTRO -> GirlScenePhase.INTRO;
         default -> GirlScenePhase.ACTIVE;
      };
   }
}
