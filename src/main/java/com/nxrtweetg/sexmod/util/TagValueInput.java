package com.nxrtweetg.sexmod.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

/**
 * Thin stand-in for the 26.x value-NBT API ({@code ValueInput}) on top of the plain
 * {@code CompoundTag} the 1.21.x line uses, so the entity load code written against the newer API
 * keeps working verbatim.
 */
public final class TagValueInput {
   private final CompoundTag tag;
   private final HolderLookup.Provider registries;

   private TagValueInput(CompoundTag tag, HolderLookup.Provider registries) {
      this.tag = tag;
      this.registries = registries;
   }

   public static TagValueInput wrap(CompoundTag tag) {
      return new TagValueInput(tag, null);
   }

   public static TagValueInput wrap(CompoundTag tag, HolderLookup.Provider registries) {
      return new TagValueInput(tag, registries);
   }

   public CompoundTag tag() {
      return this.tag;
   }

   public boolean getBooleanOr(String key, boolean fallback) {
      return this.tag.contains(key) ? this.tag.getBoolean(key) : fallback;
   }

   public int getIntOr(String key, int fallback) {
      return this.tag.contains(key) ? this.tag.getInt(key) : fallback;
   }

   public long getLongOr(String key, long fallback) {
      return this.tag.contains(key) ? this.tag.getLong(key) : fallback;
   }

   public float getFloatOr(String key, float fallback) {
      return this.tag.contains(key) ? this.tag.getFloat(key) : fallback;
   }

   public double getDoubleOr(String key, double fallback) {
      return this.tag.contains(key) ? this.tag.getDouble(key) : fallback;
   }

   public java.util.Optional<String> getString(String key) {
      return this.tag.contains(key) ? Optional.of(this.tag.getString(key)) : Optional.empty();
   }

   public java.util.Optional<Integer> getInt(String key) {
      return this.tag.contains(key) ? Optional.of(this.tag.getInt(key)) : Optional.empty();
   }

   public String getStringOr(String key, String fallback) {
      return this.tag.contains(key) ? this.tag.getString(key) : fallback;
   }

   public TagValueInput childOrEmpty(String key) {
      return new TagValueInput(this.tag.getCompound(key), this.registries);
   }

   public TagValueInput child(String key) {
      return new TagValueInput(this.tag.getCompound(key), this.registries);
   }

   public <T> Optional<T> read(String key, Codec<T> codec) {
      if (!this.tag.contains(key)) {
         return Optional.empty();
      }
      DynamicOps<Tag> ops = this.registries != null ? this.registries.createSerializationContext(NbtOps.INSTANCE) : NbtOps.INSTANCE;
      return codec.parse(ops, this.tag.get(key)).result();
   }
}
