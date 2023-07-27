package com.baasflow.events;

import com.baasflow.events.internal.KafkaSender;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TestAuditService {

    @Test
    public void testSendWithBuilder() throws Exception {
        AuditService auditService = new AuditService();
        auditService.kafkaSender = mock(KafkaSender.class);

        ArgumentCaptor<Event> valueCapture = ArgumentCaptor.forClass(Event.class);
        doNothing().when(auditService.kafkaSender).send(valueCapture.capture());

        auditService.sendEvent(event ->
                event.setPayload("payload")
                        .setPayloadType("string")
                        .setSourceModule("source module"));

        verify(auditService.kafkaSender, Mockito.atLeastOnce()).send(any());

        Event captured = valueCapture.getValue();
        assertEquals("payload", captured.getPayload());
        assertEquals("string", captured.getPayloadType());
        assertEquals("source module", captured.getSourceModule());
    }
}