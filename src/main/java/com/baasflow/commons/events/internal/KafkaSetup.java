/*
 * Licensed to BaaSFlow Corporation "BaaSFlow" under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  BaaSFlow licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this  file except in compliance
 * with the License. You may obtain a copy of the License at
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

import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class KafkaSetup {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${baasflow.events.kafka.producer.key-serializer:org.apache.kafka.common.serialization.StringSerializer}")
    String keySerializer;

    @Value("${baasflow.events.kafka.producer.value-serializer:org.apache.kafka.common.serialization.ByteArraySerializer}")
    String valueSerializer;

    @Autowired
    KafkaConfigProperties kafkaConfigProperties;


    @PostConstruct
    public void kafkaTemplates() {
        logger.info("setting up kafka producers");
        for (String key : kafkaConfigProperties.getEvents().keySet()) {
            var properties = kafkaConfigProperties.getEvents().get(key).getKafka();
            logger.info("setting up kafka producer for '{}' events using brokers: {}", key, properties.getBrokers());
            var producerFactory = createProducerFactory(properties.getBrokers());
            properties.setKafkaTemplate(new KafkaTemplate<>(producerFactory));
        }
    }

    private ProducerFactory<String, byte[]> createProducerFactory(String brokers) {
        var configProps = new HashMap<String, Object>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        return new DefaultKafkaProducerFactory<>(configProps);
    }
}
