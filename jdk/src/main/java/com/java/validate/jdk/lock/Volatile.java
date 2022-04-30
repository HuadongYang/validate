package com.java.validate.jdk.lock;

public class Volatile {

    private static volatile int x = 0;


    public static void main(String[] args) {
        new Thread(() -> {
            int tv = x;
            while (x < 5) {
                if (tv != x) {
                    System.out.println("get change for x: " + x);
                    tv = x;
                }
            }
            System.out.println("break");
        }).start();

        new Thread(() -> {
            while (x < 5) {
                System.out.println("incrementing x to " + (x + 1) + "");
                x ++;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
