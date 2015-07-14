package com.satnar.smpp.codec;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import com.satnar.common.LogService;
import com.satnar.smpp.codec.SmppParameter.Type;

public class COctetString extends SmppParameter {
    
    private String value = null;
    
    protected COctetString() {
        super.type = Type.C_OCTET_STRING;
    }

    protected COctetString(String value) {
        super.type = Type.INTEGER;
        this.value = value + '\0';
    }

    @Override
    public byte[] encode() {
        if (this.value == null)
            return ("" + '\0').getBytes();
        return this.value.getBytes();
    }

    @Override
    public String decode(byte[] encoded) {
        return new String(encoded).substring(0, (encoded.length-1));
    }

    @Override
    public String getValue() {
        if (this.value == null)
            return "" + '\0';
        return this.value;
    }
    
    @Override
    public int getLength() {
        if (this.value == null)
            return 1;
        return this.value.length();
    }
    
    public static COctetString readString(DataInputStream parser) {
	    String string = "";
	    
	    try {
	    	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	    	do {
	    		int i = parser.read();
	    		if (i == 0)
	    			break;
	    		buffer.write(i);
	    	} while (true);
	    	
	    	string = new String(buffer.toByteArray());
	    	
	    	buffer.close();
	    	buffer = null;
	    } catch (IOException e) {
	    	//TODO: log only.. no need to complicate the error handling
	    	LogService.appLog.debug("COctetString-readString: Encountered exception",e);
	    }
	    
	    COctetString output = (COctetString) SmppParameter.getInstance(Type.C_OCTET_STRING);
	    output.setValue(string);
		return output;
    }
    
	

    public void setValue(String value) {
        this.value = value + '\0';
    }
    
    public String getString() {
        return this.value.substring(0, (this.value.length()-1));
    }
    
    
}
