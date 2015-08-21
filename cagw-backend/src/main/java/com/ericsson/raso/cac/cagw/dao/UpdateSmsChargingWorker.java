package com.ericsson.raso.cac.cagw.dao;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.set;

import java.util.concurrent.Callable;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Update;

public class UpdateSmsChargingWorker implements Callable<Boolean> {
    
    private static final String TRANSACTION_TABLE = "transaction";
    
    private Session session = null;
    private String keyspace = null;
    private Transaction transaction = null;
    
    public UpdateSmsChargingWorker(Session session, String keyspace, Transaction transaction) {
        this.session = session;
        this.keyspace = keyspace;
        this.transaction = transaction;
    }

    @Override
    public Boolean call() throws Exception {
        try {
            Update update = QueryBuilder.update(this.keyspace, TRANSACTION_TABLE);
            update.with(set("accountId", this.transaction.getAccountId()));
            update.with(set("amount", this.transaction.getAmount()));
            update.with(set("accountType", this.transaction.getAccountType()));
            update.where(eq("transactionTime", this.transaction.getTransactionTime()))
                    .and((eq("transactionId", this.transaction.getTransactionId())));
            
            session.execute(update);
            return true;
        } catch (Exception e) {
            System.out.println("Unable to async update SMS Transaction with error: " + e.getMessage());
            return false;
        }
        
    }
    
    
    
}
