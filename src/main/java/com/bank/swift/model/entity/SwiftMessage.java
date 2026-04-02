package com.bank.swift.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 原始SWIFT报文
 */
@Data
@TableName("swift_message")
public class SwiftMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 报文唯一ID (SWIFT UIN)
     */
    private String messageId;

    /**
     * 报文类型，如 pacs.008.001.08
     */
    private String messageType;

    /**
     * 方向：INBOUND / OUTBOUND
     */
    private String direction;

    /**
     * 原始报文内容
     */
    private String rawContent;

    /**
     * 状态：RECEIVED / PARSING / PARSED / ROUTING / ROUTED / FAILED
     */
    private String status;

    /**
     * 发送方BIC
     */
    private String senderBic;

    /**
     * 接收方BIC
     */
    private String receiverBic;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 关联的解析后数据ID
     */
    private Long parsedDataId;
}
