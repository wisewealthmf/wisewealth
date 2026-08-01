package com.wisewealth.entity;

public enum StatusEnum {
    NEW("New"),
    IN_PROGRESS("In Progress"),
    APPOINTMENT_CONFIRMED("Appointment Confirmed"),
    REPLIED("Replied"),
    CLIENT_CONFIRMED("Client Confirmed"),
    CLOSE("Close");

    private final String value;

    StatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
