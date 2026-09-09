package dev.rylex.jeirecipepins.client;

import dev.rylex.jeirecipepins.JeiRecipePins;
import dev.rylex.jeirecipepins.pin.PinBoard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = JeiRecipePins.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    private static final PinDragger DRAGGER = new PinDragger();

    private ClientEvents() {}

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        PinKeys.register(event);
    }

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerBelow(VanillaGuiLayers.CHAT, JeiRecipePins.id("pins"), PinOverlay::renderHud);
    }

    @SubscribeEvent
    public static void registerCommands(RegisterClientCommandsEvent event) {
        PinCommands.register(event);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        PinBoard.get().tick(minecraft);
        PinKeys.handle(minecraft);
    }

    @SubscribeEvent
    public static void onScreenRendered(ScreenEvent.Render.Post event) {
        Screen screen = event.getScreen();
        if (PinScreens.drawsPins(screen)) {
            PinOverlay.renderOverScreen(
                    event.getGuiGraphics(), screen, event.getMouseX(), event.getMouseY(), event.getPartialTick());
        }
    }

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (PinScreens.drawsPins(event.getScreen())
                && PinKeys.handleInScreen(event.getKeyCode(), event.getScanCode())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
        Screen screen = event.getScreen();
        if (!PinScreens.drawsPins(screen)) {
            return;
        }
        if (PinToggleButton.click(screen, event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
            return;
        }
        PinBoard board = PinBoard.get();
        if (board.isVisible() && DRAGGER.press(board, event.getMouseX(), event.getMouseY(), event.getButton())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseDragged(ScreenEvent.MouseDragged.Pre event) {
        Screen screen = event.getScreen();
        if (PinScreens.drawsPins(screen)
                && DRAGGER.drag(event.getMouseX(), event.getMouseY(), screen.width, screen.height)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        if (PinScreens.drawsPins(event.getScreen()) && DRAGGER.release(PinBoard.get())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        if (!PinScreens.drawsPins(event.getScreen())) {
            return;
        }
        PinBoard board = PinBoard.get();
        if (!board.isVisible()) {
            return;
        }
        board.pinAt(event.getMouseX(), event.getMouseY()).ifPresent(pin -> {
            if (PinInput.scroll(
                    pin, event.getMouseX(), event.getMouseY(), event.getScrollDeltaX(), event.getScrollDeltaY())) {
                event.setCanceled(true);
            }
        });
    }
}
