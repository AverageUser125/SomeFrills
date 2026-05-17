package com.somefrills.mixin.skyblock_enhancements;

import com.github.kd_gaming1.skyblockenhancements.feature.pricing.PriceDataFetcher;
import com.somefrills.features.misc.PriceDataCacheManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(PriceDataFetcher.class)
public class PriceDataFetcherMixin {
    @Unique
    private static AtomicBoolean hasFetchedOnce = new AtomicBoolean(false);

    @Inject(
            method = "httpGet",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void httpGetStop(String url, CallbackInfoReturnable<String> cir) {
        var cached = somefrills$getCachedForUrl(url);
        if (cached != null) {
            cir.setReturnValue(cached);
        }
    }

    @Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable = true
    )
    public void tickStop(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (hasFetchedOnce.get()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "refreshAsync",
            at = @At("HEAD"),
            cancellable = true
    )
    private void refreshAsyncStop(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (hasFetchedOnce.get()) {
            ci.cancel();
        }
    }

    @Unique
    private static String somefrills$getCachedForUrl(String url) {
        hasFetchedOnce.set(true);
        if (url.contains("lowestbin")) {
            return PriceDataCacheManager.loadLowestBin();
        } else if (url.contains("bazaar")) {
            return PriceDataCacheManager.loadBazaar();
        }
        return null;
    }

    @Unique
    private static void somefrills$saveCacheForUrl(String url, String jsonResponse) {
        if (url.contains("lowestbin")) {
            PriceDataCacheManager.saveLowestBin(jsonResponse);
        } else if (url.contains("bazaar")) {
            PriceDataCacheManager.saveBazaar(jsonResponse);
        }
    }
}
