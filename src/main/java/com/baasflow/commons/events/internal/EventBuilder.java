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

    public Event auditlogEvent(String sourceModule, String event, EventStatus eventStatus, Map<String, String> correlationIds) {
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
