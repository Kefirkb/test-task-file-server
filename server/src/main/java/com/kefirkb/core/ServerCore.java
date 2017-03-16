package com.kefirkb.core;

import java.io.IOException;


public interface ServerCore {
    void start();

    ReceiverSenderService getReceiverSenderService();

    void shutDown() throws IOException;
}
