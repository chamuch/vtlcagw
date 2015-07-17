package com.ericsson.raso.cac.cagw.dao;

import java.util.concurrent.Callable;

import com.satnar.common.LogService;

public class MoveTransactionToArchive implements Callable<Void> {
    
    private Transaction transactionData = null;
    private Archive archiveData = null;

    public MoveTransactionToArchive(Transaction transaction, Archive archive) {
        this.transactionData  = transaction;
    }

    @Override
    public Void call() {
        try {
            ArchiveDao archivePersistenceHelper = new ArchiveDao();
            archivePersistenceHelper.pushToArchive(archiveData);
            
            TransactionDao txnPersistenceHelper = new TransactionDao();
            txnPersistenceHelper.deleteSmsCharging(this.transactionData);
            //TODO: Log success
            LogService.appLog.info("MoveTransactionToArchive: Success!!");
        } catch (PersistenceException e) {
            // TODO Just log this... nothing can be done in a zombie thread ;)
        	LogService.appLog.debug("MoveTransactionToArchive: Encountered exception.",e);
        }
        return null;
    }
    
}
