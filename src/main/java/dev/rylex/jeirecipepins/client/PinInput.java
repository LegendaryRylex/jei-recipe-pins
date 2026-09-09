package dev.rylex.jeirecipepins.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.rylex.jeirecipepins.pin.PinnedRecipe;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.runtime.IJeiKeyMapping;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

final class PinInput {
    private PinInput() {}

    private record MouseInput(InputConstants.Key key, MouseButtonEvent event, boolean simulate)
            implements IJeiUserInput {
        @Override
        public InputConstants.Key getKey() {
            return key;
        }

        @Override
        public int getModifiers() {
            return event.modifiers();
        }

        @Override
        public InputWithModifiers getInputWithModifiers() {
            return event;
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
        IJeiInputHandler handler = pin.layout().getInputHandler();
        InputConstants.Key key = InputConstants.Type.MOUSE.getOrCreate(button);
        MouseButtonEvent event = new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, 0));
        if (!handler.handleInput(mouseX, mouseY, new MouseInput(key, event, true))) {
            return false;
        }
        return handler.handleInput(mouseX, mouseY, new MouseInput(key, event, false));
    }

    static boolean scroll(PinnedRecipe pin, double mouseX, double mouseY, double deltaX, double deltaY) {
        return pin.layout().getInputHandler().handleMouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }
}
