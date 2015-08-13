package com.satnar.smpp.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import com.satnar.common.LogService;
import com.satnar.smpp.CommandId;
import com.satnar.smpp.CommandSequence;
import com.satnar.smpp.CommandStatus;
import com.satnar.smpp.InterfaceVersion;
import com.satnar.smpp.NumberingPlanIndicator;
import com.satnar.smpp.StackMap;
import com.satnar.smpp.TypeOfNumber;
import com.satnar.smpp.codec.COctetString;
import com.satnar.smpp.codec.SmppCodecException;
import com.satnar.smpp.codec.SmppParameter;
import com.satnar.smpp.codec.SmppParameter.Type;
import com.satnar.smpp.pdu.BindReceiver;
import com.satnar.smpp.pdu.BindReceiverResponse;
import com.satnar.smpp.pdu.BindTransceiver;
import com.satnar.smpp.pdu.BindTransceiverResponse;
import com.satnar.smpp.pdu.BindTransmitter;
import com.satnar.smpp.pdu.BindTransmitterResponse;
import com.satnar.smpp.pdu.DeliverSm;
import com.satnar.smpp.pdu.DeliverSmResponse;
import com.satnar.smpp.pdu.EnquireLink;
import com.satnar.smpp.pdu.EnquireLinkResponse;
import com.satnar.smpp.pdu.GNack;
import com.satnar.smpp.pdu.SmppPdu;
import com.satnar.smpp.pdu.Unbind;
import com.satnar.smpp.transport.SmppSessionState;
import com.satnar.smpp.transport.SmppTransportException;

public abstract class EsmeHelper {
    
    public static final int        REQUEST_MASK       = 0x00000000;
    public static final int        RESPONSE_MASK      = 0x80000000;
    
    
    public static SmppPdu getBindTransceiver(String username, 
                                             String password, 
                                             String systemType, 
                                             InterfaceVersion interfaceVersion, 
                                             TypeOfNumber esmeTon, 
                                             NumberingPlanIndicator esmeNpi,
                                             String addressRange) {
        
        
        BindTransceiver bindPdu = new BindTransceiver();
        
        // Command Length - is dynamically calculated when encoded realtime
        // Command ID - is handled implicitly by PDU type
        // Command Sequence
        bindPdu.setCommandSequence(CommandSequence.getInstance());
        
        // systemId
        COctetString systemId = (COctetString) SmppParameter.getInstance(Type.C_OCTET_STRING);
        systemId.setValue(username);
        bindPdu.setSystemId(systemId);
        
        // password
        COctetString syspassword = (COctetString) SmppParameter.getInstance(Type.C_OCTET_STRING);
        syspassword.setValue(password);
        bindPdu.setPassword(syspassword);
        
        // systemType
        COctetString sysType = (COctetString) SmppParameter.getInstance(Type.C_OCTET_STRING);
        sysType.setValue(systemType);
        bindPdu.setSystemType(sysType);
        
        // interfaceVersion
        bindPdu.setInterfaceVersion(interfaceVersion);
        
        // esmeTon
        bindPdu.setAddressTon(esmeTon);
        
        // emseNpi
        bindPdu.setAddressNpi(esmeNpi);
        
        // addressRange
        COctetString addrRange = (COctetString) SmppParameter.getInstance(Type.C_OCTET_STRING);
        addrRange.setValue(addressRange);
        bindPdu.setAddressRange(addrRange);
        
        return bindPdu;
    }
    
    
    public static SmppPdu getBindTransmitter(String username, 
                                            String password, 
                                            String systemType, 
                                            InterfaceVersion interfaceVersion, 
                                            TypeOfNumber esmeTon, 
                                            NumberingPlanIndicator esmeNpi,
                                            String addressRange) {
        
        
        BindTransmitter bindPdu = new BindTransmitter();
        
        // Command Length - is dynamically calculated when encoded realtime
        // Command ID - is handled implicitly by PDU type
        // Command Sequence
        bindPdu.setCommandSequence(CommandSequence.getInstance());
        
        // systemId
        COctetString systemId = (COctetString) SmppParameter.getInstance(Type.C_OCTET_STRING);
        systemId.setValue(username);
        bindPdu.setSystemId(systemId);
        
        // password
        COctetString syspassword = (COctetString) SmppParameter.getInstance(Type.C_OCTET_STRING);
        syspassword.setValue(password);
        bindPdu.setPassword(syspassword);
        
        // systemType
        COctetString sysType = (COctetString) SmppParameter.getInstance(Type.C_OCTET_STRING);
        sysType.setValue(systemType);
        bindPdu.setSystemType(sysType);
        
        // interfaceVersion
        bindPdu.setInterfaceVersion(interfaceVersion);
        
        // esmeTon
        bindPdu.setAddressTon(esmeTon);
        
        // emseNpi
        bindPdu.setAddressNpi(esmeNpi);
        
        // addressRange
        COctetString addrRange = (COctetString) SmppParameter.getInstance(Type.C_OCTET_STRING);
        addrRange.setValue(addressRange);
        bindPdu.setAddressRange(addrRange);
        
        return bindPdu;
    }

    
    public static SmppPdu getBindReceiver(String username, 
                                            String password, 
                                            String systemType, 
                                            InterfaceVersion interfaceVersion, 
                                            TypeOfNumber esmeTon, 
                                            NumberingPlanIndicator esmeNpi,
                                            String addressRange) {
        
        
        BindReceiver bindPdu = new BindReceiver();
        
        // Command Length - is dynamically calculated when encoded realtime
        // Command ID - is handled implicitly by PDU type
        // Command Sequence
        bindPdu.setCommandSequence(CommandSequence.getInstance());
        
        // systemId
        COctetString systemId = (COctetString) SmppParameter.getInstance(Type.C_OCTET_STRING);
        systemId.setValue(username);
        bindPdu.setSystemId(systemId);
        
        // password
        COctetString syspassword = (COctetString) SmppParameter.getInstance(Type.C_OCTET_STRING);
        syspassword.setValue(password);
        bindPdu.setPassword(syspassword);
        
        // systemType
        COctetString sysType = (COctetString) SmppParameter.getInstance(Type.C_OCTET_STRING);
        sysType.setValue(systemType);
        bindPdu.setSystemType(sysType);
        
        // interfaceVersion
        bindPdu.setInterfaceVersion(interfaceVersion);
        
        // esmeTon
        bindPdu.setAddressTon(esmeTon);
        
        // emseNpi
        bindPdu.setAddressNpi(esmeNpi);
        
        // addressRange
        COctetString addrRange = (COctetString) SmppParameter.getInstance(Type.C_OCTET_STRING);
        addrRange.setValue(addressRange);
        bindPdu.setAddressRange(addrRange);
        
        return bindPdu;
    }


