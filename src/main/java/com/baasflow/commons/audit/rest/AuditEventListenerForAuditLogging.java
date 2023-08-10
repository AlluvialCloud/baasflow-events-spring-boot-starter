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
