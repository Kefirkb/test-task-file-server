package com.kefirkb.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface FileHelperService {
    void createFile(File file) throws FileNotFoundException;

    void writeByteBuffer(byte[] bytes) throws IOException;

    void writeByteBuffer(byte[] bytes, int offset, int length) throws IOException;

    void closeFile() throws IOException;
}
