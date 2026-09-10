package com.agile.echallan.service;

import com.agile.echallan.enums.PaymentStatus;
import com.agile.echallan.enums.VehicleRiskCategory;
import com.agile.echallan.enums.VehicleType;
import com.agile.echallan.enums.ViolationType;
import com.agile.echallan.exception.ChallanAlreadyPaidException;
import com.agile.echallan.exception.ChallanNotFoundException;
import com.agile.echallan.exception.DuplicateChallanException;
import com.agile.echallan.exception.DuplicateVehicleException;
import com.agile.echallan.exception.InvalidVehicleException;
import com.agile.echallan.exception.VehicleNotFoundException;
import com.agile.echallan.model.Challan;
import com.agile.echallan.model.Owner;
import com.agile.echallan.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChallanManagementServiceTest {

    private ChallanManagementService service;

    @BeforeEach
    void setUp() {
        service = new ChallanManagementService();
    }

    private Owner newOwner(String suffix) {
        return new Owner("Owner " + suffix, "Address " + suffix, "9876543210", "LIC" + suffix);
    }

    private Vehicle registerVehicle(String number) {
        Vehicle vehicle = new Vehicle(number, newOwner(number), VehicleType.CAR);
        return service.registerVehicle(vehicle);
    }

    @Test
    void registersVehicleSuccessfully() {
        registerVehicle("KA01AB1234");
        assertEquals(1, service.getVehicleCount());
        assertEquals("KA01AB1234", service.getVehicle("KA01AB1234").getVehicleNumber());
    }

    @Test
    void rejectsNullVehicleRegistration() {
        assertThrows(InvalidVehicleException.class, () -> service.registerVehicle(null));
    }

    @Test
    void rejectsDuplicateVehicleRegistration() {
        registerVehicle("KA01AB1234");
        assertThrows(DuplicateVehicleException.class, () -> registerVehicle("KA01AB1234"));
    }

    @Test
    void throwsWhenLookingUpUnregisteredVehicle() {
        assertThrows(VehicleNotFoundException.class, () -> service.getVehicle("KA01AB1234"));
    }

    @Test
    void recordsViolationAndGeneratesUnpaidChallan() {
        registerVehicle("KA01AB1234");
        Challan challan = service.recordViolation("KA01AB1234", ViolationType.ILLEGAL_PARKING,
                "MG Road", LocalDateTime.now().minusMinutes(5), 0, 0);

        assertEquals(PaymentStatus.UNPAID, challan.getPaymentStatus());
        assertEquals(300.0, challan.getFineAmount());
        assertEquals(1, service.getChallansForVehicle("KA01AB1234").size());
    }

    @Test
    void throwsWhenRecordingViolationForUnknownVehicle() {
        assertThrows(VehicleNotFoundException.class, () ->
                service.recordViolation("KA01AB1234", ViolationType.ILLEGAL_PARKING, "MG Road",
                        LocalDateTime.now(), 0, 0));
    }

    @Test
    void preventsDuplicateChallanForSameViolationEvent() {
        registerVehicle("KA01AB1234");
        LocalDateTime timestamp = LocalDateTime.now().minusMinutes(10);
        service.recordViolation("KA01AB1234", ViolationType.ILLEGAL_PARKING, "MG Road", timestamp, 0, 0);

        assertThrows(DuplicateChallanException.class, () ->
                service.recordViolation("KA01AB1234", ViolationType.ILLEGAL_PARKING, "MG Road", timestamp, 0, 0));
    }

    @Test
    void allowsSameVehicleAndTypeAtDifferentLocationsOrTimestamps() {
        registerVehicle("KA01AB1234");
        LocalDateTime timestamp = LocalDateTime.now().minusMinutes(10);
        service.recordViolation("KA01AB1234", ViolationType.ILLEGAL_PARKING, "MG Road", timestamp, 0, 0);
        service.recordViolation("KA01AB1234", ViolationType.ILLEGAL_PARKING, "Brigade Road", timestamp, 0, 0);
        service.recordViolation("KA01AB1234", ViolationType.ILLEGAL_PARKING, "MG Road", timestamp.plusMinutes(1), 0, 0);

        assertEquals(3, service.getChallansForVehicle("KA01AB1234").size());
    }

    @Test
    void appliesHigherFineForRepeatedViolationsOfSameType() {
        registerVehicle("KA01AB1234");
        LocalDateTime timestamp = LocalDateTime.now().minusHours(2);
        Challan first = service.recordViolation("KA01AB1234", ViolationType.SIGNAL_JUMPING, "MG Road",
                timestamp, 0, 0);
        Challan second = service.recordViolation("KA01AB1234", ViolationType.SIGNAL_JUMPING, "Brigade Road",
                timestamp.plusMinutes(30), 0, 0);

        assertTrue(second.getFineAmount() > first.getFineAmount());
    }

    @Test
    void paysChallanSuccessfullyAndMovesItToPaidList() {
        registerVehicle("KA01AB1234");
        Challan challan = service.recordViolation("KA01AB1234", ViolationType.ILLEGAL_PARKING, "MG Road",
                LocalDateTime.now().minusMinutes(1), 0, 0);

        service.payChallan(challan.getChallanId());

        assertEquals(PaymentStatus.PAID, service.getChallan(challan.getChallanId()).getPaymentStatus());
        assertEquals(1, service.getPaidChallans().size());
        assertEquals(0, service.getUnpaidChallans().size());
    }

    @Test
    void throwsWhenPayingAlreadyPaidChallan() {
        registerVehicle("KA01AB1234");
        Challan challan = service.recordViolation("KA01AB1234", ViolationType.ILLEGAL_PARKING, "MG Road",
                LocalDateTime.now().minusMinutes(1), 0, 0);
        service.payChallan(challan.getChallanId());

        assertThrows(ChallanAlreadyPaidException.class, () -> service.payChallan(challan.getChallanId()));
    }

    @Test
    void throwsWhenPayingUnknownChallan() {
        assertThrows(ChallanNotFoundException.class, () -> service.payChallan("does-not-exist"));
    }

    @Test
    void calculatesOutstandingFinesCorrectlyForVehicleAndOverall() {
        registerVehicle("KA01AB1234");
        registerVehicle("MH12CD5678");
        LocalDateTime timestamp = LocalDateTime.now().minusHours(1);

        Challan c1 = service.recordViolation("KA01AB1234", ViolationType.ILLEGAL_PARKING, "MG Road", timestamp, 0, 0);
        service.recordViolation("KA01AB1234", ViolationType.NO_HELMET, "MG Road", timestamp.plusMinutes(5), 0, 0);
        service.recordViolation("MH12CD5678", ViolationType.SIGNAL_JUMPING, "Brigade Road", timestamp, 0, 0);

        double vehicleOutstanding = service.getOutstandingFinesForVehicle("KA01AB1234");
        assertEquals(300.0 + 500.0, vehicleOutstanding);

        double totalOutstanding = service.getTotalOutstandingFines();
        assertEquals(300.0 + 500.0 + 1000.0, totalOutstanding);

        service.payChallan(c1.getChallanId());
        assertEquals(500.0, service.getOutstandingFinesForVehicle("KA01AB1234"));
        assertEquals(500.0 + 1000.0, service.getTotalOutstandingFines());
    }

    @Test
    void classifiesVehicleRiskBasedOnViolationHistory() {
        registerVehicle("KA01AB1234");
        LocalDateTime timestamp = LocalDateTime.now().minusDays(1);

        assertEquals(VehicleRiskCategory.CLEAN, service.classifyVehicle("KA01AB1234"));

        service.recordViolation("KA01AB1234", ViolationType.NO_HELMET, "MG Road", timestamp, 0, 0);
        assertEquals(VehicleRiskCategory.MODERATE_RISK, service.classifyVehicle("KA01AB1234"));

        service.recordViolation("KA01AB1234", ViolationType.NO_HELMET, "MG Road", timestamp.plusMinutes(1), 0, 0);
        service.recordViolation("KA01AB1234", ViolationType.NO_HELMET, "MG Road", timestamp.plusMinutes(2), 0, 0);
        assertEquals(VehicleRiskCategory.HIGH_RISK, service.classifyVehicle("KA01AB1234"));

        service.recordViolation("KA01AB1234", ViolationType.NO_HELMET, "MG Road", timestamp.plusMinutes(3), 0, 0);
        service.recordViolation("KA01AB1234", ViolationType.NO_HELMET, "MG Road", timestamp.plusMinutes(4), 0, 0);
        assertEquals(VehicleRiskCategory.BLACKLISTED, service.classifyVehicle("KA01AB1234"));
    }

    @Test
    void throwsWhenClassifyingUnknownVehicle() {
        assertThrows(VehicleNotFoundException.class, () -> service.classifyVehicle("KA01AB1234"));
    }

    @Test
    void multipleSimultaneousFailuresAreEachReportedIndependently() {
        assertThrows(InvalidVehicleException.class, () -> new Vehicle("", newOwner("x"), VehicleType.CAR));
        assertThrows(VehicleNotFoundException.class, () -> service.getVehicle("KA01AB1234"));
        assertThrows(ChallanNotFoundException.class, () -> service.getChallan("unknown-id"));
        assertEquals(0, service.getVehicleCount());
        assertEquals(0, service.getChallanCount());
    }

    @Test
    void getChallansForVehicleReturnsEmptyListWhenNoViolationsRecorded() {
        registerVehicle("KA01AB1234");
        List<Challan> challans = service.getChallansForVehicle("KA01AB1234");
        assertTrue(challans.isEmpty());
    }
}
