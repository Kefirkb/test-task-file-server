package com.kefirkb.core;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

@Service
@Slf4j
public class ClientServiceImpl implements ClientService {

    private InetSocketAddress socketAddress;

    @Setter
    private Integer bytesFrameSize = 1024;
    private Socket clientSocket;
    private ObjectOutputStream writer;
    private ObjectInputStream reader;

    @Autowired
    public ClientServiceImpl(@Qualifier(value = "inetSocketAddress")
                                     InetSocketAddress addr) throws IOException {
        this.socketAddress = addr;
    }

    @Override
    public void connect() throws IOException {
        this.clientSocket = new Socket(socketAddress.getHostName(), socketAddress.getPort());
        this.writer = new ObjectOutputStream(clientSocket.getOutputStream());
        this.reader = new ObjectInputStream(clientSocket.getInputStream());
        log.info("Connection successfull");
    }

    @Override
    public void sendObject(Object obj) throws IOException {
        log.info("Sending object...");
        writer.writeObject(obj);
        writer.flush();
        log.info("Sending object Successfull!");
    }

    @Override
    public Object receiveObject() throws IOException, ClassNotFoundException {
        return reader.readObject();
    }

    @Override
    public byte[] receiveBytes() throws IOException {
        byte[] receivedBytes = new byte[bytesFrameSize];
        this.clientSocket.getInputStream().read(receivedBytes);
        return receivedBytes;
    }

    @Override
    public void closeConnection() throws IOException {
        if (this.isConnected())
            this.clientSocket.close();
    }

    @Override
    public boolean isConnected() {
        return this.clientSocket.isConnected();
    }
}
