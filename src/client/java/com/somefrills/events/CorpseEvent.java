package com.somefrills.events;

import com.somefrills.features.mining.CorpseApi.Corpse;

public class CorpseEvent {
    public final Corpse corpse;

    public CorpseEvent(Corpse corpse) {
        this.corpse = corpse;
    }
}