    public static SmppPdu getUnbind() {
        Unbind unbindPdu = new Unbind();
        
        // Command Length - is dynamically calculated when encoded realtime
        // Command ID - is handled implicitly by PDU type
        // Command Sequence
        unbindPdu.setCommandSequence(CommandSequence.getInstance());
        
        return unbindPdu;
    }


    public static void handleGNack(byte[] rawPdu) {
        try {
            SmppPdu gnackPdu = new GNack();
            gnackPdu.decode(rawPdu);
            
            String sessionId = StackMap.getEsmeLabel("" + gnackPdu.getCommandSequence().getValue());
            Esme session = StackMap.getStack(sessionId);
            StackMap.removeMessageIndex("" + gnackPdu.getCommandSequence().getValue());
            LogService.appLog.error("Received GNACK for Seq: " + gnackPdu.getCommandSequence().getValue());
            
            if (session.isCanUseTrx()) {
                if (session.getTrxChannel().getConnectionState() == SmppSessionState.OPEN) {
                    session.stop();
                }
            }
            
        } catch (SmppCodecException e) {
            //TODO: Log this... Cant throw exceptions.... just drop the PDU...
        	LogService.stackTraceLog.debug("EsmeHelper-handleGNack:Encountered exception",e);
        }
        
    }


    public static void handleBindTransceiverResponse(byte[] rawPdu) {
        try {
            SmppPdu bindTrxRespPdu = new BindTransceiverResponse();
            bindTrxRespPdu.decode(rawPdu);
            LogService.stackTraceLog.trace("Decoded Read Buffer - " + bindTrxRespPdu.toString());
            
            String sessionId = StackMap.getEsmeLabel("" + bindTrxRespPdu.getCommandSequence().getValue());
            Esme session = StackMap.getStack(sessionId);
            StackMap.removeMessageIndex("" + bindTrxRespPdu.getCommandSequence().getValue());

            if (bindTrxRespPdu.getCommandStatus() == CommandStatus.ESME_ROK) {
                session.getTrxChannel().setConnectionState(SmppSessionState.BOUND_TRX);
                LogService.appLog.info(sessionId + " is successfully boound TRX");
            } else {
                LogService.appLog.error(sessionId + " failed to bind TRX. Error: " + bindTrxRespPdu.getCommandStatus()); 
                session.stop();
            }
            

        } catch (SmppCodecException e) {
            //TODO: Log this... Cant throw exceptions.... just drop the PDU...
        	LogService.stackTraceLog.debug("EsmeHelper-handleBindTransceiverResponse:Encountered exception",e);
        } catch (SmppTransportException e) {
            // TODO just log... ideall will not happen;
        	LogService.stackTraceLog.debug("EsmeHelper-handleBindTransceiverResponse:Encountered exception",e);
        }
    }
    
