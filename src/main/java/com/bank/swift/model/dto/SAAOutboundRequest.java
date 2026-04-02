package com.bank.swift.model.dto;

import lombok.Data;

/**
 * SAA出向请求（发送报文到SWIFT）
 */
@Data
public class SAAOutboundRequest {
    private String messageType;
    private String senderBic;
    private String receiverBic;
    private BusinessDataDTO businessData;
    private String callbackUrl;
}
