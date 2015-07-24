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
        try {
            FileReader fileInput = new FileReader(csvPath);
            BufferedReader textReader = new BufferedReader(fileInput);
            
            TransactionDao txnHelper = new TransactionDao();
            while(true) {
                recordEntry = textReader.readLine();
                if (recordEntry == null) {
                    System.out.println("No more entries... exiting..");
                    break;
                }
                
                // Handle record
                Transaction txn = new Transaction();
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
                System.out.println("From file messageid:"+fields[3]+":SourceAddress:"+fields[0]+":DestinationAddress:"+fields[1]);
                txn = new TransactionDao().fetchSmsCharging(fields[3],fields[0],fields[1]);
                
                if(txn != null){
                	System.out.println("Record found for messageId:"+txn.getMessageId());
	                txn.setSourceAddress(fields[0]);
	                txn.setDestinationAddress(fields[1]);
	                txn.setChargedParty(fields[2]);
	                txn.setMessageId(fields[3]);
	                txn.setChargingSessionId(fields[5]); 
	                
	                System.out.println("Fields Length:"+fields.length);
	                
	                if(fields.length > 6){
	                	String accounts = "";
	                    String amounts = "";
	                    String accountTypes = "";
	                	String[] daList = fields[6].split(SEMI_COLON);//split with semi colon
	                	
	                	for(int i=0; i< daList.length; i++){
	                		System.out.println("DA:"+daList[i]);
	                        String[] accountInfo = daList[i].split(ACCT_DELIM);
	                        if (accounts.length() > 0) { accounts += PIPE; }
	                        accounts += accountInfo[0];
	                        if (amounts.length() > 0) { amounts += PIPE; }
	                        amounts += accountInfo[1];
	                        if (accountTypes.length() > 0) { accountTypes += PIPE; }
	                        accountTypes += accountInfo[2];
	                	}
	                	txn.setAccountId(accounts);
	                	txn.setAmount(amounts);
	                	txn.setAccountType(accountTypes);
	                	
	                	System.out.println("Updating sms charging for transaction time:"+txn.getTransactionTime() + ":transactionId:"+txn.getTransactionId());
	                    txnHelper.updateSmsCharging(txn);
	                    System.out.println("update successful");
	                } else {
	                    //TODO: delete the transaction...
	                	System.out.println("Deleting record");
	                    txn.setChargeStatus(true);
	                    txnHelper.deleteSmsCharging(txn);
	                }
                }else{
                	System.out.println("No record exists with messageid:"+fields[3]+":SourceAddress:"+fields[0]+":DestinationAddress:"+fields[1]);
                }
            }
            new File(csvPath + ".success").createNewFile();
            System.out.println("Successfully processed the file: " + csvPath);            
            System.exit(0);
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found: " + e.getMessage());
            try {
                new File(csvPath + ".failed").createNewFile();
            } catch (IOException e1) {
                System.out.println("Unable to create Failed File");
            }
        } catch (IOException e) {
            System.out.println("File IO Access Failed: " + e.getMessage());
            try {
                new File(csvPath + ".failed").createNewFile();
            } catch (IOException e1) {
                System.out.println("Unable to create Failed File");
            }
        } catch (SecurityException e) {
            System.out.println("File Access Privilege Failed: " + e.getMessage());
            try {
                new File(csvPath + ".failed").createNewFile();
            } catch (IOException e1) {
                System.out.println("Unable to create Failed File");
            }
        } catch (PersistenceException e) {
            System.out.println("Processing stopped at entry: " + recordEntry);
            e.printStackTrace();
        }
        
    }
    
}
