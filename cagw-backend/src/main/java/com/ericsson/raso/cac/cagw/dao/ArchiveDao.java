/**
 * 
 */
package com.ericsson.raso.cac.cagw.dao;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.satnar.common.LogService;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;

public class ArchiveDao  {
    
    private static final int ARCHIVE_EXPIRY = (30 * 24 * 60 * 60);
	private static final String ARCHIVE_TABLE = "archive";
	
    private DataStaxConnection connection = null;
    private static Cluster cluster = null;
	private static Session session = null;
	
	public ArchiveDao() {
	    connection = DataStaxConnection.getInstance();
		cluster = DataStaxConnection.getCluster();
		session = DataStaxConnection.getSession();
	}
	
	public void pushToArchive(Archive archiveInfo) throws PersistenceException {
		try {
			if(cluster != null && session != null) {
			    Insert insert = QueryBuilder.insertInto(connection.getKeyspace(), ARCHIVE_TABLE)
			                                .value("transactionTime", archiveInfo.getTransactionTime())
			                                .value("transactionId", archiveInfo.getTransactionId())
			                                .value("messageId", archiveInfo.getMessageId())
			                                .value("chargingSessionId", archiveInfo.getChargingSessionId())
                                            .value("sourceAddress", archiveInfo.getSourceAddress())
                                            .value("destinationAddress", archiveInfo.getDestinationAddress())
                                            .value("chargedParty", archiveInfo.getChargedParty())
                                            .value("accountId", archiveInfo.getAccountId())
                                            .value("amount", archiveInfo.getAmount())
                                            .value("accountType", archiveInfo.getAccountType())
                                            .value("chargeStatus", archiveInfo.isChargeStatus())
                                            .value("deliveryStatus", archiveInfo.isDeliveryStatus())
                                            .value("refundStatus", archiveInfo.isRefundStatus())
                                            .value("refundTime", archiveInfo.getRefundTime());
			    session.execute(insert);
			    //System.out.println("Inserted Archive..");
			    LogService.appLog.info("Inserted into archive:MessageId:"+archiveInfo.getMessageId());
			}else{
				//System.out.println("Unable to get connection to insert archive!!");
				LogService.appLog.info("Unable to get connection to insert archive!!");
			}			
		} catch (Exception e) {
			LogService.appLog.debug("Unable to insert archive data!!",e);
            throw new PersistenceException("Unable to insert archive data!!", e);
		}		
	}
	
	public void removeArchive(Archive archiveInfo) throws PersistenceException {
        PreparedStatement insertPstmt = null;
        try {
            if(cluster != null && session != null) {
                Delete delete = QueryBuilder.delete().from(connection.getKeyspace(), ARCHIVE_TABLE);
                                        delete.where(eq("messageId", archiveInfo.getMessageId()))
                                            .and(eq("chargingSessionId", archiveInfo.getChargingSessionId()))
                                            .and(eq("sourceAddress", archiveInfo.getSourceAddress()))
                                            .and(eq("destinationAddress", archiveInfo.getDestinationAddress()))
                                            .and(eq("chargedParty", archiveInfo.getChargedParty()));
                session.execute(delete);
                //System.out.println("Deleted Archive..");
                LogService.appLog.info("Deleted from archive:MessageId:"+archiveInfo.getMessageId());
            }else{
                //System.out.println("Unable to get connection to delete archive!!");
            	LogService.appLog.info("Unable to get connection to delete archive!!");
            }           
        } catch (Exception e) {
        	LogService.appLog.debug("Unable to delete archive data!!",e);
            throw new PersistenceException("Unable to delete archive data!!", e);
        }       
    }
    
    

}
