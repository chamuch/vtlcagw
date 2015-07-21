package com.ericsson.raso.cac.cagw.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.pps.diameter.rfcapi.base.avp.Avp;
import com.ericsson.pps.diameter.rfcapi.base.avp.AvpDataException;
import com.ericsson.pps.diameter.rfcapi.base.avp.ResultCodeAvp;
import com.satnar.common.LogService;

public class MmsDccCharge {
    private Map<Integer, Avp> avpList = new HashMap<Integer, Avp>();
    private ResultCodeAvp resultCode = null;
    
    
    public ResultCodeAvp getResultCode() {
        return resultCode;
    }

    public void setResultCode(ResultCodeAvp resultCode) {
        this.resultCode = resultCode;
    }
    
    public void addAvp(Avp avp) {
        this.avpList.put(avp.getAvpCode(), avp);
    }

    public void addAvps(List<Avp> avps) {
        for (Avp avp: avps) {
            this.avpList.put(avp.getAvpCode(), avp);
        }
    }
    
    public List<Avp> getAvps() {
        ArrayList<Avp> avps = new ArrayList<Avp>();
        for (Avp avp: this.avpList.values()) {
            avps.add(avp);
        }
        return avps;
    }
    
    public Avp getAvp(int avpCode) {
        LogService.appLog.debug(String.format("Looking for avpCode: %s in request AvpList", avpCode));
        return this.avpList.get(avpCode);
    }
    
    public String getSessionId() {
        try {
            return this.avpList.get(263).getAsUTF8String();
        } catch (AvpDataException e) {
            return "Unable to get value from diameter!";
        }
    }

    @Override
    public String toString() {
        return String.format("MmsDccCharge [resultCode=%s, avpList=%s]", resultCode, avpList);
    }

    public Avp[] getMultipleServicesCreditControlArray() {
        return new Avp[] {}; //ZTE is not sending this
    }
    
    
    
}
