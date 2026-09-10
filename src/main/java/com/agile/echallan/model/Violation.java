package com.agile.echallan.model;

import com.agile.echallan.enums.ViolationType;
import com.agile.echallan.exception.InvalidViolationException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Violation {
    private final String violationId;
    private final String vehicleNumber;
    private final ViolationType violationType;
    private final String location;
    private final LocalDateTime timestamp;
    private final double speed;
    private final double permittedSpeed;

    public Violation(String vehicleNumber, ViolationType violationType, String location,
                      LocalDateTime timestamp, double speed, double permittedSpeed) {
        if (vehicleNumber == null || vehicleNumber.trim().isEmpty()) {
            throw new InvalidViolationException("Vehicle number cannot be null or empty");
        }
        if (violationType == null) {
            throw new InvalidViolationException("Violation type cannot be null");
        }
        if (location == null || location.trim().isEmpty()) {
            throw new InvalidViolationException("Violation location cannot be null or empty");
        }
        if (timestamp == null) {
            throw new InvalidViolationException("Violation timestamp cannot be null");
        }
        if (timestamp.isAfter(LocalDateTime.now())) {
            throw new InvalidViolationException("Violation timestamp cannot be in the future");
        }
        if (violationType == ViolationType.OVER_SPEEDING) {
            if (speed <= 0) {
                throw new InvalidViolationException("Recorded speed must be greater than zero for over-speeding violations");
            }
            if (permittedSpeed <= 0) {
                throw new InvalidViolationException("Permitted speed must be greater than zero for over-speeding violations");
            }
            if (speed <= permittedSpeed) {
                throw new InvalidViolationException("Recorded speed must exceed permitted speed for an over-speeding violation");
            }
        }
        this.violationId = UUID.randomUUID().toString();
        this.vehicleNumber = vehicleNumber.trim().toUpperCase();
        this.violationType = violationType;
        this.location = location.trim();
        this.timestamp = timestamp;
        this.speed = speed;
        this.permittedSpeed = permittedSpeed;
    }

    public String getViolationId() {
        return violationId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public ViolationType getViolationType() {
        return violationType;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public double getSpeed() {
        return speed;
    }

    public double getPermittedSpeed() {
        return permittedSpeed;
    }

    public String eventKey() {
        return vehicleNumber + "|" + violationType + "|" + location + "|" + timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Violation)) return false;
        Violation violation = (Violation) o;
        return violationId.equals(violation.violationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(violationId);
    }

    @Override
    public String toString() {
        return "Violation{id='" + violationId + "', vehicle='" + vehicleNumber + "', type=" + violationType
                + ", location='" + location + "', timestamp=" + timestamp + "}";
    }
}
