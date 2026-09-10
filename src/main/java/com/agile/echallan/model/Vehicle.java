package com.agile.echallan.model;

import com.agile.echallan.enums.VehicleType;
import com.agile.echallan.exception.InvalidVehicleException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class Vehicle {

    private static final Pattern VEHICLE_NUMBER_PATTERN =
            Pattern.compile("^[A-Z]{2}[0-9]{1,2}[A-Z]{1,3}[0-9]{4}$");

    private final String vehicleNumber;
    private final Owner owner;
    private final VehicleType vehicleType;
    private final List<Violation> violationHistory = new ArrayList<>();

    public Vehicle(String vehicleNumber, Owner owner, VehicleType vehicleType) {
        this.vehicleNumber = normalizeAndValidate(vehicleNumber);
        if (owner == null) {
            throw new InvalidVehicleException("Owner details cannot be null");
        }
        if (vehicleType == null) {
            throw new InvalidVehicleException("Vehicle type cannot be null");
        }
        this.owner = owner;
        this.vehicleType = vehicleType;
    }

    public static String normalizeAndValidate(String vehicleNumber) {
        if (vehicleNumber == null || vehicleNumber.trim().isEmpty()) {
            throw new InvalidVehicleException("Vehicle number cannot be null or empty");
        }
        String normalized = vehicleNumber.trim().toUpperCase().replace(" ", "").replace("-", "");
        if (!VEHICLE_NUMBER_PATTERN.matcher(normalized).matches()) {
            throw new InvalidVehicleException("Vehicle number '" + vehicleNumber + "' is not a valid registration number");
        }
        return normalized;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public Owner getOwner() {
        return owner;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public List<Violation> getViolationHistory() {
        return new ArrayList<>(violationHistory);
    }

    public void addViolation(Violation violation) {
        if (violation == null) {
            throw new InvalidVehicleException("Violation cannot be null");
        }
        violationHistory.add(violation);
    }

    public int getViolationCountForType(com.agile.echallan.enums.ViolationType type) {
        int count = 0;
        for (Violation violation : violationHistory) {
            if (violation.getViolationType() == type) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle)) return false;
        Vehicle vehicle = (Vehicle) o;
        return vehicleNumber.equals(vehicle.vehicleNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleNumber);
    }

    @Override
    public String toString() {
        return "Vehicle{number='" + vehicleNumber + "', type=" + vehicleType + ", owner=" + owner.getName() + "}";
    }
}
