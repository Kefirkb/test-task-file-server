package com.kefirkb.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
@Slf4j
public class FileHelperServiceImpl implements FileHelperService {

    private String workFolderName;
    private BufferedOutputStream fileWriter;

    @Autowired
    public FileHelperServiceImpl(@Qualifier(value = "workFolder") String workFolderName) {
        this.validateWorkFolderName(workFolderName);
        this.workFolderName = workFolderName;
    }

    private void validateWorkFolderName(String workFolderName) {
        File file = new File(workFolderName);

        if (!file.exists() || !file.isDirectory()) {
            String message = "Invalid file directory " + workFolderName;
            log.error(message);
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public void createFile(File file) throws FileNotFoundException {
        this.fileWriter =
                new BufferedOutputStream(
                        new FileOutputStream(
                                new File(this.workFolderName + File.separator + file.getName())));
    }

    @Override
    public void writeByteBuffer(byte[] bytes) throws IOException {
        this.writeByteBuffer(bytes, 0, bytes.length);
    }

    @Override
    public void writeByteBuffer(byte[] bytes, int offset, int length) throws IOException {
        this.fileWriter.write(bytes, offset, length);
        this.fileWriter.flush();
    }

    @Override
    public void closeFile() throws IOException {
        this.fileWriter.close();
    }
}
