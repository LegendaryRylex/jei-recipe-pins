package dev.rylex.jeirecipepins;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JeiRecipePinsTest {
    @Test
    void modIdIsValid() {
        assertTrue(JeiRecipePins.MOD_ID.matches("[a-z][a-z0-9_]{1,63}"));
    }
}
