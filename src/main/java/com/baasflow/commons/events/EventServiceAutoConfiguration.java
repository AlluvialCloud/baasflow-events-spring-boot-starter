package com.baasflow.commons.events;

import com.baasflow.commons.events.internal.KafkaConfigProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KafkaConfigProperties.class)
@ComponentScan(basePackages = "com.baasflow.commons.events")
public class EventServiceAutoConfiguration {

}
