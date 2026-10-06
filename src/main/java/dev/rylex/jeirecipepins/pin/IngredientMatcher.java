package dev.rylex.jeirecipepins.pin;

import dev.rylex.jeirecipepins.compat.ae2.Ae2Compat;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

final class IngredientMatcher {

    private IngredientMatcher() {}

    static Set<Object> uids(IRecipeSlotView slot, IIngredientManager ingredients) {
        IIngredientHelper<ItemStack> helper = ingredients.getIngredientHelper(VanillaTypes.ITEM_STACK);
        return slot.getItemStacks()
                .map(stack -> helper.getUid(stack, UidContext.Recipe))
                .collect(Collectors.toUnmodifiableSet());
    }

    static void update(
            IIngredientManager ingredients,
            @Nullable Player player,
            @Nullable AbstractContainerMenu menu,
            NetworkStock carried,
            List<PinnedRecipe> pins,
            BitSet containerSlots) {
        containerSlots.clear();
        if (player == null || pins.isEmpty()) {
            for (PinnedRecipe pin : pins) {
                mark(pin, Set.of(), Set.of());
            }
            return;
        }
        IIngredientHelper<ItemStack> helper = ingredients.getIngredientHelper(VanillaTypes.ITEM_STACK);
        Set<Object> wanted = new HashSet<>();
        for (PinnedRecipe pin : pins) {
            pin.inputUids().forEach(wanted::addAll);
        }
        Set<Object> available = new HashSet<>();
        Set<Object> craftable = new HashSet<>();
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                available.add(helper.getUid(stack, UidContext.Recipe));
            }
        }
        if (menu != null) {
            List<Slot> slots = menu.slots;
            for (int i = 0; i < slots.size(); i++) {
                Slot slot = slots.get(i);
                if (!slot.hasItem()) {
                    continue;
                }
                Object uid = helper.getUid(slot.getItem(), UidContext.Recipe);
                if (!Ae2Compat.isTerminalSlot(slot)) {
                    available.add(uid);
                }
                if (!(slot.container instanceof Inventory) && wanted.contains(uid)) {
                    containerSlots.set(i);
                }
            }
            Ae2Compat.networkStock(menu, () -> wantedItems(pins))
                    .ifPresent(stock -> addStock(stock, helper, available, craftable));
        }
        addStock(carried, helper, available, craftable);
        for (PinnedRecipe pin : pins) {
            mark(pin, available, craftable);
        }
    }

    private static void addStock(
            NetworkStock stock, IIngredientHelper<ItemStack> helper, Set<Object> available, Set<Object> craftable) {
        stock.stored().forEach(stack -> available.add(helper.getUid(stack, UidContext.Recipe)));
        stock.craftable().forEach(stack -> craftable.add(helper.getUid(stack, UidContext.Recipe)));
    }

    static Set<Item> wantedItems(List<PinnedRecipe> pins) {
        Set<Item> items = new HashSet<>();
        for (PinnedRecipe pin : pins) {
            for (IRecipeSlotView input : pin.inputs()) {
                input.getItemStacks().forEach(stack -> items.add(stack.getItem()));
            }
        }
        return items;
    }

    private static void mark(PinnedRecipe pin, Set<Object> available, Set<Object> craftable) {
        List<Set<Object>> uids = pin.inputUids();
        for (int i = 0; i < uids.size(); i++) {
            Set<Object> wanted = uids.get(i);
            if (wanted.isEmpty() || !Collections.disjoint(wanted, available)) {
                pin.setStatus(i, InputStatus.PRESENT);
            } else if (!Collections.disjoint(wanted, craftable)) {
                pin.setStatus(i, InputStatus.CRAFTABLE);
            } else {
                pin.setStatus(i, InputStatus.MISSING);
            }
        }
    }
}
