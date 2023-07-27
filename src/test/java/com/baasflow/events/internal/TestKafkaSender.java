package com.baasflow.events.internal;

import com.baasflow.events.Event;
import com.baasflow.events.EventStatus;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TestKafkaSender {

    private KafkaSender kafkaSender = new KafkaSender();

    @Test
    public void testSerialization() throws IOException {
        Event event1 = new AuditEventBuilder().auditlogEvent("sample-module", "event-happened", EventStatus.success);
        Event event2 = new AuditEventBuilder().auditlogEvent("sample-module", "event-happened", EventStatus.success);
        byte[] result1 = kafkaSender.serialize(event1);
        byte[] result2 = kafkaSender.serialize(event1);
        byte[] result3 = kafkaSender.serialize(event2);

        assertArrayEquals(result1, result2);
        assertEquals(new String(result1), new String(result2));
        assertNotEquals(new String(result1), new String(result3));
    }
}