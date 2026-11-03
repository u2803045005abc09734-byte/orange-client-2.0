package com.orange.client.gui;

import com.orange.client.OrangeClient;
import com.orange.client.module.Category;
import com.orange.client.module.ClientModule;
import com.orange.client.module.ModuleManager;
import com.orange.client.render.Theme;
import com.orange.client.render.UiDraw;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class OrangeClickGuiScreen extends Screen {
    private static final String[] VISUAL_TABS = {"Players", "End crystals", "Hand", "Items", "Colours"};
    private static final String[] THEMES = {"purple", "orange", "blue"};

    private Category selectedCategory = Category.VISUALS;
    private int visualTab;
    private boolean rendersExpanded = true;
    private boolean searchActive;
    private String search = "";
    private int scrollOffset;
    private long openedAt;
    private int windowX, windowY, windowW, windowH;
    private int accent;
    private int contentTop;
    private int contentBottom;

    public OrangeClickGuiScreen() {
        super(Text.literal("Orange Client"));
        openedAt = System.currentTimeMillis();
    }

    @Override
    protected void init() {
        recalculateLayout();
    }

    private void recalculateLayout() {
        windowW = Math.min(width - 24, Math.max(520, (int) (width * 0.66f)));
        windowH = Math.min(height - 24, Math.max(480, (int) (height * 0.84f)));
        windowX = (width - windowW) / 2;
        windowY = (height - windowH) / 2;
        contentTop = windowY + 164;
        contentBottom = windowY + windowH - 28;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        OrangeClient orange = OrangeClient.instance();
        if (orange == null) return;
        accent = Theme.accent(orange.theme());
        recalculateLayout();

        context.fill(0, 0, width, height, Theme.BACKDROP);
        UiDraw.rounded(context, windowX + 2, windowY + 5, windowW, windowH, 24, 0x45000000);
        UiDraw.bordered(context, windowX, windowY, windowW, windowH, 24, Theme.WINDOW, Theme.BORDER);

        drawHeader(context, mouseX, mouseY, orange);
        drawNavigation(context, mouseX, mouseY);
        if (searchActive) drawSearchContent(context, mouseX, mouseY, orange.modules());
        else if (selectedCategory == Category.VISUALS) drawVisuals(context, mouseX, mouseY, orange);
        else drawCategory(context, mouseX, mouseY, orange.modules(), selectedCategory);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawHeader(DrawContext context, int mouseX, int mouseY, OrangeClient orange) {
        int x = windowX + 28;
        int y = windowY + 28;
        UiDraw.rounded(context, x, y + 1, 25, 25, 8, accent);
        UiDraw.text(context, "O", x + 7, y + 6, 0xFFFFFFFF);
        UiDraw.text(context, "Orange", x + 39, y + 3, Theme.TEXT);

        int iconX = windowX + windowW - 250;
        String[] icons = {"⌕", "☼", "◎", "◇", "▱"};
        for (int i = 0; i < icons.length; i++) {
            int bx = iconX + i * 44;
            boolean hover = UiDraw.inside(mouseX, mouseY, bx - 6, y - 3, 34, 34);
            if (hover) UiDraw.rounded(context, bx - 6, y - 3, 34, 34, 9, Theme.HOVER);
            UiDraw.centered(context, icons[i], bx + 11, y + 5, i == 0 && searchActive ? accent : Theme.MUTED);
        }

        if (searchActive) {
            int sx = windowX + 26;
            int sy = windowY + 68;
            int sw = Math.max(170, windowW - 52);
            UiDraw.bordered(context, sx, sy, sw, 34, 10, Theme.PANEL, Theme.BORDER);
            UiDraw.text(context, search.isEmpty() ? "Search modules..." : search, sx + 12, sy + 11,
                    search.isEmpty() ? Theme.DISABLED : Theme.TEXT);
            UiDraw.text(context, "×", sx + sw - 24, sy + 9, Theme.MUTED);
        }
    }

    private void drawNavigation(DrawContext context, int mouseX, int mouseY) {
        int y = windowY + 100;
        int x = windowX + 28;
        int tabWidth = Math.min(112, (windowW - 56) / 5);
        for (Category category : Category.values()) {
            int bx = x;
            boolean active = selectedCategory == category && !searchActive;
            boolean hover = UiDraw.inside(mouseX, mouseY, bx, y, tabWidth - 10, 42);
            if (hover && !active) UiDraw.rounded(context, bx - 7, y - 4, tabWidth - 2, 38, 8, Theme.HOVER);
            UiDraw.text(context, category.title(), bx, y + 9, active ? Theme.TEXT : Theme.MUTED);
            if (active) UiDraw.rounded(context, bx, y + 36, tabWidth - 34, 3, 2, accent);
            x += tabWidth;
        }
        context.fill(windowX + 26, y + 49, windowX + windowW - 26, y + 50, 0x55292B3C);
    }

    private void drawVisuals(DrawContext context, int mouseX, int mouseY, OrangeClient orange) {
        ModuleManager modules = orange.modules();
        int x = windowX + 18;
        int w = windowW - 36;
        int y = contentTop;
        int toggleX = x + w - 88;

        UiDraw.rounded(context, x, y, w, 54, 15, Theme.PANEL_ACTIVE);
        UiDraw.text(context, "Renders", x + 22, y + 19, Theme.TEXT);
        boolean anyRender = modules.isEnabled("player_glow") || modules.isEnabled("crystal_glow");
        UiDraw.toggle(context, toggleX, y + 13, anyRender, accent);
        UiDraw.text(context, rendersExpanded ? "⌄" : "›", x + w - 34, y + 18, Theme.accentSoft(orange.theme()));
        y += 66;

        if (rendersExpanded) {
            UiDraw.bordered(context, x + 6, y, w - 12, 196, 17, Theme.PANEL, 0xFF3A315F);
            int tabY = y + 18;
            int tabX = x + 28;
            int[] tabWidths = {104, 154, 84, 80, 100};
            for (int i = 0; i < VISUAL_TABS.length; i++) {
                int tw = tabWidths[i];
                UiDraw.text(context, VISUAL_TABS[i], tabX, tabY, visualTab == i ? Theme.TEXT : Theme.MUTED);
                if (visualTab == i) UiDraw.rounded(context, tabX, tabY + 26, Math.max(48, tw - 28), 3, 2, accent);
                tabX += tw;
            }
            context.fill(x + 28, y + 64, x + w - 28, y + 65, 0x55292B3C);
            drawVisualSubpage(context, x + 28, y + 80, w - 56, orange);
            y += 208;
        }

        List<ClientModule> rows = List.of(
                modules.get("nametags"),
                modules.get("hitboxes"),
                modules.get("fullbright"),
                modules.get("player_glow"),
                modules.get("crystal_glow")
        );
        drawModuleRows(context, mouseX, mouseY, x + 6, y, w - 12, rows, modules, orange);
    }

    private void drawVisualSubpage(DrawContext context, int x, int y, int width, OrangeClient orange) {
        ModuleManager manager = orange.modules();
        if (visualTab == 0) {
            drawSettingToggle(context, x, y + 4, width, "Glow", "player_glow", manager);
            UiDraw.text(context, "Vanilla player outline", x, y + 48, Theme.MUTED);
            UiDraw.text(context, "Outline colour is controlled by Minecraft's team colour system.", x, y + 68, Theme.DISABLED);
        } else if (visualTab == 1) {
            drawSettingToggle(context, x, y + 4, width, "Glow", "crystal_glow", manager);
            UiDraw.text(context, "Uses Minecraft's built-in glowing outline for End Crystals.", x, y + 48, Theme.MUTED);
        } else if (visualTab == 4) {
            UiDraw.text(context, "UI accent", x, y + 4, Theme.TEXT);
            int[] swatches = {Theme.accent("purple"), Theme.accent("orange"), Theme.accent("blue")};
            String[] names = {"Purple", "Orange", "Blue"};
            for (int i = 0; i < swatches.length; i++) {
                int sx = x + i * 112;
                UiDraw.rounded(context, sx, y + 30, 28, 28, 8, swatches[i]);
                UiDraw.text(context, names[i], sx + 38, y + 39, orange.theme().equals(THEMES[i]) ? Theme.TEXT : Theme.MUTED);
            }
        } else {
            String subject = visualTab == 2 ? "Hand rendering" : "Item rendering";
            UiDraw.text(context, subject + " effects are not part of this build yet.", x, y + 12, Theme.MUTED);
            UiDraw.text(context, "No inactive controls are shown as working settings.", x, y + 36, Theme.DISABLED);
        }
    }

    private void drawSettingToggle(DrawContext context, int x, int y, int width, String label, String moduleId, ModuleManager manager) {
        UiDraw.text(context, label, x, y + 8, Theme.TEXT);
        ClientModule module = manager.get(moduleId);
        if (module != null) UiDraw.toggle(context, x + width - 54, y, module.enabled(), accent);
    }

    private void drawCategory(DrawContext context, int mouseX, int mouseY, ModuleManager manager, Category category) {
        int x = windowX + 22;
        int y = contentTop;
        int w = windowW - 44;
        UiDraw.text(context, category.title(), x + 4, y + 2, Theme.TEXT);
        UiDraw.text(context, categoryDescription(category), x + 4, y + 24, Theme.MUTED);
        y += 54;
        drawModuleRows(context, mouseX, mouseY, x, y, w, manager.in(category), manager, OrangeClient.instance());
    }

    private String categoryDescription(Category category) {
        return switch (category) {
            case PVP -> "Client-side PvP utilities";
            case HUD -> "Toggle and arrange on-screen information";
            case PLAYER -> "Player view and movement options";
            case MISC -> "General ClickGUI preferences";
            case VISUALS -> "Visual rendering options";
        };
    }

    private void drawModuleRows(DrawContext context, int mouseX, int mouseY, int x, int y, int width,
                                List<ClientModule> modules, ModuleManager manager, OrangeClient orange) {
        int rowHeight = manager.isCompact() ? 48 : 58;
        int descriptionHeight = manager.showDescriptions() && !manager.isCompact() ? 14 : 0;
        int rowStride = rowHeight + descriptionHeight;
        int visibleHeight = Math.max(0, contentBottom - y);
        int maxScroll = Math.max(0, modules.size() * rowStride - visibleHeight);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        int drawY = y - scrollOffset;
        boolean any = false;
        String filter = searchActive ? search.toLowerCase(Locale.ROOT).trim() : "";

        for (ClientModule module : modules) {
            if (module == null) continue;
            if (!filter.isEmpty() && !(module.name().toLowerCase(Locale.ROOT).contains(filter)
                    || module.description().toLowerCase(Locale.ROOT).contains(filter))) continue;
            any = true;
            if (drawY + rowHeight >= y && drawY <= contentBottom) {
                boolean hover = UiDraw.inside(mouseX, mouseY, x, drawY, width, rowHeight - 5);
                UiDraw.rounded(context, x, drawY, width, rowHeight - 5, 11, hover ? Theme.HOVER : 0x00111121);
                UiDraw.text(context, module.name(), x + 18, drawY + 12, module.enabled() ? Theme.TEXT : Theme.MUTED);
                if (manager.showDescriptions() && !manager.isCompact()) {
                    UiDraw.text(context, module.description(), x + 18, drawY + 31, Theme.DISABLED);
                }
                UiDraw.toggle(context, x + width - 74, drawY + 9, module.enabled(), accent);
            }
            drawY += rowStride;
        }
        if (!any) {
            String message = searchActive ? "No modules found for this search." : "No modules are registered in this category.";
            UiDraw.text(context, message, x + 18, y + 20, Theme.MUTED);
        }
    }

    private void drawSearchContent(DrawContext context, int mouseX, int mouseY, ModuleManager manager) {
        int x = windowX + 22;
        int y = contentTop;
        UiDraw.text(context, "Search results", x + 4, y + 2, Theme.TEXT);
        UiDraw.text(context, "Search module names and descriptions", x + 4, y + 24, Theme.MUTED);
        List<ClientModule> results = new ArrayList<>();
        String filter = search.toLowerCase(Locale.ROOT).trim();
        if (!filter.isEmpty()) {
            for (ClientModule module : manager.all()) {
                if (module.name().toLowerCase(Locale.ROOT).contains(filter)
                        || module.description().toLowerCase(Locale.ROOT).contains(filter)) results.add(module);
            }
        }
        drawModuleRows(context, mouseX, mouseY, x, y + 54, windowW - 44, results, manager, OrangeClient.instance());
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_1) return super.mouseClicked(click, doubled);
        OrangeClient orange = OrangeClient.instance();
        if (orange == null) return true;
        int headerY = windowY + 25;
        int iconX = windowX + windowW - 250;
        if (mouseY >= headerY - 4 && mouseY <= headerY + 34) {
            for (int i = 0; i < 5; i++) {
                int bx = iconX + i * 44 - 6;
                if (UiDraw.inside(mouseX, mouseY, bx, headerY - 3, 34, 34)) {
                    handleHeaderAction(i, orange);
                    return true;
                }
            }
        }

        if (searchActive && UiDraw.inside(mouseX, mouseY, windowX + 24, windowY + 66, windowW - 48, 38)) {
            if (mouseX > windowX + windowW - 60) search = "";
            return true;
        }

        int navY = windowY + 100;
        int navX = windowX + 28;
        int tabWidth = Math.min(112, (windowW - 56) / 5);
        for (Category category : Category.values()) {
            if (UiDraw.inside(mouseX, mouseY, navX - 6, navY - 4, tabWidth - 2, 44)) {
                selectedCategory = category;
                searchActive = false;
                search = "";
                scrollOffset = 0;
                return true;
            }
            navX += tabWidth;
        }

        if (selectedCategory == Category.VISUALS && !searchActive) {
            int x = windowX + 18;
            int w = windowW - 36;
            int y = contentTop;
            if (UiDraw.inside(mouseX, mouseY, x, y, w, 54)) {
                if (mouseX >= x + w - 120) toggleRenderMaster(orange);
                else rendersExpanded = !rendersExpanded;
                return true;
            }
            y += 66;
            if (rendersExpanded && UiDraw.inside(mouseX, mouseY, x + 6, y, w - 12, 196)) {
                int tabX = x + 28;
                int[] widths = {104, 154, 84, 80, 100};
                for (int i = 0; i < widths.length; i++) {
                    if (UiDraw.inside(mouseX, mouseY, tabX - 6, y + 4, widths[i], 34)) {
                        visualTab = i;
                        return true;
                    }
                    tabX += widths[i];
                }
                if (visualTab == 4) {
                    int swatchY = y + 110;
                    for (int i = 0; i < 3; i++) {
                        int sx = x + 28 + i * 112;
                        if (UiDraw.inside(mouseX, mouseY, sx, swatchY - 4, 104, 36)) {
                            orange.setTheme(THEMES[i]);
                            return true;
                        }
                    }
                }
                if (visualTab == 0 && UiDraw.inside(mouseX, mouseY, x + 28, y + 80, w - 56, 44)) {
                    toggleModule(orange, "player_glow");
                    return true;
                }
                if (visualTab == 1 && UiDraw.inside(mouseX, mouseY, x + 28, y + 80, w - 56, 44)) {
                    toggleModule(orange, "crystal_glow");
                    return true;
                }
                return true;
            }
            int rowY = contentTop + (rendersExpanded ? 274 : 66);
            List<String> ids = List.of("nametags", "hitboxes", "fullbright", "player_glow", "crystal_glow");
            for (String id : ids) {
                if (UiDraw.inside(mouseX, mouseY, x + 6, rowY, w - 12, 56)) {
                    toggleModule(orange, id);
                    return true;
                }
                rowY += (orange.modules().isCompact() ? 48 : 58)
                        + (orange.modules().showDescriptions() && !orange.modules().isCompact() ? 14 : 0);
            }
        } else {
            Category target = selectedCategory;
            List<ClientModule> visible = new ArrayList<>();
            if (searchActive) {
                String filter = search.toLowerCase(Locale.ROOT).trim();
                for (ClientModule module : orange.modules().all()) {
                    if (!filter.isEmpty() && (module.name().toLowerCase(Locale.ROOT).contains(filter)
                            || module.description().toLowerCase(Locale.ROOT).contains(filter))) visible.add(module);
                }
            } else visible.addAll(orange.modules().in(target));

            int y = contentTop + (searchActive ? 54 : 54);
            int rowHeight = orange.modules().isCompact() ? 48 : 58;
            int rowStride = rowHeight + (orange.modules().showDescriptions() && !orange.modules().isCompact() ? 14 : 0);
            int rowIndex = 0;
            for (ClientModule module : visible) {
                int rowY = y + rowIndex * rowStride - scrollOffset;
                if (UiDraw.inside(mouseX, mouseY, windowX + 22, rowY, windowW - 44, rowHeight - 4)) {
                    if (searchActive) selectedCategory = module.category();
                    toggleModule(orange, module.id());
                    return true;
                }
                rowIndex++;
            }
        }
        return false;
    }

    private void handleHeaderAction(int action, OrangeClient orange) {
        switch (action) {
            case 0 -> {
                searchActive = !searchActive;
                if (!searchActive) search = "";
            }
            case 1 -> {
                int index = 0;
                for (int i = 0; i < THEMES.length; i++) if (THEMES[i].equals(orange.theme())) index = i;
                orange.setTheme(THEMES[(index + 1) % THEMES.length]);
            }
            case 2 -> {
                selectedCategory = Category.PLAYER;
                searchActive = false;
            }
            case 3 -> {
                selectedCategory = Category.VISUALS;
                visualTab = 4;
                searchActive = false;
            }
            case 4 -> {
                selectedCategory = Category.MISC;
                searchActive = false;
            }
        }
    }

    private void toggleRenderMaster(OrangeClient orange) {
        boolean enable = !orange.modules().isEnabled("player_glow") && !orange.modules().isEnabled("crystal_glow");
        setModule(orange, "player_glow", enable);
        setModule(orange, "crystal_glow", enable);
        orange.save();
    }

    private void toggleModule(OrangeClient orange, String id) {
        ClientModule module = orange.modules().get(id);
        if (module == null) return;
        module.toggle(MinecraftClient.getInstance());
        orange.save();
    }

    private void setModule(OrangeClient orange, String id, boolean enabled) {
        ClientModule module = orange.modules().get(id);
        if (module != null) module.setEnabled(enabled, MinecraftClient.getInstance());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset = Math.max(0, scrollOffset - (int) (verticalAmount * 28));
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.key();
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (searchActive) {
                searchActive = false;
                search = "";
            } else close();
            return true;
        }
        if (searchActive && keyCode == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1);
            return true;
        }
        if (searchActive && keyCode == GLFW.GLFW_KEY_ENTER) {
            searchActive = false;
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (searchActive && input.isValidChar() && search.length() < 48) {
            search += input.asString();
            return true;
        }
        return super.charTyped(input);
    }

    @Override
    public void removed() {
        OrangeClient orange = OrangeClient.instance();
        if (orange != null) orange.save();
        super.removed();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
