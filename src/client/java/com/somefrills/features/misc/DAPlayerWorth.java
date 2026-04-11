package com.somefrills.features.misc;

import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;

public class DAPlayerWorth extends Feature {
    public DAPlayerWorth() {
        super(FrillsConfig.instance.misc.daPlayerWorth.enabled);
    }


}
