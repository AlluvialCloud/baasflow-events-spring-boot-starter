package com.baasflow.commons.audit.rest;

import com.baasflow.commons.audit.rest.interceptor.CommonNames;
import com.baasflow.commons.events.EventLogLevel;
import com.baasflow.commons.events.EventStatus;
import com.baasflow.commons.events.EventType;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

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
