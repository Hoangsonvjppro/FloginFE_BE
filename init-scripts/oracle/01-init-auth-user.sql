-- Create auth_user for authentication
CREATE USER auth_user IDENTIFIED BY "AuthPassword123";

-- Grant privileges
GRANT CONNECT, RESOURCE TO auth_user;
GRANT CREATE SESSION TO auth_user;
GRANT CREATE TABLE TO auth_user;
GRANT CREATE SEQUENCE TO auth_user;
GRANT CREATE VIEW TO auth_user;
GRANT UNLIMITED TABLESPACE TO auth_user;

-- Connect as auth_user and create tables
ALTER SESSION SET CURRENT_SCHEMA = auth_user;

-- Users table will be created automatically by JPA/Hibernate
-- This script just sets up the user and permissions

EXIT;
