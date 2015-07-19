package com.satnar.smpp.client;

import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import com.satnar.common.LogService;
import com.satnar.smpp.CommandId;
import com.satnar.smpp.CommandSequence;
import com.satnar.smpp.InterfaceVersion;
import com.satnar.smpp.NumberingPlanIndicator;
import com.satnar.smpp.StackMap;
import com.satnar.smpp.TypeOfNumber;
import com.satnar.smpp.codec.COctetString;
import com.satnar.smpp.codec.SmppCodecException;
import com.satnar.smpp.codec.SmppParameter;
import com.satnar.smpp.codec.SmppParameter.Type;
import com.satnar.smpp.pdu.BindReceiver;
import com.satnar.smpp.pdu.BindTransceiver;
import com.satnar.smpp.pdu.BindTransmitter;
import com.satnar.smpp.pdu.EnquireLink;
import com.satnar.smpp.pdu.SmppPdu;
import com.satnar.smpp.transport.Connection;
import com.satnar.smpp.transport.ReadHelper;
import com.satnar.smpp.transport.SmppSessionState;
import com.satnar.smpp.transport.SmppTransportException;
import com.satnar.smpp.transport.TcpConnection;
import com.satnar.smpp.transport.WriteHelper;

public class Esme {
    
    // literals
    private static final String    USERNAME             = "username";
    private static final String    PASSWORD             = "password";
    private static final String    SYSTEM_TYPE          = "systemType";
    private static final String    INTERFACE_VERSION    = "interfaceVersion";
    private static final String    ESME_TON             = "esmeTon";
    private static final String    ESME_NPI             = "esmeNpi";
    private static final String    ADDRESS_RANGE        = "addressRange";
    private static final String    ENQUIRE_LINK_ENABLED = "isEnquireLinkEnabled";
    private static final String    ENQUIRE_LINK_PERIOD  = "enquireLinkPeriod";
    private static final String    CAN_USE_TRX          = "canUseTrx";
    private static final String    THREADPOOL_SIZE      = "threadPoolSize";
    
    private static final String    TX_TCP_ADDRESS       = "tx.tcpAddress";
    private static final String    TX_TCP_PORT          = "tx.tcpPort";
    private static final String    TX_ESME_LABEL        = "tx.esmeLabel";
    private static final String    TX_LAZY_WRITE_WAIT   = "tx.lazyWriteWait";
    
    private static final String    RX_TCP_ADDRESS       = "rx.tcpAddress";
    private static final String    RX_TCP_PORT          = "rx.tcpPort";
    private static final String    RX_ESME_LABEL        = "rx.esmeLabel";
    private static final String    RX_LAZY_WRITE_WAIT   = "rx.lazyWriteWait";
    
    private static final String    TRX_TCP_ADDRESS      = "trx.tcpAddress";
    private static final String    TRX_TCP_PORT         = "trx.tcpPort";
    private static final String    TRX_ESME_LABEL       = "trx.esmeLabel";
    private static final String    TRX_LAZY_WRITE_WAIT  = "trx.lazyWriteWait";
    
    // functional config
    private boolean                canUseTrx            = false;
    private boolean                isEnquireLinkEnabled = false;
    private int                    enquireLinkPeriod    = 0;
    private Timer                  enquireLinkSchedule  = null;
    
    // stack related config
    private String                 username             = null;
    private String                 password             = null;
    private String                 systemType           = null;
    private InterfaceVersion       interfaceVersion     = null;
    private TypeOfNumber           esmeTon              = null;
    private NumberingPlanIndicator esmeNpi              = null;
    private String                 addressRange         = null;
    
    // members
    private Properties             config               = null;
    private Properties             txConfig             = null;
    private Properties             rxConfig             = null;
    private Properties             trxConfig            = null;
    private Connection             trxChannel           = null;
    private Connection             txChannel            = null;
    private Connection             rxChannel            = null;
    private WriteHelper            txWriter             = null;
    private ReadHelper             txReader             = null;
    private WriteHelper            rxWriter             = null;
    private ReadHelper             rxReader             = null;
    private WriteHelper            trxWriter            = null;
    private ReadHelper             trxReader             = null;
    
    public Esme(Properties esmeConfig) {
        this.config = esmeConfig;
    }
    
