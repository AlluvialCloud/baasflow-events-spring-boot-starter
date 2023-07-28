package com.baasflow.events.internal.test;

import com.baasflow.events.EventService;
import com.baasflow.events.EventStatus;
import com.baasflow.events.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SendingTestComponent implements CommandLineRunner {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    EventService eventService;

    @Value("${baasflow.events.testing:false}")
    boolean testing;

    @Override
    public void run(String... args) throws InterruptedException {
        if (testing) {
            logger.warn("running in testing mode");
            Thread.sleep(1000);

            while (true) {
                int count = (int) (Math.random() * 10000);
                if (count % 2 == 0) {
                    eventService.sendAuditlog("source" + count, "event" + count, Math.random() > 0.3 ? EventStatus.success : EventStatus.failure);
                } else {
                    eventService.sendEvent("source" + count, "event" + count, EventType.business, Math.random() > 0.3 ? EventStatus.success : EventStatus.failure, "payload" + count, "payload" + count);
                }
                Thread.sleep(5000);
            }
        }
    }
}