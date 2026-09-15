package dev.rylex.jeirecipepins.pin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.rylex.jeirecipepins.pin.PinGeometry.Rect;
import dev.rylex.jeirecipepins.pin.PinGeometry.Span;
import java.util.List;
import org.junit.jupiter.api.Test;

class PinGeometryTest {
    @Test
    void firstPinStartsAtTheTopGap() {
        assertEquals(24, PinGeometry.firstFreeTop(24, 50, new Span[0]));
    }

    @Test
    void nextPinStacksBelowTheLowestPinWhenNothingIsFree() {
        Span[] occupied = {new Span(24, 40), new Span(68, 30)};
        assertEquals(98 + PinGeometry.STACK_GAP, PinGeometry.firstFreeTop(24, 50, occupied));
    }

    @Test
    void anEvictedPinsSlotIsReusedBeforeStackingLower() {
        Span[] occupied = {new Span(68, 30), new Span(102, 30)};
        assertEquals(24, PinGeometry.firstFreeTop(24, 40, occupied));
    }

    @Test
    void aGapTooSmallForThePanelIsSkipped() {
        Span[] occupied = {new Span(24, 20), new Span(60, 30)};
        assertEquals(90 + PinGeometry.STACK_GAP, PinGeometry.firstFreeTop(24, 40, occupied));
    }

    @Test
    void clampKeepsThePanelInsideTheWindow() {
        assertEquals(0, PinGeometry.clamp(-5, 50, 200));
        assertEquals(150, PinGeometry.clamp(180, 50, 200));
        assertEquals(20, PinGeometry.clamp(20, 50, 200));
    }

    @Test
    void clampFallsBackToTheOriginWhenThePanelIsWiderThanTheWindow() {
        assertEquals(0, PinGeometry.clamp(30, 300, 200));
    }

    @Test
    void aPinUnderATopObstacleMovesBelowIt() {
        List<Rect> pins = List.of(new Rect(4, 24, 120, 60));
        assertEquals(List.of(new Rect(4, 38, 120, 60)), PinGeometry.avoid(pins, new Rect(0, 0, 68, 34), 240));
    }

    @Test
    void aPinThePushedPinLandsOnIsPushedAfterIt() {
        List<Rect> pins = List.of(new Rect(4, 88, 90, 40), new Rect(4, 24, 120, 60), new Rect(4, 200, 90, 20));
        assertEquals(
                List.of(new Rect(4, 102, 90, 40), new Rect(4, 38, 120, 60), new Rect(4, 200, 90, 20)),
                PinGeometry.avoid(pins, new Rect(0, 0, 68, 34), 400));
    }

    @Test
    void pinsClearOfTheObstacleStayPut() {
        List<Rect> pins = List.of(new Rect(100, 10, 120, 60), new Rect(4, 40, 60, 30));
        assertEquals(pins, PinGeometry.avoid(pins, new Rect(0, 0, 68, 34), 240));
    }

    @Test
    void aBottomObstaclePushesPinsUp() {
        List<Rect> pins = List.of(new Rect(4, 180, 100, 40));
        assertEquals(List.of(new Rect(4, 160, 100, 40)), PinGeometry.avoid(pins, new Rect(0, 204, 68, 34), 240));
    }

    @Test
    void resizingFollowsTheDraggedCorner() {
        assertEquals(1.5, PinGeometry.scaleToReach(150, 75, 100, 50));
    }

    @Test
    void resizingAlongOneEdgeStillScales() {
        assertEquals(1.8125, PinGeometry.scaleToReach(200, 50, 100, 50));
    }

    @Test
    void resizingSnapsToSixteenths() {
        assertEquals(1.0, PinGeometry.scaleToReach(103, 50, 100, 50));
    }

    @Test
    void resizingStaysWithinTheScaleRange() {
        assertEquals(PinGeometry.MAX_SCALE, PinGeometry.scaleToReach(1000, 1000, 100, 50));
        assertEquals(PinGeometry.MIN_SCALE, PinGeometry.scaleToReach(0, 0, 100, 50));
    }
}
