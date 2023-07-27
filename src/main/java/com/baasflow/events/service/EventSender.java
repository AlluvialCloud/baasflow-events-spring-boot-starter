package com.baasflow.events.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EventSender {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void send() {
        logger.info("send invoked");
    }
}
