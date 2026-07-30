package at.redi2go.photonics.client.config.screen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToggleableListScreen extends Screen {
   private final Screen parent;
   private final ToggleableListScreen.Model model;
   private final Tooltip buttonTooltip;
   private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 61, 33);
   private ToggleableListScreen.ToggleableList list;

   public ToggleableListScreen(Screen parent, String headline, ToggleableListScreen.Model model, @Nullable Tooltip buttonTooltip) {
      super(Component.nullToEmpty(headline));
      this.parent = parent;
      this.model = model;
      this.buttonTooltip = buttonTooltip;
   }

   protected void init() {
      super.init();
      EditBox searchBox = new EditBox(this.font, 200, 20, Component.nullToEmpty("Search"));
      searchBox.setResponder(searchText -> {
         this.model.setFilter(searchText);
         this.list.init(this.buttonTooltip);
      });
      LinearLayout searchLinearLayout = (LinearLayout)this.layout.addToHeader(LinearLayout.vertical().spacing(8));
      searchLinearLayout.addChild(new StringWidget(this.getTitle(), this.font), LayoutSettings::alignHorizontallyCenter);
      searchLinearLayout.addChild(searchBox, LayoutSettings::alignHorizontallyCenter);
      this.list = new ToggleableListScreen.ToggleableList(Minecraft.getInstance(), this);
      this.list.init(this.buttonTooltip);
      this.layout.addToContents(this.list);
      this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).width(200).build());
      this.layout.visitWidgets(x$0 -> this.addRenderableWidget(x$0));
      this.layout.arrangeElements();
   }

   protected void repositionElements() {
      this.layout.arrangeElements();
      this.list.updateSize(this.width, this.layout);
   }

   public void render(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
      super.render(drawContext, mouseX, mouseY, delta);
   }

   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.setScreen(this.parent);
      }
   }

   public abstract static class Entry extends net.minecraft.client.gui.components.ContainerObjectSelectionList.Entry<ToggleableListScreen.Entry> {
      abstract void refreshEntry();
   }

   public static class Model {
      private final List<ToggleableListScreen.ModelEntry> content;
      private final List<ToggleableListScreen.ModelEntry> filteredContent = new ArrayList<>();

      public Model(List<ToggleableListScreen.ModelEntry> content) {
         this.content = content;
         this.setFilter("");
      }

      public void setFilter(String filter) {
         List<String> filterTokens = Arrays.stream(filter.split(" ")).map(String::toLowerCase).toList();
         this.filteredContent.clear();
         this.content.stream().filter(entry -> {
            String displayValue = entry.getDisplayValue().toLowerCase();

            for (String filterToken : filterTokens) {
               if (!displayValue.contains(filterToken)) {
                  return false;
               }
            }

            return true;
         }).forEach(this.filteredContent::add);
      }

      public List<ToggleableListScreen.ModelEntry> getFilteredContent() {
         return this.filteredContent;
      }
   }

   public abstract static class ModelEntry {
      public abstract String getDisplayValue();

      public abstract boolean isEnabled();

      public abstract void setEnabled(boolean var1);
   }

   public class ToggleableList extends ContainerObjectSelectionList<ToggleableListScreen.Entry> {
      public ToggleableList(Minecraft minecraft, ToggleableListScreen toggleableListScreen) {
         super(minecraft, toggleableListScreen.width, toggleableListScreen.layout.getContentHeight(), toggleableListScreen.layout.getHeaderHeight(), 20);
      }

      public void init(@Nullable Tooltip tooltip) {
         this.clearEntries();

         for (ToggleableListScreen.ModelEntry entry : ToggleableListScreen.this.model.getFilteredContent()) {
            this.addEntry(new ToggleableListScreen.ToggleableList.ListEntry(entry, tooltip));
         }
      }

      public class ListEntry extends ToggleableListScreen.Entry {
         private final ToggleableListScreen.ModelEntry model;
         private final Button resetButton;

         public ListEntry(ToggleableListScreen.ModelEntry model, @Nullable Tooltip tooltip) {
            this.model = model;
            this.resetButton = Button.builder(Component.nullToEmpty(model.isEnabled() ? "ON" : "OFF"), button -> {
               model.setEnabled(!model.isEnabled());
               this.refreshEntry();
            }).tooltip(tooltip).bounds(0, 0, 75, 20).build();
         }

         @Override
         void refreshEntry() {
            this.resetButton.setMessage(Component.nullToEmpty(this.model.isEnabled() ? "ON" : "OFF"));
         }

         @NotNull
         public List<? extends NarratableEntry> narratables() {
            return List.of();
         }

         @NotNull
         public List<? extends GuiEventListener> children() {
            return List.of(this.resetButton);
         }

         public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            int k = ToggleableList.this.getScrollbarPosition() - this.resetButton.getWidth() - 10;
            int l = top - 2;
            this.resetButton.setPosition(k, l);
            this.resetButton.render(guiGraphics, mouseX, mouseY, partialTick);
            guiGraphics.drawString(ToggleableList.this.minecraft.font, this.model.getDisplayValue(), left, top + height / 2 - 4, -1);
         }
      }
   }
}
