package com.baasflow.events.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventSender {
    public void send() {
        log.info("send invoked");
    }
}
