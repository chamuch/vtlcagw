package com.ericsson.raso.cac.cagw.backend.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.ericsson.raso.cac.smpp.pdu.viettel.AuthAcc;
import com.ericsson.raso.cac.smpp.pdu.viettel.SmResultNotify;
import com.ericsson.raso.cac.smpp.pdu.viettel.WinMoMtFlag;
import com.ericsson.raso.cac.smpp.pdu.viettel.WinVersion;
import com.google.gson.Gson;
import com.satnar.smpp.CommandSequence;
import com.satnar.smpp.codec.Byte;
import com.satnar.smpp.codec.CDecimalString;
import com.satnar.smpp.codec.CHexString;
import com.satnar.smpp.codec.SmppParameter;
import com.satnar.smpp.codec.SmppParameter.Type;


public class SmResultNotifyTest {
    
    private static AtomicInteger smId = new AtomicInteger(0x10000000);
    
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java SmResultNotifyTest <cagw-url> <number of repetition>");
            System.exit(-1);
        }
        
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
        
        for (int i=0; i < repetitions; i++) {
            postSmsRefund(args[0]);
        }
        
        //msisdn:84980200604
        
    }

    private static void postSmsRefund(String cagwUrl) {
        // Pack the pdu first....
        SmResultNotify smResultPdu = new SmResultNotify();
        smResultPdu.setCommandSequence(CommandSequence.getInstance());
        smResultPdu.setDestinationAddress((CDecimalString) SmppParameter.getInstance(Type.C_DECIMAL_STRING, "84980200605"));
        smResultPdu.setSourceAddress((CDecimalString) SmppParameter.getInstance(Type.C_DECIMAL_STRING, "84980200604"));
        smResultPdu.setMoMscAddress((CDecimalString) SmppParameter.getInstance(Type.C_DECIMAL_STRING, "84981234567"));
        smResultPdu.setMoMscNoa((Byte) SmppParameter.getInstance(Type.BYTE, (byte)1));
        smResultPdu.setMoMscNpi((Byte) SmppParameter.getInstance(Type.BYTE, (byte)1));
        smResultPdu.setMoMtFlag(WinMoMtFlag.MO);
        smResultPdu.setServiceId((com.satnar.smpp.codec.Integer) SmppParameter.getInstance(Type.INTEGER, 1));
        smResultPdu.setSmId((CHexString) SmppParameter.getInstance(Type.C_HEX_STRING, Integer.toHexString(smId.incrementAndGet())));
        smResultPdu.setSmLength((com.satnar.smpp.codec.Integer) SmppParameter.getInstance(Type.INTEGER, 140));
        smResultPdu.setSmscAddress((CDecimalString) SmppParameter.getInstance(Type.C_DECIMAL_STRING, "84981234567"));
        smResultPdu.setVersion(WinVersion.V_3);
        smResultPdu.setFinalState((com.satnar.smpp.codec.Integer) SmppParameter.getInstance(Type.INTEGER, 1));
        smResultPdu.getCommandLength();
        
        System.out.println("Packed SMS Refund PDU for Sequence: " + smId.get());
        System.out.println("SMS Refund PDU: " + smResultPdu.toString());
        
        // now post the rest api
        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(cagwUrl);
            StringEntity body = new StringEntity(new Gson().toJson(smResultPdu));
            
            post.setEntity(body);
            HttpResponse response = client.execute(post);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
            }

        } catch (UnsupportedEncodingException e) {
            System.out.println("Failed to serialize JSON Body for the REST call... Error: " + e.getMessage());
        } catch (ClientProtocolException e) {
            System.out.println("Failed to connect to REST URL... Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Failed to send/receive the REST call... Error: " + e.getMessage());
        }
    }
    
    
    
    
}
