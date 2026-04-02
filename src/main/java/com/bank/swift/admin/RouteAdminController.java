package com.bank.swift.admin;

import com.bank.swift.model.dto.ApiResponse;
import com.bank.swift.model.entity.MessageRoute;
import com.bank.swift.service.RouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 路由规则管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/routes")
@RequiredArgsConstructor
public class RouteAdminController {

    private final RouteService routeService;

    /**
     * 查询所有路由规则
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MessageRoute>>> listRoutes() {
        log.info("查询所有路由规则");
        List<MessageRoute> routes = routeService.list();
        return ResponseEntity.ok(ApiResponse.success(routes));
    }

    /**
     * 新增路由规则
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createRoute(@RequestBody MessageRoute route) {
        log.info("新增路由规则: type={}, target={}",
                route.getMessageType(), route.getTargetSystem());
        routeService.save(route);
        return ResponseEntity.ok(ApiResponse.success("创建成功", route.getId()));
    }

    /**
     * 更新路由规则
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateRoute(
            @PathVariable Long id,
            @RequestBody MessageRoute route) {
        log.info("更新路由规则: id={}", id);
        route.setId(id);
        routeService.updateById(route);
        return ResponseEntity.ok(ApiResponse.success("更新成功", null));
    }

    /**
     * 删除路由规则
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoute(@PathVariable Long id) {
        log.info("删除路由规则: id={}", id);
        routeService.removeById(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
}
