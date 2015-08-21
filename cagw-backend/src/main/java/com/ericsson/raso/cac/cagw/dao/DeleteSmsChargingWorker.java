package com.ericsson.raso.cac.cagw.dao;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.set;

import java.util.concurrent.Callable;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Update;

public class DeleteSmsChargingWorker implements Callable<Boolean> {
    
    private static final String TRANSACTION_TABLE = "transaction";
    
    private Session session = null;
    private String keyspace = null;
    private Transaction transaction = null;
    
    public DeleteSmsChargingWorker(Session session, String keyspace, Transaction transaction) {
        this.session = session;
        this.keyspace = keyspace;
        this.transaction = transaction;
    }

    @Override
    public Boolean call() throws Exception {
        try {
            Delete delete = QueryBuilder.delete().from(this.keyspace, TRANSACTION_TABLE);
            delete.where(eq("transactionTime", this.transaction.getTransactionTime()))
                   .and((eq("transactionId", this.transaction.getTransactionId())));

            session.execute(delete);
            return true;
        } catch (Exception e) {
            System.out.println("Unable to async delete SMS Transaction with error: " + e.getMessage());
            return false;
        }
        
    }
    
    
    
}
