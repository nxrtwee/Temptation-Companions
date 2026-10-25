package com.nxrtweetg.sexmod.client;

import com.nxrtweetg.sexmod.girls.scene.GirlCustomPhase;
import com.nxrtweetg.sexmod.girls.scene.GirlSceneAction;
import com.nxrtweetg.sexmod.girls.SceneGirlEntity;
import com.nxrtweetg.sexmod.girls.scene.GirlPayment;
import com.nxrtweetg.sexmod.network.GirlCommandPayload;
import com.nxrtweetg.sexmod.network.JennySceneInputPayload;
import com.nxrtweetg.sexmod.network.JennyStartInteractionPayload;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Recreation of the original girl menu (m_class414): action buttons hug the bottom-right corner with
 * the price stack drawn beside each one, while the universal commands slide out of the left edge.
 */
@Environment(EnvType.CLIENT)
public final class GirlInteractionScreen extends Screen {
   private static final ResourceLocation GIRL_INVENTORY = ResourceLocation.fromNamespaceAndPath("sexmod", "textures/gui/girlinventory.png");
   private static final int TEXTURE_SIZE = 256;
   /** Icon column in girlinventory.png for each command, in the original's order. */
   private static final int[] COMMAND_ICON_U = {64, 80, 47, 32, 96};
   private static final String[] COMMAND_KEYS = {
      "action.names.followme", "action.names.stopfollowme", "action.names.gohome", "action.names.setnewhome", "action.names.equipment"
   };
   /** How far each command button may grow on hover, from the original's width table. */
   private static final int[] COMMAND_EXPANDED_WIDTH = {50, 90, 50, 80, 60};
   private static final int ACTION_BUTTON_WIDTH = 100;
   private static final int ACTION_BUTTON_HEIGHT = 20;
   private static final int ACTION_BUTTON_SPACING = 30;
   private static final int ACTION_FIRST_BOTTOM_OFFSET = 70;
   private static final int ACTION_TOP_MARGIN = 20;
   private static final int PRICE_TEXT_BOTTOM_OFFSET = 52;
   /** Lifts the 16px icon so it sits centred on the one-line plate rather than hanging below it. */
   private static final int PRICE_ITEM_RISE = 4;
   private static final int COMMAND_BUTTON_X = 35;
   private static final int COMMAND_FIRST_Y = 70;
   private static final int COMMAND_BUTTON_SPACING = 30;
   private static final int COMMAND_BUTTON_HEIGHT = 20;
   private static final int PANEL_WIDTH = 32;
   private static final int PANEL_HEIGHT = 130;
   private static final int PANEL_Y = 60;
   /** Screen-space rows of the six gear stacks drawn in that frame, from the original. */
   private static final int[] EQUIPMENT_SLOT_Y = {68, 87, 109, 127, 146, 166};
   private static final EquipmentSlot[] EQUIPMENT_SLOTS = {
      EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
   };
   /** The original drew the price as a tooltip, which offsets it by +12/-12 from the anchor. */
   private static final int TOOLTIP_OFFSET = 12;
   /** A one-line vanilla tooltip is 8px tall, not font.lineHeight (m_class414.a). */
   private static final int TOOLTIP_LINE_HEIGHT = 8;
   /** The whole menu slides in over five ticks, then the commands unfold; from the original's m/n. */
   private static final float SLIDE_TICKS = 5.0F;

   private final int entityId;
   private final String girlName;
   private final boolean hasMaster;
   private final SceneGirlEntity girl;
   private final List<GirlCommandButton> commandButtons = new ArrayList<>();
   private final List<Button> actionButtons = new ArrayList<>();
   private SceneGirlEntity.InteractionSection currentSection;
   private int actionScrollOffset;
   private float slideMain;
   private float slideCommands;
   private boolean actionChosen;

