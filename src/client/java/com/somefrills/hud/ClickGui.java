package com.somefrills.hud;

import com.daqem.uilib.gui.AbstractScreen;
import com.somefrills.config.Config;
import com.somefrills.config.FeatureRegistry;
import com.somefrills.config.FeatureRegistry.FeatureInfo;
import com.somefrills.hud.components.FeatureWidget;
import com.somefrills.misc.Utils;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.*;

public class ClickGui extends AbstractScreen {
    private final List<CategoryData> categories = new ArrayList<>();

    // transient layout info used for click detection
    private final Map<FeatureInfo, Rect> featureBounds = new HashMap<>();
    // NOTE: global input forwarding / event hook removed. Rely on AbstractScreen/widget input handling.

    public ClickGui() {
        super(Component.literal("SomeFrills - Click GUI"));
    }

    @Override
    protected void init() {
        super.init();
        // Build categories by grouping features by their package segment
        Map<String, List<FeatureInfo>> byCategory = new TreeMap<>();
        for (FeatureInfo info : FeatureRegistry.getFeatures()) {
            String pkg = info.clazz.getPackage() != null ? info.clazz.getPackage().getName() : "";
            String[] parts = pkg.split("\\.");
            String cat = parts.length > 0 ? parts[parts.length - 1] : "misc";
            if ("features".equals(cat) && parts.length > 1) cat = parts[parts.length - 2];
            cat = Utils.humanize(cat);
            byCategory.computeIfAbsent(cat, k -> new ArrayList<>()).add(info);
        }

        for (Map.Entry<String, List<FeatureInfo>> e : byCategory.entrySet()) {
            this.categories.add(new CategoryData(e.getKey(), e.getValue()));
        }

        // create search box (vanilla EditBox works with ui-lib)
        int sbWidth = 200;
        EditBox searchBox = new EditBox(this.font, 10, 10, sbWidth, 20, Component.literal("Search"));
        searchBox.setValue("");
        searchBox.setResponder(this::onSearchChanged);
        this.addRenderableWidget(searchBox);
        this.refreshSearchResults("");

        final int START_FEATURE_Y = 40;
        final int FEATURE_WIDTH = 150;
        final int FEATURE_HEIGHT = 20;
        int startX = 10;
        int startY = START_FEATURE_Y;
        for (CategoryData category : this.categories) {
            for (FeatureInfo info : category.features) {
                addWidget(new FeatureWidget(startX, startY, FEATURE_WIDTH, FEATURE_HEIGHT, info));
                startY += FEATURE_HEIGHT + 10;
            }
            startY = START_FEATURE_Y;
            startX += FEATURE_WIDTH + 5;
        }
    }

    private void onSearchChanged(String value) {
        this.refreshSearchResults(value);
    }


    private boolean matchSearch(String text, String search) {
        if (text == null) return false;
        if (search == null || search.isEmpty()) return true;
        return Utils.toLower(text).replaceAll(" ", "").contains(Utils.toLower(search).replaceAll(" ", ""));
    }

    private void refreshSearchResults(String value) {
        for (CategoryData category : this.categories) {
            List<FeatureInfo> features = new ArrayList<>(category.features);
            if (value != null && !value.isEmpty()) {
                features.removeIf(info -> {
                    if (matchSearch(info.name, value) || matchSearch(info.description, value)) return false;
                    for (FeatureRegistry.SettingInfo entry : info.settings) {
                        if (matchSearch(entry.name(), value) || matchSearch(entry.description(), value)) return false;
                    }
                    return true;
                });
            }
            category.filteredFeatures.clear();
            category.filteredFeatures.addAll(features);
        }
    }


    @Override
    public void onClose() {
        // persist config
        Config.save();
        super.onClose();
    }

    public static class CategoryData {
        public final String name;
        public final List<FeatureInfo> features;
        public final List<FeatureInfo> filteredFeatures = new ArrayList<>();

        public CategoryData(String name, List<FeatureInfo> features) {
            this.name = name;
            this.features = features;
            this.filteredFeatures.addAll(features);
        }
    }

    private static class Rect {
        int x, y, w, h;

        Rect(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
}