    public static void handleBindTransmitterResponse(byte[] rawPdu) {
        try {
            SmppPdu bindTxRespPdu = new BindTransmitterResponse();
            bindTxRespPdu.decode(rawPdu);
            
            String sessionId = StackMap.getEsmeLabel("" + bindTxRespPdu.getCommandSequence().getValue());
            Esme session = StackMap.getStack(sessionId);
            StackMap.removeMessageIndex("" + bindTxRespPdu.getCommandSequence().getValue());

            if (bindTxRespPdu.getCommandStatus() == CommandStatus.ESME_ROK) {
                session.getTxChannel().setConnectionState(SmppSessionState.BOUND_TX);
                LogService.appLog.info(sessionId + " is successfully boound TX");
            } else {
                LogService.appLog.error(sessionId + " failed to bind TX. Error: " + bindTxRespPdu.getCommandStatus()); 
               session.stop();
            }
            

        } catch (SmppCodecException e) {
            //TODO: Log this... Cant throw exceptions.... just drop the PDU...
        	LogService.stackTraceLog.debug("EsmeHelper-handleBindTransmitterResponse:Encountered exception",e);
        } catch (SmppTransportException e) {
            // TODO just log... ideall will not happen;
        	LogService.stackTraceLog.debug("EsmeHelper-handleBindTransmitterResponse:Encountered exception",e);
        }
    }
    
    public static void handleBindReceiverResponse(byte[] rawPdu, String sessionId) {
        try {
            SmppPdu bindRxRespPdu = new BindReceiverResponse();
            bindRxRespPdu.decode(rawPdu);
            
            Esme session = StackMap.getStack(sessionId);
            StackMap.removeMessageIndex("" + bindRxRespPdu.getCommandSequence().getValue());

            if (bindRxRespPdu.getCommandStatus() == CommandStatus.ESME_ROK) {
                session.getRxChannel().setConnectionState(SmppSessionState.BOUND_RX);
                LogService.appLog.info(sessionId + " is successfully boound RX");
            } else {
                LogService.appLog.error(sessionId + " failed to bind RX. Error: " + bindRxRespPdu.getCommandStatus()); 
                session.stop();
            }
            

        } catch (SmppCodecException e) {
            //TODO: Log this... Cant throw exceptions.... just drop the PDU...
        	LogService.stackTraceLog.debug("EsmeHelper-handleBindReceiverResponse:Encountered exception",e);
        } catch (SmppTransportException e) {
            // TODO just log... ideall will not happen;
        	LogService.stackTraceLog.debug("EsmeHelper-handleBindReceiverResponse:Encountered exception",e);
        }
    }


    public static void handleDeliverSmRequest(byte[] rawPdu) {
       try {
           SmppPdu deliverSmPdu = new DeliverSm();
           deliverSmPdu.decode(rawPdu);
           
           String sessionId = StackMap.getEsmeLabel("" + deliverSmPdu.getCommandSequence().getValue());
           Esme session = StackMap.getStack(sessionId);
           StackMap.removeMessageIndex("" + deliverSmPdu.getCommandSequence().getValue());
           
           SmppPdu deliverSmRespPdu = new DeliverSmResponse();
           deliverSmRespPdu.setCommandStatus(CommandStatus.ESME_ROK);
           deliverSmRespPdu.setCommandSequence(deliverSmPdu.getCommandSequence());
           
           session.sendPdu(deliverSmRespPdu, ChannelMode.RX);
           

       } catch (SmppCodecException e) {
           //TODO: Log this... Cant throw exceptions.... just drop the PDU...
    	   LogService.stackTraceLog.debug("EsmeHelper-handleDeliverSmRequest:Encountered exception",e);
       }
        
    }


    public static void handleEnquireLinkRequest(byte[] rawPdu, String esmeLabel, ChannelMode channelMode) {
        try {
            SmppPdu enquireLinkPdu = new EnquireLink();
            enquireLinkPdu.decode(rawPdu);
            LogService.appLog.debug("Handling PDU: " + enquireLinkPdu.toString() + ", esme: " + esmeLabel + ", mode: " + channelMode); 
            
            
            Esme session = StackMap.getStack(esmeLabel);
            LogService.appLog.debug("ESME Session found is available: " + (session != null)); 
            
            SmppPdu enquireLinkResponsePdu = new EnquireLinkResponse();
            enquireLinkResponsePdu.setCommandStatus(CommandStatus.ESME_ROK);
            enquireLinkResponsePdu.setCommandSequence(enquireLinkPdu.getCommandSequence());
            LogService.appLog.debug("Responding PDU: " + enquireLinkResponsePdu.toString() + ", esme: " + esmeLabel + ", mode: " + channelMode); 
            
            LogService.appLog.debug("Sending EnquireLink Response: " + esmeLabel);
            session.sendPdu(enquireLinkResponsePdu, channelMode);
            LogService.appLog.debug("Sent EnquireLink Response: " + esmeLabel);
        } catch (SmppCodecException e) {
            //TODO: Log this... Cant throw exceptions.... just drop the PDU...
        	LogService.stackTraceLog.debug("EsmeHelper-handleEnquireLinkRequest:Encountered exception",e);
        } catch(Exception e){
        	LogService.stackTraceLog.debug("EsmeHelper-handleEnquireLinkRequest:Encountered gen exception",e);
        }
    }