    public void start() throws SmppServiceException {
        // first check if we have everything in order to start connecting...
        this.validateInitializeConfig();
        LogService.appLog.debug("Check initialization parameters: " + this.toString());
        
        try {
            if (this.canUseTrx) {
                // lets try to connect...
                this.trxChannel = new TcpConnection(this.trxConfig, ChannelMode.TRX);
                this.trxChannel.connect();
                LogService.appLog.debug("Socket Conneted for :" + this.getEsmeLabel() );
                
                this.trxWriter = new WriteHelper(this.trxChannel);
                LogService.appLog.debug("Lazy Write Ready for :" + this.getEsmeLabel() );
                
                this.trxReader = new ReadHelper(this.trxChannel);
                new Thread(this.trxReader).start();
                LogService.appLog.debug("Sliding Window Reader Ready for :" + this.getEsmeLabel() );
                
                if (this.isEnquireLinkEnabled) {
                    this.enquireLinkSchedule = new Timer("EnquireLink-TRX-" + this.getEsmeLabel());
                    this.enquireLinkSchedule.schedule(new EnquireLinkTask(this), this.enquireLinkPeriod, this.enquireLinkPeriod);
                    LogService.appLog.debug("Connection Watchdog Ready for :" + this.getEsmeLabel() );
                }
                
                // lets try to bind...
                this.bindTransceiver();
                LogService.appLog.debug("Bind Success for :" + this.getEsmeLabel() );
                StackMap.addSession(this.getEsmeLabel(), this);
                
                LogService.stackTraceLog.info("Esme-start:BindingTransiever is successful!!");
            } else {
                // lets try to connect...
                this.txChannel = new TcpConnection(this.txConfig, ChannelMode.TX);
                // ----- this is a dirty hack only for 5h1tty fucking Huawei SMPP+ crappy motherass fucking implementation
                ((TcpConnection)this.txChannel).validateInitializeConfig();
//                LogService.appLog.debug("Socket Conneted for :" + this.username + "@" + this.systemType );
                
                
                /*this.txChannel.connect();
                this.txWriter = new WriteHelper(this.txChannel);
                this.txReader = new ReadHelper(this.txChannel);
                new Thread(this.txReader).start();
                if (this.isEnquireLinkEnabled) {
                    this.enquireLinkSchedule = new Timer("EnquireLink-TX-" + this.txChannel.getEsmeLabel());
                    this.enquireLinkSchedule.schedule(new EnquireLinkTask(this), this.enquireLinkPeriod, this.enquireLinkPeriod);
                }*/
                
                this.rxChannel = new TcpConnection(this.rxConfig, ChannelMode.RX);
                this.rxChannel.connect();
                LogService.appLog.debug("Socket Connected for :" + this.getEsmeLabel() );
                
                this.rxWriter = new WriteHelper(this.rxChannel);
                LogService.appLog.debug("Lazy Write Ready for :" + this.getEsmeLabel() );
                
                this.rxReader = new ReadHelper(this.rxChannel);
                new Thread(this.rxReader).start();
                LogService.appLog.debug("Sliding Window Reader Ready for :" + this.getEsmeLabel() );
                
                if (this.isEnquireLinkEnabled) {
                    this.enquireLinkSchedule = new Timer("EnquireLink-RX-" + this.getEsmeLabel());
                    this.enquireLinkSchedule.schedule(new EnquireLinkTask(this), this.enquireLinkPeriod, this.enquireLinkPeriod);
                    LogService.appLog.debug("Connection Watchdog Ready for :" + this.getEsmeLabel() );
               }
                
                // lets try to bind...
                // lets bind TX first....
//                this.bindTransmitter();
//                LogService.appLog.debug("Bind Success for :" + this.txChannel.getEsmeLabel() );
//                StackMap.addSession(this.txChannel.getEsmeLabel(), this);
                
                
                // lets bind RX next....
                this.bindReceiver();
                LogService.appLog.debug("Bind Success for :" + this.getEsmeLabel() );
                StackMap.addSession(this.getEsmeLabel(), this);
                
            }
        } catch (SmppTransportException e) {
            // TODO Log for troubleshooting
        	LogService.stackTraceLog.debug(this.getEsmeLabel() + "-Esme-start:Unable to connect & bind!:",e);
            throw new SmppServiceException(this.getEsmeLabel() + "-Unable to connect & bind!", e);
        }
    }
    
