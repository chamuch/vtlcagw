package com.satnar.smpp;

public abstract class OptionalParameter {
    
    private OptionalParameterTag tag = null;
    private int length = 0;
    
    public abstract Object getValue();
    
    
    public OptionalParameterTag getTag() {
        return tag;
    }
    public void setTag(OptionalParameterTag tag) {
        this.tag = tag;
    }
    public int getLength() {
        return length;
    }
    public void setLength(int length) {
        this.length = length;
    }
    
}
