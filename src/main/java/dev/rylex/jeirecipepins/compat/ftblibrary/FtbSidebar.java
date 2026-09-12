package dev.rylex.jeirecipepins.compat.ftblibrary;

import dev.ftb.mods.ftblibrary.FTBLibraryClient;
import dev.ftb.mods.ftblibrary.sidebar.SidebarGroupGuiButton;
import dev.rylex.jeirecipepins.pin.PinGeometry;
import java.util.Optional;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

final class FtbSidebar {
    private FtbSidebar() {}

    static Optional<PinGeometry.Rect> area(Screen screen) {
        Rect2i area = SidebarGroupGuiButton.lastDrawnArea;
        if (!FTBLibraryClient.areButtonsVisible(screen) || area.getWidth() <= 0 || area.getHeight() <= 0) {
            return Optional.empty();
        }
        return Optional.of(new PinGeometry.Rect(area.getX(), area.getY(), area.getWidth(), area.getHeight()));
    }
}
