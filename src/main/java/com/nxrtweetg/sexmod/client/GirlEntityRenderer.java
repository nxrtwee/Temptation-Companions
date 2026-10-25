package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.girls.scene.GirlScenePhase;
import com.nxrtweetg.sexmod.config.SexmodConfig;
import software.bernie.geckolib.animatable.processing.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Map;
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
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Environment(EnvType.CLIENT)
public final class GirlEntityRenderer extends GeoEntityRenderer<SceneGirlEntity, GirlEntityRenderer.GirlGeoRenderState> {
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

   /** Bone whose world position feeds the scene camera this frame, set during bone adjustment. */
   private String cameraAnchorBone;
   private GirlSceneAction cameraAnchorAction;
   private GirlScenePhase cameraAnchorPhase;
   private String cameraAnchorCharacter;
   private GirlScenePhase cameraAnchorConfigPhase;
   // DIAGNOSTIC
   private static int diagRig, diagAnchor;

   /**
    * GeckoLib 5.2 stores per-render data through a {@code GeoRenderState} interface that it mixes into
    * vanilla render states at runtime; at compile time the interface is not visible on
    * {@code LivingEntityRenderState}, so the renderer carries its own subclass that implements it.
    */
   public static final class GirlGeoRenderState extends LivingEntityRenderState implements GeoRenderState {
      private final GeoRenderState.Impl geckolib$impl = new GeoRenderState.Impl();

      @Override
      public <D> void addGeckolibData(DataTicket<D> ticket, D value) {
         this.geckolib$impl.addGeckolibData(ticket, value);
      }

      @Override
      public boolean hasGeckolibData(DataTicket<?> ticket) {
         return this.geckolib$impl.hasGeckolibData(ticket);
      }

      @Override
      public <D> D getGeckolibData(DataTicket<D> ticket) {
         // GeckoLib 5.2's Impl throws when the ticket was never added; the 5.5 line returns
         // null. Callers throughout the mod treat a missing value as null, so bypass the throw.
         return (D)this.geckolib$impl.getDataMap().get(ticket);
      }

      @Override
      public Map<DataTicket<?>, Object> getDataMap() {
         return this.geckolib$impl.getDataMap();
      }

      /**
       * GeckoLib's EntityRenderState mixin injects its own (class-level, thus higher-priority)
       * implementation of this method into LivingEntityRenderState, reading the mixin's own
       * empty data map while our hasGeckolibData answers from ours - which made a present-but-
       * unread ticket come back as null. Route it through the same map everything else uses.
       */
      @Override
      public <D> D getOrDefaultGeckolibData(DataTicket<D> ticket, D fallback) {
         D value = this.getGeckolibData(ticket);
         return value != null ? value : fallback;
      }
   }

   public GirlEntityRenderer(Context context, GirlGeoModel model) {
      super(context, model);
      this.shadowRadius = 0.45F;
      model.setRigListener(this);
      this.addRenderLayer(new GirlPaymentGeoLayer(context, this));
      this.addRenderLayer(new GirlWeaponGeoLayer(context, this));
      this.addRenderLayer(new GirlSteveGeoLayer(this));
   }

