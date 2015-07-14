package com.ericsson.raso.cac.smpp.pdu.viettel;

import com.satnar.smpp.codec.Integer;
import com.satnar.smpp.codec.SmppParameter;
import com.satnar.smpp.codec.SmppParameter.Type;

public enum WinOperationResult {
	SUCCESS ((Integer)SmppParameter.getInstance(Type.INTEGER, 0)),
	PROTOCOL_VERSION_NO_SUPPORT ((Integer)SmppParameter.getInstance(Type.INTEGER, 1)),
	OTHER_ERRORS ((Integer)SmppParameter.getInstance(Type.INTEGER, 10)),
	MO_USER_ACCT_NOT_EXIST ((Integer)SmppParameter.getInstance(Type.INTEGER, 11)),
	MT_USER_ACCT_NOT_EXIST ((Integer)SmppParameter.getInstance(Type.INTEGER, 12	)),
	MO_USER_ACCT_NOT_CORRECT ((Integer)SmppParameter.getInstance(Type.INTEGER, 13)),
	MT_USER_ACCT_NOT_CORRECT ((Integer)SmppParameter.getInstance(Type.INTEGER, 14)),
	MO_USER_ACCT_NOT_ENOUGH_CREDIT ((Integer)SmppParameter.getInstance(Type.INTEGER, 15)),
	MT_USER_ACCT_NOT_ENOUGH_CREDIT ((Integer)SmppParameter.getInstance(Type.INTEGER, 16)),
	MO_USER_VAS_NO_SUPPORT ((Integer)SmppParameter.getInstance(Type.INTEGER, 17)),
	MT_USER_VAS_NO_SUPPORT ((Integer)SmppParameter.getInstance(Type.INTEGER, 18)),
	;
	
	
	private Integer value = null;
	
	private WinOperationResult(Integer value) {
		this.value = value;
	}

	public Integer getValue() {
		return value;
	}
	
	public static WinOperationResult valueOf(int value) {
		switch(value) {
			case 0:
				return SUCCESS;
			case 1:
				return PROTOCOL_VERSION_NO_SUPPORT;
			case 10:
				return OTHER_ERRORS;
			case 11:
				return MO_USER_ACCT_NOT_EXIST;
			case 12:
				return MT_USER_ACCT_NOT_EXIST;
			case 13:
				return MO_USER_ACCT_NOT_CORRECT;
			case 14:
				return MT_USER_ACCT_NOT_CORRECT;
			case 15:
				return MO_USER_ACCT_NOT_ENOUGH_CREDIT;
			case 16:
				return MT_USER_ACCT_NOT_ENOUGH_CREDIT;
			case 17:
				return MO_USER_VAS_NO_SUPPORT;
			case 18:
				return MT_USER_VAS_NO_SUPPORT;
			default:
				return null;
		}
	}
}
