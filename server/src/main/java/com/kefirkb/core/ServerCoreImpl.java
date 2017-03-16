package com.kefirkb.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
public abstract class ServerCoreImpl implements ServerCore {

    private List<ReceiverSenderService> clientList;
    private volatile boolean shutDown;

    private ServerSocket serverSocket;
    private ApplicationContext applicationContext;
    private ThreadPoolTaskExecutor executor;
    private int frequencySaveStatistics;
    private FileWorkerService fileWorkerService;
    private int maxClients;

    public ServerCoreImpl(
            @Autowired ServerSocket serverSocket,
            @Autowired FileWorkerService fileWorkerService,
            @Value("${time.frequencySaveStatistics}") int frequencySaveStatistics,
            @Qualifier(value = "threadPoolTaskExecutor") ThreadPoolTaskExecutor executor,
            @Value("${server.maxClients}") int maxClients) throws IOException {
        this.serverSocket = serverSocket;
        this.fileWorkerService = fileWorkerService;
        this.frequencySaveStatistics = frequencySaveStatistics;
        this.maxClients = maxClients;
        this.executor = executor;

        this.validateMaxClients();

        this.executor.setCorePoolSize(this.getPoolThreadsSize());
        this.shutDown = false;
        this.clientList = new ArrayList<>();
        log.info("Server created with port " + serverSocket.getLocalPort());

    }

    private void validateMaxClients() {

        if (this.maxClients > this.executor.getMaxPoolSize() - 2 || this.maxClients < 1) {
            String message = "Invalid max count of clients.";
            log.error(message);
            throw new IllegalArgumentException(message);
        }
    }

    private int getPoolThreadsSize() {
        return this.maxClients + 2;//1 thread for accept connections and 1 thread for fileWorker;
    }

    @Override
    public void start() {
        log.info("Server start working!");
        executor.execute(new AcceptHelper());
        executor.execute(new StatisticsSaverRunnable());
    }

    @Override
    public void shutDown() throws IOException {
        log.info("Start shutDown server.");
        shutDown = true;
        executor.shutdown();
        if (!serverSocket.isClosed()) {
            serverSocket.close();
        }

        for (ReceiverSenderService c : clientList) {
            if (!c.isClosed()) {
                c.closeReceiver();
            }
        }
        log.info("End shutDown server.");
    }

    private void addClient(Socket socket) throws IOException {

        if (this.isClientsPoolFull()) {
            log.info("Cannot accept connection of client because current count of clients = maxClient");
            socket.close();
            return;
        }
        final ReceiverSenderService clientReceiver = this.getReceiverSenderService();
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

    private boolean isClientsPoolFull() {
        return this.clientList.size() >= this.executor.getCorePoolSize() - 2;
    }

    private boolean isShutDown() {
        return ServerCoreImpl.this.shutDown;
    }

    private class AcceptHelper implements Runnable {
        @Override
        public void run() {
            while (!isShutDown()) {

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

    private class StatisticsSaverRunnable implements Runnable {
        @Override
        public void run() {
            try {
                while (!isShutDown()) {
                    ServerCoreImpl.this.fileWorkerService.saveStatistics();
                    Thread.sleep(1000 * frequencySaveStatistics);
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            } catch (InterruptedException e) {
                log.debug("Thread with statistics saver interrupted");
                log.info(e.getMessage());
            }
        }
    }
}