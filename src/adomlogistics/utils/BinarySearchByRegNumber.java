package adomlogistics.utils;

import adomlogistics.model.Vehicle;

public class BinarySearchByRegNumber {

    public static Vehicle binarySearch(Vehicle[] vehicles, String targetRegNumber) {
        int start = 0;
        int end = vehicles.length - 1;

        while (start <= end) {
            int mid = start + (end - start) / 2;
            int comparison = vehicles[mid].regNumber.compareTo(targetRegNumber);

            if (comparison == 0) {
                return vehicles[mid]; // Found
            } else if (comparison < 0) {
                start = mid + 1; // Search right half
            } else {
                end = mid - 1;   // Search left half
            }
        }

        return null; // Not found
    }
}
