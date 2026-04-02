package com.bank.swift.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * SAA入向请求（从SWIFT接收报文）
 */
@Data
public class SAAInboundRequest {
    private String messageId;
    private String messageType;
    private String senderBic;
    private String receiverBic;
    private String rawXml;
    private LocalDateTime sentTime;
}
