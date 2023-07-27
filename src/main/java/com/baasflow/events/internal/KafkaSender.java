package com.baasflow.events.internal;

import com.baasflow.events.Event;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class KafkaSender {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${auditlog.kafka.topic}")
    String auditlogTopic;

    @Autowired
    KafkaTemplate<String, byte[]> kafkaTemplate;

    @PostConstruct
    public void init() {
        logger.info("KafkaSender is configured to send auditlog to topic '{}'", auditlogTopic);
    }

    public void send(Event event) throws IOException {
        String eventId = event.getId().toString();
        logger.info("sending event {} to kafka topic {}: {}", eventId, auditlogTopic, event);
        byte[] serialized = serialize(event);
        kafkaTemplate.send(auditlogTopic, eventId, serialized);
    }

    byte[] serialize(Event event) throws IOException {
        byte[] message = event.toByteBuffer().array();
        logger.debug("serialized event {} to {} bytes", event.getId(), message.length);
        return message;
    }
}
