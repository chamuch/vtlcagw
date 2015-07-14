package com.ericsson.raso.cac.diameter.dcc.server.viettel;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.ericsson.pps.diameter.rfcapi.base.PeerConnectionListener;
import com.ericsson.pps.diameter.rfcapi.base.message.ApplicationRequestListener;
import com.satnar.charging.diameter.dcc.server.DiameterServiceEndpoint;


public class SpringHelper implements ApplicationContextAware {
    
    private static ApplicationContext applicationContext = null;

    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.applicationContext = context;
    }
    
    public static ApplicationRequestListener getRequestListener() {
        return applicationContext.getBean(ApplicationRequestListener.class);
    }
    
    public static PeerConnectionListener getPeerConnectionListener() {
        return applicationContext.getBean(PeerConnectionListener.class);
    }

    public static DiameterServiceEndpoint getDiameterStack() {
        return applicationContext.getBean(DiameterServiceEndpoint.class);
    }
    
}
