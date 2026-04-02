package com.bank.swift.service;

import com.bank.swift.model.entity.NotificationLog;
import com.bank.swift.model.entity.SystemSubscription;
import com.bank.swift.model.event.MessageParsedEvent;
import com.bank.swift.notification.HttpNotificationSender;
import com.bank.swift.notification.MqNotificationSender;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.swift.model.entity.NotificationLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通知服务
 */
@Slf4j
@Service
public class NotificationService extends ServiceImpl<NotificationLogMapper, NotificationLog> {

    private final SubscriptionService subscriptionService;
    private final ObjectProvider<HttpNotificationSender> httpSenderProvider;
    private final ObjectProvider<MqNotificationSender> mqSenderProvider;

    public NotificationService(SubscriptionService subscriptionService,
                                ObjectProvider<HttpNotificationSender> httpSenderProvider,
                                ObjectProvider<MqNotificationSender> mqSenderProvider) {
        this.subscriptionService = subscriptionService;
        this.httpSenderProvider = httpSenderProvider;
        this.mqSenderProvider = mqSenderProvider;
    }

    /**
     * 保存通知日志
     */
    public boolean saveLog(NotificationLog logEntry) {
        return baseMapper.insert(logEntry) > 0;
    }

    /**
     * 更新通知状态
     */
    public void updateStatus(NotificationLog logEntry) {
        baseMapper.updateById(logEntry);
    }

    /**
     * 双通道发送通知
     */
    public void sendDualChannel(MessageParsedEvent event) {
        String messageType = event.getMessage().getMessageType();
        log.info("双通道发送通知: messageId={}, type={}",
                event.getMessage().getMessageId(), messageType);

        // 查找匹配的订阅
        List<SystemSubscription> subscriptions = subscriptionService.findByMessageType(messageType);

        for (SystemSubscription subscription : subscriptions) {
            // 根据端点类型选择发送通道
            if ("HTTP".equals(subscription.getEndpointType())) {
                HttpNotificationSender sender = httpSenderProvider.getIfAvailable();
                if (sender != null) {
                    sender.sendAsync(event, subscription);
                }
            } else if ("MQ".equals(subscription.getEndpointType())) {
                MqNotificationSender sender = mqSenderProvider.getIfAvailable();
                if (sender != null) {
                    sender.sendAsync(event, subscription);
                }
            }
        }
    }

    /**
     * 分页查询通知日志
     */
    public Page<NotificationLog> queryLogs(Long messageId, String targetSystem,
                                            String status, int page, int size) {
        LambdaQueryWrapper<NotificationLog> wrapper = new LambdaQueryWrapper<>();
        if (messageId != null) {
            wrapper.eq(NotificationLog::getMessageId, messageId);
        }
        if (targetSystem != null) {
            wrapper.eq(NotificationLog::getTargetSystem, targetSystem);
        }
        if (status != null) {
            wrapper.eq(NotificationLog::getStatus, status);
        }
        wrapper.orderByDesc(NotificationLog::getCreatedAt);

        return baseMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
