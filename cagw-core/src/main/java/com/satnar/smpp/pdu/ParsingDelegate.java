package com.satnar.smpp.pdu;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.concurrent.Callable;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;

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
    private ChannelMode channelMode = null;
    
    public ParsingDelegate(CamelContext context) {
        producerTemplate = context.createProducerTemplate();
    }
    
    public ParsingDelegate(byte[] serialized, ChannelMode channelMode) {
        this.raw = serialized;
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
                    LogService.appLog.debug("GNACK Received and delegated");
                    EsmeHelper.handleGNack(rawPdu);
                    return null;
                case BIND_RECEIVER_RESP:
                    LogService.appLog.debug("BIND_RECEIVER_RESP Received and delegated");
                    EsmeHelper.handleBindReceiverResponse(rawPdu);
                    return null;
                case BIND_TRANSCEIVER_RESP:
                    LogService.appLog.debug("BIND_TRANSCEIVER_RESP Received and delegated");
                    EsmeHelper.handleBindTransceiverResponse(rawPdu);
                    return null;
                case BIND_TRANSMITTER_RESP:
                    LogService.appLog.debug("BIND_TRANSMITTER_RESP Received and delegated");
                    EsmeHelper.handleBindTransmitterResponse(rawPdu);
                    return null;
                case DELIVER_SM:
                    LogService.appLog.debug("DELIVER_SM Received and delegated");
                    if (com.satnar.common.SpringHelper.getTraffiControl().authorizeIngress())
                        EsmeHelper.handleDeliverSmRequest(rawPdu);
                    else
                        EsmeHelper.sendDeliverSmThrottled(rawPdu);
                    com.satnar.common.SpringHelper.getTraffiControl().updateExgress();
                    return null;
                case ENQUIRE_LINK:
                    LogService.appLog.debug("ENQUIRE_LINK Received and delegated");
                    EsmeHelper.handleEnquireLinkRequest(rawPdu);
                    return null;
                case ENQUIRE_LINK_RESP:
                    LogService.appLog.debug("ENQUIRE_LINK_RESP Received and delegated");
                    EsmeHelper.handleEnquireLinkResponse(rawPdu);
                    return null;
                default:
                    LogService.appLog.debug("UNKNOWN/EXTENDED Received and delegated");
                    SmppPdu request = SpringHelper.getSmppPduImplementation(Integer.toHexString(commandId));
                    pdu.decode(rawPdu);

                    // this is where we throw the event to backend for processing...
                    LogService.appLog.debug("ParsingDeligate-call:Sending request to cagw-backend!!:CommandId:"+request.getCommandId().name()+"CommandSequence:"+request.getCommandSequence().getValue());
                    SmppPdu response = null;
                    if (com.satnar.common.SpringHelper.getTraffiControl().authorizeIngress()){
                        response = producerTemplate.requestBody(processingEndpoint, pdu, SmppPdu.class);
                    	LogService.appLog.debug("ParsingDeligate-call:Received response:CommandId:"+request.getCommandId().name()+":CommandSequence:"+request.getCommandSequence().getValue()+":Command Status:"+response.getCommandStatus().name());
                    }
                    else {
                        response = EsmeHelper.getThrottledResponse(pdu);
                    	LogService.appLog.debug("ParsingDeligate-call:Throttling this request:CommandId:"+request.getCommandId().name()+"CommandSequence:"+request.getCommandSequence().getValue());
                    }
                    if ((commandId & EsmeHelper.REQUEST_MASK) == EsmeHelper.REQUEST_MASK) {                         
                        String sessionId = StackMap.getEsmeLabel("" + request.getCommandSequence().getValue());
                        Esme session = StackMap.getStack(sessionId);
                        session.sendPdu(response, this.channelMode);
                        
                        LogService.appLog.debug("ParsingDeligate-call:Received response:CommandId:"+request.getCommandId().name()+":CommandSequence:"+request.getCommandSequence().getValue()+":Command Status:"+response.getCommandStatus().name());
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
