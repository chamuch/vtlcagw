package com.satnar.common;

public class PlatformConcurrency {
    
    public static void main(String[] args) {
        
        int threadCount = 0;
        
        try {
            for (threadCount=0; threadCount < 3000; threadCount++) {
                new Thread(new Runnable() {
                    
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3600000);
                        } catch (InterruptedException e) {
                            System.out.println("Thread broke its slumber!!");
                        }
                        
                    }
                }).start();
            }
        } catch (Exception e) {
            System.out.println("Broke at count: " + threadCount);
            e.printStackTrace();
        } catch (Error e) {
            System.out.println("Broke at count: " + threadCount);
            e.printStackTrace();
        }
    }
    
}
