package dev.rylex.jeirecipepins.compat.ftblibrary;

import dev.rylex.jeirecipepins.pin.PinGeometry;
import java.util.Optional;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModList;

public final class FtbLibraryCompat {
    private static final boolean LOADED = ModList.get().isLoaded("ftblibrary");

    private FtbLibraryCompat() {}

    public static Optional<PinGeometry.Rect> sidebarArea(Screen screen) {
        return LOADED ? FtbSidebar.area(screen) : Optional.empty();
    }
}
