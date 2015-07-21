package com.satnar.smpp.pdu;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.concurrent.Callable;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.ProducerTemplate;

import com.ericsson.raso.cac.smpp.pdu.viettel.AuthAcc;
import com.ericsson.raso.cac.smpp.pdu.viettel.AuthAccResponse;
import com.ericsson.raso.cac.smpp.pdu.viettel.SmResultNotify;
import com.ericsson.raso.cac.smpp.pdu.viettel.SmResultNotifyResponse;
import com.satnar.common.LogService;
import com.satnar.smpp.CommandId;
import com.satnar.smpp.CommandSequence;
import com.satnar.smpp.CommandStatus;
import com.satnar.smpp.SpringHelper;
import com.satnar.smpp.StackMap;
import com.satnar.smpp.client.ChannelMode;
import com.satnar.smpp.client.Esme;
import com.satnar.smpp.client.EsmeHelper;

public class ParsingDelegate implements Callable<Void> {
    
    private static final String     processingEndpoint = "seda:cagw-backend";
    
    private static ProducerTemplate producerTemplate = null;
    
    private byte[] raw = null;
    private String esmeLabel = null;
    private ChannelMode channelMode = null;
    
    public ParsingDelegate(CamelContext context) {
        producerTemplate = context.createProducerTemplate();
    }
    
    public ParsingDelegate(byte[] serialized, String esmeLabel, ChannelMode channelMode) {
        this.raw = serialized;
        this.esmeLabel = esmeLabel;
        this.channelMode = channelMode;
    }

    public Void call() throws Exception {
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
                    return null;
                case BIND_RECEIVER_RESP:
                    LogService.appLog.debug(String.format("Session: %s - BIND_RECEIVER_RESP Received and delegated", this.esmeLabel));
                    EsmeHelper.handleBindReceiverResponse(rawPdu);
                    return null;
                case BIND_TRANSCEIVER_RESP:
                    LogService.appLog.debug(String.format("Session: %s - BIND_TRANSCEIVER_RESP Received and delegated", this.esmeLabel));
                    EsmeHelper.handleBindTransceiverResponse(rawPdu);
                    return null;
                case BIND_TRANSMITTER_RESP:
                    LogService.appLog.debug(String.format("Session: %s - BIND_TRANSMITTER_RESP Received and delegated", this.esmeLabel));
                    EsmeHelper.handleBindTransmitterResponse(rawPdu);
                    return null;
                case DELIVER_SM:
                    LogService.appLog.debug(String.format("Session: %s - DELIVER_SM Received and delegated", this.esmeLabel));
                    if (com.satnar.common.SpringHelper.getTraffiControl().authorizeIngress()) {
                        EsmeHelper.handleDeliverSmRequest(rawPdu);
                        com.satnar.common.SpringHelper.getTraffiControl().updateExgress();
                    } else
                        EsmeHelper.sendDeliverSmThrottled(rawPdu);
                    return null;
                case ENQUIRE_LINK:
                    LogService.appLog.debug(String.format("Session: %s - ENQUIRE_LINK Received and delegated", this.esmeLabel));
                    EsmeHelper.handleEnquireLinkRequest(rawPdu, this.esmeLabel, this.channelMode);
                    return null;
                case ENQUIRE_LINK_RESP:
                    LogService.appLog.debug(String.format("Session: %s - ENQUIRE_LINK_RESP Received and delegated", this.esmeLabel));
                    EsmeHelper.handleEnquireLinkResponse(rawPdu);
                    return null;
                default:
                    LogService.appLog.debug(String.format("Session: %s - UNKNOWN/EXTENDED Received and delegated", this.esmeLabel));
                    pdu = SpringHelper.getSmppPduImplementation(Integer.toHexString(commandId));
                    pdu.decode(rawPdu);

                    // this is where we throw the event to backend for processing...
                    LogService.appLog.debug(String.format("Session: %s - Sending request to cagw-backend!!:CommandId: %s, Sequence: %s", this.esmeLabel, pdu.getCommandId(), 
                            pdu.getCommandSequence().getValue()));
                    SmppPdu response = null;
                    if (com.satnar.common.SpringHelper.getTraffiControl().authorizeIngress()){
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
                        }
                        if (pdu.getCommandId().getId() == CommandId.SM_RESULT_NOTIFY.getId()) {
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
                            
                        }
                    } else {
                        LogService.stackTraceLog.info(String.format("Session: %s - Throttling ingress for Command: %s & Sequence: %s", this.esmeLabel, pdu.getCommandId(), pdu.getCommandSequence().getValue()));
                        EsmeHelper.sendThrottledResponse(pdu, this.channelMode);
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
                    com.satnar.common.SpringHelper.getTraffiControl().updateExgress();
                    return null;
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
            return null;
        }
    }
    
	
}
