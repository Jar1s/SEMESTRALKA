-- SQL schema initialization script
-- This script ensures the database has all required columns
-- It's safe to run multiple times (uses IF NOT EXISTS and ALTER TABLE IF NOT EXISTS)

-- Note: SQLite doesn't support IF NOT EXISTS for ALTER TABLE
-- So we'll handle this in the application code or use a migration approach

-- For now, Hibernate will create the schema automatically
-- This file is kept for reference and future migrations

