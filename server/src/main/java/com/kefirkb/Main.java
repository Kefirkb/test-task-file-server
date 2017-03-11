package com.kefirkb;

import com.kefirkb.core.ServerCore;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws InterruptedException, IOException {

        ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-context.xml");
        ServerCore core = ctx.getBean(ServerCore.class);
        core.start();
    }

}
