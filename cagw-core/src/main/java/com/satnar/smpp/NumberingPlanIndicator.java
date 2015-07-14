package com.satnar.smpp;

public enum NumberingPlanIndicator implements Parameter {
    UNKNOWN             (0x00000000),
    ISDN_E164           (0x00000001),
    DATA_X121           (0x00000003),
    TELEX_F69           (0x00000004),
    LANDMOBILE_E212     (0x00000006),
    NATIONAL            (0x00000008),
    PRIVATE             (0x00000009),
    ERMES               (0x0000000A),
    INTERNET_IP         (0x0000000E),
    WAP_CLIENT          (0x00000012);

   
    private int npi = -1;
    
    private NumberingPlanIndicator(int value) {
        this.npi = value;
    }
    
    public int getValue() {
        return this.npi;
    }

    public int getLength() {
        return 1; // as per SMPP Specs 3.4 Issue 1.2
    }
    
}
