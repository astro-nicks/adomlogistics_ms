//Y'all finna be pissed with the comments
package adomlogistics.storage;

import adomlogistics.model.Vehicle;
import adomlogistics.model.Driver;
import adomlogistics.model.*;
import java.sql.*;
import java.util.*;

public class Database {
    // Database credentials - used by all services for persistence
    private static final String url = "jdbc:mysql://localhost:3306/adom_logistics";
    private static final String USER = "root";
    private static final String PASSWORD = "ROOTm$Q723";

    // Shared connection object used by:
    // - DispatcherService (for driver operations)
    // - VehicleService (for fleet management)
    // - DeliveryService (for package tracking)
    // - MaintenanceService (for service records)
    private Connection connection;

    public Database() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, USER, PASSWORD);
            initializeDatabase(); // Creates tables for all services if they don't exist
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }

    private void initializeDatabase() throws SQLException {
        // Creates tables that support all service operations:
        try (Statement stmt = connection.createStatement()) {
            // Vehicles table - used by VehicleService and MaintenanceService
            stmt.execute("CREATE TABLE IF NOT EXISTS vehicles (" +
                    "reg_number VARCHAR(20) PRIMARY KEY, " +  // Key field for VehicleService operations
                    "name VARCHAR(100), " +                   // Display info
                    "type VARCHAR(50), " +                    // Used by DeliveryService for capacity planning
                    "fuel_usage FLOAT, " +                   // Used by MaintenanceService for service calculations
                    "mileage INT, " +                         // Critical for MaintenanceService scheduling
                    "driver_id INT, " +                       // Links to Driver table (DispatcherService)
                    "maintenance_history TEXT, " +            // Used by MaintenanceService
                    "last_service_date VARCHAR(20))");        // Used by MaintenanceService for urgency calculations

            // Drivers table - core table for DispatcherService
            stmt.execute("CREATE TABLE IF NOT EXISTS drivers (" +
                    "id INT PRIMARY KEY, " +                  // Driver ID used across all services
                    "name VARCHAR(100), " +                   // Display info
                    "experience_years INT, " +                // Used by DispatcherService for priority assignment
                    "distance_from_pickup FLOAT, " +          // Used by DeliveryService for driver selection
                    "available BOOLEAN)");                    // Used by DispatcherService for availability tracking

            // Deliveries table - main table for DeliveryService
            stmt.execute("CREATE TABLE IF NOT EXISTS deliveries (" +
                    "package_id VARCHAR(50) PRIMARY KEY, " +   // Key field for DeliveryService
                    "origin VARCHAR(100), " +                  // Route info used by DispatcherService
                    "destination VARCHAR(100), " +            // Route info used by DispatcherService
                    "assigned_vehicle_id VARCHAR(20), " +      // Links to Vehicles table
                    "assigned_driver_id INT, " +               // Links to Drivers table
                    "estimated_hours INT, " +                  // Used by DispatcherService for route planning
                    "status VARCHAR(20), " +                   // Used by DeliveryService state management
                    "created_at VARCHAR(50))");               // Used for reporting
        }
    }

    /**
     * Used by DispatcherService to check driver existence before operations
     * @param id Driver ID to check
     * @return true if driver exists
     */
    public boolean driverExists(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM drivers WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Used by VehicleService to persist vehicle state changes
     * Supports operations from:
     * - DeliveryService (vehicle assignment)
     * - MaintenanceService (service updates)
     */
    public void saveVehicle(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO vehicles VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "name=VALUES(name), type=VALUES(type), fuel_usage=VALUES(fuel_usage), " +
                "mileage=VALUES(mileage), driver_id=VALUES(driver_id), " +
                "maintenance_history=VALUES(maintenance_history), last_service_date=VALUES(last_service_date)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, vehicle.regNumber);  // Primary key
            stmt.setString(2, vehicle.name);       // Display info
            stmt.setString(3, vehicle.type);       // Used by DeliveryService
            stmt.setFloat(4, vehicle.fuelUsage);    // Used by MaintenanceService
            stmt.setInt(5, vehicle.mileage);       // Critical for maintenance
            stmt.setObject(6, vehicle.driverId, Types.INTEGER);  // Links to Driver
            stmt.setString(7, vehicle.maintenanceHistory);  // Maintenance records
            stmt.setString(8, vehicle.lastServiceDate);    // Maintenance scheduling
            stmt.executeUpdate();
        }
    }

    /**
     * Used by DispatcherService to persist driver information
     * Called when:
     * - New drivers are added
     * - Driver status changes (available/unavailable)
     * - Driver details are updated
     */
    public void saveDriver(Driver driver) throws SQLException {
        String sql = "INSERT INTO drivers (id, name, experience_years, distance_from_pickup, available) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE name=VALUES(name), experience_years=VALUES(experience_years), " +
                "distance_from_pickup=VALUES(distance_from_pickup), available=VALUES(available)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, driver.id);  // Primary key
            stmt.setString(2, driver.name);  // Display info
            stmt.setInt(3, driver.experienceYears);  // Used for priority
            stmt.setDouble(4, driver.distanceFromPickup);  // Used for assignment
            stmt.setBoolean(5, driver.available);  // Availability status
            stmt.executeUpdate();
        }
    }

    /**
     * Used by VehicleService during system initialization
     * Loads all vehicles into memory for:
     * - DeliveryService (assignment pool)
     * - MaintenanceService (scheduling)
     */
    public List<Vehicle> loadAllVehicles() throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT * FROM vehicles";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Vehicle vehicle = new Vehicle(
                        rs.getString("reg_number"),  // Key field
                        rs.getString("name"),       // Display
                        rs.getString("type"),       // Capacity info
                        rs.getFloat("fuel_usage"), // Maintenance metric
                        rs.getInt("mileage"),      // Maintenance trigger
                        rs.getObject("driver_id", Integer.class),  // Assignment status
                        rs.getString("maintenance_history"),  // Service records
                        rs.getString("last_service_date")    // Maintenance planning
                );
                vehicles.add(vehicle);
            }
        }
        return vehicles;
    }

    // === Newly Added Methods with Documentation ===

    /**
     * Used by VehicleService before vehicle operations
     * @param regNumber Vehicle registration number
     * @return true if vehicle exists in system
     */
    public boolean vehicleExists(String regNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM vehicles WHERE reg_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, regNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Used by both VehicleService and MaintenanceService to:
     * - Check vehicle details
     * - Verify existence before maintenance scheduling
     * - Lookup vehicle for assignment
     */
    public Vehicle searchVehicle(String regNumber) throws SQLException {
        String sql = "SELECT * FROM vehicles WHERE reg_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, regNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Vehicle(
                            rs.getString("reg_number"),
                            rs.getString("name"),
                            rs.getString("type"),
                            rs.getFloat("fuel_usage"),
                            rs.getInt("mileage"),
                            rs.getObject("driver_id", Integer.class),
                            rs.getString("maintenance_history"),
                            rs.getString("last_service_date")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Used by VehicleService when removing vehicles from fleet
     * Also called by DeliveryService when cleaning up assignments
     */
    public void removeVehicle(String regNumber) throws SQLException {
        String sql = "DELETE FROM vehicles WHERE reg_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, regNumber);
            stmt.executeUpdate();
        }
    }

    /**
     * Used by VehicleService for maintenance prioritization
     * Called by MaintenanceService for scheduling
     * Sorts vehicles by mileage (highest first)
     */
    public Vehicle[] getVehiclesByMileage() throws SQLException {
        String sql = "SELECT * FROM vehicles ORDER BY mileage DESC";
        List<Vehicle> vehicles = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Vehicle vehicle = new Vehicle(
                        rs.getString("reg_number"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getFloat("fuel_usage"),
                        rs.getInt("mileage"),
                        rs.getObject("driver_id", Integer.class),
                        rs.getString("maintenance_history"),
                        rs.getString("last_service_date")
                );
                vehicles.add(vehicle);
            }
        }
        return vehicles.toArray(new Vehicle[0]);
    }

    /**
     * Used by DeliveryService to persist delivery state changes:
     * - New deliveries created
     * - Assignment updates
     * - Status changes (pending -> active -> completed)
     */
    public void saveDelivery(Delivery delivery) throws SQLException {
        String sql = "INSERT INTO deliveries VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "origin=VALUES(origin), destination=VALUES(destination), " +
                "assigned_vehicle_id=VALUES(assigned_vehicle_id), " +
                "assigned_driver_id=VALUES(assigned_driver_id), " +
                "estimated_hours=VALUES(estimated_hours), " +
                "status=VALUES(status), created_at=VALUES(created_at)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, delivery.packageId);
            stmt.setString(2, delivery.origin);
            stmt.setString(3, delivery.destination);
            stmt.setString(4, delivery.assignedVehicleId);
            stmt.setObject(5, delivery.assignedDriverId, Types.INTEGER);
            stmt.setInt(6, delivery.estimatedHours);
            stmt.setString(7, delivery.status);
            stmt.setString(8, java.time.LocalDate.now().toString());
            stmt.executeUpdate();
        }
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();  // Cleanup called by Main during shutdown
        }
    }
}
