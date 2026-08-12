package com.nxrtweetg.sexmod.girls.state;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

public final class GirlAnimationState {
   private final EnumMap<GirlAnimationParameter, Boolean> values;

   private GirlAnimationState(EnumMap<GirlAnimationParameter, Boolean> values) {
      this.values = values;
   }

   public static GirlAnimationState createDefault() {
      EnumMap<GirlAnimationParameter, Boolean> enummap = new EnumMap<>(GirlAnimationParameter.class);

      for (GirlAnimationParameter girlanimationparameter : GirlAnimationParameter.values()) {
         enummap.put(girlanimationparameter, girlanimationparameter.defaultValue());
      }

      return new GirlAnimationState(enummap);
   }

   public boolean get(GirlAnimationParameter parameter) {
      return this.values.getOrDefault(parameter, parameter.defaultValue());
   }

   public void set(GirlAnimationParameter parameter, boolean value) {
      this.values.put(parameter, value);
   }

   public void apply(Map<GirlAnimationParameter, Boolean> updates) {
      this.values.putAll(updates);
   }

   public Map<GirlAnimationParameter, Boolean> snapshot() {
      return Collections.unmodifiableMap(new EnumMap<>(this.values));
   }

   public EnumMap<GirlAnimationParameter, Boolean> diff(Map<GirlAnimationParameter, Boolean> requestedValues) {
      EnumMap<GirlAnimationParameter, Boolean> enummap = new EnumMap<>(GirlAnimationParameter.class);

      for (Entry<GirlAnimationParameter, Boolean> entry : requestedValues.entrySet()) {
         if (this.get(entry.getKey()) != entry.getValue()) {
            enummap.put(entry.getKey(), entry.getValue());
         }
      }

      return enummap;
   }
}
