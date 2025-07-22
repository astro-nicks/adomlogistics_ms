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
        if (driverMap.containsKey(driver.id)) {
            throw new IllegalArgumentException("Driver ID exists");
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
        Route[] currentRoutes = driverRoutes.get(driverId);
        Route[] newRoutes = new Route[currentRoutes.length + 1];
        System.arraycopy(currentRoutes, 0, newRoutes, 0, currentRoutes.length);
        newRoutes[currentRoutes.length] = route;
        driverRoutes.put(driverId, newRoutes);
    }
}