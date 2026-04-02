package com.bank.swift.notification;

import com.bank.swift.model.event.MessageParsedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 事件总线
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventBus {

    private final ApplicationEventPublisher publisher;

    /**
     * 发布报文解析完成事件
     */
    public void publish(MessageParsedEvent event) {
        log.info("发布MessageParsedEvent: messageId={}, type={}",
                event.getMessage().getMessageId(),
                event.getMessage().getMessageType());
        publisher.publishEvent(event);
    }
}
