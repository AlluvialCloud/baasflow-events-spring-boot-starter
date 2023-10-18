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

import com.baasflow.commons.events.Event;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

@Service
@DependsOn("kafkaSetup")
public class KafkaSender {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    EventsConfigProperties eventsConfigProperties;


    @PostConstruct
    public void init() {
        if (eventsConfigProperties.isDisabled()) {
            logger.warn("Baasflow Events module is disabled in configuration");
            return;
        }

        eventsConfigProperties.getChannels().forEach((key, value) -> {
            logger.info("triggering Kafka producer initialization for channel '{}'", key);
            value.getKafka().getKafkaTemplate().metrics();
        });
    }

    public CompletableFuture<SendResult<String, byte[]>> send(Event event) throws IOException {
        if (eventsConfigProperties.isDisabled()) {
            logger.warn("skip sending event to Kafka, Baasflow Events module is disabled in configuration");
            return null;
        }

        String eventId = event.getId().toString();
        byte[] serialized = serialize(event);

        var properties = eventsConfigProperties.getChannels().get(event.getEventType().name());
        if (properties == null) {
            throw new RuntimeException("Failed to find Events Channel configuration for event type: " + event.getEventType().name());
        }
        var kafkaTemplate = properties.getKafka().getKafkaTemplate();
        var topic = properties.getTopic();

        logger.info("sending {} event {} to topic {}: {}", event.getEventType().name(), eventId, topic, event);
        CompletableFuture<SendResult<String, byte[]>> future = kafkaTemplate.send(topic, eventId, serialized);

        if (logger.isTraceEnabled()) {
            future.thenRun(() -> logger.trace("%% EVENT SENT to topic: {}: {}", topic, eventId));
        }
        future.exceptionally(e -> {
            var message = new String(Base64.getEncoder().encode(serialized), StandardCharsets.UTF_8);
            logger.error("%% EVENT SENDING FAILED to topic: {}: {}", topic, message, e);
            return null;
        });
        return future;
    }

    byte[] serialize(Event event) throws IOException {
        byte[] message = event.toByteBuffer().array();
        logger.trace("serialized event {} to {} bytes", event.getId(), message.length);
        return message;
    }
}
