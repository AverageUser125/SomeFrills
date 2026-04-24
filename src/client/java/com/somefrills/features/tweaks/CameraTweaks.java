package com.somefrills.features.tweaks;

import com.somefrills.config.FrillsConfig;
import com.somefrills.config.tweaks.TweaksCategory.CameraTweaksConfig;
import com.somefrills.features.core.PassiveFeature;

public class CameraTweaks extends PassiveFeature {
    private final CameraTweaksConfig config;

    public CameraTweaks() {
        super();
        config = FrillsConfig.instance.tweaks.cameraTweaks;
    }

    public boolean clip() {
        return config.clip;
    }
}
