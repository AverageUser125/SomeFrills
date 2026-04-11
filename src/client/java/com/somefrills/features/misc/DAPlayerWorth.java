package com.somefrills.features.misc;

import com.somefrills.config.Feature;
import com.somefrills.config.FrillsConfig;
import com.somefrills.misc.LobbyFinancialUtils;
import com.somefrills.misc.Utils;

public class DAPlayerWorth extends Feature {
    public DAPlayerWorth() {
        super(FrillsConfig.instance.misc.daPlayerWorth.enabled);
    }

    /**
     * Start fetching and displaying player financial data
     */
    public static void startFetching() {
        LobbyFinancialUtils.streamLobbyFinancials(financials -> {
            String totalMoney = Utils.formatNumber(financials.totalWealth);
            Utils.infoFormat("{} - {}", financials.playerName, totalMoney);
        });
    }
}
