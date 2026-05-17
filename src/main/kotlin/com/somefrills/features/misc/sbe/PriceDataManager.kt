package com.somefrills.features.misc.sbe

import com.somefrills.Main
import com.somefrills.features.misc.PriceDataCacheManager
import com.somefrills.features.misc.SteganosProxyClient
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object PriceDataManager {

    private val lock = ReentrantLock()

    fun getLowestBin(): String? = lock.withLock {
        val fresh = SteganosProxyClient.lowestBin()
        if (fresh != null) {
            PriceDataCacheManager.saveLowestBin(fresh)
            return fresh
        }
        Main.LOGGER.warn("Failed to fetch fresh lowestbin data, falling back to cache")
        return PriceDataCacheManager.loadLowestBin()
    }

    fun getBazaar(): String? = lock.withLock {
        val fresh = SteganosProxyClient.bazaar()
        if (fresh != null) {
            PriceDataCacheManager.saveBazaar(fresh)
            return fresh
        }
        Main.LOGGER.warn("Failed to fetch fresh bazaar data, falling back to cache")
        return PriceDataCacheManager.loadBazaar()
    }
}