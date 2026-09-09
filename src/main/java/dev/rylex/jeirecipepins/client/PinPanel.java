package dev.rylex.jeirecipepins.client;

import dev.rylex.jeirecipepins.config.PinsConfig;
import dev.rylex.jeirecipepins.pin.PinnedRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;

final class PinPanel {
    private static final int FRAME = 0xC0202020;
    private static final int TITLE = 0xFFFFFFFF;
    private static final int CLOSE = 0xFFFF6060;
    private static final int MISSING = 0x60FF3030;
    private static final int OFFSCREEN_MOUSE = -10000;

    private PinPanel() {}

    static void draw(GuiGraphicsExtractor guiGraphics, PinnedRecipe pin, int mouseX, int mouseY) {
        pin.layout().setPosition(pin.layoutX(), pin.layoutY());
        drawFrame(guiGraphics, pin, pin.x(), pin.y(), true);
        pin.layout().drawRecipe(guiGraphics, mouseX, mouseY);
        drawMissing(guiGraphics, pin);
    }

    static void drawScaled(GuiGraphicsExtractor guiGraphics, PinnedRecipe pin, int x, int y, double scale) {
        var pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale((float) scale, (float) scale);
        pin.layout()
                .setPosition(
                        PinnedRecipe.PAD + pin.border(), PinnedRecipe.TITLE_HEIGHT + PinnedRecipe.PAD + pin.border());
        drawFrame(guiGraphics, pin, 0, 0, false);
        pin.layout().drawRecipe(guiGraphics, OFFSCREEN_MOUSE, OFFSCREEN_MOUSE);
        drawMissing(guiGraphics, pin);
        pose.popMatrix();
    }

    static void drawTooltips(GuiGraphicsExtractor guiGraphics, PinnedRecipe pin, int mouseX, int mouseY) {
        pin.layout().drawOverlays(guiGraphics, mouseX, mouseY);
    }

    private static void drawFrame(
            GuiGraphicsExtractor guiGraphics, PinnedRecipe pin, int x, int y, boolean closeButton) {
        int width = pin.width();
        int height = pin.height();
        guiGraphics.fill(x, y, x + width, y + height, FRAME);
        Font font = Minecraft.getInstance().font;
        int titleWidth = width - 2 * PinnedRecipe.PAD - (closeButton ? PinnedRecipe.TITLE_HEIGHT : 0);
        FormattedText clipped = font.substrByWidth(pin.title(), titleWidth);
        guiGraphics.text(
                font, Language.getInstance().getVisualOrder(clipped), x + PinnedRecipe.PAD, y + 2, TITLE, true);
        if (closeButton) {
            drawCross(guiGraphics, x + width - PinnedRecipe.TITLE_HEIGHT + 3, y + 3);
        }
    }

    private static void drawCross(GuiGraphicsExtractor guiGraphics, int x, int y) {
        for (int i = 0; i < 6; i++) {
            guiGraphics.fill(x + i, y + i, x + i + 1, y + i + 1, CLOSE);
            guiGraphics.fill(x + 5 - i, y + i, x + 6 - i, y + i + 1, CLOSE);
        }
    }

    private static void drawMissing(GuiGraphicsExtractor guiGraphics, PinnedRecipe pin) {
        if (!PinsConfig.markMissingInputs()) {
            return;
        }
        Rect2i rect = pin.layout().getRect();
        var pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.translate(rect.getX(), rect.getY());
        for (int i = 0; i < pin.inputs().size(); i++) {
            if (pin.isMissing(i)) {
                pin.inputs().get(i).drawHighlight(guiGraphics, MISSING);
            }
        }
        pose.popMatrix();
    }
}
