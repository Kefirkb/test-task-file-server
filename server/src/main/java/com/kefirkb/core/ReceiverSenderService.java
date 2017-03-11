package com.kefirkb.core;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by Sergey on 11.03.2017.
 */
public interface ReceiverSenderService {
    void openReceiverSender(Socket clientSocket) throws IOException;

    boolean isClosed();

    void start() throws IOException, ClassNotFoundException;

    void closeReceiver() throws IOException;
}
