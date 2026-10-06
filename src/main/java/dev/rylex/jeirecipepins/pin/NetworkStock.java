package dev.rylex.jeirecipepins.pin;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public record NetworkStock(List<ItemStack> stored, List<ItemStack> craftable) {
    public static final NetworkStock EMPTY = new NetworkStock(List.of(), List.of());
}
