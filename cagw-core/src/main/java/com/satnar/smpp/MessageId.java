package com.satnar.smpp;

import java.util.concurrent.atomic.AtomicLong;

public class MessageId {
    
    private static AtomicLong sequence = new AtomicLong(0);
    private long value = 0;
    
    private MessageId() {}
    
    public static synchronized MessageId getInstance() {
        MessageId instance = new MessageId();
        instance.value = sequence.incrementAndGet();
        return instance;
    }
    
    public String getValue() {
        return Long.toHexString(this.value);
    }
    
    public int getLength() {
        return 4; // as per SMPP Specs 3.4 Issue 1.2
    }
    
    public void setValue(int value) {
        this.value = value;
    }
    
    
}
