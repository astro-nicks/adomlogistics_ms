package adomlogistics.utils;
import adomlogistics.model.Vehicle;

public class QuickSortByMileage {

    // Main quicksort method
    public static void quickSort(Vehicle[] vehicles, int start, int end) {
        if (start < end) {
            // Partition the array and get the pivot index
            int pivotIndex = partition(vehicles, start, end);

            // Recursively sort elements before and after partition
            quickSort(vehicles, start, pivotIndex - 1);
            quickSort(vehicles, pivotIndex + 1, end);
        }
    }

    // Partition method using the last element as pivot
    private static int partition(Vehicle[] vehicles, int start, int end) {
        Vehicle pivot = vehicles[end]; // Last element is the pivot
        int i = start - 1;         // Pointer for the smaller element

        // Loop through from start to end - 1
        for (int j = start; j < end; j++) {
            if (vehicles[j].getMileage() < pivot.getMileage()) {
                i++; // Increment smaller element index
                swap(vehicles, i, j); // Swap vehicles[i] and vehicles[j]
            }
        }

        // Place the pivot in the correct position
        swap(vehicles, i + 1, end);

        return i + 1; // Return the pivot index
    }

    // Swap two vehicles in the array
    private static void swap(Vehicle[] vehicles, int i, int j) {
        Vehicle temp = vehicles[i];
        vehicles[i] = vehicles[j];
        vehicles[j] = temp;
    }
}


