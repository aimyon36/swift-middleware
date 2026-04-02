package com.bank.swift.service;

import com.bank.swift.model.dto.BusinessDataDTO;
import com.bank.swift.model.entity.ParsedBusinessData;
import com.bank.swift.model.entity.SwiftMessage;
import com.bank.swift.model.event.MessageParsedEvent;
import com.bank.swift.notification.EventBus;
import com.bank.swift.parser.MXParser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.swift.model.entity.SwiftMessageMapper;
import com.bank.swift.model.entity.ParsedBusinessDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 报文服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService extends ServiceImpl<SwiftMessageMapper, SwiftMessage> {

    private final MXParser mxParser;
    private final EventBus eventBus;
    private final ParsedBusinessDataMapper parsedBusinessDataMapper;

    /**
     * 处理入向报文 - 保存到数据库
     */
    @Transactional
    public SwiftMessage processInbound(SwiftMessage message) {
        log.info("处理入向报文: messageId={}, type={}", message.getMessageId(), message.getMessageType());

        // 生成报文ID
        if (message.getMessageId() == null || message.getMessageId().isBlank()) {
            message.setMessageId(generateMessageId());
        }

        // 设置状态和方向
        message.setDirection("INBOUND");
        message.setStatus("RECEIVED");
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());

        // 保存报文
        baseMapper.insert(message);
        log.info("入向报文已保存: id={}, messageId={}", message.getId(), message.getMessageId());

        return message;
    }

    /**
     * 解析报文
     */
    @Transactional
    public ParsedBusinessData parseMessage(SwiftMessage message) {
        log.info("解析报文: messageId={}", message.getMessageId());

        // 更新状态为解析中
        message.setStatus("PARSING");
        baseMapper.updateById(message);

        try {
            // 解析报文
            ParsedBusinessData parsedData = mxParser.parse(message.getRawContent(), message.getMessageType());
            parsedData.setMessageId(message.getId());
            parsedData.setCreatedAt(LocalDateTime.now());

            // 保存解析结果
            parsedBusinessDataMapper.insert(parsedData);

            // 更新报文状态
            message.setStatus("PARSED");
            message.setParsedDataId(parsedData.getId());
            message.setUpdatedAt(LocalDateTime.now());
            baseMapper.updateById(message);

            // 发布解析完成事件
            MessageParsedEvent event = new MessageParsedEvent(message, parsedData);
            eventBus.publish(event);

            log.info("报文解析完成: messageId={}, businessType={}", message.getMessageId(), parsedData.getBusinessType());
            return parsedData;

        } catch (Exception e) {
            log.error("报文解析失败: messageId={}, error={}", message.getMessageId(), e.getMessage());
            message.setStatus("FAILED");
            message.setUpdatedAt(LocalDateTime.now());
            baseMapper.updateById(message);
            throw new RuntimeException("报文解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理出向报文
     */
    @Transactional
    public SwiftMessage processOutbound(String messageType, BusinessDataDTO businessData) {
        log.info("处理出向报文: type={}", messageType);

        SwiftMessage message = new SwiftMessage();
        message.setMessageId(generateMessageId());
        message.setMessageType(messageType);
        message.setDirection("OUTBOUND");
        message.setStatus("SUBMITTED");
        message.setSenderBic(businessData.getSenderBic());
        message.setReceiverBic(businessData.getReceiverBic());
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());

        // TODO: 组装SWIFT报文到 rawContent

        baseMapper.insert(message);
        log.info("出向报文已保存: id={}, messageId={}", message.getId(), message.getMessageId());

        return message;
    }

    /**
     * 分页查询报文
     */
    public Page<SwiftMessage> queryMessages(int page, int size, String messageType,
                                             String direction, String status,
                                             String senderBic, String receiverBic) {
        LambdaQueryWrapper<SwiftMessage> wrapper = new LambdaQueryWrapper<>();

        if (messageType != null && !messageType.isBlank()) {
            wrapper.eq(SwiftMessage::getMessageType, messageType);
        }
        if (direction != null && !direction.isBlank()) {
            wrapper.eq(SwiftMessage::getDirection, direction);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(SwiftMessage::getStatus, status);
        }
        if (senderBic != null && !senderBic.isBlank()) {
            wrapper.eq(SwiftMessage::getSenderBic, senderBic);
        }
        if (receiverBic != null && !receiverBic.isBlank()) {
            wrapper.eq(SwiftMessage::getReceiverBic, receiverBic);
        }

        wrapper.orderByDesc(SwiftMessage::getCreatedAt);

        Page<SwiftMessage> result = baseMapper.selectPage(new Page<>(page, size), wrapper);
        log.info("查询报文: page={}, size={}, total={}", page, size, result.getTotal());
        return result;
    }

    /**
     * 根据messageId查询报文
     */
    public SwiftMessage findByMessageId(String messageId) {
        LambdaQueryWrapper<SwiftMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SwiftMessage::getMessageId, messageId);
        return baseMapper.selectOne(wrapper);
    }

    /**
     * 根据数据库ID查询报文
     */
    public SwiftMessage findById(Long id) {
        return baseMapper.selectById(id);
    }

    /**
     * 更新报文状态
     */
    @Transactional
    public void updateStatus(Long messageId, String status) {
        SwiftMessage message = baseMapper.selectById(messageId);
        if (message != null) {
            message.setStatus(status);
            message.setUpdatedAt(LocalDateTime.now());
            baseMapper.updateById(message);
            log.info("报文状态更新: messageId={}, status={}", message.getMessageId(), status);
        }
    }

    /**
     * 生成报文ID
     */
    private String generateMessageId() {
        return "MSG" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
