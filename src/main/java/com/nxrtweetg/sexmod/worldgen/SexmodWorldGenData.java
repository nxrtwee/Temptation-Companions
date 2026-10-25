package com.nxrtweetg.sexmod.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class SexmodWorldGenData extends SavedData {
   public record PlacedStructure(String type, int chunkX, int chunkZ) {
      public static final Codec<PlacedStructure> CODEC = RecordCodecBuilder.create(instance ->
         instance.group(
            Codec.STRING.fieldOf("type").forGetter(PlacedStructure::type),
            Codec.INT.fieldOf("chunkX").forGetter(PlacedStructure::chunkX),
            Codec.INT.fieldOf("chunkZ").forGetter(PlacedStructure::chunkZ)
         ).apply(instance, PlacedStructure::new)
      );
   }

   private final List<PlacedStructure> structures = new ArrayList<>();
   private final Set<Long> processedChunks = new HashSet<>();

   public static final Codec<SexmodWorldGenData> CODEC = RecordCodecBuilder.create(instance ->
      instance.group(
         PlacedStructure.CODEC.listOf().fieldOf("structures").forGetter(SexmodWorldGenData::getStructuresList),
         Codec.LONG.listOf().fieldOf("processedChunks").forGetter(SexmodWorldGenData::getProcessedChunksList)
      ).apply(instance, SexmodWorldGenData::new)
   );

   public static final SavedDataType<SexmodWorldGenData> TYPE = new SavedDataType<>(
      "sexmod:generation",
      SexmodWorldGenData::new,
      CODEC,
      DataFixTypes.SAVED_DATA_STRUCTURE_FEATURE_INDICES
   );

   public SexmodWorldGenData() {
   }

   public SexmodWorldGenData(List<PlacedStructure> structures, List<Long> processedChunks) {
      this.structures.addAll(structures);
      this.processedChunks.addAll(processedChunks);
   }

   public List<PlacedStructure> getStructuresList() {
      return this.structures;
   }

   public List<Long> getProcessedChunksList() {
      return new ArrayList<>(this.processedChunks);
   }

   public synchronized boolean isChunkProcessed(long chunkKey) {
      return this.processedChunks.contains(chunkKey);
   }

   public synchronized void markChunkProcessed(long chunkKey) {
      this.processedChunks.add(chunkKey);
      this.setDirty();
   }

   public synchronized boolean canPlaceStructure(String type, int chunkX, int chunkZ) {
      for (PlacedStructure placed : this.structures) {
         float dist = (float) Math.hypot(chunkX - placed.chunkX(), chunkZ - placed.chunkZ());
         float minRequired = placed.type().equalsIgnoreCase(type) ? 40.0F : 16.0F;
         if (dist < minRequired) {
            return false;
         }
      }
      return true;
   }

   public synchronized void addStructure(String type, int chunkX, int chunkZ) {
      this.structures.add(new PlacedStructure(type, chunkX, chunkZ));
      this.setDirty();
   }
}
