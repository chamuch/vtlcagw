package com.ericsson.raso.cac.cagw.dao;

import java.util.List;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import com.satnar.common.LogService;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;

public class TransactionDao {
    
    private static final String TRANSACTION_TABLE = "transaction";
    
    private DataStaxConnection connection = null;
	private Cluster cluster = null;
	private Session session = null;
	
	public TransactionDao() {
	    connection = DataStaxConnection.getInstance();
		cluster = DataStaxConnection.getCluster();
		session = DataStaxConnection.getSession();
	}
	
	public void persistSmsCharging(Transaction txnInfo) throws PersistenceException {
		try {
			if(cluster != null && session != null) {
			    
			    Insert insert = QueryBuilder.insertInto(connection.getKeyspace(), TRANSACTION_TABLE)
			                                .value("transactionTime", txnInfo.getTransactionTime())
			                                .value("transactionId", txnInfo.getTransactionId())
			                                .value("messageId", txnInfo.getMessageId())
			                                .value("chargingSessionId", txnInfo.getChargingSessionId())
                                            .value("sourceAddress", txnInfo.getSourceAddress())
                                            .value("destinationAddress", txnInfo.getDestinationAddress())
                                            .value("chargedParty", txnInfo.getChargedParty())
                                            .value("chargeStatus", txnInfo.isChargeStatus());
                                            
				session.execute(insert);
				LogService.stackTraceLog.info("TransactionDao-persistSmsCharging:Success !!");
			}else{
				LogService.stackTraceLog.info("TransactionDao-persistSmsCharging:Unable to get connection to insert txn");				
			}			
		} catch (Exception e) {
			LogService.stackTraceLog.debug("TransactionDao-persistSmsCharging:Encountered exception.",e);
			throw new PersistenceException("Unable to insert transaction data!!", e);
		}		
	}
	
	public void updateSmsCharging (Transaction txnInfo) throws PersistenceException {
	    try {
            if(cluster != null && session != null) {
                
                Update update = QueryBuilder.update(connection.getKeyspace(), TRANSACTION_TABLE);
                                      update.with(set("accountId", txnInfo.getAccountId()));
                                      update.with(set("amount", txnInfo.getAmount()));
                                      update.with(set("accountType", txnInfo.getAccountType()));
                                      update.where(eq("messageId", txnInfo.getMessageId()))
                                            .and(eq("chargingSessionId", txnInfo.getChargingSessionId()))
                                            .and(eq("sourceAddress", txnInfo.getSourceAddress()))
                                            .and(eq("chargedParty", txnInfo.getChargedParty()));
                session.execute(update);
                LogService.stackTraceLog.info("TransactionDao-updateSmsCharging:Success !!");
            }else{
            	LogService.stackTraceLog.info("TransactionDao-updateSmsCharging:Unable to get connection to insert txn");	
            }           
        } catch (Exception e) {
        	LogService.stackTraceLog.debug("TransactionDao-updateSmsCharging:Encountered exception.",e);
            throw new PersistenceException("Unable to update transaction data!!", e);
        }   
	}
	
	public void deleteSmsCharging (Transaction txnInfo) throws PersistenceException {
        try {
            if(cluster != null && session != null) {
                Delete delete = QueryBuilder.delete().from(connection.getKeyspace(), TRANSACTION_TABLE);
                                             delete.where((eq("messageId", txnInfo.getMessageId())))
                                                    .and(eq("chargingSessionId", txnInfo.getChargingSessionId()))
                                                    .and(eq("chargedParty", txnInfo.getChargedParty()));
                
                session.execute(delete);
                
                LogService.appLog.info("TransactionDao-deleteSmsCharging:Success !!");
            }else{                
                LogService.appLog.info("TransactionDao-deleteSmsCharging:Unable to get connection to insert txn");	
            }           
        } catch (Exception e) {
        	LogService.stackTraceLog.debug("TransactionDao-deleteSmsCharging:Encountered exception.",e);
            throw new PersistenceException("Unable to delete transaction data!!", e);
        }   
    }
	
	public Transaction fetchSmsCharging (String messageId, String sourceAddress, String destinationAddress) throws PersistenceException {
        try {
            if(cluster != null && session != null) {
                Select select = QueryBuilder.select().all().from(connection.getKeyspace(), TRANSACTION_TABLE);
                                                select.where((eq("messageId", messageId)))
                                                .and(eq("sourceAddress", sourceAddress))
                                                .and(eq("destinationAddress", destinationAddress));
                                                select.allowFiltering();

                List<Row> results = session.execute(select).all();
                LogService.stackTraceLog.info("TransactionDao-deleteSmsCharging:Success !! Query Returned: " + results.size());
                if (results.size() == 0)
                    return null;
                
                
                Row firstMatch = results.get(0);
                
                Transaction tx = new Transaction();
                tx.setTransactionTime(firstMatch.getLong("transactionTime"));
                tx.setTransactionId(firstMatch.getInt("transactionId"));
                tx.setMessageId(firstMatch.getString("messageId"));
                tx.setChargingSessionId(firstMatch.getString("chargingSessionId"));
                tx.setSourceAddress(firstMatch.getString("sourceAddress"));
                tx.setDestinationAddress(firstMatch.getString("destinationAddress"));
                tx.setChargedParty(firstMatch.getString("chargedParty"));
                tx.setAccountId(firstMatch.getString("accountId"));
                tx.setAmount(firstMatch.getString("amount"));
                tx.setAccountType(firstMatch.getString("accountType"));
                tx.setChargeStatus(firstMatch.getBool("chargeStatus"));
                
                return tx;
            }else{                
                LogService.stackTraceLog.info("TransactionDao-fetchSmsCharging:Unable to get connection to insert txn");
                throw new PersistenceException("No connection to fetch transaction data!!");
            }           
        } catch (Exception e) {
        	LogService.stackTraceLog.debug("TransactionDao-fetchSmsCharging:Encountered exception.",e);
            throw new PersistenceException("Failed to fetch transaction data!!", e);
        }   
    }
    
	

}
