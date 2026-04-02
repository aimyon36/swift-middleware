package com.bank.swift.model.event;

import com.bank.swift.model.entity.ParsedBusinessData;
import com.bank.swift.model.entity.SwiftMessage;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * 报文解析完成事件
 */
@Getter
public class MessageParsedEvent extends ApplicationEvent {

    private final SwiftMessage message;
    private final ParsedBusinessData parsedData;
    private final LocalDateTime occurredOn;

    public MessageParsedEvent(SwiftMessage message, ParsedBusinessData parsedData) {
        super(message);
        this.message = message;
        this.parsedData = parsedData;
        this.occurredOn = LocalDateTime.now();
    }
}