    public void stop() {
        String label = null;
        
        if (this.enquireLinkSchedule != null) {
            this.enquireLinkSchedule.cancel();
            this.enquireLinkSchedule = null;
            LogService.appLog.info(this.getEsmeLabel() + " - Watchdog thread stopped");
        }

        try {
            if (this.canUseTrx) {
                if (this.trxChannel.getConnectionState() == SmppSessionState.BOUND_TRX) {
                    LogService.appLog.info(this.getEsmeLabel() + " - TRX Mode in valid BOUND state... Unbinding");
                    this.unbindTrx();
                }
               
                if(this.trxWriter != null) this.trxWriter.stop();
                if(this.trxReader != null)this.trxReader.stop();
                this.trxWriter = null;
                this.trxReader = null;
                LogService.appLog.info(this.getEsmeLabel() + " - Cleanup transport resources");
               
                label = this.getEsmeLabel();
                StackMap.removeSession(label);
                if(this.trxChannel != null) this.trxChannel.disconnect();
                this.trxChannel = null;
                
                LogService.stackTraceLog.info(this.getEsmeLabel() + " Esme-stop:Transiever is successful!!");
            } else {
                if (this.txChannel.getConnectionState() == SmppSessionState.BOUND_TX) {
                    LogService.appLog.info(this.getEsmeLabel() + " - TX Mode in valid BOUND state... Unbinding");
                    this.unbindTx();
                }
                
                if (this.rxChannel.getConnectionState() == SmppSessionState.BOUND_RX) {
                    LogService.appLog.info(this.getEsmeLabel() + " - RX Mode in valid BOUND state... Unbinding");
                    this.unbindRx();
                }
                
                if(this.txWriter != null) this.txWriter.stop();
                if(this.txReader != null) this.txReader.stop();
                if(this.rxWriter != null) this.rxWriter.stop();
                if(this.rxReader != null) this.rxReader.stop();
                this.txWriter = null;
                this.rxWriter = null;
                this.txReader = null;
                this.rxReader = null;
                
//                label = this.txChannel.getEsmeLabel();
//                StackMap.removeSession(label);
//                if(this.txChannel != null) this.txChannel.disconnect();
//                this.txChannel = null;
                
                LogService.stackTraceLog.info(this.getEsmeLabel() + "Esme-stop:Transmitter is successful!!");
                if(this.rxChannel != null) this.rxChannel.disconnect();
                this.rxChannel = null;
                
                LogService.stackTraceLog.info(this.getEsmeLabel() + "Esme-stop:Receiver is successful!!");
            }
        } catch (SmppTransportException e) {
            // TODO Log for troubleshooting
        	LogService.stackTraceLog.debug(this.getEsmeLabel() + "Esme-stop:Unable to stop!:",e);
            StackMap.removeSession(label);
        } catch (SmppServiceException e) {
            // TODO Log for troubleshooting
        	LogService.stackTraceLog.debug(this.getEsmeLabel() + "Esme-stop:Unable to stop!:",e);
            StackMap.removeSession(label);
        }
    }
    
    public void sendPdu(SmppPdu pdu, ChannelMode mode) throws SmppCodecException {
        LogService.stackTraceLog.debug(this.getEsmeLabel() + " - Esme-sendPdu: Attempting... CommandId:"+pdu.getCommandId().name()+", CommandSequence:"+pdu.getCommandSequence().getValue());

        try {
            if (this.canUseTrx) {
                LogService.appLog.debug(this.getEsmeLabel() + " - TRX Lazy Write PDU:" + pdu.toString());
                this.trxWriter.writeLazy(pdu);
                return;
            }
            
            LogService.appLog.debug(this.getEsmeLabel() + " - Command ID: " + pdu.getCommandId());
            if (pdu.getCommandId() == CommandId.EXTENDED || pdu.getCommandId() == CommandId.ENQUIRE_LINK_RESP ) {
                if (mode == ChannelMode.TX) {
                    LogService.appLog.debug(this.getEsmeLabel() + " - TX Lazy Write PDU:" + pdu.toString());
                    this.txWriter.writeLazy(pdu);
                } else {
                    LogService.appLog.debug(this.getEsmeLabel() + " - RX Lazy Write PDU:" + pdu.toString());
                    this.rxWriter.writeLazy(pdu);
                }
                return;
            }
            
            LogService.appLog.debug(this.getEsmeLabel() + " - Standard Command ID: " + pdu.getCommandId());
            if (pdu.getCommandId().isRxCompatible()) {
                LogService.appLog.debug(this.getEsmeLabel() + " - RX Lazy Write PDU:" + pdu.toString());
                this.rxWriter.writeLazy(pdu);
            } else if (pdu.getCommandId().isTxCompatible()) {
                LogService.appLog.debug(this.getEsmeLabel() + " - TX Lazy Write PDU:" + pdu.toString());
                this.txWriter.writeLazy(pdu);
            } else {
                LogService.appLog.error(String.format("Quite Strange that CommandID: %s is not compatible with both Tx and Rx", pdu.getCommandId()));
            }
            
        } catch (SmppTransportException e) {
            //TODO: Log for troubelshooting. Seems like the transport is broken. Must stop the stack.
        	LogService.stackTraceLog.debug(this.getEsmeLabel() + " - Esme-sendPdu:Seems like the transport is broken. Stopping the stack. CommandSequence:"+pdu.getCommandSequence().getValue());
            this.stop();
        } catch (Exception e){
        	LogService.stackTraceLog.debug("ESME-SendPDU:Encountered exception..:"+e);
        }
    }
    
