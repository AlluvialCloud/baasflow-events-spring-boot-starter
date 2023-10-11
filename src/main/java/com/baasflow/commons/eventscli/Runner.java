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

package com.baasflow.commons.eventscli;

import com.baasflow.commons.events.EventLogLevel;
import com.baasflow.commons.events.EventService;
import com.baasflow.commons.events.EventStatus;
import com.baasflow.commons.events.EventType;
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
        System.setProperty("spring.main.web-application-type", "none");
        SpringApplication application = new SpringApplication(Runner.class);
        application.setBannerMode(Banner.Mode.OFF);
        application.setLogStartupInfo(false);
        application.run(args);
    }
}
