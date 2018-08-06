ALTER SYSTEM SET max_prepared_transactions = 64;

-- Create the KeyCloak database
CREATE USER keycloak WITH PASSWORD 'password';
CREATE DATABASE keycloak;
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;

