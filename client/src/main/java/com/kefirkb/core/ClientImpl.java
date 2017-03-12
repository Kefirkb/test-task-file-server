package com.kefirkb.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class ClientImpl implements Client {

    private ClientService clientService;
    private FileHelperService fileHelperService;
    @Getter
    private Integer frameSize = 1024;
    @Getter
    private List<String> availableCommandLines;

    @Autowired
    public ClientImpl(@Autowired ClientService clientService,
                      @Autowired FileHelperService fileHelperService) {
        this.clientService = clientService;
        this.fileHelperService = fileHelperService;
    }

    @Override
    public void start() throws IOException, ClassNotFoundException {
        this.clientService.connect();
        this.getStartInfo();
    }

    private void getStartInfo() throws IOException, ClassNotFoundException {
        this.frameSize = (Integer) this.clientService.receiveObject();
        this.clientService.setBytesFrameSize(frameSize);
        this.availableCommandLines = (List<String>) this.clientService.receiveObject();
    }

    @Override
    public void executeCommand(String commandLine) throws IOException, ClassNotFoundException {
        this.clientService.sendObject(commandLine);
        Object response = this.clientService.receiveObject();
        this.processResponse(response);
    }

    private void processResponse(Object response) throws IOException {

        if (response instanceof String) {
            log.info("Received string: " + response);
            return;
        }

        if (response instanceof List) {
            this.processIfResponseIsList((List<File>) response);
            return;
        }

        if (response instanceof File) {
            this.processIfResponseIsFileInfo((File) response);
        }
    }

    private void processIfResponseIsList(List<File> response) {
        log.info("Received list: ");
        response.forEach(
                f -> System.out.println(f.getAbsolutePath())
        );
    }

    private void processIfResponseIsFileInfo(File response) throws IOException {
        log.info("Receive File info " + response.getAbsolutePath());
        long fileSize = response.length();
        long counter = 0;
        this.fileHelperService.createFile(response);

        while (counter < fileSize) {
            byte[] receivedBytes = clientService.receiveBytes();
            counter += this.frameSize;

            if (counter < fileSize) {
                this.fileHelperService.writeByteBuffer(receivedBytes);
            } else {
                this.fileHelperService.writeByteBuffer(receivedBytes, 0, this.frameSize - (int) (counter - fileSize));
            }
        }

        this.fileHelperService.closeFile();
    }
}
