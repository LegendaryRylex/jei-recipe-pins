package dev.rylex.jeirecipepins.compat.integratedterminals;

import dev.rylex.jeirecipepins.pin.NetworkStock;
import dev.rylex.jeirecipepins.pin.StackSpotVisitor;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;

public final class IntegratedTerminalsCompat {
    private static final boolean LOADED = ModList.get().isLoaded("integratedterminals");

    private IntegratedTerminalsCompat() {}

    public static Optional<NetworkStock> networkStock(AbstractContainerMenu menu, Supplier<Set<Item>> wanted) {
        return LOADED ? StorageTerminal.stock(menu, wanted) : Optional.empty();
    }

    public static void forEachGridStack(Screen screen, StackSpotVisitor visitor) {
        if (LOADED) {
            StorageTerminal.forEachGridStack(screen, visitor);
        }
    }
}
