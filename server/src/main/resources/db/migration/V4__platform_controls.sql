-- V4__platform_controls.sql
-- Security policy, integration resilience, document versions, traceability and reporting controls.

CREATE TABLE IF NOT EXISTS security_roles (
    id          BIGSERIAL PRIMARY KEY,
    key         VARCHAR(100) NOT NULL UNIQUE,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    is_system   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS security_permissions (
    id          BIGSERIAL PRIMARY KEY,
    key         VARCHAR(150) NOT NULL UNIQUE,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS security_role_permissions (
    id            BIGSERIAL PRIMARY KEY,
    role_id       BIGINT NOT NULL REFERENCES security_roles(id),
    permission_id BIGINT NOT NULL REFERENCES security_permissions(id),
    UNIQUE(role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS user_role_assignments (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id),
    role_id     BIGINT NOT NULL REFERENCES security_roles(id),
    starts_at   TIMESTAMP,
    ends_at     TIMESTAMP,
    assigned_by BIGINT REFERENCES users(id),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS integration_connectors (
    id           BIGSERIAL PRIMARY KEY,
    system_key   VARCHAR(100) NOT NULL UNIQUE,
    endpoint     VARCHAR(500) NOT NULL,
    protocol     VARCHAR(50)  NOT NULL DEFAULT 'REST',
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    retry_policy VARCHAR(100) NOT NULL DEFAULT 'exponential-3',
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS integration_calls (
    id              BIGSERIAL PRIMARY KEY,
    connector_id    BIGINT       NOT NULL REFERENCES integration_connectors(id),
    idempotency_key VARCHAR(120) NOT NULL,
    request_payload TEXT,
    response_payload TEXT,
    status          VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    attempts        INT          NOT NULL DEFAULT 0,
    error_message   TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE(connector_id, idempotency_key)
);

CREATE TABLE IF NOT EXISTS outbox_events (
    id              BIGSERIAL PRIMARY KEY,
    aggregate_type  VARCHAR(100) NOT NULL,
    aggregate_id    VARCHAR(100) NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    payload         TEXT         NOT NULL,
    status          VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    retries         INT          NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS inbox_events (
    id           BIGSERIAL PRIMARY KEY,
    source_system VARCHAR(100)  NOT NULL,
    message_key  VARCHAR(120)   NOT NULL UNIQUE,
    event_type   VARCHAR(100)   NOT NULL,
    payload      TEXT           NOT NULL,
    processed    BOOLEAN        NOT NULL DEFAULT FALSE,
    processed_at TIMESTAMP,
    created_at   TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS dead_letters (
    id         BIGSERIAL PRIMARY KEY,
    source_type VARCHAR(50)  NOT NULL,
    source_id  VARCHAR(100) NOT NULL,
    reason     TEXT         NOT NULL,
    payload    TEXT,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS dossier_document_versions (
    id               BIGSERIAL PRIMARY KEY,
    application_id   BIGINT       NOT NULL REFERENCES applications(id),
    part_number      INT          NOT NULL,
    file_name        VARCHAR(500) NOT NULL,
    file_hash        VARCHAR(128) NOT NULL,
    storage_path     TEXT         NOT NULL,
    signature_status VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    uploaded_by      BIGINT REFERENCES users(id),
    uploaded_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS traceability_events (
    id          BIGSERIAL PRIMARY KEY,
    event_type  VARCHAR(100) NOT NULL,
    batch_id    BIGINT REFERENCES batches(id),
    qr_code     VARCHAR(255),
    location    VARCHAR(500),
    severity    VARCHAR(30)  NOT NULL DEFAULT 'INFO',
    payload     TEXT,
    occurred_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS report_templates (
    id            BIGSERIAL PRIMARY KEY,
    key           VARCHAR(100) NOT NULL UNIQUE,
    title         VARCHAR(255) NOT NULL,
    description   TEXT,
    schedule_type VARCHAR(30)  NOT NULL DEFAULT 'ON_DEMAND',
    query_spec    TEXT,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS report_runs (
    id           BIGSERIAL PRIMARY KEY,
    template_id  BIGINT      NOT NULL REFERENCES report_templates(id),
    status       VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    output_ref   TEXT,
    requested_by BIGINT REFERENCES users(id),
    error_message TEXT,
    started_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    finished_at  TIMESTAMP
);

CREATE TABLE IF NOT EXISTS report_submissions (
    id             BIGSERIAL PRIMARY KEY,
    run_id         BIGINT       NOT NULL REFERENCES report_runs(id),
    authority      VARCHAR(255) NOT NULL,
    status         VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    submission_ref VARCHAR(255),
    submitted_at   TIMESTAMP
);
