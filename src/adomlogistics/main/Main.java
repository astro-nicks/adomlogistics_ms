package adomlogistics.main;

import adomlogistics.model.Delivery;
import adomlogistics.model.Driver;
import adomlogistics.model.Vehicle;

import java.util.Scanner;

import adomlogistics.service.DeliveryService;
import adomlogistics.service.DispatcherService;
import adomlogistics.service.MaintenanceService;
import adomlogistics.service.VehicleService;
import adomlogistics.storage.Database;
import adomlogistics.utils.QuickSortByMileage;

import java.sql.SQLException;

public class Main {
    private static DispatcherService dispatcher;
    private static VehicleService vehicleService;
    private static DeliveryService deliveryService;
    private static MaintenanceService maintenanceService;
    private static Database database;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            database = new Database();
            maintenanceService = new MaintenanceService();
            vehicleService = new VehicleService(maintenanceService, database);
            dispatcher = new DispatcherService();
            deliveryService = new DeliveryService(100, dispatcher, vehicleService);
            loadSampleData();
            runMainMenu();
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        } finally {
            if (database != null) {
                try {
                    database.close();
                } catch (SQLException e) {
                    System.err.println("Error closing database: " + e.getMessage());
                }
            }
        }
    }

    private static void loadSampleData() {
        dispatcher.addDriver(new Driver(1, "John Doe", 5, 10.5f));
        dispatcher.addDriver(new Driver(2, "Jane Smith", 3, 5.2f));

        Vehicle vehicle1 = new Vehicle("VH1001", "Ford Transit", "Truck",
                12.5f, 45000, null, "Oil change needed", "2023-01-15");
        Vehicle vehicle2 = new Vehicle("VH1002", "Mercedes Sprinter", "Van",
                10.2f, 32000, null, "Good condition", "2023-03-20");

        try {
            vehicleService.addVehicle(vehicle1);
            vehicleService.addVehicle(vehicle2);
        } catch (Exception e) {
            System.err.println("Error saving vehicles: " + e.getMessage());
        }

        deliveryService.addVehicle(vehicle1);
        deliveryService.addVehicle(vehicle2);
        maintenanceService.scheduleMaintenance(vehicle1);

        deliveryService.addDelivery(new Delivery("PKG001", "Warehouse A", "Customer X", 2));
        deliveryService.addDelivery(new Delivery("PKG002", "Warehouse B", "Customer Y", 3));
    }

    private static void runMainMenu() {
        while (true) {
            System.out.println("\n=== Adom Logistics ===");
            System.out.println("1. Manage Deliveries");
            System.out.println("2. Manage Vehicles");
            System.out.println("3. Manage Drivers");
            System.out.println("4. Maintenance");
            System.out.println("5. View Reports");
            System.out.println("6. Exit");
            System.out.print("Select option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1: deliveryMenu(); break;
                case 2: vehicleMenu(); break;
                case 3: driverMenu(); break;
                case 4: maintenanceMenu(); break;
                case 5: reportsMenu(); break;
                case 6:
                    System.out.println("Exiting system...");
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    private static void deliveryMenu() {
        while (true) {
            System.out.println("\n=== Delivery Management ===");
            System.out.println("1. Add New Delivery");
            System.out.println("2. Process Next Delivery");
            System.out.println("3. Mark Delivery as Completed");
            System.out.println("4. View Pending Deliveries");
            System.out.println("5. View Active Deliveries");
            System.out.println("6. Back to Main Menu");
            System.out.print("Select option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter Package ID: ");
                    String pkgId = scanner.nextLine();
                    System.out.print("Enter Origin: ");
                    String origin = scanner.nextLine();
                    System.out.print("Enter Destination: ");
                    String dest = scanner.nextLine();
                    System.out.print("Estimated Hours: ");
                    int hours = scanner.nextInt();

                    deliveryService.addDelivery(
                            new Delivery(pkgId, origin, dest, hours)
                    );
                    System.out.println("Delivery added!");
                    break;

                case 2:
                    deliveryService.assignNextDelivery();
                    System.out.println("Delivery assigned to driver/vehicle");
                    break;

                case 3:
                    System.out.print("Enter Package ID to complete: ");
                    String completeId = scanner.nextLine();
                    deliveryService.markAsDelivered(completeId);
                    System.out.println("Delivery marked as completed");
                    break;

                case 4:
                    System.out.println("\nPending Deliveries:");
                    for (Delivery d : deliveryService.getPendingDeliveries()) {
                        System.out.println(d);
                    }
                    break;

                case 5:
                    System.out.println("\nActive Deliveries:");
                    for (Delivery d : deliveryService.getActiveDeliveries()) {
                        System.out.println(d);
                    }
                    break;

                case 6: return;

                default: System.out.println("Invalid choice!");
            }
        }
    }

    private static void vehicleMenu() {
        // Placeholder for vehicle menu implementation
        System.out.println("Vehicle menu not yet implemented.");
    }

    private static void driverMenu() {
        // Placeholder for driver menu implementation
        System.out.println("Driver menu not yet implemented.");
    }

    private static void maintenanceMenu() {
        // Placeholder for maintenance menu implementation
        System.out.println("Maintenance menu not yet implemented.");
    }

    private static void reportsMenu() {
        while (true) {
            System.out.println("\n=== Reports ===");
            System.out.println("1. Delivery Status Report");
            System.out.println("2. Vehicle Utilization Report");
            System.out.println("3. Driver Performance Report");
            System.out.println("4. Maintenance History");
            System.out.println("5. Back to Main Menu");
            System.out.print("Select option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("\n=== Delivery Status ===");
                    System.out.println("Pending Deliveries: " +
                            deliveryService.getPendingDeliveries().length);
                    System.out.println("Active Deliveries: " +
                            deliveryService.getActiveDeliveries().length);

                    System.out.println("\nRecent Deliveries:");
                    Delivery[] active = deliveryService.getActiveDeliveries();
                    for (int i = 0; i < Math.min(5, active.length); i++) {
                        System.out.println(active[i]);
                    }
                    break;

                case 2:
                    System.out.println("\n=== Vehicle Utilization ===");
                    Vehicle[] vehicles = vehicleService.getAllVehicles();
                    int totalMileage = 0;
                    for (Vehicle v : vehicles) {
                        totalMileage += v.mileage;
                        System.out.println(v.regNumber + " - " + v.mileage + " km | " +
                                (v.driverId != null ? "In Use" : "Available"));
                    }
                    System.out.println("\nAverage Mileage: " +
                            (vehicles.length > 0 ? totalMileage/vehicles.length : 0) + " km");
                    break;

                case 3:
                    System.out.println("\n=== Driver Performance ===");
                    Driver[] drivers = dispatcher.getAllDrivers();
                    for (Driver d : drivers) {
                        int deliveries = dispatcher.getDriverRoutes(d.id).length;
                        System.out.println(d.name + " (" + d.experienceYears + " yrs) | " +
                                "Deliveries: " + deliveries);
                    }
                    break;

                case 4:
                    System.out.println("\n=== Maintenance History ===");
                    Vehicle[] allVehicles = vehicleService.getAllVehicles();
                    for (Vehicle v : allVehicles) {
                        if (v.maintenanceHistory != null && !v.maintenanceHistory.isEmpty()) {
                            System.out.println(v.regNumber + " - Last Service: " +
                                    v.lastServiceDate + "\n  " +
                                    v.maintenanceHistory);
                        }
                    }
                    break;

                case 5:
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}
