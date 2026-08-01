-- ============================================================
-- 04_queries.sql
-- Customer queries / support requests submitted via the website.
-- Depends on: 02_users.sql, 01_enums.sql
-- ============================================================

CREATE TABLE queries (
    query_id        BIGSERIAL      PRIMARY KEY,

    -- Optional link to a registered user (NULL for guest queries)
    user_id         BIGINT         REFERENCES users (user_id) ON DELETE SET NULL,

    -- Submitter details (captured at submission time)
    name            VARCHAR(255)   NOT NULL,
    email           VARCHAR(255),
    phone           VARCHAR(30),

    -- Query content
    query_text      TEXT           NOT NULL,
    category        category_enum  NOT NULL DEFAULT 'OTHER',

    -- Workflow status and admin reply
    status          status_enum    NOT NULL DEFAULT 'NEW',
    reply           TEXT,

    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_queries_user_id      ON queries (user_id);
CREATE INDEX idx_queries_status       ON queries (status);
CREATE INDEX idx_queries_category     ON queries (category);

-- Reuse the same trigger function created in 03_consultation_bookings.sql
CREATE TRIGGER trg_queries_updated_at
    BEFORE UPDATE ON queries
    FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();
