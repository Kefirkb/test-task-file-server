package com.kefirkb.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ServerCoreImpl implements ServerCore {

    private List<ReceiverSenderService> clientList;
    private volatile boolean shutdown;

    private ServerSocket serverSocket;
    private ApplicationContext applicationContext;
    private ThreadPoolTaskExecutor executor;

    public ServerCoreImpl(
            @Autowired
                    ApplicationContext applicationContext,
            @Autowired
                    ServerSocket serverSocket,
            @Qualifier(value = "threadPoolTaskExecutor")
                    ThreadPoolTaskExecutor executor) throws IOException {
        this.applicationContext = applicationContext;
        this.serverSocket = serverSocket;
        this.executor = executor;
        this.executor.setCorePoolSize(100);
        this.shutdown = false;
        this.clientList = new ArrayList<>();
        log.info("Server created with port " + serverSocket.getLocalPort());

    }

    @Override
    public void start() {
        log.info("Server start working!");
        executor.execute(new AcceptHelper());
    }

    @Override
    public void shutDown() throws IOException {
        log.info("Start shutdown server.");
        shutdown = true;
        executor.shutdown();
        if (!serverSocket.isClosed()) {
            serverSocket.close();
        }

        for (ReceiverSenderService c : clientList) {
            if (!c.isClosed()) {
                c.closeReceiver();
            }
        }
        log.info("End shutdown server.");
    }

    private void addClient(Socket socket) throws IOException {
        final ReceiverSenderService clientReceiver = this.applicationContext.getBean(ReceiverSenderService.class);
        clientReceiver.openReceiverSender(socket);
        this.clientList.add(clientReceiver);

        this.executor.execute(() -> {
            try {
                clientReceiver.start();
            } catch (IOException | ClassNotFoundException e) {
                log.info(e.getMessage());
            }
        });
    }

    private class AcceptHelper implements Runnable {
        @Override
        public void run() {
            while (!ServerCoreImpl.this.shutdown) {

                try {
                    Socket sock = serverSocket.accept();
                    ServerCoreImpl.this.addClient(sock);
                    System.out.println("Client add " + sock.hashCode());
                } catch (IOException e) {
                    log.info(e.getMessage());
                }
            }
        }
    }
}