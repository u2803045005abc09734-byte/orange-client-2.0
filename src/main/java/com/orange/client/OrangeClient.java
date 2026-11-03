package com.orange.client;

import com.orange.client.config.ConfigManager;
import com.orange.client.gui.OrangeClickGuiScreen;
import com.orange.client.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class OrangeClient implements ClientModInitializer {
    private static OrangeClient instance;

    private final ConfigManager config = new ConfigManager();
    private final ModuleManager modules = new ModuleManager();
    private KeyBinding clickGuiKey;
    private boolean configApplied;
    private String theme = "purple";

    @Override
    public void onInitializeClient() {
        instance = this;
        config.load();
        theme = config.theme();
        clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.orange.clickgui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.create(Identifier.of("orange", "client"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> save());
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world != null && client.player != null) modules.renderHud(client, context);
        });
    }

    private void onClientTick(MinecraftClient client) {
        if (!configApplied) {
            modules.applySavedState(config, client);
            configApplied = true;
        }

        while (clickGuiKey.wasPressed()) {
            if (client.currentScreen instanceof OrangeClickGuiScreen) {
                client.setScreen(null);
            } else if (canOpenClickGui(client)) {
                client.setScreen(new OrangeClickGuiScreen());
            }
        }

        if (client.currentScreen instanceof OrangeClickGuiScreen && !isInLoadedWorld(client)) {
            client.setScreen(null);
        }

        modules.tick(client);
    }

    private boolean canOpenClickGui(MinecraftClient client) {
        return client.currentScreen == null && isInLoadedWorld(client);
    }

    private boolean isInLoadedWorld(MinecraftClient client) {
        return client != null
                && client.player != null
                && client.world != null
                && client.player.getEntityWorld() == client.world;
    }

    public void save() {
        modules.save(config, theme);
    }

    public void setTheme(String theme) {
        this.theme = theme;
        save();
    }

    public String theme() { return theme; }
    public ConfigManager config() { return config; }
    public ModuleManager modules() { return modules; }
    public static OrangeClient instance() { return instance; }
}
