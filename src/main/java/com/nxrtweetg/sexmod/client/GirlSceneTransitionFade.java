package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.girls.scene.GirlScenePhase;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Плавное затемнение экрана при переходах между фазами сцены, скрывающее резкие скачки
 * координат и параметров камеры в момент смены профиля подгонки.
 *
 * <p>Жизненный цикл:
 * <ol>
 *   <li>При обнаружении нужного перехода — {@code trigger()}.
 *   <li>Каждый тик сцены — {@code tick()}.
 *   <li>При отрисовке HUD — {@code getAlpha(partialTick)} для интерполированного значения.
 *   <li>При выходе из сцены — {@code reset()}.
 * </ol>
 *
 * <p>Кривая: плавный fade-out → удержание чёрного → плавный fade-in.
 * Используется синусоидальное смягчение (ease-in-out) для более естественного перехода.
 */
@Environment(EnvType.CLIENT)
public final class GirlSceneTransitionFade {
   /**
    * Fade timings.
    *
    * <p>Short on purpose. The original has no blackout at all - the camera is a single teleport - so the
    * only cuts left to cover are the two where our scene camera switches on or off, and a 2.8-second
    * blackout at each of them was more noticeable than the cut it was hiding. The hold has to outlast
    * {@code SceneGirlEntity.FADE_LEAD_TICKS} minus the fade-out so the cut still lands on black even
    * with a few ticks of latency between the server's signal and the client's tick.
    */
   public static float fadeOutTicks() {
      return (float)com.nxrtweetg.sexmod.config.SexmodConfig.getFadeDurationTicks();
   }

   public static float blackHoldTicks() {
      return (float)com.nxrtweetg.sexmod.config.SexmodConfig.getFadeLeadTicks();
   }

   public static float fadeInTicks() {
      return (float)com.nxrtweetg.sexmod.config.SexmodConfig.getFadeDurationTicks();
   }

   /** Текущий тик анимации, 0 = неактивна. */
   private static int transitionTick = 0;

   private GirlSceneTransitionFade() {
   }

   private static float total() { return fadeOutTicks() + blackHoldTicks() + fadeInTicks(); }

   /**
    * Запускает/перезапускает fade.
    * Если идёт fade-out или black-hold — не перезапускаем (экран уже чёрный, всё хорошо).
    * Если идёт fade-in — перезапускаем с начала: пришёл новый переход, нужно снова закрыть.
    */
   public static void trigger() {
      if (!com.nxrtweetg.sexmod.config.SexmodConfig.isSceneFadeEnabled()) {
         transitionTick = 0;
         return;
      }
      float t = transitionTick > 0 ? (transitionTick - 1) : -1;
      boolean inFadeIn = t > fadeOutTicks() + blackHoldTicks();
      boolean inactive = transitionTick == 0;
      if (inactive || inFadeIn) {
         transitionTick = 1;
      }
   }

   /** Продвигает анимацию на один тик. */
   public static void tick() {
      if (transitionTick > 0) {
         transitionTick++;
         if (transitionTick > (int)total() + 1) {
            transitionTick = 0;
         }
      }
   }

   public static void reset() {
      transitionTick = 0;
   }

   public static boolean isActive() {
      return transitionTick > 0;
   }

   public static float getAlpha(float partialTick) {
      if (!com.nxrtweetg.sexmod.config.SexmodConfig.isSceneFadeEnabled() || transitionTick == 0) return 0.0F;

      float fo = fadeOutTicks();
      float bh = blackHoldTicks();
      float fi = fadeInTicks();
      float tot = fo + bh + fi;
      float t = Math.max(0.0F, Math.min((transitionTick - 1) + partialTick, tot));

      if (t <= fo) {
         return easeIn(t / fo);
      } else if (t <= fo + bh) {
         return 1.0F;
      } else {
         float elapsed = t - fo - bh;
         return easeOut(elapsed / fi);
      }
   }

   private static float easeIn(float t)  { return 1.0F - (float)Math.cos(t * Math.PI / 2.0); }
   private static float easeOut(float t) { return 1.0F - (float)Math.sin(t * Math.PI / 2.0); }
}
