package com.example.user.config;

import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@Configuration
public class SessionConfig {

    @Bean
    public ServletListenerRegistrationBean<HttpSessionListener> httpSessionListener() {
        ServletListenerRegistrationBean<HttpSessionListener> listenerRegBean = 
            new ServletListenerRegistrationBean<>();
        
        listenerRegBean.setListener(new HttpSessionListener() {
            @Override
            public void sessionCreated(HttpSessionEvent se) {
                se.getSession().setMaxInactiveInterval(3600);
            }

            @Override
            public void sessionDestroyed(HttpSessionEvent se) {
            }
        });
        
        return listenerRegBean;
    }
}
