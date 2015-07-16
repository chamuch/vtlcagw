package com.ericsson.raso.cac.cagw.dao;

import com.satnar.common.LogService;

public class PersistSmsChargeTransaction implements Runnable {
    
    private Transaction transactionData = null;

    public PersistSmsChargeTransaction(Transaction transaction) {
        this.transactionData  = transaction;
    }

    @Override
    public void run() {
        try {
            TransactionDao txnPersistenceHelper = new TransactionDao();
            txnPersistenceHelper.persistSmsCharging(this.transactionData);
            //TODO: Log success
            LogService.appLog.info("PersistSmsChargeTransaction(" + this.transactionData + "): Success!!");
        } catch (PersistenceException e) {
            // TODO Just log this... nothing can be done in a zombie thread ;)
        	LogService.appLog.error("PersistSmsChargeTransaction(" + this.transactionData + "): Persistence Failure!!",e);
        }
    }
    
}
