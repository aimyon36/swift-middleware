package com.bank.swift.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报文路由规则
 */
@Data
@TableName("message_route")
public class MessageRoute {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 报文类型，如 pacs.008.001.08
     */
    private String messageType;

    /**
     * 业务场景
     */
    private String businessScenario;

    /**
     * 目标系统代码
     */
    private String targetSystem;

    /**
     * 路由规则表达式
     */
    private String routingRule;

    /**
     * 优先级
     */
    private Integer priority;

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
