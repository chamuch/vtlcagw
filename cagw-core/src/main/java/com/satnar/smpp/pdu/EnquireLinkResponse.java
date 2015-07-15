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
import com.satnar.smpp.codec.Integer;
import com.satnar.smpp.codec.SmppCodecException;
import com.satnar.smpp.codec.SmppParameter;
import com.satnar.smpp.codec.SmppParameter.Type;

public class EnquireLinkResponse extends SmppPdu {
    
    public EnquireLinkResponse() {
        Integer length = (Integer) SmppParameter.getInstance(Type.INTEGER);
        length.setValue(16);
        this.setCommandLength(length);
        this.setCommandId(CommandId.ENQUIRE_LINK_RESP);
        this.setCommandStatus(CommandStatus.ESME_ROK);
    }
    
    
    @Override
    public byte[] encode() throws SmppCodecException {
        try {
            ByteArrayOutputStream baosSerialized = new ByteArrayOutputStream();
            DataOutputStream encoder = new DataOutputStream(baosSerialized);
            
            encoder.writeInt(this.getCommandLength().getValue());
            encoder.writeInt(this.getCommandId().getId());
            encoder.writeInt(this.getCommandStatus().getCode());
            encoder.writeInt(this.getCommandSequence().getValue());
            
            encoder.close();
            encoder = null;
            return baosSerialized.toByteArray();
        } catch (IOException e) {
            //TODO: Log for troubleshooting
        	LogService.appLog.debug("EnquireLinkResponse-encode: Failed to serialize pdu. ",e);
            throw new SmppCodecException("Failed to serialize pdu!!", e);
        }
    }
    
    @Override
    public void decode(byte[] payload) throws SmppCodecException {
        try {
            ByteArrayInputStream raw = new ByteArrayInputStream(payload);
            DataInputStream parser = new DataInputStream(raw);
            
            this.setCommandStatus(CommandStatus.valueOf(parser.readInt()));
            
            CommandSequence sequence = CommandSequence.getInstance();
            sequence.setValue(parser.readInt());
            this.setCommandSequence(sequence);
            
            // sanity purposes only
            Integer commandLength = (Integer) SmppParameter.getInstance(Type.INTEGER);
            commandLength.setValue(16);
            this.setCommandLength(commandLength);
        } catch (IOException e) {
            //TODO: log for troubleshooting. Ideally wont happen
        	LogService.appLog.debug("EnquireLinkResponse-decode: Failed to decode pdu. ",e);
            throw new SmppCodecException("Failed to decode pdu!!", e);
        }
        
    }
    
    @Override
    public void validate() throws SmppCodecException {
        // check if all mandatory header are initialized
        if (this.getCommandLength() == null)
            throw new SmppCodecException("'command_length' is not set!!");
        
        if (this.getCommandId() == null)
            throw new SmppCodecException("'command_id' is not set!!");
        
        if (this.getCommandStatus() == null)
            throw new SmppCodecException("'command_status' is not set!!");
        
        if (this.getCommandSequence() == null)
            throw new SmppCodecException("'command_sequence' is not set!!");
        
    }

    @Override
    public Integer getCommandLength() {
        Integer length = (Integer) SmppParameter.getInstance(Type.INTEGER);
        length.setValue(16);
        return length;
    }


    @Override
    public String toString() {
        return "EnquireLinkResponse [getCommandId()=" + getCommandId() + ", getCommandLength()=" + getCommandLength() + ", getCommandStatus()=" + getCommandStatus()
                + ", getCommandSequence()=" + getCommandSequence() + "]";
    }
    
    
    
}
