package com.baasflow.events;

import com.baasflow.events.internal.KafkaConfigProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KafkaConfigProperties.class)
@ComponentScan(basePackages = "com.baasflow.events")
public class EventServiceAutoConfiguration {

}
