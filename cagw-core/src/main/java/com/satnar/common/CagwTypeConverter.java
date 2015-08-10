package com.satnar.common;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.FallbackConverter;
import org.apache.camel.NoTypeConversionAvailableException;
import org.apache.camel.TypeConversionException;
import org.apache.camel.TypeConverter;

import com.ericsson.raso.cac.smpp.pdu.viettel.AuthAcc;
import com.ericsson.raso.cac.smpp.pdu.viettel.AuthAccResponse;
import com.ericsson.raso.cac.smpp.pdu.viettel.SmResultNotify;
import com.ericsson.raso.cac.smpp.pdu.viettel.SmResultNotifyResponse;
import com.google.gson.Gson;

@Converter
public class CagwTypeConverter implements TypeConverter {
    
    private static Gson gson = new Gson();
    
    static {
        System.out.println(">>>>>>>>>>>> CagwTypeConvertor - Static - Am I getting scanned or not?!!??!!");
    }
    
    public CagwTypeConverter() {
        System.out.println(">>>>>>>>>>>> CagwTypeConvertor - Cstr - Am I getting scanned or not?!!??!!");
    }
    
    @Converter
    @FallbackConverter
    public static AuthAcc convertToAuthAcc(Class<AuthAcc> type, String gsonPayload, Exchange exchange) {
        System.out.println(">>>>>>>>>>>> CagwTypeConvertor - unmarshalling AuthAcc...");
        AuthAcc result = gson.fromJson(gsonPayload, AuthAcc.class);
        System.out.println(String.format(">>>>>>>>>>>> CagwTypeConvertor - unmarshalling ||JSON: %s||PDU: %s||", gsonPayload, result));
        return result;
    }
    
    @Converter
    @FallbackConverter
    public static AuthAccResponse convertToAuthAccResp(Class<AuthAccResponse> type, String gsonPayload, Exchange exchange) {
        System.out.println(">>>>>>>>>>>> CagwTypeConvertor - unmarshalling AuthAccResponse...");
        AuthAccResponse result = gson.fromJson(gsonPayload, AuthAccResponse.class);
        System.out.println(String.format(">>>>>>>>>>>> CagwTypeConvertor - unmarshalling ||JSON: %s||PDU: %s||", gsonPayload, result));
        return result;
    }
    
    @Converter
    @FallbackConverter
    public static SmResultNotify convertToSmResultNotify(Class<SmResultNotify> type, String gsonPayload, Exchange exchange) {
        System.out.println(">>>>>>>>>>>> CagwTypeConvertor - unmarshalling SmResultNotify...");
        SmResultNotify result = gson.fromJson(gsonPayload, SmResultNotify.class);
        System.out.println(String.format(">>>>>>>>>>>> CagwTypeConvertor - unmarshalling ||JSON: %s||PDU: %s||", gsonPayload, result));
        return result;
    }
    
    @Converter
    @FallbackConverter
    public static SmResultNotifyResponse convertToSmResultNotifyResp(Class<SmResultNotifyResponse> type, String gsonPayload, Exchange exchange) {
        System.out.println(">>>>>>>>>>>> CagwTypeConvertor - unmarshalling SmResultNotifyResponse...");
        SmResultNotifyResponse result = gson.fromJson(gsonPayload, SmResultNotifyResponse.class);
        System.out.println(String.format(">>>>>>>>>>>> CagwTypeConvertor - unmarshalling ||JSON: %s||PDU: %s||", gsonPayload, result));
        return result;
    }
    

    @Converter
    @FallbackConverter
    public static String convertAuthAcc(Class<AuthAcc> type, AuthAcc pojoPayload, Exchange exchange) {
        System.out.println(">>>>>>>>>>>> CagwTypeConvertor - marshalling AuthAcc...");
        String result = gson.toJson(pojoPayload, AuthAcc.class);
        System.out.println(String.format(">>>>>>>>>>>> CagwTypeConvertor - unmarshalling ||JSON: %s||PDU: %s||", result, pojoPayload));
        return result;
    }
    
    @Converter
    @FallbackConverter
    public static String convertAuthAccResp(Class<AuthAccResponse> type, AuthAccResponse pojoPayload, Exchange exchange) {
        System.out.println(">>>>>>>>>>>> CagwTypeConvertor - marshalling AuthAccResponse...");
        String result = gson.toJson(pojoPayload, AuthAccResponse.class);
        System.out.println(String.format(">>>>>>>>>>>> CagwTypeConvertor - unmarshalling ||JSON: %s||PDU: %s||", result, pojoPayload));
        return result;
    }
    
