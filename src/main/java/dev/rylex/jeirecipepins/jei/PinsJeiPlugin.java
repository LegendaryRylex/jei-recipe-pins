package dev.rylex.jeirecipepins.jei;

import dev.rylex.jeirecipepins.JeiRecipePins;
import dev.rylex.jeirecipepins.pin.PinBoard;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.Identifier;

@JeiPlugin
public final class PinsJeiPlugin implements IModPlugin {
    private static final Identifier UID = JeiRecipePins.id("pins");

    @Override
    public Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {
        registration.addRecipeButtonFactory(
                new PinButtonFactory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGlobalGuiHandler(new PinsGlobalGuiHandler());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        PinBoard.get().runtimeAvailable(jeiRuntime);
    }

    @Override
    public void onRuntimeUnavailable() {
        PinBoard.get().runtimeUnavailable();
    }
}
