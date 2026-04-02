package com.bank.swift.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 统一业务数据DTO
 */
@Data
public class BusinessDataDTO {
    private String sourceAccount;
    private String destAccount;
    private String sourceBank;
    private String destBank;
    private String senderBic;
    private String receiverBic;
    private BigDecimal amount;
    private String currency;
    private LocalDate valueDate;
    private String reference;
    private String purpose;
}
