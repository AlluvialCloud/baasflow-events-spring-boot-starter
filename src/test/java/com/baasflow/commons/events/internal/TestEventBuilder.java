package com.baasflow.commons.events.internal;

import com.baasflow.commons.events.Event;
import com.baasflow.commons.events.EventStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TestEventBuilder {

    private EventBuilder builder = new EventBuilder();

    @Test
    public void testPlain() throws JSONException {
        Event event = builder.auditlogEvent("sample-module", "event-happened", EventStatus.success);
        JSONObject json = new JSONObject(event.toString());

        assertEquals(UUID.randomUUID().toString().length(), json.getString("id").length());
        assertEquals("sample-module", json.getString("sourceModule"));
        assertEquals("event-happened", json.getString("event"));
        assertEquals("success", json.getString("eventStatus"));
    }

    @Test
    public void testCorrelation() throws JSONException {
        Event event = builder.auditlogEvent("sample-module", "event-happened", EventStatus.success, Map.of("corrId", "val1"));
        JSONObject json = new JSONObject(event.toString());
        System.out.println(json);
    }
}