    private void validateInitializeConfig() throws SmppServiceException {
        String param = null;
        
        // check username...
        param = this.config.getProperty(USERNAME);
        if (param == null || param.equalsIgnoreCase(""))
            throw new SmppServiceException("'username' was not defined or empty!!");
        this.username = param;
        
        // check password...
        param = this.config.getProperty(PASSWORD);
        if (param == null || param.equalsIgnoreCase(""))
            throw new SmppServiceException("'password' was not defined or empty!!");
        this.password = param;
        
        // check systemType...
        param = this.config.getProperty(SYSTEM_TYPE);
        if (param == null || param.equalsIgnoreCase(""))
            throw new SmppServiceException("'systemType' was not defined or empty!!");
        this.systemType = param;
        
        // check systemType...
        param = this.config.getProperty(ADDRESS_RANGE);
        /*
         * disabling mandatory check for this param, since it is possible to
         * send an empty string
         * 
         * if (param == null || param.equalsIgnoreCase("")) throw new
         * SmppServiceException("'systemType' was not defined or empty!!");
         */
        this.addressRange = param;
        
        // check the interface version...
        param = this.config.getProperty(INTERFACE_VERSION);
        if (param == null || param.equalsIgnoreCase(""))
            throw new SmppServiceException("'interfaceVersion' was not defined or empty!!");
        
        try {
            this.interfaceVersion = InterfaceVersion.valueOf(param);
        } catch (IllegalArgumentException e) {
            throw new SmppServiceException("'interfaceVersion' was defined but not valid!! Found: " + param);
        }
        
        // check the esme ton...
        param = this.config.getProperty(ESME_TON);
        if (param == null || param.equalsIgnoreCase(""))
            throw new SmppServiceException("'esmeTon' was not defined or empty!!");
        
        try {
            this.esmeTon = TypeOfNumber.valueOf(param);
        } catch (IllegalArgumentException e) {
            throw new SmppServiceException("'esmeTon' was defined but not valid!! Found: " + param);
        }
        
        // check the esme npi...
        param = this.config.getProperty(ESME_NPI);
        if (param == null || param.equalsIgnoreCase(""))
            throw new SmppServiceException("'esmeNpi' was not defined or empty!!");
        
        try {
            this.esmeNpi = NumberingPlanIndicator.valueOf(param);
        } catch (IllegalArgumentException e) {
            throw new SmppServiceException("'esmeNpi' was defined but not valid!! Found: " + param);
        }
        
        // check enquire link enabled...
        param = this.config.getProperty(ENQUIRE_LINK_ENABLED);
        if (param != null && (param.equalsIgnoreCase("true") || param.equalsIgnoreCase("false"))) {
            this.isEnquireLinkEnabled = Boolean.parseBoolean(param);
        } else {
            throw new SmppServiceException(
                    "'isEnquireLinkEnabled' is not defined 'true' or 'false'. Rejecting config to avoid unintended behaviours!!");
        }
        
        // check enquire link period...
        param = this.config.getProperty(ENQUIRE_LINK_PERIOD);
        if (param == null || param.equalsIgnoreCase("")) {
            if (this.isEnquireLinkEnabled)
                throw new SmppServiceException("'isEnquireLinkEnabled' is enabled but 'enquireLinkPeriod' is not defined!!");
        }
        
        try {
            this.setEnquireLinkPeriod(java.lang.Integer.parseInt(param));
        } catch (Exception e) {
            if (this.isEnquireLinkEnabled)
                throw new SmppServiceException(
                        "'isEnquireLinkEnabled' is enabled but 'enquireLinkPeriod' is not numeric!! Found: " + param, e);
        }
        
        
        // check TRX enabled...
        param = this.config.getProperty(CAN_USE_TRX);
        if (param != null && (param.equalsIgnoreCase("true") || param.equalsIgnoreCase("false"))) {
            this.canUseTrx = Boolean.parseBoolean(param);
        } else {
            throw new SmppServiceException(
                    "'canUseTrx' is not defined 'true' or 'false'. Rejecting config to avoid unintended behaviours!!");
        }
        
        // check & prepare Connection specific config...
        if (this.canUseTrx) {
            this.trxConfig = new Properties();
            
            // get tcpAddress
            param = this.config.getProperty(TRX_TCP_ADDRESS);
            if (param == null || param.equalsIgnoreCase("")) {
                throw new SmppServiceException("'trx.tcpAddress' is not defined when TRX is enabled!!");
            } else {
                this.trxConfig.setProperty("tcpAddress", param);
            }
            
            // get tcpPort
            param = this.config.getProperty(TRX_TCP_PORT);
            if (param == null || param.equalsIgnoreCase("")) {
                throw new SmppServiceException("'trx.tcpPort' is not defined when TRX is enabled!!");
            } else {
                this.trxConfig.setProperty("tcpPort", param);
            }
            
            // get esmeLabel
            param = this.config.getProperty(TRX_ESME_LABEL);
            if (param == null || param.equalsIgnoreCase("")) {
                throw new SmppServiceException("'trx.esmeLabel' is not defined when TRX is enabled!!");
            } else {
                this.trxConfig.setProperty("esmeLabel", param);
            }
            
            // get lazyWriteWait
            param = this.config.getProperty(TRX_LAZY_WRITE_WAIT);
            if (param == null || param.equalsIgnoreCase("")) {
                throw new SmppServiceException("'trx.lazyWriteWait' is not defined when TRX is enabled!!");
            } else {
                this.trxConfig.setProperty("lazyWriteWait", param);
            }
        } else {
            this.txConfig = new Properties();
            this.rxConfig = new Properties();
            
            // first txConfig...
            // get tcpAddress
            param = this.config.getProperty(TX_TCP_ADDRESS);
            if (param == null || param.equalsIgnoreCase("")) {
                throw new SmppServiceException("'tx.tcpAddress' is not defined when TRX is disabled!!");
            } else {
                this.txConfig.setProperty("tcpAddress", param);
            }
            
            // get tcpPort
            param = this.config.getProperty(TX_TCP_PORT);
            if (param == null || param.equalsIgnoreCase("")) {
                throw new SmppServiceException("'tx.tcpPort' is not defined when TRX is disabled!!");
            } else {
                this.txConfig.setProperty("tcpPort", param);
            }
            
            // get esmeLabel
            param = this.config.getProperty(TX_ESME_LABEL);
            if (param == null || param.equalsIgnoreCase("")) {
                throw new SmppServiceException("'tx.esmeLabel' is not defined when TRX is disabled!!");
            } else {
                this.txConfig.setProperty("esmeLabel", param);
            }
            
            // get lazyWriteWait
            param = this.config.getProperty(TX_LAZY_WRITE_WAIT);
            if (param == null || param.equalsIgnoreCase("")) {
                throw new SmppServiceException("'tx.lazyWriteWait' is not defined when TRX is disabled!!");
            } else {
                this.txConfig.setProperty("lazyWriteWait", param);
            }
            
            // then rxConfig...
            // get tcpAddress
            param = this.config.getProperty(RX_TCP_ADDRESS);
            if (param == null || param.equalsIgnoreCase("")) {
                throw new SmppServiceException("'rx.tcpAddress' is not defined when TRX is disabled!!");
            } else {
                this.rxConfig.setProperty("tcpAddress", param);
            }
            
            // get tcpPort
            param = this.config.getProperty(RX_TCP_PORT);
            if (param == null || param.equalsIgnoreCase("")) {
                throw new SmppServiceException("'rx.tcpPort' is not defined when TRX is disabled!!");
            } else {
                this.rxConfig.setProperty("tcpPort", param);
            }
            
            // get esmeLabel
            param = this.config.getProperty(RX_ESME_LABEL);
            if (param == null || param.equalsIgnoreCase("")) {
                throw new SmppServiceException("'esmeLabel' is not defined when TRX is disabled!!");
            } else {
                if (!this.txConfig.getProperty("esmeLabel").equals(param))
                    throw new SmppServiceException("'tx.esmeLabel' and 'rx.esmeLabel' are not identical for the esme configured!! Check ESMELabel: " + param);
                this.rxConfig.setProperty("esmeLabel", param);
            }
            
            // get lazyWriteWait
            param = this.config.getProperty(RX_LAZY_WRITE_WAIT);
            if (param == null || param.equalsIgnoreCase("")) {
                throw new SmppServiceException("'rx.lazyWriteWait' is not defined when TRX is disabled!!");
            } else {
                this.rxConfig.setProperty("lazyWriteWait", param);
            }
        }
        
        // check thread pool size...
        param = this.config.getProperty(THREADPOOL_SIZE);
        if (param == null || param.equalsIgnoreCase("")) {
            throw new SmppServiceException("'threadPoolSize' is not defined or empty!!");
        }
        
        try {
            int size = java.lang.Integer.parseInt(param);
            if (this.canUseTrx) {
                this.trxConfig.setProperty("threadPoolSize", param);
            } else {
                this.txConfig.setProperty("threadPoolSize", param);
                this.rxConfig.setProperty("threadPoolSize", param);
            }
        } catch (Exception e) {
            if (this.isEnquireLinkEnabled)
                throw new SmppServiceException(
                        "'isEnquireLinkEnabled' is enabled but 'enquireLinkPeriod' is not numeric!! Found: " + param, e);
        }
        

    }
    
