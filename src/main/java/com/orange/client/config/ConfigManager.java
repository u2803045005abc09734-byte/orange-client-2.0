package com.orange.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orange.client.module.ClientModule;
import com.orange.client.module.ModuleManager;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path file = FabricLoader.getInstance().getConfigDir().resolve("orange-client.json");
    private JsonObject root = new JsonObject();

    public void load() {
        if (!Files.exists(file)) {
            root = defaults();
            return;
        }
        try {
            String json = Files.readString(file);
            JsonObject parsed = JsonParser.parseString(json).getAsJsonObject();
            root = parsed;
            if (!root.has("modules") || !root.get("modules").isJsonObject()) {
                root = defaults();
            }
        } catch (Exception exception) {
            try {
                Path backup = file.resolveSibling("orange-client.json.broken");
                Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) { }
            root = defaults();
        }
    }

    private JsonObject defaults() {
        JsonObject object = new JsonObject();
        object.addProperty("version", 1);
        object.addProperty("theme", "purple");
        object.add("modules", new JsonObject());
        return object;
    }

    public boolean isModuleEnabled(String id) {
        if (!root.has("modules") || !root.get("modules").isJsonObject()) return false;
        JsonObject modules = root.getAsJsonObject("modules");
        return modules.has(id) && modules.get(id).getAsBoolean();
    }

    public String theme() {
        return root.has("theme") ? root.get("theme").getAsString() : "purple";
    }

    public void save(ModuleManager manager, String theme) {
        JsonObject output = new JsonObject();
        output.addProperty("version", 1);
        output.addProperty("theme", theme == null ? "purple" : theme);
        JsonObject states = new JsonObject();
        for (ClientModule module : manager.all()) states.addProperty(module.id(), module.enabled());
        output.add("modules", states);
        try {
            Files.createDirectories(file.getParent());
            Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temporary, GSON.toJson(output));
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException ignored) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
            root = output;
        } catch (IOException exception) {
            System.err.println("[Orange Client] Could not save config: " + exception.getMessage());
        }
    }
}
