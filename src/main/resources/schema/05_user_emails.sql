-- ============================================================
-- 05_user_emails.sql
-- Lead-capture table: stores emails (and names) collected from
-- the Free Guide modal and the Financial Health Check tool.
-- No dependency on users — captures guest leads independently.
-- ============================================================

CREATE TABLE user_emails (
    id              BIGSERIAL    PRIMARY KEY,

    -- Submitter details
    name            VARCHAR(255) NOT NULL DEFAULT '',
    email           VARCHAR(255) NOT NULL UNIQUE,

    -- Where the lead came from: FREE_GUIDE | WEALTH_CHECK
    purpose         VARCHAR(50)  NOT NULL DEFAULT 'FREE_GUIDE',

    -- TRUE if this email also has a registered account in the users table
    is_user         BOOLEAN      NOT NULL DEFAULT FALSE,

    -- Admin follow-up tracking
    has_followed_up BOOLEAN      NOT NULL DEFAULT FALSE,

    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_emails_email ON user_emails (email);