    private void bindTransceiver() throws SmppTransportException, SmppServiceException {
        SmppPdu bindTrx = null;
        try {
            bindTrx = EsmeHelper.getBindTransceiver(this.username, this.password, this.systemType, this.interfaceVersion, this.esmeTon, this.esmeNpi, this.addressRange);
            LogService.stackTraceLog.trace("Encoded Write - " + bindTrx.toString());
            this.trxWriter.writeImmediate(bindTrx);
            StackMap.addMessageIndex("" + bindTrx.getCommandSequence().getValue(), this.getEsmeLabel());
            StackMap.addSession(this.getEsmeLabel(), this);
            LogService.appLog.debug("Added Session to StackMap with key: " + this.getEsmeLabel());
        } catch (SmppCodecException e) {
            // TODO Log for troubleshooting
        	LogService.stackTraceLog.debug("Esme-bindTransceiver:Encountered exception:AddressRange:"+this.addressRange+"CommandSequence:"+bindTrx.getCommandSequence().getValue(),e);
            this.trxChannel.setConnectionState(SmppSessionState.CLOSED);
            StackMap.removeMessageIndex("" + bindTrx.getCommandSequence().getValue());
            throw new SmppServiceException("Unable to request Bind TRX!", e);
        } catch (SmppTransportException e) {
            // TODO Log for troubleshooting
        	LogService.stackTraceLog.debug("Esme-bindTransceiver:Encountered exception:AddressRange:"+this.addressRange+"CommandSequence:"+bindTrx.getCommandSequence().getValue(),e);
            this.trxChannel.setConnectionState(SmppSessionState.CLOSED);
            StackMap.removeMessageIndex("" + bindTrx.getCommandSequence().getValue());
            throw new SmppServiceException("Unable to request Bind TRX!", e);
        }
        
        while (this.trxChannel.getConnectionState() != SmppSessionState.BOUND_TRX || 
                this.trxChannel.getConnectionState() != SmppSessionState.CLOSED) {
            continue;
        }
        StackMap.removeMessageIndex("" + bindTrx.getCommandSequence().getValue());
        
    }
    
