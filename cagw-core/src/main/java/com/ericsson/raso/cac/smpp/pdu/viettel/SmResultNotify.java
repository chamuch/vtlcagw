package com.ericsson.raso.cac.smpp.pdu.viettel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.satnar.common.LogService;
import com.satnar.smpp.CommandId;
import com.satnar.smpp.CommandSequence;
import com.satnar.smpp.CommandStatus;
import com.satnar.smpp.codec.Byte;
import com.satnar.smpp.codec.CDecimalString;
import com.satnar.smpp.codec.CHexString;
import com.satnar.smpp.codec.Integer;
import com.satnar.smpp.codec.SmppCodecException;
import com.satnar.smpp.codec.SmppParameter;
import com.satnar.smpp.codec.SmppParameter.Type;
import com.satnar.smpp.pdu.SmppPdu;
import com.satnar.smpp.transport.SmppSessionState;

public class SmResultNotify extends SmppPdu {

	private WinVersion version = null;
	private CDecimalString smscAddress = null;
	private Byte moMscNoa = null;
	private Byte moMscNpi = null;
	private CDecimalString moMscAddress = null;
	private CDecimalString sourceAddress = null;
	private CDecimalString destinationAddress = null;
	private WinMoMtFlag moMtFlag = null;
	private CHexString smId = null;
	private Integer smLength = null;
	private Integer finalState = null;
	private Integer serviceId = null;
	
	
	private int myCommandLength = 0;
	
	public SmResultNotify() {
		CommandId commandId = CommandId.EXTENDED;
		commandId.setId(0x01000002);
		commandId.setEsmeInitiated(false);
		commandId.setSmscInitiated(true);
		commandId.setRxCompatible(true);
		commandId.setTxCompatible(false);
		
		List<SmppSessionState> states = new ArrayList<>();
		states.add(SmppSessionState.BOUND_RX);
		states.add(SmppSessionState.BOUND_TRX);
		commandId.setRequiredStates(states);
	    super.setCommandId(commandId);
	    super.setCommandStatus(CommandStatus.ESME_ROK);
    }
	
	@Override
	public byte[] encode() throws SmppCodecException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutputStream encoder = new DataOutputStream(buffer);
		
		try {
			encoder.writeInt(this.getCommandLength().getValue());
			encoder.writeInt(super.getCommandId().getId());
			encoder.writeInt(super.getCommandStatus().getCode());
			encoder.writeInt(super.getCommandSequence().getValue());
			
			encoder.write(this.version.getValue().encode());
			encoder.write(this.smscAddress.encode());
			encoder.write(this.moMscNoa.encode());
			encoder.write(this.moMscNpi.encode());
			encoder.write(this.moMscAddress.encode());
			encoder.write(this.moMtFlag.getValue().encode());
			encoder.write(this.smId.encode());
			encoder.writeInt(this.smLength.getValue());
			encoder.writeInt(this.finalState.getValue());
			encoder.writeInt(this.serviceId.getValue());

			encoder.close();
			encoder = null;
			LogService.appLog.info("SmResultNotify-encode:Success:SMId:"+this.smId+":SMSCAddress:"+this.smscAddress+":ServiceId:"+this.serviceId);
		} catch (IOException e) {
            // TODO Log for troubleshooting
			LogService.appLog.debug("SmResultNotify-encode:Failed to serialize pdu:"+this.smId+":SMSCAddress:"+this.smscAddress+":ServiceId:"+this.serviceId,e);
            throw new SmppCodecException("Failed to serialize pdu", e);
        }
		
