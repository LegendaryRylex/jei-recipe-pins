package dev.rylex.jeirecipepins.compat.ae2;

import appeng.api.config.FuzzyMode;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.helpers.WirelessTerminalMenuHost;
import appeng.integration.modules.curios.CuriosIntegration;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocators;
import dev.rylex.jeirecipepins.pin.NetworkStock;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

final class Ae2Wireless {
    private Ae2Wireless() {}

    /** Ticks each terminal's own menu host, so range, quantum links and power follow AE2 and its addons. */
    static NetworkStock stock(Player player, Set<Item> wanted, int limit) {
        for (ItemMenuHostLocator locator : terminals(player)) {
            if (!(locator.locateItem(player).getItem() instanceof WirelessTerminalItem item)) {
                continue;
            }
            WirelessTerminalMenuHost<?> host = item.getMenuHost(player, locator, locator.hitResult());
            if (host == null) {
                continue;
            }
            host.tick();
            IGridNode node = host.getActionableNode();
            if (host.getLinkStatus().connected() && node != null) {
                return read(node.getGrid(), wanted, limit);
            }
        }
        return NetworkStock.EMPTY;
    }

    private static List<ItemMenuHostLocator> terminals(Player player) {
        List<ItemMenuHostLocator> found = new ArrayList<>();
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).getItem() instanceof WirelessTerminalItem) {
                found.add(MenuLocators.forInventorySlot(i));
            }
        }
        ResourceHandler<ItemResource> curios = player.getCapability(CuriosIntegration.ITEM_HANDLER);
        if (curios != null) {
            for (int i = 0; i < curios.size(); i++) {
                if (curios.getResource(i).getItem() instanceof WirelessTerminalItem) {
                    found.add(MenuLocators.forCurioSlot(i));
                }
            }
        }
        return found;
    }

    private static NetworkStock read(IGrid grid, Set<Item> wanted, int limit) {
        KeyCounter inventory = grid.getStorageService().getCachedInventory();
        Set<AEKey> stored = new LinkedHashSet<>();
        for (Item item : wanted) {
            for (Object2LongMap.Entry<AEKey> entry : inventory.findFuzzy(AEItemKey.of(item), FuzzyMode.IGNORE_ALL)) {
                if (entry.getLongValue() > 0) {
                    stored.add(entry.getKey());
                }
            }
        }
        Set<AEKey> craftable = new LinkedHashSet<>(grid.getCraftingService()
                .getCraftables(key -> key instanceof AEItemKey itemKey && wanted.contains(itemKey.getItem())));
        craftable.removeAll(stored);
        return new NetworkStock(stacks(stored, limit), stacks(craftable, limit));
    }

    private static List<ItemStack> stacks(Collection<AEKey> keys, int limit) {
        return keys.stream()
                .filter(AEItemKey.class::isInstance)
                .map(key -> ((AEItemKey) key).toStack())
                .limit(limit)
                .toList();
    }
}
