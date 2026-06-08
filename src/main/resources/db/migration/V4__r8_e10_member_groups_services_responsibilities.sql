CREATE TABLE community_groups (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL,
    label VARCHAR(120) NOT NULL,
    description VARCHAR(500) NULL,
    display_order INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uk_community_groups_code UNIQUE (code)
) ENGINE = InnoDB;

CREATE TABLE community_services (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL,
    label VARCHAR(120) NOT NULL,
    description VARCHAR(500) NULL,
    display_order INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uk_community_services_code UNIQUE (code)
) ENGINE = InnoDB;

CREATE TABLE member_group_assignments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_member_group_assignment_member FOREIGN KEY (member_id) REFERENCES cmda_members (id),
    CONSTRAINT fk_member_group_assignment_group FOREIGN KEY (group_id) REFERENCES community_groups (id)
) ENGINE = InnoDB;

CREATE TABLE member_service_assignments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_member_service_assignment_member FOREIGN KEY (member_id) REFERENCES cmda_members (id),
    CONSTRAINT fk_member_service_assignment_service FOREIGN KEY (service_id) REFERENCES community_services (id)
) ENGINE = InnoDB;

CREATE TABLE member_responsibilities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    title VARCHAR(160) NOT NULL,
    context_label VARCHAR(160) NULL,
    description VARCHAR(1000) NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_member_responsibility_member FOREIGN KEY (member_id) REFERENCES cmda_members (id)
) ENGINE = InnoDB;

CREATE INDEX idx_member_group_assignment_member_dates ON member_group_assignments (member_id, start_date, end_date);
CREATE INDEX idx_member_service_assignment_member_dates ON member_service_assignments (member_id, start_date, end_date);
CREATE INDEX idx_member_responsibility_member_dates ON member_responsibilities (member_id, start_date, end_date);

INSERT INTO community_groups (code, label, description, display_order, active) VALUES
    ('CATECHESE', 'Catechese', 'Groupe de formation et transmission de la foi.', 10, TRUE),
    ('CHORALE', 'Chorale', 'Groupe musical et liturgique.', 20, TRUE),
    ('LITURGIE', 'Liturgie', 'Groupe de preparation liturgique.', 30, TRUE),
    ('JEUNES_SERVITEURS', 'Jeunes serviteurs', 'Groupe de service communautaire.', 40, TRUE);

INSERT INTO community_services (code, label, description, display_order, active) VALUES
    ('ACCUEIL', 'Accueil', 'Accueil et orientation des membres.', 10, TRUE),
    ('PRIERE', 'Priere', 'Service de priere communautaire.', 20, TRUE),
    ('FORMATION', 'Formation', 'Service de formation.', 30, TRUE),
    ('ACTION_SOCIALE', 'Action sociale', 'Service de solidarite.', 40, TRUE);
