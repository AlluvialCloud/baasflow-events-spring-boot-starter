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

package com.baasflow.commons.audit.rest.interceptor;

import com.baasflow.commons.audit.rest.AuditEventPublisher.SecurityEventType;
import com.baasflow.commons.audit.rest.AuditSecurityEvent;
import com.baasflow.commons.events.EventLogLevel;
import com.baasflow.commons.events.EventStatus;
import com.baasflow.commons.events.EventType;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class AuditSecurityEventMapper {

    public static final String UNKNOWN = "unknown";

    public void toAuditSecurityEventContext(final AuditSecurityEvent from, @Nullable final Operation openApiOperation,
                                            final AuditSecurityEventContext to) {
        to.setOperationId(determineOperationId(from, openApiOperation));
        to.setDomains(from.domains());
        to.setEventType(from.eventType());
        to.setSourceModule(from.sourceModule());
        to.setEventLogLevel(from.eventLogLevel());
    }

    private static String determineOperationId(final AuditSecurityEvent from, @Nullable final Operation openApiOperation) {
        final var auditOperationId = StringUtils.trimToNull(from.operationId());
        final var openApiOperationId = (null == openApiOperation) ? null : StringUtils.trimToNull(openApiOperation.operationId());
        final var operationId = ObjectUtils.firstNonNull(auditOperationId, openApiOperationId, UNKNOWN);
        if (UNKNOWN.equals(operationId)) {
            log.warn("Operation id is not defined for audit event: {}", from);
        }
        return operationId;
    }

    public void toSecurityEventType(final AuditSecurityEventContext from, final SecurityEventType to) {
        to.setTenantId(orUnknown(from.getTenant()));
        to.setOperationId(orUnknown(from.getOperationId()));
        to.setDomains(orUnknown(from.getDomains()));
        to.setEventType(orAudit(from.getEventType()));
        to.setSourceModule(orUnknown(from.getSourceModule()));
        to.setEventLogLevel(orWarn(from.getEventLogLevel()));
        to.setStatusCode(from.getStatusCode());
        to.setEventStatus(from.isSuccess() ? EventStatus.success : EventStatus.failure);
        to.setParams(convertListSetToMap(from.getParams()));
    }

    private Map<String, String> convertListSetToMap(Map<String, Set<String>> params) {
        var map = new HashMap<String, String>();
        for (var entry : params.entrySet()) {
            map.put(entry.getKey(), String.join(",", entry.getValue()));
        }
        return map;
    }

    private String orUnknown(final String value) {
        return StringUtils.defaultIfBlank(value, UNKNOWN);
    }

    private EventType orAudit(final EventType value) {
        return null == value ? EventType.audit : value;
    }

    private EventLogLevel orWarn(final EventLogLevel value) {
        return null == value ? EventLogLevel.WARN : value;
    }
}
