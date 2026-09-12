package dev.rylex.jeirecipepins.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.rylex.jeirecipepins.pin.PinnedRecipe;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.runtime.IJeiKeyMapping;
import net.minecraft.client.KeyMapping;

final class PinInput {
    private PinInput() {}

    private record MouseInput(InputConstants.Key key, boolean simulate) implements IJeiUserInput {
        @Override
        public InputConstants.Key getKey() {
            return key;
        }

        @Override
        public int getModifiers() {
            return 0;
        }

        @Override
        public boolean isSimulate() {
            return simulate;
        }

        @Override
        public boolean is(KeyMapping keyMapping) {
            return keyMapping.isActiveAndMatches(key);
        }

        @Override
        public boolean is(IJeiKeyMapping keyMapping) {
            return keyMapping.isActiveAndMatches(key);
        }
    }

    static boolean click(PinnedRecipe pin, double mouseX, double mouseY, int button) {
        pin.alignLayoutTo(mouseX, mouseY);
        IJeiInputHandler handler = pin.layout().getInputHandler();
        InputConstants.Key key = InputConstants.Type.MOUSE.getOrCreate(button);
        if (!handler.handleInput(mouseX, mouseY, new MouseInput(key, true))) {
            return false;
        }
        return handler.handleInput(mouseX, mouseY, new MouseInput(key, false));
    }

    static boolean scroll(PinnedRecipe pin, double mouseX, double mouseY, double deltaX, double deltaY) {
        pin.alignLayoutTo(mouseX, mouseY);
        return pin.layout().getInputHandler().handleMouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }
}
