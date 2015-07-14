package com.satnar.smpp.pdu;

import java.nio.ByteBuffer;

import com.satnar.smpp.CommandId;
import com.satnar.smpp.CommandSequence;
import com.satnar.smpp.CommandStatus;
import com.satnar.smpp.codec.Integer;
import com.satnar.smpp.codec.SmppCodecException;

public abstract class SmppPdu {
    
    private Integer commandLength = null;
    private CommandId commandId = null;
    private CommandStatus commandStatus = null;
    private CommandSequence commandSequence = null;
    
    
    
    public abstract byte[] encode() throws SmppCodecException;

    public abstract void decode(byte[] payload) throws SmppCodecException;
    
    public abstract void validate() throws SmppCodecException;
    
    
    public abstract Integer getCommandLength();

    
    public void setCommandLength(Integer commandLength) {
        this.commandLength = commandLength;
    }


    public CommandId getCommandId() {
        return commandId;
    }


    public void setCommandId(CommandId commandId) {
        this.commandId = commandId;
    }


    public CommandStatus getCommandStatus() {
        return commandStatus;
    }


    public void setCommandStatus(CommandStatus commandStatus) {
        this.commandStatus = commandStatus;
    }


    public CommandSequence getCommandSequence() {
        return commandSequence;
    }


    public void setCommandSequence(CommandSequence commandSequence) {
        this.commandSequence = commandSequence;
    }




        
    
}
