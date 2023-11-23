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

package com.baasflow.commons.events.internal.test;

import com.baasflow.commons.events.EventService;
import com.baasflow.commons.events.EventStatus;
import com.baasflow.commons.events.EventType;
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
