package com.satnar.smpp.codec;

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

   
    
}
