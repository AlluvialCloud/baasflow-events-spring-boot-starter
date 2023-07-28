package com.baasflow.events;

import com.baasflow.events.EventService;
import com.baasflow.events.EventStatus;
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

    @Value("${auditlog.testing:false}")
    boolean testing;

    @Override
    public void run(String... args) throws InterruptedException {
        if (testing) {
            logger.warn("running in testing mode");
            Thread.sleep(1000);

            while (true) {
                logger.info("sending sample event to kafka");
                int count = (int) (Math.random() * 10000);
                eventService.sendAuditlog("source" + count, "event" + count, Math.random() > 0.3 ? EventStatus.success : EventStatus.failure);

                Thread.sleep(5000);
            }
        }
    }
}