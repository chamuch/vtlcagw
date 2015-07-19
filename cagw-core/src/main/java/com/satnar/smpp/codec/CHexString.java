package com.satnar.smpp.codec;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.satnar.common.LogService;
import com.satnar.smpp.codec.SmppParameter.Type;

public class CHexString extends SmppParameter {
    
	private static final Pattern PATTERN = Pattern.compile("[\\da-fA-F]*");
		
    private String value = null;
    
    protected CHexString() {
        super.type = Type.C_HEX_STRING;
    }

    protected CHexString(String value) {
        super.type = Type.C_HEX_STRING;

        Matcher matcher = PATTERN.matcher(value);
    	if (matcher.matches())
    		this.value = value + '\0';
    	else
    		throw new IllegalArgumentException("Given value is not valid. Only HexaDecimal Characters allowed!!. Found: " + value);
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
    
    public static CHexString readString(DataInputStream parser) {
	    String string = "";
	    
	    try {
	    	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	    	do {
	    		int i = parser.read();
	    		if (i == 0)
	    			break;
	    		buffer.write(i);
	    	} while (true);
	    	
	    	if (buffer.size() > 0)
	    	    string = new String(buffer.toByteArray());
	    	
	    	buffer.close();
	    	buffer = null;
	    } catch (IOException e) {
	    	//TODO: log only.. no need to complicate the error handling
	    	LogService.appLog.debug("CHexString-readString: Encountered exception",e);
	    }
	    
	    CHexString output = (CHexString) SmppParameter.getInstance(Type.C_HEX_STRING);
	    output.setValue(string);
		return output;
    }
    
	

    public void setValue(String value) {
        Matcher matcher = PATTERN.matcher(value);
    	if (matcher.matches())
    		this.value = value + '\0';
    	else
    		throw new IllegalArgumentException("Given value is not valid. Only HexaDecimal Characters allowed!!. Found: " + value);
    }
    
    public String getString() {
        return this.value.substring(0, (this.value.length()-1));
    }

    @Override
    public String toString() {
        return String.format("CHexString [value=%s]", value);
    }
    
    
    
}
