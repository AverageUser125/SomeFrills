package com.somefrills.misc

import java.util.AbstractSet
import java.util.Spliterator
import java.util.concurrent.ConcurrentHashMap

class ConcurrentHashSet<E> : AbstractSet<E>() {

    companion object {
        private val PRESENT = Any()
    }

    private val map = ConcurrentHashMap<E, Any>()

    override fun iterator(): MutableIterator<E> {
        return map.keys.iterator()
    }

    override val size: Int
        get() = map.size

    override fun isEmpty(): Boolean {
        return map.isEmpty()
    }

    override fun contains(element: E): Boolean {
        return map.containsKey(element)
    }

    override fun add(element: E): Boolean {
        return map.put(element, PRESENT) == null
    }

    override fun remove(element: E): Boolean {
        return map.remove(element) === PRESENT
    }

    override fun clear() {
        map.clear()
    }

    override fun spliterator(): Spliterator<E> {
        return map.keys.spliterator()
    }
}