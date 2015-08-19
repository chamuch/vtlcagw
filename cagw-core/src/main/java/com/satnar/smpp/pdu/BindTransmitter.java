package com.satnar.smpp.pdu;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.satnar.common.LogService;
import com.satnar.smpp.CommandId;
import com.satnar.smpp.CommandStatus;
import com.satnar.smpp.InterfaceVersion;
import com.satnar.smpp.NumberingPlanIndicator;
import com.satnar.smpp.TypeOfNumber;
import com.satnar.smpp.codec.COctetString;
import com.satnar.smpp.codec.Integer;
import com.satnar.smpp.codec.SmppCodecException;
import com.satnar.smpp.codec.SmppParameter;
import com.satnar.smpp.codec.SmppParameter.Type;

public class BindTransmitter extends SmppPdu {
    
    private COctetString           systemId         = null;
    private COctetString           password         = null;
    private COctetString           systemType       = null;
    private InterfaceVersion       interfaceVersion = null;
    private TypeOfNumber           addressTon       = null;
    private NumberingPlanIndicator addressNpi       = null;
    private COctetString           addressRange     = null;
    private int                    commandLength    = 0;
    
    public BindTransmitter() {
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
                                    this.systemId.getLength() + 
                                    this.password.getLength() + 
                                    this.systemType.getLength() + 
                                    this.interfaceVersion.getLength() + 
                                    this.addressTon.getLength() + 
                                    this.addressNpi.getLength() + 
                                    this.addressRange.getLength();
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
            
            // Body - systemId, password, systemType, interfaceVersion, addressTon,
            // addressNpi, addressRange
            writeBuffer.write(this.systemId.encode());
            writeBuffer.write(this.password.encode());
            writeBuffer.write(this.systemType.encode());
            writeBuffer.writeByte(this.interfaceVersion.getValue());
            writeBuffer.writeByte(this.addressTon.getValue());
            writeBuffer.writeByte(this.addressNpi.getValue());
            writeBuffer.write(this.addressRange.encode());
            
            writeBuffer.close();
            writeBuffer = null;
            return baosBuffer.toByteArray();
        } catch (IOException e) {
        	LogService.appLog.debug("BindTransmitter-encode: Failed to serialize pdu. AddressRange:"+this.addressRange,e);
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
        if (this.systemId == null)
            throw new SmppCodecException("'system_id' parameter is not set!!");
        if (this.systemId.getLength() == 0x00000001)
            throw new SmppCodecException("'system_id' parameter is not properly initialized or set!!");
        
        // check password
        if (this.password == null)
            throw new SmppCodecException("'password' parameter is not set!!");
        if (this.password.getLength() == 0x00000001)
            throw new SmppCodecException("'password' parameter is not properly initialized or set!!");
        
        // check system type
        if (this.systemType == null)
            throw new SmppCodecException("'system_type' parameter is not set!!");
        if (this.systemType.getLength() == 0x00000001)
            throw new SmppCodecException("'system_type' parameter is not properly initialized or set!!");
        
        // check interface version
        if (super.getCommandSequence() == null)
            throw new SmppCodecException("'interface_version' parameter is not set!!");
        if (super.getCommandSequence().getValue() == 0x00000000)
            throw new SmppCodecException("'interface_version' parameter is not properly initialized or set!!");
        
        // check address ton
        if (this.addressTon == null)
            throw new SmppCodecException("'addr_ton' parameter is not set!!");
        
        // check address npi
        if (this.addressNpi == null)
            throw new SmppCodecException("'addr_npi' parameter is not set!!");
        
        // check
        if (this.systemType == null) {
            LogService.appLog.warn("BindTransmitter-validate: SystemType is null AddressNpi:"+this.addressNpi);
        }
        if (this.systemType.getLength() == 0x00000001) {
            LogService.appLog.warn("BindTransmitter-validate: SystemType is empty AddressNpi:"+this.addressNpi);
        }
        
    }
    
    public COctetString getSystemId() {
        return systemId;
    }
    
    public void setSystemId(COctetString systemId) {
        this.systemId = systemId;
    }
    
    public COctetString getPassword() {
        return password;
    }
    
    public void setPassword(COctetString password) {
        this.password = password;
    }
    
    public COctetString getSystemType() {
        return systemType;
    }
    
    public void setSystemType(COctetString systemType) {
        this.systemType = systemType;
    }
    
    public InterfaceVersion getInterfaceVersion() {
        return interfaceVersion;
    }
    
    public void setInterfaceVersion(InterfaceVersion interfaceVersion) {
        this.interfaceVersion = interfaceVersion;
    }
    
    public TypeOfNumber getAddressTon() {
        return addressTon;
    }
    
    public void setAddressTon(TypeOfNumber addressTon) {
        this.addressTon = addressTon;
    }
    
    public NumberingPlanIndicator getAddressNpi() {
        return addressNpi;
    }
    
    public void setAddressNpi(NumberingPlanIndicator addressNpi) {
        this.addressNpi = addressNpi;
    }
    
    public COctetString getAddressRange() {
        return addressRange;
    }
    
    public void setAddressRange(COctetString addressRange) {
        this.addressRange = addressRange;
    }
    
}
