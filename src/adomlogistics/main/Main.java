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
            // Initialize database connection
            database = new Database();

            // Initialize services
            maintenanceService = new MaintenanceService();
            vehicleService = new VehicleService(maintenanceService, database);
            dispatcher = new DispatcherService();
            deliveryService = new DeliveryService(100, dispatcher, vehicleService);

            // Load data
            loadSampleData();

            // Run application
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
        // Sample Drivers (no database operations here)
        dispatcher.addDriver(new Driver(1, "John Doe", 5, 10.5));
        dispatcher.addDriver(new Driver(2, "Jane Smith", 3, 5.2));

        // Sample Vehicles
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

        // Sample Deliveries (no database operations here)
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
            scanner.nextLine(); // Consume newline

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
// Modified this option
    private static void vehicleMenu() {
        while (true) {
            System.out.println("\n=== Vehicle Management ===");
            System.out.println("1. Add New Vehicle");
            System.out.println("2. Search Vehicle");
            System.out.println("3. Remove Vehicle");
            System.out.println("4. List All Vehicles (by Mileage)");
            System.out.println("5. Sort All Vehicles (by Mileage)");// Added this Option
            System.out.println("6. Back to Main Menu");
            System.out.print("Select option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Registration Number: ");
                    String regNum = scanner.nextLine();
                    System.out.print("Vehicle Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Type (Truck/Van): ");
                    String type = scanner.nextLine();
                    System.out.print("Fuel Usage (L/100km): ");
                    float fuelUsage = scanner.nextFloat();
                    System.out.print("Current Mileage: ");
                    int mileage = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Maintenance History: ");
                    String history = scanner.nextLine();
                    System.out.print("Last Service Date (YYYY-MM-DD): ");
                    String lastService = scanner.nextLine();

                    Vehicle newVehicle = new Vehicle(regNum, name, type, fuelUsage,
                            mileage, null, history, lastService);
                    vehicleService.addVehicle(newVehicle);
                    deliveryService.addVehicle(newVehicle);

                    if (history.contains("critical") || mileage > 50000) {
                        maintenanceService.scheduleMaintenance(newVehicle);
                    }
                    System.out.println("Vehicle added!");
                    break;

                case 2:
                    System.out.print("Enter Registration Number: ");
                    String searchReg = scanner.nextLine();
                    Vehicle found = vehicleService.searchVehicle(searchReg);
                    if (found != null) {
                        System.out.println("\nVehicle Found:");
                        System.out.println(found);
                    } else {
                        System.out.println("Vehicle not found!");
                    }
                    break;

                case 3:
                    System.out.print("Enter Registration Number to remove: ");
                    String removeReg = scanner.nextLine();
                    vehicleService.removeVehicle(removeReg);
                    System.out.println("Vehicle removed (if existed)");
                    break;

                case 4:
                    System.out.println("\nAll Vehicles (Sorted by Mileage):");
                    Vehicle[] vehicles = vehicleService.getVehiclesByMileage();
                    for (Vehicle v : vehicles) {
                        System.out.println(v);
                    }
                    break;

                case 5:
                    vehicles = vehicleService.getVehiclesByMileage();
                    QuickSortByMileage.quickSort(vehicles, 0, vehicles.length - 1);

                    System.out.println("\nVehicles Sorted by Mileage:");
                    for (Vehicle v : vehicles) {
                        System.out.println(v.regNumber + " - Mileage: " + v.mileage + " km");
                    }
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    private static void driverMenu() {
        while (true) {
            System.out.println("\n=== Driver Management ===");
            System.out.println("1. Add New Driver");
            System.out.println("2. View Available Drivers");
            System.out.println("3. View Driver Details");
            System.out.println("4. Back to Main Menu");
            System.out.print("Select option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Driver Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Years of Experience: ");
                    int exp = scanner.nextInt();
                    System.out.print("Distance from Pickup (km): ");
                    float distance = scanner.nextFloat();
                    scanner.nextLine();

                    int newId = dispatcher.getDriverCount() + 1;
                    dispatcher.addDriver(new Driver(newId, name, exp, distance));
                    System.out.println("Driver added with ID: " + newId);
                    break;

                case 2:
                    System.out.println("\nAvailable Drivers:");
                    Driver[] available = dispatcher.getAvailableDrivers();
                    for (Driver d : available) {
                        System.out.println(d);
                    }
                    break;

                case 3:
                    System.out.print("Enter Driver ID: ");
                    int id = scanner.nextInt();
                    scanner.nextLine();
                    Driver driver = dispatcher.getDriver(id);
                    if (driver != null) {
                        System.out.println("\nDriver Details:");
                        System.out.println(driver);
                        System.out.println("Assigned Routes: " +
                                dispatcher.getDriverRoutes(id).length);
                    } else {
                        System.out.println("Driver not found!");
                    }
                    break;

                case 4:
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    private static void maintenanceMenu() {
        while (true) {
            System.out.println("\n=== Maintenance Management ===");
            System.out.println("1. View Maintenance Schedule");
            System.out.println("2. Process Next Maintenance");
            System.out.println("3. Add Maintenance Record");
            System.out.println("4. Back to Main Menu");
            System.out.print("Select option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("\nMaintenance Queue (by Urgency):");
                    Vehicle[] maintenanceQueue = maintenanceService.getMaintenanceQueue();
                    for (Vehicle v : maintenanceQueue) {
                        int urgency = maintenanceService.calculateUrgency(v);
                        System.out.println(v.regNumber + " - " + v.name +
                                " | Urgency: " + urgency +
                                " | Last Service: " + v.lastServiceDate);
                    }
                    break;

                case 2:
                    Vehicle nextVehicle = maintenanceService.getNextMaintenance();
                    if (nextVehicle != null) {
                        System.out.println("\nProcessing maintenance for:");
                        System.out.println(nextVehicle);
                        System.out.print("Enter service performed: ");
                        String service = scanner.nextLine();
                        System.out.print("Enter parts replaced: ");
                        String parts = scanner.nextLine();

                        nextVehicle.maintenanceHistory = service;
                        nextVehicle.lastServiceDate = java.time.LocalDate.now().toString();
                        vehicleService.updateVehicle(nextVehicle);

                        System.out.println("Maintenance completed!");
                    } else {
                        System.out.println("No vehicles needing maintenance!");
                    }
                    break;

                case 3:
                    System.out.print("Enter Vehicle Registration: ");
                    String regNum = scanner.nextLine();
                    Vehicle vehicle = vehicleService.searchVehicle(regNum);
                    if (vehicle != null) {
                        maintenanceService.scheduleMaintenance(vehicle);
                        System.out.println("Maintenance scheduled for " + regNum);
                    } else {
                        System.out.println("Vehicle not found!");
                    }
                    break;

                case 4:
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
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
                    Vehicle[] vehicles = vehicleService.getVehiclesByMileage();
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
                    Vehicle[] allVehicles = vehicleService.getVehiclesByMileage();
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