package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.girls.scene.GirlScenePhase;
import com.nxrtweetg.sexmod.config.SexmodConfig;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import com.nxrtweetg.sexmod.girls.goblin.GoblinColors;
import com.nxrtweetg.sexmod.girls.inventory.GirlArmorMaterial;
import com.nxrtweetg.sexmod.girls.kobold.KoboldColors;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneBodyConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public final class GirlEntityRenderer extends GeoEntityRenderer<SceneGirlEntity, LivingEntityRenderState> {
   private static final double FACEFUCK_STEVE_RENDER_OFFSET_Y = -0.15;
   private static final double FACEFUCK_NAMEPLATE_Y_OFFSET = 0.4;
   private static final float FACEFUCK_EYELASH_SCALE_Y = 1.1925F;
   private static final float FACEFUCK_BROW_L_ROT_Z = (float)Math.toRadians(-0.75);
   private static final float FACEFUCK_BROW_R_ROT_Z = (float)Math.toRadians(-2.0);
   /** Armour bone groups and the flesh they cover, from the original mod. */
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
   /** Slime sits on a blob of herself, and only while the doggy scene is running. */
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
      "cock", "ballL", "ballR", "bone123", "bone99", "bone148", "bone149", "bone150", "bone151"
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
      renderState.addGeckolibData(DataTickets.IS_MOVING, isDisplacing(animatable));
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

      if (animatable instanceof com.nxrtweetg.sexmod.girls.kobold.KoboldEntity kobold) {
         renderState.addGeckolibData(GirlRenderData.KOBOLD_TRIBE_COLOR, kobold.tribeColorIndex());
         renderState.addGeckolibData(GirlRenderData.KOBOLD_EYE_COLOR, kobold.eyeColorArgb());
      }
      if (animatable instanceof com.nxrtweetg.sexmod.girls.goblin.GoblinEntity goblin) {
         renderState.addGeckolibData(GirlRenderData.GOBLIN_SKIN_COLOR, goblin.skinColorIndex());
         renderState.addGeckolibData(GirlRenderData.GOBLIN_HAIR_COLOR, goblin.hairColorIndex());
         renderState.addGeckolibData(GirlRenderData.GOBLIN_EYE_COLOR, goblin.eyeColorIndex());
         renderState.addGeckolibData(GirlRenderData.GOBLIN_HAIR_STYLE, goblin.hairStyle());
         renderState.addGeckolibData(GirlRenderData.GOBLIN_CROWN, goblin.crown());
      }
      if (animatable instanceof com.nxrtweetg.sexmod.girls.bee.BeeEntity bee) {
         renderState.addGeckolibData(GirlRenderData.HAS_CHEST, bee.hasChest());
      }
      if (animatable instanceof com.nxrtweetg.sexmod.girls.allie.AllieEntity allie) {
         renderState.addGeckolibData(GirlRenderData.ALLIE_DISMISS_SCALE, allie.getDismissScale(partialTick));
      }
      renderState.addGeckolibData(GirlRenderData.CAMERA_YAW, resolveCameraYaw(animatable));
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

      if (animatable instanceof com.nxrtweetg.sexmod.girls.custom.CustomGirlEntity customGirl) {
         com.nxrtweetg.sexmod.girls.custom.TCEditorLoader.CustomCharacterData data = customGirl.getCustomData();
         if (data != null) {
            Identifier skin = CustomSkinManager.getOrLoadSkin(data.id(), data.skinFileName());
            renderState.addGeckolibData(GirlRenderData.CUSTOM_SKIN, skin);
            boolean isSlim = "slim".equalsIgnoreCase(data.modelType());
            renderState.addGeckolibData(GirlRenderData.CUSTOM_IS_SLIM, isSlim);
            renderState.addGeckolibData(GirlRenderData.CUSTOM_SCALE, data.scale());
            renderState.addGeckolibData(GirlRenderData.CUSTOM_HAS_BREASTS, data.hasBreasts());
            renderState.addGeckolibData(GirlRenderData.CUSTOM_BREAST_SCALE, data.breastScale());
            renderState.addGeckolibData(GirlRenderData.CUSTOM_HAS_BOOTY, data.hasBooty());
            renderState.addGeckolibData(GirlRenderData.CUSTOM_BOOTY_SCALE, data.bootyScale());
            renderState.addGeckolibData(GirlRenderData.CUSTOM_HEAD_SCALE_X, data.headScaleX());
            renderState.addGeckolibData(GirlRenderData.CUSTOM_HEAD_SCALE_Y, data.headScaleY());
            renderState.addGeckolibData(GirlRenderData.CUSTOM_HEAD_SCALE_Z, data.headScaleZ());
            renderState.addGeckolibData(GirlRenderData.CUSTOM_TORSO_SCALE_X, data.torsoScaleX());
            renderState.addGeckolibData(GirlRenderData.CUSTOM_TORSO_SCALE_Y, data.torsoScaleY());
            renderState.addGeckolibData(GirlRenderData.CUSTOM_TORSO_SCALE_Z, data.torsoScaleZ());
            renderState.addGeckolibData(GirlRenderData.CUSTOM_ARM_SCALE_X, data.armScaleX());
            renderState.addGeckolibData(GirlRenderData.CUSTOM_ARM_SCALE_Y, data.armScaleY());
            renderState.addGeckolibData(GirlRenderData.CUSTOM_ARM_SCALE_Z, data.armScaleZ());
            renderState.addGeckolibData(GirlRenderData.CUSTOM_LEG_SCALE_X, data.legScaleX());
            renderState.addGeckolibData(GirlRenderData.CUSTOM_LEG_SCALE_Y, data.legScaleY());
            renderState.addGeckolibData(GirlRenderData.CUSTOM_LEG_SCALE_Z, data.legScaleZ());
            renderState.addGeckolibData(GirlRenderData.CUSTOM_COSMETICS_COLOR, data.cosmeticsColor());
         }
      }
   }

   /**
    * Whether she moved during this tick, which is the only thing the original ever asked before picking
    * the walk take over the idle one: {@code |prevPosX - posX| + |prevPosZ - posZ| > 0} in Jenny's
    * movement controller, evaluated on the client.
    *
    * <p>GeckoLib fills {@link DataTickets#IS_MOVING} from {@code LivingEntity.walkAnimation.speed()},
    * which is a decaying average - it stays above the threshold for several ticks after she has actually
    * stopped, and that tail is what left her marching on the spot at the bed. Overwriting the ticket here
    * (the render state is a plain map, and {@code fillRenderState} runs
    * {@code captureDefaultRenderState} before this hook) fixes it for every controller at once, since
    * {@code AnimationTest.isMoving()} only reads the ticket back.
    *
    * <p>{@code ClientLevel.tickNonPassenger} calls {@code setOldPosAndRot} before ticking an entity, so
    * {@code xo}/{@code zo} hold the position at the start of the tick - the same quantity 1.12.2 read out
    * of {@code prevPosX}/{@code prevPosZ}.
    */
   private static boolean isDisplacing(SceneGirlEntity animatable) {
      return Math.abs(animatable.xo - animatable.getX()) + Math.abs(animatable.zo - animatable.getZ()) > 0.005;
   }



   /**
    * Whether her floating name is drawn at all.
    *
    * <p>The original draws it unconditionally: the girl renderer overrides {@code renderName} and calls
    * {@code renderLivingLabel} for anyone within 300 blocks, with no custom-name test whatsoever. The
    * only things that suppress it are the config's own toggle, the entity being an off-world GUI dummy,
    * and - silly, but that is what the code does - the crosshair not happening to be on some entity.
    * Vanilla's rule is the opposite: a mob needs a name tag item <em>and</em> the crosshair on her, which
    * is why no girl here ever had a plate.
    *
    * <p>So we keep the original's "always visible" half and drop the rest, with one deliberate
    * difference: a scene hides the plate. The original leaves it on, and since the scene camera sits
    * inside her head that prints her name across the middle of every shot. GeckoLib widens the vanilla
    * rule (it adds team and invisibility tests on top) but keeps the custom-name requirement, so this
    * has to replace its version rather than delegate to it.
    *
    * <p>The entity's own {@code shouldShowName()} is still consulted, because that is where a character
    * opts out entirely - the slime does, matching {@code fn.t()} returning false unconditionally - and
    * every other girl is named in her constructor with the flag set, so it reads true for them.
    */
   @Override
   public boolean shouldShowName(SceneGirlEntity entity, double distanceToCameraSq) {
      if (!entity.shouldShowName()) {
         return false;
      }

      if (entity.trackedScenePhase() != GirlScenePhase.IDLE || !entity.customActionId().isEmpty()) {
         return false;
      }

      // The distance cutoff is vanilla's, applied by extractNameTags before this is consulted.
      AbstractClientPlayer player = Minecraft.getInstance().player;
      return player == null || !entity.isInvisibleTo(player);
   }

   /**
    * The floating name, nudged for characters whose head does not sit where the hitbox says it does.
    *
    * <p>The nudge cannot go through {@code extractNameTags(entity, state, tick, double, double)}: those
    * two doubles are the distance at which the plate stops being drawn and the distance at which the
    * score line stops, not offsets. Passing an offset as the first of them cut the draw distance to
    * zero, which is why no girl had a name plate at all. The plate's position comes from the
    * {@code NAME_TAG} attachment the extraction resolves, so the nudge belongs on that.
    */
   @Override
   protected void extractNameTags(SceneGirlEntity entity, LivingEntityRenderState renderState, float partialTick) {
      super.extractNameTags(entity, renderState, partialTick);
      boolean facefuck = entity.trackedActiveAction().filter(action -> action == GirlSceneAction.FACEFUCK).isPresent();
      double offset = entity.nameplateYOffset() + (facefuck ? FACEFUCK_NAMEPLATE_Y_OFFSET : 0.0);
      if (offset != 0.0 && renderState.nameTagAttachment != null) {
         renderState.nameTagAttachment = renderState.nameTagAttachment.add(0.0, offset, 0.0);
      }
   }

   /**
    * GeckoLib does submit the plate, just not from {@code submit}: its own body only calls
    * {@code performRenderPass}, which ends with {@code postRenderPass}, and
    * {@code GeoEntityRenderer.postRenderPass} calls {@code EntityRenderer.submit} (by {@code invokespecial},
    * so an override here would not be reached) after the pose stack has been popped back to entity space.
    * That is the vanilla body, leash and name plate included, so nothing needs adding here - an extra
    * {@code submitNameDisplay} call only drew the text twice.
    */
   @Override
   public void adjustModelBonesForRender(RenderPassInfo<LivingEntityRenderState> renderPassInfo, BoneSnapshots snapshots) {
      super.adjustModelBonesForRender(renderPassInfo, snapshots);
      LivingEntityRenderState renderState = renderPassInfo.renderState();
      GirlSceneAction action = renderState.getGeckolibData(GirlRenderData.ACTIVE_ACTION);
      GirlScenePhase phase = renderState.getOrDefaultGeckolibData(GirlRenderData.SCENE_PHASE, GirlScenePhase.IDLE);
      boolean renderPlayer = renderState.getOrDefaultGeckolibData(GirlRenderData.RENDER_PLAYER, false);
      String character = renderState.getOrDefaultGeckolibData(GirlRenderData.CHARACTER, "jenny");

      applyPlayerBoneVisibility(renderPassInfo, snapshots, character);
      if (character.startsWith("custom")) {
         applyCustomCompanionRig(renderPassInfo, snapshots, renderState);
      }
      applyPaymentBoneVisibility(snapshots, renderState);
      applyArmorBoneVisibility(snapshots, renderState);
      applySceneBodyScale(snapshots, character, action);
      applyGirlScale(snapshots, character, action, phase);
      float allieScale = renderState.getOrDefaultGeckolibData(GirlRenderData.ALLIE_DISMISS_SCALE, 1.0F);
      if (allieScale < 1.0F) {
         bodyRoot(snapshots).ifPresent(bone -> bone.setScale(bone.getScaleX() * allieScale, bone.getScaleY() * allieScale, bone.getScaleZ() * allieScale));
      }
      // Arms for partner are hidden in GirlSteveGeoLayer during partner pass

      if (action == GirlSceneAction.FACEFUCK && (phase == GirlScenePhase.ACTIVE)) {
         applyFacefuckFacePose(snapshots);
      }

      boolean blushActions = action == GirlSceneAction.KOBOLD_BLOWJOB || action == GirlSceneAction.MATING_PRESS;
      if (blushActions && (phase == GirlScenePhase.ACTIVE || phase == GirlScenePhase.CLIMAX)) {
         snapshots.ifPresent("blush", bone -> bone.setTranslation(0.0F, 0.0F, -1.0F));
      }

      applyBodySceneTune(snapshots, renderState, action);
      applyHelperTransform(snapshots, renderState, character, action, phase);
      applyHeadRotation(snapshots, renderState, phase);
      applySlimeRig(snapshots, character, action);
      applyBeeRig(snapshots, renderState, character);
      applyGoblinRig(snapshots, renderState, character);
      applyGalathRig(snapshots, renderState, character, action);
      applyManglelieRig(snapshots, renderState, character, action);

      if (renderPlayer && action != null) {
         registerCameraAnchor(renderPassInfo, snapshots, renderState, character, action, phase);
      }
   }

   /**
    * Her flesh is drawn without diffuse lighting, which is what makes the polygons invisible in the
    * original - see {@link GirlFlatLitRenderType}. Only the ordinary opaque path is swapped: the
    * invisible and glowing types GeckoLib picks are left alone, and since
    * {@code RenderTypes.entityCutout} is memoized the identity test is exact.
    *
    * <p>{@link GirlSteveGeoLayer} asks the renderer for its type as well, so the partner's body and the
    * genital mesh come through here too.
    */
   @Override
   public RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
      RenderType renderType = super.getRenderType(renderState, texture);
      if (ShaderCompat.isShadersActive()) {
         return renderType;
      }
      return renderType == RenderTypes.entityCutout(texture) ? GirlFlatLitRenderType.get(texture) : renderType;
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
   private static void applyPlayerBoneVisibility(RenderPassInfo<LivingEntityRenderState> renderPassInfo, BoneSnapshots snapshots, String character) {
      // steve subtree is the partner rig, rendered exclusively in GirlSteveGeoLayer during scenes
      snapshots.ifPresent("steve", bone -> bone.skipRender(true).skipChildrenRender(true));
      setBoneHidden(renderPassInfo, "steve", true);
      for (String bone : CUSTOM_GENITAL_BONES) {
         setBoneHidden(renderPassInfo, bone, true);
      }
   }

   private static void applyCustomCompanionRig(
      RenderPassInfo<LivingEntityRenderState> renderPassInfo,
      BoneSnapshots snapshots,
      LivingEntityRenderState renderState
   ) {
      // 1. Arm format: Alex (Slim) vs Steve (Classic)
      boolean isSlim = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_IS_SLIM, false);
      for (String bone : CUSTOM_COMPANION_ALEX_ARMS) {
         setBoneHidden(renderPassInfo, bone, !isSlim);
      }
      for (String bone : CUSTOM_COMPANION_STEVE_ARMS) {
         setBoneHidden(renderPassInfo, bone, isSlim);
      }

      // 2. Cosmetics: Jenny's Boobs & Booty
      boolean hasBreasts = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_HAS_BREASTS, false);
      float breastScale = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_BREAST_SCALE, 1.0F);
      setBoneHidden(renderPassInfo, "boobs", !hasBreasts);
      if (hasBreasts && breastScale != 1.0F) {
         snapshots.ifPresent("boobs", b -> b.setScale(breastScale, breastScale, breastScale));
      }

      boolean hasBooty = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_HAS_BOOTY, false);
      float bootyScale = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_BOOTY_SCALE, 1.0F);
      setBoneHidden(renderPassInfo, "booty", !hasBooty);
      if (hasBooty && bootyScale != 1.0F) {
         snapshots.ifPresent("booty", b -> b.setScale(bootyScale, bootyScale, bootyScale));
      }

      // 3. Stretches / Dimensions
      float hx = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_HEAD_SCALE_X, 1.0F);
      float hy = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_HEAD_SCALE_Y, 1.0F);
      float hz = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_HEAD_SCALE_Z, 1.0F);
      if (hx != 1.0F || hy != 1.0F || hz != 1.0F) {
         snapshots.ifPresent("head", b -> b.setScale(b.getScaleX() * hx, b.getScaleY() * hy, b.getScaleZ() * hz));
         snapshots.ifPresent("Head2", b -> b.setScale(b.getScaleX() * hx, b.getScaleY() * hy, b.getScaleZ() * hz));
      }

      float tx = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_TORSO_SCALE_X, 1.0F);
      float ty = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_TORSO_SCALE_Y, 1.0F);
      float tz = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_TORSO_SCALE_Z, 1.0F);
      if (tx != 1.0F || ty != 1.0F || tz != 1.0F) {
         snapshots.ifPresent("torso", b -> b.setScale(b.getScaleX() * tx, b.getScaleY() * ty, b.getScaleZ() * tz));
         snapshots.ifPresent("Torso2", b -> b.setScale(b.getScaleX() * tx, b.getScaleY() * ty, b.getScaleZ() * tz));
      }

      float ax = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_ARM_SCALE_X, 1.0F);
      float ay = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_ARM_SCALE_Y, 1.0F);
      float az = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_ARM_SCALE_Z, 1.0F);
      if (ax != 1.0F || ay != 1.0F || az != 1.0F) {
         snapshots.ifPresent("armR", b -> b.setScale(b.getScaleX() * ax, b.getScaleY() * ay, b.getScaleZ() * az));
         snapshots.ifPresent("armL", b -> b.setScale(b.getScaleX() * ax, b.getScaleY() * ay, b.getScaleZ() * az));
         snapshots.ifPresent("LeftArm", b -> b.setScale(b.getScaleX() * ax, b.getScaleY() * ay, b.getScaleZ() * az));
         snapshots.ifPresent("RightArm", b -> b.setScale(b.getScaleX() * ax, b.getScaleY() * ay, b.getScaleZ() * az));
      }

      float lx = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_LEG_SCALE_X, 1.0F);
      float ly = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_LEG_SCALE_Y, 1.0F);
      float lz = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_LEG_SCALE_Z, 1.0F);
      if (lx != 1.0F || ly != 1.0F || lz != 1.0F) {
         snapshots.ifPresent("legR", b -> b.setScale(b.getScaleX() * lx, b.getScaleY() * ly, b.getScaleZ() * lz));
         snapshots.ifPresent("legL", b -> b.setScale(b.getScaleX() * lx, b.getScaleY() * ly, b.getScaleZ() * lz));
         snapshots.ifPresent("LeftLeg", b -> b.setScale(b.getScaleX() * lx, b.getScaleY() * ly, b.getScaleZ() * lz));
         snapshots.ifPresent("RightLeg", b -> b.setScale(b.getScaleX() * lx, b.getScaleY() * ly, b.getScaleZ() * lz));
      }

      // 4. Overall scaling
      float scale = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_SCALE, 1.0F);
      if (scale != 1.0F) {
         snapshots.ifPresent("body", b -> b.setScale(scale, scale, scale));
         snapshots.ifPresent("steve", b -> b.setScale(scale, scale, scale));
      }
   }

   private static void setBoneHidden(RenderPassInfo<?> pass, String name, boolean hidden) {
      pass.model().getBone(name).ifPresent(b -> {
         if (hidden) {
            if (b.frameSnapshot == null) {
               BoneSnapshot.create(b).apply();
            }
            b.frameSnapshot.skipRender(true).skipChildrenRender(true);
         } else {
            if (b.frameSnapshot != null) {
               b.frameSnapshot.skipRender(false).skipChildrenRender(false);
            }
         }
      });
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
    * Rotates the character's head bone (pitch & yaw) when looking around during idle/wandering.
    */
   private static void applyHeadRotation(BoneSnapshots snapshots, LivingEntityRenderState renderState, GirlScenePhase phase) {
      if (phase == GirlScenePhase.IDLE || phase == GirlScenePhase.WALKING || phase == GirlScenePhase.WAITING) {
         float headPitch = (float) Math.toRadians(Mth.clamp(renderState.xRot, -85.0F, 85.0F));
         float headYaw = (float) Math.toRadians(Mth.clamp(-renderState.yRot, -85.0F, 85.0F));
         snapshots.ifPresent("head", bone -> {
            bone.setRotX(bone.getRotX() + headPitch);
            bone.setRotY(bone.getRotY() + headYaw);
         });
         snapshots.ifPresent("Head2", bone -> {
            bone.setRotX(bone.getRotX() + headPitch);
            bone.setRotY(bone.getRotY() + headYaw);
         });
      }
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

   /**
    * The bee's satchel bone. The original's BeeModel hid "chest" unless the take it was playing had
    * "chest" in its name, which amounted to "she is carrying one"; the flag says that directly, and her
    * movement take is pinned to idle_has_chest so the two never disagree.
    */
   private static void applyBeeRig(BoneSnapshots snapshots, LivingEntityRenderState renderState, String character) {
      if (!"bee".equals(character)) {
         return;
      }

      boolean hasChest = renderState.getOrDefaultGeckolibData(GirlRenderData.HAS_CHEST, false);
      snapshots.ifPresent("chest", bone -> bone.skipRender(!hasChest).skipChildrenRender(!hasChest));
   }

   private static void applyGoblinRig(BoneSnapshots snapshots, LivingEntityRenderState renderState, String character) {
      if (!"goblin".equals(character)) {
         return;
      }
      int crown = renderState.getOrDefaultGeckolibData(GirlRenderData.GOBLIN_CROWN, 0);
      snapshots.ifPresent("crown", bone -> bone.skipRender(crown == 0).skipChildrenRender(crown == 0));

      int hairStyle = Math.floorMod(renderState.getOrDefaultGeckolibData(GirlRenderData.GOBLIN_HAIR_STYLE, 0), 6);
      for (int i = 0; i < 6; i++) {
         boolean hide = i != hairStyle;
         snapshots.ifPresent("hair" + i, bone -> bone.skipRender(hide).skipChildrenRender(hide));
      }
   }

   private static void applyGalathRig(
      BoneSnapshots snapshots, LivingEntityRenderState renderState, String character, GirlSceneAction action
   ) {
      if (!"galath".equals(character)) {
         return;
      }
      // Hide floating coin and summon pentagram circle during regular rendering
      snapshots.ifPresent("coin", bone -> bone.skipRender(true).skipChildrenRender(true));
      snapshots.ifPresent("pentagram", bone -> bone.skipRender(true).skipChildrenRender(true));

      // Hide futa bones
      snapshots.ifPresent("futa", bone -> bone.skipRender(true).skipChildrenRender(true));
      snapshots.ifPresent("futaCock", bone -> bone.skipRender(true).skipChildrenRender(true));
      snapshots.ifPresent("futaBallLL", bone -> bone.skipRender(true).skipChildrenRender(true));
      snapshots.ifPresent("futaBallLR", bone -> bone.skipRender(true).skipChildrenRender(true));

      int variant = renderState.getOrDefaultGeckolibData(GirlRenderData.MODEL_VARIANT, 1);
      boolean stripped = (variant == 0);
      snapshots.ifPresent("braBoobL", bone -> bone.skipRender(stripped).skipChildrenRender(stripped));
      snapshots.ifPresent("braBoobR", bone -> bone.skipRender(stripped).skipChildrenRender(stripped));
      snapshots.ifPresent("slip", bone -> bone.skipRender(stripped).skipChildrenRender(stripped));
      snapshots.ifPresent("nippleL", bone -> bone.skipRender(!stripped).skipChildrenRender(!stripped));
      snapshots.ifPresent("nippleR", bone -> bone.skipRender(!stripped).skipChildrenRender(!stripped));
   }

   private static void applyManglelieRig(
      BoneSnapshots snapshots, LivingEntityRenderState renderState, String character, GirlSceneAction action
   ) {
      if (!"manglelie".equals(character)) {
         return;
      }
      int variant = renderState.getOrDefaultGeckolibData(GirlRenderData.MODEL_VARIANT, 1);
      boolean hasSkirt = (variant != 0);

      snapshots.ifPresent("skirt", bone -> bone.skipRender(!hasSkirt).skipChildrenRender(!hasSkirt));
      snapshots.ifPresent("sideLSkirt", bone -> bone.skipRender(!hasSkirt).skipChildrenRender(!hasSkirt));
      snapshots.ifPresent("sideRSkirt", bone -> bone.skipRender(!hasSkirt).skipChildrenRender(!hasSkirt));
      snapshots.ifPresent("cheekSkirtL", bone -> bone.skipRender(!hasSkirt).skipChildrenRender(!hasSkirt));
      snapshots.ifPresent("cheekSkirtR", bone -> bone.skipRender(!hasSkirt).skipChildrenRender(!hasSkirt));

      snapshots.ifPresent("sideLNoSkirt", bone -> bone.skipRender(hasSkirt).skipChildrenRender(hasSkirt));
      snapshots.ifPresent("sideRNoSkirt", bone -> bone.skipRender(hasSkirt).skipChildrenRender(hasSkirt));
      snapshots.ifPresent("cheekLBelowSkirt", bone -> bone.skipRender(hasSkirt).skipChildrenRender(hasSkirt));
      snapshots.ifPresent("cheekRBelowSkirt", bone -> bone.skipRender(hasSkirt).skipChildrenRender(hasSkirt));
   }

   @Override
   public void submitRenderTasks(
      RenderPassInfo<LivingEntityRenderState> renderPassInfo,
      OrderedSubmitNodeCollector renderTasks,
      RenderType renderType
   ) {
      LivingEntityRenderState renderState = renderPassInfo.renderState();
      String character = renderState.getOrDefaultGeckolibData(GirlRenderData.CHARACTER, "jenny");
      if (!"kobold".equals(character) && !"goblin".equals(character) && !CustomSkinManager.isCustomCharacter(character)) {
         super.submitRenderTasks(renderPassInfo, renderTasks, renderType);
         return;
      }

      if (renderType == null) {
         return;
      }
      BakedGeoModel model = renderPassInfo.model();
      if (model.isMissingno()) {
         super.submitRenderTasks(renderPassInfo, renderTasks, renderType);
         return;
      }

      int packedLight = renderPassInfo.packedLight();
      int packedOverlay = renderPassInfo.packedOverlay();
      int defaultColor = renderPassInfo.renderColor();

      renderTasks.submitCustomGeometry(renderPassInfo.poseStack(), renderType, (pose, vc) -> {
         PoseStack ps = renderPassInfo.poseStack();
         ps.pushPose();
         ps.last().set(pose);
         renderPassInfo.renderPosed(() -> {
            for (GeoBone root : model.topLevelBones()) {
               if ("steve".equals(root.name())) continue;
               renderCustomColoredBone(
                  renderPassInfo, ps, root, vc, packedLight, packedOverlay, character, renderState, defaultColor
               );
            }
         });
         ps.popPose();
      });
   }

   private void renderCustomColoredBone(
      RenderPassInfo<LivingEntityRenderState> pass,
      PoseStack ps,
      GeoBone bone,
      VertexConsumer vc,
      int packedLight,
      int packedOverlay,
      String character,
      LivingEntityRenderState renderState,
      int defaultColor
   ) {
      ps.pushPose();
      RenderUtil.prepMatrixForBoneAndUpdateListeners(ps, bone, pass);
      int color = resolveCustomBoneColor(character, bone.name(), renderState, defaultColor);
      bone.render(pass, ps, vc, packedLight, packedOverlay, color);

      BoneSnapshot snap = bone.frameSnapshot;
      if (snap == null || !snap.areChildrenHidden()) {
         for (GeoBone child : bone.children()) {
            renderCustomColoredBone(
               pass, ps, child, vc, packedLight, packedOverlay, character, renderState, defaultColor
            );
         }
      }
      ps.popPose();
   }

   private static int resolveCustomBoneColor(
      String character, String boneName, LivingEntityRenderState renderState, int defaultColor
   ) {
      if ("kobold".equals(character)) {
         return resolveKoboldBoneColor(boneName, renderState, defaultColor);
      }
      if ("goblin".equals(character)) {
         return resolveGoblinBoneColor(boneName, renderState, defaultColor);
      }
      if (CustomSkinManager.isCustomCharacter(character)) {
         if (isCustomCosmeticBone(boneName)) {
            return renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_COSMETICS_COLOR, 0xFFFFFFFF);
         }
         return defaultColor;
      }
      return defaultColor;
   }

   private static boolean isCustomCosmeticBone(String name) {
      return name.startsWith("armorBoobs") || name.startsWith("bra") || name.startsWith("midsection")
         || name.startsWith("midSection") || name.startsWith("armorBooty") || name.startsWith("innerCheek");
   }

   private static int resolveKoboldBoneColor(String boneName, LivingEntityRenderState renderState, int defaultColor) {
      int tribeIdx = renderState.getOrDefaultGeckolibData(
         GirlRenderData.KOBOLD_TRIBE_COLOR, KoboldColors.TribeColor.PURPLE.ordinal()
      );
      KoboldColors.TribeColor tribe = KoboldColors.TribeColor.byId(tribeIdx);

      if (KOBOLD_MAIN_BONES.contains(boneName)) {
         return tribe.mainArgb();
      }
      if (KOBOLD_SECONDARY_BONES.contains(boneName)) {
         return tribe.secondaryArgb();
      }
      if ("irisR".equals(boneName) || "irisL".equals(boneName)) {
         return renderState.getOrDefaultGeckolibData(GirlRenderData.KOBOLD_EYE_COLOR, tribe.mainArgb());
      }
      return 0xFFFFFFFF;
   }

   private static int resolveGoblinBoneColor(String boneName, LivingEntityRenderState renderState, int defaultColor) {
      if (boneName.contains("band")) {
         return 0xFFFFFFFF;
      }
      if (boneName.contains("eyeColor") || boneName.contains("eyeColor2")) {
         int eyeIdx = renderState.getOrDefaultGeckolibData(
            GirlRenderData.GOBLIN_EYE_COLOR, GoblinColors.EyeColor.RED.ordinal()
         );
         return GoblinColors.EyeColor.byId(eyeIdx).argb();
      }
      if (boneName.contains("hair") || GOBLIN_HAIR_BONES.contains(boneName)) {
         int hairIdx = renderState.getOrDefaultGeckolibData(
            GirlRenderData.GOBLIN_HAIR_COLOR, GoblinColors.HairColor.PURPLE.ordinal()
         );
         return GoblinColors.HairColor.byId(hairIdx).argb();
      }
      if (boneName.contains("variant") || boneName.contains("boob") || GOBLIN_SKIN_BONES.contains(boneName)) {
         int skinIdx = renderState.getOrDefaultGeckolibData(
            GirlRenderData.GOBLIN_SKIN_COLOR, GoblinColors.SkinColor.LIGHT_GREEN.ordinal()
         );
         return GoblinColors.SkinColor.byId(skinIdx).argb();
      }
      return defaultColor;
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
      float breastScale = (float)GirlSceneBodyConfig.breastScale(character, action);
      float bootyScale = (float)GirlSceneBodyConfig.bootyScale(character, action);

      snapshots.ifPresent("boobs", bone -> bone.setScale(breastScale, breastScale, breastScale));

      snapshots.ifPresent("booty", bone -> bone.setScale(bootyScale, bootyScale, bootyScale));
   }

   /**
    * Her overall size, on her root bone only. The partner lives under {@code steve} and is scaled
    * separately in {@link #applyHelperTransform}, so the two can be sized independently. Multiplied
    * into whatever the animation set, since nearly every animation drives this bone.
    */
   private static void applyGirlScale(BoneSnapshots snapshots, String character, GirlSceneAction action, GirlScenePhase phase) {
      float globalScale = com.nxrtweetg.sexmod.config.SexmodConfig.getGirlScale(character);
      float sceneScale = (action != null) ? (float)GirlSceneCameraConfig.girlScale(character, action, cameraConfigPhase(phase)) : 1.0F;
      float scale = globalScale * sceneScale;
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
      if (action != null) {
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
         default -> 0.0;
      };
      double baseForward = 0.0;
      float vertical = (float)(baseVertical + GirlSceneCameraConfig.helperVerticalOffset(character, action, configPhase));
      float forward = (float)(baseForward + GirlSceneCameraConfig.helperForwardOffset(character, action, configPhase));
      float side = (float)GirlSceneCameraConfig.helperSideOffset(character, action, configPhase);
      float scale = (float)GirlSceneCameraConfig.helperScale(character, action, configPhase);

      snapshots.ifPresent("steve", bone -> {
         bone.setTranslation(bone.getTranslateX() + side, bone.getTranslateY() + vertical, bone.getTranslateZ() + forward)
            .setScale(bone.getScaleX() * scale, bone.getScaleY() * scale, bone.getScaleZ() * scale);
      });
   }

   /**
    * Puts the scene camera somewhere. Two paths, matching the original's two:
    *
    * <ul>
    *   <li>bone mode - the camera is mounted on the cam bone the animator authored, so the take moves
    *       the view. {@code girlCam} is added to the player's own pinned spot, exactly as
    *       {@code e_.java} does it; {@code boyCam} is read as a world position, as {@code l.java} does.
    *   <li>legacy mode - our measured {@code Head2} framing, which the tuning in
    *       {@code sexmod-camera.json} was built against, held still once the pose settles.
    * </ul>
    */
   private static void registerCameraAnchor(
      RenderPassInfo<LivingEntityRenderState> renderPassInfo,
      BoneSnapshots snapshots,
      LivingEntityRenderState renderState,
      String character,
      GirlSceneAction action,
      GirlScenePhase phase
   ) {
      GirlScenePhase configPhase = cameraConfigPhase(phase);
      GirlSceneCameraState.clearRig();
      if (SexmodConfig.isBoneCamDynamic()) {
         registerDynamicCameraAnchor(renderPassInfo, snapshots, renderState, character, action, configPhase);
      } else {
         registerLegacyCameraAnchor(renderPassInfo, renderState, character, action, phase, configPhase);
      }
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

   private static void registerDynamicCameraAnchor(
      RenderPassInfo<LivingEntityRenderState> renderPassInfo,
      BoneSnapshots snapshots,
      LivingEntityRenderState renderState,
      String character,
      GirlSceneAction action,
      GirlScenePhase configPhase
   ) {
      String headBone = snapshots.get("Head2").isPresent() ? "Head2" :
                       (snapshots.get(GirlSceneCamRig.BOY_CAM).isPresent() ? GirlSceneCamRig.BOY_CAM : GirlSceneCamRig.GIRL_CAM);
      renderPassInfo.addBonePositionListener(headBone, (worldPos, modelPos, localPos) -> {
         if (worldPos != null) {
            float yaw = renderState.getOrDefaultGeckolibData(GirlRenderData.CAMERA_YAW, 0.0F);
            Vec3 forward = horizontalForward(yaw);
            Vec3 right = horizontalRight(yaw);

            double distance = cameraBaseDistance(action) * GirlSceneCameraConfig.distanceMultiplier(character, action, configPhase);
            double vertical = cameraBaseVertical(action) + GirlSceneCameraConfig.verticalOffset(character, action, configPhase);
            double lateral = GirlSceneCameraConfig.lateralOffset(character, action, configPhase);

            Vec3 anchor = worldPos.add(forward.scale(distance)).add(right.scale(lateral)).add(0.0, vertical, 0.0);
            GirlSceneCameraState.clearRig();
            GirlSceneCameraState.update(anchor);
         }
      });
   }

   private static void registerLegacyCameraAnchor(
      RenderPassInfo<LivingEntityRenderState> renderPassInfo,
      LivingEntityRenderState renderState,
      String character,
      GirlSceneAction action,
      GirlScenePhase phase,
      GirlScenePhase configPhase
   ) {
      renderPassInfo.addBonePositionListener("Head2", (worldPos, modelPos, localPos) -> {
         if (worldPos != null) {
            float yaw = renderState.getOrDefaultGeckolibData(GirlRenderData.CAMERA_YAW, 0.0F);
            Vec3 forward = horizontalForward(yaw);
            Vec3 right = horizontalRight(yaw);

            double distance = cameraBaseDistance(action) * GirlSceneCameraConfig.distanceMultiplier(character, action, configPhase);
            double vertical = cameraBaseVertical(action) + GirlSceneCameraConfig.verticalOffset(character, action, configPhase);
            double lateral = GirlSceneCameraConfig.lateralOffset(character, action, configPhase);

            Vec3 anchor = worldPos.add(forward.scale(distance)).add(right.scale(lateral)).add(0.0, vertical, 0.0);
            if (holdsStillPose(phase)) {
               GirlSceneCameraState.update(List.of(String.valueOf(character), action, distance, vertical, lateral), anchor);
            } else {
               GirlSceneCameraState.update(anchor);
            }
         }
      });
   }

   /**
    * Which phases hold a pose that is meant to stand still.
    *
    * <p>The original's camera is a single teleport, so once the take is running the view must not move at
    * all; while she is still being paid or getting into position the anchor has to keep up with her.
    */
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