    public static void handleEnquireLinkResponse(byte[] rawPdu) {
        try {
            SmppPdu enquireLinkPdu = new EnquireLinkResponse();
            enquireLinkPdu.decode(rawPdu);
            
            String sessionId = StackMap.getEsmeLabel("" + enquireLinkPdu.getCommandSequence().getValue());
            Esme session = StackMap.getStack(sessionId);
            StackMap.removeMessageIndex("" + enquireLinkPdu.getCommandSequence().getValue());

            if (enquireLinkPdu.getCommandStatus() != CommandStatus.ESME_ROK) {
                //TODO: log this... we shutdown the stack
            	LogService.stackTraceLog.debug("EsmeHelper-handleEnquireLinkResponse: Response is not OK..");
                session.stop();
            }
            
        } catch (SmppCodecException e) {
            //TODO: Log this... Cant throw exceptions.... just drop the PDU...
        	LogService.stackTraceLog.debug("EsmeHelper-handleEnquireLinkResponse:Encountered exception",e);
        }
    }


    public static void sendDeliverSmThrottled(byte[] rawPdu) {
        try {
            SmppPdu deliverSmPdu = new DeliverSm();
            deliverSmPdu.decode(rawPdu);
            
            String sessionId = StackMap.getEsmeLabel("" + deliverSmPdu.getCommandSequence().getValue());
            Esme session = StackMap.getStack(sessionId);
            StackMap.removeMessageIndex("" + deliverSmPdu.getCommandSequence().getValue());
            
            SmppPdu deliverSmRespPdu = new DeliverSmResponse();
            deliverSmRespPdu.setCommandStatus(CommandStatus.ESME_RTHROTTLED);
            deliverSmRespPdu.setCommandSequence(deliverSmPdu.getCommandSequence());
            
            session.sendPdu(deliverSmRespPdu, ChannelMode.RX);
            

        } catch (SmppCodecException e) {
        	LogService.stackTraceLog.debug("EsmeHelper-sendDeliverSmThrottled:Encountered exception",e);
        }
         
    }


    public static void sendThrottledResponse(SmppPdu pdu, ChannelMode mode) {
        try {
            pdu.setCommandId(CommandId.valueOf(pdu.getCommandId().getId() | RESPONSE_MASK));
            pdu.setCommandStatus(CommandStatus.ESME_RTHROTTLED);
            
            String sessionId = StackMap.getEsmeLabel("" + pdu.getCommandSequence().getValue());
            Esme session = StackMap.getStack(sessionId);
            StackMap.removeMessageIndex("" + pdu.getCommandSequence().getValue());
            
            session.sendPdu(pdu, mode);
        } catch (SmppCodecException e) {
            LogService.stackTraceLog.debug("EsmeHelper-sendThrottledResponse:Encountered exception",e);
        }
        return;
    }
    
    public static void sendThrottledResponse(int commandId, int sequence, String esmeLabel, ChannelMode mode) {
        try {
            CommandSequence commandSequence = CommandSequence.getInstance();
            commandSequence.setValue(sequence);
            
            SmppPdu pdu = new GNack();
            pdu.setCommandId(CommandId.valueOf(commandId));
            pdu.setCommandStatus(CommandStatus.ESME_RTHROTTLED);
            pdu.setCommandSequence(commandSequence);
            
            Esme session = StackMap.getStack(esmeLabel);
            
            session.sendPdu(pdu, mode);
        } catch (SmppCodecException e) {
            LogService.appLog.error("EsmeHelper-sendThrottledResponse: Unable to send ThrottledResponse!", e);
        }
        return;
    }


    public static String prettyPrint(byte[] serialized) {
        StringBuilder sbPrettyPrint = new StringBuilder();
        for (byte atom: serialized) {
            sbPrettyPrint.append(Integer.toHexString( (0xff&atom)));
            sbPrettyPrint.append(" ");
        }
        return sbPrettyPrint.toString();
    }
   
    
    
}
