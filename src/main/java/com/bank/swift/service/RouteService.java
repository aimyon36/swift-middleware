package com.bank.swift.service;

import com.bank.swift.model.entity.MessageRoute;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.swift.model.entity.MessageRouteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 路由服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService extends ServiceImpl<MessageRouteMapper, MessageRoute> {

    /**
     * 根据报文类型查找路由规则
     */
    public List<MessageRoute> findByMessageType(String messageType) {
        LambdaQueryWrapper<MessageRoute> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageRoute::getMessageType, messageType)
                .eq(MessageRoute::getEnabled, true)
                .orderByAsc(MessageRoute::getPriority);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 根据报文类型和业务数据匹配目标系统
     */
    public List<String> matchTargetSystems(String messageType, Object businessData) {
        List<MessageRoute> routes = findByMessageType(messageType);

        // 简化实现：返回所有匹配路由的目标系统
        // 实际实现应该根据routingRule表达式进行匹配
        return routes.stream()
                .map(MessageRoute::getTargetSystem)
                .toList();
    }

    /**
     * 查询所有启用的路由
     */
    public List<MessageRoute> findAllEnabled() {
        LambdaQueryWrapper<MessageRoute> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageRoute::getEnabled, true)
                .orderByAsc(MessageRoute::getPriority);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 保存路由规则
     */
    public void saveRoute(MessageRoute route) {
        if (route.getId() == null) {
            baseMapper.insert(route);
            log.info("新增路由规则: type={}, target={}", route.getMessageType(), route.getTargetSystem());
        } else {
            baseMapper.updateById(route);
            log.info("更新路由规则: id={}", route.getId());
        }
    }

    /**
     * 删除路由规则
     */
    public void deleteRoute(Long id) {
        baseMapper.deleteById(id);
        log.info("删除路由规则: id={}", id);
    }

    /**
     * 根据ID查询路由
     */
    public MessageRoute findById(Long id) {
        return baseMapper.selectById(id);
    }
}
