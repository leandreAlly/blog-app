-- Drop old constraint that only allowed BLOGGER and READER
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;

-- Rename any existing BLOGGER users to AUTHOR
UPDATE users SET role = 'AUTHOR' WHERE role = 'BLOGGER';

-- Add updated constraint with the three canonical roles
ALTER TABLE users
    ADD CONSTRAINT users_role_check CHECK (role IN ('ADMIN', 'AUTHOR', 'READER'));
