package com.agile.echallan.model;

import com.agile.echallan.exception.InvalidVehicleException;

public class Owner {
    private final String name;
    private final String address;
    private final String phoneNumber;
    private final String licenseNumber;

    public Owner(String name, String address, String phoneNumber, String licenseNumber) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidVehicleException("Owner name cannot be null or empty");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new InvalidVehicleException("Owner address cannot be null or empty");
        }
        if (phoneNumber == null || !phoneNumber.matches("\\d{10}")) {
            throw new InvalidVehicleException("Owner phone number must be a 10 digit number");
        }
        if (licenseNumber == null || licenseNumber.trim().isEmpty()) {
            throw new InvalidVehicleException("Owner license number cannot be null or empty");
        }
        this.name = name.trim();
        this.address = address.trim();
        this.phoneNumber = phoneNumber.trim();
        this.licenseNumber = licenseNumber.trim();
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    @Override
    public String toString() {
        return "Owner{name='" + name + "', licenseNumber='" + licenseNumber + "'}";
    }
}
