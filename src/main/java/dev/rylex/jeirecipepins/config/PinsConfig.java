package dev.rylex.jeirecipepins.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class PinsConfig {

    private PinsConfig() {}

    public static final ModConfigSpec SPEC;

    private static final boolean DEFAULT_SHOW_IN_WORLD = true;
    private static final double DEFAULT_IN_WORLD_SCALE = 1.0;
    private static final int DEFAULT_TOP_GAP = 24;
    private static final int DEFAULT_LEFT_GAP = 4;
    private static final int DEFAULT_MAX_PINS = 3;
    private static final boolean DEFAULT_PER_WORLD = true;
    private static final boolean DEFAULT_HIGHLIGHT_CONTAINER_SLOTS = true;
    private static final boolean DEFAULT_MARK_MISSING_INPUTS = true;

    private static final ModConfigSpec.BooleanValue SHOW_IN_WORLD;
    private static final ModConfigSpec.DoubleValue IN_WORLD_SCALE;
    private static final ModConfigSpec.IntValue TOP_GAP;
    private static final ModConfigSpec.IntValue LEFT_GAP;
    private static final ModConfigSpec.IntValue MAX_PINS;
    private static final ModConfigSpec.BooleanValue PER_WORLD;
    private static final ModConfigSpec.BooleanValue HIGHLIGHT_CONTAINER_SLOTS;
    private static final ModConfigSpec.BooleanValue MARK_MISSING_INPUTS;

    static {
        var builder = new ModConfigSpec.Builder();
        builder.push("display");
        SHOW_IN_WORLD = builder.comment("Draw pinned recipes over the world while no screen is open.")
                .translation("jeirecipepins.configuration.display.show_in_world")
                .define("show_in_world", DEFAULT_SHOW_IN_WORLD);
        IN_WORLD_SCALE = builder.comment(
                        "Size of pinned recipes over the world, relative to their size inside a screen.")
                .translation("jeirecipepins.configuration.display.in_world_scale")
                .defineInRange("in_world_scale", DEFAULT_IN_WORLD_SCALE, 0.25, 2.0);
        TOP_GAP = builder.comment("Pixels kept free above the first pinned recipe.")
                .translation("jeirecipepins.configuration.display.top_gap")
                .defineInRange("top_gap", DEFAULT_TOP_GAP, 0, 200);
        LEFT_GAP = builder.comment("Pixels kept free to the left of pinned recipes.")
                .translation("jeirecipepins.configuration.display.left_gap")
                .defineInRange("left_gap", DEFAULT_LEFT_GAP, 0, 200);
        builder.pop();
        builder.push("pins");
        MAX_PINS = builder.comment("How many recipes can be pinned at once. Pinning one more unpins the oldest.")
                .translation("jeirecipepins.configuration.pins.max_pins")
                .defineInRange("max_pins", DEFAULT_MAX_PINS, 1, 10);
        PER_WORLD = builder.comment("Keep a separate set of pins for every world and server instead of one shared set.")
                .translation("jeirecipepins.configuration.pins.per_world")
                .define("per_world", DEFAULT_PER_WORLD);
        builder.pop();
        builder.push("highlight");
        HIGHLIGHT_CONTAINER_SLOTS = builder.comment(
                        "Tint the slots of an open container that hold an input of a pinned recipe.")
                .translation("jeirecipepins.configuration.highlight.container_slots")
                .define("container_slots", DEFAULT_HIGHLIGHT_CONTAINER_SLOTS);
        MARK_MISSING_INPUTS = builder.comment(
                        "Tint the inputs of a pinned recipe that are neither in your inventory nor in the open container.")
                .translation("jeirecipepins.configuration.highlight.missing_inputs")
                .define("missing_inputs", DEFAULT_MARK_MISSING_INPUTS);
        builder.pop();
        SPEC = builder.build();
    }

    public static boolean showInWorld() {
        return SPEC.isLoaded() ? SHOW_IN_WORLD.get() : DEFAULT_SHOW_IN_WORLD;
    }

    public static double inWorldScale() {
        return SPEC.isLoaded() ? IN_WORLD_SCALE.get() : DEFAULT_IN_WORLD_SCALE;
    }

    public static int topGap() {
        return SPEC.isLoaded() ? TOP_GAP.get() : DEFAULT_TOP_GAP;
    }

    public static int leftGap() {
        return SPEC.isLoaded() ? LEFT_GAP.get() : DEFAULT_LEFT_GAP;
    }

    public static int maxPins() {
        return SPEC.isLoaded() ? MAX_PINS.get() : DEFAULT_MAX_PINS;
    }

    public static boolean perWorld() {
        return SPEC.isLoaded() ? PER_WORLD.get() : DEFAULT_PER_WORLD;
    }

    public static boolean highlightContainerSlots() {
        return SPEC.isLoaded() ? HIGHLIGHT_CONTAINER_SLOTS.get() : DEFAULT_HIGHLIGHT_CONTAINER_SLOTS;
    }

    public static boolean markMissingInputs() {
        return SPEC.isLoaded() ? MARK_MISSING_INPUTS.get() : DEFAULT_MARK_MISSING_INPUTS;
    }
}
