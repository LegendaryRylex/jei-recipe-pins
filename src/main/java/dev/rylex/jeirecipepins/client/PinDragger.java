package dev.rylex.jeirecipepins.client;

import dev.rylex.jeirecipepins.pin.PinBoard;
import dev.rylex.jeirecipepins.pin.PinGeometry;
import dev.rylex.jeirecipepins.pin.PinnedRecipe;
import org.jetbrains.annotations.Nullable;

final class PinDragger {
    @Nullable
    private PinnedRecipe dragging;

    private double offsetX;
    private double offsetY;

    boolean press(PinBoard board, double mouseX, double mouseY, int button) {
        dragging = null;
        PinnedRecipe pin = board.pinAt(mouseX, mouseY).orElse(null);
        if (pin == null) {
            return false;
        }
        if (button == 0 && pin.onCloseButton(mouseX, mouseY)) {
            board.unpin(pin);
            return true;
        }
        if (button == 0 && pin.inTitleBar(mouseX, mouseY)) {
            board.bringToFront(pin);
            dragging = pin;
            offsetX = mouseX - pin.x();
            offsetY = mouseY - pin.y();
            return true;
        }
        PinInput.click(pin, mouseX, mouseY, button);
        return true;
    }

    boolean drag(double mouseX, double mouseY, int width, int height) {
        if (dragging == null) {
            return false;
        }
        int x = PinGeometry.clamp((int) Math.round(mouseX - offsetX), dragging.width(), width);
        int y = PinGeometry.clamp((int) Math.round(mouseY - offsetY), dragging.height(), height);
        dragging.moveTo(x, y);
        return true;
    }

    boolean release(PinBoard board) {
        if (dragging == null) {
            return false;
        }
        dragging = null;
        board.save();
        return true;
    }
}
