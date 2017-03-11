package com.kefirkb.core;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface FileWorkerService {

    void updateStatistics(String fileKey) throws IOException;

    List<File> getFullListOfFiles();

    File containsFile(String fileName);
}
