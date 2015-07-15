package com.satnar.smpp.codec;

import java.util.Arrays;

public class OctetString extends SmppParameter {
    
    private byte[] value = null;
    
    protected OctetString() {
        super.type = Type.OCTET_STRING;
    }
    
    public OctetString(byte[] value) {
        super.type = Type.OCTET_STRING;
        this.value = value;
    }
    
    public OctetString(String value) {
        super.type = Type.OCTET_STRING;
        this.value = value.getBytes();
    }
    

    @Override
    public byte[] encode() {
        return this.value;
    }

    @Override
    public String decode(byte[] encoded) {
        return new String(this.value);
    }

    @Override
    public byte[] getValue() {
        return this.value;
    }

    @Override
    public int getLength() {
        return this.value.length;
    }

    @Override
    public String toString() {
        return String.format("OctetString [value=%s]", prettyPrint(value));
    }

    private String prettyPrint(byte[] serialized) {
        StringBuilder sbPrettyPrint = new StringBuilder();
        for (byte atom: serialized) {
            sbPrettyPrint.append(java.lang.Integer.toHexString((0xff|atom)));
            sbPrettyPrint.append(" ");
        }
        return sbPrettyPrint.toString();
    }
    
}
