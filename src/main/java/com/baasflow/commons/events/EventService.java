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
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

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
     * Performs an audited event by executing the provided function with the given event builder.
     *
     * @param eventBuilder the event builder function that configures the event message
     * @param function     the function to be executed as part of the audited event
     * @param <T>          the type of the result returned by the function
     * @return the result of the function execution
     * @throws Exception if an exception occurs during the execution of the function
     */
    public <T> T auditedEvent(Function<Event.Builder, Event.Builder> eventBuilder, Supplier<T> function) {
        Event eventMessage = eventBuilder.apply(EventBuilder.createEventBuilder()).build();
        try {
            T result = function.get();
            eventMessage.setEventStatus(EventStatus.success);
            return result;
        } catch (Exception e) {
            eventMessage.setEventStatus(EventStatus.failure);
            throw e;
        }
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
