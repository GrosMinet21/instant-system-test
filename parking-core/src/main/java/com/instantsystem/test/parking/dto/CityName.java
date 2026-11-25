package com.instantsystem.test.parking.dto;

public enum CityName {
    POITIERS;

    public static CityName fromName(String name) {
        if (name == null) return null;
        return valueOf(name.toUpperCase());
    }
}
