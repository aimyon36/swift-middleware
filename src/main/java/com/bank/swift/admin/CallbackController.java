package com.bank.swift.admin;

import com.bank.swift.model.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 内部系统回调接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/callback")
@RequiredArgsConstructor
public class CallbackController {

    /**
     * 接收内部系统ACK确认
     */
    @PostMapping("/{systemCode}/ack")
    public ResponseEntity<ApiResponse<Void>> receiveAck(
            @PathVariable String systemCode,
            @RequestBody Map<String, Object> payload) {

        log.info("收到{} ACK确认: payload={}", systemCode, payload);

        String messageId = (String) payload.get("messageId");
        String status = (String) payload.get("status");
        Map<String, Object> extInfo = (Map<String, Object>) payload.get("extInfo");

        // TODO: 更新通知日志状态
        log.info("ACK处理完成: system={}, messageId={}, status={}",
                systemCode, messageId, status);

        return ResponseEntity.ok(ApiResponse.success("确认收到", null));
    }

    /**
     * 接收内部系统处理失败回调
     */
    @PostMapping("/{systemCode}/fail")
    public ResponseEntity<ApiResponse<Void>> receiveFail(
            @PathVariable String systemCode,
            @RequestBody Map<String, Object> payload) {

        log.info("收到{}失败回调: payload={}", systemCode, payload);

        String messageId = (String) payload.get("messageId");
        String errorCode = (String) payload.get("errorCode");
        String errorMessage = (String) payload.get("errorMessage");
        Boolean retryable = (Boolean) payload.get("retryable");

        // TODO: 处理失败回调，可能需要重试
        log.warn("处理失败: system={}, messageId={}, errorCode={}, errorMessage={}, retryable={}",
                systemCode, messageId, errorCode, errorMessage, retryable);

        return ResponseEntity.ok(ApiResponse.success("失败收到", null));
    }
}
