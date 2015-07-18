package com.ericsson.raso.cac.diameter.dcc.server.viettel;

import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;

import com.ericsson.pps.diameter.dccapi.avp.CCRequestNumberAvp;
import com.ericsson.pps.diameter.dccapi.avp.CCRequestTypeAvp;
import com.ericsson.pps.diameter.dccapi.command.Cca;
import com.ericsson.pps.diameter.dccapi.command.Ccr;
import com.ericsson.pps.diameter.rfcapi.base.avp.AuthApplicationIdAvp;
import com.ericsson.pps.diameter.rfcapi.base.avp.Avp;
import com.ericsson.pps.diameter.rfcapi.base.avp.AvpDataException;
import com.ericsson.pps.diameter.rfcapi.base.avp.SessionIdAvp;
import com.ericsson.pps.diameter.rfcapi.base.message.ApplicationRequestListener;
import com.ericsson.pps.diameter.rfcapi.base.message.BadMessageException;
import com.ericsson.pps.diameter.rfcapi.base.message.DiameterAnswer;
import com.ericsson.pps.diameter.rfcapi.base.message.DiameterRequest;
import com.satnar.common.LogService;
import com.satnar.common.charging.diameter.ResultCode;

public class RequestHandler implements ApplicationRequestListener {
    
    private static final String BACKEND_ENDPOINT = "seda:cagw-backend";
    private static final long  DIAMETER_COMMAND_UNSUPPORTED = 3001;
    private static final long  DIAMETER_UNABLE_TO_COMPLY = 5012;
    
    
    private CamelContext context = null;
    private ProducerTemplate producer = null;
    
    public RequestHandler(CamelContext camelContext) {
        this.context = camelContext;
        this.producer = this.context.createProducerTemplate();
    }

    public DiameterAnswer processRequest(DiameterRequest request) {
    	
    	StringBuilder logMsg = null;
        try {
            Ccr dccRequest = new Ccr(request);
            //TODO: if any AVPs must be expliclitly copied, perform the same here...
            
            if (com.satnar.common.SpringHelper.getTraffiControl().authorizeIngress()) {
                logMsg = new StringBuilder("");
            	logMsg.append(":SessionId:");logMsg.append(dccRequest.getSessionId());
            	logMsg.append(":OriginHost:");logMsg.append(dccRequest.getOriginHost());
            	logMsg.append(":OriginRealm:");logMsg.append(dccRequest.getOriginRealm());
            	logMsg.append(":DestinationHost:");logMsg.append(dccRequest.getDestinationHost());
            	logMsg.append(":DestinationRealm:");logMsg.append(dccRequest.getDestinationHost());
            	LogService.appLog.debug("RequestHandler-ProcessRequest:Sending Request.."+logMsg.toString());
            	
                Cca dccResponse = this.producer.requestBodyAndHeader(BACKEND_ENDPOINT, dccRequest, "fe", "mmsc", Cca.class);
                logMsg.append(":ResultCode:");logMsg.append(dccResponse.getResultCode());
                LogService.stackTraceLog.debug("RequestHandler-ProcessRequest:Received Response.."+logMsg.toString());
                
                DiameterAnswer response = createAnswer(request, dccResponse.getResultCode());
                //TODO: based on CCA response from cagw-backend, copy relevant AVPs back.
             
                com.satnar.common.SpringHelper.getTraffiControl().updateExgress();
                
                return response;
            } else {
            	LogService.appLog.debug("RequestHandler-processRequest:DIAMETER_TO_BUSY");
                DiameterAnswer response = createAnswer(request, ResultCode.DIAMETER_TO_BUSY.getCode());                
                //TODO: based on CCA response from cagw-backend, copy relevant AVPs back.
                return response;
            }
            
        } catch (BadMessageException e) {
            //TODO: log to troubleshoot... wont handle anything for CCRs...
        	LogService.appLog.debug("RequestHandler-processRequest:Encountered Exception. wont handle anything for CCRs...",e);
            return createAnswer(request, DIAMETER_COMMAND_UNSUPPORTED);
        } catch (AvpDataException e) {
            // TODO Auto-generated catch block
        	LogService.appLog.debug("RequestHandler-processRequest:Encounterd exception",e);
            return createAnswer(request, DIAMETER_UNABLE_TO_COMPLY);
        }finally{
        	logMsg = null;
        }
    }
    
    private static DiameterAnswer createAnswer(DiameterRequest request, long statusCode) {
        DiameterAnswer answer = new DiameterAnswer(request, statusCode);
        Avp sessionIdAvp = request.getAvp(SessionIdAvp.AVP_CODE);
        if (sessionIdAvp != null) {
            answer.getDiameterMessage().add(sessionIdAvp);
        }
        Avp requestTypeAvp = request.getAvp(CCRequestTypeAvp.AVP_CODE);
        if (requestTypeAvp != null) {
            answer.getDiameterMessage().add(requestTypeAvp);
        }
        
        Avp requestNumberAvp = request.getAvp(CCRequestNumberAvp.AVP_CODE);
        if (requestNumberAvp != null) {
            answer.getDiameterMessage().add(requestNumberAvp);
        }
        
        Avp authAvp = request.getAvp(AuthApplicationIdAvp.AVP_CODE);
        if (authAvp != null) {
            answer.getDiameterMessage().add(authAvp);
        }
        return answer;
    }

    public void setConfig(Properties config) {
        // TODO Auto-generated method stub
        
    }
    
    
}
