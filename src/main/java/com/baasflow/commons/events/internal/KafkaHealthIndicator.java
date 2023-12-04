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
package com.baasflow.commons.events.internal;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component("kafkaHealthIndicator")
public class KafkaHealthIndicator implements HealthIndicator {
    private final AtomicReference<Health> healthStatus = new AtomicReference<>(Health.unknown().build());


    @Override
    public Health health() {
        return healthStatus.get();
    }

    public void setHealthy() {
        if (!isUp()) {
            this.healthStatus.set(Health.up().build());
        }
    }

    public void setUnhealthy(Exception exception) {
        if (!isDown()) {
            this.setUnhealthy(exception == null ? "unknown" : ExceptionUtils.getStackTrace(exception));
        }
    }

    public void setUnhealthy(String detail) {
        if (!isDown()) {
            this.healthStatus.set(Health.down().withDetail("exception", detail).build());
        }

    }

    private boolean isUp() {
        return Status.UP.equals(health().getStatus());
    }

    private boolean isDown() {
        return Status.DOWN.equals(health().getStatus());
    }
}
