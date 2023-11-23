/*
 * Licensed to BaaSFlow Corporation "BaaSFlow" under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  BaaSFlow licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this  file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.baasFlow.com/licenses/Apache_LICENSE-2.0
 * or the root of this project.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
}
