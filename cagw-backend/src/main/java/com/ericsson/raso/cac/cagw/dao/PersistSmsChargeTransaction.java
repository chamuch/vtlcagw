package com.ericsson.raso.cac.cagw.dao;

import java.util.concurrent.Callable;

import com.satnar.common.LogService;

public class PersistSmsChargeTransaction implements Callable<Void> {
    
    private Transaction transactionData = null;

    public PersistSmsChargeTransaction(Transaction transaction) {
        this.transactionData  = transaction;
    }

    @Override
    public Void call() {
        try {
            TransactionDao txnPersistenceHelper = new TransactionDao();
            txnPersistenceHelper.persistSmsCharging(this.transactionData);
            LogService.appLog.info("PersistSmsChargeTransaction(" + this.transactionData + "): Success!!");
        } catch (PersistenceException e) {
        	LogService.appLog.error("PersistSmsChargeTransaction(" + this.transactionData + "): Persistence Failure!!",e);
        }
        return null;
    }
    
}
