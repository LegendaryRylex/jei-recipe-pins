package dev.rylex.jeirecipepins.pin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class PinGeometry {
    public static final int STACK_GAP = 4;

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

    /**
     * JEI hides only the overlay slots a rectangle covers and shifts the grid only when its page buttons are covered, so
     * pins beside the GUI are reported as a band across that whole side to make the overlay always start below them.
     */
    public static List<Rect> exclusionAreas(List<Rect> pins, int guiLeft, int guiRight, int screenWidth) {
        List<Rect> areas = new ArrayList<>();
        int leftBottom = 0;
        int rightBottom = 0;
        for (Rect pin : pins) {
            if (pin.right() <= guiLeft) {
                leftBottom = Math.max(leftBottom, pin.bottom());
            } else if (pin.x() >= guiRight) {
                rightBottom = Math.max(rightBottom, pin.bottom());
            } else {
                areas.add(pin);
            }
        }
        if (leftBottom > 0) {
            areas.add(new Rect(0, 0, guiLeft, leftBottom));
        }
        if (rightBottom > 0) {
            areas.add(new Rect(guiRight, 0, screenWidth - guiRight, rightBottom));
        }
        return areas;
    }
}
