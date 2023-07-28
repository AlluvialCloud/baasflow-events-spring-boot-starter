package com.baasflow.events.internal;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfiguration {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${baasflow.events.auditlog.kafka.brokers}")
    String auditlogBrokers;

    @Value("${baasflow.events.generic.kafka.brokers}")
    String genericBrokers;

    @Value("${spring.kafka.producer.key-serializer}")
    String keySerializer;

    @Value("${spring.kafka.producer.value-serializer}")
    String valueSerializer;


    @Qualifier("auditlog")
    @Bean
    public ProducerFactory<String, byte[]> auditlogProducerFactory() {
        logger.info("configuring Kafka connection for auditlog brokers to: {}", auditlogBrokers);
        return createProducerFactory(auditlogBrokers);
    }

    @Qualifier("auditlog")
    @Bean
    public KafkaTemplate<String, byte[]> auditlogKafkaTemplate() {
        return new KafkaTemplate<>(auditlogProducerFactory());
    }

    @Qualifier("generic")
    @Bean
    public ProducerFactory<String, byte[]> genericProducerFactory() {
        logger.info("configuring Kafka connection for generic brokers to: {}", auditlogBrokers);
        return createProducerFactory(genericBrokers);
    }

    @Qualifier("generic")
    @Bean
    public KafkaTemplate<String, byte[]> genericKafkaTemplate() {
        return new KafkaTemplate<>(genericProducerFactory());
    }

    private ProducerFactory<String, byte[]> createProducerFactory(String brokers) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        return new DefaultKafkaProducerFactory<>(configProps);
    }
}
