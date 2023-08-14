package com.baasflow.commons.audit.rest;

import com.baasflow.commons.audit.rest.interceptor.ServletInterceptorConfig;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * Annotation to enable Auditing Servlet calls.
 * <p>
 * Usage: Add this annotation to any @Configuration annotated class to enable Auditing REST calls.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(ServletInterceptorConfig.class)
public @interface EnableServletEvent {

}
