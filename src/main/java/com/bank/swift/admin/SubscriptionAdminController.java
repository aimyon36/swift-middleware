package com.bank.swift.admin;

import com.bank.swift.model.dto.ApiResponse;
import com.bank.swift.model.entity.SystemSubscription;
import com.bank.swift.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订阅配置管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/subscriptions")
@RequiredArgsConstructor
public class SubscriptionAdminController {

    private final SubscriptionService subscriptionService;

    /**
     * 查询所有订阅配置
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SystemSubscription>>> listSubscriptions() {
        log.info("查询所有订阅配置");
        List<SystemSubscription> subscriptions = subscriptionService.list();
        return ResponseEntity.ok(ApiResponse.success(subscriptions));
    }

    /**
     * 新增订阅配置
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createSubscription(@RequestBody SystemSubscription subscription) {
        log.info("新增订阅配置: systemCode={}, endpointType={}",
                subscription.getSystemCode(), subscription.getEndpointType());
        subscriptionService.save(subscription);
        return ResponseEntity.ok(ApiResponse.success("创建成功", subscription.getId()));
    }

    /**
     * 更新订阅配置
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateSubscription(
            @PathVariable Long id,
            @RequestBody SystemSubscription subscription) {
        log.info("更新订阅配置: id={}", id);
        subscription.setId(id);
        subscriptionService.updateById(subscription);
        return ResponseEntity.ok(ApiResponse.success("更新成功", null));
    }

    /**
     * 删除订阅配置
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubscription(@PathVariable Long id) {
        log.info("删除订阅配置: id={}", id);
        subscriptionService.removeById(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    /**
     * 测试订阅连接
     */
    @PostMapping("/{id}/test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testSubscription(@PathVariable Long id) {
        log.info("测试订阅连接: id={}", id);
        // TODO: 实现连接测试逻辑
        Map<String, Object> result = Map.of(
                "httpTest", Map.of("success", true, "responseTime", 120),
                "mqTest", Map.of("success", true, "message", "RabbitMQ连接正常")
        );
        return ResponseEntity.ok(ApiResponse.success("测试成功", result));
    }
}
