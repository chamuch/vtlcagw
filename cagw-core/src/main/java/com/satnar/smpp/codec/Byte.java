package com.satnar.smpp.codec;

public class Byte extends SmppParameter {
    
    private int value = 0;
    
    protected Byte() {
        super.type = Type.BYTE;
    }
    
    protected Byte(int value) {
        super.type = Type.BYTE;
        this.value = value;
    }

    @Override
    public byte[] encode() {
        return new byte[]{(byte)this.value};
    }

    @Override
    public java.lang.Integer decode(byte[] encoded) {
        return (int) encoded[0];
    }

    @Override
    public java.lang.Integer getValue() {
        return this.value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public int getLength() {
        return 1; // as per SMPP Specs 3.4 Issue 1.2
    }
    
    
    
}
