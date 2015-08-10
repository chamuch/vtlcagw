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
    
    
    
    
    
    
}
