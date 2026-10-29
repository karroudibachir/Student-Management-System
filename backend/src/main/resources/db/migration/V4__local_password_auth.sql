ALTER TABLE app_users ADD COLUMN password_hash VARCHAR(100);
ALTER TABLE app_users ADD COLUMN student_id BIGINT;
DELETE old_viewer FROM app_user_roles old_viewer
JOIN app_user_roles old_staff ON old_staff.subject = old_viewer.subject
WHERE old_viewer.role = 'VIEWER' AND old_staff.role = 'STAFF';
UPDATE app_user_roles SET role = 'STUDENT' WHERE role IN ('STAFF', 'VIEWER');
UPDATE app_users u JOIN students s ON LOWER(u.email) = LOWER(s.email) SET u.student_id = s.id;
CREATE UNIQUE INDEX uq_app_users_email ON app_users(email);
CREATE UNIQUE INDEX uq_app_users_student_id ON app_users(student_id);
ALTER TABLE app_users ADD CONSTRAINT fk_app_users_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE SET NULL;
