package dev.rylex.jeirecipepins.jei;

import dev.rylex.jeirecipepins.client.PinScreens;
import dev.rylex.jeirecipepins.pin.PinBoard;
import dev.rylex.jeirecipepins.pin.PinGeometry;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.gui.handlers.IGuiProperties;
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
        if (!PinScreens.showsPins(board, screen) || board.pins().isEmpty()) {
            return List.of();
        }
        List<PinGeometry.Rect> pins = board.pins().stream()
                .map(pin -> new PinGeometry.Rect(pin.x(), pin.y(), pin.width(), pin.height()))
                .toList();
        IGuiProperties properties = board.runtime()
                .flatMap(runtime -> runtime.getScreenHelper().getGuiProperties(screen))
                .orElse(null);
        List<PinGeometry.Rect> areas = properties == null
                ? pins
                : PinGeometry.exclusionAreas(
                        pins, properties.guiLeft(), properties.guiRight(), properties.screenWidth());
        return areas.stream()
                .map(rect -> new Rect2i(rect.x(), rect.y(), rect.width(), rect.height()))
                .toList();
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
            layout.setPosition(pin.layoutX(), pin.layoutY());
            return layout.getSlotUnderMouse(mouseX, mouseY)
                    .flatMap(under ->
                            under.slot().getDisplayedIngredient().flatMap(typed -> build(factory, typed, under)));
        });
    }

    private static <T> Optional<IClickableIngredient<?>> build(
            IClickableIngredientFactory factory, ITypedIngredient<T> typed, RecipeSlotUnderMouse under) {
        Rect2i rect = under.slot().getAreaIncludingBackground();
        ScreenPosition offset = under.offset();
        Optional<IClickableIngredient<T>> built = factory.createBuilder(typed)
                .buildWithArea(offset.x() + rect.getX(), offset.y() + rect.getY(), rect.getWidth(), rect.getHeight());
        return Optional.ofNullable(built.orElse(null));
    }
}
