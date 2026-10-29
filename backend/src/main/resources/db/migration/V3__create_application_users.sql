CREATE TABLE app_users (
 subject VARCHAR(255) NOT NULL PRIMARY KEY,
 email VARCHAR(254),
 display_name VARCHAR(160),
 last_seen_at TIMESTAMP(6) NOT NULL
);
CREATE INDEX idx_app_users_email ON app_users(email);
CREATE TABLE app_user_roles (
 subject VARCHAR(255) NOT NULL,
 role VARCHAR(16) NOT NULL,
 PRIMARY KEY(subject, role),
 CONSTRAINT fk_app_user_roles_user FOREIGN KEY(subject) REFERENCES app_users(subject) ON DELETE CASCADE
);
CREATE INDEX idx_app_user_roles_role ON app_user_roles(role);
