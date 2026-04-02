package com.bank.swift.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * SAA入向响应
 */
@Data
public class SAAInboundResponse {
    private String messageId;
    private String status;
    private LocalDateTime receivedAt;
}
