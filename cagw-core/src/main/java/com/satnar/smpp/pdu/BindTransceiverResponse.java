package com.satnar.smpp.pdu;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
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

public class BindTransceiverResponse extends SmppPdu {
    
    private COctetString      systemId           = (COctetString) SmppParameter.getInstance(Type.C_OCTET_STRING, "");
    private OptionalParameter scInterfaceVersion = null;
    
    private int               commandLength      = 0;
    
    public BindTransceiverResponse() {
        super.setCommandId(CommandId.BIND_TRANSCEIVER_RESP);
        super.setCommandStatus(CommandStatus.ESME_ROK);
    }
    
    @Override
    public Integer getCommandLength() {
        if (this.commandLength == 0) {
            this.commandLength = 4 + // length of command length  
                                    super.getCommandId().getLength() + 
                                    super.getCommandStatus().getLength() + 
                                    super.getCommandSequence().getLength() + 
                                    this.systemId.getLength() + 
                                    this.scInterfaceVersion.getLength();
        }

        Integer len = (Integer) SmppParameter.getInstance(Type.INTEGER);
        len.setValue(this.commandLength);
        
        return len;
    }
    
    @Override
    public byte[] encode() throws SmppCodecException {
        
        throw new SmppCodecException("Not Implemented in ESME mode!!");
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
            //TODO: sc_interface_version
        } catch (IOException e) {
            LogService.appLog.error("BindReceiverResponse-decode:Encountered exception:",e);
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
        
        // check system id
        if (this.systemId == null)
            throw new SmppCodecException("'system_id' parameter is not set!!");
        if (this.systemId.getLength() == 0x01)
            throw new SmppCodecException("'system_id' parameter is not properly initialized or set!!");
        
        
        
    }
    
    public String toString() {
        return "PDU:: Command Length: " + this.getCommandLength().getValue() + ", " 
                + CommandId.BIND_TRANSCEIVER_RESP + "(" + java.lang.Integer.toHexString(CommandId.BIND_TRANSCEIVER_RESP.getId())
                + ", Command Status: " + this.getCommandStatus()
                + ", Command Sequence: " + this.getCommandSequence().getValue()
                + ", System ID: " + this.systemId.getString();
    }
    
    public COctetString getSystemId() {
        return systemId;
    }
    
    public void setSystemId(COctetString systemId) {
        this.systemId = systemId;
    }
    
    
    
}
