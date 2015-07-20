package com.ericsson.raso.cac.smpp.pdu.viettel;

import com.satnar.common.LogService;
import com.satnar.smpp.codec.Byte;
import com.satnar.smpp.codec.SmppParameter;
import com.satnar.smpp.codec.SmppParameter.Type;

public enum WinMoMtFlag {
	MO ((Byte)SmppParameter.getInstance(Type.BYTE, (byte)1)),
	MT ((Byte)SmppParameter.getInstance(Type.BYTE, (byte)2)),
	RESERVED ((Byte)SmppParameter.getInstance(Type.BYTE, (byte)3));
	
	
	private Byte value = null;
	
	private WinMoMtFlag(Byte value) {
		this.value = value;
	}

	public Byte getValue() {
		return value;
	}
	
	public static WinMoMtFlag valueOf(int value) {
		switch(value) {
			case 1:
				return MO;
			case 2:
				return MT;
			case 3:
				return RESERVED;
			default:
			    LogService.appLog.debug("UNknown value received: " + value);
				return null;
		}
	}
}
