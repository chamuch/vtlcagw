package com.ericsson.raso.cac.smpp.pdu.viettel;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.satnar.common.LogService;
import com.satnar.smpp.CommandId;
import com.satnar.smpp.CommandStatus;
import com.satnar.smpp.codec.Integer;
import com.satnar.smpp.codec.SmppCodecException;
import com.satnar.smpp.codec.SmppParameter;
import com.satnar.smpp.codec.SmppParameter.Type;
import com.satnar.smpp.pdu.SmppPdu;
import com.satnar.smpp.transport.SmppSessionState;


public class SmResultNotifyResponse extends SmppPdu {

	private WinOperationResult operationResult = null;

	private int commandLength = 0;
	
	public SmResultNotifyResponse() {
		CommandId commandId = CommandId.EXTENDED;
		commandId.setId(0x81000002);
		commandId.setEsmeInitiated(true);
		commandId.setSmscInitiated(false);
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
			
			encoder.writeInt(this.operationResult.getValue().getValue());
            
			encoder.close();
			encoder = null;
			LogService.appLog.info("SmResultNotifyResponse-encode:Success");
		} catch (IOException e) {
            // TODO Log for troubleshooting
			LogService.appLog.debug("SmResultNotifyResponse-encode:Failed to serialize pdu",e);
            throw new SmppCodecException("Failed to serialize pdu", e);
        }
		
		return buffer.toByteArray();
	}

	@Override
	public void decode(byte[] payload) throws SmppCodecException {
		// TODO not needed!!

	}

	@Override
	public void validate() throws SmppCodecException {
		// TODO not needed!!

	}

	@Override
	public Integer getCommandLength() {
        if (this.commandLength == 0) {
            this.commandLength = 4 + // length of command length 
                                    super.getCommandId().getLength() + 
                                    super.getCommandStatus().getLength() + 
                                    super.getCommandSequence().getLength() + 
                                    this.operationResult.getValue().getLength();
        }

        Integer len  = (Integer) SmppParameter.getInstance(Type.INTEGER);
        len.setValue(this.commandLength);
       
        return len;
    }


    public WinOperationResult getOperationResult() {
        return operationResult;
    }


    public void setOperationResult(WinOperationResult operationResult) {
        this.operationResult = operationResult;
    }

	
}
