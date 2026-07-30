-- V1__init_schema.sql
-- GIS Дари-дермек — Initial Database Schema
-- All 13 tables for the veterinary pharmaceutical regulation platform

-- ═══════════════════════════════════════
-- USERS & AUTH
-- ═══════════════════════════════════════

CREATE TABLE IF NOT EXISTS users (
    id             BIGSERIAL PRIMARY KEY,
    login          VARCHAR(255) NOT NULL UNIQUE,
    password_hash  VARCHAR(255),
    full_name      VARCHAR(500) NOT NULL,
    role           VARCHAR(50)  NOT NULL DEFAULT 'APPLICANT',
    organization   VARCHAR(500),
    egov_sso_id    VARCHAR(255) UNIQUE,
    ecp_serial     VARCHAR(255),
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_egov_sso_id ON users(egov_sso_id);

-- ═══════════════════════════════════════
-- DRUG REGISTRY
-- ═══════════════════════════════════════

CREATE TABLE IF NOT EXISTS manufacturers (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(500) NOT NULL,
    country         VARCHAR(100),
    address         VARCHAR(1000),
    gmp_certificate VARCHAR(255),
    gmp_expiry_date DATE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS drugs (
    id                   BIGSERIAL PRIMARY KEY,
    trade_name           VARCHAR(500) NOT NULL,
    inn                  VARCHAR(500),
    type                 VARCHAR(50)   NOT NULL DEFAULT 'PHARMACEUTICAL',
    dosage_form          VARCHAR(255),
    active_substances    TEXT          DEFAULT '[]',  -- JSON array
    manufacturer_id      BIGINT REFERENCES manufacturers(id),
    registration_number  VARCHAR(100) UNIQUE,
    registration_date    DATE,
    expiry_date          DATE,
    is_annex8            BOOLEAN       NOT NULL DEFAULT FALSE,
    is_annex16           BOOLEAN       NOT NULL DEFAULT FALSE,
    target_animals       TEXT          DEFAULT '[]',  -- JSON array
    status               VARCHAR(50)   NOT NULL DEFAULT 'ACTIVE',
    created_at           TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_drugs_type ON drugs(type);
CREATE INDEX idx_drugs_status ON drugs(status);
CREATE INDEX idx_drugs_trade_name ON drugs(trade_name);
CREATE INDEX idx_drugs_registration_number ON drugs(registration_number);

-- ═══════════════════════════════════════
-- REGISTRATION APPLICATIONS
-- ═══════════════════════════════════════

CREATE TABLE IF NOT EXISTS applications (
    id                   BIGSERIAL PRIMARY KEY,
    applicant_id         BIGINT       NOT NULL REFERENCES users(id),
    drug_id              BIGINT REFERENCES drugs(id),
    pathway              VARCHAR(50)  NOT NULL,
    status               VARCHAR(50)  NOT NULL DEFAULT 'DRAFT',
    drug_trade_name      VARCHAR(500) NOT NULL,
    drug_type            VARCHAR(50)  NOT NULL DEFAULT 'PHARMACEUTICAL',
    manufacturer_name    VARCHAR(500),
    submission_date      DATE,
    deadline_date        DATE,
    max_working_days     INT,
    working_days_elapsed INT          NOT NULL DEFAULT 0,
    is_clock_paused      BOOLEAN      NOT NULL DEFAULT FALSE,
    clock_paused_at      TIMESTAMP,
    total_paused_days    INT          NOT NULL DEFAULT 0,
    query_count          INT          NOT NULL DEFAULT 0,
    notes                TEXT,
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_applications_applicant ON applications(applicant_id);
CREATE INDEX idx_applications_pathway ON applications(pathway);
CREATE INDEX idx_applications_status ON applications(status);

CREATE TABLE IF NOT EXISTS application_status_history (
    id             BIGSERIAL PRIMARY KEY,
    application_id BIGINT       NOT NULL REFERENCES applications(id),
    old_status     VARCHAR(50),
    new_status     VARCHAR(50)  NOT NULL,
    changed_by     BIGINT REFERENCES users(id),
    comment        TEXT,
    changed_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_status_history_app ON application_status_history(application_id);

-- ═══════════════════════════════════════
-- DOSSIER (CTD Parts 1-4)
-- ═══════════════════════════════════════

CREATE TABLE IF NOT EXISTS dossier_parts (
    id             BIGSERIAL PRIMARY KEY,
    application_id BIGINT       NOT NULL REFERENCES applications(id),
    part_number    INT          NOT NULL,  -- 1, 2, 3, or 4
    title          VARCHAR(500) NOT NULL,
    file_path      VARCHAR(1000),
    file_size      BIGINT,
    uploaded_by    BIGINT REFERENCES users(id),
    uploaded_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_dossier_application ON dossier_parts(application_id);

-- ═══════════════════════════════════════
-- BATCH TRACKING & QR TRACEABILITY
-- ═══════════════════════════════════════

CREATE TABLE IF NOT EXISTS batches (
    id              BIGSERIAL PRIMARY KEY,
    drug_id         BIGINT       NOT NULL REFERENCES drugs(id),
    batch_number    VARCHAR(100) NOT NULL,
    manufacture_date DATE,
    expiry_date     DATE,
    quantity        INT,
    status          VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    cold_chain_ok   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_batches_drug ON batches(drug_id);
CREATE INDEX idx_batches_status ON batches(status);
CREATE UNIQUE INDEX idx_batches_number ON batches(drug_id, batch_number);

CREATE TABLE IF NOT EXISTS qr_vials (
    id          BIGSERIAL PRIMARY KEY,
    batch_id    BIGINT       NOT NULL REFERENCES batches(id),
    qr_code     VARCHAR(255) NOT NULL UNIQUE,
    is_scanned  BOOLEAN      NOT NULL DEFAULT FALSE,
    scanned_by  BIGINT REFERENCES users(id),
    scanned_at  TIMESTAMP,
    location    VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_qr_vials_batch ON qr_vials(batch_id);
CREATE INDEX idx_qr_vials_code ON qr_vials(qr_code);

-- ═══════════════════════════════════════
-- LIS (Laboratory Information System)
-- ═══════════════════════════════════════

CREATE TABLE IF NOT EXISTS lab_protocols (
    id             BIGSERIAL PRIMARY KEY,
    application_id BIGINT REFERENCES applications(id),
    batch_id       BIGINT REFERENCES batches(id),
    protocol_number VARCHAR(100),
    discipline     VARCHAR(50)  NOT NULL DEFAULT 'CHEMISTRY',
    lab_name       VARCHAR(500),
    analyst_id     BIGINT REFERENCES users(id),
    start_date     DATE,
    end_date       DATE,
    result         VARCHAR(50),     -- PASS, FAIL, PENDING
    conclusion     TEXT,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_lab_protocols_app ON lab_protocols(application_id);
CREATE INDEX idx_lab_protocols_batch ON lab_protocols(batch_id);

-- ═══════════════════════════════════════
-- CONTROL PURCHASES (Market Surveillance)
-- ═══════════════════════════════════════

CREATE TABLE IF NOT EXISTS control_purchases (
    id            BIGSERIAL PRIMARY KEY,
    drug_id       BIGINT REFERENCES drugs(id),
    batch_id      BIGINT REFERENCES batches(id),
    inspector_id  BIGINT REFERENCES users(id),
    purchase_date DATE,
    location      VARCHAR(500),
    result        VARCHAR(50),     -- COMPLIANT, NON_COMPLIANT, PENDING
    notes         TEXT,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_control_purchases_drug ON control_purchases(drug_id);

-- ═══════════════════════════════════════
-- IMPORT DECLARATIONS (Border Control)
-- ═══════════════════════════════════════

CREATE TABLE IF NOT EXISTS import_declarations (
    id                BIGSERIAL PRIMARY KEY,
    drug_id           BIGINT REFERENCES drugs(id),
    batch_id          BIGINT REFERENCES batches(id),
    declaration_number VARCHAR(100),
    importer_id       BIGINT REFERENCES users(id),
    origin_country    VARCHAR(100),
    entry_point       VARCHAR(255),
    declaration_date  DATE,
    status            VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    inspector_id      BIGINT REFERENCES users(id),
    inspection_date   DATE,
    notes             TEXT,
    created_at        TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_import_declarations_drug ON import_declarations(drug_id);
CREATE INDEX idx_import_declarations_status ON import_declarations(status);

-- ═══════════════════════════════════════
-- VETERINARY PRESCRIPTIONS (Antibiotic Tracking)
-- ═══════════════════════════════════════

CREATE TABLE IF NOT EXISTS vet_prescriptions (
    id               BIGSERIAL PRIMARY KEY,
    drug_id          BIGINT REFERENCES drugs(id),
    prescriber_id    BIGINT REFERENCES users(id),
    animal_species   VARCHAR(100),
    animal_count     INT,
    farm_name        VARCHAR(500),
    farm_location    VARCHAR(500),
    diagnosis        TEXT,
    dosage           VARCHAR(255),
    treatment_days   INT,
    withdrawal_days  INT,
    prescription_date DATE,
    notes            TEXT,
    created_at       TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_vet_prescriptions_drug ON vet_prescriptions(drug_id);
CREATE INDEX idx_vet_prescriptions_prescriber ON vet_prescriptions(prescriber_id);

-- ═══════════════════════════════════════
-- SEED DATA
-- ═══════════════════════════════════════

-- Default admin user
INSERT INTO users (login, full_name, role, organization)
VALUES ('admin@gis.kz', 'Администратор ГИС', 'ADMIN', 'ГИС Дари-дермек')
ON CONFLICT (login) DO NOTHING;

-- Demo applicant
INSERT INTO users (login, full_name, role, organization)
VALUES ('applicant@egov.kz', 'ТОО «ВетФарм Казахстан»', 'APPLICANT', 'ТОО «ВетФарм Казахстан»')
ON CONFLICT (login) DO NOTHING;

-- Demo KVKN staff
INSERT INTO users (login, full_name, role, organization)
VALUES ('kvkn@gov.kz', 'Серіков Б.А.', 'COMMITTEE_STAFF', 'КВКН МСХ РК')
ON CONFLICT (login) DO NOTHING;

-- Demo NRCV expert
INSERT INTO users (login, full_name, role, organization)
VALUES ('expert@nrcv.kz', 'Нурбекова Ж.К.', 'NRCV_EXPERT', 'НРЦВ')
ON CONFLICT (login) DO NOTHING;

-- Demo manufacturers
INSERT INTO manufacturers (name, country, address, gmp_certificate) VALUES
    ('Bayer AG', 'Германия', 'Leverkusen, Germany', 'GMP-DE-2023-001'),
    ('ФГБНУ ВИЭВ', 'Россия', 'Москва, Рязанский пр.', NULL),
    ('ТОО «ВетФарм Казахстан»', 'Казахстан', 'г. Алматы, ул. Толе би 85', 'GMP-KZ-2024-015'),
    ('Ceva Santé Animale', 'Франция', 'Libourne, France', 'GMP-FR-2022-008'),
    ('ФГБНУ ВНИИЗЖ', 'Россия', 'г. Владимир', NULL),
    ('НВЦ «Агроветзащита»', 'Россия', 'г. Москва', 'GMP-RU-2023-042'),
    ('Interchemie', 'Нидерланды', 'Venray, Netherlands', 'GMP-NL-2023-017'),
    ('Щёлковский биокомбинат', 'Россия', 'г. Щёлково, МО', NULL);

-- Demo drugs
INSERT INTO drugs (trade_name, inn, type, dosage_form, active_substances, manufacturer_id, registration_number, registration_date, expiry_date, is_annex8, target_animals, status) VALUES
    ('Ивермектин 1%', 'Ivermectin', 'PHARMACEUTICAL', 'Раствор для инъекций', '["Ивермектин"]', 1, 'RK-VET-001-2024', '2024-03-15', '2029-03-15', FALSE, '["КРС","МРС","Лошади"]', 'ACTIVE'),
    ('Вакцина против ящура', NULL, 'IMMUNOLOGICAL', 'Суспензия для инъекций', '["Инактивированный вирус ящура"]', 2, 'RK-VET-002-2023', '2023-07-01', '2028-07-01', TRUE, '["КРС","МРС","Свиньи"]', 'ACTIVE'),
    ('Альбендазол 10%', 'Albendazole', 'PHARMACEUTICAL', 'Суспензия для перорального применения', '["Альбендазол"]', 3, 'RK-VET-003-2025', '2025-01-20', '2030-01-20', FALSE, '["КРС","МРС","Лошади","Собаки"]', 'ACTIVE'),
    ('Амоксициллин 15%', 'Amoxicillin', 'PHARMACEUTICAL', 'Суспензия для инъекций', '["Амоксициллина тригидрат"]', 4, 'RK-VET-004-2024', '2024-09-10', '2029-09-10', TRUE, '["КРС","Свиньи"]', 'ACTIVE'),
    ('Вакцина против бруцеллёза Rev-1', NULL, 'IMMUNOLOGICAL', 'Лиофилизат', '["Brucella melitensis Rev-1"]', 5, 'RK-VET-005-2022', '2022-05-15', '2027-05-15', TRUE, '["МРС"]', 'ACTIVE'),
    ('Креолин-Х', NULL, 'DISINFECTANT', 'Эмульсия', '["Циперметрин"]', 6, 'RK-VET-006-2024', '2024-11-01', '2029-11-01', FALSE, '["КРС","МРС","Лошади"]', 'ACTIVE'),
    ('Тилозин 200', 'Tylosin', 'PHARMACEUTICAL', 'Раствор для инъекций', '["Тилозина тартрат"]', 7, NULL, NULL, NULL, FALSE, '["КРС","Свиньи","Птица"]', 'PENDING'),
    ('Диагностикум бруцеллёзный', NULL, 'DIAGNOSTIC', 'Антиген', '["Инактивированный антиген Brucella abortus"]', 8, 'RK-VET-008-2023', '2023-04-10', '2028-04-10', FALSE, '[]', 'ACTIVE');

-- Demo batches
INSERT INTO batches (drug_id, batch_number, manufacture_date, expiry_date, quantity, status, cold_chain_ok) VALUES
    (2, 'A-102', '2026-01-15', '2027-01-15', 5000, 'ACTIVE', TRUE),
    (3, 'B-205', '2026-03-01', '2028-03-01', 10000, 'ACTIVE', TRUE),
    (4, 'C-301', '2026-02-10', '2028-02-10', 3000, 'SUSPENDED', FALSE);

-- Demo QR codes
INSERT INTO qr_vials (batch_id, qr_code) VALUES
    (1, 'VET-FMD-A102-001'),
    (1, 'VET-FMD-A102-002'),
    (2, 'VET-ALB-B205-003'),
    (3, 'VET-AMOX-C301-007');
