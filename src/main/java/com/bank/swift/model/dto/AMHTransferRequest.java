package com.bank.swift.model.dto;

import lombok.Data;

/**
 * AMH传输请求
 */
@Data
public class AMHTransferRequest {
    private String messageId;
    private String destination;
    private String priority;
}
