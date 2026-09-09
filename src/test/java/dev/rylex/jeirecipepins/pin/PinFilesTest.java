package dev.rylex.jeirecipepins.pin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PinFilesTest {
    @Test
    void worldFilesLiveUnderLocal() {
        assertEquals("local/new_world.json", PinFiles.world("New World"));
    }

    @Test
    void serverFilesKeepHostAndPortApart() {
        assertEquals("server/play.example.org_25566.json", PinFiles.server("play.example.org:25566"));
    }

    @Test
    void emptyNamesStillGetAFile() {
        assertEquals("unnamed", PinFiles.sanitize(""));
    }
}
