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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "baasflow.events")
public class EventsConfigProperties {
    private boolean disabled;

    @NestedConfigurationProperty
    private KafkaProperties kafka = KafkaProperties.builder()
            .keySerializer("org.apache.kafka.common.serialization.StringSerializer")
            .valueSerializer("org.apache.kafka.common.serialization.ByteArraySerializer")
            .connectionTimeoutMs(5_000)
            .requestTimeoutMs(5_000)
            .deliveryTimeoutMs(5_000)
            .retryBackoffMs(100)
            .maxBlockMs(60_000)
            .retriesCount(1)
            .msk(false)
            .build();

    private Map<String, Event> channels;


    @Data
    @NoArgsConstructor
    public static class Event {
        private String topic;

        @NestedConfigurationProperty
        private KafkaProperties kafka = new KafkaProperties();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class KafkaProperties {
        private Boolean msk;
        private String brokers;
        private String keySerializer;
        private String valueSerializer;

        private Integer connectionTimeoutMs;
        private Integer requestTimeoutMs;
        private Integer deliveryTimeoutMs;
        private Integer retryBackoffMs;
        private Integer maxBlockMs;
        private Integer retriesCount;

        private transient KafkaTemplate<String, byte[]> kafkaTemplate;
    }
}
