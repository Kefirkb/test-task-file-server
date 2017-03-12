package com.kefirkb;


import com.kefirkb.core.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;

@Slf4j
public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-context.xml");
        Client client = ctx.getBean(Client.class);
        client.start();

        BufferedReader inputStream = new BufferedReader((new InputStreamReader(System.in)));
        String input = "";
        printMenu(client);

        try {
            while (!"y".equals(input)) {
                System.out.println("Enter command line: ");
                System.out.print(">");
                input = inputStream.readLine();

                if (!"y".equals(input)) {
                    client.executeCommand(input);
                }
            }
        } catch (EOFException ex) {
            log.info("Disconnected.");
        } catch (SocketException ex) {
            log.info("Socket error, disconnect server.");
        }
    }

    private static void printMenu(Client client) {
        printFrameSize(client);
        printAvailableCommands(client);
        System.out.println("y = exit");
    }

    private static void printAvailableCommands(Client client) {
        System.out.println("Available Commands:");
        client.getAvailableCommandLines().forEach(System.out::println);
        System.out.println();
    }

    private static void printFrameSize(Client client) {
        System.out.println("Byte Frame size: " + client.getFrameSize());
        System.out.println();
    }
}
