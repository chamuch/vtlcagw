package com.satnar.smpp.codec;

public abstract class SmppParameter {
    
    protected Type type = null;
    
    public abstract byte[] encode();
    
    public abstract Object decode(byte[] encoded);
    
    public abstract Object getValue();
    
    public abstract int getLength();
    
    public static SmppParameter getInstance(Type type) {
        switch(type) {
            case INTEGER:
                return new com.satnar.smpp.codec.Integer();
            case BYTE:
                return new com.satnar.smpp.codec.Byte();
            case C_OCTET_STRING:
                return new COctetString();
            case C_DECIMAL_STRING:
            	return new CDecimalString();
            case C_HEX_STRING:
                return new CHexString();
            case OCTET_STRING:
                return new OctetString();
            default:
                return null;
                //throw new SmppCodecException("No Such Parameter Type. Found: " + type);
            
        }
    }
    
    public static SmppParameter getInstance(Type type, Object initialValue) {
        switch(type) {
            case INTEGER:
                return new com.satnar.smpp.codec.Integer((int) initialValue);
            case BYTE:
                return new com.satnar.smpp.codec.Byte((byte) initialValue);
            case C_OCTET_STRING:
                return new COctetString((String) initialValue);
            case C_DECIMAL_STRING:
            	return new CDecimalString((String) initialValue);
            case C_HEX_STRING:
            	return new CHexString((String) initialValue);
            case OCTET_STRING:
                return new OctetString((String) initialValue);
            default:
                return null;
                //throw new SmppCodecException("No Such Parameter Type. Found: " + type);
            
        }
    }
    
    public enum Type {
        BYTE,
        INTEGER,
        C_OCTET_STRING,
        C_DECIMAL_STRING,
        C_HEX_STRING,
        OCTET_STRING;
    }

}
