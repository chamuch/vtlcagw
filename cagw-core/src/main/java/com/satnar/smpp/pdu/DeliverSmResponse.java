package com.satnar.smpp.pdu;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.satnar.common.LogService;
import com.satnar.smpp.CommandId;
import com.satnar.smpp.CommandSequence;
import com.satnar.smpp.CommandStatus;
import com.satnar.smpp.OptionalParameter;
import com.satnar.smpp.codec.COctetString;
import com.satnar.smpp.codec.Integer;
import com.satnar.smpp.codec.SmppCodecException;
import com.satnar.smpp.codec.SmppParameter;
import com.satnar.smpp.codec.SmppParameter.Type;

public class DeliverSmResponse extends SmppPdu {
    
    private COctetString      messageId           = null;
    
    private int               commandLength      = 0;
    
    public DeliverSmResponse() {
        super.setCommandId(CommandId.DATA_SM_RESP);
        super.setCommandStatus(CommandStatus.ESME_ROK);
    }
    
    @Override
    public Integer getCommandLength() {
        if (this.commandLength == 0) {
            this.commandLength = 4 + // length of command length  
                                    super.getCommandId().getLength() + 
                                    super.getCommandStatus().getLength() + 
                                    super.getCommandSequence().getLength() + 
                                    this.messageId.getLength();
        }

        Integer len = (Integer) SmppParameter.getInstance(Type.INTEGER);
        len.setValue(this.commandLength);
        
        return len;
    }
    
    @Override
    public byte[] encode() throws SmppCodecException {
        this.validate();
        
        ByteArrayOutputStream baosSerialized = new ByteArrayOutputStream();
        DataOutputStream encoder = new DataOutputStream(baosSerialized);
        
        try {
            // Headers - Command Length, Command Id, Command Status, Command sequence
            
            encoder.writeInt(this.getCommandLength().getValue());
            encoder.writeInt(this.getCommandId().getId());
            encoder.writeInt(this.getCommandStatus().getCode());
            encoder.writeInt(this.getCommandSequence().getValue());
            encoder.write(this.getMessageId().encode());
            
            encoder.close();
            encoder = null;
            return baosSerialized.toByteArray();
        } catch (IOException e) {
            // TODO Log for troubleshooting
        	LogService.appLog.debug("DeliverSmResponse-encode: Failed to serialize pdu. ",e);
            throw new SmppCodecException("Failed to serialize pdu", e);
        }
    }
    
    @Override
    public void decode(byte[] payload) throws SmppCodecException {
        ByteArrayInputStream baisRawStream = new ByteArrayInputStream(payload);
        DataInputStream parser = new DataInputStream(baisRawStream);
        
        try {
            super.setCommandStatus(CommandStatus.valueOf(parser.readInt()));
            CommandSequence sequence = CommandSequence.getInstance();
            sequence.setValue(parser.readInt());
            super.setCommandSequence(sequence);
            //TODO: system_id
        } catch (IOException e) {
            //TODO: Log situation
        	LogService.appLog.debug("DeliverSmResponse-decode: Decode Failure with underlying streams!! ",e);
            throw new SmppCodecException("Decode Failure with underlying streams!!", e);
        }
        
    }
    
    @Override
    public void validate() throws SmppCodecException {
        // check command sequence
        if (super.getCommandSequence() == null)
            throw new SmppCodecException("'command_sequence' parameter is not set!!");
        if (super.getCommandSequence().getValue() == 0x00000000)
            throw new SmppCodecException("'command_sequence' parameter is not properly initialized or set!!");
        
        // check message id
        if (this.messageId == null)
            throw new SmppCodecException("'message_id' parameter is not set!!");
        
        
    }

    public COctetString getMessageId() {
        return messageId;
    }

    public void setMessageId(COctetString messageId) {
        this.messageId = messageId;
    }
    
    
    
}
