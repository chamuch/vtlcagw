package com.satnar.smpp.codec;

import com.satnar.smpp.SmppStackException;

public class SmppCodecException extends SmppStackException {
    private static final long serialVersionUID = -4565644324796825349L;

    public SmppCodecException(String message, Throwable cause) {
        super(message, cause);
    }

    public SmppCodecException(String message) {
        super(message);
    }

    
}
