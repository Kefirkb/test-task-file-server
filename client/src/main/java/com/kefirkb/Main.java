package com.kefirkb;


import com.kefirkb.core.Client;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-config.xml");
        Client client = ctx.getBean(Client.class);
        client.start();

        BufferedReader inputStream = new BufferedReader((new InputStreamReader(System.in)));
        String input = "";
        printMenu(client);

        while (!"y".equals(input)) {
            System.out.println("Enter command line: ");
            System.out.print(">");
            input = inputStream.readLine();

            if (!"y".equals(input)) {
                client.executeCommand(input);
            }
        }
    }

    private static void printMenu(Client client) {
        printFrameSize(client);
        printAvailableCommands(client);
        System.out.println("y = exit");
        System.out.println("Enter command");
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
