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

import com.amazonaws.services.schemaregistry.utils.AWSSchemaRegistryConstants;
import com.baasflow.commons.events.Event;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.config.SaslConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.ProducerListener;
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

    @Autowired
    KafkaHealthIndicator kafkaHealthIndicator;


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
                localKafkaProperties.setKafkaTemplate(createKafkaTemplate(producerFactory));
            }
        } else {
            logger.warn("no Events set up in application.yml");
        }
    }

    private KafkaTemplate<String, Event> createKafkaTemplate(ProducerFactory<String, Event> producerFactory) {
        KafkaTemplate<String, Event> kafkaTemplate = new KafkaTemplate<>(producerFactory);
        kafkaTemplate.setProducerListener(new ProducerListener<>() {
            @Override
            public void onSuccess(ProducerRecord<String, Event> producerRecord, RecordMetadata recordMetadata) {
                kafkaHealthIndicator.setHealthy();
            }

            @Override
            public void onError(ProducerRecord<String, Event> producerRecord, RecordMetadata recordMetadata, Exception exception) {
                kafkaHealthIndicator.setUnhealthy(exception);
            }
        });
        return kafkaTemplate;
    }

    private ProducerFactory<String, Event> createProducerFactory(String channel, EventsConfigProperties.KafkaProperties global, EventsConfigProperties.KafkaProperties local) {
        var brokers = getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getBrokers);
        var connectionTimeoutMs = getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getConnectionTimeoutMs);
        var requestTimeoutMs = getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getRequestTimeoutMs);
        var deliveryTimeoutMs = getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getDeliveryTimeoutMs);
        var retryBackoffMs = getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getRetryBackoffMs);
        var maxBlockMs = getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getMaxBlockMs);
        var retriesCount = getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getRetriesCount);
        var msk = getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getMsk);
        var awsRegion = getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getGlueAwsRegion);
        var registryName = getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getGlueRegistryName);
        var schemaName = getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getGlueSchemaName);
        var localSchemaRegistryEndpoint = getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getLocalSchemaRegistryEndpoint);

        var properties = new HashMap<String, Object>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, msk ? "com.amazonaws.services.schemaregistry.serializers.GlueSchemaRegistryKafkaSerializer" : "io.confluent.kafka.serializers.KafkaAvroSerializer");
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

            properties.put(AWSSchemaRegistryConstants.AWS_REGION, awsRegion);
            properties.put(AWSSchemaRegistryConstants.SCHEMA_AUTO_REGISTRATION_SETTING, "true");
            properties.put(AWSSchemaRegistryConstants.DATA_FORMAT, "AVRO");
            properties.put(AWSSchemaRegistryConstants.REGISTRY_NAME, registryName);
            properties.put(AWSSchemaRegistryConstants.SCHEMA_NAME, schemaName);
        } else {
            properties.put("schema.registry.url", localSchemaRegistryEndpoint);
        }

        logger.info("kafka producer config for channel {}: {}", channel, properties.toString().replaceAll(",", "\n"));
        return new DefaultKafkaProducerFactory<>(properties);
    }

    static <T, S> T getLocalOrFallback(S global, S local, Function<S, T> f) {
        return Optional.ofNullable(local).map(f).orElseGet(() -> f.apply(global));
    }
}
