package dev.rylex.jeirecipepins.client;

import dev.rylex.jeirecipepins.JeiRecipePins;
import dev.rylex.jeirecipepins.pin.PinBoard;
import dev.rylex.jeirecipepins.pin.PinGeometry;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.gui.handlers.IGuiProperties;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public final class PinToggleButton {
    private static final Identifier ICON = JeiRecipePins.id("textures/gui/pin_menu.png");
    private static final Identifier SHOWN_ICON = JeiRecipePins.id("textures/gui/pin_menu_active.png");
    private static final int ICON_SIZE = 16;
    private static final int BUTTON_SIZE = 20;
    private static final int BUTTON_GAP = 2;
    private static final int JEI_MARGIN = 6;
    private static final int JEI_ROW_X = JEI_MARGIN + 2 * (BUTTON_SIZE + BUTTON_GAP);

    private static final PinButton BUTTON = new PinButton(
            Button.builder(CommonComponents.EMPTY, ignored -> PinBoard.get().toggleVisible())
                    .size(BUTTON_SIZE, BUTTON_SIZE));

    private PinToggleButton() {}

    private static final class PinButton extends Button {
        private PinButton(Builder builder) {
            super(builder);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            extractDefaultSprite(graphics);
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    PinBoard.get().isVisible() ? SHOWN_ICON : ICON,
                    getX() + (BUTTON_SIZE - ICON_SIZE) / 2,
                    getY() + (BUTTON_SIZE - ICON_SIZE) / 2,
                    0,
                    0,
                    ICON_SIZE,
                    ICON_SIZE,
                    ICON_SIZE,
                    ICON_SIZE);
        }
    }

    static void render(GuiGraphicsExtractor guiGraphics, Screen screen, int mouseX, int mouseY, float partialTick) {
        PinButton current = button(screen).orElse(null);
        if (current == null) {
            return;
        }
        Component message = Component.translatable(
                PinBoard.get().isVisible() ? "jeirecipepins.button.hide" : "jeirecipepins.button.show");
        current.setMessage(message);
        current.setTooltip(Tooltip.create(message));
        current.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
    }

    static boolean click(Screen screen, double mouseX, double mouseY, int mouseButton) {
        return button(screen)
                .map(current -> current.mouseClicked(
                        new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(mouseButton, 0)), false))
                .orElse(false);
    }

    public static Optional<Rect2i> exclusionArea(@Nullable Screen screen) {
        return properties(screen)
                .filter(properties -> jeiRowSpot(properties, pinAreas()).isEmpty())
                .map(properties -> besideGui(properties, pinAreas()));
    }

    private static Optional<IGuiProperties> properties(@Nullable Screen screen) {
        if (screen == null || PinBoard.get().pins().isEmpty()) {
            return Optional.empty();
        }
        return PinBoard.get().runtime().flatMap(runtime -> runtime.getScreenHelper()
                .getGuiProperties(screen));
    }

    private static Optional<PinButton> button(Screen screen) {
        return properties(screen).map(PinToggleButton::area).map(area -> {
            BUTTON.setRectangle(area.getWidth(), area.getHeight(), area.getX(), area.getY());
            return BUTTON;
        });
    }

    private static Rect2i area(IGuiProperties properties) {
        List<PinGeometry.Rect> pins = pinAreas();
        return jeiRowSpot(properties, pins).orElseGet(() -> besideGui(properties, pins));
    }

    private static List<PinGeometry.Rect> pinAreas() {
        return PinBoard.get().pins().stream()
                .map(pin -> new PinGeometry.Rect(pin.x(), pin.y(), pin.width(), pin.height()))
                .toList();
    }

    private static Optional<Rect2i> jeiRowSpot(IGuiProperties properties, List<PinGeometry.Rect> pins) {
        if (JEI_ROW_X + BUTTON_SIZE > properties.guiLeft() - JEI_MARGIN
                || properties.screenHeight() < 2 * JEI_MARGIN + BUTTON_SIZE) {
            return Optional.empty();
        }
        PinGeometry.Rect spot = new PinGeometry.Rect(
                JEI_ROW_X, properties.screenHeight() - JEI_MARGIN - BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
        return PinGeometry.overlapsAny(spot, pins) ? Optional.empty() : Optional.of(toRect2i(spot));
    }

    private static Rect2i besideGui(IGuiProperties properties, List<PinGeometry.Rect> pins) {
        int top = Math.max(0, Math.min(properties.guiTop(), properties.screenHeight() - BUTTON_SIZE));
        int left = properties.guiLeft() - BUTTON_GAP - BUTTON_SIZE;
        int right = properties.guiRight() + BUTTON_GAP;
        for (int x : new int[] {left, right}) {
            if (x < 0 || x + BUTTON_SIZE > properties.screenWidth()) {
                continue;
            }
            int y = PinGeometry.freeTop(x, top, BUTTON_SIZE, pins);
            if (y + BUTTON_SIZE <= properties.screenHeight()) {
                return new Rect2i(x, y, BUTTON_SIZE, BUTTON_SIZE);
            }
        }
        int x = left >= 0 ? left : Math.min(right, properties.screenWidth() - BUTTON_SIZE);
        return new Rect2i(Math.max(0, x), top, BUTTON_SIZE, BUTTON_SIZE);
    }

    private static Rect2i toRect2i(PinGeometry.Rect rect) {
        return new Rect2i(rect.x(), rect.y(), rect.width(), rect.height());
    }
}
