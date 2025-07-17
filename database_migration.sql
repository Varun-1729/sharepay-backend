-- Add password column to users table
ALTER TABLE users ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT '';

-- Update existing users with a default password (they'll need to register again)
-- Password is 'demo123' hashed with BCrypt
UPDATE users SET password = '$2a$10$N.zmdr9k7uOLQvQHbh/OWu6fcrCcXhUh6LoqMwpY0s4pMiRlZOCvK' WHERE password = '';

-- Note: Existing users will need to register again with their email to set a proper password
-- Or you can manually update their passwords in the database
