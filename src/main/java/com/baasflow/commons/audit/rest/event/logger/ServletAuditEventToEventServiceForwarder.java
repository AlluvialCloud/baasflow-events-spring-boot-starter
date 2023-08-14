package com.baasflow.commons.audit.rest.event.logger;

import com.baasflow.commons.audit.rest.ServletEventPublisher.ServletEvent;
import com.baasflow.commons.events.EventService;
import com.baasflow.commons.events.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
@ConditionalOnProperty(name = "com.baasflow.commons.audit.rest.event.logger.enabled", havingValue = "true")
public class ServletAuditEventToEventServiceForwarder {

  private final EventService eventService;

  @EventListener
  public void handleServletEvent(final ServletEvent servletEvent) {
    if (servletEvent.getEventType() != EventType.audit) {
      log.debug("Ignoring servlet event type: {}", servletEvent.getEventType());
      return;
    }

    handleServletAuditEvent(servletEvent);
  }

  private void handleServletAuditEvent(ServletEvent servletEvent) {
    log.debug("Handling servlet event type audit message: {}", servletEvent);
    try {
      eventService.sendAuditlog(
          servletEvent.getSourceModule(),
          servletEvent.getOperationId(),
          servletEvent.getEventStatus(),
          servletEvent.getCorrelationIds());
    } catch (Exception e) {
      log.error("Error while sending event to kafka", e);
      // DO NOT THROW THE EXCEPTION. WE LOGGED IT. WE DO NOT WANT TO BREAK THE FLOW
    }
  }
}
