/*
 * Licensed to BaaSFlow Corporation "BaaSFlow" under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  BaaSFlow licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this  file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.baasFlow.com/licenses/Apache_LICENSE-2.0
 * or the root of this project.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.baasflow.commons.events;

import com.baasflow.commons.events.internal.EventBuilder;
import com.baasflow.commons.events.internal.KafkaSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * This class represents the EventService, which is a service responsible for sending event messages.
 * It provides methods for sending audit log and event messages.
 */
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
    public void sendAuditlog(String sourceModule, String event, EventStatus eventStatus, Map<String, String> correlationIds) {
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
    public void sendEvent(String sourceModule, String event, EventType eventType, EventStatus eventStatus, String payload, String payloadType, Map<String, String> correlationIds) {
        Event eventMessage = builder.event(sourceModule, event, eventType, eventStatus, payload, payloadType, correlationIds);
        send(eventMessage);
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
     * @param payloadFormat  the format of the payload
     * @param correlationIds the correlation IDs to associate with the event
     */
    public void sendEvent(String sourceModule, String event, EventType eventType, EventStatus eventStatus, String payload, String payloadFormat, String payloadType, Map<String, String> correlationIds) {
        Event eventMessage = builder.event(sourceModule, event, eventType, eventStatus, payload, payloadFormat, payloadType, correlationIds);
        send(eventMessage);
    }

    /**
     * Executes a function with an audited event message using the provided event builder.
     *
     * @param eventBuilder the function that applies modifications to the event builder
     * @param function     the function to be executed with the modified event builder
     * @param <T>          the type of the result of the function
     * @return the result of the executed function
     * @throws Exception if an exception occurs while executing the function
     */
    public <T> T auditedEvent(Function<Event.Builder, Event.Builder> eventBuilder, Function<Event.Builder, T> function) {
        Event.Builder builder = eventBuilder.apply(EventBuilder.createEventBuilder()
                        .setEventType(EventType.audit));

        Event errorEvent = null;
        try {
            T result = function.apply(builder);
            if (builder.getEventStatus() == null) {
                builder.setEventStatus(EventStatus.success);
            }
            return result;

        } catch (Exception e) {
            builder.setEventStatus(EventStatus.failure);

            errorEvent = createErrorEvent(e, builder);
            throw e;

        } finally {
            send(builder.build());
            if (errorEvent != null) {
                send(errorEvent);
            }
        }
    }

    private static Event createErrorEvent(Exception e, Event.Builder builder) {
        Map<String, String> originalCorrelationIds = builder.getCorrelationIds();
        Map<String, String> correlationIds = originalCorrelationIds != null ? new HashMap<>(originalCorrelationIds) : new HashMap<>();
        correlationIds.put("originalEventId", builder.getId().toString());

        return EventBuilder.createEventBuilder()
                .setEventLogLevel(EventLogLevel.ERROR)
                .setEventStatus(EventStatus.failure)

                .setEventType(builder.getEventType())
                .setSourceModule(builder.getSourceModule())
                .setEvent(builder.getEvent())
                .setTenantId(builder.getTenantId())
                .setPayload(printStacktrace(e))
                .setPayloadFormat("text/plain")
                .setPayloadType("string")
                .setCorrelationIds(correlationIds)
                .build();
    }

    public static String printStacktrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
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
