package com.kefirkb.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class ServerCore {

    private ServerSocket serverSocket;
    private Thread fileStatsThread;
    private List<Socket> clientSocketList;
    private boolean shutdown;
    private FileWorkerService fworker;

    private class AcceptionClass implements Runnable {
        @Override
        public void run() {

            while (!shutdown) {
                try {
                    Socket sock = serverSocket.accept();
                    addClient(sock);
                    System.out.println("Client add " + sock.hashCode());
                } catch (SocketException e) {
                    System.out.println(e);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ServerCore(String workFolder, int port) throws IOException {
        shutdown = false;
        serverSocket = new ServerSocket(port);
        System.out.println("Server inet address " + serverSocket.getInetAddress());
        clientSocketList = new ArrayList<>();
        fworker = new FileWorkerService(workFolder, null);
        Thread acceptionThread = new Thread(new AcceptionClass());
        acceptionThread.start();
        fileStatsThread = new Thread(() -> {
            while (!shutdown) {
                try {
                    Thread.currentThread().sleep(2000);
                    fworker.saveStats();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        fileStatsThread.start();
    }

    public void shutDown() throws IOException {
        shutdown = true;
        if (!serverSocket.isClosed()) serverSocket.close();
        for (Socket c : clientSocketList) {
            if (!c.isClosed()) {
                try {
                    c.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void addClient(Socket socket) throws IOException {
        clientSocketList.add(socket);
        ReceiverSenderClass clientreciever = new ReceiverSenderClass(socket);//start client thread to service client
    }
}