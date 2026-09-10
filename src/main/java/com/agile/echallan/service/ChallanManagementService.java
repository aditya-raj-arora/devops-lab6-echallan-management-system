package com.agile.echallan.service;

import com.agile.echallan.enums.PaymentStatus;
import com.agile.echallan.enums.VehicleRiskCategory;
import com.agile.echallan.enums.ViolationType;
import com.agile.echallan.exception.ChallanAlreadyPaidException;
import com.agile.echallan.exception.ChallanNotFoundException;
import com.agile.echallan.exception.DuplicateChallanException;
import com.agile.echallan.exception.DuplicateVehicleException;
import com.agile.echallan.exception.InvalidVehicleException;
import com.agile.echallan.exception.VehicleNotFoundException;
import com.agile.echallan.model.Challan;
import com.agile.echallan.model.Vehicle;
import com.agile.echallan.model.Violation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChallanManagementService {

    private final Map<String, Vehicle> vehicles = new LinkedHashMap<>();
    private final Map<String, Challan> challans = new LinkedHashMap<>();
    private final Map<String, String> violationEventIndex = new LinkedHashMap<>();

    public Vehicle registerVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            throw new InvalidVehicleException("Vehicle cannot be null");
        }
        if (vehicles.containsKey(vehicle.getVehicleNumber())) {
            throw new DuplicateVehicleException("Vehicle with number " + vehicle.getVehicleNumber() + " is already registered");
        }
        vehicles.put(vehicle.getVehicleNumber(), vehicle);
        return vehicle;
    }

    public Vehicle getVehicle(String vehicleNumber) {
        String normalized = Vehicle.normalizeAndValidate(vehicleNumber);
        Vehicle vehicle = vehicles.get(normalized);
        if (vehicle == null) {
            throw new VehicleNotFoundException("No vehicle registered with number " + normalized);
        }
        return vehicle;
    }

    public Challan recordViolation(String vehicleNumber, ViolationType violationType, String location,
                                    LocalDateTime timestamp, double speed, double permittedSpeed) {
        Vehicle vehicle = getVehicle(vehicleNumber);
        Violation violation = new Violation(vehicle.getVehicleNumber(), violationType, location, timestamp,
                speed, permittedSpeed);

        if (violationEventIndex.containsKey(violation.eventKey())) {
            throw new DuplicateChallanException("A challan already exists for this exact violation event");
        }

        int priorCount = vehicle.getViolationCountForType(violationType);
        double fineAmount = FineCalculator.calculateFine(violationType, speed, permittedSpeed, priorCount);

        vehicle.addViolation(violation);
        Challan challan = new Challan(violation, fineAmount);
        challans.put(challan.getChallanId(), challan);
        violationEventIndex.put(violation.eventKey(), challan.getChallanId());
        return challan;
    }

    public Challan getChallan(String challanId) {
        if (challanId == null || challanId.trim().isEmpty()) {
            throw new InvalidVehicleException("Challan ID cannot be null or empty");
        }
        Challan challan = challans.get(challanId.trim());
        if (challan == null) {
            throw new ChallanNotFoundException("No challan found with ID " + challanId);
        }
        return challan;
    }

    public Challan payChallan(String challanId) {
        Challan challan = getChallan(challanId);
        if (challan.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ChallanAlreadyPaidException("Challan " + challanId + " has already been paid");
        }
        challan.markPaid();
        return challan;
    }

    public List<Challan> getUnpaidChallans() {
        return filterByStatus(PaymentStatus.UNPAID);
    }

    public List<Challan> getPaidChallans() {
        return filterByStatus(PaymentStatus.PAID);
    }

    private List<Challan> filterByStatus(PaymentStatus status) {
        List<Challan> result = new ArrayList<>();
        for (Challan challan : challans.values()) {
            if (challan.getPaymentStatus() == status) {
                result.add(challan);
            }
        }
        return result;
    }

    public List<Challan> getChallansForVehicle(String vehicleNumber) {
        Vehicle vehicle = getVehicle(vehicleNumber);
        List<Challan> result = new ArrayList<>();
        for (Challan challan : challans.values()) {
            if (challan.getViolation().getVehicleNumber().equals(vehicle.getVehicleNumber())) {
                result.add(challan);
            }
        }
        return result;
    }

    public double getOutstandingFinesForVehicle(String vehicleNumber) {
        Vehicle vehicle = getVehicle(vehicleNumber);
        double total = 0.0;
        for (Challan challan : challans.values()) {
            if (challan.getViolation().getVehicleNumber().equals(vehicle.getVehicleNumber())
                    && challan.getPaymentStatus() == PaymentStatus.UNPAID) {
                total += challan.getFineAmount();
            }
        }
        return Math.round(total * 100.0) / 100.0;
    }

    public double getTotalOutstandingFines() {
        double total = 0.0;
        for (Challan challan : challans.values()) {
            if (challan.getPaymentStatus() == PaymentStatus.UNPAID) {
                total += challan.getFineAmount();
            }
        }
        return Math.round(total * 100.0) / 100.0;
    }

    public VehicleRiskCategory classifyVehicle(String vehicleNumber) {
        Vehicle vehicle = getVehicle(vehicleNumber);
        int violationCount = vehicle.getViolationHistory().size();
        if (violationCount >= 5) {
            return VehicleRiskCategory.BLACKLISTED;
        }
        if (violationCount >= 3) {
            return VehicleRiskCategory.HIGH_RISK;
        }
        if (violationCount >= 1) {
            return VehicleRiskCategory.MODERATE_RISK;
        }
        return VehicleRiskCategory.CLEAN;
    }

    public int getVehicleCount() {
        return vehicles.size();
    }

    public int getChallanCount() {
        return challans.size();
    }
}
