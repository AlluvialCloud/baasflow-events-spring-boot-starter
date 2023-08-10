package com.baasflow.commons.audit.rest.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class AuditRestInterceptorConfig implements WebMvcConfigurer {

  private final AuditSecurityInterceptor logInterceptor;

  @Override
  public void addInterceptors(final InterceptorRegistry registry) {
    final var interceptorRegistration = registry.addInterceptor(logInterceptor);
    interceptorRegistration.order(Ordered.HIGHEST_PRECEDENCE);
  }
}