   public GirlInteractionScreen(SceneGirlEntity girl) {
      super(girl.getName());
      this.girl = girl;
      this.entityId = girl.getId();
      this.girlName = girl.getName().getString();
      this.hasMaster = girl.trackedHasMaster();
   }

   /** The original menu draws straight over the world: no blur, no darkening. */
   @Override
   public void renderTransparentBackground(GuiGraphics context) {
   }

   @Override
   protected void init() {
      ClientPlayNetworking.send(new GirlCommandPayload(this.entityId, "menu_open"));
      this.commandButtons.clear();
      this.actionButtons.clear();
      List<GirlInteractionScreen.MenuEntry> entries = this.visibleEntries();
      int y = this.height - ACTION_FIRST_BOTTOM_OFFSET;

      for (GirlInteractionScreen.MenuEntry entry : entries) {
         Button button = Button.builder(Component.literal(entry.label()), press -> entry.onClick().run())
            .bounds(this.actionButtonX(), y, ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT)
            .build();
         button.visible = false;
         this.actionButtons.add(this.addRenderableWidget(button));
         y -= ACTION_BUTTON_SPACING;
      }

      if (this.girl.hasCompanionCommands()) {
         int commandY = COMMAND_FIRST_Y;

         for (int i = 0; i < COMMAND_KEYS.length; i++) {
            // The original showed exactly one of follow me / stop follow me, based on the master state.
            if (i == 0 && this.hasMaster || i == 1 && !this.hasMaster) {
               continue;
            }

            String key = COMMAND_KEYS[i];
            Runnable action;
            if ("action.names.setnewhome".equals(key)) {
               action = () -> {
                  if (Minecraft.getInstance() != null) {
                     Minecraft.getInstance().setScreen(new GirlHomeSettingsScreen(this.girl, this));
                  }
               };
            } else {
               action = () -> this.sendCommand(key);
            }

            GirlCommandButton button = new GirlCommandButton(
               COMMAND_BUTTON_X,
               commandY,
               COMMAND_BUTTON_HEIGHT,
               Component.literal(I18n.get(key)),
               COMMAND_ICON_U[i],
               COMMAND_EXPANDED_WIDTH[i],
               action
            );
            button.visible = false;
            this.commandButtons.add(this.addRenderableWidget(button));
            commandY += COMMAND_BUTTON_SPACING;
         }
      }
   }

   @Override
   public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
      this.slideMain = Math.min(1.0F, this.slideMain + delta / SLIDE_TICKS);
      if (this.slideMain >= 1.0F) {
         this.slideCommands = Math.min(1.0F, this.slideCommands + delta / SLIDE_TICKS);
      }

      int actionX = this.actionButtonX();

      for (Button button : this.actionButtons) {
         button.setX(actionX);
         button.visible = this.slideMain > 0.0F && actionX < this.width;
      }

      if (this.girl.hasCompanionCommands()) {
         for (GirlCommandButton button : this.commandButtons) {
            button.setSlide(this.slideCommands);
            button.visible = this.slideCommands > 0.0F && (button.getX() + button.getWidth() > 0);
         }

         // Order matters: 26.2 auto-layers each element above whatever it overlaps, so the frame has to
         // be submitted before the stacks or it would bury them.
         if (this.slideCommands > 0.0F) {
            context.blit(
               RenderPipelines.GUI_TEXTURED,
               GIRL_INVENTORY,
               Mth.lerpInt(this.slideMain, -30, 120) - 113,
               PANEL_Y,
               0,
               0,
               PANEL_WIDTH,
               PANEL_HEIGHT,
               TEXTURE_SIZE,
               TEXTURE_SIZE
            );
         }

         this.renderEquipment(context);
      }

