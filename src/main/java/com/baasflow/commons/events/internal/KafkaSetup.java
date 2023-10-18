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

import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
public class KafkaSetup {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    EventsConfigProperties eventsConfigProperties;


    @PostConstruct
    public void kafkaTemplates() {
        if (eventsConfigProperties.isDisabled()) {
            logger.warn("Baasflow Events library is disabled in the configuration");
            return;
        }

        Map<String, EventsConfigProperties.Event> channels = eventsConfigProperties.getChannels();
        if (channels != null) {
            EventsConfigProperties.KafkaProperties globalKafkaProperties = eventsConfigProperties.getKafka();
            for (String channel : channels.keySet()) {
                var localKafkaProperties = channels.get(channel).getKafka();
                var producerFactory = createProducerFactory(channel, globalKafkaProperties, localKafkaProperties);
                localKafkaProperties.setKafkaTemplate(new KafkaTemplate<>(producerFactory));
            }
        } else {
            logger.warn("no Events set up in application.yml");
        }
    }

    private ProducerFactory<String, byte[]> createProducerFactory(String channel, EventsConfigProperties.KafkaProperties global, EventsConfigProperties.KafkaProperties local) {
        var brokers = getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getBrokers);
        var keySerializer = getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getKeySerializer);
        var valueSerializer = getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getValueSerializer);
        var connectionTimeoutMs = getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getConnectionTimeoutMs);
        var requestTimeoutMs = getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getRequestTimeoutMs);
        var deliveryTimeoutMs = getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getDeliveryTimeoutMs);
        var retryBackoffMs = getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getRetryBackoffMs);
        var maxBlockMs = getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getMaxBlockMs);
        var retriesCount = getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getRetriesCount);
        var msk = getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getMsk);

        var properties = new HashMap<String, Object>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        properties.put(ProducerConfig.SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG, connectionTimeoutMs);
        properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs);
        properties.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeoutMs);
        properties.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, retryBackoffMs);
        properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, maxBlockMs);
        properties.put(ProducerConfig.RETRIES_CONFIG, retriesCount);

        if (msk) {
            properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
            properties.put(SaslConfigs.SASL_MECHANISM, "AWS_MSK_IAM");
            properties.put(SaslConfigs.SASL_JAAS_CONFIG, "software.amazon.msk.auth.iam.IAMLoginModule required;");
            properties.put(SaslConfigs.SASL_CLIENT_CALLBACK_HANDLER_CLASS, "software.amazon.msk.auth.iam.IAMClientCallbackHandler");
        }

        logger.info("kafka producer config for channel {}: {}", channel, properties);
        return new DefaultKafkaProducerFactory<>(properties);
    }

    static <T, S> T getLocalOrFallback(S global, S local, Function<S, T> f) {
        return Optional.ofNullable(local).map(f).orElseGet(() -> f.apply(global));
    }
}
