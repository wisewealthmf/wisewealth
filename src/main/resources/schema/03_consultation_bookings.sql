-- ============================================================
-- 03_consultation_bookings.sql
-- Consultation / appointment bookings submitted via the website.
-- Depends on: 02_users.sql, 01_enums.sql
-- ============================================================

CREATE TABLE consultation_bookings (
    consultation_id BIGSERIAL    PRIMARY KEY,

    -- Optional link to a registered user (NULL for guest bookings)
    user_id         BIGINT       REFERENCES users (user_id) ON DELETE SET NULL,

    -- Submitter details (captured at booking time, independent of user account)
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255),
    phone           VARCHAR(30),
    financial_goal  TEXT,

    -- Workflow status
    status          status_enum  NOT NULL DEFAULT 'NEW',

    -- Internal admin notes
    notes           TEXT,

    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_consultation_user_id ON consultation_bookings (user_id);
CREATE INDEX idx_consultation_status  ON consultation_bookings (status);

-- Auto-update updated_at on every row modification
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_consultation_updated_at
    BEFORE UPDATE ON consultation_bookings
    FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();
