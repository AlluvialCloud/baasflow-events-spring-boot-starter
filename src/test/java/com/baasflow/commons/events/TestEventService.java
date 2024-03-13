package com.baasflow.commons.events;

import com.baasflow.commons.events.internal.KafkaSender;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TestEventService {
    private static Logger logger = LoggerFactory.getLogger(TestEventService.class);

    @Test
    public void testSendWithBuilder() throws Exception {
        EventService eventService = new EventService();
        eventService.kafkaSender = mock(KafkaSender.class);

        ArgumentCaptor<Event> valueCapture = ArgumentCaptor.forClass(Event.class);
        doReturn(null).when(eventService.kafkaSender).send(valueCapture.capture());

        eventService.sendEvent(event ->
                event.setPayload("payload")
                        .setPayloadFormat("text/plain")
                        .setPayloadType("string")
                        .setSourceModule("source module"));

        verify(eventService.kafkaSender, Mockito.atLeastOnce()).send(any());

        Event captured = valueCapture.getValue();
        assertEquals("payload", captured.getPayload());
        assertEquals("text/plain", captured.getPayloadFormat());
        assertEquals("string", captured.getPayloadType());
        assertEquals("source module", captured.getSourceModule());
    }

    @Test
    public void testAuditedEventSucceeds() throws Exception {
        EventService eventService = mockEventService(EventStatus.unknown);

        eventService.auditedEvent(event -> event.setEventStatus(EventStatus.unknown), event -> {
            assertEquals(EventStatus.unknown, event.getEventStatus());
            System.out.println("processing");
            return null;
        });
    }

    @Test
    public void testAuditedEventFails() throws Exception {
        EventService eventService = mockEventService(EventStatus.failure);

        try {
            eventService.auditedEvent(event -> event.setEventStatus(EventStatus.unknown)
                            .setPayload("payload")
                            .setCorrelationIds(Map.of("a", "b"))
                            .setTenantId("tenantId")
                            .setSourceModule("sourceModule")
                            .setEvent("test event")
                    , event -> {
                assertEquals(EventStatus.unknown, event.getEventStatus());
                System.out.println("processing");
                throw new RuntimeException("test error while processing");
            });
            fail(); // exception should get thrown here
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testAuditedEventDefaultTypeAndStatus() throws Exception {
        EventService eventService = mockEventService(EventStatus.success);
        eventService.auditedEvent(event -> event, event -> {
            assertEquals(EventType.audit, event.getEventType());
            return null;
        });
    }

    private static EventService mockEventService(EventStatus expectedStatus) throws IOException {
        EventService eventService = new EventService();
        eventService.kafkaSender = mock(KafkaSender.class);
        doAnswer(invocationOnMock -> {
            logger.info("sending to kafka: " + invocationOnMock.getArgument(0));
            Event event = invocationOnMock.getArgument(0, Event.class);
            assertNotNull(event.getEventType());
            assertNotNull(event.getEventStatus());
            assertEquals(expectedStatus, event.getEventStatus());
            return null;
        }).when(eventService.kafkaSender).send(any());
        return eventService;
    }
}
