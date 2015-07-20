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
import com.satnar.smpp.codec.CDecimalString;
import com.satnar.smpp.codec.CHexString;
import com.satnar.smpp.codec.Integer;
import com.satnar.smpp.codec.Byte;
import com.satnar.smpp.codec.SmppCodecException;
import com.satnar.smpp.codec.SmppParameter;
import com.satnar.smpp.codec.SmppParameter.Type;
import com.satnar.smpp.pdu.SmppPdu;
import com.satnar.smpp.transport.SmppSessionState;


public class AuthAcc extends SmppPdu {
	
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
	private Integer serviceId = null;
	
	private int myCommandLength = 0;
	
	
	
	public AuthAcc() {
		CommandId commandId = CommandId.EXTENDED;
		commandId.setId(0x01000001);
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
									((this.serviceId != null)?this.serviceId.getLength():0);
		}
	    
        return (Integer) SmppParameter.getInstance(Type.INTEGER, this.myCommandLength);
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
			encoder.writeInt(this.serviceId.getValue());

			encoder.close();
			encoder = null;
			
			LogService.appLog.info("AuthAcc-encode:Success:SMId:"+this.smId+":SMSCAddress:"+this.smscAddress+":ServiceId:"+this.serviceId);
		} catch (IOException e) {
            // TODO Log for troubleshooting
			LogService.appLog.debug("AuthAcc-encode:Failed to serialize pdu:"+this.smId+":SMSCAddress:"+this.smscAddress+":ServiceId:"+this.serviceId,e);
            throw new SmppCodecException("Failed to serialize pdu", e);
        }
		
		return buffer.toByteArray();
	}

	@Override
	public void decode(byte[] payload) throws SmppCodecException {
		try {
			LogService.appLog.info("AuthAcc-decode:Entered");
			ByteArrayInputStream buffer = new ByteArrayInputStream(payload);
			DataInputStream parser = new DataInputStream(buffer);
			
			/*
			 * 1 0 0 1 - command id
			 * 0 0 0 0 - command status 
			 * 0 0 0 4 - command sequence
			 * 3 - version 
			 * 38 34 39 38 30 32 30 30 36 31 33 0 - smsc addr 
			 * 4 - msc noa
			 * 1 - msc npi
			 * 38 34 39 38 30 32 30 30 33 30 34 0 - msc addr
			 * 38 34 31 36 36 39 30 30 38 33 34 32 0 - source addr
			 * 38 34 31 36 33 33 35 33 30 30 39 33 0 - dest addr
			 * 1 - momtflag
			 * 41 31 42 38 39 41 43 31 0 - smid 
			 * 0 0 0 0 - sm len 
			 * 0 0 0 1 - service id
			 */
			
			
			super.setCommandStatus(CommandStatus.valueOf(parser.readInt()));
			CommandSequence sequence = CommandSequence.getInstance();
			sequence.setValue(parser.readInt());
			super.setCommandSequence(sequence);
			
            this.version = WinVersion.valueOf(parser.read()); LogService.appLog.debug("Read version: " + this.version);
			this.smscAddress = CDecimalString.readString(parser); LogService.appLog.debug("Read smscAddress: " + this.smscAddress);
			this.moMscNoa = (Byte) SmppParameter.getInstance(Type.BYTE, (byte) parser.read()); LogService.appLog.debug("Read moMscNoa: " + this.moMscNoa);
			this.moMscNpi = (Byte) SmppParameter.getInstance(Type.BYTE, (byte) parser.read()); LogService.appLog.debug("Read moMscNpi: " + this.moMscNpi);
			this.moMscAddress = CDecimalString.readString(parser); LogService.appLog.debug("Read moMscAddress: " + this.moMscAddress);
			this.sourceAddress = CDecimalString.readString(parser); LogService.appLog.debug("Read sourceAddress: " + this.moMscAddress);
			this.destinationAddress = CDecimalString.readString(parser); LogService.appLog.debug("Read sourceAddress: " + this.moMscAddress);
            this.moMtFlag = WinMoMtFlag.valueOf(parser.read()); LogService.appLog.debug("Read moMtFlag: " + this.moMtFlag);
			this.smId = CHexString.readString(parser); LogService.appLog.debug("Read smId: " + this.smId);
			this.smLength = (Integer) SmppParameter.getInstance(Type.INTEGER, parser.readInt()); LogService.appLog.debug("Read smLength: " + this.smLength);
			if (parser.available() > 0) {
			    this.serviceId = (Integer) SmppParameter.getInstance(Type.INTEGER, parser.readInt()); LogService.appLog.debug("Read serviceId: " + this.serviceId);
			}
			
			parser.close();
			parser = null;
			
			LogService.appLog.info("AuthAcc-decode:Success:SMId:"+this.smId+":SMSCAddress:"+this.smscAddress+":ServiceId:"+this.serviceId);
		} catch (IOException e) {
			LogService.appLog.debug("AuthAcc-decode:Failed to deserialize pdu:"+this.smId+":SMSCAddress:"+this.smscAddress+":ServiceId:"+this.serviceId,e);
            throw new SmppCodecException("Failed to serialize pdu", e);
        } catch (Exception e) {
            LogService.appLog.debug("AuthAcc-decode:Failed to serialize pdu:"+this.smId+":SMSCAddress:"+this.smscAddress+":ServiceId:"+this.serviceId,e);
            throw new SmppCodecException("Failed to serialize pdu", e);
        }
	}

	@Override
	public void validate() throws SmppCodecException {
		// TODO do nothing...we dont know anything about this chinese shit!!

	}
	
	public AuthAccResponse getFailedResponse(CommandStatus esmeResultCode) {
	    AuthAccResponse response = new AuthAccResponse();
	    response.setCommandStatus(esmeResultCode);
	    response.setCommandSequence(this.getCommandSequence());
	    response.setNotifyMode(WinNotifyMode.NOTIFY_NEVER);
	    response.setOperationResult(WinOperationResult.OTHER_ERRORS);
	    return response;
    }


	
	public String toString() {
	    return "PDU:: Command Length: " + this.getCommandLength().getValue() + ", " 
                + CommandId.AUTH_ACC + "(" + java.lang.Integer.toHexString(CommandId.AUTH_ACC.getId())
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


	public Integer getServiceId() {
		return serviceId;
	}


	public void setServiceId(Integer serviceId) {
		this.serviceId = serviceId;
	}


    
}
