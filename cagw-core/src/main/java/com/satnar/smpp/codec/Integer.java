package com.satnar.smpp.codec;

public class Integer extends SmppParameter {
    
    private int value = 0;
    
    protected Integer() {
        super.type = Type.INTEGER;
    }
    
    protected Integer(int value) {
        super.type = Type.INTEGER;
        this.value = value;
    }

    @Override
    public byte[] encode() {
        return new byte[] {(byte)(0xff & (value >> 24)), 
                (byte)(0xff & (value >> 16)),
                (byte)(0xff & (value >>  8)),
                (byte)(0xff & value)};
    }

    @Override
    public java.lang.Integer decode(byte[] encoded) {
        throw new IllegalStateException("Not implemented for this type!!");
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
        return 4; // as per SMPP Specs 3.4 Issue 1.2
    }

    @Override
    public String toString() {
        return String.format("Integer [value=%s]", value);
    }
    
    
    
}
