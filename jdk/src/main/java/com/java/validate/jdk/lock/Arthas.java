package com.java.validate.jdk.lock;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class Arthas {

    private static AtomicInteger i = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        Thread.currentThread().setName("realme");
        for (; ; ) {
            String s = new Arthas().getTime();
            Thread.sleep(1000);

            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    System.out.println(Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "realme" + i.addAndGet(1)).start();
        }
    }

    private String getTime() {
        return new Date().toString();
    }
}
