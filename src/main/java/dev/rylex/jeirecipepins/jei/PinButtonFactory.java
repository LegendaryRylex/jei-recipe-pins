package dev.rylex.jeirecipepins.jei;

import dev.rylex.jeirecipepins.JeiRecipePins;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.buttons.IIconButtonController;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.advanced.IRecipeButtonControllerFactory;
import net.minecraft.resources.ResourceLocation;

final class PinButtonFactory implements IRecipeButtonControllerFactory {
    private static final ResourceLocation ICON = JeiRecipePins.id("textures/gui/pin.png");
    private static final ResourceLocation PINNED_ICON = JeiRecipePins.id("textures/gui/pin_active.png");

    private final IDrawable icon;
    private final IDrawable pinnedIcon;

    PinButtonFactory(IGuiHelper guiHelper) {
        this.icon =
                guiHelper.drawableBuilder(ICON, 0, 0, 9, 9).setTextureSize(9, 9).build();
        this.pinnedIcon = guiHelper
                .drawableBuilder(PINNED_ICON, 0, 0, 9, 9)
                .setTextureSize(9, 9)
                .build();
    }

    @Override
    public <T> IIconButtonController createButtonController(IRecipeLayoutDrawable<T> recipeLayoutDrawable) {
        return new PinButtonController(recipeLayoutDrawable, icon, pinnedIcon);
    }
}
