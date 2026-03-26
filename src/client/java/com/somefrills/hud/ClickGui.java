package com.somefrills.hud;

import com.daqem.uilib.gui.AbstractScreen;
import com.daqem.uilib.gui.widget.EditBoxWidget;
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

        final int START_FEATURE_Y = 20;
        final int FEATURE_WIDTH = 145;
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
}



