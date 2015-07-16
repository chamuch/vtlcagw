package com.ericsson.raso.cac.cagw.backend.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.ericsson.raso.cac.smpp.pdu.viettel.AuthAcc;
import com.ericsson.raso.cac.smpp.pdu.viettel.SmResultNotifyResponse;
import com.ericsson.raso.cac.smpp.pdu.viettel.WinMoMtFlag;
import com.ericsson.raso.cac.smpp.pdu.viettel.WinVersion;
import com.google.gson.Gson;
import com.satnar.air.ucip.client.AirClient;
import com.satnar.air.ucip.client.UcipException;
import com.satnar.air.ucip.client.command.UpdateBalanceAndDateCommand;
import com.satnar.air.ucip.client.internal.AirClientImpl;
import com.satnar.air.ucip.client.request.DedicatedAccountUpdateInformation;
import com.satnar.air.ucip.client.request.UpdateBalanceAndDateRequest;
import com.satnar.air.ucip.client.response.UpdateBalanceAndDateResponse;
import com.satnar.common.LogService;
import com.satnar.common.SpringHelper;
import com.satnar.smpp.CommandSequence;
import com.satnar.smpp.codec.Byte;
import com.satnar.smpp.codec.CDecimalString;
import com.satnar.smpp.codec.CHexString;
import com.satnar.smpp.codec.SmppParameter;
import com.satnar.smpp.codec.SmppParameter.Type;


public class UcipUbdTest {
    
    private static AtomicInteger smId = new AtomicInteger(0x10000000);
    
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java UcipUbdTest <air-ucip-url> <number of repetition> <da/ma-id> <units> <type>");
            System.exit(-1);
        }
        
        // check params first...
        try {
            URL url = new URL(args[0]);
        } catch (Exception e) {
            System.out.println("URL not valid. Check: " + args[0]);
        }
        
        int repetitions = 0;
        try {
            repetitions = Integer.parseInt(args[1]);
        } catch (Exception e) {
            System.out.println("Repetitions not valid. Check: " + args[1]);
        }
        
        int accountId = 0;
        try {
            accountId = Integer.parseInt(args[2]);
        } catch (Exception e) {
            System.out.println("DA/MA ID not valid. Check: " + args[2]);
        }
        
        int units = 0;
        try {
            units = Integer.parseInt(args[3]);
        } catch (Exception e) {
            System.out.println("Units not valid. Check: " + args[3]);
        }
        
        int type = 0;
        try {
            type = Integer.parseInt(args[4]);
        } catch (Exception e) {
            System.out.println("Units not valid. Check: " + args[4]);
        }
        
        
        
        // execute the load....
        for (int i=0; i < repetitions; i++) {
            postUbdRequests(args[0], accountId, units, type);
        }
        
        //msisdn:84980200604
        
    }

    private static void postUbdRequests(String ucipUrl, int accountId, int units, int type) {
        
        Properties ucipConfig = SpringHelper.getConfig().getProperties("cs-air1");
        AirClient airClient = new AirClientImpl(ucipConfig);
        
        // Pack the pdu first....
        UpdateBalanceAndDateRequest ubdRequest = new UpdateBalanceAndDateRequest();
        ubdRequest.setSubscriberNumber("841669005768");
        ubdRequest.setSubscriberNumberNAI(1);
        ubdRequest.setSiteId("1");
        ubdRequest.setNegotiatedCapabilities(805646916);
        ubdRequest.setTransactionCode("841669005769");
        
        StringBuilder sbLog = new StringBuilder("");
        sbLog.append(":SubscriberNumber:");sbLog.append(ubdRequest.getSubscriberNumber());
        
        List<DedicatedAccountUpdateInformation> dasToUpdate = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            DedicatedAccountUpdateInformation dauInfo = new DedicatedAccountUpdateInformation();
            dauInfo.setDedicatedAccountID(accountId);
            dauInfo.setDedicatedAccountUnitType(type);
            dauInfo.setAdjustmentAmountRelative("-" + units);
            dasToUpdate.add(dauInfo); 
            
            sbLog.append(":DA:[");sbLog.append(i);sbLog.append("]:Id:");sbLog.append(dauInfo.getDedicatedAccountID());
            sbLog.append(":DA:[");sbLog.append(i);sbLog.append("]:AccountUnitType:");sbLog.append(dauInfo.getDedicatedAccountUnitType());
            sbLog.append(":DA:[");sbLog.append(i);sbLog.append("]:AdjustmentAmountRelative:");sbLog.append(dauInfo.getAdjustmentAmountRelative());
        }           
        System.out.println("SmsRefundProcessor-process:AIR request:" + sbLog.toString());
        sbLog = null;
        
        boolean refundResult = false;
        try {
            UpdateBalanceAndDateCommand command = new UpdateBalanceAndDateCommand(ubdRequest);
            UpdateBalanceAndDateResponse ubdResponse = command.execute();
            
            System.out.println("SmsRefundProcessor-process:AIR response: AirResponseCode:"+ubdResponse.getResponseCode());
            refundResult = true;
        } catch (UcipException e) {
            System.out.println("SmsRefundProcessor-process:Failed to refund !!"); e.printStackTrace();
        }  
    }
    
    
    
    
}
