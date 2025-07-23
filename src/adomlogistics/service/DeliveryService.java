package adomlogistics.service;

import adomlogistics.model.Delivery;
import adomlogistics.model.Driver;
import adomlogistics.model.Route;
import adomlogistics.model.Vehicle;
import adomlogistics.utils.BasicHashMap;
import adomlogistics.utils.DeliveryQueue;

public class DeliveryService {
    private DeliveryQueue pendingDeliveries;
    private BasicHashMap<String, Delivery> activeDeliveries;
    private BasicHashMap<String, Vehicle> vehicles;
    private DispatcherService dispatcher;
    private VehicleService vehicleService;

    public DeliveryService(int capacity, DispatcherService dispatcher,
                           VehicleService vehicleService) {
        this.pendingDeliveries = new DeliveryQueue(capacity);
        this.activeDeliveries = new BasicHashMap<>();
        this.vehicles = new BasicHashMap<>();
        this.dispatcher = dispatcher;
        this.vehicleService = vehicleService;
    }

    public void addDelivery(Delivery delivery) {
        if (delivery == null || delivery.packageId == null) {
            throw new IllegalArgumentException("Invalid delivery");
        }
        if (activeDeliveries.get(delivery.packageId) != null) {
            throw new IllegalArgumentException("Delivery exists");
        }
        pendingDeliveries.enqueue(delivery);
    }

    public void assignNextDelivery() {
        if (pendingDeliveries.isEmpty()) return;

        Delivery delivery = pendingDeliveries.peek();
        Driver driver = dispatcher.assignDriver();
        Vehicle vehicle = findAvailableVehicle();

        if (driver != null && vehicle != null) {
            vehicle.driverId = driver.id;
            vehicleService.updateVehicle(vehicle);

            delivery.assignedDriverId = driver.id;
            delivery.assignedVehicleId = vehicle.regNumber;
            delivery.status = "In Transit";

            pendingDeliveries.dequeue();
            activeDeliveries.put(delivery.packageId, delivery);
            dispatcher.addRouteToDriver(driver.id,
                    new Route(delivery.packageId.hashCode(),
                            delivery.origin,
                            delivery.destination,
                            java.time.LocalDate.now().toString(),
                            delivery.estimatedHours * 60));
        }
    }

    private Vehicle findAvailableVehicle() {
        Vehicle[] allVehicles = vehicleService.getAllVehicles();
        for (Vehicle v : allVehicles) {
            if (v.driverId == null) return v;
        }
        return null;
    }

    public void markAsDelivered(String packageId) {
        Delivery delivery = activeDeliveries.get(packageId);
        if (delivery == null) return;

        delivery.status = "Delivered";
        Vehicle vehicle = vehicles.get(delivery.assignedVehicleId);
        if (vehicle != null) {
            vehicle.driverId = null;
            vehicleService.updateVehicle(vehicle);
        }
        activeDeliveries.remove(packageId);
    }

    public Delivery[] getPendingDeliveries() {
        return pendingDeliveries.getAllDeliveries();
    }

    public Delivery[] getActiveDeliveries() {
        Object[] raw = activeDeliveries.values();
        Delivery[] deliveries = new Delivery[raw.length];
        for (int i = 0; i < raw.length; i++) {
            if (raw[i] instanceof Delivery) {
                deliveries[i] = (Delivery) raw[i];
            } else {
                deliveries[i] = null; 
            }
        }
        return deliveries;
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles.put(vehicle.regNumber, vehicle);
        vehicleService.addVehicle(vehicle);
    }

    public void removeVehicle(String regNumber) {
        vehicles.remove(regNumber);
        vehicleService.removeVehicle(regNumber);
    }
}
