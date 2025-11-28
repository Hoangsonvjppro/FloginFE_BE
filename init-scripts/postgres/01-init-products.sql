-- PostgreSQL initialization script for products database
-- Database is already created by POSTGRES_DB environment variable

-- Grant all privileges to product_user (if needed)
GRANT ALL PRIVILEGES ON DATABASE products TO product_user;

-- Products table will be created automatically by JPA/Hibernate
-- This script just ensures proper permissions

-- Optional: Create extensions if needed
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