    private void bindTransmitter() throws SmppServiceException, SmppTransportException {
        SmppPdu bindTx = null;
        try {
            bindTx = EsmeHelper.getBindTransmitter(this.username, this.password, this.systemType, this.interfaceVersion, this.esmeTon, this.esmeNpi, this.addressRange);
            this.txWriter.writeImmediate(bindTx);
            StackMap.addMessageIndex("" + bindTx.getCommandSequence().getValue(), this.getEsmeLabel());
            StackMap.addSession(this.getEsmeLabel(), this);
            LogService.appLog.debug("Added Session to StackMap with key: " + this.getEsmeLabel());

        } catch (SmppCodecException e) {
            // TODO Log for troubleshooting
        	LogService.stackTraceLog.debug("Esme-bindTransmitter:Encountered exception:AddressRange:"+this.addressRange+"CommandSequence:"+bindTx.getCommandSequence().getValue(),e);
            this.txChannel.setConnectionState(SmppSessionState.CLOSED);
            StackMap.removeMessageIndex("" + bindTx.getCommandSequence().getValue());
            throw new SmppServiceException("Unable to request Bind TX!", e);
        } catch (SmppTransportException e) {
            // TODO Log for troubleshooting
        	LogService.stackTraceLog.debug("Esme-bindTransmitter:Encountered exception:AddressRange:"+this.addressRange+"CommandSequence:"+bindTx.getCommandSequence().getValue(),e);
            this.txChannel.setConnectionState(SmppSessionState.CLOSED);
            StackMap.removeMessageIndex("" + bindTx.getCommandSequence().getValue());
            throw new SmppServiceException("Unable to request Bind TX!", e);
        }

        while (this.txChannel.getConnectionState() != SmppSessionState.BOUND_TX || 
                this.txChannel.getConnectionState() != SmppSessionState.CLOSED) {
            continue;
        }
        StackMap.removeMessageIndex("" + bindTx.getCommandSequence().getValue());
   }
    
    private void bindReceiver() throws SmppServiceException, SmppTransportException {
        SmppPdu bindRx = null;
        try {
            bindRx = EsmeHelper.getBindReceiver(this.username, this.password, this.systemType, this.interfaceVersion, this.esmeTon, this.esmeNpi, this.addressRange);
            this.rxWriter.writeImmediate(bindRx);
            StackMap.addMessageIndex("" + bindRx.getCommandSequence().getValue(), this.getEsmeLabel());
            StackMap.addSession(this.getEsmeLabel(), this);
            LogService.appLog.debug("Added Session to StackMap with key: " + this.getEsmeLabel());
       } catch (SmppCodecException e) {
            // TODO Log for troubleshooting
    	   LogService.stackTraceLog.debug("Esme-bindReceiver:Encountered exception:AddressRange:"+this.addressRange+"CommandSequence:"+bindRx.getCommandSequence().getValue(),e);
            this.rxChannel.setConnectionState(SmppSessionState.CLOSED);
            StackMap.removeMessageIndex("" + bindRx.getCommandSequence().getValue());
            throw new SmppServiceException("Unable to request Bind RX!", e);
        } catch (SmppTransportException e) {
            // TODO Log for troubleshooting
        	LogService.stackTraceLog.debug("Esme-bindReceiver:Encountered exception:AddressRange:"+this.addressRange+"CommandSequence:"+bindRx.getCommandSequence().getValue(),e);        	
            this.rxChannel.setConnectionState(SmppSessionState.CLOSED);
            StackMap.removeMessageIndex("" + bindRx.getCommandSequence().getValue());
            throw new SmppServiceException("Unable to request Bind RX!", e);
        }

        while (this.rxChannel.getConnectionState() != SmppSessionState.BOUND_RX || 
                this.rxChannel.getConnectionState() != SmppSessionState.CLOSED) {
            continue;
        }
        StackMap.removeMessageIndex("" + bindRx.getCommandSequence().getValue());
    }
    
