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

package com.baasflow.commons.events.internal;

import com.baasflow.commons.events.Event;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class KafkaSender {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    KafkaConfigProperties kafkaConfigProperties;


    @PostConstruct
    public void init() {
        logger.info("Events is set up using the following configuration: {}", kafkaConfigProperties);
    }

    public void send(Event event) throws IOException {
        String eventId = event.getId().toString();
        byte[] serialized = serialize(event);

        var properties = kafkaConfigProperties.getEvents().get(event.getEventType().name()).getKafka();
        var kafkaTemplate = properties.getKafkaTemplate();
        var topic = properties.getTopic();
        logger.info("sending {} event {} to topic {}: {}", event.getEventType().name(), eventId, topic, event);
        kafkaTemplate.send(topic, eventId, serialized);
    }

    byte[] serialize(Event event) throws IOException {
        byte[] message = event.toByteBuffer().array();
        logger.debug("serialized event {} to {} bytes", event.getId(), message.length);
        return message;
    }
}
