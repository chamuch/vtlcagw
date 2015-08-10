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
import com.ericsson.raso.cac.smpp.pdu.viettel.WinMoMtFlag;
import com.ericsson.raso.cac.smpp.pdu.viettel.WinVersion;
import com.google.gson.Gson;
import com.satnar.smpp.CommandSequence;
import com.satnar.smpp.codec.Byte;
import com.satnar.smpp.codec.CDecimalString;
import com.satnar.smpp.codec.CHexString;
import com.satnar.smpp.codec.SmppParameter;
import com.satnar.smpp.codec.SmppParameter.Type;


public class AuthAccTest {
    
    private static AtomicInteger smId = new AtomicInteger(0x10000000);
    
    static URL url = null;
    
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java AuthAccTest <cagw-url> <number of repetition>");
            System.exit(-1);
        }
        
        try {
            url = new URL(args[0]);
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
            new Thread(new Runnable() {
                public void run() {
                    postSmsCharge(url.toString());
                }
            }).start();
            
        }
        
        //msisdn:841669005768
        
    }

    private static void postSmsCharge(String cagwUrl) {
        // Pack the pdu first....
        AuthAcc authAccPdu = new AuthAcc();
        authAccPdu.setCommandSequence(CommandSequence.getInstance());
        authAccPdu.setDestinationAddress((CDecimalString) SmppParameter.getInstance(Type.C_DECIMAL_STRING, "841669005769"));
        authAccPdu.setSourceAddress((CDecimalString) SmppParameter.getInstance(Type.C_DECIMAL_STRING, "841669005768"));
        authAccPdu.setMoMscAddress((CDecimalString) SmppParameter.getInstance(Type.C_DECIMAL_STRING, "849812345678"));
        authAccPdu.setMoMscNoa((Byte) SmppParameter.getInstance(Type.BYTE, (byte)1));
        authAccPdu.setMoMscNpi((Byte) SmppParameter.getInstance(Type.BYTE, (byte)1));
        authAccPdu.setMoMtFlag(WinMoMtFlag.MO);
        authAccPdu.setServiceId((com.satnar.smpp.codec.Integer) SmppParameter.getInstance(Type.INTEGER, 1));
        authAccPdu.setSmId((CHexString) SmppParameter.getInstance(Type.C_HEX_STRING, Integer.toHexString(smId.incrementAndGet())));
        authAccPdu.setSmLength((com.satnar.smpp.codec.Integer) SmppParameter.getInstance(Type.INTEGER, 140));
        authAccPdu.setSmscAddress((CDecimalString) SmppParameter.getInstance(Type.C_DECIMAL_STRING, "849812345678"));
        authAccPdu.setVersion(WinVersion.V_3);
        authAccPdu.getCommandLength();
        
        System.out.println("Packed SMS Charging PDU for Sequence: " + smId.get());
        System.out.println("SMS Charging PDU: " + authAccPdu.toString());
        
        // now post the rest api
        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(cagwUrl);
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Accept", "application/json");
            post.setHeader("fe", "auth_acc");
            StringEntity body = new StringEntity(new Gson().toJson(authAccPdu));
            
            post.setEntity(body);
            HttpResponse response = client.execute(post);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
            }

        } catch (UnsupportedEncodingException e) {
            System.out.println("Failed to serialize JSON Body for the REST call... Error: UnsupportedEncoding-" + e.getMessage());
        } catch (ClientProtocolException e) {
            System.out.println("Failed to connect to REST URL... Error: ClientProtocol-" + e.getMessage());
        } catch (IOException e) {
            System.out.println("Failed to send/receive the REST call... Error: IOException-" + e.getMessage());
        }
    }
    
    
    
    
}
