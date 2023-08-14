package com.baasflow.commons.audit.rest;

import com.baasflow.commons.events.EventLogLevel;
import com.baasflow.commons.events.EventType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditSecurityEvent {

  String operationId() default "";

  String sourceModule() default "";

  String domains(); // enum

  EventType eventType() default EventType.audit;

  EventLogLevel eventLogLevel() default EventLogLevel.INFO;

  /**
   * Retrieves the header names which should be included in correlation parameters.
   *
   * @return an array of header names
   */
  String[] headerNames() default {};

  String payloadType() default "";
}
