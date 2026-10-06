package dev.rylex.jeirecipepins.compat.ae2;

import dev.rylex.jeirecipepins.pin.NetworkStock;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;

public final class Ae2Compat {
    private static final boolean LOADED = ModList.get().isLoaded("ae2");

    private Ae2Compat() {}

    public static Optional<NetworkStock> networkStock(AbstractContainerMenu menu, Supplier<Set<Item>> wanted) {
        return LOADED ? Ae2Terminal.stock(menu, wanted) : Optional.empty();
    }

    public static boolean isTerminalSlot(Slot slot) {
        return LOADED && Ae2Terminal.isRepoSlot(slot);
    }

    /** {@link NetworkStock#EMPTY} when no carried terminal reaches a network. */
    public static Optional<NetworkStock> carriedStock(Player player, Set<Item> wanted, int limit) {
        return LOADED ? Optional.of(Ae2Wireless.stock(player, wanted, limit)) : Optional.empty();
    }
}
