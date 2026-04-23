-- V10__create_auth_tables.sql

CREATE TABLE IF NOT EXISTS users (
                                     user_id CHAR(36) PRIMARY KEY,
                                     email VARCHAR(100) NOT NULL UNIQUE,
                                     password_hash VARCHAR(60) NOT NULL,
                                     name VARCHAR(100) NOT NULL,
                                     active BOOLEAN NOT NULL DEFAULT FALSE,
                                     super_admin BOOLEAN NOT NULL DEFAULT FALSE,
                                     created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              token_id CHAR(36) PRIMARY KEY,
                                              user_id CHAR(36) NOT NULL,
                                              token_hash VARCHAR(64) NOT NULL,
                                              expires_at TIMESTAMP NOT NULL,
                                              used_at TIMESTAMP NULL,
                                              revoked_at TIMESTAMP NULL,
                                              replaced_by CHAR(36) NULL,
                                              created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                              INDEX idx_refresh_token_hash (token_hash),
                                              INDEX idx_refresh_user_id (user_id)
);

-- Seed super-admin user.
-- Email: superadmin@minebuddy.com
-- Password: SuperAdmin123! (BCrypt hash below — change after first login!)
-- Active + super_admin are both TRUE by default here.
INSERT INTO users (user_id, email, password_hash, name, active, super_admin)
VALUES (
           UUID(),
           'superadmin@minebuddy.com',
           '$2a$10$N9qo8uLOickgx2ZMRZoMye.FzGZQh6LMfDl3x9RiKC1yWUOWLKbqu',
           'Super Admin',
           TRUE,
           TRUE
       );