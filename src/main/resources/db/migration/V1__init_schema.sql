-- SWIFT Middleware Database Schema

-- 原始报文表
CREATE TABLE swift_message (
    id              BIGSERIAL PRIMARY KEY,
    message_id      VARCHAR(64) UNIQUE NOT NULL,
    message_type    VARCHAR(50) NOT NULL,
    direction       VARCHAR(10) NOT NULL,
    raw_content     TEXT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'RECEIVED',
    sender_bic      VARCHAR(20),
    receiver_bic    VARCHAR(20),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    parsed_data_id  BIGINT
);

CREATE INDEX idx_swift_message_type ON swift_message(message_type);
CREATE INDEX idx_swift_message_direction ON swift_message(direction);
CREATE INDEX idx_swift_message_status ON swift_message(status);
CREATE INDEX idx_swift_message_created_at ON swift_message(created_at);

-- 解析后业务数据（统一模型）
CREATE TABLE parsed_business_data (
    id              BIGSERIAL PRIMARY KEY,
    message_id      BIGINT NOT NULL REFERENCES swift_message(id) ON DELETE CASCADE,
    business_type   VARCHAR(50),
    source_account  VARCHAR(64),
    dest_account    VARCHAR(64),
    source_bank     VARCHAR(20),
    dest_bank       VARCHAR(20),
    amount          DECIMAL(18,2),
    currency        VARCHAR(3),
    value_date      DATE,
    reference       VARCHAR(64),
    raw_json        JSONB,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_parsed_business_data_message_id ON parsed_business_data(message_id);
CREATE INDEX idx_parsed_business_data_business_type ON parsed_business_data(business_type);

-- 报文路由表
CREATE TABLE message_route (
    id              BIGSERIAL PRIMARY KEY,
    message_type    VARCHAR(50) NOT NULL,
    business_scenario VARCHAR(50),
    target_system   VARCHAR(50) NOT NULL,
    routing_rule    VARCHAR(500),
    priority        INT DEFAULT 0,
    enabled         BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_message_route_message_type ON message_route(message_type);
CREATE INDEX idx_message_route_target_system ON message_route(target_system);
CREATE INDEX idx_message_route_enabled ON message_route(enabled);

-- 内部系统订阅配置
CREATE TABLE system_subscription (
    id              BIGSERIAL PRIMARY KEY,
    system_code     VARCHAR(50) NOT NULL UNIQUE,
    system_name     VARCHAR(100),
    endpoint_type   VARCHAR(10) NOT NULL,
    endpoint_url    VARCHAR(500),
    mq_type         VARCHAR(20),
    mq_topic        VARCHAR(200),
    message_types   VARCHAR(500)[],
    auth_type       VARCHAR(20) DEFAULT 'NONE',
    auth_username   VARCHAR(100),
    auth_password   VARCHAR(200),
    retry_count     INT DEFAULT 3,
    timeout_ms      INT DEFAULT 5000,
    enabled         BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_system_subscription_system_code ON system_subscription(system_code);
CREATE INDEX idx_system_subscription_enabled ON system_subscription(enabled);

-- 通知日志表
CREATE TABLE notification_log (
    id              BIGSERIAL PRIMARY KEY,
    message_id      BIGINT NOT NULL REFERENCES swift_message(id) ON DELETE CASCADE,
    target_system   VARCHAR(50) NOT NULL,
    channel         VARCHAR(10) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    request_payload TEXT,
    response_payload TEXT,
    error_message   TEXT,
    mq_offset       BIGINT,
    retry_count     INT DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notification_log_message_id ON notification_log(message_id);
CREATE INDEX idx_notification_log_target_system ON notification_log(target_system);
CREATE INDEX idx_notification_log_status ON notification_log(status);
CREATE INDEX idx_notification_log_created_at ON notification_log(created_at);

-- 初始路由规则数据
INSERT INTO message_route (message_type, business_scenario, target_system, routing_rule, priority, enabled)
VALUES
    ('pacs.008.001.08', 'CREDIT_TRANSFER', 'CASH_MGMT', 'amount > 10000', 1, true),
    ('pacs.008.001.08', 'CREDIT_TRANSFER', 'CORE_BANKING', NULL, 2, true),
    ('pacs.002.001.08', 'PAYMENT_STATUS', 'CASH_MGMT', NULL, 1, true),
    ('pain.001.001.09', 'PAYMENT_REQUEST', 'CASH_MGMT', NULL, 1, true),
    ('pain.001.001.09', 'PAYMENT_REQUEST', 'CORE_BANKING', NULL, 2, true),
    ('camt.056.001.08', 'PAYMENT_REVOKE', 'CASH_MGMT', NULL, 1, true);

-- 初始订阅配置
INSERT INTO system_subscription (system_code, system_name, endpoint_type, endpoint_url, message_types, auth_type, retry_count, timeout_ms, enabled)
VALUES
    ('CASH_MGMT', '现金管理系统', 'HTTP', 'http://localhost:8082/api/callback/cash-mgmt', ARRAY['pacs.008.001.08', 'pacs.002.001.08', 'pain.001.001.09', 'camt.056.001.08'], 'BEARER_TOKEN', 3, 5000, true),
    ('CORE_BANKING', '核心银行系统', 'HTTP', 'http://localhost:8083/api/callback/core-banking', ARRAY['pacs.008.001.08', 'pain.001.001.09'], 'BASIC', 3, 5000, true),
    ('CREDIT_CARD', '信用卡系统', 'MQ', NULL, ARRAY['pacs.008.001.08'], 'NONE', 3, 5000, true);

-- 更新订阅的MQ配置
UPDATE system_subscription SET mq_type = 'KAFKA', mq_topic = 'swift.message.cc' WHERE system_code = 'CREDIT_CARD';
