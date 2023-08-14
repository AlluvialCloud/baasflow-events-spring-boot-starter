package com.baasflow.commons.audit.rest.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class ServletInterceptorConfig implements WebMvcConfigurer {

  private final ServletInterceptor interceptor;

  @Override
  public void addInterceptors(final InterceptorRegistry registry) {
    final var interceptorRegistration = registry.addInterceptor(interceptor);
    interceptorRegistration.order(Ordered.HIGHEST_PRECEDENCE);
  }
}
