package com.baasflow.commons.events.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TestKafkaSetupProperties {

    @Test
    public void testLocalGlobalConfig() {
        EventsConfigProperties.KafkaProperties global = new EventsConfigProperties.KafkaProperties();
        EventsConfigProperties.KafkaProperties local = new EventsConfigProperties.KafkaProperties();
        assertNull(KafkaSetup.getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getBrokers));

        global.setBrokers("global");
        assertEquals("global", KafkaSetup.getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getBrokers));

        local.setBrokers("local");
        assertEquals("local", KafkaSetup.getLocalOrFallback(global, local, EventsConfigProperties.KafkaProperties::getBrokers));
    }
}