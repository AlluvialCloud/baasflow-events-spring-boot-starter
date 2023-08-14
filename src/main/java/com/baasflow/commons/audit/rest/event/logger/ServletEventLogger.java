package com.baasflow.commons.audit.rest.event.logger;

import com.baasflow.commons.audit.rest.ServletEventPublisher.ServletEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
//@ConditionalOnProperty(name = "com.baasflow.commons.audit.rest.event.logger.slf4j-debug-logger.enabled", havingValue = "true")
public class ServletEventLogger {
  private final ObjectMapper objectMapper;

  @EventListener
  public void handleServletEvent(final ServletEvent servletEvent) {
    log.debug("Handling REST event: {}", servletEvent.toString(objectMapper));
  }
}
