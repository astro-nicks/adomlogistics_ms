package adomlogistics.service;

import adomlogistics.model.Vehicle;
import adomlogistics.utils.BasicBinarySearchTree;
import adomlogistics.utils.BasicHashMap;
import adomlogistics.storage.Database;

public class VehicleService {
    private BasicHashMap<String, Vehicle> vehicleMap;
    private BasicBinarySearchTree mileageTree;
    private MaintenanceService maintenanceService;
    private Database database;

    public VehicleService(MaintenanceService maintenanceService) {
        this.vehicleMap = new BasicHashMap<>();
        this.mileageTree = new BasicBinarySearchTree();
        this.maintenanceService = maintenanceService;
    }
    // Updated constructor to include Database parameter
    public VehicleService(MaintenanceService maintenanceService, Database database) {
        this.vehicleMap = new BasicHashMap<>();
        this.mileageTree = new BasicBinarySearchTree();
        this.maintenanceService = maintenanceService;
        this.database = database;
    }
    public void addVehicle(Vehicle vehicle) {
        vehicleMap.put(vehicle.regNumber, vehicle);
        mileageTree.insert(vehicle);

        if (vehicle.mileage > 50000 ||
                (vehicle.maintenanceHistory != null &&
                        vehicle.maintenanceHistory.contains("critical"))) {
            maintenanceService.scheduleMaintenance(vehicle);
        }
    }

    public Vehicle searchVehicle(String regNumber) {
        return vehicleMap.get(regNumber);
    }

    public void removeVehicle(String regNumber) {
        Vehicle vehicle = vehicleMap.get(regNumber);
        if (vehicle != null) {
            vehicleMap.remove(regNumber);
            // Note: Actual BST removal would require implementation
        }
    }

    public Vehicle[] getVehiclesByMileage() {
        return mileageTree.inOrderTraversal();
    }

    public void updateVehicle(Vehicle vehicle) {
        vehicleMap.put(vehicle.regNumber, vehicle);
        // Note: BST update would require removal and reinsertion
    }

   public Vehicle[] getAllVehicles() {
    Object[] raw = vehicleMap.values();  // returns Object[]
    Vehicle[] vehicles = new Vehicle[raw.length];

    for (int i = 0; i < raw.length; i++) {
        vehicles[i] = (Vehicle) raw[i];  // safe cast element-by-element
    }

    return vehicles;
}

}