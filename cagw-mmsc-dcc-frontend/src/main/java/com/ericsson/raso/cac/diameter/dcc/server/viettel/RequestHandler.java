package com.ericsson.raso.cac.diameter.dcc.server.viettel;

import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
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
            	LogService.appLog.debug("RequestHandler-ProcessRequest: Handling Request.."+logMsg.toString());
            	
            	
            	MmsDccCharge charge = new MmsDccCharge();
            	charge.addAvps(request.getAvps());
            	
            	DiameterAnswer response = null;
            	try {
            	    LogService.stackTraceLog.info("BACKEND.REQ>> " + charge);
            	    charge = this.producer.requestBodyAndHeader(BACKEND_ENDPOINT, charge, "fe", "mmsc", MmsDccCharge.class);
            	    LogService.stackTraceLog.info("BACKEND.RES>> " + charge);
            	    response = createAnswer(request, charge.getResultCode().getAsLong());
            	} catch(AvpDataException e) {
                    response = createAnswer(request, ResultCode.DIAMETER_INVALID_AVP_VALUE.getCode());
                } catch (CamelExecutionException e) {
                    response = createAnswer(request, ResultCode.DIAMETER_UNABLE_TO_COMPLY.getCode());
                } catch (Error e) {
                    response = createAnswer(request, ResultCode.DIAMETER_UNABLE_TO_COMPLY.getCode());
                }
 
            	com.satnar.common.SpringHelper.getTraffiControl().updateExgress();
                
                return response;
            } else {
            	LogService.appLog.debug("RequestHandler-processRequest:DIAMETER_TO_BUSY");
                DiameterAnswer response = createAnswer(request, ResultCode.DIAMETER_TOO_BUSY.getCode());                
                //TODO: based on CCA response from cagw-backend, copy relevant AVPs back.
                return response;
            }
            
        } catch (BadMessageException e) {
        	LogService.appLog.debug("RequestHandler-processRequest:Encountered Exception. wont handle anything for CCRs...",e);
            return createAnswer(request, ResultCode.DIAMETER_INVALID_AVP_VALUE.getCode());
        } catch (AvpDataException e) {
        	LogService.appLog.debug("RequestHandler-processRequest:Encounterd exception",e);
            return createAnswer(request, ResultCode.DIAMETER_INVALID_AVP_VALUE.getCode());
        } catch (Error e) {
            LogService.appLog.debug("RequestHandler-processRequest:Encounterd exception",e);
            return createAnswer(request, ResultCode.DIAMETER_UNABLE_TO_COMPLY.getCode());
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
        
        //TODO: Add other avps from mmscharge based on integration inputs from Viettel
        return answer;
    }

    public void setConfig(Properties config) {
        // TODO Auto-generated method stub
        
    }
    
    
}
