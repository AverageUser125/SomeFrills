package com.somefrills.misc;

import java.util.Arrays;
import java.util.Objects;

@SuppressWarnings("unchecked")
public final class ImmutableClassToInstanceMap<B> {

    @SuppressWarnings("rawtypes")
    private static final ImmutableClassToInstanceMap<?> EMPTY =
            new ImmutableClassToInstanceMap<>(new Class[0], new Object[0]);
    private final Class<? extends B>[] keys;
    private final Object[] values;
    private final int[] table; // hash table (indexes into keys[])
    private final int mask;

    private ImmutableClassToInstanceMap(Class<? extends B>[] keys, Object[] values) {
        this.keys = keys;
        this.values = values;

        int size = keys.length;
        int tableSize = closedTableSize(size);
        this.mask = tableSize - 1;
        this.table = new int[tableSize];
        Arrays.fill(table, -1);

        for (int i = 0; i < size; i++) {
            insert(keys[i], i);
        }
    }

    private static int smear(int hashCode) {
        hashCode ^= (hashCode >>> 20) ^ (hashCode >>> 12);
        return hashCode ^ (hashCode >>> 7) ^ (hashCode >>> 4);
    }

    private static int closedTableSize(int expectedSize) {
        int tableSize = Integer.highestOneBit(expectedSize * 2);
        if (tableSize < expectedSize * 2) {
            tableSize <<= 1;
        }
        return Math.max(4, tableSize);
    }

    @SuppressWarnings("unchecked")
    public static <B> ImmutableClassToInstanceMap<B> of() {
        return (ImmutableClassToInstanceMap<B>) EMPTY;
    }

    public static <B> Builder<B> builder() {
        return new Builder<>();
    }

    private void insert(Class<?> key, int index) {
        int h = smear(key.hashCode());
        int i = h & mask;

        while (true) {
            if (table[i] == -1) {
                table[i] = index;
                return;
            }
            if (keys[table[i]] == key) {
                throw new IllegalArgumentException("Duplicate key: " + key);
            }
            i = (i + 1) & mask;
        }
    }

    public <T extends B> T getInstance(Class<T> type) {
        Objects.requireNonNull(type);
        if (keys.length == 0) return null;

        int h = smear(type.hashCode());
        int i = h & mask;

        while (true) {
            int index = table[i];
            if (index == -1) return null;
            if (keys[index] == type) {
                return type.cast(values[index]);
            }
            i = (i + 1) & mask;
        }
    }

    public int size() {
        return keys.length;
    }

    public boolean isEmpty() {
        return keys.length == 0;
    }

    // ---------------- Builder ----------------

    public static final class Builder<B> {
        @SuppressWarnings({"rawtypes", "RedundantSuppression"})
        private Class<? extends B>[] keys = new Class[8];
        private Object[] values = new Object[8];
        private int size = 0;

        private void ensureCapacity(int min) {
            if (min > keys.length) {
                int newCap = keys.length * 2;
                keys = Arrays.copyOf(keys, newCap);
                values = Arrays.copyOf(values, newCap);
            }
        }

        public <T extends B> Builder<B> put(Class<T> type, T value) {
            Objects.requireNonNull(type);
            Objects.requireNonNull(value);

            ensureCapacity(size + 1);
            keys[size] = type;
            values[size] = type.cast(value);
            size++;
            return this;
        }

        public ImmutableClassToInstanceMap<B> build() {
            if (size == 0) return ImmutableClassToInstanceMap.of();

            return new ImmutableClassToInstanceMap<>(
                    Arrays.copyOf(keys, size),
                    Arrays.copyOf(values, size)
            );
        }
    }
}