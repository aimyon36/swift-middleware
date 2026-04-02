package com.bank.swift.notification;

import com.bank.swift.model.entity.NotificationLog;
import com.bank.swift.model.entity.SystemSubscription;
import com.bank.swift.model.event.MessageParsedEvent;
import com.bank.swift.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * MQ消息通知发送器 (RabbitMQ)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqNotificationSender {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @Value("${swift.notification.mq.exchange}")
    private String exchangeName;

    /**
     * 异步发送MQ消息
     */
    @Async("notificationExecutor")
    public CompletableFuture<Boolean> sendAsync(MessageParsedEvent event, SystemSubscription subscription) {
        log.info("MQ发送通知: messageId={}, system={}, queue={}",
                event.getMessage().getMessageId(),
                subscription.getSystemCode(),
                subscription.getMqTopic());

        String routingKey = subscription.getMqTopic();
        if (routingKey == null || routingKey.isBlank()) {
            log.warn("订阅 {} 未配置MQ RoutingKey", subscription.getSystemCode());
            return CompletableFuture.completedFuture(false);
        }

        // 创建通知日志
        NotificationLog logEntry = new NotificationLog();
        logEntry.setMessageId(event.getMessage().getId());
        logEntry.setTargetSystem(subscription.getSystemCode());
        logEntry.setChannel("MQ");
        logEntry.setStatus("PENDING");
        logEntry.setRetryCount(0);

        try {
            // 构建消息内容
            Map<String, Object> payload = buildPayload(event, subscription);
            String jsonPayload = objectMapper.writeValueAsString(payload);
            logEntry.setRequestPayload(jsonPayload);
            notificationService.saveLog(logEntry);

            // 发送RabbitMQ消息
            rabbitTemplate.convertAndSend(exchangeName, routingKey, payload);

            log.info("MQ发送成功: messageId={}, system={}, routingKey={}",
                    event.getMessage().getMessageId(),
                    subscription.getSystemCode(),
                    routingKey);

            logEntry.setStatus("SUCCESS");
            logEntry.setResponsePayload("routingKey:" + routingKey);
            notificationService.updateStatus(logEntry);

            return CompletableFuture.completedFuture(true);

        } catch (Exception e) {
            log.error("MQ通知构建失败: messageId={}, system={}, error={}",
                    event.getMessage().getMessageId(), subscription.getSystemCode(), e.getMessage());
            logEntry.setStatus("FAILED");
            logEntry.setErrorMessage(e.getMessage());
            notificationService.updateStatus(logEntry);
            return CompletableFuture.completedFuture(false);
        }
    }

    private Map<String, Object> buildPayload(MessageParsedEvent event, SystemSubscription subscription) {
        return Map.of(
                "messageId", event.getMessage().getMessageId(),
                "messageType", event.getMessage().getMessageType(),
                "businessData", event.getParsedData(),
                "timestamp", event.getOccurredOn().toString(),
                "notifySystem", subscription.getSystemCode()
        );
    }
}
