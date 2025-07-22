package adomlogistics.storage;

import adomlogistics.model.Vehicle;

import adomlogistics.model.*;
import java.sql.*;
import java.util.*;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/adomlogistics_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Flight$23";
    private Connection connection;

    public Database() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }

    private void initializeDatabase() throws SQLException {
        // Create tables if they don't exist
        try (Statement stmt = connection.createStatement()) {
            // Vehicles table
            stmt.execute("CREATE TABLE IF NOT EXISTS vehicles (" +
                    "reg_number VARCHAR(20) PRIMARY KEY, " +
                    "name VARCHAR(100), " +
                    "type VARCHAR(50), " +
                    "fuel_usage FLOAT, " +
                    "mileage INT, " +
                    "driver_id INT, " +
                    "maintenance_history TEXT, " +
                    "last_service_date VARCHAR(20))");

            // Drivers table
            stmt.execute("CREATE TABLE IF NOT EXISTS drivers (" +
                    "id INT PRIMARY KEY, " +
                    "name VARCHAR(100), " +
                    "experience_years INT, " +
                    "distance_from_pickup FLOAT, " +
                    "available BOOLEAN)");

            // Deliveries table
            stmt.execute("CREATE TABLE IF NOT EXISTS deliveries (" +
                    "package_id VARCHAR(50) PRIMARY KEY, " +
                    "origin VARCHAR(100), " +
                    "destination VARCHAR(100), " +
                    "assigned_vehicle_id VARCHAR(20), " +
                    "assigned_driver_id INT, " +
                    "estimated_hours INT, " +
                    "status VARCHAR(20), " +
                    "created_at VARCHAR(50))");
        }
    }

    public void saveVehicle(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO vehicles VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "name=VALUES(name), type=VALUES(type), fuel_usage=VALUES(fuel_usage), " +
                "mileage=VALUES(mileage), driver_id=VALUES(driver_id), " +
                "maintenance_history=VALUES(maintenance_history), last_service_date=VALUES(last_service_date)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, vehicle.regNumber);
            stmt.setString(2, vehicle.name);
            stmt.setString(3, vehicle.type);
            stmt.setFloat(4, vehicle.fuelUsage);
            stmt.setInt(5, vehicle.mileage);
            stmt.setObject(6, vehicle.driverId, Types.INTEGER);
            stmt.setString(7, vehicle.maintenanceHistory);
            stmt.setString(8, vehicle.lastServiceDate);
            stmt.executeUpdate();
        }
    }

    public List<Vehicle> loadAllVehicles() throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT * FROM vehicles";

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
        return vehicles;
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}