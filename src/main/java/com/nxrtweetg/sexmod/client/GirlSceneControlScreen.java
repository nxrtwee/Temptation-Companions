package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.girls.scene.GirlScenePhase;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * The in-scene editor. Every scene exposes the full set of controls -- camera, placement, both body
 * sizes -- so the panel is taller than the screen in most scenes and scrolls. Each value can be
 * nudged with the buttons or typed in directly.
 */
@Environment(EnvType.CLIENT)
public final class GirlSceneControlScreen extends Screen {
   private static final int PANEL_WIDTH = 316;
   private static final int PANEL_MARGIN = 12;
   private static final int PADDING = 10;
   private static final int ROW_HEIGHT = 18;
   private static final int ROW_GAP = 3;
   private static final int COLUMN_GAP = 8;
   private static final int HEADER_HEIGHT = 52;
   private static final int SCROLL_STEP = 18;
   private static final int LABEL_WIDTH = 46;
   private static final int FIELD_WIDTH = 42;
   private static final int STEP_WIDTH = 16;

   private final List<Row> rows = new ArrayList<>();
   private int scroll;
   private int contentHeight;

   public GirlSceneControlScreen() {
      super(Component.literal("Scene Controls"));
   }

   @Override
   protected void init() {
      this.clearWidgets();
      this.rows.clear();
      Optional<SceneGirlEntity> owned = this.currentGirl();
      if (owned.isEmpty()) {
         this.onClose();
         return;
      }

      SceneGirlEntity girl = owned.get();
      if (girl.trackedActiveAction().isEmpty() && !girl.shouldRenderPlayerTracked()) {
         this.onClose();
         return;
      }

      Minecraft client = this.minecraft;
      this.rows.add(Row.wide(girl.isSceneFrozenTracked() ? "Resume" : "Freeze", () -> {
         GirlSceneInputController.sendSceneControlInput(client, "toggle_freeze");
         this.rebuildWidgets();
      }));

      this.rows.add(Row.header("Camera"));
      this.addCameraRow("Dist", GirlSceneCameraConfig.Field.DISTANCE, 0.1);
      this.addCameraRow("Height", GirlSceneCameraConfig.Field.VERTICAL, 0.05);
      this.addCameraRow("Side", GirlSceneCameraConfig.Field.LATERAL, 0.05);


      this.rows.add(Row.header("Placement"));
      this.rows.add(Row.value("Fwd", girl::trackedSceneTuneForward, value -> this.sendPlacement("forward", value), "scene_back", "scene_forward"));
      this.rows.add(Row.value("Side", girl::trackedSceneTuneSide, value -> this.sendPlacement("side", value), "scene_left", "scene_right"));
      this.rows.add(Row.value("Height", girl::trackedSceneTuneVertical, value -> this.sendPlacement("vertical", value), "scene_down", "scene_up"));

      this.rows.add(Row.header("Partner"));
      this.addCameraRow("Height", GirlSceneCameraConfig.Field.HELPER_VERTICAL, 0.02);
      this.addCameraRow("Fwd", GirlSceneCameraConfig.Field.HELPER_FORWARD, 0.02);
      this.addCameraRow("Side", GirlSceneCameraConfig.Field.HELPER_SIDE, 0.02);

      this.rows.add(Row.header("Size"));
      this.addCameraRow("Girl", GirlSceneCameraConfig.Field.GIRL_SCALE, 0.02);
      this.addCameraRow("Partner", GirlSceneCameraConfig.Field.HELPER_SCALE, 0.02);
      this.rows.add(
         Row.local(
            "Breasts", () -> GirlSceneCameraTuner.breastScale(client), value -> GirlSceneCameraTuner.setBreastScale(client, value), 0.05
         )
      );
      this.rows.add(
         Row.local("Booty", () -> GirlSceneCameraTuner.bootyScale(client), value -> GirlSceneCameraTuner.setBootyScale(client, value), 0.05)
      );

      this.rows.add(Row.wide("Reset size", () -> {
         GirlSceneCameraTuner.resetScales(client);
         this.rebuildWidgets();
      }));
      this.rows.add(Row.wide("Close [N]", this::onClose));

      this.layOutRows();
   }

   private void addCameraRow(String label, GirlSceneCameraConfig.Field field, double step) {
      Minecraft client = this.minecraft;
      this.rows
         .add(
            Row.local(
               label,
               () -> GirlSceneCameraTuner.cameraField(client, field),
               value -> GirlSceneCameraTuner.setCameraField(client, field, value),
               step
            )
         );
   }

   private void sendPlacement(String axis, double value) {
      GirlSceneInputController.sendSceneTuneInput(this.minecraft, "scene_set:" + axis + ":" + value);
   }

