package com.orange.client.module;

import com.orange.client.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.client.gui.hud.debug.DebugHudEntryVisibility;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class ModuleManager {
    private final List<ClientModule> modules = new ArrayList<>();
    private final Map<Entity, Boolean> previousPlayerGlowStates = new IdentityHashMap<>();
    private final Map<Entity, Boolean> previousCrystalGlowStates = new IdentityHashMap<>();
    private final Map<PlayerEntity, NameState> previousNames = new IdentityHashMap<>();
    private DebugHudEntryVisibility originalHitboxVisibility;
    private boolean originalBobView;
    private double originalGamma;

    private record NameState(Text name, boolean visible) { }

    public ModuleManager() {
        registerDefaults();
    }

    private void registerDefaults() {
        add(new ClientModule(
                "auto_sprint", "Auto Sprint", "Automatically sprint while moving forward.", Category.PVP,
                client -> {
                    if (client.player == null || client.currentScreen != null) return;
                    boolean movingForward = client.options.forwardKey.isPressed();
                    boolean sneaking = client.options.sneakKey.isPressed();
                    if (movingForward && !sneaking && !client.player.isUsingItem() && !client.player.isTouchingWater()) {
                        client.player.setSprinting(true);
                    }
                }, null, null
        ));

        add(new ClientModule(
                "nametags", "Nametags", "Show local name labels above other players.", Category.VISUALS,
                client -> {
                    if (client.world == null || client.player == null) return;
                    for (PlayerEntity player : client.world.getPlayers()) {
                        if (player == client.player) continue;
                        previousNames.putIfAbsent(player, new NameState(player.getCustomName(), player.isCustomNameVisible()));
                        if (player.getCustomName() == null) player.setCustomName(Text.literal(player.getName().getString()));
                        player.setCustomNameVisible(true);
                    }
                }, null, client -> restoreNames()
        ));

        add(new ClientModule(
                "hitboxes", "Hitboxes", "Enable Minecraft's built-in entity hitbox debug overlay.", Category.VISUALS,
                null,
                client -> {
                    originalHitboxVisibility = client.debugHudEntryList.getVisibility(DebugHudEntries.ENTITY_HITBOXES);
                    client.debugHudEntryList.setEntryVisibility(DebugHudEntries.ENTITY_HITBOXES, DebugHudEntryVisibility.ALWAYS_ON);
                },
                client -> {
                    if (originalHitboxVisibility != null) {
                        client.debugHudEntryList.setEntryVisibility(DebugHudEntries.ENTITY_HITBOXES, originalHitboxVisibility);
                        originalHitboxVisibility = null;
                    }
                }
        ));

        add(new ClientModule(
                "fullbright", "Fullbright", "Set the vanilla gamma option to its maximum value.", Category.VISUALS,
                client -> {
                    if (client.options != null) client.options.getGamma().setValue(1.0D);
                },
                client -> {
                    if (client.options != null) {
                        originalGamma = client.options.getGamma().getValue();
                        client.options.getGamma().setValue(1.0D);
                    }
                },
                client -> {
                    if (client.options != null) client.options.getGamma().setValue(originalGamma);
                }
        ));

        add(new ClientModule(
                "player_glow", "Player Glow", "Apply vanilla glowing outlines to other players locally.", Category.VISUALS,
                client -> {
                    if (client.world == null || client.player == null) return;
                    for (PlayerEntity player : client.world.getPlayers()) {
                        if (player == client.player) continue;
                        rememberAndGlow(player, previousPlayerGlowStates);
                    }
                }, null, client -> restoreGlowStates(previousPlayerGlowStates)
        ));

        add(new ClientModule(
                "crystal_glow", "Crystal Glow", "Apply vanilla glowing outlines to end crystals locally.", Category.VISUALS,
                client -> {
                    if (client.world == null || client.player == null) return;
                    for (EndCrystalEntity entity : client.world.getEntitiesByClass(EndCrystalEntity.class, client.player.getBoundingBox().expand(128.0), crystal -> true)) {
                        rememberAndGlow(entity, previousCrystalGlowStates);
                    }
                }, null, client -> restoreGlowStates(previousCrystalGlowStates)
        ));

        add(new ClientModule(
                "coordinates", "Coordinates", "Display your current block coordinates on the HUD.", Category.HUD,
                null, null, null
        ));
        add(new ClientModule(
                "fps_counter", "FPS Counter", "Display the current frame rate on the HUD.", Category.HUD,
                null, null, null
        ));
        add(new ClientModule(
                "armor_status", "Armor Status", "Display equipped armor durability on the HUD.", Category.HUD,
                null, null, null
        ));
        add(new ClientModule(
                "clock", "Clock", "Display the local system time on the HUD.", Category.HUD,
                null, null, null
        ));

        add(new ClientModule(
                "no_bob", "No View Bobbing", "Disable camera bobbing while this module is enabled.", Category.PLAYER,
                client -> client.options.getBobView().setValue(false),
                client -> {
                    originalBobView = client.options.getBobView().getValue();
                    client.options.getBobView().setValue(false);
                },
                client -> client.options.getBobView().setValue(originalBobView)
        ));

        add(new ClientModule(
                "compact_layout", "Compact Layout", "Use denser rows and spacing in the ClickGUI.", Category.MISC,
                null, null, null
        ));
        add(new ClientModule(
                "module_descriptions", "Module Descriptions", "Show a short explanation below module names.", Category.MISC,
                null, null, null
        ));
    }

    private void rememberAndGlow(Entity entity, Map<Entity, Boolean> stateMap) {
        stateMap.putIfAbsent(entity, entity.isGlowing());
        entity.setGlowing(true);
    }

    private void restoreGlowStates(Map<Entity, Boolean> stateMap) {
        for (Map.Entry<Entity, Boolean> entry : stateMap.entrySet()) {
            Entity entity = entry.getKey();
            if (!entity.isRemoved()) entity.setGlowing(entry.getValue());
        }
        stateMap.clear();
    }

    private void restoreNames() {
        for (Map.Entry<PlayerEntity, NameState> entry : previousNames.entrySet()) {
            PlayerEntity player = entry.getKey();
            if (!player.isRemoved()) {
                player.setCustomName(entry.getValue().name());
                player.setCustomNameVisible(entry.getValue().visible());
            }
        }
        previousNames.clear();
    }

    private void add(ClientModule module) {
        modules.add(module);
    }

    public List<ClientModule> all() {
        return List.copyOf(modules);
    }

    public List<ClientModule> in(Category category) {
        return modules.stream().filter(module -> module.category() == category).toList();
    }

    public ClientModule get(String id) {
        return modules.stream().filter(module -> module.id().equals(id)).findFirst().orElse(null);
    }

    public void tick(MinecraftClient client) {
        previousPlayerGlowStates.keySet().removeIf(Entity::isRemoved);
        previousCrystalGlowStates.keySet().removeIf(Entity::isRemoved);
        previousNames.keySet().removeIf(Entity::isRemoved);
        for (ClientModule module : modules) module.tick(client);
    }

    public void applySavedState(ConfigManager config, MinecraftClient client) {
        for (ClientModule module : modules) module.setEnabled(config.isModuleEnabled(module.id()), client);
    }

    public void save(ConfigManager config, String theme) {
        config.save(this, theme);
    }

    public boolean isEnabled(String id) {
        ClientModule module = get(id);
        return module != null && module.enabled();
    }

    public boolean isCompact() {
        return isEnabled("compact_layout");
    }

    public boolean showDescriptions() {
        return isEnabled("module_descriptions");
    }

    public void renderHud(MinecraftClient client, DrawContext context) {
        if (client.player == null || client.world == null) return;
        int x = 8;
        int y = 8;
        int line = 0;
        if (isEnabled("coordinates")) {
            String coords = "XYZ " + client.player.getBlockX() + " / " + client.player.getBlockY() + " / " + client.player.getBlockZ();
            drawHudLine(client, context, x, y + line++ * 12, coords);
        }
        if (isEnabled("fps_counter")) {
            drawHudLine(client, context, x, y + line++ * 12, "FPS " + client.getCurrentFps());
        }
        if (isEnabled("armor_status")) {
            List<String> armor = new ArrayList<>();
            for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
                ItemStack stack = client.player.getEquippedStack(slot);
                if (!stack.isEmpty() && stack.isDamageable()) {
                    int remaining = Math.max(0, stack.getMaxDamage() - stack.getDamage());
                    int percent = Math.round(remaining * 100.0f / Math.max(1, stack.getMaxDamage()));
                    armor.add(percent + "%");
                }
            }
            if (!armor.isEmpty()) drawHudLine(client, context, x, y + line++ * 12, "Armor " + String.join("  ", armor));
        }
        if (isEnabled("clock")) {
            drawHudLine(client, context, x, y + line * 12, LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }
    }

    private void drawHudLine(MinecraftClient client, DrawContext context, int x, int y, String value) {
        int width = client.textRenderer.getWidth(value) + 10;
        context.fill(x - 4, y - 3, x + width, y + 10, 0x990B0D14);
        context.drawText(client.textRenderer, value, x, y - 1, 0xFFF3F3FA, true);
    }
}
