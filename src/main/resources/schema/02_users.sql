-- ============================================================
-- 02_users.sql
-- Registered application users (both regular users and admins).
-- ============================================================

CREATE TABLE users (
    user_id                   BIGSERIAL       PRIMARY KEY,
    name                      VARCHAR(255)    NOT NULL,
    email                     VARCHAR(255)    NOT NULL UNIQUE,
    password_hash             VARCHAR(255)    NOT NULL,
    phone                     VARCHAR(30),

    -- Account status
    is_active                 BOOLEAN         NOT NULL DEFAULT TRUE,

    -- Email verification
    is_email_verified         BOOLEAN         NOT NULL DEFAULT FALSE,
    verification_token        VARCHAR(255),
    verification_token_expiry TIMESTAMP,

    -- Admin flag (true = full admin access)
    is_admin                  BOOLEAN         NOT NULL DEFAULT FALSE,

    created_at                TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Fast lookup by email (login, duplicate check, verification)
CREATE INDEX idx_users_email ON users (email);
