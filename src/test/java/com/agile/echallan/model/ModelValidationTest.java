package com.agile.echallan.model;

import com.agile.echallan.enums.VehicleType;
import com.agile.echallan.enums.ViolationType;
import com.agile.echallan.exception.InvalidVehicleException;
import com.agile.echallan.exception.InvalidViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModelValidationTest {

    private Owner validOwner() {
        return new Owner("Rahul Sharma", "12 MG Road, Bangalore", "9876543210", "DL1420110012345");
    }

    @ParameterizedTest
    @ValueSource(strings = {"KA01AB1234", "ka01ab1234", "MH12CD5678", "DL1AB1234"})
    void acceptsValidVehicleNumberFormats(String number) {
        Vehicle vehicle = new Vehicle(number, validOwner(), VehicleType.CAR);
        assertEquals(number.trim().toUpperCase(), vehicle.getVehicleNumber());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "12345", "KA-AB-1234", "TOOLONGVEHICLENUMBER1234"})
    void rejectsInvalidVehicleNumberFormats(String number) {
        assertThrows(InvalidVehicleException.class, () -> new Vehicle(number, validOwner(), VehicleType.CAR));
    }

    @Test
    void rejectsNullOwnerOrVehicleType() {
        assertThrows(InvalidVehicleException.class, () -> new Vehicle("KA01AB1234", null, VehicleType.CAR));
        assertThrows(InvalidVehicleException.class, () -> new Vehicle("KA01AB1234", validOwner(), null));
    }

    @ParameterizedTest
    @CsvSource({
            ",Address,9876543210,DL123",
            "Name,,9876543210,DL123",
            "Name,Address,12345,DL123",
            "Name,Address,9876543210,"
    })
    void rejectsInvalidOwnerFields(String name, String address, String phone, String license) {
        assertThrows(InvalidVehicleException.class, () -> new Owner(name, address, phone, license));
    }

    @Test
    void rejectsViolationWithMissingFields() {
        assertThrows(InvalidViolationException.class, () ->
                new Violation(null, ViolationType.ILLEGAL_PARKING, "MG Road", LocalDateTime.now(), 0, 0));
        assertThrows(InvalidViolationException.class, () ->
                new Violation("KA01AB1234", null, "MG Road", LocalDateTime.now(), 0, 0));
        assertThrows(InvalidViolationException.class, () ->
                new Violation("KA01AB1234", ViolationType.ILLEGAL_PARKING, "", LocalDateTime.now(), 0, 0));
        assertThrows(InvalidViolationException.class, () ->
                new Violation("KA01AB1234", ViolationType.ILLEGAL_PARKING, "MG Road", null, 0, 0));
    }

    @Test
    void rejectsFutureViolationTimestamp() {
        assertThrows(InvalidViolationException.class, () ->
                new Violation("KA01AB1234", ViolationType.ILLEGAL_PARKING, "MG Road",
                        LocalDateTime.now().plusDays(1), 0, 0));
    }

    @Test
    void rejectsOverSpeedingViolationWithoutValidSpeeds() {
        assertThrows(InvalidViolationException.class, () ->
                new Violation("KA01AB1234", ViolationType.OVER_SPEEDING, "MG Road", LocalDateTime.now(), 0, 60));
        assertThrows(InvalidViolationException.class, () ->
                new Violation("KA01AB1234", ViolationType.OVER_SPEEDING, "MG Road", LocalDateTime.now(), 80, 0));
        assertThrows(InvalidViolationException.class, () ->
                new Violation("KA01AB1234", ViolationType.OVER_SPEEDING, "MG Road", LocalDateTime.now(), 50, 60));
    }

    @Test
    void acceptsValidOverSpeedingViolation() {
        Violation violation = new Violation("KA01AB1234", ViolationType.OVER_SPEEDING, "MG Road",
                LocalDateTime.now(), 90, 60);
        assertEquals(90, violation.getSpeed());
        assertEquals(60, violation.getPermittedSpeed());
    }

    @Test
    void rejectsNullChallanConstructionArguments() {
        assertThrows(com.agile.echallan.exception.InvalidViolationException.class, () -> new Challan(null, 100));
        Violation violation = new Violation("KA01AB1234", ViolationType.ILLEGAL_PARKING, "MG Road",
                LocalDateTime.now(), 0, 0);
        assertThrows(com.agile.echallan.exception.InvalidViolationException.class, () -> new Challan(violation, 0));
        assertThrows(com.agile.echallan.exception.InvalidViolationException.class, () -> new Challan(violation, -50));
    }
}
