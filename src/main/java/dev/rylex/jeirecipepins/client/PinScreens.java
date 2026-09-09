package dev.rylex.jeirecipepins.client;

import dev.rylex.jeirecipepins.pin.PinBoard;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.jetbrains.annotations.Nullable;

public final class PinScreens {
    private PinScreens() {}

    public static boolean drawsPins(@Nullable Screen screen) {
        return screen instanceof AbstractContainerScreen<?> || screen instanceof RecipesGui;
    }

    public static boolean showsPins(PinBoard board, @Nullable Screen screen) {
        return board.isVisible() && (drawsPins(screen) || screen instanceof PinEditorScreen);
    }

    static boolean isJei(@Nullable Screen screen) {
        return screen != null && screen.getClass().getName().startsWith("mezz.jei.");
    }
}
