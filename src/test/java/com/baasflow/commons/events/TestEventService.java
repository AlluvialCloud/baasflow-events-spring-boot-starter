package com.baasflow.commons.events;

import com.baasflow.commons.events.internal.KafkaSender;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TestEventService {

    @Test
    public void testSendWithBuilder() throws Exception {
        EventService eventService = new EventService();
        eventService.kafkaSender = mock(KafkaSender.class);

        ArgumentCaptor<Event> valueCapture = ArgumentCaptor.forClass(Event.class);
        doNothing().when(eventService.kafkaSender).send(valueCapture.capture());

        eventService.sendEvent(event ->
                event.setPayload("payload")
                        .setPayloadType("string")
                        .setSourceModule("source module"));

        verify(eventService.kafkaSender, Mockito.atLeastOnce()).send(any());

        Event captured = valueCapture.getValue();
        assertEquals("payload", captured.getPayload());
        assertEquals("string", captured.getPayloadType());
        assertEquals("source module", captured.getSourceModule());
    }

    @Test
    public void testAuditedEventSucceeds() throws Exception {
        EventService eventService = new EventService();
        eventService.kafkaSender = mock(KafkaSender.class);
        doAnswer(invocationOnMock -> {
            System.out.println("sending to kafka: " + invocationOnMock.getArgument(0));
            assertEquals(EventStatus.success, invocationOnMock.getArgument(0, Event.class).getEventStatus());
            return null;
        }).when(eventService.kafkaSender).send(any());

        eventService.auditedEvent(event -> event.setEventStatus(EventStatus.unknown), event -> {
            assertEquals(EventStatus.unknown, event.getEventStatus());
            System.out.println("processing");
            return null;
        });
    }

    @Test
    public void testAuditedEventFails() throws Exception {
        EventService eventService = new EventService();
        eventService.kafkaSender = mock(KafkaSender.class);
        doAnswer(invocationOnMock -> {
            System.out.println("sending to kafka: " + invocationOnMock.getArgument(0));
            assertEquals(EventStatus.failure, invocationOnMock.getArgument(0, Event.class).getEventStatus());
            return null;
        }).when(eventService.kafkaSender).send(any());

        try {
            eventService.auditedEvent(event -> event.setEventStatus(EventStatus.unknown), event -> {
                assertEquals(EventStatus.unknown, event.getEventStatus());
                System.out.println("processing");
                throw new RuntimeException("test error while processing");
            });
            fail(); // exception should get thrown here
        } catch (Exception e) {
            // expected
        }
    }
}
