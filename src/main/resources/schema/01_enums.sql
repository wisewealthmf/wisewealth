-- ============================================================
-- 01_enums.sql
-- PostgreSQL custom enum types used across the WiseWealth schema.
-- Must be created before any table that references them.
-- ============================================================

-- Status values for consultation_bookings and queries
-- Stored as uppercase Java enum names (e.g. NEW, IN_PROGRESS)
CREATE TYPE status_enum AS ENUM (
    'NEW',
    'IN_PROGRESS',
    'APPOINTMENT_CONFIRMED',
    'REPLIED',
    'CLIENT_CONFIRMED',
    'CLOSE'
);

-- Category values for queries
CREATE TYPE category_enum AS ENUM (
    'ACCOUNT_OPENING',
    'SIP',
    'LUMPSUM',
    'GOAL_PLANNING',
    'RETIREMENT',
    'TAX',
    'PORTFOLIO_REVIEW',
    'OTHER'
);
