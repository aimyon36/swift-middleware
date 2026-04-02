package com.bank.swift.admin;

import com.bank.swift.model.dto.ApiResponse;
import com.bank.swift.model.entity.SwiftMessage;
import com.bank.swift.model.entity.NotificationLog;
import com.bank.swift.service.MessageService;
import com.bank.swift.service.NotificationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 报文管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageAdminController {

    private final MessageService messageService;
    private final NotificationService notificationService;

    /**
     * 查询报文详情
     */
    @GetMapping("/{messageId}")
    public ResponseEntity<ApiResponse<SwiftMessage>> getMessage(@PathVariable String messageId) {
        log.info("查询报文: messageId={}", messageId);
        SwiftMessage message = messageService.findByMessageId(messageId);
        if (message == null) {
            return ResponseEntity.ok(ApiResponse.error("0001", "报文不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    /**
     * 根据数据库ID查询报文详情
     */
    @GetMapping("/id/{id}")
    public ResponseEntity<ApiResponse<SwiftMessage>> getMessageById(@PathVariable Long id) {
        log.info("查询报文: id={}", id);
        SwiftMessage message = messageService.findById(id);
        if (message == null) {
            return ResponseEntity.ok(ApiResponse.error("0001", "报文不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    /**
     * 分页查询报文列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SwiftMessage>>> listMessages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String messageType,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String senderBic,
            @RequestParam(required = false) String receiverBic) {

        log.info("分页查询报文: page={}, size={}, type={}, direction={}, status={}",
                page, size, messageType, direction, status);

        Page<SwiftMessage> result = messageService.queryMessages(page, size, messageType, direction, status, senderBic, receiverBic);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 查询通知日志
     */
    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<Page<NotificationLog>>> listNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String messageId,
            @RequestParam(required = false) String targetSystem,
            @RequestParam(required = false) String status) {

        log.info("分页查询通知日志: page={}, size={}, messageId={}, targetSystem={}, status={}",
                page, size, messageId, targetSystem, status);

        Long msgId = null;
        if (messageId != null && !messageId.isBlank()) {
            try {
                msgId = Long.parseLong(messageId);
            } catch (NumberFormatException ignored) {}
        }

        Page<NotificationLog> result = notificationService.queryLogs(msgId, targetSystem, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
