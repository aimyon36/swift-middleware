package com.bank.swift.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 内部系统订阅配置
 */
@Data
@TableName("system_subscription")
public class SystemSubscription {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 系统代码
     */
    private String systemCode;

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 端点类型：HTTP / MQ
     */
    private String endpointType;

    /**
     * 端点URL（HTTP回调地址或MQ地址）
     */
    private String endpointUrl;

    /**
     * MQ类型：KAFKA / ROCKETMQ / RABBITMQ
     */
    private String mqType;

    /**
     * MQ Topic / Routing Key
     */
    private String mqTopic;

    /**
     * 订阅的报文类型列表（逗号分隔）
     */
    private String messageTypes;

    /**
     * 认证类型：NONE / BASIC / BEARER_TOKEN
     */
    private String authType;

    /**
     * 认证用户名
     */
    private String authUsername;

    /**
     * 认证密码（加密存储）
     */
    private String authPassword;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 超时时间（毫秒）
     */
    private Integer timeoutMs;

    /**
     * 是否启用
     */
    private Boolean enabled;

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
