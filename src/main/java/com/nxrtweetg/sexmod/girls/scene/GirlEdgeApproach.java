package com.nxrtweetg.sexmod.girls.scene;

import com.nxrtweetg.sexmod.girls.anim.GirlAnimationRole;

/**
 * How one girl waits at the bed before a sit-on-the-edge scene starts.
 *
 * <p>The original walks her to the spot the bed table picked, plays her sit-down take once and then
 * loops a holding take until the player closes the distance himself, exactly as the doggy scenes do
 * with {@code doggygoonbed} / {@code doggywait} (Ellie's {@code q() -> j() -> t() -> u()} chain). It
 * cannot be a property of the scene, because which takes exist is a property of the character: Bia
 * and Ellie ship the sit-down pair, Luna has only a hold, and Jenny neither - her scenes start where
 * the player stands.
 *
 * @param sitDown the take she plays once on arrival, or null when she ships none and only holds
 * @param hold the take she loops until he walks up
 */
public record GirlEdgeApproach(GirlAnimationRole sitDown, GirlAnimationRole hold) {
   /** Bia's and Ellie's pair: {@code sitdown} once, then {@code sitdownidle} (fp.SITDOWN/SITDOWNIDLE). */
   public static final GirlEdgeApproach SIT_DOWN = new GirlEdgeApproach(GirlAnimationRole.SIT_DOWN, GirlAnimationRole.SIT_DOWN_IDLE);
   /** Luna walks over and waits standing, since {@code animation.cat.wait} is all she has (fp.WAIT_CAT). */
   public static final GirlEdgeApproach HOLD_ONLY = new GirlEdgeApproach(null, GirlAnimationRole.WAIT);
}
