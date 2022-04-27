package com.java.validate.jdk.lock;

import java.util.HashMap;
import java.util.Map;

public class SynchronizedVolatile {
    private volatile Map<String, String> map = new HashMap<>();
    public void run1() throws InterruptedException {
        String test = map.get("test");
        if (test == null) {
            synchronized (map) {
                System.out.println(Thread.currentThread().getName());
                System.out.println("put start");
                Thread.sleep(2000);
                map.put("test", "value");
                System.out.println("put end");
            }
        }
    }
    public void run2() throws InterruptedException {
        String test = map.get("test");
        if (test == null) {
            synchronized (map) {
                System.out.println(Thread.currentThread().getName() + " " + test);
            }
        }
    }
    public static void main(String[] args) throws InterruptedException {
        SynchronizedVolatile synchronizedVolatile = new SynchronizedVolatile();
        new Thread(() -> {
            try {
                synchronizedVolatile.run1();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        Thread.sleep(1000);
        new Thread((Runnable) () -> {
            try {
                synchronizedVolatile.run2();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
