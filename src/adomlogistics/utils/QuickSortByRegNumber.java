package adomlogistics.utils;

import adomlogistics.model.Vehicle;

public class QuickSortByRegNumber {

    public static void quickSort(Vehicle[] vehicles, int start, int end) {
        if (start < end) {
            int pivotIndex = partition(vehicles, start, end);
            quickSort(vehicles, start, pivotIndex - 1);
            quickSort(vehicles, pivotIndex + 1, end);
        }
    }

    private static int partition(Vehicle[] vehicles, int start, int end) {
        Vehicle pivot = vehicles[end];
        int i = start - 1;

        for (int j = start; j < end; j++) {
            // Compare regNumbers alphabetically
            if (vehicles[j].regNumber.compareTo(pivot.regNumber) < 0) {
                i++;
                swap(vehicles, i, j);
            }
        }

        swap(vehicles, i + 1, end);
        return i + 1;
    }

    private static void swap(Vehicle[] vehicles, int i, int j) {
        Vehicle temp = vehicles[i];
        vehicles[i] = vehicles[j];
        vehicles[j] = temp;
    }
}
