-- Create auth_user and grant permissions
-- This script runs automatically when Oracle container starts

ALTER SESSION SET CONTAINER = XEPDB1;

-- Drop user if exists (for re-runs)
BEGIN
   EXECUTE IMMEDIATE 'DROP USER auth_user CASCADE';
EXCEPTION
   WHEN OTHERS THEN
      IF SQLCODE != -1918 THEN
         RAISE;
      END IF;
END;
/

-- Create user
CREATE USER auth_user IDENTIFIED BY AuthPassword123
  DEFAULT TABLESPACE USERS
  TEMPORARY TABLESPACE TEMP
  QUOTA UNLIMITED ON USERS;

-- Grant necessary privileges
GRANT CREATE SESSION TO auth_user;
GRANT CREATE TABLE TO auth_user;
GRANT CREATE VIEW TO auth_user;
GRANT CREATE SEQUENCE TO auth_user;
GRANT CREATE TRIGGER TO auth_user;
GRANT CREATE PROCEDURE TO auth_user;

-- Grant additional privileges for Hibernate
GRANT CONNECT, RESOURCE TO auth_user;
GRANT UNLIMITED TABLESPACE TO auth_user;

EXIT;
