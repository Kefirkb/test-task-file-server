package com.kefirkb.core;

import java.io.IOException;


public interface ServerCore {
    void start();

    void shutDown() throws IOException;
}
