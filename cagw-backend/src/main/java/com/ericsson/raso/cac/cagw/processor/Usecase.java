package com.ericsson.raso.cac.cagw.processor;

import org.apache.camel.Processor;

import com.satnar.common.LogService;

public enum Usecase {
    RESERVE                 ("RESERVE",             DccNotImplementedProcessor.class),
    COMMIT                  ("COMMIT",              DccNotImplementedProcessor.class),
    DIRECT_DEBIT            ("DIRECT_DEBIT",        ChargeAmountProcessor.class),
    REFUND                  ("REFUND",              MmsRefundProcessor.class),
    CANCEL                  ("CANCEL",              DccNotImplementedProcessor.class),
    MMS_CHARGING            ("MMS_CHARGING",        DccNotImplementedProcessor.class),
    AUTH_CC                 ("AUTH_CC",             SmsChargingProcessor.class),
    SM_RESULT_NOTIFY        ("SM_RESULT_NOTIFY",    SmsRefundProcessor.class),
    DELIVER_SM              ("DELIVER_SM",          DefaultSmppProcessor.class),
    DATA_SM                 ("DATA_SM",             DefaultSmppProcessor.class),
    ALERT_NOTIFICATION      ("ALERT_NOTIFICATION",  DefaultSmppProcessor.class);
    
    
    
    
    private String operation;
    private Class processor;
    
    Usecase(String operation,Class processor) {
        this.operation = operation;
        this.processor = processor;
    }

    public Processor getProcessor() {
        try {
            return (Processor) processor.newInstance();
        } catch (InstantiationException e) {
            LogService.appLog.error("Usecase-getProcessor:Unable to get instance",e);
            return null;
        } catch (IllegalAccessException e) {
            LogService.appLog.error("Usecase-getProcessor:Unable to get processir instance",e);
            return null;
        }
    }
    
    

}
