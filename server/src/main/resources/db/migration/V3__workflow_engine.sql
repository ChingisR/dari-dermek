-- V3__workflow_engine.sql
-- Registration workflow execution model with SLA tracking and stage history.

CREATE TABLE IF NOT EXISTS registration_workflows (
    id                   BIGSERIAL PRIMARY KEY,
    application_id       BIGINT REFERENCES applications(id),
    pathway              VARCHAR(50)  NOT NULL,
    current_stage        VARCHAR(100) NOT NULL,
    state                VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    sla_working_days     INT          NOT NULL,
    elapsed_working_days INT          NOT NULL DEFAULT 0,
    paused_at            TIMESTAMP,
    due_date             DATE,
    created_by           BIGINT REFERENCES users(id),
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_registration_workflows_application_id ON registration_workflows(application_id);
CREATE INDEX IF NOT EXISTS idx_registration_workflows_state ON registration_workflows(state);
CREATE INDEX IF NOT EXISTS idx_registration_workflows_pathway ON registration_workflows(pathway);

CREATE TABLE IF NOT EXISTS workflow_stage_history (
    id          BIGSERIAL PRIMARY KEY,
    workflow_id BIGINT       NOT NULL REFERENCES registration_workflows(id),
    from_stage  VARCHAR(100),
    to_stage    VARCHAR(100) NOT NULL,
    changed_by  BIGINT REFERENCES users(id),
    note        TEXT,
    changed_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_workflow_stage_history_workflow ON workflow_stage_history(workflow_id);
