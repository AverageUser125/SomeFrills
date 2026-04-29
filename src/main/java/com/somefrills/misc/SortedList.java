package com.somefrills.misc;

import org.jspecify.annotations.NonNull;

import java.util.*;

public class SortedList<T> implements List<T> {
    private T[] array;

    public SortedList(T[] array) {
        this.array = Arrays.copyOf(array, array.length);
        Arrays.sort(this.array);
    }

    @SuppressWarnings("unchecked")
    public SortedList(List<T> list) {
        this.array = (T[]) list.toArray();
        Arrays.sort(this.array);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SortedList() {
        this.array = (T[]) new Comparable[0];
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (o == null) return false;
        try {
            return Arrays.binarySearch(array, o) >= 0;
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public @NonNull Iterator<T> iterator() {
        return Arrays.asList(array).iterator();
    }

    @Override
    public @NonNull Object[] toArray() {
        return Arrays.copyOf(array, array.length);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull <T1> T1[] toArray(@NonNull T1[] a) {
        if (a.length < array.length) {
            return (T1[]) Arrays.copyOf(array, array.length, a.getClass());
        }
        System.arraycopy(array, 0, a, 0, array.length);
        if (a.length > array.length) a[array.length] = null;
        return a;
    }

    @Override
    public boolean add(T t) {
        int index = Arrays.binarySearch(array, t);
        if (index < 0) index = -index - 1;

        array = insertAt(array, t, index);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        int index = indexOf(o);
        if (index < 0) return false;
        array = removeAt(array, index);
        return true;
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) return false;
        }
        return true;
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends T> c) {
        boolean modified = false;
        for (T t : c) {
            add(t);
            modified = true;
        }
        return modified;
    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends T> c) {
        // index is ignored because we maintain sorted order
        return addAll(c);
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        boolean modified = false;
        for (Object o : c) {
            modified |= remove(o);
        }
        return modified;
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        List<T> temp = new ArrayList<>();
        for (T t : array) {
            if (c.contains(t)) temp.add(t);
        }
        if (temp.size() != array.length) {
            array = temp.toArray(array.clone());
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        array = Arrays.copyOf(array, 0);
    }

    @Override
    public T get(int index) {
        return array[index];
    }

    @Override
    public T set(int index, T element) {
        T old = array[index];
        remove(index);
        add(element);
        return old;
    }

    @Override
    public void add(int index, T element) {
        // ignore index to preserve sorting
        add(element);
    }

    @Override
    public T remove(int index) {
        T old = array[index];
        array = removeAt(array, index);
        return old;
    }

    @Override
    public int indexOf(Object o) {
        if (o == null) return -1;
        try {
            int idx = Arrays.binarySearch(array, o);
            return idx >= 0 ? idx : -1;
        } catch (ClassCastException e) {
            return -1;
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        return indexOf(o); // no duplicates handling improvement
    }

    @Override
    public @NonNull ListIterator<T> listIterator() {
        return Arrays.asList(array).listIterator();
    }

    @Override
    public @NonNull ListIterator<T> listIterator(int index) {
        return Arrays.asList(array).listIterator(index);
    }

    @Override
    public @NonNull List<T> subList(int fromIndex, int toIndex) {
        return Arrays.asList(Arrays.copyOfRange(array, fromIndex, toIndex));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof List<?> other)) return false;
        return Arrays.equals(this.array, other.toArray());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(array);
    }

    @Override
    public String toString() {
        return stream().map(s -> Utils.capitalizeType(s.toString())).reduce((a, b) -> a + ", " + b).orElse("");
    }

    private T[] insertAt(T[] arr, T value, int index) {
        arr = Arrays.copyOf(arr, arr.length + 1);
        System.arraycopy(arr, index, arr, index + 1, arr.length - index - 1);
        arr[index] = value;
        return arr;
    }

    private T[] removeAt(T[] arr, int index) {
        T[] newArr = Arrays.copyOf(arr, arr.length - 1);
        System.arraycopy(arr, 0, newArr, 0, index);
        System.arraycopy(arr, index + 1, newArr, index, arr.length - index - 1);
        return newArr;
    }
}
