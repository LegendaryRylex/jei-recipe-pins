package dev.rylex.jeirecipepins.compat.integratedterminals;

import dev.rylex.jeirecipepins.pin.NetworkStock;
import dev.rylex.jeirecipepins.pin.StackSpotVisitor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.cyclops.commoncapabilities.api.ingredient.IngredientComponent;
import org.cyclops.integratedterminals.api.terminalstorage.ITerminalStorageTabClient;
import org.cyclops.integratedterminals.client.gui.container.ContainerScreenTerminalStorage;
import org.cyclops.integratedterminals.core.terminalstorage.TerminalStorageTabIngredientComponentClient;
import org.cyclops.integratedterminals.core.terminalstorage.crafting.HandlerWrappedTerminalCraftingOption;
import org.cyclops.integratedterminals.inventory.container.ContainerTerminalStorageBase;
import org.jetbrains.annotations.Nullable;

final class StorageTerminal {
    private StorageTerminal() {}

    static Optional<NetworkStock> stock(AbstractContainerMenu menu, Supplier<Set<Item>> wanted) {
        if (!(menu instanceof ContainerTerminalStorageBase<?> terminal)) {
            return Optional.empty();
        }
        return itemTab(terminal).map(tab -> stock(tab, terminal.getSelectedChannel(), wanted.get()));
    }

    private static NetworkStock stock(
            TerminalStorageTabIngredientComponentClient<ItemStack, ?> tab, int channel, Set<Item> items) {
        List<ItemStack> stored = new ArrayList<>();
        for (ItemStack stack : tab.getRawUnfilteredIngredientsView(channel)) {
            if (items.contains(stack.getItem())) {
                stored.add(stack);
            }
        }
        List<ItemStack> craftable = new ArrayList<>();
        Collection<HandlerWrappedTerminalCraftingOption<ItemStack>> options = tab.getCraftingOptions(channel);
        if (options != null) {
            for (HandlerWrappedTerminalCraftingOption<ItemStack> option : options) {
                option.getCraftingOption().getOutputs().forEachRemaining(stack -> {
                    if (items.contains(stack.getItem())) {
                        craftable.add(stack);
                    }
                });
            }
        }
        return new NetworkStock(stored, craftable);
    }

    @SuppressWarnings("unchecked")
    private static Optional<TerminalStorageTabIngredientComponentClient<ItemStack, ?>> itemTab(
            ContainerTerminalStorageBase<?> terminal) {
        String selected = terminal.getSelectedTab();
        ITerminalStorageTabClient<?> tab = selected == null ? null : terminal.getTabClient(selected);
        if (!isItemTab(tab)) {
            tab = terminal.getTabsClient().values().stream()
                    .filter(StorageTerminal::isItemTab)
                    .findFirst()
                    .orElse(null);
        }
        return Optional.ofNullable((TerminalStorageTabIngredientComponentClient<ItemStack, ?>) tab);
    }

    private static boolean isItemTab(@Nullable ITerminalStorageTabClient<?> tab) {
        return tab instanceof TerminalStorageTabIngredientComponentClient<?, ?> ingredients
                && ingredients.getIngredientComponent() == IngredientComponent.ITEMSTACK;
    }

    static void forEachGridStack(Screen screen, StackSpotVisitor visitor) {
        if (!(screen instanceof ContainerScreenTerminalStorage<?, ?> terminal)
                || !(terminal.getSelectedClientTab().orElse(null)
                        instanceof TerminalStorageTabIngredientComponentClient<?, ?> tab)) {
            return;
        }
        int first = terminal.getStorageSlotIndexAtPosition(
                terminal.getGuiLeftTotal() + tab.getSlotOffsetX() + 1,
                terminal.getGuiTopTotal() + tab.getSlotOffsetY() + 1);
        if (first < 0) {
            return;
        }
        var slots = tab.getSlots(
                terminal.getMenu().getSelectedChannel(), first, tab.getSlotRowLength() * tab.getSlotVisibleRows());
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).getInstance() instanceof ItemStack stack && !stack.isEmpty()) {
                Rect2i spot = terminal.getStorageSlotRect(first + i);
                visitor.visit(spot.getX(), spot.getY(), stack);
            }
        }
    }
}
