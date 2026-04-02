package com.bank.swift.service;

import com.bank.swift.model.entity.SystemSubscription;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bank.swift.model.entity.SystemSubscriptionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 订阅服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService extends ServiceImpl<SystemSubscriptionMapper, SystemSubscription> {

    /**
     * 根据报文类型查找订阅配置
     */
    public List<SystemSubscription> findByMessageType(String messageType) {
        LambdaQueryWrapper<SystemSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemSubscription::getEnabled, true);

        List<SystemSubscription> allSubscriptions = baseMapper.selectList(wrapper);

        // 过滤匹配报文类型的订阅
        return allSubscriptions.stream()
                .filter(sub -> {
                    String typesStr = sub.getMessageTypes();
                    if (typesStr == null || typesStr.isBlank()) {
                        return false;
                    }
                    String[] types = typesStr.split(",");
                    // 支持通配符 *
                    return Arrays.stream(types).anyMatch(t ->
                            "*".equals(t.trim()) || t.trim().equals(messageType) || messageType.startsWith(t.trim().replace("*", "")));
                })
                .toList();
    }

    /**
     * 根据系统代码查找订阅
     */
    public SystemSubscription findBySystemCode(String systemCode) {
        LambdaQueryWrapper<SystemSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemSubscription::getSystemCode, systemCode);
        wrapper.eq(SystemSubscription::getEnabled, true);
        return baseMapper.selectOne(wrapper);
    }

    /**
     * 查询所有启用的订阅
     */
    public List<SystemSubscription> findAllEnabled() {
        LambdaQueryWrapper<SystemSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemSubscription::getEnabled, true);
        return baseMapper.selectList(wrapper);
    }
}
