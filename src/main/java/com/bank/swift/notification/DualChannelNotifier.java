package com.bank.swift.notification;

import com.bank.swift.model.event.MessageParsedEvent;
import com.bank.swift.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 双通道通知器
 * 监听报文解析完成事件，并行发送HTTP和MQ通知
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DualChannelNotifier {

    private final NotificationService notificationService;

    /**
     * 监听报文解析完成事件
     */
    @EventListener
    @Async("notificationExecutor")
    public void onMessageParsed(MessageParsedEvent event) {
        log.info("收到报文解析事件: messageId={}, type={}",
                event.getMessage().getMessageId(),
                event.getMessage().getMessageType());

        try {
            notificationService.sendDualChannel(event);
        } catch (Exception e) {
            log.error("双通道通知失败: messageId={}, error={}",
                    event.getMessage().getMessageId(), e.getMessage(), e);
        }
    }
}
