package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.config.SexmodConfig;
import java.lang.reflect.Method;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public final class ShaderCompat {
   private static boolean irisApiChecked = false;
   private static Object irisApiInstance = null;
   private static Method isShaderPackInUseMethod = null;

   private static boolean irisClassChecked = false;
   private static Method irisIsShadersEnabledMethod = null;

   private ShaderCompat() {
   }

   /**
    * Returns true if Iris shaders (or an external shaderpack) are actively rendering,
    * or if the user has disabled the custom flat-lit pipeline in configuration.
    * When true, standard vanilla entity render types must be used to prevent vertex explosion
    * and pipeline mismatch in Iris/Sodium.
    */
   public static boolean isShadersActive() {
      if (!SexmodConfig.isFlatLitRenderingEnabled()) {
         return true;
      }

      // 1. Official Iris API v0 check: IrisApi.getInstance().isShaderPackInUse()
      if (!irisApiChecked) {
         irisApiChecked = true;
         if (FabricLoader.getInstance().isModLoaded("iris") || FabricLoader.getInstance().isModLoaded("oculus")) {
            try {
               Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
               Method getInstanceMethod = apiClass.getMethod("getInstance");
               irisApiInstance = getInstanceMethod.invoke(null);
               isShaderPackInUseMethod = apiClass.getMethod("isShaderPackInUse");
            } catch (Throwable ignored) {
            }
         }
      }

      if (irisApiInstance != null && isShaderPackInUseMethod != null) {
         try {
            Object result = isShaderPackInUseMethod.invoke(irisApiInstance);
            if (result instanceof Boolean inUse && inUse) {
               return true;
            }
         } catch (Throwable ignored) {
         }
      }

      // 2. Direct internal fallback: net.irisshaders.iris.Iris.isShadersEnabled()
      if (!irisClassChecked) {
         irisClassChecked = true;
         if (FabricLoader.getInstance().isModLoaded("iris")) {
            try {
               Class<?> irisClass = Class.forName("net.irisshaders.iris.Iris");
               irisIsShadersEnabledMethod = irisClass.getMethod("isShadersEnabled");
            } catch (Throwable ignored) {
            }
         }
      }

      if (irisIsShadersEnabledMethod != null) {
         try {
            Object result = irisIsShadersEnabledMethod.invoke(null);
            if (result instanceof Boolean enabled && enabled) {
               return true;
            }
         } catch (Throwable ignored) {
         }
      }

      return false;
   }
}
