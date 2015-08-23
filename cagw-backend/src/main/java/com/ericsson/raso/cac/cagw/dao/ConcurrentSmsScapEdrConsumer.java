package com.ericsson.raso.cac.cagw.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

public class ConcurrentSmsScapEdrConsumer {
    
    private static final String COMMA = ",";
    
    private static List<SmsEdrPersistenceHelper> workers = null;
    private static int workerIndex = 0;
    
    public static void main(String[] args) {
        if (args.length < 5) {
           System.out.println("Usage: java SmsScapEdrConsumer <fully_qualified_path_to_input_file> <cassandra_ip_address_csv_list> <keyspace_name> <number_of_workers> <throttle_delay");  
           System.exit(1);
        }
        
        int workerCount = 0;
        try {
            workerCount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            System.out.println("'number_of_workers' was not found positive integer!");
            System.exit(2);
        }
        
        int throttleDelay = 0;
        try {
            throttleDelay = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.out.println("'throttle_delay' was not found positive integer!");
            System.exit(2);
        }
                
        processInputFile(args[0], args[1], args[2], workerCount, throttleDelay);
    }

    private static void processInputFile(String csvPath, String cassandraIp, String keyspace, int numberOfWorkers, int throttleDelay) {
        
        workers = new ArrayList<SmsEdrPersistenceHelper>(numberOfWorkers);
        int poolSize = 800/numberOfWorkers;
        for (int i=0; i<numberOfWorkers; i++) {
            workers.add(new SmsEdrPersistenceHelper(cassandraIp, keyspace, poolSize));
        }
        System.out.println("Prepared Concurrent Cassandra Pool with: " + cassandraIp);
        
        String recordEntry = null;
        FileReader fileInput = null;
        BufferedReader textReader = null;
        int totalCount = 0;
        int successCount = 0;
        
        try {
        	System.out.println("Processing started for file:"+csvPath);
            fileInput = new FileReader(csvPath);
            textReader = new BufferedReader(fileInput);            
            
            String[] fields = null;
            
            SmsEdrPersistenceHelper worker = null;
            
            while(true) {            	
                recordEntry = textReader.readLine();
                if (recordEntry == null) {
                    System.out.println("No more entries... exiting..from:"+csvPath);
                    break;
                }
                
                // Handle record
                totalCount++;
                if (recordEntry.equals("") || !recordEntry.contains(COMMA)) {
                    System.out.println("Skipping invalid entry at line: " + totalCount);
                    continue;
                }
                
                
                fields = recordEntry.split(COMMA);
                if (fields != null) {
                    worker = getWorker();
                    try {
                        worker.submitFetchTransaction(fields);
                    } catch (RejectedExecutionException e) {
                        Thread.sleep(throttleDelay); System.out.println("Must throttle speed now");
                        do {
                            try {
                                worker.submitFetchTransaction(fields);
                                break;
                            } catch (RejectedExecutionException e1) {
                                Thread.sleep(throttleDelay); System.out.println("Must throttle speed further now");
                            }
                        } while (true);
                    }
                }
            } // distributed load across workers to fetch the transaction.
            
            do {
                worker = getWorker();
                Transaction transaction = worker.fetchTransaction();
                if (transaction != null) {
                    worker = getWorker();
                    if (fields.length > 6) { 
                        try {
                        worker.submitUpdateTransaction(transaction);
                        } catch (RejectedExecutionException e) {
                            Thread.sleep(throttleDelay); System.out.println("Must throttle speed now");
                            do {
                                try {
                                    worker.submitUpdateTransaction(transaction);
                                    break;
                                } catch (RejectedExecutionException e1) {
                                    Thread.sleep(throttleDelay); System.out.println("Must throttle speed further now");
                                }
                            } while (true);
                        }
                    } else {
                        transaction.setChargeStatus(true);
                        try{
                            worker.submitDeleteTransaction(transaction);
                        } catch (RejectedExecutionException e) {
                            Thread.sleep(throttleDelay); System.out.println("Must throttle speed now");
                            do {
                                try {
                                    worker.submitDeleteTransaction(transaction);
                                    break;
                                } catch (RejectedExecutionException e1) {
                                    Thread.sleep(throttleDelay); System.out.println("Must throttle speed further now");
                                }
                            } while (true);
                        }
                    }
                }
            } while (worker.anyFetchPending()); //distributed load across workers to update/delete
            
            do {
                worker = getWorker();
                Boolean result = worker.getUpdateResult();
                if (result != null && result == true)
                    successCount++;
                
                worker = getWorker();
                result = worker.getDeleteResult();
                if (result != null && result == true)
                    successCount++;
            } while(worker.anyUpdatePending() || worker.anyDeletePending());  // gather the results
                    
                    
                    
            while (workers.size() > 0) {
                worker = workers.get(0);
                worker.stop();
                workers.remove(worker);
            }
            System.out.println("All workers shutdown and cleaned up!");
	         
            
        } catch(Exception genE){
        	System.out.println("Encountered exception while processing file: " + genE.getMessage());
        	genE.printStackTrace();
        	try {
                new File(csvPath + ".failed").createNewFile();
            } catch (IOException e1) {
            	System.out.println("Unable to create Failed File for:"+csvPath);
            }
        }
        
        
        System.out.println("\n\nExecution Summary");
        System.out.println("=================");
        System.out.println("Total Records processed: " + totalCount);
        System.out.println("Successfully processed:  " + successCount);
        System.out.println("Failure in processing:   " + (totalCount - successCount));
        System.exit(0);
    }
    
    private static SmsEdrPersistenceHelper getWorker() {
        System.out.println("workIndex: " + workerIndex);
        if (workerIndex == workers.size())
            workerIndex = 0;
         return workers.get(workerIndex++);
    }
}