   @Override
   public GirlGeoRenderState createRenderState() {
      return new GirlGeoRenderState();
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

   @Override
   public GirlGeoRenderState fillRenderState(SceneGirlEntity animatable, Void relatedObject, GirlGeoRenderState renderState, float partialTick) {
      super.fillRenderState(animatable, relatedObject, renderState, partialTick);
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

      if (animatable.hasActiveScene()) {
         // The layout yaw only holds once she has stopped: during approach/walk the body must follow
         // the travel rotation the server snaps, or she glides sideways facing where the scene
         // *will* be laid out.
         GirlScenePhase yawPhase = animatable.trackedScenePhase();
         boolean settled = yawPhase != GirlScenePhase.APPROACH && yawPhase != GirlScenePhase.WALKING
            && yawPhase != GirlScenePhase.POSITIONING;
         if (settled) {
            float sceneYaw = animatable.trackedSceneYaw();
            renderState.addGeckolibData(software.bernie.geckolib.constant.DataTickets.ENTITY_YAW, sceneYaw);
            renderState.addGeckolibData(software.bernie.geckolib.constant.DataTickets.ENTITY_BODY_YAW, sceneYaw);
         }
      }

      net.minecraft.world.item.ItemStack weapon = weaponFor(animatable);
      if (!weapon.isEmpty()) {
         net.minecraft.client.renderer.item.ItemStackRenderState weaponState = new net.minecraft.client.renderer.item.ItemStackRenderState();
         this.itemModelResolver.updateForTopItem(
            weaponState, weapon, net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, animatable.level(), animatable, 0
         );
         renderState.addGeckolibData(GirlRenderData.WEAPON_STACK_STATE, weaponState);
      }
      renderState.addGeckolibData(GirlRenderData.HELMET_MATERIAL, GirlArmorMaterial.indexFor(animatable.getItemBySlot(EquipmentSlot.HEAD)));
      renderState.addGeckolibData(GirlRenderData.CHESTPLATE_MATERIAL, GirlArmorMaterial.indexFor(animatable.getItemBySlot(EquipmentSlot.CHEST)));
      renderState.addGeckolibData(GirlRenderData.LEGGINGS_MATERIAL, GirlArmorMaterial.indexFor(animatable.getItemBySlot(EquipmentSlot.LEGS)));
      renderState.addGeckolibData(GirlRenderData.BOOTS_MATERIAL, GirlArmorMaterial.indexFor(animatable.getItemBySlot(EquipmentSlot.FEET)));
      // Скин партнёра резолвится один раз за сцену в tickClient (GirlSceneCameraController),
      // а не каждый кадр — это единственный способ избежать мерцания при загрузке текстуры.
      ResourceLocation partnerSkin = GirlSceneCameraController.getLockedPartnerSkin();
      if (partnerSkin != null) {
         renderState.addGeckolibData(GirlRenderData.PARTNER_SKIN, partnerSkin);
         renderState.addGeckolibData(GirlRenderData.PARTNER_IS_SLIM, GirlSceneCameraController.isLockedPartnerSlim());
      }

      if (animatable instanceof com.nxrtweetg.sexmod.girls.custom.CustomGirlEntity customGirl) {
         com.nxrtweetg.sexmod.girls.custom.TCEditorLoader.CustomCharacterData data = customGirl.getCustomData();
         if (data != null) {
            ResourceLocation skin = CustomSkinManager.getOrLoadSkin(data.id(), data.skinFileName());
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
         }
      }
      return renderState;
   }

   /**
    * Whether she moved during this tick, which is the only thing the original ever asked before picking
    * the walk take over the idle one: {@code |prevPosX - posX| + |prevPosZ - posZ| > 0} in Jenny's
    * movement controller, evaluated on the client.
    *
    * <p>GeckoLib fills {@link DataTickets#IS_MOVING} from {@code LivingEntity.walkAnimation.speed()},
    * which is a decaying average - it stays above the threshold for several ticks after she has actually
    * stopped, and that tail is what left her marching on the spot at the bed. Overwriting the ticket here
    * fixes it for every controller at once.
    */
   private static boolean isDisplacing(SceneGirlEntity animatable) {
      return Math.abs(animatable.xo - animatable.getX()) + Math.abs(animatable.zo - animatable.getZ()) > 0.005;
   }

   /**
    * Whether her floating name is drawn at all.
    *
    * <p>The original draws it unconditionally; vanilla's rule is the opposite. So we keep the original's
    * "always visible" half and drop the rest, with one deliberate difference: a scene hides the plate,
    * because the scene camera sits inside her head and would print her name across every shot.
    */
   @Override
   public boolean shouldShowName(SceneGirlEntity entity, double distanceToCameraSq) {
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
    * The floating name, nudged for characters whose head does not sit where the hitbox says it does.
    * The plate's position comes from the {@code NAME_TAG} attachment the extraction resolves, so the
    * nudge belongs on that.
    */
   @Override
   public void extractRenderState(SceneGirlEntity entity, GirlGeoRenderState renderState, float partialTick) {
      super.extractRenderState(entity, renderState, partialTick);
      boolean facefuck = entity.trackedActiveAction().filter(action -> action == GirlSceneAction.FACEFUCK).isPresent();
      double offset = entity.nameplateYOffset() + (facefuck ? FACEFUCK_NAMEPLATE_Y_OFFSET : 0.0);
      if (offset != 0.0 && renderState.nameTagAttachment != null) {
         renderState.nameTagAttachment = renderState.nameTagAttachment.add(0.0, offset, 0.0);
      }
   }

   /**
    * 1.21.x entity shading is the two-light directional diffuse the original fought as well, and
    * GeckoLib 5.2 still exposes the {@code createVerticesOfQuad} hook the 1.12.2 renderer used to
    * force every quad onto one flat normal - that is the whole trick, no custom render pipeline
    * needed on this version.
    */
   @Override
   public void createVerticesOfQuad(
      GirlGeoRenderState renderState, GeoQuad quad, Matrix4f poseMatrix, Vector3f normal,
      VertexConsumer buffer, int packedLight, int packedOverlay, int color
   ) {
      // Kobold and goblin palettes live on real directional shading (the per-bone tints assume the
      // two-light diffuse); flattening every normal to +Y floods them into a single shade.
      String flatLitCharacter = renderState.getOrDefaultGeckolibData(GirlRenderData.CHARACTER, "jenny");
      boolean procedurallyTinted = "kobold".equals(flatLitCharacter) || "goblin".equals(flatLitCharacter);
      if (!ShaderCompat.isShadersActive() && SexmodConfig.isFlatLitRenderingEnabled() && !procedurallyTinted) {
         normal = new Vector3f(0.0F, 1.0F, 0.0F);
      }
      super.createVerticesOfQuad(renderState, quad, poseMatrix, normal, buffer, packedLight, packedOverlay, color);
   }

   /**
    * The per-frame bone pass: everything the 1.12.2 renderer did to the rig after the animation ran.
    *
    * <p>GeckoLib 5.2 ticks animations inside {@code actuallyRender}, which runs AFTER
    * {@code adjustPositionForRender}, and writes every animated bone absolutely (animation value +
    * initial snapshot) - so transform edits made in adjustPositionForRender are wiped before the
    * model is drawn. {@link GirlGeoModel#setCustomAnimations} is GeckoLib's hook at the end of that
    * same animation pass, so the edits below land on the final pose and survive into the render.
    * Called from the model; the renderer carries the camera-anchor fields the recursion reads later.
    */
   public void applyRigPass(BakedGeoModel model, AnimationState<SceneGirlEntity> animationState) {
      GirlGeoRenderState renderState = (GirlGeoRenderState)animationState.renderState();

      GirlSceneAction action = renderState.getGeckolibData(GirlRenderData.ACTIVE_ACTION);
      GirlScenePhase phase = renderState.getOrDefaultGeckolibData(GirlRenderData.SCENE_PHASE, GirlScenePhase.IDLE);
      boolean renderPlayer = renderState.getOrDefaultGeckolibData(GirlRenderData.RENDER_PLAYER, false);
      String character = renderState.getOrDefaultGeckolibData(GirlRenderData.CHARACTER, "jenny");

      // DIAGNOSTIC: trace rig state and animation ticks on GeckoLib 5.2
      if (diagRig < 8 || diagRig % 600 == 0) {
         diagRig++;
         Object animTicks = renderState.getGeckolibData(software.bernie.geckolib.constant.DataTickets.ANIMATION_TICKS);
         com.nxrtweetg.sexmod.SexmodMod.LOGGER.info(
            "[diag] rigPass #{}: char={} action={} phase={} renderPlayer={} amount={} animTicks={} model={}",
            diagRig, character, action, phase, renderPlayer,
            renderState.getOrDefaultGeckolibData(GirlRenderData.PAYMENT_AMOUNT, -1),
            animTicks, model == null ? "null" : "ok"
         );
      }

      applyPlayerBoneVisibility(model, character);
      if (character.startsWith("custom")) {
         applyCustomCompanionRig(model, renderState);
      }
      applyPaymentBoneVisibility(model, renderState);
      applyArmorBoneVisibility(model, renderState);
      applySceneBodyScale(model, character, action);
      applyGirlScale(model, character, action, phase);
      float allieScale = renderState.getOrDefaultGeckolibData(GirlRenderData.ALLIE_DISMISS_SCALE, 1.0F);
      if (allieScale < 1.0F) {
         bodyRoot(model).ifPresent(bone -> {
            bone.setScaleX(bone.getScaleX() * allieScale);
            bone.setScaleY(bone.getScaleY() * allieScale);
            bone.setScaleZ(bone.getScaleZ() * allieScale);
         });
      }
      // Arms for partner are hidden in GirlSteveGeoLayer during partner pass

      if (action == GirlSceneAction.FACEFUCK && (phase == GirlScenePhase.ACTIVE)) {
         applyFacefuckFacePose(model);
      }

      boolean blushActions = action == GirlSceneAction.KOBOLD_BLOWJOB || action == GirlSceneAction.MATING_PRESS;
      boolean blushActive = blushActions && (phase == GirlScenePhase.ACTIVE || phase == GirlScenePhase.CLIMAX);
      bone(model, "blush").ifPresent(b -> b.setPosZ(blushActive ? -1.0F : 0.0F));

      boolean actionUsesHeart = action == GirlSceneAction.BLOWJOB || action == GirlSceneAction.SITTING
            || action == GirlSceneAction.SITTING_ALT || action == GirlSceneAction.COWGIRL;
      if (!actionUsesHeart) {
         setBoneHidden(model, "heart", true);
         setBoneHidden(model, "heart2", true);
         setBoneHidden(model, "blush2", true);
      }

      applyBodySceneTune(model, renderState, action);
      applyHelperTransform(model, renderState, character, action, phase);
      applyHeadRotation(model, renderState, phase);
      applySlimeRig(model, character, action);
      applyBeeRig(model, renderState, character);
      applyGoblinRig(model, renderState, character);
      applyGalathRig(model, renderState, character, action);
      applyManglelieRig(model, renderState, character, action);

      this.cameraAnchorBone = null;
      if (renderPlayer && action != null) {
         registerCameraAnchor(model, character, action, phase);
      }
   }

   /**
    * The partner is never drawn by the main pass: his bones sample a different atlas, so
    * {@link GirlSteveGeoLayer} draws that subtree separately. Hiding the whole subtree here is what
    * keeps him out of the girl's own texture pass.
    */
   private static void applyPlayerBoneVisibility(BakedGeoModel model, String character) {
      // steve subtree is the partner rig (including partner genitals), rendered exclusively in GirlSteveGeoLayer during scenes
      bone(model, "steve").ifPresent(b -> {
         b.setHidden(true);
         b.setChildrenHidden(true);
      });
   }

   private static void applyCustomCompanionRig(BakedGeoModel model, GirlGeoRenderState renderState) {
      // 1. Arm format: Alex (Slim) vs Steve (Classic)
      boolean isSlim = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_IS_SLIM, false);
      for (String boneName : CUSTOM_COMPANION_ALEX_ARMS) {
         setBoneHidden(model, boneName, !isSlim);
      }
      for (String boneName : CUSTOM_COMPANION_STEVE_ARMS) {
         setBoneHidden(model, boneName, isSlim);
      }

      // 2. Cosmetics: Jenny's Boobs & Booty
      boolean hasBreasts = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_HAS_BREASTS, false);
      float breastScale = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_BREAST_SCALE, 1.0F);
      setBoneHidden(model, "boobs", !hasBreasts);
      if (hasBreasts && breastScale != 1.0F) {
         bone(model, "boobs").ifPresent(b -> scaleBone(b, breastScale));
      }

