package com.somefrills.mixin.skyblock_enhancements;

import com.somefrills.features.misc.PriceDataCacheManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.somefrills.Main.LOGGER;

@Pseudo
@Mixin(targets = "com.github.kd_gaming1.skyblockenhancements.feature.pricing.PriceDataFetcher", remap = false)
public class PriceDataFetcherMixin {

    /**
     * Session-wide flag: once set to true, HTTP fetches are permanently disabled
     * and cached data is used instead.
     */
    @Unique
    private static boolean somefrills$httpFailed = false;

    /**
     * Injects at HEAD of httpGet() to skip HTTP if already failed.
     */
    @Inject(
            method = "httpGet",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void somefrills$httpGet_checkFailed(String url, CallbackInfoReturnable<String> cir) {
        if (somefrills$httpFailed) {
            String cached = somefrills$getCachedForUrl(url);
            if (cached != null) {
                LOGGER.debug("Using cached price data for {}", url);
                cir.setReturnValue(cached);
            }
        }
    }

    /**
     * Injects after successful httpGet() to cache the response.
     */
    @Inject(
            method = "httpGet",
            at = @At("RETURN")
    )
    private static void somefrills$httpGet_cacheResult(String url, CallbackInfoReturnable<String> cir) {
        String response = cir.getReturnValue();
        if (response != null && !somefrills$httpFailed) {
            somefrills$saveCacheForUrl(url, response);
        }
    }

    /**
     * Intercepts the tick() method to skip HTTP requests if httpFailed is set.
     */
    @Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable = true
    )
    public void somefrills$tick_skipIfHttpFailed(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (somefrills$httpFailed) {
            LOGGER.trace("Skipping price refresh - HTTP disabled for session");
            ci.cancel();
        }
    }

    /**
     * Injects into refreshAsync's catch block to set httpFailed flag when fetch fails.
     */
    @Inject(
            method = "refreshAsync",
            at = @At(value = "INVOKE",
                    target = "Lcom/github/kd_gaming1/skyblockenhancements/feature/pricing/PriceStore;setFetchFailed()V")
    )
    private void somefrills$refreshAsync_onFetchFailed(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        somefrills$httpFailed = true;
        LOGGER.warn("Price data fetch failed - HTTP requests disabled for this session, cached data will be used");
    }

    /**
     * Helper: Returns cached JSON for a given URL.
     */
    @Unique
    private static String somefrills$getCachedForUrl(String url) {
        if (url.contains("lowestbin")) {
            return PriceDataCacheManager.loadLowestBin();
        } else if (url.contains("bazaar")) {
            return PriceDataCacheManager.loadBazaar();
        }
        return null;
    }

    /**
     * Helper: Saves JSON response to cache for a given URL.
     */
    @Unique
    private static void somefrills$saveCacheForUrl(String url, String jsonResponse) {
        if (url.contains("lowestbin")) {
            PriceDataCacheManager.saveLowestBin(jsonResponse);
        } else if (url.contains("bazaar")) {
            PriceDataCacheManager.saveBazaar(jsonResponse);
        }
    }
}
