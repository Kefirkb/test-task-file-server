package com.kefirkb.core;


import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@NoArgsConstructor
@Service
@Slf4j
public class FileWorkerServiceImpl implements FileWorkerService {

    private String workFolder;
    private String statisticsFilePath;

    private List<File> fullListOfFiles;

    private HashMap<String, Integer> mapStatistics;
    private BufferedWriter writer;
    private File fileStatistics;

    @Autowired
    public FileWorkerServiceImpl(@Qualifier(value = "workFolder") String workFolder,
                                 @Qualifier(value = "fileStatistics") String statisticsFilePath) throws IOException {
        this.workFolder = workFolder;
        this.statisticsFilePath = statisticsFilePath;
    }

    @PostConstruct
    private void initialWork() throws IOException {
        this.validateWorkFolder(workFolder);
        this.fullListOfFiles = getFullListOfFiles();
        this.createStatistics();
        this.saveStatistics();
    }

    private void validateWorkFolder(String str) throws FileNotFoundException {
        File f = new File(str);

        if (!f.exists() || !f.isDirectory()) {
            String message = "file " + str + "not exists or is not a directory!";
            log.error(message);
            throw new FileNotFoundException(message);
        }
    }

    @Override
    public List<File> getFullListOfFiles() {
        fullListOfFiles = new ArrayList<>();
        this.getFilesRecursive(new File(workFolder));
        return fullListOfFiles;
    }

    private void getFilesRecursive(File folder) {

        if (nonNull(folder) && folder.isDirectory()) {
            File[] listFiles = folder.listFiles();

            if (nonNull(listFiles)) {

                for (File f : listFiles) {

                    if (!f.isDirectory()) fullListOfFiles.add(f);

                    if (f.isDirectory()) {
                        this.getFilesRecursive(f);
                    }
                }
            }
        }
    }

    private void createStatistics() throws IOException {

        mapStatistics = new HashMap<>();
        fileStatistics = new File(statisticsFilePath);
        writer = new BufferedWriter(new FileWriter(fileStatistics));

        for (File fl : fullListOfFiles) {
            mapStatistics.put(fl.getAbsolutePath(), 0);
            writer.write(fl.getAbsolutePath() + System.lineSeparator() + "=0" + System.lineSeparator());
            writer.flush();
        }
        writer.close();
    }

    @Override
    synchronized public void updateStatistics(String fileKey) throws IOException {
        File file = this.containsFile(fileKey);

        if (nonNull(file)) {
            Integer value = mapStatistics.get(fileKey);

            if (nonNull(value)) {
                value++;
                mapStatistics.put(fileKey, value);
                saveStatistics();
            }
        }
    }

    @Override
    public File containsFile(String fileName) {

        if (isNull(fileName) || fileName.isEmpty()) {
            String message = fileName + " is invalid file name";
            log.error(message);
            throw new IllegalArgumentException(message);
        }

        for (File f : fullListOfFiles) {

            if (fileName.equals(f.getAbsolutePath())) {
                return f;
            }
        }
        return null;
    }

    private void saveStatistics() throws IOException {
        writer = new BufferedWriter(new FileWriter(fileStatistics));

        for (Map.Entry<String, Integer> rec : mapStatistics.entrySet()) {
            writer.write(rec.getKey() + "=" + rec.getValue() + System.lineSeparator());
        }
        writer.close();
    }

}