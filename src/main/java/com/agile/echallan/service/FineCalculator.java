package com.agile.echallan.service;

import com.agile.echallan.enums.ViolationType;
import com.agile.echallan.exception.InvalidViolationException;

public class FineCalculator {

    private static final double OVER_SPEED_RATE_PER_KMPH = 20.0;
    private static final double REPEAT_PENALTY_STEP = 0.5;
    private static final double MAX_REPEAT_MULTIPLIER = 3.0;

    public static double calculateFine(ViolationType violationType, double speed, double permittedSpeed,
                                        int priorViolationCountOfSameType) {
        if (violationType == null) {
            throw new InvalidViolationException("Violation type cannot be null");
        }
        if (priorViolationCountOfSameType < 0) {
            throw new InvalidViolationException("Prior violation count cannot be negative");
        }

        double baseFine = violationType.getBaseFine();
        double fine = baseFine;

        if (violationType == ViolationType.OVER_SPEEDING) {
            if (speed <= permittedSpeed) {
                throw new InvalidViolationException("Speed must exceed permitted speed to calculate an over-speeding fine");
            }
            double excess = speed - permittedSpeed;
            fine = baseFine + (excess * OVER_SPEED_RATE_PER_KMPH);
        }

        double multiplier = Math.min(1.0 + (REPEAT_PENALTY_STEP * priorViolationCountOfSameType), MAX_REPEAT_MULTIPLIER);
        fine = fine * multiplier;

        return Math.round(fine * 100.0) / 100.0;
    }
}
