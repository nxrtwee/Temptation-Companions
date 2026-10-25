package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.girls.scene.GirlScenePhase;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import com.nxrtweetg.sexmod.girls.state.GirlPaymentItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public final class GirlRenderData {
   /** Which girl this is, so the render pass can read her own tuning rather than Jenny's. */
   public static final DataTicket<String> CHARACTER = DataTicket.create("girl_character", String.class);
   public static final DataTicket<GirlSceneAction> ACTIVE_ACTION = DataTicket.create("jenny_active_action", GirlSceneAction.class);
   public static final DataTicket<GirlScenePhase> SCENE_PHASE = DataTicket.create("jenny_scene_phase", GirlScenePhase.class);
   public static final DataTicket<Boolean> RENDER_PLAYER = DataTicket.create("jenny_render_player", Boolean.class);
   public static final DataTicket<Integer> MODEL_VARIANT = DataTicket.create("jenny_model_variant", Integer.class);
   public static final DataTicket<Integer> PAYMENT_AMOUNT = DataTicket.create("jenny_payment_amount", Integer.class);
   public static final DataTicket<GirlPaymentItem> PAYMENT_ITEM = DataTicket.create("jenny_payment_item", GirlPaymentItem.class);
   public static final DataTicket<Double> TUNE_FORWARD = DataTicket.create("jenny_tune_forward", Double.class);
   public static final DataTicket<Double> TUNE_SIDE = DataTicket.create("jenny_tune_side", Double.class);
   public static final DataTicket<Double> TUNE_VERTICAL = DataTicket.create("jenny_tune_vertical", Double.class);
   public static final DataTicket<Double> KOBOLD_SIDE_OFFSET = DataTicket.create("jenny_kobold_side_offset", Double.class);
   /**
    * Whether the bee is carrying her satchel. The original showed that bone whenever the take she was
    * playing had "chest" in its name; the flag is the same thing said in a way the bone pass can read.
    */
   public static final DataTicket<Boolean> HAS_CHEST = DataTicket.create("girl_has_chest", Boolean.class);
   public static final DataTicket<Float> CAMERA_YAW = DataTicket.create("jenny_camera_yaw", Float.class);
   public static final DataTicket<Boolean> IS_OWNER = DataTicket.create("jenny_is_owner", Boolean.class);
   public static final DataTicket<ItemStackRenderState> PAYMENT_STACK_STATE = DataTicket.create(
      "jenny_payment_stack_state", ItemStackRenderState.class
   );
   public static final DataTicket<ItemStackRenderState> WEAPON_STACK_STATE = DataTicket.create(
      "jenny_weapon_stack_state", ItemStackRenderState.class
   );
   /** Index into GirlArmorMaterial, or -1 when that slot is empty. */
   public static final DataTicket<Integer> HELMET_MATERIAL = DataTicket.create("jenny_helmet_material", Integer.class);
   public static final DataTicket<Integer> CHESTPLATE_MATERIAL = DataTicket.create("jenny_chestplate_material", Integer.class);
   public static final DataTicket<Integer> LEGGINGS_MATERIAL = DataTicket.create("jenny_leggings_material", Integer.class);
   public static final DataTicket<Integer> BOOTS_MATERIAL = DataTicket.create("jenny_boots_material", Integer.class);
   /**
    * Skin texture of the active player, resolved at render time from {@code AbstractClientPlayer.getSkin()}.
    * Used by {@code GirlSteveGeoLayer} so the partner wears the real player's skin.
    * Null when no active player is found — the layer falls back to the hardcoded steve.png.
    */
   public static final DataTicket<ResourceLocation> PARTNER_SKIN = DataTicket.create("jenny_partner_skin", ResourceLocation.class);
   /** True = partner uses Alex (SLIM) arm geometry; false = Steve (WIDE). */
   public static final DataTicket<Boolean> PARTNER_IS_SLIM = DataTicket.create("jenny_partner_is_slim", Boolean.class);
   public static final DataTicket<Integer> KOBOLD_TRIBE_COLOR = DataTicket.create("girl_kobold_tribe_color", Integer.class);
   public static final DataTicket<Integer> KOBOLD_EYE_COLOR = DataTicket.create("girl_kobold_eye_color", Integer.class);
   public static final DataTicket<Integer> GOBLIN_SKIN_COLOR = DataTicket.create("girl_goblin_skin_color", Integer.class);
   public static final DataTicket<Integer> GOBLIN_HAIR_COLOR = DataTicket.create("girl_goblin_hair_color", Integer.class);
   public static final DataTicket<Integer> GOBLIN_EYE_COLOR = DataTicket.create("girl_goblin_eye_color", Integer.class);
   public static final DataTicket<Integer> GOBLIN_HAIR_STYLE = DataTicket.create("girl_goblin_hair_style", Integer.class);
   public static final DataTicket<Integer> GOBLIN_CROWN = DataTicket.create("girl_goblin_crown", Integer.class);
   public static final DataTicket<Float> ALLIE_DISMISS_SCALE = DataTicket.create("girl_allie_dismiss_scale", Float.class);

   public static final DataTicket<ResourceLocation> CUSTOM_SKIN = DataTicket.create("girl_custom_skin", ResourceLocation.class);
   public static final DataTicket<Boolean> CUSTOM_IS_SLIM = DataTicket.create("girl_custom_is_slim", Boolean.class);
   public static final DataTicket<Float> CUSTOM_SCALE = DataTicket.create("girl_custom_scale", Float.class);
   public static final DataTicket<Boolean> CUSTOM_HAS_BREASTS = DataTicket.create("girl_custom_has_breasts", Boolean.class);
   public static final DataTicket<Float> CUSTOM_BREAST_SCALE = DataTicket.create("girl_custom_breast_scale", Float.class);
   public static final DataTicket<Boolean> CUSTOM_HAS_BOOTY = DataTicket.create("girl_custom_has_booty", Boolean.class);
   public static final DataTicket<Float> CUSTOM_BOOTY_SCALE = DataTicket.create("girl_custom_booty_scale", Float.class);

   public static final DataTicket<Float> CUSTOM_HEAD_SCALE_X = DataTicket.create("girl_custom_head_scale_x", Float.class);
   public static final DataTicket<Float> CUSTOM_HEAD_SCALE_Y = DataTicket.create("girl_custom_head_scale_y", Float.class);
   public static final DataTicket<Float> CUSTOM_HEAD_SCALE_Z = DataTicket.create("girl_custom_head_scale_z", Float.class);

   public static final DataTicket<Float> CUSTOM_TORSO_SCALE_X = DataTicket.create("girl_custom_torso_scale_x", Float.class);
   public static final DataTicket<Float> CUSTOM_TORSO_SCALE_Y = DataTicket.create("girl_custom_torso_scale_y", Float.class);
   public static final DataTicket<Float> CUSTOM_TORSO_SCALE_Z = DataTicket.create("girl_custom_torso_scale_z", Float.class);

   public static final DataTicket<Float> CUSTOM_ARM_SCALE_X = DataTicket.create("girl_custom_arm_scale_x", Float.class);
   public static final DataTicket<Float> CUSTOM_ARM_SCALE_Y = DataTicket.create("girl_custom_arm_scale_y", Float.class);
   public static final DataTicket<Float> CUSTOM_ARM_SCALE_Z = DataTicket.create("girl_custom_arm_scale_z", Float.class);

   public static final DataTicket<Float> CUSTOM_LEG_SCALE_X = DataTicket.create("girl_custom_leg_scale_x", Float.class);
   public static final DataTicket<Float> CUSTOM_LEG_SCALE_Y = DataTicket.create("girl_custom_leg_scale_y", Float.class);
   public static final DataTicket<Float> CUSTOM_LEG_SCALE_Z = DataTicket.create("girl_custom_leg_scale_z", Float.class);

   private GirlRenderData() {
   }
}
