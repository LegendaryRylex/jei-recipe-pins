package dev.rylex.jeirecipepins.client;

import dev.rylex.jeirecipepins.JeiRecipePins;
import dev.rylex.jeirecipepins.mixin.BookmarkOverlayAccessor;
import dev.rylex.jeirecipepins.pin.PinBoard;
import java.util.Optional;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.buttons.IButtonState;
import mezz.jei.api.gui.buttons.IIconButtonController;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.elements.IconButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;

final class PinToggleButton {
    private static final Identifier ICON = JeiRecipePins.id("textures/gui/pin_menu.png");
    private static final Identifier SHOWN_ICON = JeiRecipePins.id("textures/gui/pin_menu_active.png");
    private static final int ICON_SIZE = 16;
    private static final int BUTTON_GAP = 2;

    @Nullable
    private static IconButton button;

    private PinToggleButton() {}

    private static final class Controller implements IIconButtonController {
        private final IDrawable icon;
        private final IDrawable shownIcon;

        private Controller(IJeiRuntime runtime) {
            IGuiHelper guiHelper = runtime.getJeiHelpers().getGuiHelper();
            this.icon = guiHelper
                    .drawableBuilder(ICON, 0, 0, ICON_SIZE, ICON_SIZE)
                    .setTextureSize(ICON_SIZE, ICON_SIZE)
                    .build();
            this.shownIcon = guiHelper
                    .drawableBuilder(SHOWN_ICON, 0, 0, ICON_SIZE, ICON_SIZE)
                    .setTextureSize(ICON_SIZE, ICON_SIZE)
                    .build();
        }

        @Override
        public void updateState(IButtonState state) {
            boolean visible = PinBoard.get().isVisible();
            state.setIcon(visible ? shownIcon : icon);
            state.setForcePressed(visible);
        }

        @Override
        public boolean onPress(IJeiUserInput input) {
            if (!input.isSimulate()) {
                PinBoard.get().toggleVisible();
            }
            return true;
        }

        @Override
        public void getTooltips(ITooltipBuilder tooltip) {
            tooltip.add(Component.translatable(
                    PinBoard.get().isVisible() ? "jeirecipepins.button.hide" : "jeirecipepins.button.show"));
        }
    }

    static void render(GuiGraphicsExtractor guiGraphics, Screen screen, int mouseX, int mouseY, float partialTick) {
        IconButton current = button(screen).orElse(null);
        if (current == null) {
            return;
        }
        current.draw(guiGraphics, mouseX, mouseY, partialTick);
        if (current.isMouseOver(mouseX, mouseY)) {
            current.drawTooltips(guiGraphics, mouseX, mouseY);
        }
    }

    static boolean click(Screen screen, double mouseX, double mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return false;
        }
        boolean hit = button(screen)
                .map(current -> current.isMouseOver(mouseX, mouseY))
                .orElse(false);
        if (hit) {
            Minecraft.getInstance()
                    .getSoundManager()
                    .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            PinBoard.get().toggleVisible();
        }
        return hit;
    }

    private static Optional<IconButton> button(Screen screen) {
        IJeiRuntime runtime = PinBoard.get().runtime().orElse(null);
        if (runtime == null
                || runtime.getScreenHelper().getGuiProperties(screen).isEmpty()) {
            return Optional.empty();
        }
        if (!(runtime.getBookmarkOverlay() instanceof BookmarkOverlayAccessor overlay)) {
            return Optional.empty();
        }
        IconButton history = overlay.jeirecipepins$historyButton();
        ImmutableRect2i anchor = history.isVisible()
                ? history.getArea()
                : overlay.jeirecipepins$bookmarkButton().getArea();
        if (anchor.isEmpty()) {
            return Optional.empty();
        }
        if (button == null) {
            button = new IconButton(new Controller(runtime));
        }
        button.updateBounds(anchor.moveRight(anchor.width() + BUTTON_GAP));
        button.tick();
        return Optional.of(button);
    }
}
