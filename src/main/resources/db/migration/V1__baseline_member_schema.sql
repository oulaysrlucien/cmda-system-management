CREATE TABLE IF NOT EXISTS cmda_members (
    id BIGINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL,
    birthday DATE NULL,
    profession VARCHAR(255) NULL,
    status VARCHAR(32) NOT NULL,
    fraternity_id BIGINT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB;
