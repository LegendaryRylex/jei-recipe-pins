package dev.rylex.jeirecipepins.pin;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import dev.rylex.jeirecipepins.JeiRecipePins;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class PinnedRecipe {
    public static final int PAD = 3;
    public static final int TITLE_HEIGHT = 12;
    public static final int GRIP = 6;

    private final IRecipeLayoutDrawable<?> layout;
    private final RecipeType<?> type;

    @Nullable
    private final ResourceLocation registryName;

    @Nullable
    private final JsonElement encoded;

    private final Component title;
    private final int border;
    private final int innerWidth;
    private final int innerHeight;
    private final List<IRecipeSlotView> inputs;
    private final List<Set<Object>> inputUids;
    private final boolean[] missing;
    private int x;
    private int y;
    private int nudge;
    private double scale = 1.0;

    private PinnedRecipe(
            IRecipeLayoutDrawable<?> layout,
            RecipeType<?> type,
            @Nullable ResourceLocation registryName,
            @Nullable JsonElement encoded,
            Component title,
            List<IRecipeSlotView> inputs,
            List<Set<Object>> inputUids) {
        layout.setPosition(0, 0);
        Rect2i rect = layout.getRect();
        Rect2i withBorder = layout.getRectWithBorder();
        this.layout = layout;
        this.type = type;
        this.registryName = registryName;
        this.encoded = encoded;
        this.title = title;
        this.border = rect.getX() - withBorder.getX();
        this.innerWidth = withBorder.getWidth();
        this.innerHeight = withBorder.getHeight();
        this.inputs = inputs;
        this.inputUids = inputUids;
        this.missing = new boolean[inputs.size()];
    }

    static <T> Optional<PinnedRecipe> create(
            IJeiRuntime runtime, IRecipeCategory<T> category, T recipe, RegistryOps<JsonElement> ops) {
        IRecipeManager recipes = runtime.getRecipeManager();
        IJeiHelpers helpers = runtime.getJeiHelpers();
        IIngredientManager ingredients = runtime.getIngredientManager();
        return recipes.createRecipeLayoutDrawable(
                        category, recipe, helpers.getFocusFactory().getEmptyFocusGroup())
                .map(layout -> {
                    ResourceLocation registryName = category.getRegistryName(recipe);
                    JsonElement encoded = registryName == null ? null : encode(category, recipe, runtime, ops);
                    List<IRecipeSlotView> inputs =
                            layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.INPUT).stream()
                                    .filter(slot -> !slot.isEmpty())
                                    .toList();
                    List<Set<Object>> uids = inputs.stream()
                            .map(slot -> IngredientMatcher.uids(slot, ingredients))
                            .toList();
                    return new PinnedRecipe(
                            layout,
                            category.getRecipeType(),
                            registryName,
                            encoded,
                            title(layout, ingredients),
                            inputs,
                            uids);
                });
    }

    static <T> Optional<PinnedRecipe> decode(
            IJeiRuntime runtime, RecipeType<T> type, JsonElement json, RegistryOps<JsonElement> ops) {
        IRecipeCategory<T> category = runtime.getRecipeManager().getRecipeCategory(type);
        Codec<T> codec = category.getCodec(runtime.getJeiHelpers().getCodecHelper(), runtime.getRecipeManager());
        return codec.parse(ops, json)
                .resultOrPartial(
                        error -> JeiRecipePins.LOGGER.warn("Dropping pinned {} recipe: {}", type.getUid(), error))
                .flatMap(recipe -> create(runtime, category, recipe, ops));
    }

    @Nullable
    private static <T> JsonElement encode(
            IRecipeCategory<T> category, T recipe, IJeiRuntime runtime, RegistryOps<JsonElement> ops) {
        Codec<T> codec = category.getCodec(runtime.getJeiHelpers().getCodecHelper(), runtime.getRecipeManager());
        return codec.encodeStart(ops, recipe)
                .resultOrPartial(error -> JeiRecipePins.LOGGER.warn(
                        "Pinned {} recipe will not survive a restart: {}",
                        category.getRecipeType().getUid(),
                        error))
                .orElse(null);
    }

    private static Component title(IRecipeLayoutDrawable<?> layout, IIngredientManager ingredients) {
        for (IRecipeSlotView slot : layout.getRecipeSlotsView().getSlotViews(RecipeIngredientRole.OUTPUT)) {
            Optional<ITypedIngredient<?>> shown = slot.getDisplayedIngredient();
            if (shown.isPresent()) {
                return Component.literal(displayName(shown.get(), ingredients));
            }
        }
        return layout.getRecipeCategory().getTitle();
    }

    private static <V> String displayName(ITypedIngredient<V> typed, IIngredientManager ingredients) {
        return ingredients.getIngredientHelper(typed.getType()).getDisplayName(typed.getIngredient());
    }

    static <T> boolean sameRecipe(PinnedRecipe pin, IRecipeLayoutDrawable<T> other) {
        if (!pin.type.equals(other.getRecipeCategory().getRecipeType())) {
            return false;
        }
        ResourceLocation otherName = other.getRecipeCategory().getRegistryName(other.getRecipe());
        if (pin.registryName != null && otherName != null) {
            return pin.registryName.equals(otherName);
        }
        return pin.layout.getRecipe() == other.getRecipe();
    }

    public IRecipeLayoutDrawable<?> layout() {
        return layout;
    }

    public RecipeType<?> type() {
        return type;
    }

    @Nullable
    public JsonElement encoded() {
        return encoded;
    }

    public Component title() {
        return title;
    }

    public int border() {
        return border;
    }

    public List<IRecipeSlotView> inputs() {
        return inputs;
    }

    List<Set<Object>> inputUids() {
        return inputUids;
    }

    public boolean isMissing(int input) {
        return missing[input];
    }

    void setMissing(int input, boolean value) {
        missing[input] = value;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y + nudge;
    }

    public int width() {
        return scaled(innerWidth) + 2 * PAD;
    }

    public int height() {
        return TITLE_HEIGHT + scaled(innerHeight) + 2 * PAD;
    }

    public double scale() {
        return scale;
    }

    public void resize(double scale) {
        this.scale = PinGeometry.clampScale(scale);
    }

    public void resizeToCorner(double right, double bottom) {
        scale = PinGeometry.scaleToReach(right - PAD - bodyX(), bottom - PAD - bodyY(), innerWidth, innerHeight);
    }

    private int scaled(int length) {
        return (int) Math.round(length * scale);
    }

    public int bodyX() {
        return x + PAD;
    }

    public int bodyY() {
        return y() + TITLE_HEIGHT + PAD;
    }

    public double toBodyX(double screenX) {
        return (screenX - bodyX()) / scale;
    }

    public double toBodyY(double screenY) {
        return (screenY - bodyY()) / scale;
    }

    public Rect2i toScreen(int recipeX, int recipeY, int width, int height) {
        return new Rect2i(
                (int) Math.round(bodyX() + (border + recipeX) * scale),
                (int) Math.round(bodyY() + (border + recipeY) * scale),
                scaled(width),
                scaled(height));
    }

    /**
     * Places the unscaled layout so the recipe point drawn under this screen point sits on it, which keeps JEI's hit
     * tests and tooltips in screen space at any scale.
     */
    public void alignLayoutTo(double screenX, double screenY) {
        layout.setPosition(
                (int) Math.round(screenX - toBodyX(screenX)) + border,
                (int) Math.round(screenY - toBodyY(screenY)) + border);
    }

    public int homeY() {
        return y;
    }

    public void moveTo(int x, int y) {
        this.x = x;
        this.y = y;
        nudge = 0;
    }

    public void nudgeTo(int displayY) {
        nudge = displayY - y;
    }

    public void clampTo(int width, int height) {
        x = PinGeometry.clamp(x, width(), width);
        y = PinGeometry.clamp(y, height(), height);
    }

    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseY >= y() && mouseX < x + width() && mouseY < y() + height();
    }

    public boolean inTitleBar(double mouseX, double mouseY) {
        return contains(mouseX, mouseY) && mouseY < y() + TITLE_HEIGHT;
    }

    public boolean onCloseButton(double mouseX, double mouseY) {
        return inTitleBar(mouseX, mouseY) && mouseX >= x + width() - TITLE_HEIGHT;
    }

    public boolean onResizeGrip(double mouseX, double mouseY) {
        return contains(mouseX, mouseY) && mouseX >= x + width() - GRIP && mouseY >= y() + height() - GRIP;
    }
}
