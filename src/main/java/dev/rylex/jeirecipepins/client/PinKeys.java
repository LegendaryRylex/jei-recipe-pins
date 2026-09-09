package dev.rylex.jeirecipepins.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.rylex.jeirecipepins.JeiRecipePins;
import dev.rylex.jeirecipepins.pin.PinBoard;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

final class PinKeys {
    private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(JeiRecipePins.id("keys"));

    static final KeyMapping TOGGLE = new KeyMapping(
            "key.jeirecipepins.toggle",
            KeyConflictContext.UNIVERSAL,
            KeyModifier.ALT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            CATEGORY);
    static final KeyMapping EDIT = new KeyMapping(
            "key.jeirecipepins.edit",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            CATEGORY);
    static final KeyMapping RESET = new KeyMapping(
            "key.jeirecipepins.reset",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY);

    private PinKeys() {}

    static void register(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(TOGGLE);
        event.register(EDIT);
        event.register(RESET);
    }

    static void handle(Minecraft minecraft) {
        PinBoard board = PinBoard.get();
        while (TOGGLE.consumeClick()) {
            board.toggleVisible();
        }
        while (EDIT.consumeClick()) {
            if (minecraft.screen == null && board.isReady()) {
                minecraft.setScreen(new PinEditorScreen(null));
            }
        }
        while (RESET.consumeClick()) {
            if (board.isReady()) {
                board.snapAll();
            }
        }
    }

    /**
     * Key mappings are not ticked while a screen is open, so the toggle is matched from the screen key event too;
     * {@code matches} compares the raw key alone, so the conflict context and modifier are checked separately.
     */
    static boolean handleInScreen(KeyEvent keyEvent) {
        if (TOGGLE.matches(keyEvent) && TOGGLE.isConflictContextAndModifierActive()) {
            PinBoard.get().toggleVisible();
            return true;
        }
        return false;
    }
}
