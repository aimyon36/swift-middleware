package com.bank.swift.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * SAA出向响应
 */
@Data
public class SAAOutboundResponse {
    private String messageId;
    private String status;
    private LocalDateTime submittedAt;
    private LocalDateTime estimatedDelivery;
}
