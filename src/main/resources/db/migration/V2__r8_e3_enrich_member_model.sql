CREATE TABLE member_journey_stages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL,
    label VARCHAR(120) NOT NULL,
    display_order INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uk_member_journey_stages_code UNIQUE (code)
) ENGINE = InnoDB;

CREATE TABLE member_life_states (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL,
    label VARCHAR(120) NOT NULL,
    display_order INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uk_member_life_states_code UNIQUE (code)
) ENGINE = InnoDB;

CREATE TABLE member_archive_reasons (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL,
    label VARCHAR(120) NOT NULL,
    display_order INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uk_member_archive_reasons_code UNIQUE (code)
) ENGINE = InnoDB;

INSERT INTO member_journey_stages (code, label, display_order, active) VALUES
    ('ASPIRANT', 'Aspirant', 10, TRUE),
    ('STAGIAIRE', 'Stagiaire', 20, TRUE),
    ('ENGAGE_TEMPORAIRE_1', 'Engagé temporaire 1', 31, TRUE),
    ('ENGAGE_TEMPORAIRE_2', 'Engagé temporaire 2', 32, TRUE),
    ('ENGAGE_TEMPORAIRE_3', 'Engagé temporaire 3', 33, TRUE),
    ('ENGAGE_TEMPORAIRE_4', 'Engagé temporaire 4', 34, TRUE),
    ('ENGAGE_TEMPORAIRE_5', 'Engagé temporaire 5', 35, TRUE),
    ('ENGAGE_TEMPORAIRE_6', 'Engagé temporaire 6', 36, TRUE),
    ('ENGAGE_TEMPORAIRE_7', 'Engagé temporaire 7', 37, TRUE),
    ('ENGAGE_DEFINITIF', 'Engagé définitif', 40, TRUE);

INSERT INTO member_life_states (code, label, display_order, active) VALUES
    ('LAIC', 'Laïc', 10, TRUE),
    ('RELIGIEUSE', 'Religieuse', 20, TRUE),
    ('PRETRE', 'Prêtre', 30, TRUE);

INSERT INTO member_archive_reasons (code, label, display_order, active) VALUES
    ('DEPART_COMMUNAUTE', 'Départ de la communauté', 10, TRUE),
    ('TRANSFERT_HORS_PERIMETRE', 'Transfert hors périmètre', 20, TRUE),
    ('DOUBLON', 'Doublon', 30, TRUE),
    ('DECES', 'Décès', 40, TRUE),
    ('EXCOMMUNICATION', 'Excommunication', 50, TRUE),
    ('AUTRE', 'Autre', 60, TRUE);

UPDATE cmda_members
SET status = 'INACTIVE'
WHERE status IN ('PENDING', 'SUSPENDED');

ALTER TABLE cmda_members
    ADD COLUMN talents_and_skills TEXT NULL,
    ADD COLUMN address_line1 VARCHAR(255) NULL,
    ADD COLUMN address_line2 VARCHAR(255) NULL,
    ADD COLUMN postal_code VARCHAR(32) NULL,
    ADD COLUMN city VARCHAR(160) NULL,
    ADD COLUMN administrative_area VARCHAR(160) NULL,
    ADD COLUMN country_code VARCHAR(2) NULL,
    ADD COLUMN community_entry_date DATE NULL,
    ADD COLUMN definitive_commitment_date DATE NULL,
    ADD COLUMN photo_reference VARCHAR(255) NULL,
    ADD COLUMN internal_notes VARCHAR(2000) NULL,
    ADD COLUMN current_journey_stage_id BIGINT NULL,
    ADD COLUMN life_state_id BIGINT NULL,
    ADD COLUMN archive_reason_id BIGINT NULL,
    ADD COLUMN archive_comment VARCHAR(1000) NULL,
    ADD COLUMN archived_at DATETIME(6) NULL,
    ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD COLUMN updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    ADD CONSTRAINT fk_cmda_members_current_journey_stage
        FOREIGN KEY (current_journey_stage_id) REFERENCES member_journey_stages (id),
    ADD CONSTRAINT fk_cmda_members_life_state
        FOREIGN KEY (life_state_id) REFERENCES member_life_states (id),
    ADD CONSTRAINT fk_cmda_members_archive_reason
        FOREIGN KEY (archive_reason_id) REFERENCES member_archive_reasons (id);

CREATE INDEX idx_cmda_members_current_journey_stage
    ON cmda_members (current_journey_stage_id);

CREATE INDEX idx_cmda_members_life_state
    ON cmda_members (life_state_id);

CREATE INDEX idx_cmda_members_city
    ON cmda_members (city);

CREATE TABLE member_journey_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    journey_stage_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by_user_id BIGINT NULL,
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    updated_by_user_id BIGINT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_member_journey_history_member
        FOREIGN KEY (member_id) REFERENCES cmda_members (id),
    CONSTRAINT fk_member_journey_history_stage
        FOREIGN KEY (journey_stage_id) REFERENCES member_journey_stages (id)
) ENGINE = InnoDB;

CREATE INDEX idx_member_journey_history_member_dates
    ON member_journey_history (member_id, start_date, end_date);
