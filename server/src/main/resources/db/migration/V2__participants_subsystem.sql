-- V2__participants_subsystem.sql
-- Adds participant registry structures for the documented "Registration of system participants" subsystem.

CREATE TABLE IF NOT EXISTS participants (
    id              BIGSERIAL PRIMARY KEY,
    type            VARCHAR(50)  NOT NULL,
    name            VARCHAR(500) NOT NULL,
    bin_iin         VARCHAR(20),
    contact_person  VARCHAR(255),
    phone           VARCHAR(100),
    email           VARCHAR(255),
    address         TEXT,
    license_number  VARCHAR(100),
    status          VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_participants_type ON participants(type);
CREATE INDEX IF NOT EXISTS idx_participants_status ON participants(status);
CREATE INDEX IF NOT EXISTS idx_participants_bin_iin ON participants(bin_iin);
