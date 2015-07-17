package com.ericsson.raso.cac.cagw.dao;

import java.util.concurrent.Callable;

import com.satnar.common.LogService;

public class RemoveSmsChargeTransaction implements Callable<Void> {
    
    private Transaction transactionData = null;

    public RemoveSmsChargeTransaction(Transaction transaction) {
        this.transactionData  = transaction;
    }

    @Override
    public Void call() {
        try {
            TransactionDao txnPersistenceHelper = new TransactionDao();
            txnPersistenceHelper.deleteSmsCharging(this.transactionData);
            //TODO: Log success
            LogService.appLog.info("RemoveSmsChargeTransaction: Success!!");
        } catch (PersistenceException e) {
            // TODO Just log this... nothing can be done in a zombie thread ;)
        	LogService.appLog.debug("RemoveSmsChargeTransaction: Encountered exception.",e);
        }
        return null;
    }
    
}
