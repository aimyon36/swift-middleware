package com.bank.swift.notification;

import com.bank.swift.model.entity.NotificationLog;
import com.bank.swift.model.entity.SystemSubscription;
import com.bank.swift.model.event.MessageParsedEvent;
import com.bank.swift.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP回调通知发送器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HttpNotificationSender {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    private static final int MAX_RETRY = 3;

    /**
     * 异步发送HTTP通知
     */
    @Async("notificationExecutor")
    public CompletableFuture<Boolean> sendAsync(MessageParsedEvent event, SystemSubscription subscription) {
        log.info("HTTP发送通知: messageId={}, system={}",
                event.getMessage().getMessageId(), subscription.getSystemCode());

        String url = subscription.getEndpointUrl();
        if (url == null || url.isBlank()) {
            log.warn("订阅 {} 未配置HTTP端点", subscription.getSystemCode());
            return CompletableFuture.completedFuture(false);
        }

        // 构建请求体
        Map<String, Object> payload = buildPayload(event, subscription);

        // 创建通知日志
        NotificationLog logEntry = new NotificationLog();
        logEntry.setMessageId(event.getMessage().getId());
        logEntry.setTargetSystem(subscription.getSystemCode());
        logEntry.setChannel("HTTP");
        logEntry.setStatus("PENDING");
        logEntry.setRetryCount(0);

        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            logEntry.setRequestPayload(jsonPayload);
            notificationService.saveLog(logEntry);

            // 发送HTTP请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            addAuthHeaders(headers, subscription);

            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                logEntry.setStatus("SUCCESS");
                logEntry.setResponsePayload(response.getBody());
                notificationService.updateStatus(logEntry);
                log.info("HTTP通知成功: messageId={}, system={}",
                        event.getMessage().getMessageId(), subscription.getSystemCode());
                return CompletableFuture.completedFuture(true);
            } else {
                logEntry.setStatus("FAILED");
                logEntry.setErrorMessage("HTTP " + response.getStatusCode());
                notificationService.updateStatus(logEntry);
                return CompletableFuture.completedFuture(false);
            }

        } catch (Exception e) {
            log.error("HTTP通知失败: messageId={}, system={}, error={}",
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

    private void addAuthHeaders(HttpHeaders headers, SystemSubscription subscription) {
        switch (subscription.getAuthType()) {
            case "BASIC" -> {
                String auth = subscription.getAuthUsername() + ":" + subscription.getAuthPassword();
                byte[] encodedAuth = java.util.Base64.getEncoder().encode(auth.getBytes());
                headers.set("Authorization", "Basic " + new String(encodedAuth));
            }
            case "BEARER_TOKEN" -> headers.set("Authorization", "Bearer " + subscription.getAuthPassword());
        }
    }
}
