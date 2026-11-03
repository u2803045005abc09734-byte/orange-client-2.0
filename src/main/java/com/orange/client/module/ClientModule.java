package com.orange.client.module;

import net.minecraft.client.MinecraftClient;

import java.util.Objects;
import java.util.function.Consumer;

public final class ClientModule {
    private final String id;
    private final String name;
    private final String description;
    private final Category category;
    private final Consumer<MinecraftClient> tickAction;
    private final Consumer<MinecraftClient> enableAction;
    private final Consumer<MinecraftClient> disableAction;
    private boolean enabled;

    public ClientModule(
            String id,
            String name,
            String description,
            Category category,
            Consumer<MinecraftClient> tickAction,
            Consumer<MinecraftClient> enableAction,
            Consumer<MinecraftClient> disableAction
    ) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        this.category = Objects.requireNonNull(category);
        this.tickAction = tickAction == null ? client -> { } : tickAction;
        this.enableAction = enableAction == null ? client -> { } : enableAction;
        this.disableAction = disableAction == null ? client -> { } : disableAction;
    }

    public void setEnabled(boolean enabled, MinecraftClient client) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) enableAction.accept(client);
        else disableAction.accept(client);
    }

    public void toggle(MinecraftClient client) {
        setEnabled(!enabled, client);
    }

    public void tick(MinecraftClient client) {
        if (enabled) tickAction.accept(client);
    }

    public String id() { return id; }
    public String name() { return name; }
    public String description() { return description; }
    public Category category() { return category; }
    public boolean enabled() { return enabled; }
}
