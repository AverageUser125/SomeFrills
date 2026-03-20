package com.somefrills.hud;

import com.google.common.collect.Lists;
import com.somefrills.config.Config;
import com.somefrills.config.FeatureRegistry;
import com.somefrills.config.SettingBool;
import com.somefrills.config.SettingInt;
import com.somefrills.config.SettingDouble;
import com.somefrills.config.SettingColor;
import com.somefrills.config.SettingEnum;
import com.somefrills.config.SettingString;
import com.somefrills.config.SettingKeybind;
import com.somefrills.config.SettingJson;
import java.util.Map;
import java.util.LinkedHashMap;
import com.somefrills.hud.components.FlatTextbox;
import com.somefrills.hud.components.PlainLabel;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Utils;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClickGui extends BaseOwoScreen<FlowLayout> {
    public List<Category> categories;
    public ScrollContainer<FlowLayout> mainScroll;
    public int mouseX = 0;
    public int mouseY = 0;

    private boolean matchSearch(String text, String search) {
        return Utils.toLower(text).replaceAll(" ", "").contains(Utils.toLower(search).replaceAll(" ", ""));
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() != GLFW.GLFW_KEY_LEFT && input.key() != GLFW.GLFW_KEY_RIGHT && input.key() != GLFW.GLFW_KEY_PAGE_DOWN && input.key() != GLFW.GLFW_KEY_PAGE_UP) {
            return super.keyPressed(input);
        } else {
            for (Category category : this.categories) {
                for (Module module : category.features) {
                    if (module.isInBoundingBox(this.mouseX, this.mouseY)) {
                        return category.scroll.onMouseScroll(0, 0, input.key() == GLFW.GLFW_KEY_PAGE_UP ? 4 : -4);
                    }
                }
            }
            return this.mainScroll.onMouseScroll(0, 0, input.key() == GLFW.GLFW_KEY_PAGE_UP ? 4 : -4);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (Category category : this.categories) {
            for (Module module : category.features) {
                if (module.isInBoundingBox(this.mouseX, this.mouseY)) {
                    return category.scroll.onMouseScroll(0, 0, verticalAmount * 2);
                }
            }
        }
        return this.mainScroll.onMouseScroll(0, 0, verticalAmount * 2);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        int height = context.getScaledWindowHeight();
        context.drawTextWithShadow(this.textRenderer, "Left click a feature to toggle", 1, height - 20, RenderColor.white.argb);
        context.drawTextWithShadow(this.textRenderer, "Right click a feature open its settings", 1, height - 10, RenderColor.white.argb);
    }

    @Override
    protected void build(FlowLayout root) {
        root.surface(Surface.VANILLA_TRANSLUCENT);
        FlowLayout parent = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        // build categories automatically from the discovered feature classes (grouped by package segment)
        this.categories = Lists.newArrayList();
        this.categories.addAll(buildCategoriesFromRegistry());

        // Add the Rewarp module separately (we don't want to list the raw JSON in the settings UI).
        // Place it under the 'Farming' category if present, otherwise create that category.
        try {
            Settings rewarpSettings = new Settings(
                    new Settings.BigButton("Add waypoint", btn -> com.somefrills.features.farming.Rewarp.addWaypoint()),
                    new Settings.BigButton("Remove last waypoint", btn -> com.somefrills.features.farming.Rewarp.removeLastWaypoint()),
                    new Settings.BigButton("Clear waypoints", btn -> com.somefrills.features.farming.Rewarp.clearWaypoints())
            );
            Module rewarpModule = new Module("Rewarp", com.somefrills.features.farming.Rewarp.instance, "Rewarp waypoints", rewarpSettings);

            // Try to find an existing Farming category by title
            Category farming = null;
            for (Category c : this.categories) {
                if (c != null && "Farming".equals(c.title)) {
                    farming = c;
                    break;
                }
            }

            if (farming != null) {
                // add to the category's feature list and UI
                farming.features.add(rewarpModule);
                rewarpModule.horizontalSizing(Sizing.fixed(farming.categoryWidth));
                farming.scroll.child().child(rewarpModule);
            } else {
                // create a new Farming category containing the rewarp module
                this.categories.add(new Category("Farming", List.of(rewarpModule)));
            }
        } catch (Throwable ignored) {
        }

        // keep an empty Misc category for miscellaneous items
        this.categories.add(new Category("Misc", List.of()));

        this.categories.getLast().margins(Insets.of(5, 0, 3, 3));
        for (Category category : this.categories) {
            parent.child(category);
        }
        this.mainScroll = Containers.horizontalScroll(Sizing.fill(100), Sizing.fill(100), parent);
        this.mainScroll.scrollbarThiccness(2).scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xffffffff)));
        root.child(this.mainScroll);
        FlatTextbox searchBox = new FlatTextbox(Sizing.fixed(200));
        searchBox.setSuggestion("Search...");
        searchBox.margins(Insets.of(0, 3, 0, 0));
        searchBox.positioning(Positioning.relative(50, 100));
        searchBox.onChanged().subscribe(value -> {
            if (value.isEmpty()) {
                searchBox.setSuggestion("Search...");
                for (Category category : this.categories) {
                    category.scroll.child().clearChildren();
                    for (Module module : category.features) {
                        module.horizontalSizing(Sizing.fixed(category.categoryWidth));
                        category.scroll.child().child(module);
                    }
                }
            } else {
                searchBox.setSuggestion("");
                for (Category category : this.categories) {
                    List<Module> features = getModules(value, category);
                    category.scroll.child().clearChildren();
                    for (Module module : features) {
                        module.horizontalSizing(Sizing.fixed(category.categoryWidth));
                        category.scroll.child().child(module);
                    }
                }
            }
        });
        root.child(searchBox);
    }

    private @NotNull List<Module> getModules(String value, Category category) {
        List<Module> features = new ArrayList<>(category.features);
        features.removeIf(feature -> {
            if (matchSearch(feature.label.getText(), value) || matchSearch(feature.label.getTooltip(), value)) {
                return false;
            }
            if (feature.options != null) {
                for (FlowLayout setting : feature.options.settings) {
                    for (Component child : setting.children()) {
                        if (child instanceof PlainLabel label) {
                            if (matchSearch(label.getText(), value) || matchSearch(label.getTooltip(), value)) {
                                return false;
                            }
                        }
                    }
                }
            }
            return true;
        });
        return features;
    }

    /**
     * Build categories automatically by grouping features according to the package
     * segment under `com.somefrills.features` (e.g. com.somefrills.features.farming -> "Farming").
     */
    private List<Category> buildCategoriesFromRegistry() {
        Map<String, List<Module>> groups = new LinkedHashMap<>();
        String prefix = "com.somefrills.features.";

        for (FeatureRegistry.FeatureInfo info : FeatureRegistry.getFeatures()) {
             String pkg = info.clazz.getPackageName();
             String group = "Other";
             if (pkg != null && pkg.startsWith(prefix)) {
                 String tail = pkg.substring(prefix.length());
                 if (tail.contains(".")) tail = tail.substring(0, tail.indexOf('.'));
                 group = tail;
             }
             String pretty = group.substring(0, 1).toUpperCase() + group.substring(1);

            // create module for this feature
            List<FlowLayout> optionLayouts = new ArrayList<>();

            for (var entry : info.settings.entrySet()) {
                  java.lang.reflect.Field field = entry.getKey();
                  Object setting = entry.getValue();
                  String settingName = field.getName();
                  String desc = info.descriptions.getOrDefault(field, "");
                  if (setting instanceof SettingBool sb) {
                     optionLayouts.add(new Settings.Toggle(settingName, sb, desc));
                 } else if (setting instanceof com.somefrills.config.SettingIntSlider sis) {
                     optionLayouts.add(new Settings.SliderInt(settingName, sis.min(), sis.max(), 1, sis, desc));
                 } else if (setting instanceof SettingInt si) {
                     optionLayouts.add(new Settings.NumberInputInt(settingName, si, desc));
                 } else if (setting instanceof SettingDouble sd) {
                     optionLayouts.add(new Settings.NumberInputDouble(settingName, sd, desc));
                 } else if (setting instanceof SettingColor sc) {
                     optionLayouts.add(new Settings.ColorPicker(settingName, sc, desc));
                 } else if (setting instanceof SettingEnum<?> se) {
                     optionLayouts.add(new Settings.Dropdown<>(settingName, se, desc));
                 } else if (setting instanceof SettingString ss) {
                     optionLayouts.add(new Settings.TextInput(settingName, ss, desc));
                 } else if (setting instanceof SettingKeybind sk) {
                     optionLayouts.add(new Settings.Keybind(settingName, sk, desc));
                 } else if (setting instanceof SettingJson) {
                     optionLayouts.add(new Settings.Description(settingName, "This setting contains JSON-managed data (use commands or config to edit)."));
                 } else {
                     optionLayouts.add(new Settings.Description(settingName, "Unsupported setting type in UI."));
                 }
            }

            Settings settingsScreen = optionLayouts.isEmpty() ? null : new Settings(optionLayouts);

            Module module = new Module(info.featureInstance.key().substring(0,1).toUpperCase() + info.featureInstance.key().substring(1), info.featureInstance, "Feature: " + info.featureInstance.key(), settingsScreen);
            groups.computeIfAbsent(pretty, k -> new ArrayList<>()).add(module);
        }

        List<Category> cats = new ArrayList<>();
        for (var e : groups.entrySet()) {
            cats.add(new Category(e.getKey(), e.getValue()));
        }
        return cats;
    }

    @Override
    public void close() {
        Config.save();
        if (this.uiAdapter != null) {
            this.uiAdapter.dispose();
        }
        super.close();
    }
}
