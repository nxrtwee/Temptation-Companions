package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.girls.scene.GirlScenePhase;
import com.geckolib.constant.dataticket.DataTicket;
import com.google.common.reflect.TypeToken;
import com.nxrtweetg.sexmod.girls.state.GirlPaymentItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public final class GirlRenderData {
   /** Which girl this is, so the render pass can read her own tuning rather than Jenny's. */
   public static final DataTicket<String> CHARACTER = DataTicket.create("girl_character", new TypeToken<String>() {});
   public static final DataTicket<GirlSceneAction> ACTIVE_ACTION = DataTicket.create("jenny_active_action", new TypeToken<GirlSceneAction>() {});
   public static final DataTicket<GirlScenePhase> SCENE_PHASE = DataTicket.create("jenny_scene_phase", new TypeToken<GirlScenePhase>() {});
   public static final DataTicket<Boolean> RENDER_PLAYER = DataTicket.create("jenny_render_player", new TypeToken<Boolean>() {});
   public static final DataTicket<Integer> MODEL_VARIANT = DataTicket.create("jenny_model_variant", new TypeToken<Integer>() {});
   public static final DataTicket<Integer> PAYMENT_AMOUNT = DataTicket.create("jenny_payment_amount", new TypeToken<Integer>() {});
   public static final DataTicket<GirlPaymentItem> PAYMENT_ITEM = DataTicket.create("jenny_payment_item", new TypeToken<GirlPaymentItem>() {});
   public static final DataTicket<Double> TUNE_FORWARD = DataTicket.create("jenny_tune_forward", new TypeToken<Double>() {});
   public static final DataTicket<Double> TUNE_SIDE = DataTicket.create("jenny_tune_side", new TypeToken<Double>() {});
   public static final DataTicket<Double> TUNE_VERTICAL = DataTicket.create("jenny_tune_vertical", new TypeToken<Double>() {});
   public static final DataTicket<Double> KOBOLD_SIDE_OFFSET = DataTicket.create("jenny_kobold_side_offset", new TypeToken<Double>() {});
   public static final DataTicket<Float> CAMERA_YAW = DataTicket.create("jenny_camera_yaw", new TypeToken<Float>() {});
   public static final DataTicket<Boolean> IS_OWNER = DataTicket.create("jenny_is_owner", new TypeToken<Boolean>() {});
   public static final DataTicket<Vec3> LIGHT_DIRECTION = DataTicket.create("jenny_light_direction", new TypeToken<Vec3>() {});
   public static final DataTicket<ItemStackRenderState> PAYMENT_STACK_STATE = DataTicket.create(
      "jenny_payment_stack_state", new TypeToken<ItemStackRenderState>() {}
   );
   public static final DataTicket<ItemStackRenderState> WEAPON_STACK_STATE = DataTicket.create(
      "jenny_weapon_stack_state", new TypeToken<ItemStackRenderState>() {}
   );
   /** Index into GirlArmorMaterial, or -1 when that slot is empty. */
   public static final DataTicket<Integer> HELMET_MATERIAL = DataTicket.create("jenny_helmet_material", new TypeToken<Integer>() {});
   public static final DataTicket<Integer> CHESTPLATE_MATERIAL = DataTicket.create("jenny_chestplate_material", new TypeToken<Integer>() {});
   public static final DataTicket<Integer> LEGGINGS_MATERIAL = DataTicket.create("jenny_leggings_material", new TypeToken<Integer>() {});
   public static final DataTicket<Integer> BOOTS_MATERIAL = DataTicket.create("jenny_boots_material", new TypeToken<Integer>() {});
   /**
    * Skin texture of the active player, resolved at render time from {@code AbstractClientPlayer.getSkin()}.
    * Used by {@code GirlSteveGeoLayer} so the partner wears the real player's skin.
    * Null when no active player is found — the layer falls back to the hardcoded steve.png.
    */
   public static final DataTicket<Identifier> PARTNER_SKIN = DataTicket.create("jenny_partner_skin", new TypeToken<Identifier>() {});
   /** True = partner uses Alex (SLIM) arm geometry; false = Steve (WIDE). */
   public static final DataTicket<Boolean> PARTNER_IS_SLIM = DataTicket.create("jenny_partner_is_slim", new TypeToken<Boolean>() {});

   private GirlRenderData() {
   }
}
