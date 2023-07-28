package com.baasflow.events;

import com.baasflow.events.internal.EventBuilder;
import com.baasflow.events.internal.KafkaSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

@Service
public class EventService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    EventBuilder builder;

    @Autowired
    KafkaSender kafkaSender;


    /**
     * Sends an audit log for the specified source module, event, and event status.
     * This method is a convenience method for calling the sendAuditlog method with a null eventData parameter.
     *
     * @param sourceModule the source module of the audit log
     * @param event        the event of the audit log
     * @param eventStatus  the status of the event
     */
    public void sendAuditlog(String sourceModule, String event, EventStatus eventStatus) {
        this.sendAuditlog(sourceModule, event, eventStatus, null);
    }

    /**
     * Sends an audit log event with the specified source module, event, event status, and correlation IDs.
     *
     * @param sourceModule   the source module that generated the audit log event
     * @param event          the event description or name
     * @param eventStatus    the status of the event (e.g., success, failure)
     * @param correlationIds a map of correlation IDs associated with the event
     */
    public void sendAuditlog(String sourceModule, String event, EventStatus eventStatus, Map<CharSequence, CharSequence> correlationIds) {
        Event eventMessage = builder.auditlogEvent(sourceModule, event, eventStatus, correlationIds);
        send(eventMessage);
    }

    /**
     * Sends an event message using an Event.Builder.
     *
     * @param eventBuilder the builder function used to build the event message
     */
    public void sendEvent(Function<Event.Builder, Event.Builder> eventBuilder) {
        Event eventMessage = eventBuilder.apply(EventBuilder.createEventBuilder()).build();
        send(eventMessage);
    }

    /**
     * Sends an event message using the provided parameters.
     *
     * @param sourceModule the source module of the event
     * @param event        the name of the event
     * @param eventType    the type of the event
     * @param eventStatus  the status of the event
     * @param payload      the payload of the event
     * @param payloadType  the type of the payload
     */
    public void sendEvent(String sourceModule, String event, EventType eventType, EventStatus eventStatus, String payload, String payloadType) {
        this.sendEvent(sourceModule, event, eventType, eventStatus, payload, payloadType, null);
    }

    /**
     * Sends an event message using the provided parameters.
     *
     * @param sourceModule   the source module of the event
     * @param event          the name of the event
     * @param eventType      the type of the event
     * @param eventStatus    the status of the event
     * @param payload        the payload of the event
     * @param payloadType    the type of the payload
     * @param correlationIds the correlation IDs to associate with the event
     */
    public void sendEvent(String sourceModule, String event, EventType eventType, EventStatus eventStatus, String payload, String payloadType, Map<CharSequence, CharSequence> correlationIds) {
        Event eventMessage = builder.event(sourceModule, event, eventType, eventStatus, payload, payloadType, correlationIds);
        send(eventMessage);
    }

    private void send(Event eventMessage) {
        try {
            kafkaSender.send(eventMessage);

        } catch (IOException e) {
            logger.error("error sending auditlog event to kafka", e);
            throw new RuntimeException(e);
        }
    }
}
