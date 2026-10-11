package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.girls.GirlEntity;
import com.nxrtweetg.sexmod.home.ClientHomeManager;
import com.nxrtweetg.sexmod.home.GirlHomeRegion;
import com.nxrtweetg.sexmod.network.HomeActionPayload;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;

@Environment(EnvType.CLIENT)
public final class GirlHomeSettingsScreen extends Screen {
   private static final int PANEL_WIDTH = 380;
   private static final int PANEL_HEIGHT = 280;
   private static final int PADDING = 10;
   private static final int ROW_HEIGHT = 18;

   private final GirlEntity girl;
   private final Screen parentScreen;

   private String selectedHomeName = "";
   private int homeListPage = 0;
   private static final int HOMES_PER_PAGE = 4;

   private EditBox nameEdit;
   private EditBox x1Edit;
   private EditBox y1Edit;
   private EditBox z1Edit;
   private EditBox x2Edit;
   private EditBox y2Edit;
   private EditBox z2Edit;

   public GirlHomeSettingsScreen(GirlEntity girl) {
      this(girl, null);
   }

   public GirlHomeSettingsScreen(GirlEntity girl, Screen parentScreen) {
      super(Component.literal("Home Settings"));
      this.girl = girl;
      this.parentScreen = parentScreen;
      if (girl != null && !girl.assignedHomeName().isEmpty()) {
         this.selectedHomeName = girl.assignedHomeName();
      }
   }

