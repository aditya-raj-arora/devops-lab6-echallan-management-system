package com.agile.echallan.service;

import com.agile.echallan.enums.ViolationType;
import com.agile.echallan.exception.InvalidViolationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FineCalculatorTest {

    @Test
    void calculatesBaseFineForNonSpeedViolationWithNoRepeats() {
        double fine = FineCalculator.calculateFine(ViolationType.ILLEGAL_PARKING, 0, 0, 0);
        assertEquals(300.0, fine);
    }

    @Test
    void calculatesOverSpeedingFineProportionalToExcessSpeed() {
        double fine = FineCalculator.calculateFine(ViolationType.OVER_SPEEDING, 80, 60, 0);
        assertEquals(500.0 + (20 * 20.0), fine);
    }

    @Test
    void throwsWhenOverSpeedingSpeedDoesNotExceedPermittedSpeed() {
        assertThrows(InvalidViolationException.class, () ->
                FineCalculator.calculateFine(ViolationType.OVER_SPEEDING, 60, 60, 0));
        assertThrows(InvalidViolationException.class, () ->
                FineCalculator.calculateFine(ViolationType.OVER_SPEEDING, 50, 60, 0));
    }

    @Test
    void appliesHigherPenaltyForRepeatedViolations() {
        double firstOffence = FineCalculator.calculateFine(ViolationType.SIGNAL_JUMPING, 0, 0, 0);
        double secondOffence = FineCalculator.calculateFine(ViolationType.SIGNAL_JUMPING, 0, 0, 1);
        double thirdOffence = FineCalculator.calculateFine(ViolationType.SIGNAL_JUMPING, 0, 0, 2);

        assertTrue(secondOffence > firstOffence);
        assertTrue(thirdOffence > secondOffence);
        assertEquals(1000.0, firstOffence);
        assertEquals(1500.0, secondOffence);
        assertEquals(2000.0, thirdOffence);
    }

    @Test
    void capsRepeatPenaltyMultiplierAtMaximum() {
        double manyRepeats = FineCalculator.calculateFine(ViolationType.SIGNAL_JUMPING, 0, 0, 50);
        assertEquals(1000.0 * 3.0, manyRepeats);
    }

    @Test
    void throwsOnNullViolationType() {
        assertThrows(InvalidViolationException.class, () -> FineCalculator.calculateFine(null, 0, 0, 0));
    }

    @Test
    void throwsOnNegativePriorViolationCount() {
        assertThrows(InvalidViolationException.class, () ->
                FineCalculator.calculateFine(ViolationType.NO_HELMET, 0, 0, -1));
    }
}
