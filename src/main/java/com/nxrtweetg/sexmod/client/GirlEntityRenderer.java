package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.girls.scene.GirlScenePhase;
import com.nxrtweetg.sexmod.config.SexmodConfig;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.nxrtweetg.sexmod.girls.goblin.GoblinColors;
import com.nxrtweetg.sexmod.girls.inventory.GirlArmorMaterial;
import com.nxrtweetg.sexmod.girls.kobold.KoboldColors;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneBodyConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * The GeckoLib 4 renderer keeps the entity and partial tick as fields and its bones are mutable, so
 * every per-frame rig adjustment the original did is a direct bone write after the animation ran,
 * and every value the 5.x builds push through render-state data tickets is read straight off the
 * synced entity.
 */
@Environment(EnvType.CLIENT)
public final class GirlEntityRenderer extends GeoEntityRenderer<SceneGirlEntity> {
   private static final double FACEFUCK_STEVE_RENDER_OFFSET_Y = -0.15;
   private static final float FACEFUCK_EYELASH_SCALE_Y = 1.1925F;
   private static final float FACEFUCK_BROW_L_ROT_Z = (float)Math.toRadians(-0.75);
   private static final float FACEFUCK_BROW_R_ROT_Z = (float)Math.toRadians(-2.0);
   private static final String[] HELMET_BONES = {"armorHelmet"};
   private static final String[] HELMET_FLESH = {};
   private static final String[] CHESTPLATE_BONES = {
      "armorShoulderR", "armorShoulderL", "armorChest", "armorBoobs", "armorBoobL", "armorBoobR"
   };
   private static final String[] CHESTPLATE_FLESH = {"boobsFlesh", "upperBodyL", "upperBodyR"};
   private static final String[] LEGGINGS_BONES = {
      "armorBootyR", "armorBootyL", "armorPantsLowL", "armorPantsLowR", "armorPantsUpR", "armorPantsUpL", "armorHip",
      "armorKneeR", "armorKneeL",
      "armorCheekR", "armorCheekL", "armorLegL", "armorLegR", "armorShinL", "armorShinR", "armorTorso"
   };
   private static final String[] LEGGINGS_FLESH = {"vagina", "curvesL", "curvesR", "kneeL", "kneeR"};
   private static final String[] BOOTS_BONES = {
      "armorShoesL", "armorShoesR", "armorFootL", "armorFootR"
   };
   private static final String[] BOOTS_FLESH = {};
   private static final String[] SLIME_BLOB_BONES = {"bedSlime", "bedSlimeLayer"};

   private static final Set<String> KOBOLD_MAIN_BONES = Set.of(
      "colorSpots", "neck", "head", "snout", "midSectionR", "midSectionL", "innerCheekLR", "innerCheekRR",
      "gayL", "gayR", "legR", "legL", "shinL", "toesL", "kneeL", "curvesL", "shinR", "toesR", "kneeR",
      "curvesR", "sideL", "sideR", "hip", "torsoL", "torsoR", "armR", "lowerArmR", "ellbowR", "armL",
      "lowerArmL", "ellbowL", "hornUL", "hornUR", "tail", "tail2", "tail3", "tail4", "tail5", "hornDL2",
      "hornDR2", "hornDR3M", "hornDL3M", "frecklesAL1", "frecklesAL2", "frecklesAR1", "frecklesAR2",
      "frecklesHL1", "frecklesHL2", "frecklesHR1", "frecklesHR2"
   );
   private static final Set<String> KOBOLD_SECONDARY_BONES = Set.of(
      "boobR", "boobL", "frontNeck", "Rside", "Lside", "frontAndInside", "innerCheekLL", "innerCheekRL",
      "layer", "layer2", "down", "down2", "down3", "down4", "down5", "fuckhole", "hornDR3S", "hornDL3S",
      "assholeCoverUp", "assholeCoverUp2"
   );
   private static final Set<String> GOBLIN_SKIN_BONES = Set.of(
      "meatTorso", "meatCheekR", "meatCheekL", "meatFootR", "meatFootL", "meatShinR", "meatShinL",
      "meatLegL", "meatLegR", "nippleR", "nippleL", "preggy", "shoeL", "shoeR", "frontAndInside",
      "Lside", "Rside", "cheekR", "cheekL", "fuckhole", "head", "nose", "neck", "armL", "lowerArmL",
      "armR", "lowerArmR", "torso", "LegL", "LegR", "shinL", "shinR", "earL", "earR"
   );
   private static final Set<String> GOBLIN_HAIR_BONES = Set.of(
      "lashR", "lashL", "closedR", "closedL", "browL", "browR"
   );
   private static final String[] CUSTOM_GENITAL_BONES = {
      "cock", "ballL", "ballR", "bone123", "bone99", "bone148", "bone149", "bone150", "bone151", "bone2", "bone14", "bone16", "bone17"
   };
   static final String[] ALEX_ARM_BONES = {
      "leftArmAlex", "leftLowerArmAlex", "rightArmAlex", "rightLowerArmAlex"
   };
   static final String[] STEVE_ARM_BONES = {
      "leftArmSteve", "leftLowerArmSteve", "rightArmSteve", "rightLowerArmSteve"
   };
   private static final String[] CUSTOM_COMPANION_ALEX_ARMS = {
      "armAlexR", "lowerArmAlexR", "armAlexL", "lowerArmAlexL"
   };
   private static final String[] CUSTOM_COMPANION_STEVE_ARMS = {
      "armSteveR", "lowerArmSteveR", "armSteveL", "lowerArmSteveL"
   };

