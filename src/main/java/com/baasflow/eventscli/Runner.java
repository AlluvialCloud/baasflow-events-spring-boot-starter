package com.baasflow.eventscli;

import com.baasflow.events.EventLogLevel;
import com.baasflow.events.EventService;
import com.baasflow.events.EventStatus;
import com.baasflow.events.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@SpringBootApplication
@Command(name = "events", mixinStandardHelpOptions = true, description = "Events CLI")
public class Runner implements CommandLineRunner {
    @Autowired
    EventService eventService;

    @Command(name = "SendEvent", mixinStandardHelpOptions = true, version = "1.0", description = "Sends an event using the event service.")
    class SendEventCommand implements Runnable {
        @CommandLine.Option(names = {"-m", "--sourceModule"}, description = "Source module")
        private String sourceModule;
        @CommandLine.Option(names = {"-e", "--event"}, description = "Event")
        private String event;
        @CommandLine.Option(names = {"-t", "--eventType"}, description = "Event type")
        private String eventType;
        @CommandLine.Option(names = {"-l", "--eventLogLevel"}, description = "Event log level")
        private String eventLogLevel;

        @CommandLine.Option(names = {"-s", "--eventStatus"}, description = "Event status")
        private String eventStatus;
        @CommandLine.Option(names = {"-p", "--payload"}, description = "Payload")
        private String payload;
        @CommandLine.Option(names = {"-pt", "--payloadType"}, description = "Payload type")
        private String payloadType;


        @Override
        public void run() {
            var eventTypeResolved = EventType.valueOf(eventType);
            var eventStatusResolved = EventStatus.valueOf(eventStatus);
            var eventLogLevelResolved = EventLogLevel.valueOf(eventLogLevel);

            eventService.sendEvent(eventBuilder -> eventBuilder
                    .setSourceModule(sourceModule)
                    .setEvent(event)
                    .setEventType(eventTypeResolved)
                    .setEventStatus(eventStatusResolved)
                    .setPayload(payload)
                    .setPayloadType(payloadType)
                    .setEventLogLevel(eventLogLevelResolved)
            );
        }
    }


    @Override
    public void run(String... args) throws Exception {
        new CommandLine(new SendEventCommand()).execute(args);
    }

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Runner.class);
        application.setBannerMode(Banner.Mode.OFF);
        application.setLogStartupInfo(false);
        application.run(args);
    }
}
