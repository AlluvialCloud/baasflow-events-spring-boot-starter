package com.baasflow.commons.events.internal;

import com.baasflow.commons.events.Event;
import com.baasflow.commons.events.EventLogLevel;
import com.baasflow.commons.events.EventStatus;
import com.baasflow.commons.events.EventType;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class EventBuilder {

    public Event auditlogEvent(String sourceModule, String event, EventStatus eventStatus) {
        return this.auditlogEvent(sourceModule, event, eventStatus, null);
    }

    public Event auditlogEvent(String sourceModule, String event, EventStatus eventStatus, Map<String,String> correlationIds) {
        return createEventBuilder()
                .setEvent(event)
                .setEventLogLevel(EventLogLevel.INFO)
                .setEventType(EventType.audit)
                .setEventStatus(eventStatus)
                .setSourceModule(sourceModule)
                .setCorrelationIds(correlationIds)
                .build();
    }

    public Event event(String sourceModule, String event, EventType eventType, EventStatus eventStatus, String payload, String payloadType, Map<String, String> correlationIds) {
        return createEventBuilder()
                .setEvent(event)
                .setEventLogLevel(EventLogLevel.INFO)
                .setEventType(eventType)
                .setEventStatus(eventStatus)
                .setSourceModule(sourceModule)
                .setPayload(payload)
                .setPayloadType(payloadType)
                .setCorrelationIds(correlationIds)
                .build();
    }

    public static Event.Builder createEventBuilder() {
        return Event.newBuilder()
                .setId(UUID.randomUUID())
                .setEventTimestamp(Instant.now())
                ;
    }

}
