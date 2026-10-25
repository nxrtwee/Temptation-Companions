package com.nxrtweetg.sexmod.home;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.nbt.NbtOps;

/**
 * SavedData storing player-defined companion home regions.
 */
public final class SexmodHomeData extends SavedData {
   private final Map<String, GirlHomeRegion> homes = new LinkedHashMap<>();

   public static final Codec<SexmodHomeData> CODEC = RecordCodecBuilder.create(instance ->
      instance.group(
         GirlHomeRegion.CODEC.listOf().fieldOf("homes").forGetter(SexmodHomeData::getHomesList)
      ).apply(instance, SexmodHomeData::new)
   );

   public static final SavedData.Factory<SexmodHomeData> TYPE = new SavedData.Factory<>(
      SexmodHomeData::new,
      (tag, registries) -> CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag).result().orElseGet(SexmodHomeData::new),
      DataFixTypes.SAVED_DATA_STRUCTURE_FEATURE_INDICES
   );

   @Override
   public net.minecraft.nbt.CompoundTag save(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
      CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), this)
         .result().ifPresent(encoded -> tag.merge((net.minecraft.nbt.CompoundTag)encoded));
      return tag;
   }

   public SexmodHomeData() {
   }

   public SexmodHomeData(List<GirlHomeRegion> list) {
      for (GirlHomeRegion h : list) {
         this.homes.put(h.name().toLowerCase(), h);
      }
   }

   public static SexmodHomeData get(ServerLevel level) {
      ServerLevel overworld = level.getServer().overworld();
      return overworld.getDataStorage().computeIfAbsent(TYPE, "sexmod:homes");
   }

   public List<GirlHomeRegion> getHomesList() {
      return new ArrayList<>(this.homes.values());
   }

   public synchronized void addHome(GirlHomeRegion home) {
      this.homes.put(home.name().toLowerCase(), home);
      this.setDirty();
   }

   public synchronized boolean removeHome(String name) {
      GirlHomeRegion removed = this.homes.remove(name.toLowerCase());
      if (removed != null) {
         this.setDirty();
         return true;
      }
      return false;
   }

   public synchronized GirlHomeRegion getHome(String name) {
      if (name == null) {
         return null;
      }
      return this.homes.get(name.toLowerCase());
   }

   public synchronized Collection<GirlHomeRegion> getAllHomes() {
      return Collections.unmodifiableCollection(this.homes.values());
   }
}
