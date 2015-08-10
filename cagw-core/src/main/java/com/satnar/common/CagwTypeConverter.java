package com.satnar.common;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.FallbackConverter;

import com.ericsson.raso.cac.smpp.pdu.viettel.AuthAcc;
import com.ericsson.raso.cac.smpp.pdu.viettel.AuthAccResponse;
import com.ericsson.raso.cac.smpp.pdu.viettel.SmResultNotify;
import com.ericsson.raso.cac.smpp.pdu.viettel.SmResultNotifyResponse;
import com.google.gson.Gson;

@Converter
public class CagwTypeConverter {
    
    private static Gson gson = new Gson();
    
    @Converter
    @FallbackConverter
    public static AuthAcc convertToAuthAcc(Class<AuthAcc> type, String gsonPayload, Exchange exchange) {
        System.out.println(">>>>>>>>>>>> CagwTypeConvertor - unmarshalling AuthAcc...");
        return gson.fromJson(gsonPayload, AuthAcc.class);
    }
    
    @Converter
    @FallbackConverter
    public static AuthAccResponse convertToAuthAccResp(Class<AuthAccResponse> type, String gsonPayload, Exchange exchange) {
        System.out.println(">>>>>>>>>>>> CagwTypeConvertor - unmarshalling AuthAccResponse...");
        return gson.fromJson(gsonPayload, AuthAccResponse.class);
    }
    
    @Converter
    @FallbackConverter
    public static SmResultNotify convertToSmResultNotify(Class<SmResultNotify> type, String gsonPayload, Exchange exchange) {
        System.out.println(">>>>>>>>>>>> CagwTypeConvertor - unmarshalling SmResultNotify...");
        return gson.fromJson(gsonPayload, SmResultNotify.class);
    }
    
    @Converter
    @FallbackConverter
    public static SmResultNotifyResponse convertToSmResultNotifyResp(Class<SmResultNotifyResponse> type, String gsonPayload, Exchange exchange) {
        System.out.println(">>>>>>>>>>>> CagwTypeConvertor - unmarshalling SmResultNotifyResponse...");
        return gson.fromJson(gsonPayload, SmResultNotifyResponse.class);
    }
    

    @Converter
    @FallbackConverter
    public static String convertAuthAcc(Class<AuthAcc> type, AuthAcc pojoPayload, Exchange exchange) {
        System.out.println(">>>>>>>>>>>> CagwTypeConvertor - marshalling AuthAcc...");
        return gson.toJson(pojoPayload, AuthAcc.class);
    }
    
    @Converter
    @FallbackConverter
    public static String convertAuthAccResp(Class<AuthAccResponse> type, AuthAccResponse pojoPayload, Exchange exchange) {
        System.out.println(">>>>>>>>>>>> CagwTypeConvertor - marshalling AuthAccResponse...");
        return gson.toJson(pojoPayload, AuthAccResponse.class);
    }
    
    @Converter
    @FallbackConverter
    public static String convertToSmResultNotify(Class<SmResultNotify> type, SmResultNotify pojoPayload, Exchange exchange) {
        System.out.println(">>>>>>>>>>>> CagwTypeConvertor - marshalling SmResultNotify...");
        return gson.toJson(pojoPayload, SmResultNotify.class);
    }
    
    @Converter
    @FallbackConverter
    public static String convertToSmResultNotifyResp(Class<SmResultNotifyResponse> type, SmResultNotifyResponse pojoPayload, Exchange exchange) {
        System.out.println(">>>>>>>>>>>> CagwTypeConvertor - marshalling SmResultNotifyResponse...");
        return gson.toJson(pojoPayload, SmResultNotifyResponse.class);
    }
    
    
    
    
    
    
}
