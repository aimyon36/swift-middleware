package com.bank.swift.gateway;

import com.bank.swift.model.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * AMH模拟器控制器
 * 模拟SWIFT Alliance Messaging Hub
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/amh")
@RequiredArgsConstructor
public class AMHController {

    /**
     * AMH消息传输
     */
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<AMHTransferResponse>> transfer(
            @RequestBody AMHTransferRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {

        log.info("AMH传输消息: messageId={}, destination={}, requestId={}",
                request.getMessageId(), request.getDestination(), requestId);

        AMHTransferResponse response = new AMHTransferResponse();
        response.setTransferId(UUID.randomUUID().toString());
        response.setStatus("TRANSFERRED");
        response.setTransferTime(LocalDateTime.now());

        return ResponseEntity.ok(ApiResponse.success("传输成功", response));
    }

    /**
     * 获取队列消息
     */
    @GetMapping("/queue")
    public ResponseEntity<ApiResponse<List<AMHQueueItem>>> getQueue(
            @RequestParam String destination,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {

        log.info("AMH获取队列: destination={}, requestId={}", destination, requestId);

        // 返回空队列，实际实现应该查询队列
        return ResponseEntity.ok(ApiResponse.success("查询成功", List.of()));
    }
}
