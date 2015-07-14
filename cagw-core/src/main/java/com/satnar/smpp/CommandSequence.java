package com.satnar.smpp;

import java.util.concurrent.atomic.AtomicInteger;

public class CommandSequence {
    
    private static AtomicInteger sequence = new AtomicInteger(0);
    private int value = 0;
    
    private CommandSequence() {}
    
    public static synchronized CommandSequence getInstance() {
        CommandSequence instance = new CommandSequence();
        instance.value = sequence.incrementAndGet();
        return instance;
    }
    
    public int getValue() {
        return this.value;
    }
    
    public int getLength() {
        return 4; // as per SMPP Specs 3.4 Issue 1.2
    }
    
    public void setValue(int value) {
        this.value = value;
    }
    
    
}
