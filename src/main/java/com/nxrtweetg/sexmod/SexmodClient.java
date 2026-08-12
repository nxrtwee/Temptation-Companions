package com.nxrtweetg.sexmod;

import com.nxrtweetg.sexmod.client.GirlClientBootstrap;
import com.nxrtweetg.sexmod.client.LegacyGeckoLibResourceCompat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class SexmodClient implements ClientModInitializer {
   public void onInitializeClient() {
      SexmodMod.LOGGER.info("Initializing client hooks");
      LegacyGeckoLibResourceCompat.initialize();
      GirlClientBootstrap.initialize();
   }
}
