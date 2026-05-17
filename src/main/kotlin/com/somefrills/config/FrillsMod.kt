package com.somefrills.config

object FrillsMod {
    fun bind(instance: FrillsConfig) {
        config = instance
    }

    var config: FrillsConfig = FrillsConfig()
        get() = field
        private set(value) {
            field = value
        }
}