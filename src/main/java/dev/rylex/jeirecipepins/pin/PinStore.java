package dev.rylex.jeirecipepins.pin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.rylex.jeirecipepins.JeiRecipePins;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;

final class PinStore {
    record Entry(String type, JsonElement recipe, int x, int y, double scale) {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path file;

    private PinStore(Path file) {
        this.file = file;
    }

    static PinStore open(Minecraft minecraft, boolean perWorld) {
        Path dir = FMLPaths.CONFIGDIR.get().resolve(JeiRecipePins.MOD_ID).resolve("pins");
        return new PinStore(dir.resolve(perWorld ? currentWorldFile(minecraft) : PinFiles.GLOBAL));
    }

    private static String currentWorldFile(Minecraft minecraft) {
        IntegratedServer server = minecraft.getSingleplayerServer();
        if (server != null) {
            Path root = server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
            return PinFiles.world(root.getFileName().toString());
        }
        ServerData data = minecraft.getCurrentServer();
        if (data != null) {
            return PinFiles.server(data.ip);
        }
        return PinFiles.GLOBAL;
    }

    List<Entry> load() {
        List<Entry> entries = new ArrayList<>();
        if (!Files.isRegularFile(file)) {
            return entries;
        }
        try {
            JsonElement root = JsonParser.parseString(Files.readString(file));
            for (JsonElement element : root.getAsJsonObject().getAsJsonArray("pins")) {
                JsonObject pin = element.getAsJsonObject();
                entries.add(new Entry(
                        pin.get("type").getAsString(),
                        pin.get("recipe"),
                        pin.get("x").getAsInt(),
                        pin.get("y").getAsInt(),
                        pin.has("scale") ? pin.get("scale").getAsDouble() : 1.0));
            }
        } catch (IOException | RuntimeException e) {
            JeiRecipePins.LOGGER.warn("Could not read pinned recipes from {}", file, e);
        }
        return entries;
    }

    void save(List<PinnedRecipe> pins) {
        JsonArray array = new JsonArray();
        for (PinnedRecipe pin : pins) {
            JsonElement recipe = pin.encoded();
            if (recipe == null) {
                continue;
            }
            JsonObject object = new JsonObject();
            object.addProperty("type", pin.type().getUid().toString());
            object.add("recipe", recipe);
            object.addProperty("x", pin.x());
            object.addProperty("y", pin.homeY());
            object.addProperty("scale", pin.scale());
            array.add(object);
        }
        JsonObject root = new JsonObject();
        root.add("pins", array);
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(root));
        } catch (IOException e) {
            JeiRecipePins.LOGGER.warn("Could not save pinned recipes to {}", file, e);
        }
    }
}
