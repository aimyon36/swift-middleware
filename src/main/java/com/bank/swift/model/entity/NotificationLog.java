package com.bank.swift.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知日志
 */
@Data
@TableName("notification_log")
public class NotificationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联报文ID
     */
    private Long messageId;

    /**
     * 目标系统代码
     */
    private String targetSystem;

    /**
     * 通知渠道：HTTP / MQ
     */
    private String channel;

    /**
     * 状态：PENDING / SUCCESS / FAILED
     */
    private String status;

    /**
     * 请求payload
     */
    private String requestPayload;

    /**
     * 响应payload
     */
    private String responsePayload;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * MQ消息偏移量
     */
    private Long mqOffset;

    /**
     * 重试次数
     */
    private Integer retryCount;

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
}