   @Override
   protected void init() {
      this.clearWidgets();
      ClientPlayNetworking.send(HomeActionPayload.query());

      int panelLeft = Math.max(10, (this.width - PANEL_WIDTH) / 2);
      int panelTop = Math.max(6, (this.height - PANEL_HEIGHT) / 2);

      int col1X = panelLeft + PADDING;
      int col1W = 170;
      int col2X = panelLeft + PANEL_WIDTH / 2 + 10;
      int col2W = 165;

      // --- Left Column: Homes list & actions ---
      int y = panelTop + 40;
      List<GirlHomeRegion> homes = ClientHomeManager.getHomes();
      int totalPages = Math.max(1, (homes.size() + HOMES_PER_PAGE - 1) / HOMES_PER_PAGE);
      if (this.homeListPage >= totalPages) {
         this.homeListPage = totalPages - 1;
      }
      if (this.homeListPage < 0) {
         this.homeListPage = 0;
      }

      int startIdx = this.homeListPage * HOMES_PER_PAGE;
      int endIdx = Math.min(startIdx + HOMES_PER_PAGE, homes.size());

      for (int i = startIdx; i < endIdx; i++) {
         GirlHomeRegion h = homes.get(i);
         boolean isSelected = h.name().equalsIgnoreCase(this.selectedHomeName);
         String label = (isSelected ? "> " : "") + h.name() + " [" + h.sizeX() + "x" + h.sizeY() + "x" + h.sizeZ() + "]";
         Button homeBtn = Button.builder(Component.literal(label), b -> {
            this.selectedHomeName = h.name();
            this.rebuildWidgets();
         }).bounds(col1X, y, col1W, ROW_HEIGHT).build();
         this.addRenderableWidget(homeBtn);
         y += 20;
      }

      // Fill remaining slots if fewer than HOMES_PER_PAGE
      for (int i = endIdx; i < startIdx + HOMES_PER_PAGE; i++) {
         y += 20;
      }

      // Page switcher buttons
      int pageBtnW = 28;
      Button prevPageBtn = Button.builder(Component.literal("<"), b -> {
         if (this.homeListPage > 0) {
            this.homeListPage--;
            this.rebuildWidgets();
         }
      }).bounds(col1X, y, pageBtnW, ROW_HEIGHT).build();
      this.addRenderableWidget(prevPageBtn);

      String pageStr = (this.homeListPage + 1) + "/" + totalPages;
      Button pageInd = Button.builder(Component.literal(pageStr), b -> {}).bounds(col1X + pageBtnW + 4, y, col1W - (pageBtnW + 4) * 2, ROW_HEIGHT).build();
      pageInd.active = false;
      this.addRenderableWidget(pageInd);

      Button nextPageBtn = Button.builder(Component.literal(">"), b -> {
         if (this.homeListPage < totalPages - 1) {
            this.homeListPage++;
            this.rebuildWidgets();
         }
      }).bounds(col1X + col1W - pageBtnW, y, pageBtnW, ROW_HEIGHT).build();
      this.addRenderableWidget(nextPageBtn);
      y += 22;

      // Assign to Girl button
      if (this.girl != null) {
         Button assignBtn = Button.builder(Component.literal(I18n.get("gui.sexmod.home.assign")), b -> {
            if (!this.selectedHomeName.isEmpty()) {
               ClientPlayNetworking.send(HomeActionPayload.assignHome(this.girl.getId(), this.selectedHomeName));
               this.girl.setAssignedHomeName(this.selectedHomeName);
               this.rebuildWidgets();
            }
         }).bounds(col1X, y, col1W, ROW_HEIGHT).build();
         assignBtn.active = !this.selectedHomeName.isEmpty();
         this.addRenderableWidget(assignBtn);
         y += 20;

         // Toggle stay at home button
         boolean staying = this.girl.isStayingAtHome();
         Button stayBtn = Button.builder(
            Component.literal(I18n.get("gui.sexmod.home.stay_toggle", staying ? I18n.get("gui.sexmod.home.stay_on") : I18n.get("gui.sexmod.home.stay_off"))),
            b -> {
               ClientPlayNetworking.send(HomeActionPayload.stayHome(this.girl.getId()));
               this.girl.setStayingAtHome(!this.girl.isStayingAtHome());
               this.rebuildWidgets();
            }
         ).bounds(col1X, y, col1W, ROW_HEIGHT).build();
         this.addRenderableWidget(stayBtn);
         y += 20;
      }

      // Show boundary particles button
      Button showBoundaryBtn = Button.builder(Component.literal(I18n.get("gui.sexmod.home.show_boundary")), b -> {
         if (!this.selectedHomeName.isEmpty()) {
            ClientPlayNetworking.send(HomeActionPayload.showBoundary(this.selectedHomeName));
         }
      }).bounds(col1X, y, (col1W - 4) / 2, ROW_HEIGHT).build();
      showBoundaryBtn.active = !this.selectedHomeName.isEmpty();
      this.addRenderableWidget(showBoundaryBtn);

      // Delete home button
      Button deleteBtn = Button.builder(Component.literal(I18n.get("gui.sexmod.home.delete")), b -> {
         if (!this.selectedHomeName.isEmpty()) {
            ClientPlayNetworking.send(HomeActionPayload.deleteHome(this.selectedHomeName));
            this.selectedHomeName = "";
            this.rebuildWidgets();
         }
      }).bounds(col1X + (col1W - 4) / 2 + 4, y, (col1W - 4) / 2, ROW_HEIGHT).build();
      deleteBtn.active = !this.selectedHomeName.isEmpty();
      this.addRenderableWidget(deleteBtn);

      // --- Right Column: Create New Home ---
      int y2 = panelTop + 40;

      // Home name input
      this.nameEdit = new EditBox(this.font, col2X, y2, col2W, ROW_HEIGHT, Component.literal("Name"));
      this.nameEdit.setMaxLength(32);
      this.nameEdit.setHint(Component.literal(I18n.get("gui.sexmod.home.name_hint")));
      this.nameEdit.setValue("Home " + (homes.size() + 1));
      this.addRenderableWidget(this.nameEdit);
      y2 += 22 + 10;

      // Coordinate inputs Point 1
      BlockPos playerPos = this.minecraft != null && this.minecraft.player != null ? this.minecraft.player.blockPosition() : BlockPos.ZERO;
      int coordW = (col2W - 8) / 3;

      this.x1Edit = new EditBox(this.font, col2X, y2, coordW, ROW_HEIGHT, Component.literal("X1"));
      this.x1Edit.setMaxLength(8);
      this.x1Edit.setValue(String.valueOf(playerPos.getX() - 8));
      this.addRenderableWidget(this.x1Edit);

      this.y1Edit = new EditBox(this.font, col2X + coordW + 4, y2, coordW, ROW_HEIGHT, Component.literal("Y1"));
      this.y1Edit.setMaxLength(8);
      this.y1Edit.setValue(String.valueOf(playerPos.getY() - 2));
      this.addRenderableWidget(this.y1Edit);

      this.z1Edit = new EditBox(this.font, col2X + (coordW + 4) * 2, y2, coordW, ROW_HEIGHT, Component.literal("Z1"));
      this.z1Edit.setMaxLength(8);
      this.z1Edit.setValue(String.valueOf(playerPos.getZ() - 8));
      this.addRenderableWidget(this.z1Edit);
      y2 += 20;

      Button pos1Btn = Button.builder(Component.literal(I18n.get("gui.sexmod.home.set_pos1")), b -> {
         if (this.minecraft != null && this.minecraft.player != null) {
            BlockPos p = this.minecraft.player.blockPosition();
            this.x1Edit.setValue(String.valueOf(p.getX()));
            this.y1Edit.setValue(String.valueOf(p.getY()));
            this.z1Edit.setValue(String.valueOf(p.getZ()));
         }
      }).bounds(col2X, y2, col2W, ROW_HEIGHT).build();
      this.addRenderableWidget(pos1Btn);
      y2 += 22 + 10;

      // Coordinate inputs Point 2
      this.x2Edit = new EditBox(this.font, col2X, y2, coordW, ROW_HEIGHT, Component.literal("X2"));
      this.x2Edit.setMaxLength(8);
      this.x2Edit.setValue(String.valueOf(playerPos.getX() + 8));
      this.addRenderableWidget(this.x2Edit);

      this.y2Edit = new EditBox(this.font, col2X + coordW + 4, y2, coordW, ROW_HEIGHT, Component.literal("Y2"));
      this.y2Edit.setMaxLength(8);
      this.y2Edit.setValue(String.valueOf(playerPos.getY() + 6));
      this.addRenderableWidget(this.y2Edit);

      this.z2Edit = new EditBox(this.font, col2X + (coordW + 4) * 2, y2, coordW, ROW_HEIGHT, Component.literal("Z2"));
      this.z2Edit.setMaxLength(8);
      this.z2Edit.setValue(String.valueOf(playerPos.getZ() + 8));
      this.addRenderableWidget(this.z2Edit);
      y2 += 20;

      Button pos2Btn = Button.builder(Component.literal(I18n.get("gui.sexmod.home.set_pos2")), b -> {
         if (this.minecraft != null && this.minecraft.player != null) {
            BlockPos p = this.minecraft.player.blockPosition();
            this.x2Edit.setValue(String.valueOf(p.getX()));
            this.y2Edit.setValue(String.valueOf(p.getY()));
            this.z2Edit.setValue(String.valueOf(p.getZ()));
         }
      }).bounds(col2X, y2, col2W, ROW_HEIGHT).build();
      this.addRenderableWidget(pos2Btn);
      y2 += 22;

      // Create Home button
      Button createBtn = Button.builder(Component.literal(I18n.get("gui.sexmod.home.create")), b -> {
         String name = this.nameEdit.getValue().trim();
         if (!name.isEmpty()) {
            try {
               int x1 = Integer.parseInt(this.x1Edit.getValue().trim());
               int y1 = Integer.parseInt(this.y1Edit.getValue().trim());
               int z1 = Integer.parseInt(this.z1Edit.getValue().trim());
               int x2 = Integer.parseInt(this.x2Edit.getValue().trim());
               int parsedY2 = Integer.parseInt(this.y2Edit.getValue().trim());
               int z2 = Integer.parseInt(this.z2Edit.getValue().trim());

               ClientPlayNetworking.send(HomeActionPayload.createHome(name, x1, y1, z1, x2, parsedY2, z2));
               this.selectedHomeName = name;
               this.nameEdit.setValue("Home " + (ClientHomeManager.getHomes().size() + 2));
               this.rebuildWidgets();
            } catch (NumberFormatException ignored) {
            }
         }
      }).bounds(col2X, y2, col2W, ROW_HEIGHT).build();
      this.addRenderableWidget(createBtn);

      // --- Bottom Row: Close Button ---
      int bottomY = panelTop + PANEL_HEIGHT - 24;
      Button closeBtn = Button.builder(Component.literal(I18n.get("settings.sexmod.done")), b -> this.onClose())
         .bounds(panelLeft + PADDING, bottomY, PANEL_WIDTH - PADDING * 2, ROW_HEIGHT).build();
      this.addRenderableWidget(closeBtn);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      int panelLeft = Math.max(10, (this.width - PANEL_WIDTH) / 2);
      int panelTop = Math.max(6, (this.height - PANEL_HEIGHT) / 2);

      // Dark translucent background plate
      context.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xD810121A);
      // Border outline
      context.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + 1, 0xFF4A4E69);
      context.fill(panelLeft, panelTop + PANEL_HEIGHT - 1, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xFF4A4E69);
      context.fill(panelLeft, panelTop, panelLeft + 1, panelTop + PANEL_HEIGHT, 0xFF4A4E69);
      context.fill(panelLeft + PANEL_WIDTH - 1, panelTop, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xFF4A4E69);

      // Title & Subtitle
      context.text(this.font, Component.literal(I18n.get("gui.sexmod.home.title")), panelLeft + PADDING, panelTop + 7, 0xFFE0E0E0);
      if (this.girl != null) {
         String sub = I18n.get("gui.sexmod.home.companion", this.girl.getName().getString());
         if (!this.girl.assignedHomeName().isEmpty()) {
            sub += " | " + I18n.get("gui.sexmod.home.assigned_to", this.girl.assignedHomeName());
         }
         if (this.girl.isStayingAtHome()) {
            sub += " §a[" + I18n.get("gui.sexmod.home.staying_badge") + "]";
         }
         context.text(this.font, Component.literal(sub), panelLeft + PADDING, panelTop + 18, 0xFFA0A0A0);
      }

      // Column Headers
      int col1X = panelLeft + PADDING;
      int col2X = panelLeft + PANEL_WIDTH / 2 + 10;
      context.text(this.font, Component.literal(I18n.get("gui.sexmod.home.list_title")), col1X, panelTop + 30, 0xFFE0E0E0);
      context.text(this.font, Component.literal(I18n.get("gui.sexmod.home.create_title")), col2X, panelTop + 30, 0xFFE0E0E0);

      // Section labels in column 2
      int y2 = panelTop + 40;
      y2 += 22 + 1;
      context.text(this.font, Component.literal(I18n.get("gui.sexmod.home.pos1_label")), col2X, y2, 0xFFAAAAAA);
      y2 += 10 + 20 + 22 + 1;
      context.text(this.font, Component.literal(I18n.get("gui.sexmod.home.pos2_label")), col2X, y2, 0xFFAAAAAA);

      super.extractRenderState(context, mouseX, mouseY, delta);
   }

   @Override
   public void onClose() {
      if (this.parentScreen != null && this.minecraft != null) {
         this.minecraft.gui.setScreen(this.parentScreen);
      } else {
         super.onClose();
      }
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      if (super.keyPressed(event)) {
         return true;
      }
      if (event.key() == InputConstants.KEY_ESCAPE) {
         this.onClose();
         return true;
      }
      return false;
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   public boolean isInGameUi() {
      return true;
   }
}
