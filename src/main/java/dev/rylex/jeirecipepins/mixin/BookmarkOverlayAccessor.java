package dev.rylex.jeirecipepins.mixin;

import mezz.jei.gui.elements.IconButton;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BookmarkOverlay.class)
public interface BookmarkOverlayAccessor {

    @Accessor("bookmarkButton")
    IconButton jeirecipepins$bookmarkButton();

    @Accessor("historyButton")
    IconButton jeirecipepins$historyButton();
}
