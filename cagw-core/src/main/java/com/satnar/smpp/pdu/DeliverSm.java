package com.satnar.smpp.pdu;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.satnar.common.LogService;
import com.satnar.smpp.CommandId;
import com.satnar.smpp.CommandStatus;
import com.satnar.smpp.EsmClass;
import com.satnar.smpp.InterfaceVersion;
import com.satnar.smpp.NumberingPlanIndicator;
import com.satnar.smpp.OptionalParameter;
import com.satnar.smpp.TypeOfNumber;
import com.satnar.smpp.codec.Byte;
import com.satnar.smpp.codec.COctetString;
import com.satnar.smpp.codec.Integer;
import com.satnar.smpp.codec.OctetString;
import com.satnar.smpp.codec.SmppCodecException;
import com.satnar.smpp.codec.SmppParameter;
import com.satnar.smpp.codec.SmppParameter.Type;

public class DeliverSm extends SmppPdu {
    
    private COctetString           serviceType           = null;
    private TypeOfNumber           sourceAddressTon      = null;
    private NumberingPlanIndicator sourceAddressNpi      = null;
    private COctetString           sourceAddress         = null;
    private TypeOfNumber           destinationAddressTon = null;
    private NumberingPlanIndicator destinationAddressNpi = null;
    private COctetString           destinationAddress    = null;
    private EsmClass               esmClass              = null;
    private Byte                   protocolId            = null;
    private Byte                   priorityFlag          = null;
    private COctetString           scheduledDeliveryTime = null;
    private COctetString           validityPeriod        = null;
    private Byte                   registeredDelivery    = null;
    private Byte                   replaceIfPresentFlag  = null;
    private Byte                   dataCoding            = null;
    private Byte                   smDefaultMsgId        = null;
    private Byte                   smLength              = null;
    private OctetString            shortMessage          = null;
    
    private OptionalParameter      userMessageReference  = null;
    private OptionalParameter      sourcePort            = null;
    private OptionalParameter      destinationPort       = null;
    private OptionalParameter      sarMsgRefNum          = null;
    private OptionalParameter      sarTotalSegments      = null;
    private OptionalParameter      sarSegmentSeqNum      = null;
    private OptionalParameter      userResponseCode      = null;
    private OptionalParameter      privacyIndicator      = null;
    private OptionalParameter      payloadType           = null;
    private OptionalParameter      messagePayload        = null;
    private OptionalParameter      callbackNum           = null;
    private OptionalParameter      sourceSubAddress      = null;
    private OptionalParameter      destSubAddress        = null;
    private OptionalParameter      languageIndicator     = null;
    private OptionalParameter      itsSessionInfo        = null;
    private OptionalParameter      networkErrorCode      = null;
    private OptionalParameter      messageState          = null;
    private OptionalParameter      receiptedMessageId    = null;
    
    
    private int                    commandLength         = 0;
    
    public DeliverSm() {
        super.setCommandId(CommandId.BIND_TRANSMITTER);
        super.setCommandStatus(CommandStatus.ESME_ROK);
    }
    
    @Override
    public Integer getCommandLength() {
        if (this.commandLength == 0) {
            this.commandLength = 4 + // length of command length  
                                    super.getCommandId().getLength() + 
                                    super.getCommandStatus().getLength() + 
                                    super.getCommandSequence().getLength() + 
                                    this.serviceType.getLength(); 
            //TODO: perform correct calculation when there is a real need.
        }

        Integer len = (Integer) SmppParameter.getInstance(Type.INTEGER);
        len.setValue(this.commandLength);
        
        return len;
    }
    
    @Override
    public byte[] encode() throws SmppCodecException {
        
        this.validate();
        
        ByteArrayOutputStream baosBuffer = new ByteArrayOutputStream();
        DataOutputStream writeBuffer = new DataOutputStream(baosBuffer);
        
        try {
            // Headers - Command Length, Command Id, Command Status, Command
            // Sequence
            writeBuffer.writeInt(this.getCommandLength().getValue());
            writeBuffer.writeInt(super.getCommandId().getId());
            writeBuffer.writeInt(super.getCommandStatus().getCode());
            writeBuffer.writeInt(super.getCommandSequence().getValue());
            
            // Body - refer specs
            
            writeBuffer.close();
            writeBuffer = null;
            return baosBuffer.toByteArray();
        } catch (IOException e) {
            // TODO Log for troubleshooting
        	LogService.appLog.debug("DeliverSm-encode: Failed to serialize pdu. ",e);
            throw new SmppCodecException("Failed to serialize pdu", e);
        }
    }
    
    @Override
    public void decode(byte[] payload) throws SmppCodecException {
        throw new SmppCodecException("This PDU type is not implemented (ESME mode)");
        
    }
    
    @Override
    public void validate() throws SmppCodecException {
        // check command sequence
        if (super.getCommandSequence() == null)
            throw new SmppCodecException("'command_sequence' parameter is not set!!");
        if (super.getCommandSequence().getValue() == 0x00000000)
            throw new SmppCodecException("'command_sequence' parameter is not properly initialized or set!!");
        
        // check system id
        if (this.serviceType == null)
            throw new SmppCodecException("'system_id' parameter is not set!!");
        if (this.serviceType.getLength() == 0x00000001)
            throw new SmppCodecException("'system_id' parameter is not properly initialized or set!!");
        
        
    }
    
    public COctetString getSystemId() {
        return serviceType;
    }
    
    public void setSystemId(COctetString systemId) {
        this.serviceType = systemId;
    }
    
        
}
