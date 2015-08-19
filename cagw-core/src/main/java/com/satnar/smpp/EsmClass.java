package com.satnar.smpp;

import java.util.Hashtable;
import java.util.Map;

import com.satnar.common.LogService;

public enum EsmClass implements Parameter {
    
    MT_MESSAGING_MODE_MASK (0x03),
    MT_DEFAULT_SMSC_MODE (0x00),
    MT_DATAGRAM_MODE (0x01),
    MT_FORWARD_MODE (0x02),
    MT_STORE_AND_FORWARD_MODE (0x03),
    
    MT_MESSAGING_TYPE_MASK (0x3C),
    MT_DEFAULT_MESSAGE_TYPE (0x00),
    MT_ESME_DELIVERY_ACKNOWLEDGEMENT (0x08),
    MT_ESME_MANUAL_ACKNOWLEDGEMENT (0x10),
    
    MT_GSM_SPECIFIC_MASK (0xC0),
    MT_NO_FEATURES (0x00),
    MT_UDHI_INDICATOR (0x40),
    MT_SET_REPLY_PATH (0x80),
    MT_UDHI_AND_REPLAY_PATH (0xC0),
    
    MO_MESSAGING_TYPE_MASK (0x3C),
    MO_DEFAULT_MESSAGE_TYPE (0x00),
    MO_DELIVERY_RECEIPT (0x04),
    MO_SME_DELIVERY_ACKNOWLEDGEMENT (0x08),
    MO_RESERVED_1 (0x0C),
    MO_SME_MANUAL_ACKNOWLEDGEMENT (0x10),
    MO_RESERVED_2 (0x14),
    MO_CONV_ABORT_KOREAN_CDMA (0x18),
    MO_RESERVED_3 (0x1C),
    MO_IMMEDIATE_DELIVERY_NOTIFICATION (0x20),
    
    MO_GSM_SPECIFIC_MASK (0xC0),
    MO_NO_FEATURES (0x00),
    MO_UDHI_INDICATOR (0x40),
    MO_SET_REPLY_PATH (0x80),
    MO_UDHI_AND_REPLAY_PATH (0xC0);
    
    private int esmClassType = 0x00;
    
    private static Map<Integer, EsmClass> lookup = new Hashtable<Integer, EsmClass>();
    
    static {
        // we load up only MO, since those are the ones requiring decoding; thus avoiding duplicate confusion...
        for (EsmClass esmClass: EsmClass.values()) {
            if (esmClass.name().startsWith("MO_")) {
                lookup.put(esmClass.esmClassType, esmClass);
            }
        }
        LogService.appLog.info("SMPP-EsmClass: Hashtable loaded successfully !!");
    }
    
    private EsmClass(int esmClass) {
        this.esmClassType = esmClass;
    }
    
    public int getLength() {
        return 1; // as per SMPP Specs 3.4 Issue 1.2
    }

    public int getValue() {
        return this.esmClassType;
    }

    
    public static EsmClass valueOf(int value) {
        EsmClass result = lookup.get(value);
        if (result == null)
            throw new IllegalArgumentException("No such value defined!!. Found: " + Integer.toHexString(value));
        return result;
    }
    
}
