package com.satnar.smpp;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.ericsson.raso.cac.smpp.pdu.viettel.AuthAcc;
import com.ericsson.raso.cac.smpp.pdu.viettel.SmResultNotify;
import com.satnar.smpp.pdu.SmppPdu;

public class SpringHelper implements ApplicationContextAware {
    
    private static ApplicationContext applicationContext = null;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }
    
    public static SmppPdu getSmppPduImplementation(String commandId) {
        return applicationContext.getBean(commandId, SmppPdu.class);
    	//return (SmppPdu)applicationContext.getBean("authAccBean");    	
    }
    
}
