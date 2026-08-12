package com.nxrtweetg.sexmod;

import com.nxrtweetg.sexmod.command.SexmodCommands;
import com.nxrtweetg.sexmod.girls.ModDataSerializers;
import com.nxrtweetg.sexmod.girls.ModEntities;
import com.nxrtweetg.sexmod.girls.inventory.GirlMenuTypes;
import com.nxrtweetg.sexmod.item.ModItems;
import com.nxrtweetg.sexmod.network.SexmodNetworking;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SexmodMod implements ModInitializer {
   public static final String MOD_ID = "sexmod";
   public static final Logger LOGGER = LoggerFactory.getLogger("sexmod");

   public void onInitialize() {
      LOGGER.info("Initializing {} port scaffold", "sexmod");
      ModDataSerializers.register();
      ModEntities.register();
      ModItems.register();
      GirlMenuTypes.register();
      SexmodNetworking.register();
      SexmodCommands.register();
   }
}
