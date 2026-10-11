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
      net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
         com.nxrtweetg.sexmod.network.SyncHomesPayload.ID,
         (payload, context) -> context.client().execute(() -> com.nxrtweetg.sexmod.home.ClientHomeManager.setHomes(payload.homes()))
      );
   }
}
