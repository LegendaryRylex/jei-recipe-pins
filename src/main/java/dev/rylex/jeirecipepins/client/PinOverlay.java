package dev.rylex.jeirecipepins.client;

import dev.rylex.jeirecipepins.compat.ftblibrary.FtbLibraryCompat;
import dev.rylex.jeirecipepins.config.PinsConfig;
import dev.rylex.jeirecipepins.pin.PinBoard;
import dev.rylex.jeirecipepins.pin.PinnedRecipe;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.Slot;

final class PinOverlay {
    private static final int CONTAINER_HIGHLIGHT = 0x80FFD700;
    private static final int SCREEN_DEPTH = 300;

    private PinOverlay() {}

    static void renderHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        PinBoard board = PinBoard.get();
        Screen screen = minecraft.screen;
        if (minecraft.options.hideGui
                || !PinsConfig.showInWorld()
                || !board.isVisible()
                || board.pins().isEmpty()
                || PinScreens.drawsPins(screen)
                || screen instanceof PinEditorScreen) {
            return;
        }
        double scale = PinsConfig.inWorldScale();
        board.arrange(guiGraphics.guiWidth(), guiGraphics.guiHeight(), Optional.empty());
        for (PinnedRecipe pin : board.pins()) {
            PinPanel.drawScaled(guiGraphics, pin, pin.x(), pin.y(), scale);
        }
    }

    static void renderOverScreen(GuiGraphics guiGraphics, Screen screen, int mouseX, int mouseY, float partialTick) {
        PinBoard board = PinBoard.get();
        if (board.isVisible() && !board.pins().isEmpty()) {
            if (PinsConfig.highlightContainerSlots() && screen instanceof AbstractContainerScreen<?> container) {
                drawContainerHighlights(guiGraphics, container, board.containerSlots());
            }
            drawPins(guiGraphics, screen, board, mouseX, mouseY);
        }
        PinToggleButton.render(guiGraphics, screen, mouseX, mouseY, partialTick);
    }

    static void drawPins(GuiGraphics guiGraphics, Screen screen, PinBoard board, int mouseX, int mouseY) {
        var pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(0, 0, SCREEN_DEPTH);
        board.arrange(guiGraphics.guiWidth(), guiGraphics.guiHeight(), FtbLibraryCompat.sidebarArea(screen));
        for (PinnedRecipe pin : board.pins()) {
            PinPanel.draw(guiGraphics, pin, mouseX, mouseY);
        }
        board.pinAt(mouseX, mouseY).ifPresent(pin -> PinPanel.drawTooltips(guiGraphics, pin, mouseX, mouseY));
        pose.popPose();
    }

    private static void drawContainerHighlights(
            GuiGraphics guiGraphics, AbstractContainerScreen<?> screen, BitSet matches) {
        List<Slot> slots = screen.getMenu().slots;
        for (int i = matches.nextSetBit(0); i >= 0 && i < slots.size(); i = matches.nextSetBit(i + 1)) {
            Slot slot = slots.get(i);
            if (!slot.isActive()) {
                continue;
            }
            int x = screen.getGuiLeft() + slot.x;
            int y = screen.getGuiTop() + slot.y;
            guiGraphics.fill(RenderType.guiOverlay(), x, y, x + 16, y + 16, CONTAINER_HIGHLIGHT);
        }
    }
}
