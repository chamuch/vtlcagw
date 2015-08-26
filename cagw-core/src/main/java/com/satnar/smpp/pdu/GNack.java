package com.satnar.smpp.pdu;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.satnar.common.LogService;
import com.satnar.smpp.CommandId;
import com.satnar.smpp.codec.Integer;
import com.satnar.smpp.codec.SmppCodecException;
import com.satnar.smpp.codec.SmppParameter;
import com.satnar.smpp.codec.SmppParameter.Type;

public class GNack extends SmppPdu {
    
    private int commandLength = 0;

    public GNack() {
        this.setCommandId(CommandId.GENERIC_NACK);
    }
    
    @Override
    public Integer getCommandLength() {
        if (this.commandLength == 0) {
            this.commandLength = 4 + // length of command length  
                                    this.getCommandId().getLength() + 
                                    this.getCommandStatus().getLength() + 
                                    this.getCommandSequence().getLength();
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
            writeBuffer.writeInt(getCommandLength().getValue());
            writeBuffer.writeInt(getCommandId().getId());
            writeBuffer.writeInt(getCommandStatus().getCode());
            writeBuffer.writeInt(getCommandSequence().getValue());

            // Body - empty
           

            writeBuffer.close();
            writeBuffer = null;
            return baosBuffer.toByteArray();
        } catch (IOException e) {
            LogService.appLog.error("GNack-encode: Failed to serialize pdu. ",e);
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
        if (getCommandSequence() == null)
            throw new SmppCodecException("'command_sequence' parameter is not set!!");
        if (getCommandSequence().getValue() == 0x00000000)
            throw new SmppCodecException("'command_sequence' parameter is not properly initialized or set!!");
        
    }

    @Override
    public String toString() {
        return String.format("GNack [commandLength=%s, commandId()=%s, commandStatus()=%s, commandSequence()=%s]",
                getCommandLength(),
                getCommandId(),
                getCommandStatus(),
                getCommandSequence());
    }
    
    
    
}
