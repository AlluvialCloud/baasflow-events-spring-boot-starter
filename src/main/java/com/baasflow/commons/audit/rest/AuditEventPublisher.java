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

package com.baasflow.commons.audit.rest;

import com.baasflow.commons.events.EventLogLevel;
import com.baasflow.commons.events.EventStatus;
import com.baasflow.commons.events.EventType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuditEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Async
    public void publish(final SecurityEventType securityEventType, final Map<String, String> mdcContextMap) {
        MDC.setContextMap(mdcContextMap);
        log.info("Publishing security event: {}", securityEventType);
        applicationEventPublisher.publishEvent(securityEventType);
    }

    @Data
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class SecurityEventType {

        private String tenantId; // tenantId
        private String operationId; // event
        private String domains; // ???
        private EventType eventType; //eventType
        private String sourceModule; // sourceModule
        private EventLogLevel eventLogLevel; // eventLogLevel
        private int statusCode; // ??? --> eventStatus
        private EventStatus eventStatus; // eventStatus
        private Map<String, String> params = new HashMap<>(); // correlationIds
        // payloadType
        // payload
        // tenantId ? from http-header???
    }
}