   /** GeckoLib 4.9 keeps no partial tick on the renderer, so the bone pass stores it here. */
   private float scenePartialTick;
   /** Bone whose world position feeds the scene camera this frame, set during the bone pass. */
   private String cameraAnchorBone;
   private GirlSceneAction cameraAnchorAction;
   private GirlScenePhase cameraAnchorPhase;
   private String cameraAnchorCharacter;
   private GirlScenePhase cameraAnchorConfigPhase;

   public GirlEntityRenderer(Context context, GirlGeoModel model) {
      super(context, model);
      this.shadowRadius = 0.45F;
      this.addRenderLayer(new GirlPaymentGeoLayer(this));
      this.addRenderLayer(new GirlWeaponGeoLayer(this));
      this.addRenderLayer(new GirlSteveGeoLayer(this));
   }

   /**
    * During a scene the camera sits inside the model and faces away from it, so the entity's own
    * bounding box falls behind the near plane and vanilla culls it. The original mod set
    * {@code ignoreFrustumCheck} for the same reason.
    */
   @Override
   public boolean shouldRender(SceneGirlEntity entity, Frustum frustum, double camX, double camY, double camZ) {
      if (entity.shouldRenderPlayerTracked()) {
         return true;
      }
      return super.shouldRender(entity, frustum, camX, camY, camZ);
   }

   /**
    * Whether her floating name is drawn at all - the original drew it unconditionally, vanilla only
    * under a name tag item, and a scene hides it because the camera sits inside her head.
    */
   @Override
   public boolean shouldShowName(SceneGirlEntity entity) {
      if (!entity.shouldShowName()) {
         return false;
      }

      if (entity.trackedScenePhase() != GirlScenePhase.IDLE || !entity.customActionId().isEmpty()) {
         return false;
      }

      AbstractClientPlayer player = Minecraft.getInstance().player;
      return player == null || !entity.isInvisibleTo(player);
   }

   /**
    * 1.21.x entity shading is the two-light directional diffuse the original fought as well, and
    * GeckoLib 4 exposes the {@code createVerticesOfQuad} hook the 1.12.2 renderer used to force
    * every quad onto one flat normal - that is the whole trick.
    */
   @Override
   public void createVerticesOfQuad(GeoQuad quad, Matrix4f poseMatrix, Vector3f normal, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
      if (!ShaderCompat.isShadersActive() && SexmodConfig.isFlatLitRenderingEnabled()) {
         normal = new Vector3f(0.0F, 1.0F, 0.0F);
      }
      super.createVerticesOfQuad(quad, poseMatrix, normal, buffer, packedLight, packedOverlay, color);
   }

   /**
    * The per-frame bone pass: everything the 1.12.2 renderer did to the rig after the animation ran.
    * GeckoLib 4 has already ticked the animation by the time the super call returns, and the bones
    * are mutable, so every adjustment is a direct write.
    */
   @Override
   public void preRender(
      PoseStack poseStack, SceneGirlEntity animatable, BakedGeoModel model, MultiBufferSource bufferSource,
      VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int renderColor
   ) {
      this.scenePartialTick = partialTick;
      super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, renderColor);

      GirlSceneAction action = animatable.trackedActiveAction().orElse(null);
      GirlScenePhase phase = animatable.trackedScenePhase();
      boolean renderPlayer = animatable.shouldRenderPlayerTracked();
      String character = animatable.characterId();

