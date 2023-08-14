package com.baasflow.commons.audit.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code Audit} annotation is used to mark a parameter as auditable.
 *
 * <p>The {@code Audit} annotation can be applied to any parameter of a method or constructor to indicate that the value of the parameter should be audited.
 * The annotated parameter will be logged or tracked for audit purposes.
 *
 * <p>The {@code Audit} annotation can be customized with a {@code value} attribute to provide additional information about the audited parameter.
 * This information can be used for better understanding and tracking of the audit event.
 *
 * <p>Additionally, the {@code addToMDC} attribute can be used to specify whether the value of the annotated parameter should be added to the
 * Mapped Diagnostic Context (MDC).
 * The MDC is a map-like data structure that is often used in logging to store additional contextual information for each log message.
 * By default, the value of the {@code addToMDC} attribute is set to {@code false}.
 *
 * <p>Example usage:
 * <pre>{@code
 *    public void updateUser(@Audit(value = "userId", addToMDC = true) String userId) {
 *        // Method implementation
 *    }
 * }</pre>
 *
 * @see Target
 * @see Retention
 * @see ElementType
 * @see RetentionPolicy
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {

  String value() default "";

  boolean addToMDC() default false;
}
