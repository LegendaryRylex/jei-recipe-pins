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
    void pinsLeftOfTheGuiBecomeOneBandDownToTheLowestPin() {
        List<Rect> pins = List.of(new Rect(4, 24, 120, 60), new Rect(4, 88, 90, 40));
        assertEquals(List.of(new Rect(0, 0, 200, 128)), PinGeometry.exclusionAreas(pins, 200, 400, 600));
    }

    @Test
    void pinsRightOfTheGuiBandTheRightSide() {
        List<Rect> pins = List.of(new Rect(420, 10, 120, 60));
        assertEquals(List.of(new Rect(400, 0, 200, 70)), PinGeometry.exclusionAreas(pins, 200, 400, 600));
    }

    @Test
    void aPinOverTheGuiKeepsItsOwnRectangle() {
        List<Rect> pins = List.of(new Rect(150, 10, 120, 60));
        assertEquals(pins, PinGeometry.exclusionAreas(pins, 200, 400, 600));
    }
}
