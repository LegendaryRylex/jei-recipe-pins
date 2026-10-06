package dev.rylex.jeirecipepins.compat.ae2;

import appeng.api.stacks.AEItemKey;
import appeng.client.gui.me.common.RepoSlot;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.IClientRepo;
import appeng.menu.me.common.MEStorageMenu;
import dev.rylex.jeirecipepins.pin.NetworkStock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

final class Ae2Terminal {
    private Ae2Terminal() {}

    static Optional<NetworkStock> stock(AbstractContainerMenu menu, Supplier<Set<Item>> wanted) {
        if (!(menu instanceof MEStorageMenu storage) || !(storage.getClientRepo() instanceof IClientRepo repo)) {
            return Optional.empty();
        }
        Set<Item> items = wanted.get();
        List<ItemStack> stored = new ArrayList<>();
        List<ItemStack> craftable = new ArrayList<>();
        for (GridInventoryEntry entry : repo.getAllEntries()) {
            if (!(entry.getWhat() instanceof AEItemKey key) || !items.contains(key.getItem())) {
                continue;
            }
            if (entry.getStoredAmount() > 0) {
                stored.add(key.toStack());
            } else if (entry.isCraftable()) {
                craftable.add(key.toStack());
            }
        }
        return Optional.of(new NetworkStock(stored, craftable));
    }

    static boolean isRepoSlot(Slot slot) {
        return slot instanceof RepoSlot;
    }
}
