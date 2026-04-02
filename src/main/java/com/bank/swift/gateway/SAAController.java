package com.bank.swift.gateway;

import com.bank.swift.model.dto.*;
import com.bank.swift.model.entity.SwiftMessage;
import com.bank.swift.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * SAA模拟器控制器
 * 模拟SWIFT Alliance Access
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/saa")
@RequiredArgsConstructor
public class SAAController {

    private final MessageService messageService;

    /**
     * 接收SWIFT发送的报文
     */
    @PostMapping("/inbound")
    public ResponseEntity<ApiResponse<SAAInboundResponse>> receiveMessage(
            @RequestBody SAAInboundRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {

        log.info("SAA收到报文: messageId={}, type={}, requestId={}",
                request.getMessageId(), request.getMessageType(), requestId);

        // 创建报文实体
        SwiftMessage message = new SwiftMessage();
        message.setMessageId(request.getMessageId());
        message.setMessageType(request.getMessageType());
        message.setSenderBic(request.getSenderBic());
        message.setReceiverBic(request.getReceiverBic());
        message.setRawContent(request.getRawXml());
        message.setDirection("INBOUND");
        message.setStatus("RECEIVED");
        message.setCreatedAt(LocalDateTime.now());

        // 处理报文
        messageService.processInbound(message);

        // 构建响应
        SAAInboundResponse response = new SAAInboundResponse();
        response.setMessageId(request.getMessageId());
        response.setStatus("RECEIVED");
        response.setReceivedAt(LocalDateTime.now());

        return ResponseEntity.ok(ApiResponse.success("接收成功", response));
    }

    /**
     * 发送报文到SWIFT
     */
    @PostMapping("/outbound")
    public ResponseEntity<ApiResponse<SAAOutboundResponse>> sendMessage(
            @RequestBody SAAOutboundRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {

        log.info("SAA发送报文: type={}, requestId={}", request.getMessageType(), requestId);

        // 处理出向报文
        SwiftMessage message = messageService.processOutbound(
                request.getMessageType(),
                request.getBusinessData()
        );

        // 构建响应
        SAAOutboundResponse response = new SAAOutboundResponse();
        response.setMessageId(message.getMessageId());
        response.setStatus(message.getStatus());
        response.setSubmittedAt(LocalDateTime.now());
        response.setEstimatedDelivery(LocalDateTime.now().plusSeconds(60));

        return ResponseEntity.ok(ApiResponse.success("发送成功", response));
    }

    /**
     * 查询报文状态
     */
    @GetMapping("/status/{messageId}")
    public ResponseEntity<ApiResponse<String>> getStatus(@PathVariable String messageId) {
        log.info("查询报文状态: messageId={}", messageId);
        SwiftMessage message = messageService.findByMessageId(messageId);
        if (message == null) {
            return ResponseEntity.ok(ApiResponse.error("0001", "报文不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success("查询成功", message.getStatus()));
    }
}
