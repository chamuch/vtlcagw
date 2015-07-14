package com.ericsson.raso.cac.smpp.pdu.viettel;

import com.satnar.smpp.codec.Byte;
import com.satnar.smpp.codec.SmppParameter;
import com.satnar.smpp.codec.SmppParameter.Type;

public enum WinVersion {
	V_1 ((Byte)SmppParameter.getInstance(Type.BYTE, 1)),
	V_2 ((Byte)SmppParameter.getInstance(Type.BYTE, 2)),
	V_3 ((Byte)SmppParameter.getInstance(Type.BYTE, 3));
	
	
	private Byte value = null;
	
	private WinVersion(Byte value) {
		this.value = value;
	}

	public Byte getValue() {
		return value;
	}
	
	public static WinVersion valueOf(int version) {
		switch(version) {
			case 1:
				return V_1;
			case 2:
				return V_2;
			case 3:
				return V_3;
			default:
				return null;
		}
	}
}
