package dev.rylex.jeirecipepins.client;

import dev.rylex.jeirecipepins.pin.PinBoard;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.jetbrains.annotations.Nullable;

public final class PinScreens {
    private PinScreens() {}

    public static boolean drawsPins(@Nullable Screen screen) {
        if (screen instanceof AbstractContainerScreen<?>) {
            return true;
        }
        return screen != null
                && PinBoard.get()
                        .runtime()
                        .flatMap(runtime -> runtime.getScreenHelper().getGuiProperties(screen))
                        .isPresent();
    }

    public static boolean showsPins(PinBoard board, @Nullable Screen screen) {
        return board.isVisible() && (drawsPins(screen) || screen instanceof PinEditorScreen);
    }
}