      applyPlayerBoneVisibility(model, character);
      if (character.startsWith("custom")) {
         applyCustomCompanionRig(model, animatable);
      }
      applyPaymentBoneVisibility(model, animatable);
      applyArmorBoneVisibility(model, animatable);
      applySceneBodyScale(model, character, action);
      applyGirlScale(model, character, action, phase);
      if (animatable instanceof com.nxrtweetg.sexmod.girls.allie.AllieEntity allie) {
         float allieScale = allie.getDismissScale(this.scenePartialTick);
         if (allieScale < 1.0F) {
            bodyRoot(model).ifPresent(bone -> {
               bone.setScaleX(bone.getScaleX() * allieScale);
               bone.setScaleY(bone.getScaleY() * allieScale);
               bone.setScaleZ(bone.getScaleZ() * allieScale);
            });
         }
      }
      // Arms for partner are hidden in GirlSteveGeoLayer during partner pass

      if (action == GirlSceneAction.FACEFUCK && (phase == GirlScenePhase.ACTIVE)) {
         applyFacefuckFacePose(model);
      }

      boolean blushActions = action == GirlSceneAction.KOBOLD_BLOWJOB || action == GirlSceneAction.MATING_PRESS;
      if (blushActions && (phase == GirlScenePhase.ACTIVE || phase == GirlScenePhase.CLIMAX)) {
         bone(model, "blush").ifPresent(b -> b.setPosZ(-1.0F));
      }

      applyBodySceneTune(model, animatable, action);
      applyHelperTransform(model, character, action, phase);
      applyHeadRotation(model, animatable, phase, partialTick);
      applySlimeRig(model, character, action);
      applyBeeRig(model, animatable, character);
      applyGoblinRig(model, animatable, character);
      applyGalathRig(model, animatable, character);
      applyManglelieRig(model, animatable, character);

