package dev.rylex.jeirecipepins.client;

import dev.rylex.jeirecipepins.JeiRecipePins;
import dev.rylex.jeirecipepins.pin.PinBoard;
import java.util.Optional;
import mezz.jei.api.gui.handlers.IGuiProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class PinToggleButton {
    private static final ResourceLocation ICON = JeiRecipePins.id("textures/gui/pin_menu.png");
    private static final ResourceLocation SHOWN_ICON = JeiRecipePins.id("textures/gui/pin_menu_active.png");
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
        public void renderString(GuiGraphics guiGraphics, Font font, int color) {}
    }

    static void render(GuiGraphics guiGraphics, Screen screen, int mouseX, int mouseY, float partialTick) {
        PinButton current = button(screen).orElse(null);
        if (current == null) {
            return;
        }
        boolean visible = PinBoard.get().isVisible();
        current.setMessage(Component.translatable(visible ? "jeirecipepins.button.hide" : "jeirecipepins.button.show"));
        current.render(guiGraphics, mouseX, mouseY, partialTick);
        ResourceLocation icon = visible ? SHOWN_ICON : ICON;
        guiGraphics.blit(
                icon,
                current.getX() + (BUTTON_SIZE - ICON_SIZE) / 2,
                current.getY() + (BUTTON_SIZE - ICON_SIZE) / 2,
                0,
                0,
                ICON_SIZE,
                ICON_SIZE,
                ICON_SIZE,
                ICON_SIZE);
        if (current.isHovered()) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, current.getMessage(), mouseX, mouseY);
        }
    }

    static boolean click(Screen screen, double mouseX, double mouseY, int mouseButton) {
        return button(screen)
                .map(current -> current.mouseClicked(mouseX, mouseY, mouseButton))
                .orElse(false);
    }

    public static Optional<Rect2i> exclusionArea(@Nullable Screen screen) {
        return properties(screen)
                .filter(properties -> !fitsBesideJeiButtons(properties))
                .map(PinToggleButton::besideGui);
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
        if (fitsBesideJeiButtons(properties)) {
            return new Rect2i(
                    JEI_ROW_X, properties.screenHeight() - JEI_MARGIN - BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
        }
        return besideGui(properties);
    }

    private static boolean fitsBesideJeiButtons(IGuiProperties properties) {
        return JEI_ROW_X + BUTTON_SIZE <= properties.guiLeft() - JEI_MARGIN
                && properties.screenHeight() >= 2 * JEI_MARGIN + BUTTON_SIZE;
    }

    private static Rect2i besideGui(IGuiProperties properties) {
        int left = properties.guiLeft() - BUTTON_GAP - BUTTON_SIZE;
        int right = properties.guiRight() + BUTTON_GAP;
        int x = left >= 0 ? left : Math.min(right, properties.screenWidth() - BUTTON_SIZE);
        int y = Math.max(0, Math.min(properties.guiTop(), properties.screenHeight() - BUTTON_SIZE));
        return new Rect2i(Math.max(0, x), y, BUTTON_SIZE, BUTTON_SIZE);
    }
}
