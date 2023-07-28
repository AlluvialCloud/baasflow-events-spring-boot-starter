package com.baasflow.events.internal;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;

@Data
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "baasflow")
public class KafkaConfigProperties {
    private Map<String, Event> events;

    @Data
    @NoArgsConstructor
    public static class Event {
        @NestedConfigurationProperty
        private Kafka kafka;

        @Data
        @NoArgsConstructor
        public static class Kafka {
            private String brokers;
            private String topic;
            private transient KafkaTemplate<String, byte[]> kafkaTemplate;
        }
    }
}