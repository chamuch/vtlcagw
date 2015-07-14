package com.satnar.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringHelper implements ApplicationContextAware {
    
    private static ApplicationContext context = null;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
    
    public static IngressValve getTraffiControl() {
        return context.getBean(IngressValve.class);
    }
    
    /*public static ClusterService getClusterService() {
        return context.getBean(ClusterService.class);
    }*/

    public static IClusterService getClusterService() {
        return context.getBean(IClusterService.class);
    }
    
}
