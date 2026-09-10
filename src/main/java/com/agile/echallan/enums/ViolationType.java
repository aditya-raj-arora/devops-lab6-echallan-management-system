package com.agile.echallan.enums;

public enum ViolationType {
    OVER_SPEEDING(500.0),
    SIGNAL_JUMPING(1000.0),
    ILLEGAL_PARKING(300.0),
    NO_HELMET(500.0),
    DRUNK_DRIVING(5000.0),
    WRONG_SIDE_DRIVING(1000.0);

    private final double baseFine;

    ViolationType(double baseFine) {
        this.baseFine = baseFine;
    }

    public double getBaseFine() {
        return baseFine;
    }
}
