package dev.rylex.jeirecipepins.pin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import org.jetbrains.annotations.Nullable;

public final class PinGeometry {
    public static final int STACK_GAP = 4;
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

    private static Rect mirror(Rect rect, int screenHeight) {
        return new Rect(rect.x(), screenHeight - rect.bottom(), rect.width(), rect.height());
    }
}
