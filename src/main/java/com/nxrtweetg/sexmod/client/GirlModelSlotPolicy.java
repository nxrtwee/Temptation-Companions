package com.nxrtweetg.sexmod.client;

import com.geckolib.renderer.base.GeoRenderState;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * How a character turns her synced model variant into one of her geo slots.
 *
 * <p>The original keeps an ordered array per character - slot 0 is the base or nude body, slot 1 the
 * armored one and slot 2, where it exists, the dressed one - and indexes it with the synced outfit
 * int. Seven of its eleven models ship two slots and take that index straight ({@link #DIRECT}), and
 * the four that ship three override the lookup:
 *
 * <ul>
 *   <li>the slime and Allie ({@code cr}/{@code c2}) read variant 1 as the last slot, so their armored
 *       middle slot is reachable only by their morph, which indexes directly. Our port registers them
 *       with two slots instead: their everyday body is the armored rig, because that is the one that
 *       carries the armour, hand and prop bones the rest of our render path needs.</li>
 *   <li>Galath and Manglelie ({@code cb}/{@code ce}) force the shared third slot
 *       {@code galath_con_mang} from her scene state rather than from the variant. That is what this
 *       hook takes a render state for, and it lands with parity item 18.</li>
 * </ul>
 */
@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface GirlModelSlotPolicy {
   /**
    * {@code cv.a(em)}: the variant <i>is</i> the slot. The original prints a complaint and falls back
    * to slot 0 when the index is past the end - and crashes on the exact last index, because it tests
    * {@code >} rather than {@code >=}. Ours clamps both ends instead.
    */
   GirlModelSlotPolicy DIRECT = (renderState, variant, slotCount) -> variant >= 0 && variant < slotCount ? variant : 0;

   /**
    * Selects slot 1 (armored) only when armor is equipped and variant != 0.
    * When unarmored or stripped, falls back to slot 0 (base nude/unarmored body).
    */
   GirlModelSlotPolicy ARMORED_WHEN_EQUIPPED = (renderState, variant, slotCount) -> {
      if (variant == 0 || slotCount < 2) {
         return 0;
      }
      boolean hasArmor = renderState.getOrDefaultGeckolibData(GirlRenderData.HELMET_MATERIAL, -1) >= 0
         || renderState.getOrDefaultGeckolibData(GirlRenderData.CHESTPLATE_MATERIAL, -1) >= 0
         || renderState.getOrDefaultGeckolibData(GirlRenderData.LEGGINGS_MATERIAL, -1) >= 0
         || renderState.getOrDefaultGeckolibData(GirlRenderData.BOOTS_MATERIAL, -1) >= 0;
      return hasArmor ? 1 : 0;
   };

   GirlModelSlotPolicy MANGLELIE_POLICY = (renderState, variant, slotCount) -> 0;

   /**
    * @param renderState the state the model is being drawn from, for the policies that read more than
    *                    the variant
    * @param variant     the synced model variant, the original's {@code em.D}
    * @param slotCount   how many geo slots this character registered
    * @return the slot to draw; out-of-range answers are clamped by the caller
    */
   int resolve(GeoRenderState renderState, int variant, int slotCount);
}
