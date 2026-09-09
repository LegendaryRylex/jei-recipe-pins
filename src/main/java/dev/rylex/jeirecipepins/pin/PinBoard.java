package dev.rylex.jeirecipepins.pin;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.rylex.jeirecipepins.config.PinsConfig;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public final class PinBoard {
    private static final PinBoard INSTANCE = new PinBoard();

    private final List<PinnedRecipe> pins = new ArrayList<>();
    private final BitSet containerSlots = new BitSet();

    @Nullable
    private IJeiRuntime runtime;

    @Nullable
    private PinStore store;

    private boolean loadPending;
    private boolean visible = true;

    private PinBoard() {}

    public static PinBoard get() {
        return INSTANCE;
    }

    /** JEI recycles its runtime on every world join and resource reload, so pins are reloaded from disk each time. */
    public void runtimeAvailable(IJeiRuntime runtime) {
        this.runtime = runtime;
        pins.clear();
        loadPending = true;
    }

    public void runtimeUnavailable() {
        runtime = null;
        store = null;
        pins.clear();
        loadPending = false;
    }

    public boolean isReady() {
        return runtime != null;
    }

    public Optional<IJeiRuntime> runtime() {
        return Optional.ofNullable(runtime);
    }

    public List<PinnedRecipe> pins() {
        return Collections.unmodifiableList(pins);
    }

    public BitSet containerSlots() {
        return containerSlots;
    }

    public boolean isVisible() {
        return visible;
    }

    public void toggleVisible() {
        visible = !visible;
    }

    public void tick(Minecraft minecraft) {
        if (runtime == null) {
            return;
        }
        if (loadPending && minecraft.level != null) {
            loadPending = false;
            load(minecraft);
        }
        for (PinnedRecipe pin : pins) {
            pin.layout().tick();
        }
        AbstractContainerMenu menu =
                minecraft.screen instanceof AbstractContainerScreen<?> screen ? screen.getMenu() : null;
        IngredientMatcher.update(runtime.getIngredientManager(), minecraft.player, menu, pins, containerSlots);
    }

    public boolean isPinned(IRecipeLayoutDrawable<?> layout) {
        return find(layout).isPresent();
    }

    public void toggle(IRecipeLayoutDrawable<?> layout) {
        find(layout).ifPresentOrElse(this::unpin, () -> pin(layout));
    }

    private Optional<PinnedRecipe> find(IRecipeLayoutDrawable<?> layout) {
        return pins.stream().filter(pin -> PinnedRecipe.sameRecipe(pin, layout)).findFirst();
    }

    private <T> void pin(IRecipeLayoutDrawable<T> source) {
        Minecraft minecraft = Minecraft.getInstance();
        if (runtime == null || minecraft.level == null) {
            return;
        }
        IRecipeCategory<T> category = source.getRecipeCategory();
        PinnedRecipe.create(runtime, category, source.getRecipe(), ops(minecraft))
                .ifPresent(pin -> {
                    add(pin);
                    save();
                });
    }

    private void add(PinnedRecipe pin) {
        while (pins.size() >= PinsConfig.maxPins()) {
            pins.removeFirst();
        }
        int left = PinsConfig.leftGap();
        pin.moveTo(
                left, PinGeometry.firstFreeTop(PinsConfig.topGap(), pin.height(), occupiedColumn(left, pin.width())));
        pins.add(pin);
    }

    private PinGeometry.Span[] occupiedColumn(int left, int width) {
        return pins.stream()
                .filter(pin -> pin.x() < left + width && pin.x() + pin.width() > left)
                .map(pin -> new PinGeometry.Span(pin.y(), pin.height()))
                .toArray(PinGeometry.Span[]::new);
    }

    public void unpin(PinnedRecipe pin) {
        if (pins.remove(pin)) {
            save();
        }
    }

    public void clear() {
        pins.clear();
        save();
    }

    public void snapAll() {
        int top = PinsConfig.topGap();
        for (PinnedRecipe pin : pins) {
            pin.moveTo(PinsConfig.leftGap(), top);
            top += pin.height() + PinGeometry.STACK_GAP;
        }
        save();
    }

    public void bringToFront(PinnedRecipe pin) {
        if (pins.remove(pin)) {
            pins.add(pin);
        }
    }

    public Optional<PinnedRecipe> pinAt(double mouseX, double mouseY) {
        for (int i = pins.size() - 1; i >= 0; i--) {
            PinnedRecipe pin = pins.get(i);
            if (pin.contains(mouseX, mouseY)) {
                return Optional.of(pin);
            }
        }
        return Optional.empty();
    }

    public void save() {
        if (store != null) {
            store.save(pins);
        }
    }

    private void load(Minecraft minecraft) {
        store = PinStore.open(minecraft, PinsConfig.perWorld());
        RegistryOps<JsonElement> ops = ops(minecraft);
        for (PinStore.Entry entry : store.load()) {
            runtime.getRecipeManager()
                    .getRecipeType(Identifier.parse(entry.type()))
                    .flatMap(type -> decode(type, entry.recipe(), ops))
                    .ifPresent(pin -> {
                        add(pin);
                        pin.moveTo(entry.x(), entry.y());
                    });
        }
    }

    private <T> Optional<PinnedRecipe> decode(IRecipeType<T> type, JsonElement recipe, RegistryOps<JsonElement> ops) {
        return PinnedRecipe.decode(runtime, type, recipe, ops);
    }

    private static RegistryOps<JsonElement> ops(Minecraft minecraft) {
        return minecraft.level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
    }
}
