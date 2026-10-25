package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.config.SexmodConfig;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public final class GirlSettingsScreen extends Screen {
   private static final int PANEL_WIDTH = 340;
   private static final int PANEL_HEIGHT = 328;
   private static final int PADDING = 10;
   private static final int ROW_HEIGHT = 18;

   private int selectedCharacterIndex = 0;

   public GirlSettingsScreen() {
      super(Component.literal("SexMod Settings"));
   }

   @Override
   protected void init() {
      this.clearWidgets();

      if (this.minecraft != null && this.minecraft.player != null) {
         Optional<SceneGirlEntity> owned = GirlSceneHud.findOwnedGirl(this.minecraft.player);
         if (owned.isPresent()) {
            String charId = owned.get().characterId().toLowerCase();
            int idx = SexmodConfig.ALL_CHARACTERS.indexOf(charId);
            if (idx >= 0) {
               this.selectedCharacterIndex = idx;
            }
         }
      }

      int panelLeft = Math.max(16, (this.width - PANEL_WIDTH) / 2);
      int panelTop = Math.max(6, (this.height - PANEL_HEIGHT) / 2);
      int y = panelTop + 24;

      int halfW = (PANEL_WIDTH - PADDING * 2 - 6) / 2;

      // 1. Scene blackout toggle & Shading toggle (side by side)
      Button fadeBtn = Button.builder(
         Component.literal(I18n.get("settings.sexmod.blackout", SexmodConfig.isSceneFadeEnabled() ? "ON" : "OFF")),
         b -> {
            SexmodConfig.setSceneFadeEnabled(!SexmodConfig.isSceneFadeEnabled());
            b.setMessage(Component.literal(I18n.get("settings.sexmod.blackout", SexmodConfig.isSceneFadeEnabled() ? "ON" : "OFF")));
         }
      ).bounds(panelLeft + PADDING, y, halfW, ROW_HEIGHT).build();
      this.addRenderableWidget(fadeBtn);

      Button shadingBtn = Button.builder(
         Component.literal(I18n.get("settings.sexmod.shading", SexmodConfig.isFlatLitRenderingEnabled() ? I18n.get("settings.sexmod.shading.flat") : I18n.get("settings.sexmod.shading.shader_safe"))),
         b -> {
            SexmodConfig.setFlatLitRenderingEnabled(!SexmodConfig.isFlatLitRenderingEnabled());
            b.setMessage(Component.literal(I18n.get("settings.sexmod.shading", SexmodConfig.isFlatLitRenderingEnabled() ? I18n.get("settings.sexmod.shading.flat") : I18n.get("settings.sexmod.shading.shader_safe"))));
         }
      ).bounds(panelLeft + PADDING + halfW + 6, y, halfW, ROW_HEIGHT).build();
      this.addRenderableWidget(shadingBtn);
      y += 21;

      // 1.1 Bone Cam Mode toggle: Dynamic vs Static
      Button boneCamBtn = Button.builder(
         Component.literal(I18n.get("settings.sexmod.bone_cam", SexmodConfig.isBoneCamDynamic() ? I18n.get("settings.sexmod.bone_cam.dynamic") : I18n.get("settings.sexmod.bone_cam.static"))),
         b -> {
            SexmodConfig.setBoneCamDynamic(!SexmodConfig.isBoneCamDynamic());
            b.setMessage(Component.literal(I18n.get("settings.sexmod.bone_cam", SexmodConfig.isBoneCamDynamic() ? I18n.get("settings.sexmod.bone_cam.dynamic") : I18n.get("settings.sexmod.bone_cam.static"))));
         }
      ).bounds(panelLeft + PADDING, y, PANEL_WIDTH - PADDING * 2, ROW_HEIGHT).build();
      this.addRenderableWidget(boneCamBtn);
      y += 21;

      // 2. Fade Duration row
      int nudgeBtnW = 30;
      int nudgeTotalW = (nudgeBtnW + 2) * 3 + nudgeBtnW;
      int nudgeStartX = panelLeft + PANEL_WIDTH - PADDING - nudgeTotalW;

      this.addIntNudgeButtons(nudgeStartX, y, nudgeBtnW,
         SexmodConfig::getFadeDurationTicks,
         SexmodConfig::setFadeDurationTicks,
         1, 2
      );
      y += 21;

      // 3. Fade Early Start row
      this.addIntNudgeButtons(nudgeStartX, y, nudgeBtnW,
         SexmodConfig::getFadeLeadTicks,
         SexmodConfig::setFadeLeadTicks,
         1, 2
      );
      y += 22;

      // 4. All scenes toggle
      Button allScenesBtn = Button.builder(
         Component.literal(I18n.get("settings.sexmod.all_scenes", SexmodConfig.isAllScenesEnabled() ? "ON" : "OFF")),
         b -> {
            SexmodConfig.setAllScenesEnabled(!SexmodConfig.isAllScenesEnabled());
            b.setMessage(Component.literal(I18n.get("settings.sexmod.all_scenes", SexmodConfig.isAllScenesEnabled() ? "ON" : "OFF")));
         }
      ).bounds(panelLeft + PADDING, y, PANEL_WIDTH - PADDING * 2, ROW_HEIGHT).build();
      this.addRenderableWidget(allScenesBtn);
      y += 20 + 22; // extra spacing for 2-line experimental notice

      // 5. Character Selector row
      int btnW = 28;
      int charDisplayW = PANEL_WIDTH - PADDING * 2 - btnW * 2 - 8;
      Button prevCharBtn = Button.builder(Component.literal("<"), b -> {
         this.selectedCharacterIndex = Math.floorMod(this.selectedCharacterIndex - 1, SexmodConfig.ALL_CHARACTERS.size());
         this.rebuildWidgets();
      }).bounds(panelLeft + PADDING, y, btnW, ROW_HEIGHT).build();
      this.addRenderableWidget(prevCharBtn);

      Button charNameBtn = Button.builder(Component.literal(this.currentCharDisplayName()), b -> {
         this.selectedCharacterIndex = Math.floorMod(this.selectedCharacterIndex + 1, SexmodConfig.ALL_CHARACTERS.size());
         this.rebuildWidgets();
      }).bounds(panelLeft + PADDING + btnW + 4, y, charDisplayW, ROW_HEIGHT).build();
      this.addRenderableWidget(charNameBtn);

      Button nextCharBtn = Button.builder(Component.literal(">"), b -> {
         this.selectedCharacterIndex = Math.floorMod(this.selectedCharacterIndex + 1, SexmodConfig.ALL_CHARACTERS.size());
         this.rebuildWidgets();
      }).bounds(panelLeft + PADDING + btnW + 4 + charDisplayW + 4, y, btnW, ROW_HEIGHT).build();
      this.addRenderableWidget(nextCharBtn);
      y += 22;

      // 6. Breasts Scale Controls
      String charId = this.currentCharId();
      this.addFloatNudgeButtons(nudgeStartX, y, nudgeBtnW,
         () -> SexmodConfig.getBreastScale(charId),
         val -> SexmodConfig.setBreastScale(charId, val)
      );
      y += 21;

      // 7. Booty Scale Controls
      this.addFloatNudgeButtons(nudgeStartX, y, nudgeBtnW,
         () -> SexmodConfig.getBootyScale(charId),
         val -> SexmodConfig.setBootyScale(charId, val)
      );
      y += 21;

      // 8. Overall Size Controls
      this.addFloatNudgeButtons(nudgeStartX, y, nudgeBtnW,
         () -> SexmodConfig.getGirlScale(charId),
         val -> SexmodConfig.setGirlScale(charId, val)
      );
      y += 23;

      // 9. Reset buttons
      Button resetCharBtn = Button.builder(Component.literal(I18n.get("settings.sexmod.reset_char")), b -> {
         SexmodConfig.resetCharacter(charId);
         this.rebuildWidgets();
      }).bounds(panelLeft + PADDING, y, halfW, ROW_HEIGHT).build();
      this.addRenderableWidget(resetCharBtn);

      Button resetAllBtn = Button.builder(Component.literal(I18n.get("settings.sexmod.reset_all")), b -> {
         SexmodConfig.resetAll();
         this.rebuildWidgets();
      }).bounds(panelLeft + PADDING + halfW + 6, y, halfW, ROW_HEIGHT).build();
      this.addRenderableWidget(resetAllBtn);
      y += 22;

      // 9.1 Home settings button
      Button homeSettingsBtn = Button.builder(Component.literal(I18n.get("gui.sexmod.home.title")), b -> {
         if (this.minecraft != null) {
            this.minecraft.setScreen(new GirlHomeSettingsScreen(null, this));
         }
      }).bounds(panelLeft + PADDING, y, PANEL_WIDTH - PADDING * 2, ROW_HEIGHT).build();
      this.addRenderableWidget(homeSettingsBtn);
      y += 21;

      // 10. Close button
      Button closeBtn = Button.builder(Component.literal(I18n.get("settings.sexmod.done")), b -> this.onClose())
         .bounds(panelLeft + PADDING, y, PANEL_WIDTH - PADDING * 2, ROW_HEIGHT).build();
      this.addRenderableWidget(closeBtn);
   }

   private void addIntNudgeButtons(int startX, int y, int width, java.util.function.Supplier<Integer> getter, java.util.function.Consumer<Integer> setter, int step1, int step2) {
      Button m2 = Button.builder(Component.literal("-" + step2), b -> {
         setter.accept(getter.get() - step2);
         this.rebuildWidgets();
      }).bounds(startX, y, width, ROW_HEIGHT).build();
      this.addRenderableWidget(m2);

      Button m1 = Button.builder(Component.literal("-" + step1), b -> {
         setter.accept(getter.get() - step1);
         this.rebuildWidgets();
      }).bounds(startX + width + 2, y, width, ROW_HEIGHT).build();
      this.addRenderableWidget(m1);

      Button p1 = Button.builder(Component.literal("+" + step1), b -> {
         setter.accept(getter.get() + step1);
         this.rebuildWidgets();
      }).bounds(startX + (width + 2) * 2, y, width, ROW_HEIGHT).build();
      this.addRenderableWidget(p1);

      Button p2 = Button.builder(Component.literal("+" + step2), b -> {
         setter.accept(getter.get() + step2);
         this.rebuildWidgets();
      }).bounds(startX + (width + 2) * 3, y, width, ROW_HEIGHT).build();
      this.addRenderableWidget(p2);
   }

   private void addFloatNudgeButtons(int startX, int y, int width, java.util.function.Supplier<Float> getter, java.util.function.Consumer<Float> setter) {
      Button m10 = Button.builder(Component.literal("-0.1"), b -> {
         setter.accept(getter.get() - 0.10F);
         this.rebuildWidgets();
      }).bounds(startX, y, width, ROW_HEIGHT).build();
      this.addRenderableWidget(m10);

      Button m05 = Button.builder(Component.literal("-0.05"), b -> {
         setter.accept(getter.get() - 0.05F);
         this.rebuildWidgets();
      }).bounds(startX + width + 2, y, width, ROW_HEIGHT).build();
      this.addRenderableWidget(m05);

      Button p05 = Button.builder(Component.literal("+0.05"), b -> {
         setter.accept(getter.get() + 0.05F);
         this.rebuildWidgets();
      }).bounds(startX + (width + 2) * 2, y, width, ROW_HEIGHT).build();
      this.addRenderableWidget(p05);

      Button p10 = Button.builder(Component.literal("+0.1"), b -> {
         setter.accept(getter.get() + 0.10F);
         this.rebuildWidgets();
      }).bounds(startX + (width + 2) * 3, y, width, ROW_HEIGHT).build();
      this.addRenderableWidget(p10);
   }

   private String currentCharId() {
      List<String> list = SexmodConfig.ALL_CHARACTERS;
      return list.get(Math.floorMod(this.selectedCharacterIndex, list.size()));
   }

   private String currentCharDisplayName() {
      String id = this.currentCharId();
      return Character.toUpperCase(id.charAt(0)) + id.substring(1) + " (" + (this.selectedCharacterIndex + 1) + "/" + SexmodConfig.ALL_CHARACTERS.size() + ")";
   }

   @Override
   public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
      int panelLeft = Math.max(16, (this.width - PANEL_WIDTH) / 2);
      int panelTop = Math.max(6, (this.height - PANEL_HEIGHT) / 2);

      // Dark translucent background plate
      context.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xD810121A);
      // Border outline
      context.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + 1, 0xFF4A4E69);
      context.fill(panelLeft, panelTop + PANEL_HEIGHT - 1, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xFF4A4E69);
      context.fill(panelLeft, panelTop, panelLeft + 1, panelTop + PANEL_HEIGHT, 0xFF4A4E69);
      context.fill(panelLeft + PANEL_WIDTH - 1, panelTop, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xFF4A4E69);

      // Title
      context.drawString(this.font, Component.literal(I18n.get("settings.sexmod.title")), panelLeft + PADDING, panelTop + 7, 0xFFE0E0E0);

      // Fade duration and early start labels
      int duration = SexmodConfig.getFadeDurationTicks();
      int lead = SexmodConfig.getFadeLeadTicks();
      String durStr = I18n.get("settings.sexmod.fade_duration", duration, duration / 20.0F);
      String leadStr = I18n.get("settings.sexmod.fade_lead", lead, lead / 20.0F);
      context.drawString(this.font, Component.literal(durStr), panelLeft + PADDING, panelTop + 66 + 5, 0xFFFFFFFF);
      context.drawString(this.font, Component.literal(leadStr), panelLeft + PADDING, panelTop + 87 + 5, 0xFFFFFFFF);

      // Notice for "All Scenes for All Girls"
      String note1 = I18n.get("settings.sexmod.all_scenes_note1");
      String note2 = I18n.get("settings.sexmod.all_scenes_note2");
      context.drawString(this.font, Component.literal("§6" + note1), panelLeft + PADDING, panelTop + 129, 0xFFE0E0E0);
      context.drawString(this.font, Component.literal("§6" + note2), panelLeft + PADDING, panelTop + 138, 0xFFE0E0E0);

      // Labels for current scales
      String charId = this.currentCharId();
      float bScale = SexmodConfig.getBreastScale(charId);
      float btScale = SexmodConfig.getBootyScale(charId);
      float gScale = SexmodConfig.getGirlScale(charId);

      context.drawString(this.font, Component.literal(I18n.get("settings.sexmod.breasts", bScale)), panelLeft + PADDING, panelTop + 173 + 5, 0xFFFFFFFF);
      context.drawString(this.font, Component.literal(I18n.get("settings.sexmod.booty", btScale)), panelLeft + PADDING, panelTop + 194 + 5, 0xFFFFFFFF);
      context.drawString(this.font, Component.literal(I18n.get("settings.sexmod.size", gScale)), panelLeft + PADDING, panelTop + 215 + 5, 0xFFFFFFFF);

      super.render(context, mouseX, mouseY, delta);
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == InputConstants.KEY_Z || keyCode == InputConstants.KEY_ESCAPE) {
         this.onClose();
         return true;
      }
      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }
}
