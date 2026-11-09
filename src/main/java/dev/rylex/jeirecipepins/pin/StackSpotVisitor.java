package dev.rylex.jeirecipepins.pin;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface StackSpotVisitor {
    void visit(int x, int y, ItemStack stack);
}