   /**
    * Two columns of rows, with headers and wide buttons spanning both. The content is clamped to the
    * window and scrolls, since with every scene tuneable the list no longer fits on screen.
    */
   private void layOutRows() {
      int contentLeft = this.width - PANEL_WIDTH - PANEL_MARGIN + PADDING;
      int contentWidth = PANEL_WIDTH - PADDING * 2;
      int columnWidth = (contentWidth - COLUMN_GAP) / 2;
      int y = 0;
      boolean rightColumn = false;

      for (Row row : this.rows) {
         if (row.spansBothColumns() && rightColumn) {
            y += ROW_HEIGHT + ROW_GAP;
            rightColumn = false;
         }

         int rowY = PANEL_MARGIN + HEADER_HEIGHT + y - this.scroll;
         int x = contentLeft + (rightColumn ? columnWidth + COLUMN_GAP : 0);
         row.place(this, x, rowY, row.spansBothColumns() ? contentWidth : columnWidth);
         if (row.spansBothColumns()) {
            y += ROW_HEIGHT + ROW_GAP;
         } else {
            rightColumn = !rightColumn;
            if (!rightColumn) {
               y += ROW_HEIGHT + ROW_GAP;
            }
         }
      }

      if (rightColumn) {
         y += ROW_HEIGHT + ROW_GAP;
      }

      this.contentHeight = y;
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      int maxScroll = Math.max(0, PANEL_MARGIN + HEADER_HEIGHT + this.contentHeight + PADDING - this.height);
      if (maxScroll == 0) {
         return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
      }

      int updated = Mth.clamp(this.scroll - (int)(scrollY * SCROLL_STEP), 0, maxScroll);
      if (updated == this.scroll) {
         return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
      }

      this.scroll = updated;
      this.rebuildWidgets();
      return true;
   }

   @Override
   public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
      Optional<SceneGirlEntity> owned = this.currentGirl();
      if (owned.isEmpty()) {
         this.onClose();
         return;
      }

      SceneGirlEntity girl = owned.get();
      GirlSceneAction action = girl.trackedActiveAction().orElse(null);
      if (action == null && !girl.shouldRenderPlayerTracked()) {
         this.onClose();
         return;
      }

      int panelLeft = this.width - PANEL_WIDTH - PANEL_MARGIN;
      int panelTop = PANEL_MARGIN;
      int panelHeight = Math.min(this.height - PANEL_MARGIN * 2, HEADER_HEIGHT + this.contentHeight + PADDING);
      int textLeft = panelLeft + PADDING;

