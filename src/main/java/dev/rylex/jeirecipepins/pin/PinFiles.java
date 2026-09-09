package dev.rylex.jeirecipepins.pin;

import java.util.Locale;

public final class PinFiles {
    public static final String GLOBAL = "global.json";

    private PinFiles() {}

    public static String world(String levelName) {
        return "local/" + sanitize(levelName) + ".json";
    }

    public static String server(String address) {
        return "server/" + sanitize(address) + ".json";
    }

    static String sanitize(String name) {
        String clean = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]+", "_");
        return clean.isEmpty() ? "unnamed" : clean;
    }
}
