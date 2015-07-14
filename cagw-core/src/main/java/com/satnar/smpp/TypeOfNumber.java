package com.satnar.smpp;

public enum TypeOfNumber implements Parameter {
    UNKNOWN                 (0x00000000),
    INTERNATIONAL           (0x00000001),
    NATIONAL                (0x00000002),
    NETWORK_SPECIFIC        (0x00000003),
    SUBSCRIBER_SPECIFIC     (0x00000004),
    ALPHANUMERIC            (0x00000005),
    ABBREVIATED             (0x00000006);
    
    
    
    private int ton = -1;
    
    private TypeOfNumber(int value) {
        this.ton = value;
    }
    
    
    public TypeOfNumber valueOf(int value) {
        for (TypeOfNumber ton: values()) {
            if (ton.ton == value)
                return ton;
        }
        throw new IllegalArgumentException("No such Enum Constant defined. Found: " + value);
    }

    public int getValue() {
        return this.ton;
    }
    
    public int getLength() {
        return 1; // as per SMPP Specs 3.4 Issue 1.2
    }
    
}
