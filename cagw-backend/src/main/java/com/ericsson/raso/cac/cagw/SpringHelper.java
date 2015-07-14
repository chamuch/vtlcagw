package com.ericsson.raso.cac.cagw;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.ericsson.raso.cac.config.IConfig;
import com.satnar.charging.diameter.scap.client.IScapCharging;

public class SpringHelper implements ApplicationContextAware {
    
    private static ApplicationContext applicationContext = null;
    
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }
    
    public static IScapCharging getScapDiameter() {
        return applicationContext.getBean(IScapCharging.class);
    }
    
    /*public static IConfig getConfigService() {
        return applicationContext.getBean(IConfig.class);
    }*/
}