      context.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + panelHeight, -1072689130);
      context.drawString(this.font, Component.literal("Scene Controls"), textLeft, panelTop + PADDING, 16777215);
      context.drawString(this.font, Component.literal(action != null ? actionLabel(action) : "Head Pat"), textLeft, panelTop + 24, -4733488);
      context.drawString(
         this.font,
         Component.literal(
            (girl.isSceneFrozenTracked() ? "Frozen" : "Live") + "  -  " + phaseLabel(cameraConfigPhase(girl.trackedScenePhase())) + " profile  -  " + girl.characterId()
         ),
         textLeft,
         panelTop + 36,
         -3165574
      );
      super.render(context, mouseX, mouseY, delta);
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
   }

   private Optional<SceneGirlEntity> currentGirl() {
      Minecraft client = this.minecraft;
      return client != null && client.player != null && client.level != null
         ? GirlSceneInputController.findOwnedGirl(client.player)
         : Optional.empty();
   }

   private static String actionLabel(GirlSceneAction action) {
      return switch (action) {
         case BLOWJOB -> "Blowjob / Standard";
         case KOBOLD_BLOWJOB -> "Blowjob / Kobold";
         case DEEPTHROAT -> "Blowjob / Standard 2";
         case FACEFUCK -> "Blowjob / Facefuck";
         case BOOBJOB -> "Boobjob";
         case TOUCH_BOOBS -> "Boobjob / Touch Boobs";
         case DOGGY -> "Sex / Standard";
         case PRONE_DOGGY -> "Sex / Prone Doggy";
         case ANAL -> "Sex / Anal";
         case ANAL_ALT -> "Sex / Anal 2";
         case KOBOLD_ANAL -> "Sex / Kobold Anal";
         case SITTING -> "Sex / Sitting";
         case SITTING_ALT -> "Sex / Sitting 2";
         case MISSIONARY -> "Sex / Missionary";
         case MATING_PRESS -> "Sex / Mating Press";
         case REVERSE_COWGIRL -> "Sex / Reverse Cowgirl";
         case COWGIRL -> "Sex / Cowgirl";
         case BEE_SEX -> "Sex / Bee";
         case NELSON -> "Nelson";
         case THREESOME -> "Threesome";
         case STRIP -> "Strip";
         case HEAD_PAT -> "Head Pat";
      };
   }

   private static GirlScenePhase cameraConfigPhase(GirlScenePhase phase) {
      return switch (phase) {
         case PAYMENT -> GirlScenePhase.PAYMENT;
         case INTRO -> GirlScenePhase.INTRO;
         default -> GirlScenePhase.ACTIVE;
      };
   }

   private static String phaseLabel(GirlScenePhase phase) {
      return switch (phase) {
         case PAYMENT -> "Payment";
         case INTRO -> "Intro";
         default -> "Main";
      };
   }

   /** One line of the editor: a section header, a full-width button, or a labelled value field. */
   private sealed interface Row {
      static Row header(String label) {
         return new Header(label);
      }

      static Row wide(String label, Runnable action) {
         return new Wide(label, action);
      }

      /** A value the client owns outright, so it can be read back and written directly. */
      static Row local(String label, DoubleSupplier getter, DoubleConsumer setter, double step) {
         return new Local(label, getter, setter, step);
      }

      /** A value the entity owns: nudges go through the existing input ids, typing through a setter. */
      static Row value(String label, DoubleSupplier getter, DoubleConsumer setter, String minusInputId, String plusInputId) {
         return new Remote(label, getter, setter, minusInputId, plusInputId);
      }

      boolean spansBothColumns();

      void place(GirlSceneControlScreen screen, int x, int y, int width);

      /** Lays out "label [value] [-] [+]" and wires the box to commit on focus loss or Enter. */
      private static void placeValueRow(
         GirlSceneControlScreen screen, int x, int y, String label, DoubleSupplier getter, DoubleConsumer setter, Runnable minus, Runnable plus
      ) {
         EditBox box = new EditBox(screen.font, x + LABEL_WIDTH, y, FIELD_WIDTH, ROW_HEIGHT, Component.literal(label));
         box.setMaxLength(9);
         box.setValue(String.format("%.2f", getter.getAsDouble()));
         box.setResponder(text -> {
            try {
               double parsed = Double.parseDouble(text.trim());
               if (Double.isFinite(parsed)) {
                  setter.accept(parsed);
               }
            } catch (NumberFormatException ignored) {
               // half-typed input such as "-" or "0." is not an error, just not a number yet
            }
         });
         screen.addRenderableWidget(box);

         int stepsX = x + LABEL_WIDTH + FIELD_WIDTH + 2;
         screen.addRenderableWidget(Button.builder(Component.literal("-"), b -> {
            minus.run();
            screen.rebuildWidgets();
         }).bounds(stepsX, y, STEP_WIDTH, ROW_HEIGHT).build());
         screen.addRenderableWidget(Button.builder(Component.literal("+"), b -> {
            plus.run();
            screen.rebuildWidgets();
         }).bounds(stepsX + STEP_WIDTH + 2, y, STEP_WIDTH, ROW_HEIGHT).build());

         Button labelButton = Button.builder(Component.literal(label), b -> {
         }).bounds(x, y, LABEL_WIDTH - 2, ROW_HEIGHT).build();
         labelButton.active = false;
         screen.addRenderableWidget(labelButton);
      }

      record Header(String label) implements Row {
         @Override
         public boolean spansBothColumns() {
            return true;
         }

         @Override
         public void place(GirlSceneControlScreen screen, int x, int y, int width) {
            Button button = Button.builder(Component.literal(this.label), b -> {
            }).bounds(x, y, width, ROW_HEIGHT).build();
            button.active = false;
            screen.addRenderableWidget(button);
         }
      }

      record Wide(String label, Runnable action) implements Row {
         @Override
         public boolean spansBothColumns() {
            return true;
         }

         @Override
         public void place(GirlSceneControlScreen screen, int x, int y, int width) {
            screen.addRenderableWidget(Button.builder(Component.literal(this.label), b -> this.action.run()).bounds(x, y, width, ROW_HEIGHT).build());
         }
      }

      record Local(String label, DoubleSupplier getter, DoubleConsumer setter, double step) implements Row {
         @Override
         public boolean spansBothColumns() {
            return false;
         }

         @Override
         public void place(GirlSceneControlScreen screen, int x, int y, int width) {
            Row.placeValueRow(
               screen,
               x,
               y,
               this.label,
               this.getter,
               this.setter,
               () -> this.setter.accept(this.getter.getAsDouble() - this.step),
               () -> this.setter.accept(this.getter.getAsDouble() + this.step)
            );
         }
      }

      record Remote(String label, DoubleSupplier getter, DoubleConsumer setter, String minusInputId, String plusInputId) implements Row {
         @Override
         public boolean spansBothColumns() {
            return false;
         }

         @Override
         public void place(GirlSceneControlScreen screen, int x, int y, int width) {
            Row.placeValueRow(
               screen,
               x,
               y,
               this.label,
               this.getter,
               this.setter,
               () -> GirlSceneInputController.sendSceneTuneInput(screen.minecraft, this.minusInputId),
               () -> GirlSceneInputController.sendSceneTuneInput(screen.minecraft, this.plusInputId)
            );
         }
      }
   }
}
