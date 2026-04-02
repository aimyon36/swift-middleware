package com.bank.swift.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 解析后业务数据（统一模型）
 */
@Data
@TableName("parsed_business_data")
public class ParsedBusinessData {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联报文ID
     */
    private Long messageId;

    /**
     * 业务类型：PAYMENT / CREDIT_TRANSFER / PAYMENT_REVOKE 等
     */
    private String businessType;

    /**
     * 源账户
     */
    private String sourceAccount;

    /**
     * 目标账户
     */
    private String destAccount;

    /**
     * 源银行BIC
     */
    private String sourceBank;

    /**
     * 目标银行BIC
     */
    private String destBank;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 币种
     */
    private String currency;

    /**
     * 起息日
     */
    private LocalDate valueDate;

    /**
     * 参考号
     */
    private String reference;

    /**
     * 原始解析结果JSON
     */
    private String rawJson;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
