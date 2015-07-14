package com.satnar.smpp;

public enum InterfaceVersion implements Parameter {
    
    v3_3 (0x33),
    v3_4 (0x34);
    
    private int interfaceVersion = 0;
    
    private InterfaceVersion(int version) {
        this.interfaceVersion = version;
    }
    
    public int getLength() {
        return 1; // as per SMPP Specs 3.4 Issue 1.2
    }

    public int getValue() {
        return interfaceVersion;
    }

    public void setValue(int interfaceVersion) {
        this.interfaceVersion = interfaceVersion;
    }
    
    public static InterfaceVersion valueOf(int value) {
        if (value == 0x34)
            return v3_4;
        else
            return v3_3;
    }
    
}
