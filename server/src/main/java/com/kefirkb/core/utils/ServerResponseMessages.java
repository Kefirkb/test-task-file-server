package com.kefirkb.core.utils;


import lombok.Getter;

public enum ServerResponseMessages {
    FILE_NAME_ERROR("No file with this name!"),
    UNKNOWN_REQUEST("Unknown request!");

    @Getter
    private String message;

    ServerResponseMessages(String message) {
        this.message = "SERVER: " + message;
    }
}
