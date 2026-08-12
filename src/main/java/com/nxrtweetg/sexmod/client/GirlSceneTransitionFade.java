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
   /** Finalized fade timings — tuned in-game via numpad editor, now hardcoded. */
   public static float fadeOutTicks  = 10.0F;
   public static float blackHoldTicks = 28.0F;
   public static float fadeInTicks   = 18.0F;

   /** Текущий тик анимации, 0 = неактивна. */
   private static int transitionTick = 0;

   private GirlSceneTransitionFade() {
   }

   private static float total() { return fadeOutTicks + blackHoldTicks + fadeInTicks; }

   /**
    * Запускает/перезапускает fade.
    * Если идёт fade-out или black-hold — не перезапускаем (экран уже чёрный, всё хорошо).
    * Если идёт fade-in — перезапускаем с начала: пришёл новый переход, нужно снова закрыть.
    */
   public static void trigger() {
      float t = transitionTick > 0 ? (transitionTick - 1) : -1;
      boolean inFadeIn = t > fadeOutTicks + blackHoldTicks;
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
      if (transitionTick == 0) return 0.0F;

      float t = Math.max(0.0F, Math.min((transitionTick - 1) + partialTick, total()));

      if (t <= fadeOutTicks) {
         return easeIn(t / fadeOutTicks);
      } else if (t <= fadeOutTicks + blackHoldTicks) {
         return 1.0F;
      } else {
         float elapsed = t - fadeOutTicks - blackHoldTicks;
         return easeOut(elapsed / fadeInTicks);
      }
   }

   private static float easeIn(float t)  { return 1.0F - (float)Math.cos(t * Math.PI / 2.0); }
   private static float easeOut(float t) { return 1.0F - (float)Math.sin(t * Math.PI / 2.0); }
}
