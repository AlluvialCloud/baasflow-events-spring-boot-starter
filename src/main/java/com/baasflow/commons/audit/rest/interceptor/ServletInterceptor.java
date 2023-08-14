package com.baasflow.commons.audit.rest.interceptor;

import com.baasflow.commons.audit.rest.ServletEvent;
import com.baasflow.commons.audit.rest.ServletEventPublisher;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.CheckForNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * An interceptor that handles auditing and security events for REST calls.
 * <p>
 * Lifecycle
 * <li>{@link #preHandle} populate http request headers</li>
 * <li>{@link #handleAnnotatedMethodCall} AOP around the method, populate info from about method and method params and result, also can
 * process some cases the {@link ProblemDetail}</li>
 * <li>{@link #supports} some cases will handle the {@link ProblemDetail} via {@link #beforeBodyWrite}</li>
 * <li>{@link #beforeBodyWrite} some cases will handle the {@link ProblemDetail}</li>
 * <li>{@link #preHandle} if somehow it's not called yet, then will be processed the http request headers</li>
 * <li>{@link #afterCompletion} some cases will handle the {@link ProblemDetail}, and the event will be published</li>
 */
@RequiredArgsConstructor
@Component
@Aspect
@Slf4j
public class ServletInterceptor implements HandlerInterceptor, ResponseBodyAdvice {

  public static final String X_TRACKING_ID = "X-Tracking-Id";
  public static final String REQUEST_X_TRACKING_ID = "request.TrackingId";
  public static final String REQUEST_SESSION_ID = "request.sessionId";
  public static final String TENANT_ID = "tenantId";
  private final List<String> TRACKING_ID_HEADERS = new ArrayList<>(Arrays.asList(X_TRACKING_ID, "postman-token"));
  @Nullable
  @Value("${app.audit.logging.http-header-name:#{null}}")
  private final String auditHttpHeaderName;
  @Value("${app.tenant.http-header-name:X-baasflow-tenant-id}")
  private final String tenantHttpHeaderName;

  private final ServletEventContext context;

  private final ServletEventPublisher auditEventPublisher;
  private final EventMapper mapper;

  @CheckForNull
  private static MethodSignature getMethodSignature(final ProceedingJoinPoint joinPoint) {
    if (joinPoint instanceof final MethodSignature instance) {
      return instance;
    }

    // Handle CompletableFuture based return type method calls
    if (joinPoint instanceof final MethodInvocationProceedingJoinPoint proceedingJoinPoint) {
      final var signature = proceedingJoinPoint.getSignature();
      if (signature instanceof final MethodSignature instance) {
        return instance;
      }
    }

    return null;
  }

  @PostConstruct
  public void init() {
    if (null != auditHttpHeaderName) {
      TRACKING_ID_HEADERS.add(1, auditHttpHeaderName);
    }
  }

  @Override
  public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object object) {
    log.trace("START: preHandle: {}", object);
    if (context.isPreHandled()) {
      log.trace("END: preHandle: already preHandled");
      return true;
    }

    initializeMDCAndAuditIDs(request);
    context.setRequestMethod(request.getMethod());
    context.setRequestURI(request.getRequestURI());

    // if it's not a `HandlerMethod`, then ignore
    if (!(object instanceof final HandlerMethod handlerMethod)) {
      log.trace("END: preHandle: not a HandlerMethod");
      return true;
    }

    final var method = handlerMethod.getMethod();
    final var auditSecurityAnnotation = method.getAnnotation(ServletEvent.class);
    // if it's not annotated with `AuditSecurityEvent`, then ignore
    if (null == auditSecurityAnnotation) {
      log.trace("END: preHandle: not annotated with AuditSecurityEvent");
      return true;
    }
    String produces = getProduces(method);
    if (null != produces) {
      context.setProduces(produces);
      log.info("Produces: {}", produces);
    } else {
      log.warn("No @RequestMapping annotation found on method: {}", method);
    }

    context.setPreHandled(true);
    request.setAttribute(ServletEventContext.class.getName(), context);

    populateAuditHeaders(request, auditSecurityAnnotation.headerNames());

    log.trace("request: {}", request);
    log.trace("response: {}", response);
    log.trace("object: {}", object);
    log.trace("State: Before request reaches controller");

    log.trace("XXX: END: preHandle");
    return true;
  }

  private String getProduces(Method method) {
    var produces = Stream.<Supplier<String[]>>of(
            () -> Optional.ofNullable(AnnotationUtils.findAnnotation(method, GetMapping.class)).map(GetMapping::produces).orElse(null),
            () -> Optional.ofNullable(AnnotationUtils.findAnnotation(method, PostMapping.class)).map(PostMapping::produces).orElse(null),
            () -> Optional.ofNullable(AnnotationUtils.findAnnotation(method, PutMapping.class)).map(PutMapping::produces).orElse(null),
            () -> Optional.ofNullable(AnnotationUtils.findAnnotation(method, PatchMapping.class)).map(PatchMapping::produces).orElse(null),
            () -> Optional.ofNullable(AnnotationUtils.findAnnotation(method, DeleteMapping.class)).map(DeleteMapping::produces).orElse(null))
        .map(Supplier::get)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);

    if (produces != null && produces.length > 0) {
      var prodcucesStr = produces[0];
      log.info("Produces: {}", prodcucesStr);
      return prodcucesStr;
    } else {
      log.warn("No produces attribute or @Mapping based annotation found on method: {}", method);
    }

    return null;
  }

  @Around("@annotation(com.baasflow.commons.audit.rest.ServletEvent)")
  public Object handleAnnotatedMethodCall(final ProceedingJoinPoint joinPoint) throws Throwable {
    log.trace("START: handleAnnotatedMethodCall");

    final var methodSignature = getMethodSignature(joinPoint);
    handleMethodCall(joinPoint, methodSignature);

    // Call REST method
    final Object jointPointResult;
    try {
      jointPointResult = joinPoint.proceed();
      context.setResponseObject(jointPointResult);
    } catch (final Exception e) {
      context.extractProblemDetail(e);
      throw e;
    }

    context.collectCorrelationIDsFromResult(jointPointResult);

    log.trace("END: handleAnnotatedMethodCall");
    return jointPointResult;
  }

  /**
   * Handle ProblemDetail if {@link org.springframework.web.bind.annotation.ControllerAdvice} is used.
   *
   * @see #beforeBodyWrite(Object, MethodParameter, MediaType, Class, ServerHttpRequest, ServerHttpResponse)
   */
  @Override
  public boolean supports(final MethodParameter returnType, final Class converterType) {
    final var method = returnType.getMethod();
    if (null == method) {
      return false;
    }
    final var assignableFrom = method.getReturnType().isAssignableFrom(ProblemDetail.class);
    log.trace("supports: {}", assignableFrom);
    return assignableFrom;
  }

  /**
   * Handle ProblemDetail if {@link org.springframework.web.bind.annotation.ControllerAdvice} is used.
   */
  @Override
  public Object beforeBodyWrite(@Nullable final Object body, final MethodParameter returnType, final MediaType selectedContentType,
      final Class selectedConverterType, final ServerHttpRequest request, final ServerHttpResponse response) {
    log.trace("beforeBodyWrite: {}", body);

    if (body instanceof final ProblemDetail problemDetail) {
      // TODO Check response status code is the same as ProblemDetail status code
      context.setProblemDetail(problemDetail);
    }
    return body;
  }

  @Override
  public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response, final Object object,
      @Nullable final Exception methodException) {
    log.trace("START: afterCompletion: {}", object);

    context.setStatusCode(response.getStatus());
    log.trace("Status: {}", context.getStatusCode());

    if (context.isPostHandled()) {
      context.setSuccess(true);
    } else {
      if (object instanceof final ProblemDetail problemDetail) {
        log.trace("ProblemDetail: {}", problemDetail);
        context.setFromProblemDetail(problemDetail);
      } else {
        log.warn("Not postHandled yet! object: {}\n{}\n{}", object, response, methodException);
        if (null == context.getOperationId()) {
          context.setOperationId(request.getRequestURI());
        }
        context.setSuccess(false);
      }
    }

    publishEvent();

    log.trace("END: afterCompletion");
  }

  private void handleMethodCall(final ProceedingJoinPoint joinPoint, @Nullable final MethodSignature methodSignature) {
    if (null == methodSignature) {
      return;
    }

    final var method = methodSignature.getMethod();
    final var auditSecurityEventAnnotation = method.getAnnotation(ServletEvent.class);
    // BTW If not exists how called???
    if (null == auditSecurityEventAnnotation) {
      return;
    }
    final var openApiOperation = method.getAnnotation(io.swagger.v3.oas.annotations.Operation.class);
    mapper.toServletEventContext(auditSecurityEventAnnotation, openApiOperation, context);

    final var args = joinPoint.getArgs();
    context.populateAuditInfoFromMethodParams(method, args);
  }

  private void publishEvent() {
    // Send message to event publisher
    final var securityEventType = new ServletEventPublisher.ServletEvent();
    mapper.toServletEvent(context, securityEventType);
    auditEventPublisher.publish(securityEventType);
  }

  /**
   * Populates audit headers in the context params based on the provided header names from the {@code AuditSecurityEvent} annotation.
   *
   * @param request          The HTTP servlet request.
   * @param auditHeaderNames The audit header names.
   */
  private void populateAuditHeaders(final HttpServletRequest request, final String[] auditHeaderNames) {
    if (ObjectUtils.isEmpty(auditHeaderNames)) {
      return;
    }

    for (final var headerName : auditHeaderNames) {
      final var headerValue = request.getHeader(headerName);
      context.appendParamIfValueIsNotNull(headerName, headerValue);
    }
  }

  private void initializeMDCAndAuditIDs(final HttpServletRequest request) {
    //MDC.clear();
    final var trackingId = getTrackingId(request);
    putToMDCIfValueIsNotNull(REQUEST_X_TRACKING_ID, trackingId);
    context.appendParamIfValueIsNotNull(REQUEST_X_TRACKING_ID, trackingId);
    log.trace("trackingId: {}", trackingId);

    if (null != auditHttpHeaderName) {
      final var sessionId = request.getHeader(auditHttpHeaderName);
      putToMDCIfValueIsNotNull(REQUEST_SESSION_ID, sessionId);
      context.appendParamIfValueIsNotNull(REQUEST_SESSION_ID, sessionId);
      log.trace("sessionId: {}", sessionId);
    }

    if (null != tenantHttpHeaderName) {
      final var tenantId = Optional.ofNullable(request.getHeader(tenantHttpHeaderName)).orElse("default");
      context.setTenant(tenantId);
      putToMDCIfValueIsNotNull(TENANT_ID, tenantId);
      log.trace("tenantId: {}", tenantId);
    }
  }

  private String getTrackingId(final HttpServletRequest httpServletRequest) {
    return TRACKING_ID_HEADERS.stream()
        .map(httpServletRequest::getHeader)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(UUID.randomUUID().toString());
  }

  private void putToMDCIfValueIsNotNull(final String key, @Nullable final String value) {
    if (null == value) {
      return;
    }
    MDC.put(key, value);
  }
}