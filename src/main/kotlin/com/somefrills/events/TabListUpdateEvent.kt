package com.somefrills.events

class TabListUpdateEvent(@JvmField var lines: MutableList<String>) : FrillsEvent() {
    fun getLines(): List<String> {
        return lines
    }

    fun setLines(lines: MutableList<String>) {
        this.lines = lines
    }
}
