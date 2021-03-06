package com.ericsson.raso.cac.cagw.dao;

import java.util.concurrent.Callable;

import com.satnar.common.LogService;

public class MoveTransactionToArchive implements Callable<Void> {
    
    private Transaction transactionData = null;
    private Archive archiveData = null;

    public MoveTransactionToArchive(Transaction transaction, Archive archive) {
        this.transactionData  = transaction;
        this.archiveData = archive;
    }

    @Override
    public Void call() {
        try {
            ArchiveDao archivePersistenceHelper = new ArchiveDao();
            archivePersistenceHelper.pushToArchive(archiveData);
            
            TransactionDao txnPersistenceHelper = new TransactionDao();
            txnPersistenceHelper.deleteSmsCharging(this.transactionData);
            LogService.appLog.info("MoveTransactionToArchive: Success!!");
        } catch (PersistenceException e) {
        	LogService.appLog.debug("MoveTransactionToArchive: Encountered exception.",e);
        }
        return null;
    }
    
}
