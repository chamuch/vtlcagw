package com.ericsson.raso.cac.cagw.dao;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ConcurrencyControl {
    
    private static ExecutorService threadPool = new ThreadPoolExecutor(150, 150, 30000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(220));
    
    public static void enqueueExecution(Callable<Void> asyncThreadWorker) {
        threadPool.submit(asyncThreadWorker);
    }

    @Override
    protected void finalize() throws Throwable {
        threadPool.shutdownNow();
        threadPool.awaitTermination(400, TimeUnit.MILLISECONDS);
        super.finalize();
    }
    
    
    
}
