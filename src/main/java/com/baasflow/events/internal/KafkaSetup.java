package com.baasflow.events.internal;

import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class KafkaSetup {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${spring.kafka.producer.key-serializer}")
    String keySerializer;

    @Value("${spring.kafka.producer.value-serializer}")
    String valueSerializer;

    @Autowired
    KafkaConfigProperties kafkaConfigProperties;


    @PostConstruct
    public void kafkaTemplates() {
        for (String key : kafkaConfigProperties.getEvents().keySet()) {
            var properties = kafkaConfigProperties.getEvents().get(key).getKafka();
            logger.info("setting up kafka producer for '{}' events using brokers: {}", key, properties.getBrokers());
            var producerFactory = createProducerFactory(properties.getBrokers());
            properties.setKafkaTemplate(new KafkaTemplate<>(producerFactory));
        }
    }

    private ProducerFactory<String, byte[]> createProducerFactory(String brokers) {
        var configProps = new HashMap<String, Object>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        return new DefaultKafkaProducerFactory<>(configProps);
    }
}
