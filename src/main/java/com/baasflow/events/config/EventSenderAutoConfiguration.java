package com.baasflow.events.config;

import com.baasflow.events.service.EventSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(EventSender.class)
public class EventSenderAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EventSender eventSender() {
        return new EventSender();
    }

}
