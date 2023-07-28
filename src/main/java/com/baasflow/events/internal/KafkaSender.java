package com.baasflow.events.internal;

import com.baasflow.events.Event;
import com.baasflow.events.EventType;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class KafkaSender {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${baasflow.events.auditlog.kafka.topic}")
    String auditlogTopic;

    @Value("${baasflow.events.generic.kafka.topic}")
    String genericTopic;

    @Qualifier("generic")
    @Autowired
    KafkaTemplate<String, byte[]> genericKafkaTemplate;

    @Qualifier("auditlog")
    @Autowired
    KafkaTemplate<String, byte[]> auditlogKafkaTemplate;


    @PostConstruct
    public void init() {
        logger.info("KafkaSender is configured to send auditlog to topic '{}'", auditlogTopic);
    }

    public void send(Event event) throws IOException {
        String eventId = event.getId().toString();
        logger.info("sending {} event {} to kafka topic {}: {}", event.getEventType().name(), eventId, auditlogTopic, event);
        byte[] serialized = serialize(event);

        switch (event.getEventType()) {
            case audit -> auditlogKafkaTemplate.send(auditlogTopic, eventId, serialized);
            default -> genericKafkaTemplate.send(genericTopic, eventId, serialized);
        }
    }

    byte[] serialize(Event event) throws IOException {
        byte[] message = event.toByteBuffer().array();
        logger.debug("serialized event {} to {} bytes", event.getId(), message.length);
        return message;
    }
}
