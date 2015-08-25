package com.satnar.smpp.pdu;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.ProducerTemplate;

import com.ericsson.raso.cac.smpp.pdu.viettel.AuthAcc;
import com.ericsson.raso.cac.smpp.pdu.viettel.AuthAccResponse;
import com.ericsson.raso.cac.smpp.pdu.viettel.SmResultNotify;
import com.ericsson.raso.cac.smpp.pdu.viettel.SmResultNotifyResponse;
import com.satnar.common.LogService;
import com.satnar.common.alarmlog.AlarmCode;
import com.satnar.smpp.CommandId;
import com.satnar.smpp.CommandSequence;
import com.satnar.smpp.CommandStatus;
import com.satnar.smpp.SpringHelper;
import com.satnar.smpp.StackMap;
import com.satnar.smpp.client.ChannelMode;
import com.satnar.smpp.client.Esme;
import com.satnar.smpp.client.EsmeHelper;
import com.satnar.smpp.codec.SmppCodecException;
 
public class ParsingDelegate implements Runnable {
    
    private static final String     processingEndpoint = "seda:cagw-backend";
    
    private static ProducerTemplate producerTemplate = null;
    
    private byte[] raw = null;
    private String esmeLabel = null;
    private ChannelMode channelMode = null;
    private long creationTime = System.currentTimeMillis();
    private long executionStartTime = 0;
    
    public ParsingDelegate(CamelContext context) {
        producerTemplate = context.createProducerTemplate();
    }
    
    public ParsingDelegate(byte[] serialized, String esmeLabel, ChannelMode channelMode) {
        this.raw = serialized;
        this.esmeLabel = esmeLabel;
        this.channelMode = channelMode;
    }

