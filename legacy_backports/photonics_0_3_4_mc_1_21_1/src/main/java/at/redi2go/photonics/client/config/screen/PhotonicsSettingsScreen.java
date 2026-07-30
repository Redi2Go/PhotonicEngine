package at.redi2go.photonics.client.config.screen;

import at.redi2go.photonics.client.Photonics;
import at.redi2go.photonics.client.SchematicExporter;
import at.redi2go.photonics.client.config.PhotonicsConfig;
import com.mojang.blaze3d.platform.InputConstants;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class PhotonicsSettingsScreen extends Screen {
   private static final ToggleableListScreen.Model VOXELIZED_BLOCK_MODEL = new ToggleableListScreen.Model(
      BuiltInRegistries.BLOCK
         .stream()
         .filter(block -> {
            Set<Block> blacklistedBlocks = Set.of(Blocks.AIR, Blocks.WATER, Blocks.LAVA);
            return !blacklistedBlocks.contains(block);
         })
         .sorted(Comparator.comparing(block -> block.getName().getString()))
         .map(PhotonicsSettingsScreen.BlockModel3DEntry::new)
         .collect(Collectors.toUnmodifiableList())
   );
   private final ToggleableListScreen.Model tracedBlocksModel;
   private final Screen parent;
   private SchematicExporter schematicExporter = null;
   private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 61, 33);

   protected PhotonicsSettingsScreen(Screen parent) {
      super(Component.nullToEmpty("Photonics Client Settings"));
      this.parent = parent;
      this.tracedBlocksModel = new ToggleableListScreen.Model(
         PhotonicsConfig.getLightList()
            .keySet()
            .stream()
            .sorted(Comparator.comparing(b -> b.getName().getString()))
            .map(PhotonicsSettingsScreen.TracedLightBlockEntry::new)
            .collect(Collectors.toUnmodifiableList())
      );
      PhotonicsConfig.prepareModify();
   }

   protected void init() {
      super.init();
      List<PhotonicsSettingsScreen.PButton> buttons = new ArrayList<>();
      buttons.add(
         new PhotonicsSettingsScreen.PButton(
            "Generate schematics.zip",
            w -> this.exportSchematics(),
            "Exports all block states to the root\nMinecraft folder (schematics.zip). Only works in-game!",
            () -> Minecraft.getInstance().level != null
         )
      );
      buttons.add(new PhotonicsSettingsScreen.PButton("MultiThreading: " + (PhotonicsConfig.isMultiThreadingEnabled() ? "On" : "Off"), w -> {
         PhotonicsConfig.setMultiThreadingEnabled(!PhotonicsConfig.isMultiThreadingEnabled());
         w.setMessage(Component.literal("MultiThreading: " + (PhotonicsConfig.isMultiThreadingEnabled() ? "On" : "Off")));
      }, "Turns MultiThreading on or off; MultiThreading is considerably faster, but can cause bugs.", () -> true));
      buttons.add(new PhotonicsSettingsScreen.PButton("Enable 3D Blocks: " + (PhotonicsConfig.isVoxelizedBlocksEnabled() ? "On" : "Off"), w -> {
         PhotonicsConfig.setVoxelizedBlocksEnabled(!PhotonicsConfig.isVoxelizedBlocksEnabled());
         w.setMessage(Component.literal("Enable 3D Blocks: " + (PhotonicsConfig.isVoxelizedBlocksEnabled() ? "On" : "Off")));
      }, "A global toggle for 3D Blocks", () -> true));
      ToggleableListScreen volumetricRenderedBlocks = new ToggleableListScreen(this, "3D Blocks", VOXELIZED_BLOCK_MODEL, null);
      buttons.add(
         new PhotonicsSettingsScreen.PButton(
            "3D Blocks",
            w -> this.minecraft.setScreen(volumetricRenderedBlocks),
            "Configures, whether a specific block should be rendered as a 3D block",
            () -> true
         )
      );
      ToggleableListScreen tracedBlocks = new ToggleableListScreen(
         this, "Raytraced Block Lights", this.tracedBlocksModel, Tooltip.create(Component.literal("Shift click to reset to default"))
      );
      buttons.add(
         new PhotonicsSettingsScreen.PButton(
            "Raytraced Lights", w -> this.minecraft.setScreen(tracedBlocks), "Configures, whether a specific block should emit ray-traced light", () -> true
         )
      );
      LinearLayout linearLayout = (LinearLayout)this.layout.addToHeader(LinearLayout.vertical().spacing(8));
      linearLayout.addChild(new StringWidget(Component.nullToEmpty("Photonics Mod settings"), this.font), LayoutSettings::alignHorizontallyCenter);
      GridLayout gridLayout = new GridLayout();
      gridLayout.defaultCellSetting().paddingHorizontal(4).paddingBottom(4).alignHorizontallyCenter();
      RowHelper rowHelper = gridLayout.createRowHelper(2);
      int midX = this.width / 2;
      int i = 0;

      for (PhotonicsSettingsScreen.PButton button : buttons) {
         int x = i % 2 * 220 + midX - 210;
         int y = (i / 2 + 1) * 30;
         button.widget = Button.builder(Component.nullToEmpty(button.text), button.pressAction)
            .pos(x, y)
            .size(200, 20)
            .tooltip(Tooltip.create(Component.nullToEmpty(button.toolTip)))
            .build();
         button.widget.active = button.active.getAsBoolean();
         rowHelper.addChild(button.widget);
         i++;
      }

      this.layout.addToContents(gridLayout);
      this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, buttonx -> this.onClose()).width(200).build());
      PhotonicsSettingsScreen var13 = this;
      this.layout.visitWidgets(x$0 -> var13.addRenderableWidget(x$0));
      this.layout.arrangeElements();
   }

   public void render(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
      super.render(drawContext, mouseX, mouseY, delta);
   }

   public void exportSchematics() {
      if (this.schematicExporter == null) {
         try {
            this.schematicExporter = new SchematicExporter(new File("."));
            Minecraft.getInstance().setOverlay(buildLoadingOverlay(() -> {
               if (this.schematicExporter == null) {
                  return 1.0F;
               }

               for (int i = 0; i < 100; i++) {
                  if (!this.schematicExporter.exportOne()) {
                     this.schematicExporter = null;
                     return 1.0F;
                  }
               }

               return this.schematicExporter.getProgress();
            }));
         } catch (FileNotFoundException e) {
            e.printStackTrace();
         }
      }
   }

   public void onClose() {
      this.minecraft.setScreen(this.parent);

      try {
         PhotonicsConfig.onChanged();
         PhotonicsConfig.save();
      } catch (Exception e) {
         Photonics.error("error saving config", e);
      }
   }

   private static LoadingOverlay buildLoadingOverlay(Supplier<Float> progressSupplier) {
      return new LoadingOverlay(Minecraft.getInstance(), new ReloadInstance() {
         public CompletableFuture<Unit> done() {
            return null;
         }

         public float getActualProgress() {
            return progressSupplier.get();
         }

         public boolean isDone() {
            return this.getActualProgress() == 1.0F;
         }
      }, o -> {}, false);
   }

   public static class BlockModel3DEntry extends ToggleableListScreen.ModelEntry {
      private final Block block;

      public BlockModel3DEntry(Block block) {
         this.block = block;
      }

      @Override
      public String getDisplayValue() {
         return this.block.getName().getString();
      }

      @Override
      public boolean isEnabled() {
         return PhotonicsConfig.isVoxelized(this.block);
      }

      @Override
      public void setEnabled(boolean enabled) {
         PhotonicsConfig.setVoxelized(this.block, enabled);
      }
   }

   private static class PButton {
      public Button widget;
      public final String text;
      public final OnPress pressAction;
      public final String toolTip;
      public final BooleanSupplier active;

      public PButton(String text, OnPress pressAction, String toolTip, BooleanSupplier active) {
         this.text = text;
         this.pressAction = pressAction;
         this.toolTip = toolTip;
         this.active = active;
      }
   }

   public static class TracedLightBlockEntry extends ToggleableListScreen.ModelEntry {
      private final Block block;

      public TracedLightBlockEntry(Block block) {
         this.block = block;
      }

      @Override
      public String getDisplayValue() {
         return BuiltInRegistries.BLOCK.getKey(this.block).getPath();
      }

      @Override
      public boolean isEnabled() {
         Boolean override = PhotonicsConfig.getTracedOverrides().get(this.block);
         return override != null ? override : PhotonicsConfig.getLightList().isTraced(this.block);
      }

      private boolean isButtonHeld(int key) {
         return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key);
      }

      @Override
      public void setEnabled(boolean traced) {
         if (!this.isButtonHeld(340) && !this.isButtonHeld(344)) {
            PhotonicsConfig.getTracedOverrides().put(this.block, traced);
         } else {
            PhotonicsConfig.getTracedOverrides().remove(this.block);
         }
      }
   }
}