      boolean hasBooty = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_HAS_BOOTY, false);
      float bootyScale = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_BOOTY_SCALE, 1.0F);
      setBoneHidden(model, "booty", !hasBooty);
      if (hasBooty && bootyScale != 1.0F) {
         bone(model, "booty").ifPresent(b -> scaleBone(b, bootyScale));
      }

      // 3. Stretches / Dimensions
      float hx = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_HEAD_SCALE_X, 1.0F);
      float hy = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_HEAD_SCALE_Y, 1.0F);
      float hz = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_HEAD_SCALE_Z, 1.0F);
      if (hx != 1.0F || hy != 1.0F || hz != 1.0F) {
         mulScale(model, "head", hx, hy, hz);
         mulScale(model, "Head2", hx, hy, hz);
      }

      float tx = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_TORSO_SCALE_X, 1.0F);
      float ty = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_TORSO_SCALE_Y, 1.0F);
      float tz = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_TORSO_SCALE_Z, 1.0F);
      if (tx != 1.0F || ty != 1.0F || tz != 1.0F) {
         mulScale(model, "torso", tx, ty, tz);
         mulScale(model, "Torso2", tx, ty, tz);
      }

      float ax = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_ARM_SCALE_X, 1.0F);
      float ay = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_ARM_SCALE_Y, 1.0F);
      float az = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_ARM_SCALE_Z, 1.0F);
      if (ax != 1.0F || ay != 1.0F || az != 1.0F) {
         mulScale(model, "armR", ax, ay, az);
         mulScale(model, "armL", ax, ay, az);
         mulScale(model, "LeftArm", ax, ay, az);
         mulScale(model, "RightArm", ax, ay, az);
      }

      float lx = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_LEG_SCALE_X, 1.0F);
      float ly = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_LEG_SCALE_Y, 1.0F);
      float lz = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_LEG_SCALE_Z, 1.0F);
      if (lx != 1.0F || ly != 1.0F || lz != 1.0F) {
         mulScale(model, "legR", lx, ly, lz);
         mulScale(model, "legL", lx, ly, lz);
         mulScale(model, "LeftLeg", lx, ly, lz);
         mulScale(model, "RightLeg", lx, ly, lz);
      }

      // 4. Overall scaling
      float scale = renderState.getOrDefaultGeckolibData(GirlRenderData.CUSTOM_SCALE, 1.0F);
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
      // SLIM = Alex: hide Steve variants, show Alex variants
      // WIDE = Steve: hide Alex variants, show Steve variants
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

   /**
    * Rotates the character's head bone (pitch & yaw) when looking around during idle/wandering.
    */
   private static void applyHeadRotation(BakedGeoModel model, GirlGeoRenderState renderState, GirlScenePhase phase) {
      if (phase == GirlScenePhase.IDLE || phase == GirlScenePhase.WALKING || phase == GirlScenePhase.WAITING) {
         float headPitch = (float) Math.toRadians(Mth.clamp(renderState.xRot, -85.0F, 85.0F));
         float headYaw = (float) Math.toRadians(Mth.clamp(-renderState.yRot, -85.0F, 85.0F));
         for (String name : new String[] {"head", "Head2"}) {
            bone(model, name).ifPresent(bone -> {
               bone.setRotX(bone.getRotX() + headPitch);
               bone.setRotY(bone.getRotY() + headYaw);
            });
         }
      }
   }

   /**
    * Slime's rig needs two things the shared code cannot express, both from the original's
    * SlimeModel: the blob she sits on only exists during the doggy scene, and her hat is a top-level
    * bone that has to be driven off the head by hand.
    */
   private static void applySlimeRig(BakedGeoModel model, String character, GirlSceneAction action) {
      if (!"slime".equals(character)) {
         return;
      }

      // SlimeModel.actionsWithSlime -- the blob is part of the doggy scene and nothing else
      boolean showBlob = action == GirlSceneAction.DOGGY;
      for (String name : SLIME_BLOB_BONES) {
         bone(model, name).ifPresent(bone -> {
            bone.setHidden(!showBlob);
            bone.setChildrenHidden(!showBlob);
         });
      }

      // SlimeModel.a(new String[]{"head"}, "hat") -- "hat" hangs off "dress", not off the head, so
      // the animation never moves it. Copy the head's pose onto it every frame.
      Optional<GeoBone> head = bone(model, "head");
      head.ifPresent(h -> bone(model, "hat").ifPresent(hat -> {
         hat.updateRotation(h.getRotX(), h.getRotY(), h.getRotZ());
         hat.setPosX(h.getPosX());
         hat.setPosY(h.getPosY());
         hat.setPosZ(h.getPosZ());
      }));
   }

   /**
    * The bee's satchel bone. The original's BeeModel hid "chest" unless the take it was playing had
    * "chest" in its name, which amounted to "she is carrying one".
    */
   private static void applyBeeRig(BakedGeoModel model, GirlGeoRenderState renderState, String character) {
      if (!"bee".equals(character)) {
         return;
      }

      boolean hasChest = renderState.getOrDefaultGeckolibData(GirlRenderData.HAS_CHEST, false);
      bone(model, "chest").ifPresent(bone -> {
         bone.setHidden(!hasChest);
         bone.setChildrenHidden(!hasChest);
      });
   }

   private static void applyGoblinRig(BakedGeoModel model, GirlGeoRenderState renderState, String character) {
      if (!"goblin".equals(character)) {
         return;
      }
      int crown = renderState.getOrDefaultGeckolibData(GirlRenderData.GOBLIN_CROWN, 0);
      bone(model, "crown").ifPresent(bone -> {
         bone.setHidden(crown == 0);
         bone.setChildrenHidden(crown == 0);
      });

      int hairStyle = Math.floorMod(renderState.getOrDefaultGeckolibData(GirlRenderData.GOBLIN_HAIR_STYLE, 0), 6);
      for (int i = 0; i < 6; i++) {
         boolean hide = i != hairStyle;
         bone(model, "hair" + i).ifPresent(bone -> {
            bone.setHidden(hide);
            bone.setChildrenHidden(hide);
         });
      }
   }

   private static void applyGalathRig(BakedGeoModel model, GirlGeoRenderState renderState, String character, GirlSceneAction action) {
      if (!"galath".equals(character)) {
         return;
      }
      // Hide floating coin and summon pentagram circle during regular rendering
      for (String name : new String[] {"coin", "pentagram", "futa", "futaCock", "futaBallLL", "futaBallLR"}) {
         bone(model, name).ifPresent(bone -> {
            bone.setHidden(true);
            bone.setChildrenHidden(true);
         });
      }

      int variant = renderState.getOrDefaultGeckolibData(GirlRenderData.MODEL_VARIANT, 1);
      boolean stripped = (variant == 0);
      setBoneHidden(model, "braBoobL", stripped);
      setBoneHidden(model, "braBoobR", stripped);
      setBoneHidden(model, "slip", stripped);
      setBoneHidden(model, "nippleL", !stripped);
      setBoneHidden(model, "nippleR", !stripped);
   }

   private static void applyManglelieRig(BakedGeoModel model, GirlGeoRenderState renderState, String character, GirlSceneAction action) {
      if (!"manglelie".equals(character)) {
         return;
      }
      int variant = renderState.getOrDefaultGeckolibData(GirlRenderData.MODEL_VARIANT, 1);
      boolean hasSkirt = (variant != 0);

      for (String name : new String[] {"skirt", "sideLSkirt", "sideRSkirt", "cheekSkirtL", "cheekSkirtR"}) {
         setBoneHidden(model, name, !hasSkirt);
      }
      for (String name : new String[] {"sideLNoSkirt", "sideRNoSkirt", "cheekLBelowSkirt", "cheekRBelowSkirt"}) {
         setBoneHidden(model, name, hasSkirt);
      }
   }

   /**
    * Kobold and Goblin bodies are procedurally tinted per bone, which the shared pipeline has no hook
    * for on 5.2 - so their cubes go through a colour-forcing {@link VertexConsumer} wrapper here, and
    * their bones keep rendering through the ordinary recursive pass.
    */
   @Override
   public void renderRecursively(
      GirlGeoRenderState renderState, PoseStack poseStack, GeoBone bone,
      RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
      boolean isReRender, int packedLight, int packedOverlay, int renderColor
   ) {
      String character = renderState.getOrDefaultGeckolibData(GirlRenderData.CHARACTER, "jenny");
      if ("kobold".equals(character) || "goblin".equals(character)) {
         int boneColor = resolveCustomBoneColor(character, bone.getName(), renderState, renderColor);
         if (boneColor != renderColor) {
            buffer = new BoneColorVertexConsumer(buffer, boneColor);
         }
      }

      if (this.cameraAnchorBone != null && this.cameraAnchorBone.equals(bone.getName())) {
         poseStack.pushPose();
         RenderUtil.prepMatrixForBone(poseStack, bone);
         captureCameraAnchor(renderState, poseStack, bone);
         poseStack.popPose();
      }

      super.renderRecursively(renderState, poseStack, bone, renderType, bufferSource, buffer, isReRender, packedLight, packedOverlay, renderColor);
   }

   /**
    * Evaluates the anchor bone's world position from the current camera-relative matrix.
    * Called from {@link #renderRecursively} for main body anchors and {@link GirlSteveGeoLayer}
    * for partner subtree anchors (Head2/boyCam).
    */
   void captureCameraAnchor(GirlGeoRenderState renderState, PoseStack poseStack, GeoBone bone) {
      if (this.cameraAnchorBone == null || !this.cameraAnchorBone.equals(bone.getName())) {
         return;
      }

      Matrix4f pose = poseStack.last().pose();
      Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
      Vector4f localPivot = new Vector4f(bone.getPivotX() / 16.0F, bone.getPivotY() / 16.0F, bone.getPivotZ() / 16.0F, 1.0F);
      Vector4f worldPivot = pose.transform(localPivot, new Vector4f());
      Vec3 worldPos = new Vec3(
         camPos.x + worldPivot.x(), camPos.y + worldPivot.y(), camPos.z + worldPivot.z()
      );

      float yaw = renderState.getOrDefaultGeckolibData(GirlRenderData.CAMERA_YAW, 0.0F);
      Vec3 forward = horizontalForward(yaw);
      Vec3 right = horizontalRight(yaw);

      GirlSceneAction action = this.cameraAnchorAction;
      String character = this.cameraAnchorCharacter;
      GirlScenePhase configPhase = this.cameraAnchorConfigPhase;

      double distance = cameraBaseDistance(action) * GirlSceneCameraConfig.distanceMultiplier(character, action, configPhase);
      double vertical = cameraBaseVertical(action) + GirlSceneCameraConfig.verticalOffset(character, action, configPhase);
      double lateral = GirlSceneCameraConfig.lateralOffset(character, action, configPhase);

      Vec3 anchor = worldPos.add(forward.scale(distance)).add(right.scale(lateral)).add(0.0, vertical, 0.0);
      // DIAGNOSTIC: trace anchor capture
      if (diagAnchor < 12) {
         diagAnchor++;
         com.nxrtweetg.sexmod.SexmodMod.LOGGER.info(
            "[diag] anchor #{}: bone={} worldPos={} anchor={} entity=({},{},{})",
            diagAnchor, bone.getName(), worldPos, anchor, renderState.x, renderState.y, renderState.z
         );
      }
      if (!SexmodConfig.isBoneCamDynamic() && holdsStillPose(this.cameraAnchorPhase)) {
         GirlSceneCameraState.update(List.of(String.valueOf(character), action, distance, vertical, lateral), anchor);
      } else {
         GirlSceneCameraState.update(anchor);
      }
   }

   private static int resolveCustomBoneColor(
      String character, String boneName, GirlGeoRenderState renderState, int defaultColor
   ) {
      if ("kobold".equals(character)) {
         return resolveKoboldBoneColor(boneName, renderState, defaultColor);
      }
      if ("goblin".equals(character)) {
         return resolveGoblinBoneColor(boneName, renderState, defaultColor);
      }
      return defaultColor;
   }

   private static int resolveKoboldBoneColor(String boneName, GirlGeoRenderState renderState, int defaultColor) {
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

   private static int resolveGoblinBoneColor(String boneName, GirlGeoRenderState renderState, int defaultColor) {
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

   private static void applyPaymentBoneVisibility(BakedGeoModel model, GirlGeoRenderState renderState) {
      int amount = renderState.getOrDefaultGeckolibData(GirlRenderData.PAYMENT_AMOUNT, 0);
      bone(model, "items").ifPresent(bone -> {
         bone.setHidden(amount <= 0);
         bone.setChildrenHidden(amount <= 0);
      });

      // the baked per-currency cubes are superseded by GirlPaymentGeoLayer, which draws the real item
      for (var paymentItem : com.nxrtweetg.sexmod.girls.state.GirlPaymentItem.values()) {
         String prefix = paymentItem.name().toLowerCase();

         for (int i = 1; i <= 3; i++) {
            setBoneHidden(model, prefix + i, true);
         }
      }
   }

   /**
    * Shows one material's worth of armour bones per slot and hides the other three, then hides the
    * bare body underneath. The original paired each armour bone group with the flesh it covers
    * (GirlModel.c/f/h/e); the flesh bones it listed that our model does not have are simply skipped.
    */
   private static void applyArmorBoneVisibility(BakedGeoModel model, GirlGeoRenderState renderState) {
      applyArmorSlot(model, renderState, GirlRenderData.HELMET_MATERIAL, HELMET_BONES, HELMET_FLESH);
      applyArmorSlot(model, renderState, GirlRenderData.CHESTPLATE_MATERIAL, CHESTPLATE_BONES, CHESTPLATE_FLESH);
      applyArmorSlot(model, renderState, GirlRenderData.LEGGINGS_MATERIAL, LEGGINGS_BONES, LEGGINGS_FLESH);
      applyArmorSlot(model, renderState, GirlRenderData.BOOTS_MATERIAL, BOOTS_BONES, BOOTS_FLESH);
   }

   private static void applyArmorSlot(
      BakedGeoModel model,
      GirlGeoRenderState renderState,
      DataTicket<Integer> ticket,
      String[] armorBones,
      String[] fleshBones
   ) {
      int materialIndex = renderState.getOrDefaultGeckolibData(ticket, -1);
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

         // Ellie and Bia also keep the un-suffixed container, whose children are armour pieces the
         // material duplication left behind (Jenny has no such bone). Without this they draw the
         // diamond look no matter what is equipped.
         boolean slotEmpty = materialIndex < 0;
         setBoneHidden(model, boneName, slotEmpty);
      }

      // The nude model has no armour bones at all, so hiding the flesh there would just delete her body.
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

   /**
    * Her overall size, on her root bone only. The partner lives under {@code steve} and is scaled
    * separately in {@link #applyHelperTransform}, so the two can be sized independently. Multiplied
    * into whatever the animation set, since nearly every animation drives this bone.
    */
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
         // `items` is a root bone (not under `body`), so body-scale doesn't propagate to it.
         // Manually scale its translation so payment items land at the correct world height
         // for each character (Bia×0.84, Ellie×1.17, etc.).
         bone(model, "items").ifPresent(bone -> {
            software.bernie.geckolib.animation.state.BoneSnapshot initial = bone.getInitialSnapshot();
            float initialX = initial != null ? initial.getOffsetX() : 0.0F;
            float initialY = initial != null ? initial.getOffsetY() : 0.0F;
            float initialZ = initial != null ? initial.getOffsetZ() : 0.0F;
            bone.setPosX(initialX * scale);
            bone.setPosY(initialY * scale);
            bone.setPosZ(initialZ * scale);
         });
      }
   }

   /**
    * The bone everything of hers hangs off. Slime's rig calls it {@code dd} instead of {@code body},
    * and the original picked between the two the same way (GirlModel.java:224).
    */
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

   private static void applyBodySceneTune(BakedGeoModel model, GirlGeoRenderState renderState, GirlSceneAction action) {
      if (action != null) {
         double side = renderState.getOrDefaultGeckolibData(GirlRenderData.TUNE_SIDE, 0.0);
         if (action == GirlSceneAction.KOBOLD_BLOWJOB) {
            side += renderState.getOrDefaultGeckolibData(GirlRenderData.KOBOLD_SIDE_OFFSET, 0.0);
         }

         double vertical = renderState.getOrDefaultGeckolibData(GirlRenderData.TUNE_VERTICAL, 0.0);
         double forward = renderState.getOrDefaultGeckolibData(GirlRenderData.TUNE_FORWARD, 0.0);
         float sideOffset = (float)side;
         bodyRoot(model).ifPresent(bone -> {
            bone.setPosX(bone.getPosX() + sideOffset);
            bone.setPosY(bone.getPosY() + (float)vertical);
            bone.setPosZ(bone.getPosZ() + (float)forward);
         });
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
      BakedGeoModel model, GirlGeoRenderState renderState, String character, GirlSceneAction action, GirlScenePhase phase
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

      bone(model, "steve").ifPresent(bone -> {
         bone.setPosX(bone.getPosX() + side);
         bone.setPosY(bone.getPosY() + vertical);
         bone.setPosZ(bone.getPosZ() + forward);
         bone.setScaleX(bone.getScaleX() * scale);
         bone.setScaleY(bone.getScaleY() * scale);
         bone.setScaleZ(bone.getScaleZ() * scale);
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

   /** The weapon she is actually fighting with, mirroring the original's {@code GirlRenderer.a} gate. */
   private static net.minecraft.world.item.ItemStack weaponFor(SceneGirlEntity girl) {
      return switch (girl.resolvedActivity()) {
         case COMBAT_MELEE -> girl.getItemBySlot(EquipmentSlot.MAINHAND);
         case COMBAT_BOW -> girl.getItemBySlot(EquipmentSlot.OFFHAND);
         default -> net.minecraft.world.item.ItemStack.EMPTY;
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
