package com.baasflow.events.internal;

import com.baasflow.events.Event;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class KafkaSender {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    KafkaConfigProperties kafkaConfigProperties;


    @PostConstruct
    public void init() {
        logger.info("Events is set up using the following configuration: {}", kafkaConfigProperties);
    }

    public void send(Event event) throws IOException {
        String eventId = event.getId().toString();
        byte[] serialized = serialize(event);

        var properties = kafkaConfigProperties.getEvents().get(event.getEventType().name()).getKafka();
        var kafkaTemplate = properties.getKafkaTemplate();
        var topic = properties.getTopic();
        logger.info("sending {} event {} to topic {}: {}", event.getEventType().name(), eventId, topic, event);
        kafkaTemplate.send(topic, eventId, serialized);
    }

    byte[] serialize(Event event) throws IOException {
        byte[] message = event.toByteBuffer().array();
        logger.debug("serialized event {} to {} bytes", event.getId(), message.length);
        return message;
    }
}
