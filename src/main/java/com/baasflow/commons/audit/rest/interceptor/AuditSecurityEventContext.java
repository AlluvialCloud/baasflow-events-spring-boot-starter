/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.baasflow.commons.audit.rest.interceptor;

import com.baasflow.commons.audit.rest.Audit;
import com.baasflow.commons.audit.rest.ICorrelationDataProvider;
import com.baasflow.commons.events.EventLogLevel;
import com.baasflow.commons.events.EventType;
import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.CheckForNull;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequestScope
@Data
@Accessors(chain = true)
public class AuditSecurityEventContext {

    @Value("${app.audit.logging.module-name:}")
    private final String defaultSourceModule;
    /**
     * Pre-handled flag to avoid duplicate processing.
     */
    private boolean preHandled = false;
    private boolean postHandled = false;
    private String tenant;
    private String operationId;
    private String domains;
    private EventType eventType;
    private EventLogLevel eventLogLevel;
    private String sourceModule;
    private int statusCode;
    private boolean isSuccess;
    private Map<String, Set<String>> params = new HashMap<>();
    private Object requestObject;
    private Object responseObject;
    private ProblemDetail problemDetail;

    public AuditSecurityEventContext setFromProblemDetail(final ProblemDetail problemDetail) {
        this.problemDetail = problemDetail;
        this.statusCode = problemDetail.getStatus();
        this.isSuccess = false;
        this.postHandled = true;
        return this;
    }

    public void populateAuditInfoFromMethodParams(final Method method, final Object[] args) {
        // Args from REST method calling
        final var parameters = method.getParameters();

        final var corrParameters = this.params;
        IntStream.range(0, parameters.length)
                .filter(index -> null != parameters[index].getAnnotation(Audit.class))
                .forEach(index -> {
                    final var parameter = parameters[index];
                    final var auditParamName = getAuditParamName(parameter);
                    final var value = String.valueOf(args[index]);
                    appendParamIfValueIsNotNull(auditParamName, value);
                    final var addToMDC = Optional.ofNullable(parameter.getAnnotation(Audit.class)).map(Audit::addToMDC).orElse(false);
                    if (addToMDC) {
                        MDC.put(auditParamName, value);
                    }
                });
    }

    private String getAuditParamName(final Parameter parameter) {
        final var auditVariable = Optional.ofNullable(parameter.getAnnotation(Audit.class)).map(Audit::value).orElse(null);
        final var pathVariable = Optional.ofNullable(parameter.getAnnotation(PathVariable.class)).map(PathVariable::name).orElse(null);
        final var requestParam = Optional.ofNullable(parameter.getAnnotation(RequestParam.class)).map(RequestParam::name).orElse(null);
        final var requestHeader = Optional.ofNullable(parameter.getAnnotation(RequestHeader.class)).map(RequestHeader::name).orElse(null);
        final var paramName = parameter.getName();
        // find the first non null value
        return ObjectUtils.firstNonNull(auditVariable, pathVariable, requestParam, requestHeader, paramName);
    }

    public void collectCorrelationIDsFromResult(@Nullable final Object methodCallResult) throws Exception {
        this.postHandled = true;
        final var resultObject = extractResultObject(methodCallResult);
        if (null == resultObject) {
            return;
        }
        // TODO Implements List and Map
        // Response object exposed correlation parameters
        if (resultObject instanceof final ICorrelationDataProvider correlationParamsProvider) {
            final var correlationParameters = correlationParamsProvider.correlationParams();

            if (ObjectUtils.isNotEmpty(correlationParameters)) {
                correlationParameters.forEach((key, value) -> appendParamIfValueIsNotNull(key, value));
            }
        }
    }

    @CheckForNull
    private Object extractResultObject(final Object methodCallResult) throws Exception {
        final Object proceed;
        if (methodCallResult instanceof final CompletableFuture completableFuture) {
            try {
                proceed = completableFuture.get();
            } catch (final Exception e) {
                if (e instanceof ExecutionException) {
                    var cause = e.getCause();
                    extractProblemDetail(cause);
                    throw (Exception) cause;
                } else {
                    extractProblemDetail(e);
                    throw e;
                }
            }
        } else {
            proceed = methodCallResult;
        }

        final Object resultObject;
        // Set additional infos from response
        if (proceed instanceof final ResponseEntity responseEntity) {
            this.statusCode = responseEntity.getStatusCode().value();
            resultObject = responseEntity.getBody();
        } else {
            resultObject = proceed;
        }
        this.setResponseObject(resultObject);
        return resultObject;
    }

    public void extractProblemDetail(final Throwable e) {
        if (e instanceof final ErrorResponse errorResponse) {
            // TODO Check: ProblemDetail has a same status code?
            // context.setStatusCode(errorResponse.getStatusCode().value());
            this.setFromProblemDetail(errorResponse.getBody());
        } else {
            log.debug("XXX: Exception: {} - {}", e.getClass().getName(), e.getMessage());
        }
    }

    public void setStatusCode(final int statusCode) {
        if (0 != this.statusCode && this.statusCode != statusCode) {
            log.warn("Status code is already set to {} and will be overwritten by {}", this.statusCode, statusCode);
        }
        this.statusCode = statusCode;
    }

    /**
     * Retrieves the source module.
     *
     * @return The source module. If the current source module is empty, it returns the default source module.
     */
    public String getSourceModule() {
        return (null == StringUtils.trimToNull(this.sourceModule))
                ? this.defaultSourceModule
                : this.sourceModule;
    }

    /**
     * Appends a parameter with the given key and value to the AuditSecurityEventContext object if the value is not null.
     *
     * @param key   the key of the parameter
     * @param value the value of the parameter
     * @return the original AuditSecurityEventContext object
     */
    public AuditSecurityEventContext appendParamIfValueIsNotNull(final String key, final String value) {
        if (null == value) {
            return this;
        }

        var list = this.params.computeIfAbsent(key, missingKey -> new HashSet<>());
        list.add(value);
        return this;
    }
}
