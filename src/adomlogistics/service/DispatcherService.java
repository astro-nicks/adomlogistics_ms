package adomlogistics.service;

import adomlogistics.model.Driver;
import adomlogistics.model.Route;
import adomlogistics.utils.BasicHashMap;

public class DispatcherService {
    private Driver[] availableDrivers;
    private int driverCount;
    private BasicHashMap<Integer, Driver> driverMap;
    private BasicHashMap<Integer, Route[]> driverRoutes;

    public DispatcherService() {
        availableDrivers = new Driver[100];
        driverMap = new BasicHashMap<>();
        driverRoutes = new BasicHashMap<>();
        driverCount = 0;
    }

    public void addDriver(Driver driver) {
        // Check if driver already exists and skip if it does,
       if (driverMap.containsKey(driver.id)) {
    System.out.println("Driver with ID " + driver.id + " already exists. Skipping...");
    return;
    }


        // Insert sorted by experience (priority queue)
        int i = driverCount - 1;
        while (i >= 0 && availableDrivers[i].experienceYears < driver.experienceYears) {
            availableDrivers[i + 1] = availableDrivers[i];
            i--;
        }
        availableDrivers[i + 1] = driver;
        driverCount++;
        driverMap.put(driver.id, driver);
        driverRoutes.put(driver.id, new Route[0]);
    }

    public Driver assignDriver() {
        if (driverCount == 0) return null;
        Driver driver = availableDrivers[driverCount - 1];
        driverCount--;
        return driver;
    }

    public Driver getDriver(int driverId) {
        return driverMap.get(driverId);
    }

    public int getDriverCount() {
        return driverCount;
    }

    public Driver[] getAvailableDrivers() {
        Driver[] available = new Driver[driverCount];
        System.arraycopy(availableDrivers, 0, available, 0, driverCount);
        return available;
    }

    public Route[] getDriverRoutes(int driverId) {
        return driverRoutes.get(driverId);
    }

public Driver[] getAllDrivers() {
    Object[] raw = driverMap.values(); // Object[]
    Driver[] drivers = new Driver[raw.length];
    for (int i = 0; i < raw.length; i++) {
        drivers[i] = (Driver) raw[i]; // Safe cast
    }
    return drivers;
}


    public void addRouteToDriver(int driverId, Route route) { 
        // add a route to a specific driver and update the driver's route list
        // if the driver does not exist, do nothing
        // if the driver exists, append the new route to their existing routes
        // if the driver has no routes, create a new array with the new route
        // if the driver has routes, create a new array with the existing routes and the new route
        Route[] currentRoutes = driverRoutes.get(driverId);
        Route[] newRoutes = new Route[currentRoutes.length + 1];
        System.arraycopy(currentRoutes, 0, newRoutes, 0, currentRoutes.length);
        newRoutes[currentRoutes.length] = route;
        driverRoutes.put(driverId, newRoutes);
    }

    public String getDriverPerformance(int driverId) {
        // Calculate and return the performance of a driver based on their routes
        // performance is defined as the number of completed routes, total routes, completion rate, and total time taken
    Route[] routes = driverRoutes.get(driverId);
    if (routes == null || routes.length == 0) {
        return "No routes assigned to this driver.";
    }

    int completed = 0;
    int totalTime = 0;

    for (Route r : routes) {
        if ("Completed".equalsIgnoreCase(r.status)) {
            completed++;
        }
        totalTime += r.estimatedTime;
    }

    return String.format(
        // Formatting the output string to include driver ID, total routes, completed routes, completion rate, and total time
        "Driver ID %d - Total Routes: %d | Completed: %d | Completion Rate: %.1f%% | Total Time: %d mins",
        driverId, routes.length, completed, (100.0 * completed / routes.length), totalTime
    );
}

}