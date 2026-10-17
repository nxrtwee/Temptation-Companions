package com.nxrtweetg.sexmod.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Client-side cache of known home regions received from the server.
 */
@Environment(EnvType.CLIENT)
public final class ClientHomeManager {
   private static List<GirlHomeRegion> homes = new ArrayList<>();
   private static GirlHomeRegion selectedHome = null;

   private ClientHomeManager() {
   }

   public static synchronized void setHomes(List<GirlHomeRegion> list) {
      homes = new ArrayList<>(list);
   }

   public static synchronized List<GirlHomeRegion> getHomes() {
      return Collections.unmodifiableList(homes);
   }

   public static synchronized void setSelectedHome(GirlHomeRegion home) {
      selectedHome = home;
   }

   public static synchronized GirlHomeRegion getSelectedHome() {
      return selectedHome;
   }
}
