package com.ericsson.raso.cac.smpp.pdu.viettel;

import com.satnar.smpp.codec.Byte;
import com.satnar.smpp.codec.SmppParameter;
import com.satnar.smpp.codec.SmppParameter.Type;

public enum WinNotifyMode {
	ALWAYS_NOTIFY ((Byte)SmppParameter.getInstance(Type.BYTE, 1)),
	NOTIFY_FAILURE ((Byte)SmppParameter.getInstance(Type.BYTE, 2)),
	NOTIFY_SUCCESS ((Byte)SmppParameter.getInstance(Type.BYTE, 3)),
	NOTIFY_NEVER ((Byte)SmppParameter.getInstance(Type.BYTE, 4));
	
	
	private Byte value = null;
	
	private WinNotifyMode(Byte value) {
		this.value = value;
	}

	public Byte getValue() {
		return value;
	}
	
	public static WinNotifyMode valueOf(int mode) {
		switch(mode) {
			case 1:
				return ALWAYS_NOTIFY;
			case 2:
				return NOTIFY_FAILURE;
			case 3:
				return NOTIFY_SUCCESS;
			case 4:
				return NOTIFY_NEVER;
			default:
				return null;
		}
	}
}
