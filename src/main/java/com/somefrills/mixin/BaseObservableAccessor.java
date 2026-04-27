package com.somefrills.mixin;

import io.github.notenoughupdates.moulconfig.observer.BaseObservable;
import io.github.notenoughupdates.moulconfig.observer.Observer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(BaseObservable.class)
public interface BaseObservableAccessor<T> {
    @Accessor("observers")
    Set<Observer<T>> getObservers();
}