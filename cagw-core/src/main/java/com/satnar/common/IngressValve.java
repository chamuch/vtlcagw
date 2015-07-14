package com.satnar.common;

public interface IngressValve {

    public abstract boolean authorizeIngress();

    public abstract void updateExgress();
    
}