      this.cameraAnchorBone = null;
      if (renderPlayer && action != null) {
         registerCameraAnchor(model, character, action, phase);
      }
   }

   private static void applyPlayerBoneVisibility(BakedGeoModel model, String character) {
      // steve subtree is the partner rig, rendered exclusively in GirlSteveGeoLayer during scenes
      bone(model, "steve").ifPresent(b -> {
         b.setHidden(true);
         b.setChildrenHidden(true);
      });
      for (String boneName : CUSTOM_GENITAL_BONES) {
         bone(model, boneName).ifPresent(b -> {
            b.setHidden(true);
            b.setChildrenHidden(true);
         });
      }
   }

   private static void applyCustomCompanionRig(BakedGeoModel model, SceneGirlEntity entity) {
      if (!(entity instanceof com.nxrtweetg.sexmod.girls.custom.CustomGirlEntity customGirl)) {
         return;
      }
      com.nxrtweetg.sexmod.girls.custom.TCEditorLoader.CustomCharacterData data = customGirl.getCustomData();
      if (data == null) {
         return;
      }

      boolean isSlim = "slim".equalsIgnoreCase(data.modelType());
      for (String boneName : CUSTOM_COMPANION_ALEX_ARMS) {
         setBoneHidden(model, boneName, !isSlim);
      }
      for (String boneName : CUSTOM_COMPANION_STEVE_ARMS) {
         setBoneHidden(model, boneName, isSlim);
      }

      setBoneHidden(model, "boobs", !data.hasBreasts());
      if (data.hasBreasts() && data.breastScale() != 1.0F) {
         bone(model, "boobs").ifPresent(b -> scaleBone(b, data.breastScale()));
      }

      setBoneHidden(model, "booty", !data.hasBooty());
      if (data.hasBooty() && data.bootyScale() != 1.0F) {
         bone(model, "booty").ifPresent(b -> scaleBone(b, data.bootyScale()));
      }

      float hx = data.headScaleX();
      float hy = data.headScaleY();
      float hz = data.headScaleZ();
      if (hx != 1.0F || hy != 1.0F || hz != 1.0F) {
         mulScale(model, "head", hx, hy, hz);
         mulScale(model, "Head2", hx, hy, hz);
      }

      float tx = data.torsoScaleX();
      float ty = data.torsoScaleY();
      float tz = data.torsoScaleZ();
      if (tx != 1.0F || ty != 1.0F || tz != 1.0F) {
         mulScale(model, "torso", tx, ty, tz);
         mulScale(model, "Torso2", tx, ty, tz);
      }

      float ax = data.armScaleX();
      float ay = data.armScaleY();
      float az = data.armScaleZ();
      if (ax != 1.0F || ay != 1.0F || az != 1.0F) {
         mulScale(model, "armR", ax, ay, az);
         mulScale(model, "armL", ax, ay, az);
         mulScale(model, "LeftArm", ax, ay, az);
         mulScale(model, "RightArm", ax, ay, az);
      }

      float lx = data.legScaleX();
      float ly = data.legScaleY();
      float lz = data.legScaleZ();
      if (lx != 1.0F || ly != 1.0F || lz != 1.0F) {
         mulScale(model, "legR", lx, ly, lz);
         mulScale(model, "legL", lx, ly, lz);
         mulScale(model, "LeftLeg", lx, ly, lz);
         mulScale(model, "RightLeg", lx, ly, lz);
      }

      float scale = data.scale();
      if (scale != 1.0F) {
         bone(model, "body").ifPresent(b -> scaleBone(b, scale));
         bone(model, "steve").ifPresent(b -> scaleBone(b, scale));
      }
   }

   private static void setBoneHidden(BakedGeoModel model, String name, boolean hidden) {
      bone(model, name).ifPresent(b -> {
         b.setHidden(hidden);
         b.setChildrenHidden(hidden);
      });
   }

   /**
    * Hides the arm-model variant that does not match the player's skin (Alex vs Steve).
    * Called from {@link GirlSteveGeoLayer} while the steve bone is revealed for its own pass.
    */
   static void applyPartnerArmModel(BakedGeoModel model, boolean isSlim) {
      String hideLeft       = isSlim ? "leftArmSteve"      : "leftArmAlex";
      String hideLowerLeft  = isSlim ? "leftLowerArmSteve" : "leftLowerArmAlex";
      String hideRight      = isSlim ? "rightArmSteve"     : "rightArmAlex";
      String hideLowerRight = isSlim ? "rightLowerArmSteve": "rightLowerArmAlex";
      for (String name : new String[] {hideLeft, hideLowerLeft, hideRight, hideLowerRight}) {
         bone(model, name).ifPresent(b -> {
            b.setHidden(true);
            b.setChildrenHidden(true);
         });
      }
   }

   private static void applyHeadRotation(BakedGeoModel model, SceneGirlEntity animatable, GirlScenePhase phase, float partialTick) {
      if (phase == GirlScenePhase.IDLE || phase == GirlScenePhase.WALKING || phase == GirlScenePhase.WAITING) {
         float pitch = Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot());
         float yaw = Mth.lerp(partialTick, animatable.yRotO, animatable.getYRot());
         float headPitch = (float) Math.toRadians(Mth.clamp(pitch, -85.0F, 85.0F));
         float headYaw = (float) Math.toRadians(Mth.clamp(-yaw, -85.0F, 85.0F));
         for (String name : new String[] {"head", "Head2"}) {
            bone(model, name).ifPresent(bone -> {
               bone.setRotX(bone.getRotX() + headPitch);
               bone.setRotY(bone.getRotY() + headYaw);
            });
         }
      }
   }

   private static void applySlimeRig(BakedGeoModel model, String character, GirlSceneAction action) {
      if (!"slime".equals(character)) {
         return;
      }

      boolean showBlob = action == GirlSceneAction.DOGGY;
      for (String name : SLIME_BLOB_BONES) {
         bone(model, name).ifPresent(bone -> {
            bone.setHidden(!showBlob);
            bone.setChildrenHidden(!showBlob);
         });
      }

      Optional<GeoBone> head = bone(model, "head");
      head.ifPresent(h -> bone(model, "hat").ifPresent(hat -> {
         hat.updateRotation(h.getRotX(), h.getRotY(), h.getRotZ());
         hat.setPosX(h.getPosX());
         hat.setPosY(h.getPosY());
         hat.setPosZ(h.getPosZ());
      }));
   }

   private static void applyBeeRig(BakedGeoModel model, SceneGirlEntity animatable, String character) {
      if (!"bee".equals(character) || !(animatable instanceof com.nxrtweetg.sexmod.girls.bee.BeeEntity bee)) {
         return;
      }

      boolean hasChest = bee.hasChest();
      bone(model, "chest").ifPresent(bone -> {
         bone.setHidden(!hasChest);
         bone.setChildrenHidden(!hasChest);
      });
   }

   private static void applyGoblinRig(BakedGeoModel model, SceneGirlEntity animatable, String character) {
      if (!"goblin".equals(character) || !(animatable instanceof com.nxrtweetg.sexmod.girls.goblin.GoblinEntity goblin)) {
         return;
      }
      int crown = goblin.crown();
      bone(model, "crown").ifPresent(bone -> {
         bone.setHidden(crown == 0);
         bone.setChildrenHidden(crown == 0);
      });

      int hairStyle = Math.floorMod(goblin.hairStyle(), 6);
      for (int i = 0; i < 6; i++) {
         boolean hide = i != hairStyle;
         bone(model, "hair" + i).ifPresent(bone -> {
            bone.setHidden(hide);
            bone.setChildrenHidden(hide);
         });
      }
   }

   private static void applyGalathRig(BakedGeoModel model, SceneGirlEntity animatable, String character) {
      if (!"galath".equals(character)) {
         return;
      }
      for (String name : new String[] {"coin", "pentagram", "futa", "futaCock", "futaBallLL", "futaBallLR"}) {
         bone(model, name).ifPresent(bone -> {
            bone.setHidden(true);
            bone.setChildrenHidden(true);
         });
      }

      int variant = modelVariantOf(animatable);
      boolean stripped = (variant == 0);
      setBoneHidden(model, "braBoobL", stripped);
      setBoneHidden(model, "braBoobR", stripped);
      setBoneHidden(model, "slip", stripped);
      setBoneHidden(model, "nippleL", !stripped);
      setBoneHidden(model, "nippleR", !stripped);
   }

   private static void applyManglelieRig(BakedGeoModel model, SceneGirlEntity animatable, String character) {
      if (!"manglelie".equals(character)) {
         return;
      }
      int variant = modelVariantOf(animatable);
      boolean hasSkirt = (variant != 0);

      for (String name : new String[] {"skirt", "sideLSkirt", "sideRSkirt", "cheekSkirtL", "cheekSkirtR"}) {
         setBoneHidden(model, name, !hasSkirt);
      }
      for (String name : new String[] {"sideLNoSkirt", "sideRNoSkirt", "cheekLBelowSkirt", "cheekRBelowSkirt"}) {
         setBoneHidden(model, name, hasSkirt);
      }
   }

   /** The tracked variant, with the core-state fallback the 5.x render state carried. */
   private static int modelVariantOf(SceneGirlEntity animatable) {
      int variant = animatable.trackedModelVariant();
      return variant != 0 && variant != 1 ? animatable.coreState().currentModel() : variant;
   }

   /**
    * Kobold and Goblin bodies are procedurally tinted per bone, which the pipeline has no hook for,
    * so their cubes go through a colour-forcing {@link VertexConsumer} wrapper and keep rendering
    * through the ordinary recursive pass.
    */
   @Override
   public void renderRecursively(
      PoseStack poseStack, SceneGirlEntity animatable, GeoBone bone,
      RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
      boolean isReRender, float partialTick, int packedLight, int packedOverlay, int renderColor
   ) {
      String character = animatable.characterId();
      if ("kobold".equals(character) || "goblin".equals(character)) {
         int boneColor = resolveCustomBoneColor(character, bone.getName(), animatable, renderColor);
         if (boneColor != renderColor) {
            buffer = new BoneColorVertexConsumer(buffer, boneColor);
         }
      }

      captureCameraAnchor(poseStack, bone);

      super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, renderColor);
   }

   /**
    * The bone-position-listener equivalent: the anchor bone's world position is evaluated when the
    * recursion reaches it, from the same matrices the cubes are drawn with.
    */
   private void captureCameraAnchor(PoseStack poseStack, GeoBone bone) {
      if (this.cameraAnchorBone == null || !this.cameraAnchorBone.equals(bone.getName())) {
         return;
      }

      poseStack.pushPose();
      RenderUtil.prepMatrixForBone(poseStack, bone);
      Matrix4f pose = poseStack.last().pose();
      Vector3f modelPos = new Vector3f(pose.m30(), pose.m31(), pose.m32());
      poseStack.popPose();

      SceneGirlEntity animatable = this.animatable;
      float partialTick = this.scenePartialTick;
      Vector3f entityLocal = this.modelRenderTranslations.transformPosition(new Vector3f(modelPos));
      Vec3 worldPos = new Vec3(
         Mth.lerp(partialTick, animatable.xo, animatable.getX()) + entityLocal.x(),
         Mth.lerp(partialTick, animatable.yo, animatable.getY()) + entityLocal.y(),
         Mth.lerp(partialTick, animatable.zo, animatable.getZ()) + entityLocal.z()
      );

      float yaw = animatable.trackedSceneYaw();
      Vec3 forward = horizontalForward(yaw);
      Vec3 right = horizontalRight(yaw);

      GirlSceneAction action = this.cameraAnchorAction;
      String character = this.cameraAnchorCharacter;
      GirlScenePhase configPhase = this.cameraAnchorConfigPhase;

      double distance = cameraBaseDistance(action) * GirlSceneCameraConfig.distanceMultiplier(character, action, configPhase);
      double vertical = cameraBaseVertical(action) + GirlSceneCameraConfig.verticalOffset(character, action, configPhase);
      double lateral = GirlSceneCameraConfig.lateralOffset(character, action, configPhase);

      Vec3 anchor = worldPos.add(forward.scale(distance)).add(right.scale(lateral)).add(0.0, vertical, 0.0);
      if (!SexmodConfig.isBoneCamDynamic() && holdsStillPose(this.cameraAnchorPhase)) {
         GirlSceneCameraState.update(List.of(String.valueOf(character), action, distance, vertical, lateral), anchor);
      } else {
         GirlSceneCameraState.update(anchor);
      }
   }

   private static int resolveCustomBoneColor(String character, String boneName, SceneGirlEntity animatable, int defaultColor) {
      if ("kobold".equals(character) && animatable instanceof com.nxrtweetg.sexmod.girls.kobold.KoboldEntity kobold) {
         return resolveKoboldBoneColor(boneName, kobold, defaultColor);
      }
      if ("goblin".equals(character) && animatable instanceof com.nxrtweetg.sexmod.girls.goblin.GoblinEntity goblin) {
         return resolveGoblinBoneColor(boneName, goblin, defaultColor);
      }
      return defaultColor;
   }

   private static int resolveKoboldBoneColor(String boneName, com.nxrtweetg.sexmod.girls.kobold.KoboldEntity kobold, int defaultColor) {
      int tribeIdx = kobold.tribeColorIndex();
      KoboldColors.TribeColor tribe = KoboldColors.TribeColor.byId(tribeIdx);

      if (KOBOLD_MAIN_BONES.contains(boneName)) {
         return tribe.mainArgb();
      }
      if (KOBOLD_SECONDARY_BONES.contains(boneName)) {
         return tribe.secondaryArgb();
      }
      if ("irisR".equals(boneName) || "irisL".equals(boneName)) {
         return kobold.eyeColorArgb();
      }
      return 0xFFFFFFFF;
   }

   private static int resolveGoblinBoneColor(String boneName, com.nxrtweetg.sexmod.girls.goblin.GoblinEntity goblin, int defaultColor) {
      if (boneName.contains("band")) {
         return 0xFFFFFFFF;
      }
      if (boneName.contains("eyeColor") || boneName.contains("eyeColor2")) {
         return GoblinColors.EyeColor.byId(goblin.eyeColorIndex()).argb();
      }
      if (boneName.contains("hair") || GOBLIN_HAIR_BONES.contains(boneName)) {
         return GoblinColors.HairColor.byId(goblin.hairColorIndex()).argb();
      }
      if (boneName.contains("variant") || boneName.contains("boob") || GOBLIN_SKIN_BONES.contains(boneName)) {
         return GoblinColors.SkinColor.byId(goblin.skinColorIndex()).argb();
      }
      return defaultColor;
   }

   private static void applyPaymentBoneVisibility(BakedGeoModel model, SceneGirlEntity animatable) {
      int amount = animatable.trackedPaymentAmount();
      bone(model, "items").ifPresent(bone -> {
         bone.setHidden(amount <= 0);
         bone.setChildrenHidden(amount <= 0);
      });

      for (var paymentItem : com.nxrtweetg.sexmod.girls.state.GirlPaymentItem.values()) {
         String prefix = paymentItem.name().toLowerCase();

         for (int i = 1; i <= 3; i++) {
            setBoneHidden(model, prefix + i, true);
         }
      }
   }

   private static void applyArmorBoneVisibility(BakedGeoModel model, SceneGirlEntity animatable) {
      applyArmorSlot(model, animatable, GirlArmorMaterial.indexFor(animatable.getItemBySlot(EquipmentSlot.HEAD)), HELMET_BONES, HELMET_FLESH);
      applyArmorSlot(model, animatable, GirlArmorMaterial.indexFor(animatable.getItemBySlot(EquipmentSlot.CHEST)), CHESTPLATE_BONES, CHESTPLATE_FLESH);
      applyArmorSlot(model, animatable, GirlArmorMaterial.indexFor(animatable.getItemBySlot(EquipmentSlot.LEGS)), LEGGINGS_BONES, LEGGINGS_FLESH);
      applyArmorSlot(model, animatable, GirlArmorMaterial.indexFor(animatable.getItemBySlot(EquipmentSlot.FEET)), BOOTS_BONES, BOOTS_FLESH);
   }

   private static void applyArmorSlot(BakedGeoModel model, SceneGirlEntity animatable, int materialIndex, String[] armorBones, String[] fleshBones) {
      GirlArmorMaterial[] materials = GirlArmorMaterial.values();
      boolean armorDrawn = false;

      for (String boneName : armorBones) {
         for (int i = 0; i < materials.length; i++) {
            boolean hidden = i != materialIndex;
            String variant = boneName + "_" + materials[i].suffix();
            if (!hidden && bone(model, variant).isPresent()) {
               armorDrawn = true;
            }

            setBoneHidden(model, variant, hidden);
         }

         boolean slotEmpty = materialIndex < 0;
         setBoneHidden(model, boneName, slotEmpty);
      }

      for (String boneName : fleshBones) {
         boolean covered = armorDrawn;
         bone(model, boneName).ifPresent(bone -> bone.setHidden(covered));
      }
   }

   private static void applyFacefuckFacePose(BakedGeoModel model) {
      bone(model, "closedL").ifPresent(b -> b.setPosZ(-1.0F));
      bone(model, "openL").ifPresent(b -> b.setPosZ(1.0F));
      bone(model, "closedR").ifPresent(b -> b.setPosZ(-1.0F));
      bone(model, "openR").ifPresent(b -> b.setPosZ(1.0F));
      bone(model, "eyeLashL").ifPresent(b -> {
         b.setPosX(0.1F);
         b.setPosY(-0.125F);
         b.setPosZ(0.0F);
         b.setScaleY(FACEFUCK_EYELASH_SCALE_Y);
      });
      bone(model, "eyeLashR").ifPresent(b -> {
         b.setPosX(0.0F);
         b.setPosY(-0.32F);
         b.setPosZ(0.0F);
         b.setScaleY(FACEFUCK_EYELASH_SCALE_Y);
      });
      bone(model, "browL").ifPresent(b -> {
         b.setRotZ(FACEFUCK_BROW_L_ROT_Z);
         b.setPosX(0.1F);
         b.setPosY(-0.125F);
      });
      bone(model, "browR").ifPresent(b -> {
         b.setRotZ(FACEFUCK_BROW_R_ROT_Z);
         b.setPosX(-0.025F);
         b.setPosY(-0.05F);
      });
   }

   private static void applySceneBodyScale(BakedGeoModel model, String character, GirlSceneAction action) {
      float breastScale = (float)GirlSceneBodyConfig.breastScale(character, action);
      float bootyScale = (float)GirlSceneBodyConfig.bootyScale(character, action);

      bone(model, "boobs").ifPresent(b -> scaleBone(b, breastScale));
      bone(model, "booty").ifPresent(b -> scaleBone(b, bootyScale));
   }

   private static void applyGirlScale(BakedGeoModel model, String character, GirlSceneAction action, GirlScenePhase phase) {
      float globalScale = com.nxrtweetg.sexmod.config.SexmodConfig.getGirlScale(character);
      float sceneScale = (action != null) ? (float)GirlSceneCameraConfig.girlScale(character, action, cameraConfigPhase(phase)) : 1.0F;
      float scale = globalScale * sceneScale;
      if (scale != 1.0F) {
         bodyRoot(model).ifPresent(bone -> {
            bone.setScaleX(bone.getScaleX() * scale);
            bone.setScaleY(bone.getScaleY() * scale);
            bone.setScaleZ(bone.getScaleZ() * scale);
         });
         bone(model, "items").ifPresent(bone -> {
            bone.setPosX(bone.getPosX() * scale);
            bone.setPosY(bone.getPosY() * scale);
            bone.setPosZ(bone.getPosZ() * scale);
         });
      }
   }

   private static Optional<GeoBone> bodyRoot(BakedGeoModel model) {
      Optional<GeoBone> body = bone(model, "body");
      return body.isPresent() ? body : bone(model, "dd");
   }

   private static Optional<GeoBone> bone(BakedGeoModel model, String name) {
      return model.getBone(name);
   }

   private static void scaleBone(GeoBone bone, float scale) {
      bone.setScaleX(scale);
      bone.setScaleY(scale);
      bone.setScaleZ(scale);
   }

   private static void mulScale(BakedGeoModel model, String name, float sx, float sy, float sz) {
      bone(model, name).ifPresent(bone -> {
         bone.setScaleX(bone.getScaleX() * sx);
         bone.setScaleY(bone.getScaleY() * sy);
         bone.setScaleZ(bone.getScaleZ() * sz);
      });
   }

   private static void applyBodySceneTune(BakedGeoModel model, SceneGirlEntity animatable, GirlSceneAction action) {
      if (action != null) {
         double side = animatable.trackedSceneTuneSide();
         if (action == GirlSceneAction.KOBOLD_BLOWJOB) {
            side += animatable.koboldBlowjobVisualSideOffset();
         }

         double vertical = animatable.trackedSceneTuneVertical();
         double forward = animatable.trackedSceneTuneForward();
         float sideOffset = (float)side;
         bodyRoot(model).ifPresent(bone -> {
            bone.setPosX(bone.getPosX() + sideOffset);
            bone.setPosY(bone.getPosY() + (float)vertical);
            bone.setPosZ(bone.getPosZ() + (float)forward);
         });
      }
   }

   private static void applyHelperTransform(BakedGeoModel model, String character, GirlSceneAction action, GirlScenePhase phase) {
      if (action == null || (action == GirlSceneAction.MATING_PRESS && phase == GirlScenePhase.PAYMENT)) {
         return;
      }

      GirlScenePhase configPhase = cameraConfigPhase(phase);
      double baseVertical = switch (action) {
         case FACEFUCK -> FACEFUCK_STEVE_RENDER_OFFSET_Y;
         default -> 0.0;
      };
      double baseForward = 0.0;
      float vertical = (float)(baseVertical + GirlSceneCameraConfig.helperVerticalOffset(character, action, configPhase));
      float forward = (float)(baseForward + GirlSceneCameraConfig.helperForwardOffset(character, action, configPhase));
      float side = (float)GirlSceneCameraConfig.helperSideOffset(character, action, configPhase);
      float scale = (float)GirlSceneCameraConfig.helperScale(character, action, configPhase);

      bone(model, "steve").ifPresent(bone -> {
         bone.setPosX(bone.getPosX() + side);
         bone.setPosY(bone.getPosY() + vertical);
         bone.setPosZ(bone.getPosZ() + forward);
         bone.setScaleX(bone.getScaleX() * scale);
         bone.setScaleY(bone.getScaleY() * scale);
         bone.setScaleZ(bone.getScaleZ() * scale);
      });
   }

   private void registerCameraAnchor(BakedGeoModel model, String character, GirlSceneAction action, GirlScenePhase phase) {
      GirlSceneCameraState.clearRig();
      if (SexmodConfig.isBoneCamDynamic()) {
         this.cameraAnchorBone = bone(model, "Head2").isPresent() ? "Head2" :
                          (bone(model, GirlSceneCamRig.BOY_CAM).isPresent() ? GirlSceneCamRig.BOY_CAM : GirlSceneCamRig.GIRL_CAM);
      } else {
         this.cameraAnchorBone = "Head2";
      }
      this.cameraAnchorAction = action;
      this.cameraAnchorCharacter = character;
      this.cameraAnchorPhase = phase;
      this.cameraAnchorConfigPhase = cameraConfigPhase(phase);
   }

   private static double cameraBaseDistance(GirlSceneAction action) {
      return switch (action) {
         case BLOWJOB, KOBOLD_BLOWJOB, DEEPTHROAT -> 0.34;
         case FACEFUCK -> 0.3;
         case BOOBJOB, TOUCH_BOOBS -> 0.46;
         case KOBOLD_ANAL, SITTING, SITTING_ALT, REVERSE_COWGIRL, COWGIRL, BEE_SEX, NELSON, THREESOME -> 0.42;
         case DOGGY, PRONE_DOGGY -> 0.52;
         case ANAL, ANAL_ALT -> 0.46;
         case MISSIONARY -> 0.36;
         case MATING_PRESS -> 0.4;
         case STRIP -> 0.28;
         case HEAD_PAT -> 0.45;
      };
   }

   private static double cameraBaseVertical(GirlSceneAction action) {
      return switch (action) {
         case BLOWJOB, KOBOLD_BLOWJOB, DEEPTHROAT -> 0.02;
         case FACEFUCK -> 0.04;
         case BOOBJOB, TOUCH_BOOBS, KOBOLD_ANAL, ANAL, ANAL_ALT -> 0.1;
         case SITTING, SITTING_ALT, MATING_PRESS, REVERSE_COWGIRL, COWGIRL, BEE_SEX, NELSON, THREESOME -> 0.08;
         case DOGGY, PRONE_DOGGY -> 0.12;
         case MISSIONARY -> 0.06;
         case STRIP -> 0.0;
         case HEAD_PAT -> 0.05;
      };
   }

   private static boolean holdsStillPose(GirlScenePhase phase) {
      return switch (phase) {
         case INTRO, ACTIVE, CLIMAX -> true;
         default -> false;
      };
   }

   private static Vec3 horizontalForward(float yaw) {
      double radians = Math.toRadians(yaw);
      return new Vec3(-Math.sin(radians), 0.0, Math.cos(radians));
   }

   private static Vec3 horizontalRight(float yaw) {
      double radians = Math.toRadians(yaw);
      return new Vec3(Math.cos(radians), 0.0, Math.sin(radians));
   }

   private static GirlScenePhase cameraConfigPhase(GirlScenePhase phase) {
      return switch (phase) {
         case PAYMENT -> GirlScenePhase.PAYMENT;
         case INTRO -> GirlScenePhase.INTRO;
         default -> GirlScenePhase.ACTIVE;
      };
   }
}
