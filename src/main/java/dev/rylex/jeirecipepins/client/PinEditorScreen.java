package dev.rylex.jeirecipepins.client;

import dev.rylex.jeirecipepins.pin.PinBoard;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public final class PinEditorScreen extends Screen {
    private static final int DIM = 0x50000000;
    private static final int TEXT = 0xFFFFFFFF;

    @Nullable
    private final Screen parent;

    private final PinDragger dragger = new PinDragger();

    /** The parent is shown again on close, the way JEI restores a container behind its own recipe screen. */
    public PinEditorScreen(@Nullable Screen parent) {
        super(Component.translatable("jeirecipepins.editor.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int y = height - 26;
        addRenderableWidget(
                Button.builder(Component.translatable("jeirecipepins.editor.reset"), button -> PinBoard.get()
                                .snapAll())
                        .bounds(width / 2 - 102, y, 100, 20)
                        .build());
        addRenderableWidget(Button.builder(Component.translatable("jeirecipepins.editor.done"), button -> onClose())
                .bounds(width / 2 + 2, y, 100, 20)
                .build());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, width, height, DIM);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        PinBoard board = PinBoard.get();
        if (board.pins().isEmpty()) {
            guiGraphics.drawCenteredString(
                    font, Component.translatable("jeirecipepins.editor.empty"), width / 2, height / 2 - 4, TEXT);
        }
        PinOverlay.drawPins(guiGraphics, this, board, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return dragger.press(PinBoard.get(), mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return dragger.drag(mouseX, mouseY, width, height) || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return dragger.release(PinBoard.get()) || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return PinBoard.get()
                        .pinAt(mouseX, mouseY)
                        .map(pin -> PinInput.scroll(pin, mouseX, mouseY, scrollX, scrollY))
                        .orElse(false)
                || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        PinBoard.get().save();
        minecraft.setScreen(parent);
    }
}
