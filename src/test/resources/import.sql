INSERT INTO supported_currencies (currency_code) VALUES ('USD');
INSERT INTO supported_currencies (currency_code) VALUES ('EUR');
INSERT INTO supported_currencies (currency_code) VALUES ('GBP');
INSERT INTO supported_currencies (currency_code) VALUES ('JPY');
INSERT INTO supported_currencies (currency_code) VALUES ('CHF');
INSERT INTO supported_currencies (currency_code) VALUES ('CAD');
INSERT INTO supported_currencies (currency_code) VALUES ('AUD');
INSERT INTO supported_currencies (currency_code) VALUES ('CNY');
INSERT INTO supported_currencies (currency_code) VALUES ('SEK');
INSERT INTO supported_currencies (currency_code) VALUES ('NZD');

INSERT INTO roles (id, name) VALUES (1, 'ROLE_USER');
INSERT INTO roles (id, name) VALUES (2, 'ROLE_PREMIUM_USER');
INSERT INTO roles (id, name) VALUES (3, 'ROLE_ADMIN');

INSERT INTO users (id, email, password, enabled, created_at) VALUES (100, 'user@example.com', '$2a$12$AStxqy4GQKsltj4NHr945uueGBRZcYd7CAJlGU/n9uPhlmDZibCMm', true, CURRENT_TIMESTAMP);
INSERT INTO users (id, email, password, enabled, created_at) VALUES (101, 'premium@example.com', '$2a$12$AStxqy4GQKsltj4NHr945uueGBRZcYd7CAJlGU/n9uPhlmDZibCMm', true, CURRENT_TIMESTAMP);
INSERT INTO users (id, email, password, enabled, created_at) VALUES (102, 'admin@example.com', '$2a$12$AStxqy4GQKsltj4NHr945uueGBRZcYd7CAJlGU/n9uPhlmDZibCMm', true, CURRENT_TIMESTAMP);

INSERT INTO user_roles (user_id, role_id) VALUES (100, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (101, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (101, 2);
INSERT INTO user_roles (user_id, role_id) VALUES (102, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (102, 2);
INSERT INTO user_roles (user_id, role_id) VALUES (102, 3);
