-- =============================================================================
-- Flyway Migration V10: Seed System Users
-- Description: Creates admin and service account users for the ecommerce system
-- Author: Development Team
-- Date: 2026-02-01
-- =============================================================================

-- Create admin user
-- Password: admin123 (bcrypt hashed)
-- Role: ADMIN
INSERT INTO users (id, username, email, password_hash, enabled, created_at)
VALUES (
    gen_random_uuid(),
    'admin',
    'admin@ecommerce.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    true,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;  -- Idempotent: skip if already exists

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN'
FROM users
WHERE username = 'admin'
AND NOT EXISTS (
    SELECT 1 FROM user_roles
    WHERE user_id = (SELECT id FROM users WHERE username = 'admin')
    AND role = 'ADMIN'
);

-- Create service account for inter-service communication
-- Password: admin123 (same hash for simplicity)
-- Role: SERVICE
INSERT INTO users (id, username, email, password_hash, enabled, created_at)
VALUES (
    gen_random_uuid(),
    'service-account',
    'service@ecommerce.internal',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    true,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

-- Assign SERVICE role to service account
INSERT INTO user_roles (user_id, role)
SELECT id, 'SERVICE'
FROM users
WHERE username = 'service-account'
AND NOT EXISTS (
    SELECT 1 FROM user_roles
    WHERE user_id = (SELECT id FROM users WHERE username = 'service-account')
    AND role = 'SERVICE'
);

-- Verification: Count system users
-- Expected: 2 users (admin + service-account)
DO $$
DECLARE
    user_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO user_count FROM users
    WHERE username IN ('admin', 'service-account');

    RAISE NOTICE 'System users created: %', user_count;

    IF user_count < 2 THEN
        RAISE EXCEPTION 'System users not created properly. Expected 2, got %', user_count;
    END IF;
END $$;
