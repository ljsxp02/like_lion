CREATE TABLE colleges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    college_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT fk_departments_college FOREIGN KEY (college_id) REFERENCES colleges (id)
);

CREATE TABLE stores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    contact VARCHAR(50),
    thumbnail_url VARCHAR(500),
    latitude DOUBLE,
    longitude DOUBLE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    user_type VARCHAR(20) NOT NULL,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    college_id BIGINT,
    department_id BIGINT,
    store_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_college FOREIGN KEY (college_id) REFERENCES colleges (id),
    CONSTRAINT fk_users_department FOREIGN KEY (department_id) REFERENCES departments (id),
    CONSTRAINT fk_users_store FOREIGN KEY (store_id) REFERENCES stores (id)
);

CREATE TABLE menus (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    image_url VARCHAR(500),
    is_representative BOOLEAN NOT NULL,
    display_order INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_menus_store FOREIGN KEY (store_id) REFERENCES stores (id)
);

CREATE TABLE benefits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    is_school_wide BOOLEAN NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_benefits_store FOREIGN KEY (store_id) REFERENCES stores (id)
);

CREATE TABLE benefit_target_colleges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    benefit_id BIGINT NOT NULL,
    college_id BIGINT NOT NULL,
    CONSTRAINT fk_benefit_target_colleges_benefit FOREIGN KEY (benefit_id) REFERENCES benefits (id),
    CONSTRAINT fk_benefit_target_colleges_college FOREIGN KEY (college_id) REFERENCES colleges (id),
    CONSTRAINT uk_benefit_target_colleges UNIQUE (benefit_id, college_id)
);

CREATE TABLE benefit_target_departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    benefit_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    CONSTRAINT fk_benefit_target_departments_benefit FOREIGN KEY (benefit_id) REFERENCES benefits (id),
    CONSTRAINT fk_benefit_target_departments_department FOREIGN KEY (department_id) REFERENCES departments (id),
    CONSTRAINT uk_benefit_target_departments UNIQUE (benefit_id, department_id)
);

CREATE TABLE favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_favorites_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_favorites_store FOREIGN KEY (store_id) REFERENCES stores (id),
    CONSTRAINT uk_favorites_user_store UNIQUE (user_id, store_id)
);

CREATE TABLE qr_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    issued_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_qr_tokens_store FOREIGN KEY (store_id) REFERENCES stores (id)
);

CREATE TABLE benefit_usages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    qr_token_id BIGINT NOT NULL,
    used_at DATETIME NOT NULL,
    CONSTRAINT fk_benefit_usages_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_benefit_usages_store FOREIGN KEY (store_id) REFERENCES stores (id),
    CONSTRAINT fk_benefit_usages_qr_token FOREIGN KEY (qr_token_id) REFERENCES qr_tokens (id),
    CONSTRAINT uk_benefit_usages_user_qr_token UNIQUE (user_id, qr_token_id)
);

CREATE TABLE email_verification_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(120) NOT NULL,
    code VARCHAR(20) NOT NULL,
    verification_token VARCHAR(255),
    expires_at DATETIME NOT NULL,
    resend_available_at DATETIME NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_departments_college_id ON departments (college_id);
CREATE INDEX idx_stores_active_name ON stores (is_active, name);
CREATE INDEX idx_menus_store_order ON menus (store_id, display_order);
CREATE INDEX idx_benefits_store_active ON benefits (store_id, is_active);
CREATE INDEX idx_favorites_user_id ON favorites (user_id);
CREATE INDEX idx_qr_tokens_store_id ON qr_tokens (store_id);
CREATE INDEX idx_benefit_usages_user_used_at ON benefit_usages (user_id, used_at);
CREATE INDEX idx_email_verification_codes_email_id ON email_verification_codes (email, id);
