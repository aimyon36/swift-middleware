package com.bank.swift.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AMH传输响应
 */
@Data
public class AMHTransferResponse {
    private String transferId;
    private String status;
    private LocalDateTime transferTime;
}
