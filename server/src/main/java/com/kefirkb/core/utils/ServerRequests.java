package com.kefirkb.core.utils;


import lombok.Getter;

public enum ServerRequests {
    GET_FILE_LIST_REQUEST("get file list"),
    GET_FILE_REQUEST("get file"),
    DISCONNECT_REQUEST("disconnect");

    @Getter
    private String request;

    ServerRequests(String request) {
        this.request = request;
    }
}
