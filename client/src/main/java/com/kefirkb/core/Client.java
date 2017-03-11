package com.kefirkb.core;

import java.io.IOException;
import java.util.List;

/**
 * Created by Sergey on 11.03.2017.
 */
public interface Client {
    void start() throws IOException, ClassNotFoundException;

    void executeCommand(String commandLine) throws IOException, ClassNotFoundException;

    List<String> getAvailableCommandLines();

    Integer getFrameSize();
}
