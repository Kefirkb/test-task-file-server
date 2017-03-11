package com.kefirkb.core;


import com.kefirkb.core.utils.ServerRequests;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.kefirkb.core.utils.ServerRequests.*;
import static com.kefirkb.core.utils.ServerResponseMessages.FILE_NAME_ERROR;
import static com.kefirkb.core.utils.ServerResponseMessages.UNKNOWN_REQUEST;
import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

@Slf4j
public class ReceiverSenderServiceImpl implements ReceiverSenderService {

    private static final String GET_FILE_COMMAND = "^(" + GET_FILE_REQUEST.getRequest() + ") " + Pattern.quote("\"") + "[a-zA-Z1-9а-яА-Я+=,-_)( " + Pattern.quote(File.separator) + "]+(.)[a-zA-Z0-9]+" + Pattern.quote("\"");
    private static final int BYTE_FRAME_SIZE = 1024;

    private Socket clientSocket;
    private ObjectInputStream objectReader;
    private ObjectOutputStream objectWriter;
    private volatile boolean shutdown;

    private FileWorkerService fileWorkerService;

    public ReceiverSenderServiceImpl(@Autowired FileWorkerService fileWorkerService) throws IOException {
        this.shutdown = false;
        this.fileWorkerService = fileWorkerService;
    }

    @Override
    public void openReceiverSender(Socket clientSocket) throws IOException {
        log.info("Open ReceiverSender with " + clientSocket.getLocalAddress());
        this.clientSocket = clientSocket;
        this.objectReader = new ObjectInputStream(clientSocket.getInputStream());
        this.objectWriter = new ObjectOutputStream(clientSocket.getOutputStream());
    }

    @Override
    public void start() throws IOException, ClassNotFoundException {
        log.info("Start ReceiverSender...");
        this.sendStartInfo();
        while (!isClosed()) {
            Object obj = objectReader.readObject();
            this.processData(obj);
        }
    }

    private void sendStartInfo() throws IOException {
        log.info("Send start info.");
        this.sendResponse(BYTE_FRAME_SIZE);
        this.sendListOfCommands();
    }

    private void sendListOfCommands() throws IOException {
        this.sendResponse(
                stream(ServerRequests.values())
                        .map(ServerRequests::getRequest)
                        .collect(toList()));
    }

    @Override
    synchronized public boolean isClosed() {
        return this.shutdown;
    }

    @Override
    synchronized public void closeReceiver() throws IOException {
        log.info("Close ReceiverSender.");
        this.shutdown = true;
        this.closeConnectionSocket();
    }

    private void closeConnectionSocket() throws IOException {
        if (!this.clientSocket.isClosed()) {
            this.clientSocket.close();
        }
    }

    private void processData(Object receivedObject) throws IOException {
        log.info("Receive some request.");

        if (receivedObject instanceof String) {

            if (this.isGetFileRequest(receivedObject)) {
                log.info("Start processing get file request.");
                this.processGetFileRequest((String) receivedObject);
                return;
            }

            if (this.isGetFileListRequest(receivedObject)) {
                log.info("Start processing get file list request.");
                this.processGetFileListRequest();
                return;
            }
            if (isDisconnectRequest(receivedObject)) {
                log.info("Start processing disconnect request.");
                this.closeReceiver();
                return;
            }
            log.info(UNKNOWN_REQUEST.getMessage());
            this.sendResponse(UNKNOWN_REQUEST.getMessage());
            return;
        }

        log.info(UNKNOWN_REQUEST.getMessage());
        this.sendResponse(UNKNOWN_REQUEST.getMessage());
    }

    private boolean isGetFileRequest(Object receivedObject) {
        String commandLine = (String) receivedObject;
        Pattern pattern = Pattern.compile(GET_FILE_COMMAND);
        Matcher matcher = pattern.matcher(commandLine);
        return matcher.matches();
    }

    private boolean isGetFileListRequest(Object receivedObject) {
        return receivedObject.equals(GET_FILE_LIST_REQUEST.getRequest());
    }

    private boolean isDisconnectRequest(Object receivedObject) {
        return receivedObject.equals(DISCONNECT_REQUEST.getRequest());
    }

    private void processGetFileRequest(String commandLine) throws IOException {
        String[] parsedStrings = commandLine.split("\"");
        String parsedFileName = parsedStrings[parsedStrings.length - 1];
        File f = fileWorkerService.containsFile(parsedFileName);

        if (nonNull(f)) {
            this.sendResponse(f);

            byte[] buffer = new byte[BYTE_FRAME_SIZE];
            BufferedInputStream bufferedDataStream = new BufferedInputStream(new FileInputStream(f));

            while (bufferedDataStream.available() > 0) {
                bufferedDataStream.read(buffer);
                this.sendDataBuffer(buffer);
            }
            bufferedDataStream.close();
            fileWorkerService.updateStatistics(f.getAbsolutePath());
            return;
        }
        this.sendResponse(FILE_NAME_ERROR.getMessage());
    }

    private void processGetFileListRequest() throws IOException {
        Object response = fileWorkerService.getFullListOfFiles();
        this.sendResponse(response);
    }

    private void sendResponse(Object object) throws IOException {
        log.info("Send response to client.");
        this.objectWriter.writeObject(object);
        this.objectWriter.flush();
    }

    private void sendDataBuffer(byte[] buffer) throws IOException {
        this.clientSocket.getOutputStream().write(buffer);
        this.clientSocket.getOutputStream().flush();
    }
}