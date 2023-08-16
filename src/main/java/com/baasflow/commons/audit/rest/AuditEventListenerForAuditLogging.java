/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.baasflow.commons.audit.rest;

import com.baasflow.commons.events.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class AuditEventListenerForAuditLogging {

    private final EventService eventService;

    @EventListener
    public void handleSecurityEventType(final AuditEventPublisher.SecurityEventType securityEventType) {
        log.debug("Handling security event type audit message: {}", securityEventType);
        eventService.sendEvent(
                securityEventType.getSourceModule(),
                securityEventType.getOperationId(),
                securityEventType.getEventType(),
                securityEventType.getEventStatus(),
                null,
                null,
                securityEventType.getParams());

        // TODO: send it to kafka topic or log it if kafka is not available
    }
}
