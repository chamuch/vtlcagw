package com.ericsson.raso.cac.cagw.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class SmsEdrPersistenceHelper {
    private static final String TRANSACTION_TABLE = "transaction";
        
    private Cluster cluster = null;
    private Session session = null;
    private ExecutorService threadPool = null;
    private String keyspace = null;
    
    private static List<Future<Transaction>> fetchTasks = new ArrayList<Future<Transaction>>();
    private static List<Future<Boolean>> updateTasks = new ArrayList<Future<Boolean>>();
    private static List<Future<Boolean>> deleteTasks = new ArrayList<Future<Boolean>>();
    
    public SmsEdrPersistenceHelper(String cassandraAddresses, String keyspace, int poolSize) {
        
        Cluster.Builder builder = Cluster.builder();
        String[] addresses = cassandraAddresses.split(",");
        for (String nodeAddress: addresses) {
            builder = builder.addContactPoint(nodeAddress);
        }
        this.cluster = builder.build();
        this.keyspace = keyspace;
        this.session = this.cluster.connect(this.keyspace);
        
        this.threadPool = new ThreadPoolExecutor(poolSize, poolSize, 30000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(poolSize));
    }
    
    public void submitFetchTransaction(String[] fields) {
        Future<Transaction> fetch = this.threadPool.submit(new FetchSmsChargingWorker(this.session, this.keyspace, fields));
        fetchTasks.add(fetch);
    }
    
    public void submitUpdateTransaction(Transaction transaction) {
        Future<Boolean> update = this.threadPool.submit(new UpdateSmsChargingWorker(this.session, this.keyspace, transaction));
        updateTasks.add(update);
    }
    
    public void submitDeleteTransaction(Transaction transaction) {
        Future<Boolean> delete = this.threadPool.submit(new UpdateSmsChargingWorker(this.session, this.keyspace, transaction));
        deleteTasks.add(delete);
    }
    
    public Transaction fetchTransaction() {
        for (Future<Transaction> fetch: fetchTasks) {
            if (fetch.isDone()) {
                fetchTasks.remove(fetch);
                try {
                    return fetch.get();
                } catch (InterruptedException e) {
                    System.out.println("Fetch Transaction was interrupted in execution!!");
                    return null;
                } catch (ExecutionException e) {
                    System.out.println("Fetch Transaction has failed during execution!!");
                    return null;
                }
            }
        }
        return null;
    }
    
    public Boolean getUpdateResult() {
        for (Future<Boolean> update: updateTasks) {
            if (update.isDone()) {
                updateTasks.remove(update);
                try {
                    return update.get();
                } catch (InterruptedException e) {
                    System.out.println("Update Transaction was interrupted in execution!!");
                    return false;
                } catch (ExecutionException e) {
                    System.out.println("Update Transaction has failed during execution!!");
                    return false;
                }
            }
        }
        return null;
    }
    
    public Boolean getDeleteResult() {
        for (Future<Boolean> delete: deleteTasks) {
            if (delete.isDone()) {
                deleteTasks.remove(delete);
                try {
                    return delete.get();
                } catch (InterruptedException e) {
                    System.out.println("Delete Transaction was interrupted in execution!!");
                    return false;
                } catch (ExecutionException e) {
                    System.out.println("Delete Transaction has failed during execution!!");
                    return false;
                }
            }
        }
        return null;
    }
    
    public boolean anyFetchPending() {
        return (fetchTasks.size() > 0);
    }
    
    public boolean anyUpdatePending() {
        return (updateTasks.size() > 0);
    }
    
    public boolean anyDeletePending() {
        return (deleteTasks.size() > 0);
    }
    
    
}