    //public Void call() throws Exception {
    public void run() {
        try {
            executionStartTime = System.currentTimeMillis();
            LogService.appLog.debug("ParsingDeligate-call:Entered..");
            ByteArrayInputStream rawStream = new ByteArrayInputStream(raw);
            DataInputStream parser = new DataInputStream(rawStream);
            
            SmppPdu pdu = null;
            int commandId = parser.readInt();
            CommandId pduName = CommandId.valueOf(commandId);
            byte[] rawPdu = new byte[parser.available()];
            parser.read(rawPdu);
            parser.close();
            rawStream.close();
            parser = null;
            rawStream = null;
            
            LogService.appLog.debug("ParsingDeligate-call: Entering into handlers. pudName:"+pduName);
            
            if (pduName != null) {
                switch (pduName) {
                    case GENERIC_NACK:
                        LogService.appLog.debug(String.format("Session: %s - GNACK Received and delegated", this.esmeLabel));
                        EsmeHelper.handleGNack(rawPdu);
                        this.printStats(System.currentTimeMillis(), esmeLabel, pduName);
                        //return null;
                        return;
                    case BIND_RECEIVER_RESP:
                        LogService.appLog.debug(String.format("Session: %s - BIND_RECEIVER_RESP Received and delegated", this.esmeLabel));
                        LogService.stackTraceLog.info(String.format("Session: %s - BIND_RECEIVER_RESP Received, payload: %s", this.esmeLabel, EsmeHelper.prettyPrint(rawPdu)));
                        EsmeHelper.handleBindReceiverResponse(rawPdu, esmeLabel);
                        this.printStats(System.currentTimeMillis(), esmeLabel, pduName);
                        //return null;
                        return;
                    case BIND_TRANSCEIVER_RESP:
                        LogService.appLog.debug(String.format("Session: %s - BIND_TRANSCEIVER_RESP Received and delegated", this.esmeLabel));
                        EsmeHelper.handleBindTransceiverResponse(rawPdu);
                        this.printStats(System.currentTimeMillis(), esmeLabel, pduName);
                        //return null;
                        return;
                    case BIND_TRANSMITTER_RESP:
                        LogService.appLog.debug(String.format("Session: %s - BIND_TRANSMITTER_RESP Received and delegated", this.esmeLabel));
                        EsmeHelper.handleBindTransmitterResponse(rawPdu);
                        this.printStats(System.currentTimeMillis(), esmeLabel, pduName);
                        //return null;
                        return;
                    case DELIVER_SM:
                        LogService.appLog.debug(String.format("Session: %s - DELIVER_SM Received and delegated", this.esmeLabel));
                        if (com.satnar.common.SpringHelper.getTraffiControl().authorizeIngress()) {
                            EsmeHelper.handleDeliverSmRequest(rawPdu);
                            com.satnar.common.SpringHelper.getTraffiControl().updateExgress();
                        } else
                            EsmeHelper.sendDeliverSmThrottled(rawPdu);
                        this.printStats(System.currentTimeMillis(), esmeLabel, pduName);
                        //return null;
                        return;
                    case ENQUIRE_LINK:
                        LogService.appLog.debug(String.format("Session: %s - ENQUIRE_LINK Received and delegated", this.esmeLabel));
                        EsmeHelper.handleEnquireLinkRequest(rawPdu, this.esmeLabel, this.channelMode);
                        this.printStats(System.currentTimeMillis(), esmeLabel, pduName);
                        //return null;
                        return;
                    case ENQUIRE_LINK_RESP:
                        LogService.appLog.debug(String.format("Session: %s - ENQUIRE_LINK_RESP Received and delegated", this.esmeLabel));
                        EsmeHelper.handleEnquireLinkResponse(rawPdu);
                        this.printStats(System.currentTimeMillis(), esmeLabel, pduName);
                        //return null;
                        return;
                    default:
                        LogService.appLog.debug(String.format("Session: %s - UNKNOWN/EXTENDED Received and delegated", this.esmeLabel));
                        pdu = SpringHelper.getSmppPduImplementation(Integer.toHexString(commandId));
                        pdu.decode(rawPdu);
                        
                        // this is where we throw the event to backend for processing...
                        LogService.appLog.debug(String.format("Session: %s - Sending request to cagw-backend!!:CommandId: %s, Sequence: %s", this.esmeLabel, pdu.getCommandId(), 
                                pdu.getCommandSequence().getValue()));
                        SmppPdu response = null;
                        if (com.satnar.common.SpringHelper.getTraffiControl().authorizeIngress()){
                            long backendDelegationTime = System.currentTimeMillis();
                            LogService.alarm(AlarmCode.SMS_THROTTLE_ABATE, this.esmeLabel);
                            LogService.appLog.info(String.format("Session: %s - Watergate approved ingress!!:CommandId: %s, Sequence: %s", this.esmeLabel, pdu.getCommandId(), 
                                    pdu.getCommandSequence().getValue()));
                            if (pdu.getCommandId().getId() == CommandId.AUTH_ACC.getId()) {
                                try {
                                    LogService.stackTraceLog.info(String.format("Session: %s - BACKEND REQ>> %s", this.esmeLabel, pdu));
                                    response = producerTemplate.requestBodyAndHeader(processingEndpoint, (AuthAcc)pdu, "fe", "auth_acc", AuthAccResponse.class);
                                    LogService.stackTraceLog.info(String.format("Session: %s - BACKEND RES>> %s", this.esmeLabel, response));
                                } catch (CamelExecutionException e) {
                                    LogService.appLog.error("CAMEL Execution Failure for AUTH_ACC.", e);
                                    response = ((AuthAcc)pdu).getFailedResponse(CommandStatus.ESME_RUNKNOWNERR);
                                    LogService.stackTraceLog.info(String.format("Session: %s - BACKEND RES>> %s", this.esmeLabel, response));
                                } catch (Exception e) {
                                    LogService.appLog.error("CatchAll Execution Failure for AUTH_ACC.", e);
                                    response = ((AuthAcc)pdu).getFailedResponse(CommandStatus.ESME_RUNKNOWNERR);
                                    LogService.stackTraceLog.info(String.format("Session: %s - BACKEND RES>> %s", this.esmeLabel, response));
                                } catch (Error e) {
                                    LogService.appLog.error("Runtime Error Failure for AUTH_ACC.", e);
                                    response = ((AuthAcc)pdu).getFailedResponse(CommandStatus.ESME_RUNKNOWNERR);
                                    LogService.stackTraceLog.info(String.format("Session: %s - BACKEND RES>> %s", this.esmeLabel, response));
                                }
                            } else if (pdu.getCommandId().getId() == CommandId.SM_RESULT_NOTIFY.getId()) {
                                try {
                                    LogService.stackTraceLog.info(String.format("Session: %s - BACKEND REQ>> %s", this.esmeLabel, pdu));
                                    response = producerTemplate.requestBodyAndHeader(processingEndpoint, (SmResultNotify)pdu, "fe", "sm_result", SmResultNotifyResponse.class);
                                    LogService.stackTraceLog.info(String.format("Session: %s - BACKEND RES>> %s", this.esmeLabel, response));
                                } catch (CamelExecutionException e) {
                                    LogService.appLog.error("CAMEL Execution Failure for SM_RESULT_NOTIFY.", e);
                                    response = ((SmResultNotify)pdu).getFailedResponse(CommandStatus.ESME_RUNKNOWNERR);
                                    LogService.stackTraceLog.info(String.format("Session: %s - BACKEND RES>> %s", this.esmeLabel, response));
                                } catch (Exception e) {
                                    LogService.appLog.error("CatchAll Execution Failure for AUTH_ACC.", e);
                                    response = ((SmResultNotify)pdu).getFailedResponse(CommandStatus.ESME_RUNKNOWNERR);
                                    LogService.stackTraceLog.info(String.format("Session: %s - BACKEND RES>> %s", this.esmeLabel, response));
                                } catch (Error e) {
                                    LogService.appLog.error("Runtime Error Failure for AUTH_ACC.", e);
                                    response = ((SmResultNotify)pdu).getFailedResponse(CommandStatus.ESME_RUNKNOWNERR);
                                    LogService.stackTraceLog.info(String.format("Session: %s - BACKEND RES>> %s", this.esmeLabel, response));
                                }
                            } else {
                                LogService.appLog.error(String.format("Session: %s - Unknown Command: %s with Sequence: %s recieved. Rejecting!!", 
                                        this.esmeLabel, pdu.getCommandId(), pdu.getCommandSequence().getValue()));
                                
                                pdu = new GNack();
                                parser.readInt(); // skip the status;
                                pdu.setCommandStatus(CommandStatus.ESME_RINVCMDID);
                                CommandSequence seq = CommandSequence.getInstance();
                                seq.setValue(parser.readInt()); // copy the sequence from request.
                                pdu.setCommandSequence(seq);
                                
                                String sessionId = StackMap.getEsmeLabel("" + seq.getValue());
                                Esme session = StackMap.getStack(sessionId);
                                session.sendPdu(pdu, this.channelMode);
                                
                                LogService.alarm(AlarmCode.SMS_UNKNOWN_PDU, this.esmeLabel, pdu.getCommandId(), pdu.getCommandSequence());
                                com.satnar.common.SpringHelper.getTraffiControl().updateExgress();
                                this.printStats(System.currentTimeMillis(), esmeLabel, pduName, pdu.getCommandSequence().getValue());
                                //return null;
                                return;
                            }
                            
                            // this will update on all exgress
                            long backendCompleteTime = System.currentTimeMillis();
                            LogService.stackTraceLog.info(String.format("Session: %s - Backend TAT: %s for Command: %s & Sequence: %s", this.esmeLabel, (backendCompleteTime - backendDelegationTime), pdu.getCommandId(), pdu.getCommandSequence().getValue()));
                            com.satnar.common.SpringHelper.getTraffiControl().updateExgress();
                        } else {
                            LogService.alarm(AlarmCode.SMS_THROTTLE_REJECT, this.esmeLabel);
                            LogService.stackTraceLog.info(String.format("Session: %s - Throttling ingress for Command: %s & Sequence: %s", this.esmeLabel, pdu.getCommandId(), pdu.getCommandSequence().getValue()));
                            EsmeHelper.sendThrottledResponse(pdu, this.channelMode);
                            this.printStats(System.currentTimeMillis(), esmeLabel, pduName, pdu.getCommandSequence().getValue());
                            //return null;
                            return;
                        }
                        
                        if ((commandId & EsmeHelper.REQUEST_MASK) == EsmeHelper.REQUEST_MASK) {                         
                            LogService.appLog.debug(String.format("Session: %s - Sending response for CommandId: %s, Sequence: %s, with Status: %s", this.esmeLabel, 
                                    pdu.getCommandId(), pdu.getCommandSequence(), response.getCommandStatus()));
                            Esme session = StackMap.getStack(this.esmeLabel);
                            if (session != null) {
                                LogService.appLog.debug(String.format("Found session %s is available: %s", this.esmeLabel, (session != null)));
                                session.sendPdu(response, this.channelMode);
                            } else {
                                LogService.appLog.error("Unable to find the session: %s to send a response back!!", this.esmeLabel);
                            }
                            
                        }
                        this.printStats(System.currentTimeMillis(), esmeLabel, pduName, pdu.getCommandSequence().getValue());
                        //return null;
                        return;
                }
            } else {
                LogService.appLog.debug("ParsingDeligate-call:pduName is null !!");
                pdu = new GNack();
                parser.readInt(); // skip the status;
                pdu.setCommandStatus(CommandStatus.ESME_RINVCMDID);
                CommandSequence seq = CommandSequence.getInstance();
                seq.setValue(parser.readInt()); // copy the sequence from request.
                pdu.setCommandSequence(seq);
                
                String sessionId = StackMap.getEsmeLabel("" + seq.getValue());
                Esme session = StackMap.getStack(sessionId);
                session.sendPdu(pdu, this.channelMode);
                this.printStats(System.currentTimeMillis(), esmeLabel, pduName, pdu.getCommandSequence().getValue());
                //return null;
                return;
            }
        } catch (IOException e) {
            LogService.appLog.error("Read/Write Failure with PDU operations!!", e);
        } catch (CamelExecutionException e) {
            LogService.appLog.error("Backend Failure with PDU operations!!", e);
        } catch (SmppCodecException e) {
            LogService.appLog.error("CODEC Failure with PDU operations!!", e);
        } finally {
            LogService.appLog.error("Catchall handling (ignore and get out) with PDU operations!!");
        }
    }
    
    private void printStats(long endTime, String session, CommandId commandId, long sequence) {
        LogService.stackTraceLog.info(String.format("Session: %s - Time in queue: %s & Time in actual execution: %s, for Command: %s & Sequence: %s", 
                    this.esmeLabel, (this.executionStartTime - this.creationTime), (endTime - this.executionStartTime), commandId, sequence));
    }
    
    private void printStats(long endTime, String session, CommandId commandId) {
        LogService.stackTraceLog.info(String.format("Session: %s - Time in queue: %s & Time in actual execution: %s, for Command: %s", 
                    this.esmeLabel, (this.executionStartTime - this.creationTime), (endTime - this.executionStartTime), commandId));
    }
    
	
}
