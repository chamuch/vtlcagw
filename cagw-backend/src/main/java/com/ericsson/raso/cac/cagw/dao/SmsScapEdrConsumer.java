package com.ericsson.raso.cac.cagw.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SmsScapEdrConsumer {
    
    private static final String COMMA = ",";
    private static final String PIPE = "|";
    private static final String ACCT_DELIM = "/";
    private static final String SEMI_COLON = ";";
    private static final String DA_PREFIX = "DA";
    
    public static void main(String[] args) {
        if (args.length < 3) {
           System.out.println("Usage: java SmsScapEdrConsumer <fully_qualified_path_to_input_file> <cassandra_ip_address_csv_list> <keyspace_name>");        	
        }
        
        processInputFile(args[0], args[1], args[2]);
    }

    private static void processInputFile(String csvPath, String cassandraIp, String keyspace) {
        
        DataStaxConnection.getInstance(cassandraIp, keyspace);
        System.out.println("Cassandra Connected with: " + cassandraIp);
        
        String recordEntry = null;
        FileReader fileInput = null;
        BufferedReader textReader = null;
        Transaction txn = null;
        //int totalCount = 0;
        int successCount = 0;
        //int failureCount = 0;
        
        try {
        	System.out.println("Processing started for file:"+csvPath);
            fileInput = new FileReader(csvPath);
            textReader = new BufferedReader(fileInput);            
            TransactionDao txnHelper = new TransactionDao();
            
            while(true) {            	
                recordEntry = textReader.readLine();
                if (recordEntry == null) {
                    System.out.println("No more entries... exiting..from:"+csvPath);
                    break;
                }
                
                // Handle record
                //totalCount = totalCount + 1;
                txn = new Transaction();
                String[] fields = recordEntry.split(COMMA);
                /*txn.setChargedParty(fields[0]);
                txn.setMessageId(fields[1]);
                txn.setChargingSessionId(fields[2]);
                txn.setSourceAddress(fields[3]);
                
                if (fields.length > 4) {
                    String accounts = "";
                    String amounts = "";
                    String accountTypes = "";
                    for (int i = 4; i < fields.length; i++) {
                        String[] accountInfo = fields[i].split(ACCT_DELIM);
                        if (accounts.length() > 0) { accounts += PIPE; }
                        accounts += accountInfo[0];
                        if (amounts.length() > 0) { amounts += PIPE; }
                        amounts += accountInfo[0];
                        if (accountTypes.length() > 0) { accountTypes += PIPE; }
                        accountTypes += accountInfo[0];
                    }
                    txn.setAccountId(accounts);
                    txn.setAmount(amounts);
                    txn.setAccountType(accountTypes);
                    txn.setChargeStatus(true);*/
                if(fields != null && fields.length < 6){
                	System.out.println("Insufficient fields or incorrect delimiters - processing stopped at:"+recordEntry);
                	
                	try {
                        new File(csvPath + ".failed").createNewFile();
                    } catch (IOException e1) {
                        System.out.println("Unable to create Failed File for:"+csvPath);
                    }
                }else{
	                txn = new TransactionDao().fetchSmsCharging(fields[3],fields[0],fields[1]);
	                
	                if(txn != null){
	                	System.out.println("Record found for messageId:"+txn.getMessageId());
		                txn.setSourceAddress(fields[0]);	                
		                txn.setDestinationAddress(fields[1]);
		                txn.setChargedParty(fields[2]);
		                txn.setMessageId(fields[3]);
		                txn.setChargingSessionId(fields[5]); 
		                
		                //System.out.println("Fields Length:"+fields.length);
		                
		                if(fields.length > 6){
		                	String accounts = "";
		                    String amounts = "";
		                    String accountTypes = "";
		                	String[] daList = fields[6].split(SEMI_COLON);//split with semi colon
		                	
		                	for(int i=0; i< daList.length; i++){
		                        String[] accountInfo = daList[i].split(ACCT_DELIM);
		                        if (accounts.length() > 0) { accounts += PIPE; }
		                        
		                        //25-JUL-2015: To remove prefix "DA"
		                        if(accountInfo[0].matches("^[DA_PREFIX].*")){
		                        	accounts += accountInfo[0].replace(DA_PREFIX, "");
		                        }else{
		                        	accounts += accountInfo[0];
		                        }
		                        
		                        if (amounts.length() > 0) { amounts += PIPE; }
		                        
		                        //25-JUL-2015: As per confirmation from Tanzeem amount to be divided by 1000000
		                        //and should be multiplied by 100 when sending to AIR - so dividing by 10000
		                        amounts += Long.valueOf(accountInfo[1])/10000;
		                        
		                        if (accountTypes.length() > 0) { accountTypes += PIPE; }
		                        accountTypes += accountInfo[2];
		                	}
		                	txn.setAccountId(accounts);
		                	txn.setAmount(amounts);
		                	txn.setAccountType(accountTypes);
		                	txn.setChargeStatus(true);	                	
		                    txnHelper.updateSmsCharging(txn);
		                    System.out.println("Updating sms charging for messageId:"+txn.getMessageId()+":transaction time:"+txn.getTransactionTime() + ":transactionId:"+txn.getTransactionId());
		                } else {
		                    txn.setChargeStatus(true);
		                    txnHelper.deleteSmsCharging(txn);
		                    System.out.println("Updating sms charging for messageId:"+txn.getMessageId()+":transaction time:"+txn.getTransactionTime() + ":transactionId:"+txn.getTransactionId());
		                }
	                }else{
	                	System.out.println("No record exists with messageid:"+fields[3]+":SourceAddress:"+fields[0]+":DestinationAddress:"+fields[1]);
	                }
	                new File(csvPath + ".success").createNewFile();
		            successCount = successCount + 1;
		            System.out.println("Successfully processed the file: " + csvPath); 
	            }	            
            }
        } catch (FileNotFoundException e) {        	
            System.out.println("File Not Found: "+csvPath +":" + e.getMessage());
            try {
                new File(csvPath + ".failed").createNewFile();
            } catch (IOException e1) {
                System.out.println("Unable to create Failed File for:"+csvPath);
            }
        } catch (IOException e) {
            System.out.println("File IO Access Failed for: "+csvPath + e.getMessage());
            try {
                new File(csvPath + ".failed").createNewFile();
            } catch (IOException e1) {
            	System.out.println("Unable to create Failed File for:"+csvPath);
            }
        } catch (SecurityException e) {
            System.out.println("File Access Privilege Failed for: "+csvPath + e.getMessage());
            try {
                new File(csvPath + ".failed").createNewFile();
            } catch (IOException e1) {
            	System.out.println("Unable to create Failed File for:"+csvPath);
            }
        } catch (PersistenceException e) {
            System.out.println("Processing stopped at entry: " + recordEntry);
            //e.printStackTrace();
            try {
                new File(csvPath + ".failed").createNewFile();
            } catch (IOException e1) {
            	System.out.println("Unable to create Failed File for:"+csvPath);
            }
        } catch(Exception genE){
        	System.out.println("Encountered exception while processing file: " + genE.getMessage());
        	try {
                new File(csvPath + ".failed").createNewFile();
            } catch (IOException e1) {
            	System.out.println("Unable to create Failed File for:"+csvPath);
            }
        }finally{
        	System.out.println("Success Record Count: " + successCount); 
        	fileInput = null;
        	textReader = null;
        	txn = null;
            System.exit(0);
        }
    }
    
}