      // Before the widgets: the original pushed the price plates to zLevel -300 so the buttons sit on
      // top of them, and in 26.2 the equivalent is simply submitting them first.
      this.renderPrices(context);
      super.render(context, mouseX, mouseY, delta);
   }

   /**
    * Her gear rides in the frame on the left, one stack per typed slot. The original read these off
    * six data parameters (Fighter.L/R/X/T/U/W); vanilla now syncs the same stacks for us.
    */
   private void renderEquipment(GuiGraphics context) {
      int x = Mth.lerpInt(this.slideMain, -30, 120) - 105;

      for (int slot = 0; slot < EQUIPMENT_SLOT_Y.length; slot++) {
         ItemStack stack = this.girl.getItemBySlot(EQUIPMENT_SLOTS[slot]);
         if (!stack.isEmpty()) {
            context.renderItem(stack, x, EQUIPMENT_SLOT_Y[slot]);
         }
      }
   }

   /** Prices sit left of each action button on their own tooltip backdrop, as in the original. */
   private void renderPrices(GuiGraphics context) {
      if (this.slideCommands <= 0.0F) {
         return;
      }

      List<GirlInteractionScreen.MenuEntry> entries = this.currentEntries();
      int textX = this.width - Mth.lerpInt(this.slideCommands, 115, 161);
      int itemX = this.width - Mth.lerpInt(this.slideCommands, 91, 137);
      int textY = this.height - PRICE_TEXT_BOTTOM_OFFSET;
      for (GirlInteractionScreen.MenuEntry entry : this.visibleEntries(entries)) {
         ItemStack price = entry.price();
         if (!price.isEmpty()) {
            // The trailing spaces are the original's: they widen the backdrop so it also covers the
            // item stack, which is what makes the count and the icon read as one plate.
            Component label = Component.literal(price.getCount() + "x    ");
            int labelX = textX + TOOLTIP_OFFSET;
            int labelY = textY - TOOLTIP_OFFSET;
            TooltipRenderUtil.renderTooltipBackground(context, labelX, labelY, this.font.width(label), TOOLTIP_LINE_HEIGHT, null);
            context.drawString(this.font, label, labelX, labelY, 0xFFFFFFFF);
            context.renderItem(price, itemX, labelY - PRICE_ITEM_RISE);
         }

         textY -= ACTION_BUTTON_SPACING;
      }
   }

   private int actionButtonX() {
      return this.width - Mth.lerpInt(this.slideMain, -30, 120);
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   /**
    * Keeps the original slide-out layout. The top level is flat as the original's was - one row per
    * thing she answers to - except for the one row that opens her whole scene roster.
    */
   private List<GirlInteractionScreen.MenuEntry> currentEntries() {
      List<GirlInteractionScreen.MenuEntry> entries = new ArrayList<>();
      if (this.currentSection != null) {
         for (SceneGirlEntity.InteractionOption option : this.currentSection.options()) {
            entries.add(this.actionEntry(option));
         }

         entries.add(this.backEntry());
      } else {
         for (SceneGirlEntity.InteractionSection section : this.girl.interactionSections()) {
            entries.add(section.directOption().map(this::actionEntry).orElseGet(() -> this.submenuEntry(section)));
         }
      }

      return entries;
   }

   private List<GirlInteractionScreen.MenuEntry> visibleEntries() {
      return this.visibleEntries(this.currentEntries());
   }

   private List<GirlInteractionScreen.MenuEntry> visibleEntries(List<GirlInteractionScreen.MenuEntry> entries) {
      int visibleCount = Math.min(entries.size(), this.maxVisibleActionButtons());
      int maxOffset = Math.max(0, entries.size() - visibleCount);
      this.actionScrollOffset = Mth.clamp(this.actionScrollOffset, 0, maxOffset);
      return entries.subList(this.actionScrollOffset, this.actionScrollOffset + visibleCount);
   }

   private int maxVisibleActionButtons() {
      return Math.max(1, (this.height - ACTION_FIRST_BOTTOM_OFFSET - ACTION_TOP_MARGIN) / ACTION_BUTTON_SPACING + 1);
   }

   private GirlInteractionScreen.MenuEntry actionEntry(SceneGirlEntity.InteractionOption option) {
      return new GirlInteractionScreen.MenuEntry(I18n.get(option.labelKey()), option.price(), () -> this.sendAction(option.actionId(), option.price()));
   }

   private GirlInteractionScreen.MenuEntry submenuEntry(SceneGirlEntity.InteractionSection section) {
      return new GirlInteractionScreen.MenuEntry(I18n.get(section.labelKey()), section.pricePreview(), () -> {
         this.currentSection = section;
         this.actionScrollOffset = 0;
         this.rebuildWidgets();
      });
   }

   private GirlInteractionScreen.MenuEntry backEntry() {
      return new GirlInteractionScreen.MenuEntry("Back", ItemStack.EMPTY, () -> {
         this.currentSection = null;
         this.actionScrollOffset = 0;
         this.rebuildWidgets();
      });
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      List<GirlInteractionScreen.MenuEntry> entries = this.currentEntries();
      int hiddenEntries = entries.size() - this.maxVisibleActionButtons();
      if (hiddenEntries <= 0 || verticalAmount == 0.0) {
         return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
      }

      int nextOffset = Mth.clamp(this.actionScrollOffset - (int)Math.signum(verticalAmount), 0, hiddenEntries);
      if (nextOffset != this.actionScrollOffset) {
         this.actionScrollOffset = nextOffset;
         this.rebuildWidgets();
         return true;
      }

      return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   private static ItemStack priceStack(GirlSceneAction action) {
      return new ItemStack(action.paymentItem().item(), action.paymentAmount());
   }

   private void sendCommand(String commandKey) {
      this.actionChosen = true;
      ClientPlayNetworking.send(new GirlCommandPayload(this.entityId, commandKey));
      this.onClose();
   }

   private void sendAction(String actionId, ItemStack price) {
      this.actionChosen = true;
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null && !GirlPayment.canAfford(player, price)) {
         player.displayClientMessage(Component.literal("<" + this.girlName + "> " + I18n.get(this.girl.dialogueKey("cannotafford"))), false);
         player.level()
            .playLocalSound(
               player.blockPosition(),
               SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("sexmod", this.girl.voice("sadoh", 2) + "1")),
               SoundSource.NEUTRAL,
               1.0F,
               1.0F,
               false
            );
         this.onClose();
         return;
      }

      ClientPlayNetworking.send(new JennyStartInteractionPayload(this.entityId, actionId));
      this.onClose();
   }

   private int menuKeepAliveTicks = 0;

   @Override
   public void tick() {
      super.tick();
      if (this.minecraft != null && this.minecraft.player != null) {
         this.girl.getNavigation().stop();
         this.girl.setDeltaMovement(this.girl.getDeltaMovement().multiply(0.0, 1.0, 0.0));
         this.girl.getLookControl().setLookAt(this.minecraft.player, 30.0F, 30.0F);
         if (++this.menuKeepAliveTicks >= 20) {
            this.menuKeepAliveTicks = 0;
            ClientPlayNetworking.send(new GirlCommandPayload(this.entityId, "menu_open"));
         }
      }
   }

   @Override
   public void onClose() {
      ClientPlayNetworking.send(new GirlCommandPayload(this.entityId, "menu_close"));
      if (!this.actionChosen) {
         if (this.girl instanceof com.nxrtweetg.sexmod.girls.allie.AllieEntity) {
            ClientPlayNetworking.send(new GirlCommandPayload(this.entityId, "allie_dismiss_free"));
         } else if (!this.girl.customActionId().isEmpty() || this.girl.customPhase() == GirlCustomPhase.WAITING) {
            ClientPlayNetworking.send(new JennySceneInputPayload(this.entityId, "reset"));
         }
      }
      super.onClose();
   }

   private record MenuEntry(String label, ItemStack price, Runnable onClick) {
   }
}
