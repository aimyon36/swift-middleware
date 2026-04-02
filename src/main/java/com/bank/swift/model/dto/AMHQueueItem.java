package com.bank.swift.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AMH队列消息项
 */
@Data
public class AMHQueueItem {
    private String queueId;
    private String messageId;
    private String destination;
    private String status;
    private LocalDateTime queuedAt;
}