		return buffer.toByteArray();
	}

	@Override
	public void decode(byte[] payload) throws SmppCodecException {
		try {
			ByteArrayInputStream buffer = new ByteArrayInputStream(payload);
			DataInputStream parser = new DataInputStream(buffer);
			
			super.setCommandStatus(CommandStatus.valueOf(parser.readInt()));
			CommandSequence sequence = CommandSequence.getInstance();
			sequence.setValue(parser.readInt());
			super.setCommandSequence(sequence);
			
			this.version = WinVersion.valueOf(parser.read());
			this.smscAddress = CDecimalString.readString(parser);
			this.moMscNoa = (Byte) SmppParameter.getInstance(Type.BYTE, parser.read());
			this.moMscNpi = (Byte) SmppParameter.getInstance(Type.BYTE, parser.read());
			this.moMscAddress = CDecimalString.readString(parser);
			this.moMtFlag = WinMoMtFlag.valueOf(parser.read());
			this.smId = CHexString.readString(parser);
			this.smLength = (Integer) SmppParameter.getInstance(Type.INTEGER, parser.readInt());
			this.finalState = (Integer) SmppParameter.getInstance(Type.INTEGER, parser.readInt());
			this.serviceId = (Integer) SmppParameter.getInstance(Type.INTEGER, parser.readInt());
			
			parser.close();
			parser = null;
			LogService.appLog.info("SmResultNotify-decode:Success:SMId:"+this.smId+":SMSCAddress:"+this.smscAddress+":ServiceId:"+this.serviceId);
		} catch (IOException e) {
            // TODO Log for troubleshooting
			LogService.appLog.debug("SmResultNotify-decode:Failed to serialize pdu:"+this.smId+":SMSCAddress:"+this.smscAddress+":ServiceId:"+this.serviceId,e);
            throw new SmppCodecException("Failed to serialize pdu", e);
        }
	}

	@Override
	public void validate() throws SmppCodecException {
		// TODO do nothing...we dont know anything about this chinese shit!!

	}

	@Override
    public Integer getCommandLength() {
		if (this.myCommandLength == 0) {
			this.myCommandLength = 4 + // length of command length
									super.getCommandId().getLength() +
									super.getCommandStatus().getLength() +
									super.getCommandSequence().getLength() +
									this.version.getValue().getLength() +
									this.smscAddress.getLength() +
									this.moMscNoa.getLength() +
									this.moMscNpi.getLength() +
									this.moMscAddress.getLength() +
									this.sourceAddress.getLength() +
									this.destinationAddress.getLength() +
									this.moMtFlag.getValue().getLength() +
									this.smId.getLength() +
									this.smLength.getLength() +
									this.finalState.getLength() +
									this.serviceId.getLength();
		}
	    
        Integer len  = (Integer) SmppParameter.getInstance(Type.INTEGER);
        len.setValue(this.myCommandLength);

        return len;
    }
	
	public String toString() {
        return "PDU:: Command Length: " + this.getCommandLength().getValue() + ", " 
                + CommandId.SM_RESULT_NOTIFY + "(" + java.lang.Integer.toHexString(CommandId.SM_RESULT_NOTIFY.getId())
                + "), Command Status: " + this.getCommandStatus()
                + ", Command Sequence: " + this.getCommandSequence().getValue()
                + ", Version: " + this.version.getValue().getValue()
                + ", SMSC Address: " + this.smscAddress.getString()
                + ", MO MSC NOA: " + this.moMscNoa.getValue()
                + ", MO MSC NPI: " + this.moMscNpi.getValue()
                + ", MSCAddress: " + this.moMscAddress.getString()
                + ", MO MT Flag: " + this.moMtFlag.getValue()
                + ", SM ID: " + this.smId.getString()
                + ", SM Length: " + this.smLength.getValue()
                + ", Final State: " + this.finalState.getValue()
                + ", Service ID: " + this.serviceId.getValue()
                
                ;
    }
	

	public WinVersion getVersion() {
		return version;
	}

	public void setVersion(WinVersion version) {
		this.version = version;
	}

	public CDecimalString getSmscAddress() {
		return smscAddress;
	}

	public void setSmscAddress(CDecimalString smscAddress) {
		this.smscAddress = smscAddress;
	}

	public Byte getMoMscNoa() {
		return moMscNoa;
	}

	public void setMoMscNoa(Byte moMscNoa) {
		this.moMscNoa = moMscNoa;
	}

	public Byte getMoMscNpi() {
		return moMscNpi;
	}

	public void setMoMscNpi(Byte moMscNpi) {
		this.moMscNpi = moMscNpi;
	}

	public CDecimalString getMoMscAddress() {
		return moMscAddress;
	}

	public void setMoMscAddress(CDecimalString moMscAddress) {
		this.moMscAddress = moMscAddress;
	}

	public CDecimalString getSourceAddress() {
		return sourceAddress;
	}

	public void setSourceAddress(CDecimalString sourceAddress) {
		this.sourceAddress = sourceAddress;
	}

	public CDecimalString getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationAddress(CDecimalString destinationAddress) {
		this.destinationAddress = destinationAddress;
	}

	public WinMoMtFlag getMoMtFlag() {
		return moMtFlag;
	}

	public void setMoMtFlag(WinMoMtFlag moMtFlag) {
		this.moMtFlag = moMtFlag;
	}

	public CHexString getSmId() {
		return smId;
	}

	public void setSmId(CHexString smId) {
		this.smId = smId;
	}

	public Integer getSmLength() {
		return smLength;
	}

	public void setSmLength(Integer smLength) {
		this.smLength = smLength;
	}

	public Integer getFinalState() {
		return finalState;
	}

	public void setFinalState(Integer finalState) {
		this.finalState = finalState;
	}

	public Integer getServiceId() {
		return serviceId;
	}

	public void setServiceId(Integer serviceId) {
		this.serviceId = serviceId;
	}
	
	

}
