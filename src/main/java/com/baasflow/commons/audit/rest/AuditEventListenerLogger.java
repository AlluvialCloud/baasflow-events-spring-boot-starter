package com.baasflow.commons.audit.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuditEventListenerLogger {

  @EventListener
  public void handleSecurityEventType(final AuditEventPublisher.SecurityEventType securityEventType) {
    log.debug("Handling security event type audit message: {}", securityEventType);

    // TODO: send it to kafka topic or log it if kafka is not available
  }
}
