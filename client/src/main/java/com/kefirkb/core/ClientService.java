package com.kefirkb.core;

import java.io.IOException;

public interface ClientService {
    void connect() throws IOException;

    void sendObject(Object obj) throws IOException;

    Object receiveObject() throws IOException, ClassNotFoundException;

    byte[] receiveBytes() throws IOException;

    void closeConnection() throws IOException;

    boolean isConnected();

    void setBytesFrameSize(Integer bytesFrameSize);
}
