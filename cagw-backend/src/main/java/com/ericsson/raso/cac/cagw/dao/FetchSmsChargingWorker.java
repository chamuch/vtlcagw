package com.ericsson.raso.cac.cagw.dao;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

import java.util.List;
import java.util.concurrent.Callable;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;

public class FetchSmsChargingWorker implements Callable<Transaction> {
    
    private static final String TRANSACTION_TABLE = "transaction";
    private static final String PIPE = "|";
    private static final String ACCT_DELIM = "/";
    private static final String SEMI_COLON = ";";
    private static final String DA_PREFIX = "DA";
    
    private Session session = null;
    private String keyspace = null;
    private String[] fields = null;
    
    public FetchSmsChargingWorker(Session session, String keyspace, String[] fields) {
        this.session = session;
        this.keyspace = keyspace;
        this.fields = fields;
    }

    @Override
    public Transaction call() throws Exception {
        try {
            Select select = QueryBuilder.select().all().from(this.keyspace, TRANSACTION_TABLE);
            select.where((eq("messageId", fields[3])))
                        .and(eq("sourceAddress", fields[0]))
                        .and(eq("destinationAddress", fields[1]));
            select.allowFiltering();
            
            List<Row> results = session.execute(select).all();
            if (results.size() == 0)
                return null;
            
            Row firstMatch = results.get(0);
            
            Transaction transaction = new Transaction();
            transaction.setTransactionTime(firstMatch.getLong("transactionTime"));
            transaction.setTransactionId(firstMatch.getInt("transactionId"));
            transaction.setChargedParty(firstMatch.getString("chargedParty"));
            transaction.setAccountId(firstMatch.getString("accountId"));
            transaction.setAmount(firstMatch.getString("amount"));
            transaction.setAccountType(firstMatch.getString("accountType"));
            transaction.setChargeStatus(firstMatch.getBool("chargeStatus"));
            
            transaction.setSourceAddress(fields[0]);                    
            transaction.setDestinationAddress(fields[1]);
            transaction.setChargedParty(fields[2]);
            transaction.setMessageId(fields[3]);
            transaction.setChargingSessionId(fields[5]);
            
            if (fields.length > 6) {
                String accounts = ""; String amounts = ""; String accountTypes = "";
                String[] daList = fields[6].split(SEMI_COLON);//split with semi colon
                
                for(int i=0; i< daList.length; i++){
                    String[] accountInfo = daList[i].split(ACCT_DELIM);
                    if (accounts.length() > 0) { accounts += PIPE; }
                    
                    //25-JUL-2015: To remove prefix "DA"
                    if(accountInfo[0].matches("^" + DA_PREFIX + ".*")){
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
                transaction.setAccountId(accounts);
                transaction.setAmount(amounts);
                transaction.setAccountType(accountTypes);
                transaction.setChargeStatus(true);
            }
            
            return transaction;
        } catch (Exception e) {
            System.out.println("Unable to async fetch SMS Transaction with error: " + e.getMessage());
            return null;
        }
        
    }
    
    
    
}
