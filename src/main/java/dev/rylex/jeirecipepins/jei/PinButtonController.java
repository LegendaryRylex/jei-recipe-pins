package dev.rylex.jeirecipepins.jei;

import dev.rylex.jeirecipepins.pin.PinBoard;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.buttons.IButtonState;
import mezz.jei.api.gui.buttons.IIconButtonController;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

final class PinButtonController implements IIconButtonController {
    private static final int PINNED_TINT = 0x40FFD700;

    private final IRecipeLayoutDrawable<?> layout;
    private final IDrawable icon;
    private final IDrawable pinnedIcon;
    private boolean pinned;

    PinButtonController(IRecipeLayoutDrawable<?> layout, IDrawable icon, IDrawable pinnedIcon) {
        this.layout = layout;
        this.icon = icon;
        this.pinnedIcon = pinnedIcon;
    }

    @Override
    public void initState(IButtonState state) {
        updateState(state);
    }

    @Override
    public void updateState(IButtonState state) {
        pinned = PinBoard.get().isPinned(layout);
        state.setIcon(pinned ? pinnedIcon : icon);
        state.setForcePressed(pinned);
    }

    @Override
    public boolean onPress(IJeiUserInput input) {
        if (!input.isSimulate()) {
            PinBoard.get().toggle(layout);
        }
        return true;
    }

    @Override
    public void getTooltips(ITooltipBuilder tooltip) {
        tooltip.add(Component.translatable(pinned ? "jeirecipepins.tooltip.unpin" : "jeirecipepins.tooltip.pin"));
    }

    @Override
    public void drawExtras(GuiGraphicsExtractor guiGraphics, Rect2i area, int mouseX, int mouseY, float partialTicks) {
        if (pinned) {
            guiGraphics.fill(
                    area.getX(),
                    area.getY(),
                    area.getX() + area.getWidth(),
                    area.getY() + area.getHeight(),
                    PINNED_TINT);
        }
    }
}
