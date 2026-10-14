package dev.rylex.jeirecipepins.pin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import org.jetbrains.annotations.Nullable;

public final class PinGeometry {
    public static final int STACK_GAP = 4;

    /** JEI's navigation strip, its paddings and one 18 pixel slot row, the least room a JEI list needs above a pin. */
    public static final int LIST_MIN_HEIGHT = 48;

    public static final double MIN_SCALE = 0.5;
    public static final double MAX_SCALE = 2.0;
    private static final int SCALE_STEPS = 16;

    public record Span(int top, int height) {}

    public record Rect(int x, int y, int width, int height) {
        public int right() {
            return x + width;
        }

        public int bottom() {
            return y + height;
        }
    }

    private PinGeometry() {}

    public static int firstFreeTop(int topGap, int height, Span[] occupied) {
        Span[] sorted = occupied.clone();
        Arrays.sort(sorted, Comparator.comparingInt(Span::top));
        int candidate = topGap;
        for (Span span : sorted) {
            if (candidate + height + STACK_GAP <= span.top()) {
                return candidate;
            }
            candidate = Math.max(candidate, span.top() + span.height() + STACK_GAP);
        }
        return candidate;
    }

    public static int clamp(int position, int size, int limit) {
        if (size >= limit) {
            return 0;
        }
        return Math.max(0, Math.min(position, limit - size));
    }

    public static double clampScale(double scale) {
        return Math.clamp(scale, MIN_SCALE, MAX_SCALE);
    }

    /**
     * Projects the dragged corner onto the recipe's diagonal and snaps to sixteenths, so a drag along one edge still
     * resizes and 16 pixel slots land on whole pixels.
     */
    public static double scaleToReach(double width, double height, int baseWidth, int baseHeight) {
        double diagonal = (double) baseWidth * baseWidth + (double) baseHeight * baseHeight;
        double raw = (width * baseWidth + height * baseHeight) / diagonal;
        return clampScale(Math.round(raw * SCALE_STEPS) / (double) SCALE_STEPS);
    }

    /**
     * Pins the obstacle covers are pushed off the screen edge it sits against, and pins a pushed pin then lands on are
     * pushed after it.
     */
    public static List<Rect> avoid(List<Rect> pins, Rect obstacle, int screenHeight) {
        if (2 * obstacle.y() + obstacle.height() <= screenHeight) {
            return pushBelow(pins, obstacle);
        }
        List<Rect> mirrored =
                pins.stream().map(pin -> mirror(pin, screenHeight)).toList();
        return pushBelow(mirrored, mirror(obstacle, screenHeight)).stream()
                .map(pin -> mirror(pin, screenHeight))
                .toList();
    }

    private static List<Rect> pushBelow(List<Rect> pins, Rect obstacle) {
        Rect[] result = pins.toArray(new Rect[0]);
        List<Rect> walls = new ArrayList<>(List.of(obstacle));
        int[] order = IntStream.range(0, result.length)
                .boxed()
                .sorted(Comparator.comparingInt(i -> result[i].y()))
                .mapToInt(Integer::intValue)
                .toArray();
        for (int i : order) {
            Rect pin = result[i];
            Rect wall = firstOverlap(pin, walls);
            if (wall == null) {
                continue;
            }
            while (wall != null) {
                pin = new Rect(pin.x(), wall.bottom() + STACK_GAP, pin.width(), pin.height());
                wall = firstOverlap(pin, walls);
            }
            walls.add(pin);
            result[i] = pin;
        }
        return List.of(result);
    }

    @Nullable
    private static Rect firstOverlap(Rect pin, List<Rect> walls) {
        for (Rect wall : walls) {
            if (pin.x() < wall.right()
                    && pin.right() > wall.x()
                    && pin.y() < wall.bottom() + STACK_GAP
                    && pin.bottom() > wall.y()) {
                return wall;
            }
        }
        return null;
    }

    /**
     * JEI 19.64+ moves each overlay to wherever a band leaves it the most slots and drops its whole bookmark column
     * for any area touching the GUI's left edge.
     */
    public static List<Rect> sideBands(List<Rect> pins, int guiLeft, int guiRight, int screenWidth, int screenHeight) {
        List<Rect> bands = new ArrayList<>();
        for (int i = 0; i < pins.size(); i++) {
            Rect pin = pins.get(i);
            boolean left = pin.x() < guiLeft;
            boolean right = pin.right() > guiRight;
            if (left) {
                if (guiLeft > 1) {
                    bands.add(stretch(0, guiLeft - 1, pin, ceiling(pins, i, true, guiLeft, guiRight), screenHeight));
                }
                int inside = Math.min(pin.right(), guiRight) - guiLeft;
                if (inside > 0) {
                    bands.add(new Rect(guiLeft, pin.y(), inside, pin.height()));
                }
            }
            if (right) {
                int x = left ? guiRight : Math.min(pin.x(), guiRight);
                bands.add(stretch(x, screenWidth - x, pin, ceiling(pins, i, false, guiLeft, guiRight), screenHeight));
            }
            if (!left && !right) {
                bands.add(pin);
            }
        }
        return bands;
    }

    private static Rect stretch(int x, int width, Rect pin, int ceiling, int screenHeight) {
        if (pin.y() - ceiling >= LIST_MIN_HEIGHT) {
            return new Rect(x, pin.y(), width, screenHeight - pin.y());
        }
        return new Rect(x, 0, width, pin.bottom());
    }

    private static int ceiling(List<Rect> pins, int index, boolean leftSide, int guiLeft, int guiRight) {
        Rect pin = pins.get(index);
        int ceiling = 0;
        for (int i = 0; i < pins.size(); i++) {
            Rect other = pins.get(i);
            boolean sameSide = leftSide ? other.x() < guiLeft : other.right() > guiRight;
            if (i != index && sameSide && other.bottom() <= pin.y()) {
                ceiling = Math.max(ceiling, other.bottom());
            }
        }
        return ceiling;
    }

    public static int freeTop(int x, int top, int size, List<Rect> pins) {
        Span[] occupied = pins.stream()
                .filter(pin -> pin.x() < x + size && pin.right() > x)
                .map(pin -> new Span(pin.y(), pin.height()))
                .toArray(Span[]::new);
        return firstFreeTop(top, size, occupied);
    }

    public static boolean overlapsAny(Rect area, List<Rect> pins) {
        return firstOverlap(area, pins) != null;
    }

    private static Rect mirror(Rect rect, int screenHeight) {
        return new Rect(rect.x(), screenHeight - rect.bottom(), rect.width(), rect.height());
    }
}