    private void unbindTrx() throws SmppServiceException, SmppTransportException {
        SmppPdu unbind = null;
        try {
            unbind = EsmeHelper.getUnbind();
            this.trxWriter.writeImmediate(unbind);
        } catch (SmppCodecException e) {
            // TODO Log for troubleshooting
        	LogService.stackTraceLog.debug("Esme-unbindTrx:Encountered exception:CommandSequence:"+unbind.getCommandSequence().getValue(),e);
            this.trxChannel.setConnectionState(SmppSessionState.CLOSED);
            StackMap.removeMessageIndex("" + unbind.getCommandSequence().getValue());
            throw new SmppServiceException("Unable to request Unbind TRX!", e);
        } catch (SmppTransportException e) {
            // TODO Log for troubleshooting
        	LogService.stackTraceLog.debug("Esme-unbindTrx:Encountered exception:CommandSequence:"+unbind.getCommandSequence().getValue(),e);
            this.trxChannel.setConnectionState(SmppSessionState.CLOSED);
            StackMap.removeMessageIndex("" + unbind.getCommandSequence().getValue());
            throw new SmppServiceException("Unable to request Unbind TRX!", e);
        }

        while (this.trxChannel.getConnectionState() == SmppSessionState.UNBOUND || 
                this.trxChannel.getConnectionState() == SmppSessionState.CLOSED) {
            continue;
        }
        StackMap.removeMessageIndex("" + unbind.getCommandSequence().getValue());
    }
    
    private void unbindTx() throws SmppServiceException, SmppTransportException {
        SmppPdu unbind = null;
        try {
            unbind = EsmeHelper.getUnbind();
            this.trxWriter.writeImmediate(unbind);
        } catch (SmppCodecException e) {
            // TODO Log for troubleshooting
        	LogService.stackTraceLog.debug("Esme-unbindTx:Encountered exception:CommandSequence:"+unbind.getCommandSequence().getValue(),e);
            this.txChannel.setConnectionState(SmppSessionState.CLOSED);
            StackMap.removeMessageIndex("" + unbind.getCommandSequence().getValue());
            throw new SmppServiceException("Unable to request Unbind TRX!", e);
        } catch (SmppTransportException e) {
            // TODO Log for troubleshooting
        	LogService.stackTraceLog.debug("Esme-unbindTx:Encountered exception:CommandSequence:"+unbind.getCommandSequence().getValue(),e);
            this.txChannel.setConnectionState(SmppSessionState.CLOSED);
            StackMap.removeMessageIndex("" + unbind.getCommandSequence().getValue());
            throw new SmppServiceException("Unable to request Unbind TRX!", e);
        }

        while (this.txChannel.getConnectionState() == SmppSessionState.UNBOUND || 
                this.txChannel.getConnectionState() == SmppSessionState.CLOSED) {
            continue;
        }
        StackMap.removeMessageIndex("" + unbind.getCommandSequence().getValue());
    }
    
    private void unbindRx() throws SmppServiceException, SmppTransportException {
        SmppPdu unbind = null;
        try {
            unbind = EsmeHelper.getUnbind();
            this.rxWriter.writeImmediate(unbind);
        } catch (SmppCodecException e) {
            // TODO Log for troubleshooting
        	LogService.stackTraceLog.debug("Esme-unbindRx:Encountered exception:CommandSequence:"+unbind.getCommandSequence().getValue(),e);
            this.rxChannel.setConnectionState(SmppSessionState.CLOSED);
            StackMap.removeMessageIndex("" + unbind.getCommandSequence().getValue());
            throw new SmppServiceException("Unable to request Unbind TRX!", e);
        } catch (SmppTransportException e) {
            // TODO Log for troubleshooting
        	LogService.stackTraceLog.debug("Esme-unbindRx:Encountered exception:CommandSequence:"+unbind.getCommandSequence().getValue(),e);
            this.rxChannel.setConnectionState(SmppSessionState.CLOSED);
            StackMap.removeMessageIndex("" + unbind.getCommandSequence().getValue());
            throw new SmppServiceException("Unable to request Unbind TRX!", e);
        }

        while (this.rxChannel.getConnectionState() == SmppSessionState.UNBOUND || 
                this.rxChannel.getConnectionState() == SmppSessionState.CLOSED) {
            continue;
        }
        StackMap.removeMessageIndex("" + unbind.getCommandSequence().getValue());
    }
    
    
    class EnquireLinkTask extends TimerTask {
        
        private Esme session = null;
        
        public EnquireLinkTask(Esme esme) {
            this.session = esme;
        }

