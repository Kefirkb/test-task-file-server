package com.kefirkb.core;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface FileWorkerService {

    void saveStatistics() throws IOException;

    void incrementStatistics(String fileKey);

    List<File> getFullListOfFiles();

    File containsFile(String fileName);
}
