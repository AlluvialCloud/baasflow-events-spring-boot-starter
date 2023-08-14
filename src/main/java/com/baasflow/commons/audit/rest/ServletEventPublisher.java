package com.baasflow.commons.audit.rest;

import com.baasflow.commons.events.EventLogLevel;
import com.baasflow.commons.events.EventStatus;
import com.baasflow.commons.events.EventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ServletEventPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;

  public void publish(final ServletEvent servletEvent) {
    log.info("Publishing servlet event: {}", servletEvent);
    applicationEventPublisher.publishEvent(servletEvent);
  }

  @Data
  @Accessors(chain = true)
  @NoArgsConstructor
  public static class ServletEvent {

    private String tenantId; // tenantId
    private String operationId; // event
    private String domains; // ???
    private EventType eventType; //eventType
    private String sourceModule; // sourceModule
    private EventLogLevel eventLogLevel; // eventLogLevel
    private int statusCode; // ??? --> eventStatus
    private EventStatus eventStatus; // eventStatus
    private Map<String, String> correlationIds = new HashMap<>(); // correlationIds
    private String payloadType; // payloadType
    private Object payload; // payload
    private String requestMethod;
    private String requestURI;
    private long tookNano;
    private String produces;

    public String toString(ObjectMapper objectMapper) {
      return new StringJoiner(", ", ServletEvent.class.getSimpleName() + "[", "]")
          .add("requestMethod='" + requestMethod + "'")
          .add("requestURI='" + requestURI + "'")
          .add("tenantId='" + tenantId + "'")
          .add("operationId='" + operationId + "'")
          .add("domains='" + domains + "'")
          .add("eventType=" + eventType)
          .add("sourceModule='" + sourceModule + "'")
          .add("eventLogLevel=" + eventLogLevel)
          .add("statusCode=" + statusCode)
          .add("eventStatus=" + eventStatus)
          .add("correlationIds=" + correlationIds)
          .add("produces='" + produces + "'")
          .add("payloadType='" + payloadType + "'")
          .add("payload=" + toJson(objectMapper, produces, payload))
          .add("took=" + (tookNano / 1_000_000) + "ms")
          .toString();
    }

    @SneakyThrows
    private String toJson(ObjectMapper objectMapper, String produces, Object payload) {
      if (null == payload) {
        return null;
      }
      if (null != produces && produces.contains("application/json")) {
        return objectMapper.writeValueAsString(payload);
      }
      return payload.toString();
    }
  }
}
