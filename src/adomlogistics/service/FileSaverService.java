package adomlogistics.service;

import adomlogistics.model.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class FileSaverService {

    public static void dumpSystemState(
            List<Driver> drivers,
            List<Vehicle> vehicles,
            List<Delivery> deliveries,
            List<maintenanceRecord> maintenanceRecords
    ) {
        saveDrivers(drivers);
        saveVehicles(vehicles);
        saveDeliveries(deliveries);
        saveMaintenanceRecords(maintenanceRecords);

        System.out.println("All memory components saved to individual files.");
    }

    private static void saveDrivers(List<Driver> drivers) {
        try (PrintWriter out = new PrintWriter(new FileWriter("drivers.txt"))) {
            out.println(">>> DRIVERS:");
            for (Driver d : drivers) {
                out.println(d.toString());
            }
        } catch (IOException e) {
            System.err.println("Error writing drivers.txt: " + e.getMessage());
        }
    }

    private static void saveVehicles(List<Vehicle> vehicles) {
        try (PrintWriter out = new PrintWriter(new FileWriter("vehicles.txt"))) {
            out.println(">>> VEHICLES:");
            for (Vehicle v : vehicles) {
                out.println(v.toString());
            }
        } catch (IOException e) {
            System.err.println("Error writing vehicles.txt: " + e.getMessage());
        }
    }

    private static void saveDeliveries(List<Delivery> deliveries) {
        try (PrintWriter out = new PrintWriter(new FileWriter("deliveries.txt"))) {
            out.println(">>> DELIVERIES:");
            for (Delivery d : deliveries) {
                out.println(d.toString());
            }
        } catch (IOException e) {
            System.err.println("Error writing deliveries.txt: " + e.getMessage());
        }
    }

    private static void saveMaintenanceRecords(List<maintenanceRecord> records) {
        try (PrintWriter out = new PrintWriter(new FileWriter("maintenance.txt"))) {
            out.println(">>> MAINTENANCE RECORDS:");
            for (maintenanceRecord m : records) {
                out.printf("Record %s | Vehicle: %s | %s | %dkm | %s | Urgency: %d\n",
                        m.recordId, m.vehicleId, m.serviceType, m.mileage,
                        m.serviceDate, m.urgencyScore);
            }
        } catch (IOException e) {
            System.err.println("Error writing maintenance.txt: " + e.getMessage());
        }
    }
}
