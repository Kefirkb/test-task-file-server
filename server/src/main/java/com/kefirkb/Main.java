package com.kefirkb;

import com.kefirkb.core.FileWorkerService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

    public static void main(String[] args) {

        ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-context.xml");
        FileWorkerService service = ctx.getBean(FileWorkerService.class);
        int a = 5;
    }

}
