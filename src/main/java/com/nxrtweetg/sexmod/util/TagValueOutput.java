package com.nxrtweetg.sexmod.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.core.HolderLookup;

/**
 * Thin stand-in for the 26.x value-NBT API ({@code ValueOutput}) on top of the plain
 * {@code CompoundTag} the 1.21.x line uses, so the entity save code written against the newer API
 * keeps working verbatim.
 */
public final class TagValueOutput {
   private final CompoundTag tag;
   private final HolderLookup.Provider registries;

   private TagValueOutput(CompoundTag tag, HolderLookup.Provider registries) {
      this.tag = tag;
      this.registries = registries;
   }

   public static TagValueOutput wrap(CompoundTag tag) {
      return new TagValueOutput(tag, null);
   }

   public static TagValueOutput wrap(CompoundTag tag, HolderLookup.Provider registries) {
      return new TagValueOutput(tag, registries);
   }

   public CompoundTag tag() {
      return this.tag;
   }

   public CompoundTag buildResult() {
      return this.tag;
   }

   public void putBoolean(String key, boolean value) {
      this.tag.putBoolean(key, value);
   }

   public void putInt(String key, int value) {
      this.tag.putInt(key, value);
   }

   public void putLong(String key, long value) {
      this.tag.putLong(key, value);
   }

   public void putFloat(String key, float value) {
      this.tag.putFloat(key, value);
   }

   public void putDouble(String key, double value) {
      this.tag.putDouble(key, value);
   }

   public void putString(String key, String value) {
      this.tag.putString(key, value);
   }

   public TagValueOutput child(String key) {
      CompoundTag child = new CompoundTag();
      this.tag.put(key, child);
      return new TagValueOutput(child, this.registries);
   }

   public <T> void storeNullable(String key, Codec<T> codec, T value) {
      store(key, codec, value);
   }

   public <T> void store(String key, Codec<T> codec, T value) {
      if (value == null) {
         return;
      }
      serializationOps().map(ops -> codec.encodeStart(ops, value).result()).filter(Optional::isPresent)
         .ifPresent(encoded -> encoded.ifPresent(tagged -> this.tag.put(key, (Tag)tagged)));
   }

   private Optional<com.mojang.serialization.DynamicOps<Tag>> serializationOps() {
      if (this.registries == null) {
         return Optional.of(NbtOps.INSTANCE);
      }
      return Optional.of(this.registries.createSerializationContext(NbtOps.INSTANCE));
   }
}
