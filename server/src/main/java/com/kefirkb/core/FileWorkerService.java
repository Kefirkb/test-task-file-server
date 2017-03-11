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

@NoArgsConstructor
@Service
@Slf4j
public class FileWorkerService {

    private String workFolder;
    private String statisticsFilePath;
    private List<File> files;
    private HashMap<String, Integer> stats;
    private BufferedWriter writer;
    private File fileStatistics;
    private BufferedWriter logsWriter;

    @Autowired
    public FileWorkerService(@Qualifier(value = "workFolder") String workFolder,
                             @Qualifier(value = "fileStatistics") String statisticsFilePath) throws IOException {
        this.workFolder = workFolder;
        this.statisticsFilePath = statisticsFilePath;
    }

    @PostConstruct
    private void initialWork() throws IOException {
        validateWorkFolder(workFolder);
        getFiles();
        createStats();
        saveStats();
        files = new ArrayList<>();
    }

    private void validateWorkFolder(String str) throws FileNotFoundException {
        File f = new File(str);

        if (!f.exists() || !f.isDirectory()) {
            String message = "file " + str + "not exists or is not a directory!";
            log.error(message);
            throw new FileNotFoundException(message);
        }
    }

    private void getFilesRecursive(File folder) {

        if (folder != null && folder.isDirectory()) {
            File[] buffiles = folder.listFiles();
            if (buffiles != null)
                for (File f : buffiles) {
                    if (!f.isDirectory()) files.add(f);

                    if (f.isDirectory()) {
                        getFilesRecursive(f);
                    }
                }
        }

    }

    List<File> getFiles() {
        files = new ArrayList<>();
        getFilesRecursive(new File(workFolder));
        return files;
    }

    File contains(String fileName) {
        for (File f : files) {
            if (fileName.equals(f.getAbsolutePath())) {
                return f;
            }
        }
        return null;
    }


    private void createStats() throws IOException {
        stats = new HashMap<>();
        fileStatistics = new File(statisticsFilePath);
        writer = new BufferedWriter(new FileWriter(fileStatistics));

        for (File fl : files) {
            stats.put(fl.getAbsolutePath(), 0);
            writer.write(fl.getAbsolutePath() + System.lineSeparator() + "=0" + System.lineSeparator());
            writer.flush();

        }
        writer.close();
    }

    public synchronized void printlogs(String Message) throws IOException {
        if (logsWriter != null) {
            logsWriter.write(Message + System.lineSeparator());
            logsWriter.flush();
        } else {
            logsWriter = new BufferedWriter(new FileWriter(workFolder + File.separator + "logs.txt"));
            logsWriter.write(Message + System.lineSeparator());
            logsWriter.flush();
        }
    }


    public void saveStats() throws IOException {
        writer = new BufferedWriter(new FileWriter(fileStatistics));
        for (Map.Entry<String, Integer> rec : stats.entrySet()) {
            writer.write(rec.getKey() + "=" + rec.getValue() + System.lineSeparator());

        }
        writer.close();

    }

    synchronized public void updateStats(String fpath) throws IOException {
        Integer value = stats.get(fpath);
        if (value != null) {
            value++;
            stats.remove(fpath);
            stats.put(fpath, value);
            saveStats();

        }
    }

}
