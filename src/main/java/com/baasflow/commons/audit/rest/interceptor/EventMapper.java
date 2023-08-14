package com.baasflow.commons.audit.rest.interceptor;

import com.baasflow.commons.audit.rest.ServletEvent;
import com.baasflow.commons.audit.rest.ServletEventPublisher;
import com.baasflow.commons.events.EventLogLevel;
import com.baasflow.commons.events.EventStatus;
import com.baasflow.commons.events.EventType;
import io.swagger.v3.oas.annotations.Operation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventMapper {

  public static final String UNKNOWN = "unknown";

  public void toServletEventContext(final ServletEvent from, @Nullable final Operation openApiOperation,
      final ServletEventContext to) {
    to.setOperationId(determineOperationId(from, openApiOperation));
    to.setDomains(from.domains());
    to.setEventType(from.eventType());
    to.setSourceModule(from.sourceModule());
    to.setEventLogLevel(from.eventLogLevel());
    to.setPayloadType(StringUtils.trimToNull(from.payloadType()));
  }

  private static String determineOperationId(final ServletEvent from, @Nullable final Operation openApiOperation) {
    final var auditOperationId = StringUtils.trimToNull(from.operationId());
    final var openApiOperationId = (null == openApiOperation) ? null : StringUtils.trimToNull(openApiOperation.operationId());
    final var operationId = ObjectUtils.firstNonNull(auditOperationId, openApiOperationId, UNKNOWN);
    if (UNKNOWN.equals(operationId)) {
      log.warn("Operation id is not defined for audit event: {}", from);
    }
    return operationId;
  }

  public void toServletEvent(final ServletEventContext from, final ServletEventPublisher.ServletEvent to) {
    to.setTenantId(orUnknown(from.getTenant()));
    to.setOperationId(orUnknown(from.getOperationId()));
    to.setDomains(orUnknown(from.getDomains()));
    to.setEventType(orAudit(from.getEventType()));
    to.setSourceModule(orUnknown(from.getSourceModule()));
    to.setEventLogLevel(orWarn(from.getEventLogLevel()));
    to.setStatusCode(from.getStatusCode());
    to.setEventStatus(from.isSuccess() ? EventStatus.success : EventStatus.failure);
    to.setCorrelationIds(convertListSetToMap(from.getParams()));
    to.setPayload(from.getResponseObject());
    final var payloadType = from.getPayloadType();
    if (null != payloadType) {
      to.setPayloadType(payloadType);
    } else {
      var resultObject = from.getResponseObject();
      if (null != resultObject) {
        to.setPayloadType(resultObject.getClass().getName());
      }
    }
    to.setRequestMethod(from.getRequestMethod());
    to.setRequestURI(from.getRequestURI());
    to.setProduces(from.getProduces());


    to.setTookNano(System.nanoTime() - from.getStartTime());
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
