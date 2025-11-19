-- PostgreSQL initialization script
-- This script runs automatically when PostgreSQL container starts

-- Database is already created by POSTGRES_DB environment variable
-- Just verify connection
SELECT 'PostgreSQL database initialized successfully' AS status;

-- Grant all privileges to product_user (already done by default)
GRANT ALL PRIVILEGES ON DATABASE products TO product_user;
