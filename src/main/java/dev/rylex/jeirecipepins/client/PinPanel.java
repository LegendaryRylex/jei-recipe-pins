package dev.rylex.jeirecipepins.client;

import dev.rylex.jeirecipepins.config.PinsConfig;
import dev.rylex.jeirecipepins.pin.PinnedRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;

final class PinPanel {
    private static final int FRAME = 0xC0202020;
    private static final int TITLE = 0xFFFFFFFF;
    private static final int CLOSE = 0xFFFF6060;
    private static final int GRIP = 0xFF909090;
    private static final int GRIP_HOVERED = 0xFFFFFFFF;
    private static final int MISSING = 0x60FF3030;
    private static final int OFFSCREEN_MOUSE = -10000;

    private PinPanel() {}

    static void draw(GuiGraphics guiGraphics, PinnedRecipe pin, int mouseX, int mouseY) {
        drawFrame(guiGraphics, pin, pin.x(), pin.y(), true);
        drawBody(guiGraphics, pin, pin.bodyX(), pin.bodyY(), (int) Math.floor(pin.toBodyX(mouseX)), (int)
                Math.floor(pin.toBodyY(mouseY)));
        drawGrip(
                guiGraphics,
                pin.x() + pin.width(),
                pin.y() + pin.height(),
                pin.onResizeGrip(mouseX, mouseY) ? GRIP_HOVERED : GRIP);
    }

    static void drawScaled(GuiGraphics guiGraphics, PinnedRecipe pin, int x, int y, double scale) {
        var pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale((float) scale, (float) scale, 1);
        drawFrame(guiGraphics, pin, 0, 0, false);
        drawBody(
                guiGraphics,
                pin,
                PinnedRecipe.PAD,
                PinnedRecipe.TITLE_HEIGHT + PinnedRecipe.PAD,
                OFFSCREEN_MOUSE,
                OFFSCREEN_MOUSE);
        pose.popPose();
    }

    static void drawTooltips(GuiGraphics guiGraphics, PinnedRecipe pin, int mouseX, int mouseY) {
        pin.alignLayoutTo(mouseX, mouseY);
        pin.layout().drawOverlays(guiGraphics, mouseX, mouseY);
    }

    private static void drawBody(GuiGraphics guiGraphics, PinnedRecipe pin, int x, int y, int mouseX, int mouseY) {
        var pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale((float) pin.scale(), (float) pin.scale(), 1);
        pin.layout().setPosition(pin.border(), pin.border());
        pin.layout().drawRecipe(guiGraphics, mouseX, mouseY);
        drawMissing(guiGraphics, pin);
        pose.popPose();
    }

    private static void drawFrame(GuiGraphics guiGraphics, PinnedRecipe pin, int x, int y, boolean closeButton) {
        int width = pin.width();
        int height = pin.height();
        guiGraphics.fill(x, y, x + width, y + height, FRAME);
        Font font = Minecraft.getInstance().font;
        int titleWidth = width - 2 * PinnedRecipe.PAD - (closeButton ? PinnedRecipe.TITLE_HEIGHT : 0);
        FormattedText clipped = font.substrByWidth(pin.title(), titleWidth);
        guiGraphics.drawString(
                font, Language.getInstance().getVisualOrder(clipped), x + PinnedRecipe.PAD, y + 2, TITLE, true);
        if (closeButton) {
            drawCross(guiGraphics, x + width - PinnedRecipe.TITLE_HEIGHT + 3, y + 3);
        }
    }

    private static void drawCross(GuiGraphics guiGraphics, int x, int y) {
        for (int i = 0; i < 6; i++) {
            guiGraphics.fill(x + i, y + i, x + i + 1, y + i + 1, CLOSE);
            guiGraphics.fill(x + 5 - i, y + i, x + 6 - i, y + i + 1, CLOSE);
        }
    }

    private static void drawGrip(GuiGraphics guiGraphics, int right, int bottom, int color) {
        for (int dx = 0; dx < PinnedRecipe.GRIP; dx += 2) {
            for (int dy = 0; dx + dy < PinnedRecipe.GRIP; dy += 2) {
                guiGraphics.fill(right - dx - 2, bottom - dy - 2, right - dx - 1, bottom - dy - 1, color);
            }
        }
    }

    private static void drawMissing(GuiGraphics guiGraphics, PinnedRecipe pin) {
        if (!PinsConfig.markMissingInputs()) {
            return;
        }
        Rect2i rect = pin.layout().getRect();
        var pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(rect.getX(), rect.getY(), 0);
        for (int i = 0; i < pin.inputs().size(); i++) {
            if (pin.isMissing(i)) {
                pin.inputs().get(i).drawHighlight(guiGraphics, MISSING);
            }
        }
        pose.popPose();
    }
}