        @Override
        public void run() {
            try {
                if (this.session.isCanUseTrx()) {
                    if (this.session.trxChannel.getConnectionState() == SmppSessionState.BOUND_TRX) {
                        EnquireLink enquireLinkPdu = new EnquireLink();
                        LogService.appLog.info("Sending Enquire Link for TRX session: " + session.trxChannel.getEsmeLabel() + " @" + new Date().toString());
                        this.session.sendPdu(enquireLinkPdu, ChannelMode.TRX);
                    }
                } else {
                    if (this.session.txChannel.getConnectionState() == SmppSessionState.BOUND_TX) {
                        EnquireLink enquireLinkTxPdu = new EnquireLink();
                        LogService.appLog.info("Sending Enquire Link for TX session: " + session.txChannel.getEsmeLabel() + " @" + new Date().toString());
                        this.session.sendPdu(enquireLinkTxPdu, ChannelMode.TX);
                    }

                    if (this.session.rxChannel.getConnectionState() == SmppSessionState.BOUND_RX) {
                        EnquireLink enquireLinkRxPdu = new EnquireLink();
                        LogService.appLog.info("Sending Enquire Link for RX session: " + session.rxChannel.getEsmeLabel() + " @" + new Date().toString());
                        this.session.sendPdu(enquireLinkRxPdu, ChannelMode.RX);
                    }
                }
            } catch(SmppCodecException e) {
                LogService.appLog.error("Failed attempting to encode Enquire Link for session: " + session.txChannel.getEsmeLabel() + " @" + new Date().toString());
            }
        }
    }
    
    
    public Properties getConfig() {
        return config;
    }
    
    public void setConfig(Properties config) {
        this.config = config;
    }
    
    public boolean isCanUseTrx() {
        return canUseTrx;
    }
    
    public void setCanUseTrx(boolean canUseTrx) {
        this.canUseTrx = canUseTrx;
    }
    
    public boolean isEnquireLinkEnabled() {
        return isEnquireLinkEnabled;
    }
    
    public void setEnquireLinkEnabled(boolean isEnquireLinkEnabled) {
        this.isEnquireLinkEnabled = isEnquireLinkEnabled;
    }
    
    public int getEnquireLinkPeriod() {
        return enquireLinkPeriod;
    }
    
    public void setEnquireLinkPeriod(int enquireLinkPeriod) {
        this.enquireLinkPeriod = enquireLinkPeriod;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getSystemType() {
        return systemType;
    }
    
    public void setSystemType(String systemType) {
        this.systemType = systemType;
    }
    
    public InterfaceVersion getInterfaceVersion() {
        return interfaceVersion;
    }
    
    public void setInterfaceVersion(InterfaceVersion interfaceVersion) {
        this.interfaceVersion = interfaceVersion;
    }
    
    public TypeOfNumber getEsmeTon() {
        return esmeTon;
    }
    
    public void setEsmeTon(TypeOfNumber esmeTon) {
        this.esmeTon = esmeTon;
    }
    
    public NumberingPlanIndicator getEsmeNpi() {
        return esmeNpi;
    }
    
    public void setEsmeNpi(NumberingPlanIndicator esmeNpi) {
        this.esmeNpi = esmeNpi;
    }
    
    public String getAddressRange() {
        return addressRange;
    }
    
    public void setAddressRange(String addressRange) {
        this.addressRange = addressRange;
    }
    
    public Connection getTxChannel() {
        return txChannel;
    }
    
    public Connection getRxChannel() {
        return rxChannel;
    }

    public Connection getTrxChannel() {
        return trxChannel;
    }

    public ReadHelper getTxReader() {
        return txReader;
    }

    public ReadHelper getRxReader() {
        return rxReader;
    }

    public ReadHelper getTrxReader() {
        return trxReader;
    }

    public WriteHelper getTxWriter() {
        return txWriter;
    }

    public WriteHelper getRxWriter() {
        return rxWriter;
    }

    public WriteHelper getTrxWriter() {
        return trxWriter;
    }
    
    public String getEsmeLabel() {
        if (this.trxChannel != null)
            return this.trxChannel.getEsmeLabel();
        
        if (this.rxChannel != null)
            return this.rxChannel.getEsmeLabel();
        else
            return this.txChannel.getEsmeLabel();
    }

    @Override
    public String toString() {
        return String
                .format("Esme [canUseTrx=%s, isEnquireLinkEnabled=%s, enquireLinkPeriod=%s, enquireLinkSchedule=%s, username=%s, password=%s, systemType=%s, interfaceVersion=%s, "
                        + "esmeTon=%s, esmeNpi=%s, addressRange=%s, txConfig=%s, rxConfig=%s, trxConfig=%s, trxChannel=%s, txChannel=%s, rxChannel=%s, txWriter=%s, txReader=%s, "
                        + "rxWriter=%s, rxReader=%s, trxWriter=%s, trxReader=%s, config=%s]",
                        canUseTrx,
                        isEnquireLinkEnabled,
                        enquireLinkPeriod,
                        enquireLinkSchedule,
                        username,
                        password,
                        systemType,
                        interfaceVersion,
                        esmeTon,
                        esmeNpi,
                        addressRange,
                        txConfig,
                        rxConfig,
                        trxConfig,
                        trxChannel,
                        txChannel,
                        rxChannel,
                        txWriter,
                        txReader,
                        rxWriter,
                        rxReader,
                        trxWriter,
                        trxReader,
                        config);
    }
    
    
    
}
