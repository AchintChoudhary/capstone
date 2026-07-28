package com.capstone.scheduler.algorithms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SortingAlgorithms {

    /**
     * MergeSort algorithm implementation.
     */
    public static <T> void mergeSort(List<T> list, Comparator<? super T> comparator) {
        if (list == null || list.size() < 2) return;
        List<T> temp = new ArrayList<>(list);
        mergeSortHelper(list, temp, 0, list.size() - 1, comparator);
    }

    private static <T> void mergeSortHelper(List<T> list, List<T> temp, int left, int right, Comparator<? super T> comparator) {
        if (left >= right) return;
        int mid = left + (right - left) / 2;
        mergeSortHelper(list, temp, left, mid, comparator);
        mergeSortHelper(list, temp, mid + 1, right, comparator);
        merge(list, temp, left, mid, right, comparator);
    }

    private static <T> void merge(List<T> list, List<T> temp, int left, int mid, int right, Comparator<? super T> comparator) {
        for (int i = left; i <= right; i++) {
            temp.set(i, list.get(i));
        }

        int i = left;
        int j = mid + 1;
        int k = left;

        while (i <= mid && j <= right) {
            if (comparator.compare(temp.get(i), temp.get(j)) <= 0) {
                list.set(k++, temp.get(i++));
            } else {
                list.set(k++, temp.get(j++));
            }
        }

        while (i <= mid) {
            list.set(k++, temp.get(i++));
        }
    }

    /**
     * QuickSort algorithm implementation.
     */
    public static <T> void quickSort(List<T> list, Comparator<? super T> comparator) {
        if (list == null || list.size() < 2) return;
        quickSortHelper(list, 0, list.size() - 1, comparator);
    }

    private static <T> void quickSortHelper(List<T> list, int low, int high, Comparator<? super T> comparator) {
        if (low >= high) return;
        int pivotIndex = partition(list, low, high, comparator);
        quickSortHelper(list, low, pivotIndex - 1, comparator);
        quickSortHelper(list, pivotIndex + 1, high, comparator);
    }

    private static <T> int partition(List<T> list, int low, int high, Comparator<? super T> comparator) {
        T pivot = list.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (comparator.compare(list.get(j), pivot) <= 0) {
                i++;
                swap(list, i, j);
            }
        }
        swap(list, i + 1, high);
        return i + 1;
    }

    private static <T> void swap(List<T> list, int i, int j) {
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
}
