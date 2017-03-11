package com.kefirkb.core;


import java.io.*;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReceiverSenderClass implements Runnable {

    private Socket clientSocket;
    private ObjectInputStream reader;
    private ObjectOutputStream writer;
    private boolean shutdown;
    private FileWorkerService fileWorkerService;

    public ReceiverSenderClass(Socket socket) throws IOException {
        clientSocket = socket;
        shutdown = false;
        reader = new ObjectInputStream(clientSocket.getInputStream());
        writer = new ObjectOutputStream(clientSocket.getOutputStream());
        Thread clientThread = new Thread(this);
        clientThread.start();
    }

    @Override
    public void run() {

        try {

            while (!shutdown) {
                Object obj = reader.readObject();
                processData(obj);
            }
        } catch (IOException e) {
            shutdown = true;

            if (clientSocket.isClosed()) {
                try {
                    clientSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void processData(Object receivedObject) throws IOException {

        if (receivedObject instanceof String) {
            Object response;
            String comm = (String) receivedObject;
            Pattern pattern = Pattern.compile("^(get fname )[a-zA-Z1-9а-яА-Я+=,-_ " + Pattern.quote(File.separator) + "]+(.)[a-zA-Z0-9]+");
            Matcher matcher = pattern.matcher(comm);

            if (matcher.matches()) {
                String[] strs = comm.split(" ");
                String fname = strs[strs.length - 1];
                File f = fileWorkerService.contains(fname);

                if (f != null) {
                    writer.writeObject(f);
                    writer.flush();
                    byte[] buffer = new byte[1024];// create buffer for read file and send in network
                    BufferedInputStream rstream = new BufferedInputStream(new FileInputStream(f));

                    while (rstream.available() > 0) {
                        rstream.read(buffer);
                        clientSocket.getOutputStream().write(buffer);//write buffer
                        clientSocket.getOutputStream().flush();
                    }
                    fileWorkerService.updateStats(f.getAbsolutePath());

                } else {
                    writer.writeObject("SERVERERROR: FileName Error");
                    writer.flush();
                }
            } else if (comm.equals("get flist")) {
                response = fileWorkerService.getFiles();
                writer.writeObject(response);
            } else if (comm.equals("disconnect")) {
                this.shutdown = true;
            } else {
                writer.writeObject("SERVERERROR: Unknown request string");
                writer.flush();
            }
        } else {
            writer.writeObject("SERVERERROR: Unknown request object");
            writer.flush();
        }
    }
}