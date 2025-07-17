-- Add created_by column to users table for user isolation
ALTER TABLE users ADD COLUMN created_by BIGINT;

-- Add foreign key constraint
ALTER TABLE users ADD CONSTRAINT fk_users_created_by 
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;

-- Create index for better performance
CREATE INDEX idx_users_created_by ON users(created_by);

-- Update existing users to have no creator (they are root users)
-- This allows existing users to continue working normally
UPDATE users SET created_by = NULL WHERE created_by IS NULL;
