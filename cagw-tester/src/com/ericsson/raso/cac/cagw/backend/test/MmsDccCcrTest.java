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

import com.ericsson.pps.diameter.dccapi.avp.CCRequestNumberAvp;
import com.ericsson.pps.diameter.dccapi.avp.CCRequestTypeAvp;
import com.ericsson.pps.diameter.dccapi.avp.CCServiceSpecificUnitsAvp;
import com.ericsson.pps.diameter.dccapi.avp.MultipleServicesCreditControlAvp;
import com.ericsson.pps.diameter.dccapi.avp.RequestedServiceUnitAvp;
import com.ericsson.pps.diameter.dccapi.avp.ServiceIdentifierAvp;
import com.ericsson.pps.diameter.dccapi.avp.SubscriptionIdDataAvp;
import com.ericsson.pps.diameter.dccapi.avp.SubscriptionIdTypeAvp;
import com.ericsson.pps.diameter.dccapi.avp.UsedServiceUnitAvp;
import com.ericsson.pps.diameter.dccapi.command.Ccr;
import com.ericsson.pps.diameter.rfcapi.base.avp.Avp;
import com.ericsson.pps.diameter.rfcapi.base.message.BadMessageException;
import com.ericsson.pps.diameter.rfcapi.base.message.DiameterMessage;
import com.ericsson.pps.diameter.rfcapi.base.message.DiameterRequest;
import com.ericsson.raso.cac.smpp.pdu.viettel.AuthAcc;
import com.ericsson.raso.cac.smpp.pdu.viettel.WinMoMtFlag;
import com.ericsson.raso.cac.smpp.pdu.viettel.WinVersion;
import com.google.gson.Gson;
import com.satnar.charging.diameter.dcc.server.DccServiceEndpoint;
import com.satnar.smpp.CommandSequence;
import com.satnar.smpp.codec.Byte;
import com.satnar.smpp.codec.CDecimalString;
import com.satnar.smpp.codec.CHexString;
import com.satnar.smpp.codec.SmppParameter;
import com.satnar.smpp.codec.SmppParameter.Type;


public class MmsDccCcrTest {
    
    private static AtomicInteger requestId = new AtomicInteger(0x00000000);
    public static String Service_Context_Id = "ccs_gex@nsn.com";
    
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java AuthAccTest <cagw-url> <number of repetition>");
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
        
        DccServiceEndpoint dccStack = getDiameter();
        
        for (int i=0; i < repetitions; i++) {
            postMmsCharge(args[0], dccStack);
        }
        
        //msisdn:84980200604
        
    }

    private static DccServiceEndpoint getDiameter() {
        return null;
    }

    private static void postMmsCharge(String cagwUrl, DccServiceEndpoint dccStack) {
        try {
            // Pack the pdu first....
        	System.out.println("Packing PDU..");
            Ccr mmsDccCer = new Ccr(new DiameterRequest(new DiameterMessage(272)));
            
            mmsDccCer.setAuthApplicationId(4);
            System.out.println("setAuthApplicationId");
            mmsDccCer.setCCCorrelationId(new byte[]{0x00, 0x00, (byte)requestId.incrementAndGet()});
            mmsDccCer.setDestinationHost("10.49.5.159");
            System.out.println("setDestinationHost");
            mmsDccCer.setDestinationRealm("emmsgw01.viettel.com.vn");
            mmsDccCer.setOriginHost("mmsc_host");
            mmsDccCer.setOriginRealm("mms.viettel.com.vn");
            mmsDccCer.setCCRequestNumber(1);
            mmsDccCer.setCCRequestType(CCRequestTypeAvp.EVENT_REQUEST);
            mmsDccCer.addSubscriptionId("84980200604", SubscriptionIdTypeAvp.END_USER_E164);
            mmsDccCer.setServiceIdentifier(4);
            
            System.out.println("Adding MSCC..");
            MultipleServicesCreditControlAvp msccAvp = new MultipleServicesCreditControlAvp();
            UsedServiceUnitAvp usuAvp = new UsedServiceUnitAvp();
            CCServiceSpecificUnitsAvp ccssuAvp = new CCServiceSpecificUnitsAvp(1);
            
            usuAvp.addSubAvp(ccssuAvp);
            System.out.println("Added ccssuAvp");
            msccAvp.addSubAvp(usuAvp);
            System.out.println("Added usuAvp");
            mmsDccCer.addAvp(msccAvp);
            System.out.println("Added msccAvp");
            
            Avp serviceInfo = new Avp(873); 
            Avp ismpInfo = new Avp(20500);
            Avp chargedPartyType = new Avp(20502);
            chargedPartyType.setData(1);
            ismpInfo.addSubAvp(chargedPartyType);
            System.out.println("Added serviceInfo");
            
            Avp oASubscriptionId = new Avp(20511);
            oASubscriptionId.addSubAvp(new SubscriptionIdTypeAvp(SubscriptionIdTypeAvp.END_USER_E164));
            oASubscriptionId.addSubAvp(new SubscriptionIdDataAvp("84980200604"));
            ismpInfo.addSubAvp(oASubscriptionId);
            System.out.println("Added oASubscriptionId");
            
            Avp dASubscriptionId = new Avp(20511);
            dASubscriptionId.addSubAvp(new SubscriptionIdTypeAvp(SubscriptionIdTypeAvp.END_USER_E164));
            dASubscriptionId.addSubAvp(new SubscriptionIdDataAvp("84980200614"));
            ismpInfo.addSubAvp(dASubscriptionId);
            System.out.println("Added dASubscriptionId");
            
            serviceInfo.addSubAvp(ismpInfo);
            System.out.println("Added ismpInfo to serviceInfo");
            mmsDccCer.addAvp(serviceInfo);
            
            
            
            System.out.println("Packed MMS Charging PDU for Sequence: " + requestId.get());
            System.out.println("MMS Charging PDU: " + mmsDccCer.toString());
            
            // now post the rest api
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(cagwUrl);
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Accept", "application/json");
            post.setHeader("fe", "mmsc");
            StringEntity body = new StringEntity(new Gson().toJson(mmsDccCer));
            
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
        } catch (BadMessageException e) {
            System.out.println("Failed to create MMS DCC Request... Error: DIAMETER-BadMessage-" + e.getMessage());
        }
    }
    
    
    
    
}
