package com.ericsson.raso.cac.cagw.dao;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.satnar.common.LogService;
import com.satnar.common.SpringHelper;

public class ConcurrencyControl {
    private static final Object syncLock = new Object();
    private static final String DB_PROP = "DB_PROP";
    private static final String POOL_SIZE = "poolSize";
    private static final String KEEP_ALIVE = "keepAlive";
    private static final String QUEUE_SIZE = "queueSize";
    
    private static ExecutorService threadPool = null;
    
    public static void enqueueExecution(Callable<Void> asyncThreadWorker) {
        int poolSize = 0;
        int keepAlive = 0;
        int queueSize = 0;
        
        if (threadPool == null) {
            synchronized (syncLock) {
                if (threadPool == null) {
                    // poolsize
                    String param = SpringHelper.getConfig().getValue(DB_PROP, POOL_SIZE);
                    if (param == null) {
                        LogService.appLog.error("Section: " + DB_PROP + ", Property: " + POOL_SIZE + " - missing param in config!!");
                        throw new IllegalStateException("Bad Configuration. Concurrency Control not initialized. Check logs!");
                    }
                    try {
                        poolSize = Integer.parseInt(param);
                        if (poolSize < 0) {
                            LogService.appLog.error("Section: " + DB_PROP + ", Property: " + POOL_SIZE + " - bad param in config. Expected positive integer!!");
                            throw new IllegalStateException("Bad Configuration. Concurrency Control not initialized. Check logs!");
                        }
                    } catch (NumberFormatException e) {
                        LogService.appLog.error("Section: " + DB_PROP + ", Property: " + POOL_SIZE + " - bad param in config. Expected positive integer!!");
                        throw new IllegalStateException("Bad Configuration. Concurrency Control not initialized. Check logs!");
                    }
                    
                    // keepAlive
                    param = SpringHelper.getConfig().getValue(DB_PROP, KEEP_ALIVE);
                    if (param == null) {
                        LogService.appLog.error("Section: " + DB_PROP + ", Property: " + KEEP_ALIVE + " - missing param in config!!");
                        throw new IllegalStateException("Bad Configuration. Concurrency Control not initialized. Check logs!");
                    }
                    try {
                        keepAlive = Integer.parseInt(param);
                        if (keepAlive < 0) {
                            LogService.appLog.error("Section: " + DB_PROP + ", Property: " + KEEP_ALIVE + " - bad param in config. Expected positive integer!!");
                            throw new IllegalStateException("Bad Configuration. Concurrency Control not initialized. Check logs!");
                        }
                    } catch (NumberFormatException e) {
                        LogService.appLog.error("Section: " + DB_PROP + ", Property: " + KEEP_ALIVE + " - bad param in config. Expected positive integer!!");
                        throw new IllegalStateException("Bad Configuration. Concurrency Control not initialized. Check logs!");
                    }
                    
                    // queueSize
                    param = SpringHelper.getConfig().getValue(DB_PROP, QUEUE_SIZE);
                    if (param == null) {
                        LogService.appLog.error("Section: " + DB_PROP + ", Property: " + QUEUE_SIZE + " - missing param in config!!");
                        throw new IllegalStateException("Bad Configuration. Concurrency Control not initialized. Check logs!");
                    }
                    try {
                        queueSize = Integer.parseInt(param);
                        if (queueSize < 0) {
                            LogService.appLog.error("Section: " + DB_PROP + ", Property: " + QUEUE_SIZE + " - bad param in config. Expected positive integer!!");
                            throw new IllegalStateException("Bad Configuration. Concurrency Control not initialized. Check logs!");
                        }
                    } catch (NumberFormatException e) {
                        LogService.appLog.error("Section: " + DB_PROP + ", Property: " + QUEUE_SIZE + " - bad param in config. Expected positive integer!!");
                        throw new IllegalStateException("Bad Configuration. Concurrency Control not initialized. Check logs!");
                    }
                    
                    // thread pool init
                    threadPool = new ThreadPoolExecutor(poolSize, poolSize, keepAlive, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(queueSize));
                }
            }
        }
        
        threadPool.submit(asyncThreadWorker);
    }

    @Override
    protected void finalize() throws Throwable {
        threadPool.shutdownNow();
        threadPool.awaitTermination(400, TimeUnit.MILLISECONDS);
        super.finalize();
    }
    
    
    
}
