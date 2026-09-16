package dev.rylex.jeirecipepins.jei;

import dev.rylex.jeirecipepins.client.PinScreens;
import dev.rylex.jeirecipepins.client.PinToggleButton;
import dev.rylex.jeirecipepins.pin.PinBoard;
import dev.rylex.jeirecipepins.pin.PinnedRecipe;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickableIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

final class PinsGlobalGuiHandler implements IGlobalGuiHandler {

    @Override
    public Collection<Rect2i> getGuiExtraAreas() {
        PinBoard board = PinBoard.get();
        Screen screen = Minecraft.getInstance().screen;
        List<Rect2i> extraAreas = new ArrayList<>();
        PinToggleButton.area(screen).ifPresent(extraAreas::add);
        if (!PinScreens.showsPins(board, screen) || board.pins().isEmpty()) {
            return extraAreas;
        }
        board.pins().stream()
                .map(pin -> new Rect2i(pin.x(), pin.y(), pin.width(), pin.height()))
                .forEach(extraAreas::add);
        return extraAreas;
    }

    @Override
    public Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(
            IClickableIngredientFactory factory, double mouseX, double mouseY) {
        PinBoard board = PinBoard.get();
        if (!PinScreens.showsPins(board, Minecraft.getInstance().screen)) {
            return Optional.empty();
        }
        return board.pinAt(mouseX, mouseY).flatMap(pin -> {
            IRecipeLayoutDrawable<?> layout = pin.layout();
            pin.alignLayoutTo(mouseX, mouseY);
            return layout.getSlotUnderMouse(mouseX, mouseY)
                    .flatMap(under ->
                            under.slot().getDisplayedIngredient().flatMap(typed -> build(factory, typed, pin, under)));
        });
    }

    private static <T> Optional<IClickableIngredient<?>> build(
            IClickableIngredientFactory factory,
            ITypedIngredient<T> typed,
            PinnedRecipe pin,
            RecipeSlotUnderMouse under) {
        Rect2i slot = under.slot().getAreaIncludingBackground();
        Rect2i origin = pin.layout().getRect();
        ScreenPosition offset = under.offset();
        Rect2i area = pin.toScreen(
                offset.x() - origin.getX() + slot.getX(),
                offset.y() - origin.getY() + slot.getY(),
                slot.getWidth(),
                slot.getHeight());
        Optional<IClickableIngredient<T>> built = factory.createBuilder(typed).buildWithArea(area);
        return Optional.ofNullable(built.orElse(null));
    }
}
