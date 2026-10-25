package com.nxrtweetg.sexmod.girls.custom;

import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.anim.GirlAnimationSet;
import com.nxrtweetg.sexmod.girls.anim.GirlSoundSet;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import com.nxrtweetg.sexmod.util.TagValueInput;
import com.nxrtweetg.sexmod.util.TagValueOutput;

import java.util.EnumSet;
import java.util.Set;

public final class CustomGirlEntity extends SceneGirlEntity {
    private static final EntityDataAccessor<String> TRACKED_CUSTOM_ID =
        SynchedEntityData.defineId(CustomGirlEntity.class, EntityDataSerializers.STRING);

    public CustomGirlEntity(EntityType<? extends CustomGirlEntity> entityType, Level world) {
        super(entityType, world, "CustomCompanion");
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TRACKED_CUSTOM_ID, "default");
    }

    public String getCustomId() {
        return this.entityData.get(TRACKED_CUSTOM_ID);
    }

    public void setCustomId(String id) {
        this.entityData.set(TRACKED_CUSTOM_ID, id != null ? id : "default");
        applyCustomData();
    }

    public TCEditorLoader.CustomCharacterData getCustomData() {
        return TCEditorLoader.get(getCustomId());
    }

    public void applyCustomData() {
        TCEditorLoader.CustomCharacterData data = getCustomData();
        if (data != null) {
            this.setCustomName(Component.literal(data.name()));
            this.setCustomNameVisible(true);
        }
    }

    @Override
    public String characterId() {
        return "custom_" + getCustomId();
    }

    public static final GirlAnimationSet CUSTOM_ANIMATIONS = new GirlAnimationSet("custom", Set.of("idle", "walk", "run"));

    @Override
    protected GirlAnimationSet animations() {
        return CUSTOM_ANIMATIONS;
    }

    @Override
    protected GirlSoundSet sounds() {
        return GirlSoundSet.JENNY;
    }

    @Override
    protected software.bernie.geckolib.animation.RawAnimation selectCustomMovementAnimation(boolean moving) {
        return moving
            ? software.bernie.geckolib.animation.RawAnimation.begin().thenLoop("animation.custom.walk")
            : software.bernie.geckolib.animation.RawAnimation.begin().thenLoop("animation.custom.idle");
    }

    @Override
    protected Set<GirlSceneAction> sceneActions() {
        TCEditorLoader.CustomCharacterData data = getCustomData();
        if (data == null || data.enabledScenes() == null || data.enabledScenes().isEmpty()) {
            return EnumSet.of(GirlSceneAction.BLOWJOB, GirlSceneAction.BOOBJOB, GirlSceneAction.DOGGY, GirlSceneAction.STRIP);
        }

        Set<GirlSceneAction> set = EnumSet.noneOf(GirlSceneAction.class);
        for (String scene : data.enabledScenes()) {
            switch (scene.toLowerCase()) {
                case "blowjob" -> set.add(GirlSceneAction.BLOWJOB);
                case "boobjob" -> set.add(GirlSceneAction.BOOBJOB);
                case "doggy" -> set.add(GirlSceneAction.DOGGY);
                case "strip" -> set.add(GirlSceneAction.STRIP);
                case "mating_press" -> set.add(GirlSceneAction.MATING_PRESS);
                case "touch" -> set.add(GirlSceneAction.HEAD_PAT);
                case "deepthroat" -> set.add(GirlSceneAction.DEEPTHROAT);
                case "facefuck" -> set.add(GirlSceneAction.FACEFUCK);
            }
        }

        if (set.isEmpty()) {
            set.add(GirlSceneAction.BLOWJOB);
            set.add(GirlSceneAction.BOOBJOB);
            set.add(GirlSceneAction.DOGGY);
            set.add(GirlSceneAction.STRIP);
        }
        return set;
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
      TagValueOutput output = TagValueOutput.wrap(tag, this.registryAccess());
        super.addAdditionalSaveData(tag);
        output.putString("CustomId", getCustomId());
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
      TagValueInput input = TagValueInput.wrap(tag, this.registryAccess());
        super.readAdditionalSaveData(tag);
        input.getString("CustomId").ifPresent(this::setCustomId);
    }
}
