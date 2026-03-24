package com.somefrills.hud.clickgui;

import com.google.common.collect.Lists;
import com.somefrills.config.*;
import com.somefrills.config.FeatureRegistry.FeatureInfo;
import com.somefrills.hud.clickgui.components.FlatTextbox;
import com.somefrills.hud.clickgui.components.PlainLabel;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClickGui extends BaseOwoScreen<FlowLayout> {
    public List<Category> categories;
    public ScrollContainer<FlowLayout> mainScroll;
    public int mouseX = 0;
    public int mouseY = 0;

    private static String humanize(String in) {
        if (in == null || in.isEmpty()) return "";
        // replace underscores/dashes with spaces, split camelCase and capitalize words
        String withSpaces = in.replace('_', ' ').replace('-', ' ');
        StringBuilder out = new StringBuilder();
        char prev = ' ';
        for (int i = 0; i < withSpaces.length(); i++) {
            char c = withSpaces.charAt(i);
            if (i > 0 && Character.isUpperCase(c) && (Character.isLowerCase(prev) || Character.isDigit(prev))) {
                out.append(' ');
            }
            out.append(c);
            prev = c;
        }
        String result = out.toString().trim();
        if (result.isEmpty()) return result;
        return Character.toUpperCase(result.charAt(0)) + result.substring(1);
    }

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
        // Build categories from the reflection-based FeatureRegistry. We group features by the
        // last segment of their package (e.g. com.somefrills.features.farming -> "Farming").
        this.categories = Lists.newArrayList();
        Map<String, List<Module>> byCategory = new HashMap<>();
        for (FeatureInfo info : FeatureRegistry.getFeatures()) {
            // derive category name from package
            String pkg = info.clazz.getPackage() != null ? info.clazz.getPackage().getName() : "";
            String[] parts = pkg.split("\\.");
            String cat = parts.length > 0 ? parts[parts.length - 1] : "misc";
            if (cat.equals("features") && parts.length > 1) {
                // if package ends with .features use previous segment
                cat = parts.length > 1 ? parts[parts.length - 2] : "misc";
            }
            cat = humanize(cat);

            // build Settings screen for this feature (if it has settings)
            List<FlowLayout> optionLayouts = new ArrayList<>();
            for (Map.Entry<Field, SettingGeneric> entry : info.settings.entrySet()) {
                Field f = entry.getKey();
                SettingGeneric setting = entry.getValue();
                String name = humanize(f.getName());
                String tooltip = info.descriptions.getOrDefault(f, "");

                if (setting instanceof SettingBool sb) {
                    optionLayouts.add(new Settings.Toggle(name, sb, tooltip));
                } else if (setting instanceof SettingDouble sd) {
                    optionLayouts.add(new Settings.DoubleInput(name, sd, tooltip));
                } else if (setting instanceof SettingIntSlider sis) {
                    optionLayouts.add(new Settings.SliderInt(name, sis.min(), sis.max(), 1, sis, tooltip));
                } else if (setting instanceof SettingInt si) {
                    optionLayouts.add(new Settings.NumberInputInt(name, si, tooltip));
                } else if (setting instanceof SettingEnum<?> se) {
                    optionLayouts.add(new Settings.Dropdown<>(name, se, tooltip));
                } else if (setting instanceof SettingColor sc) {
                    optionLayouts.add(new Settings.ColorPicker(name, sc, tooltip));
                } else if (setting instanceof SettingString ss) {
                    optionLayouts.add(new Settings.TextInput(name, ss, tooltip));
                } else if (setting instanceof SettingKeybind sk) {
                    optionLayouts.add(new Settings.Keybind(name, sk, tooltip));
                } else if (setting instanceof SettingJson sj) {
                    optionLayouts.add(new Settings.Description(name, tooltip != null ? tooltip : "JSON setting"));
                } else if (setting instanceof com.somefrills.config.SettingBlockPosList sbl) {
                    optionLayouts.add(new Settings.BlockPosList(name, sbl, tooltip));
                } else {
                    // fallback: show as description
                    optionLayouts.add(new Settings.Description(name, tooltip != null ? tooltip : ""));
                }
            }

            Settings optionsScreen = optionLayouts.isEmpty() ? null : new Settings(optionLayouts);

            String moduleName = humanize(info.featureInstance.key());
            String tooltip = info.featureInstance.key();
            Module module = new Module(moduleName, info.featureInstance, tooltip, optionsScreen);

            byCategory.computeIfAbsent(cat, k -> new ArrayList<>()).add(module);
        }

        for (Map.Entry<String, List<Module>> e : byCategory.entrySet()) {
            Category c = new Category(e.getKey(), e.getValue());
            this.categories.add(c);
        }

        if (!this.categories.isEmpty()) {
            this.categories.get(this.categories.size() - 1).margins(Insets.of(5, 0, 3, 3));
        }
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

    @Override
    public void close() {
        Config.save();
        if (this.uiAdapter != null) {
            this.uiAdapter.dispose();
        }
        super.close();
    }
}
