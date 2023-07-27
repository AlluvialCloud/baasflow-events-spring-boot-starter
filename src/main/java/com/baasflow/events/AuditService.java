package com.baasflow.events;

import com.baasflow.events.internal.AuditEventBuilder;
import com.baasflow.events.internal.KafkaSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AuditService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    AuditEventBuilder builder;

    @Autowired
    KafkaSender kafkaSender;


    public void sendAuditlog(String sourceModule, String event, EventStatus eventStatus) {
        Event eventMessage = builder.auditlogEvent(sourceModule, event, eventStatus);
        try {
            kafkaSender.send(eventMessage);

        } catch (IOException e) {
            logger.error("error sending auditlog event to kafka", e);
            throw new RuntimeException(e);
        }
    }
}
