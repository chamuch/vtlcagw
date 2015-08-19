package com.satnar.smpp.pdu;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.satnar.common.LogService;
import com.satnar.smpp.CommandId;
import com.satnar.smpp.CommandStatus;
import com.satnar.smpp.codec.Integer;
import com.satnar.smpp.codec.SmppCodecException;
import com.satnar.smpp.codec.SmppParameter;
import com.satnar.smpp.codec.SmppParameter.Type;

public class Unbind extends SmppPdu {
    
    private int commandLength = 0;

    public Unbind() {
        super.setCommandId(CommandId.UNBIND);
        super.setCommandStatus(CommandStatus.ESME_ROK);
    }
    
    @Override
    public Integer getCommandLength() {
        if (this.commandLength == 0) {
            this.commandLength = 4 + // length of command length  
                                    super.getCommandId().getLength() + 
                                    super.getCommandStatus().getLength() + 
                                    super.getCommandSequence().getLength();
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
            // Headers - Command Length, Command Id, Command Status, Command Sequence
            writeBuffer.writeInt(this.getCommandLength().getValue());
            writeBuffer.writeInt(super.getCommandId().getId());
            writeBuffer.writeInt(super.getCommandStatus().getCode());
            writeBuffer.writeInt(super.getCommandSequence().getValue());

            // Body - empty
           

            writeBuffer.close();
            writeBuffer = null;
            return baosBuffer.toByteArray();
        } catch (IOException e) {
        	LogService.appLog.error("BindTranceceiver-encode: Failed to serialize pdu.");
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
        
    }
    
    
}
