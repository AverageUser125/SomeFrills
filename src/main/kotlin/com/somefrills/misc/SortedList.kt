package com.somefrills.misc

import com.somefrills.utils.TextUtils
import java.util.*

class SortedList<T : Comparable<T>>(private var array: Array<T>) : MutableList<T> {

    @Suppress("UNCHECKED_CAST")
    constructor(list: List<T>) : this(
        (arrayOfNulls<Comparable<T>>(list.size) as Array<T>).also { arr ->
            list.forEachIndexed { i, item -> arr[i] = item }
        }
    ) {
        array.sort()
    }

    constructor(list: SortedList<T>) : this(list.array.copyOf())

    @Suppress("UNCHECKED_CAST")
    constructor() : this(arrayOfNulls<Comparable<T>>(0) as Array<T>)

    init {
        array.sort()
    }

    override val size: Int
        get() = array.size

    override fun isEmpty(): Boolean = size == 0

    override fun contains(element: T): Boolean {
        return try {
            array.binarySearch(element) >= 0
        } catch (_: ClassCastException) {
            false
        }
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return elements.all { contains(it) }
    }

    override fun get(index: Int): T = array[index]

    override fun indexOf(element: T): Int {
        return try {
            val idx = array.binarySearch(element)
            if (idx >= 0) idx else -1
        } catch (_: ClassCastException) {
            -1
        }
    }

    override fun lastIndexOf(element: T): Int = indexOf(element)

    override fun iterator(): MutableIterator<T> = array.toMutableList().iterator()

    override fun listIterator(): MutableListIterator<T> = array.toMutableList().listIterator()

    override fun listIterator(index: Int): MutableListIterator<T> = array.toMutableList().listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> {
        return array.slice(fromIndex until toIndex).toMutableList()
    }

    override fun add(element: T): Boolean {
        var index = array.binarySearch(element)
        if (index < 0) index = -index - 1
        array = insertAt(array, element, index)
        return true
    }

    override fun add(index: Int, element: T) {
        add(element)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        var modified = false
        for (element in elements) {
            add(element)
            modified = true
        }
        return modified
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        return addAll(elements)
    }

    override fun clear() {
        @Suppress("UNCHECKED_CAST")
        array = arrayOfNulls<Comparable<T>>(0) as Array<T>
    }

    override fun remove(element: T): Boolean {
        val index = indexOf(element)
        return if (index < 0) false else {
            array = removeAt(array, index)
            true
        }
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        var modified = false
        for (element in elements) {
            modified = modified or remove(element)
        }
        return modified
    }

    override fun removeAt(index: Int): T {
        val old = array[index]
        array = removeAt(array, index)
        return old
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        val temp = array.filter { it in elements }
        @Suppress("UNCHECKED_CAST")
        val tempArray = arrayOfNulls<Comparable<T>>(temp.size).also { arr ->
            temp.forEachIndexed({ i, item -> arr[i] = item as Comparable<T> })
        } as Array<T>
        return if (temp.size != array.size) {
            array = tempArray
            true
        } else {
            false
        }
    }

    override fun set(index: Int, element: T): T {
        val old = array[index]
        removeAt(index)
        add(element)
        return old
    }

    override fun equals(other: Any?): Boolean {
        if (other !is List<*>) return false
        return array.contentEquals(other.toTypedArray())
    }

    override fun hashCode(): Int = array.contentHashCode()

    override fun toString(): String {
        return array.asSequence()
            .map { TextUtils.capitalizeType(it.toString()) }
            .reduceOrNull { a, b -> "$a, $b" }
            ?: ""
    }

    private fun insertAt(arr: Array<T>, value: T, index: Int): Array<T> {
        @Suppress("UNCHECKED_CAST")
        val newArr = arr.copyOf(arr.size + 1) as Array<T>
        System.arraycopy(arr, index, newArr, index + 1, arr.size - index)
        newArr[index] = value
        return newArr
    }

    private fun removeAt(arr: Array<T>, index: Int): Array<T> {
        @Suppress("UNCHECKED_CAST")
        val newArr = arr.copyOf(arr.size - 1) as Array<T>
        System.arraycopy(arr, 0, newArr, 0, index)
        System.arraycopy(arr, index + 1, newArr, index, arr.size - index - 1)
        return newArr
    }
}