    @Converter
    @FallbackConverter
    public static String convertToSmResultNotify(Class<SmResultNotify> type, SmResultNotify pojoPayload, Exchange exchange) {
        System.out.println(">>>>>>>>>>>> CagwTypeConvertor - marshalling SmResultNotify...");
        String result = gson.toJson(pojoPayload, SmResultNotify.class);
        System.out.println(String.format(">>>>>>>>>>>> CagwTypeConvertor - unmarshalling ||JSON: %s||PDU: %s||", result, pojoPayload));
        return result;
    }
    
    @Converter
    @FallbackConverter
    public static String convertToSmResultNotifyResp(Class<SmResultNotifyResponse> type, SmResultNotifyResponse pojoPayload, Exchange exchange) {
        System.out.println(">>>>>>>>>>>> CagwTypeConvertor - marshalling SmResultNotifyResponse...");
        String result = gson.toJson(pojoPayload, SmResultNotifyResponse.class);
        System.out.println(String.format(">>>>>>>>>>>> CagwTypeConvertor - unmarshalling ||JSON: %s||PDU: %s||", result, pojoPayload));
        return result;
    }

    @Override
    public boolean allowNull() {
        return false;
    }

    @Override
    public <T> T convertTo(Class<T> type, Object value) throws TypeConversionException {
        if (type.equals(AuthAcc.class)) {
            System.out.println(">>>> Converting AuthAcc.. Check 'value': " + value);
            if (value instanceof String) {
                System.out.println("AuthAcc: String -> PDU operation");
                return gson.fromJson(value.toString(), type);
            } else {
                System.out.println("Requested type:AuthAcc, but value type: " + value.getClass());
                throw new TypeConversionException(value, type, new IllegalArgumentException("Requested type:AuthAcc, but value type: " + value.getClass()));
            }
        }
        
        if (type.equals(SmResultNotify.class)) {
            System.out.println(">>>> Converting SmResultNotify.. Check 'value': " + value);
            if (value instanceof String) {
                System.out.println("SmResultNotify: String -> PDU operation");
                return gson.fromJson(value.toString(), type);
            } else {
                System.out.println("Requested SmResultNotify, but value type: " + value.getClass());
                throw new TypeConversionException(value, type, new IllegalArgumentException("Requested type:SmResultNotify, but value type: " + value.getClass()));
            }
        }
        throw new TypeConversionException(value, type, new IllegalArgumentException("Requested type:" + type.getName() + ", but value type: " + value.getClass()));
    }

    @Override
    public <T> T convertTo(Class<T> type, Exchange exchange, Object value) throws TypeConversionException {
        System.out.println("Triggered thru convertTo() with exchange method. Requested type:" + type.getName() + ", but value type: " + value.getClass());
        throw new TypeConversionException(value, type, new IllegalArgumentException("Requested type:" + type.getName() + ", but value type: " + value.getClass()));
    }

    @Override
    public <T> T mandatoryConvertTo(Class<T> type, Object value) throws TypeConversionException, NoTypeConversionAvailableException {
        System.out.println("Triggered thru mandatoryConvertTo() without exchange method. Requested type:" + type.getName() + ", but value type: " + value.getClass());
        throw new TypeConversionException(value, type, new IllegalArgumentException("Requested type:" + type.getName() + ", but value type: " + value.getClass()));
    }

    @Override
    public <T> T mandatoryConvertTo(Class<T> type, Exchange exchange, Object value) throws TypeConversionException, NoTypeConversionAvailableException {
        System.out.println("Triggered thru mandatoryConvertTo() with exchange method. Requested type:" + type.getName() + ", but value type: " + value.getClass());
        throw new TypeConversionException(value, type, new IllegalArgumentException("Requested type:" + type.getName() + ", but value type: " + value.getClass()));
    }

    @Override
    public <T> T tryConvertTo(Class<T> type, Object value) {
        System.out.println("Triggered thru tryConvertTo() without exchange method. Requested type:" + type.getName() + ", but value type: " + value.getClass());
        throw new TypeConversionException(value, type, new IllegalArgumentException("Requested type:" + type.getName() + ", but value type: " + value.getClass()));
    }

    @Override
    public <T> T tryConvertTo(Class<T> type, Exchange exchange, Object value) {
        System.out.println("Triggered thru tryConvertTo() with exchange method. Requested type:" + type.getName() + ", but value type: " + value.getClass());
        throw new TypeConversionException(value, type, new IllegalArgumentException("Requested type:" + type.getName() + ", but value type: " + value.getClass()));
    }
    
    
    
    
    
    
}
