package com.baasflow.events;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditEventBuilder {

    public Event auditlogEvent(String sourceModule, String event, EventStatus eventStatus) {
        return this.auditlogEvent(sourceModule, event, eventStatus, null);
    }

    public Event auditlogEvent(String sourceModule, String event, EventStatus eventStatus, Map<CharSequence, CharSequence> correlationIds) {
        return createEventBuilder()
                .setEvent(event)
                .setEventLogLevel(EventLogLevel.INFO)
                .setEventType(EventType.audit)
                .setEventStatus(eventStatus)
                .setSourceModule(sourceModule)
                .setCorrelationIds(correlationIds)
                .build();
    }

    private static Event.Builder createEventBuilder() {
        return Event.newBuilder()
                .setId(UUID.randomUUID())
                .setEventTimestamp(Instant.now())
                ;
    }

}
