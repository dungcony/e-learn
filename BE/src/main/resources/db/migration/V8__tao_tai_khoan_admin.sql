CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Tạo tài khoản Quản lý (Manager) mặc định
-- Mật khẩu mặc định: 123456
INSERT INTO users (
    id, 
    email, 
    password_hash, 
    full_name, 
    role, 
    status, 
    email_verified, 
    must_change_password, 
    created_at, 
    updated_at
) VALUES (
    '11111111-1111-1111-1111-111111111111', 
    'admin@gmail.com', 
    crypt('123456', gen_salt('bf', 10)), 
    'Quản trị viên', 
    'MANAGER', 
    'ACTIVE', 
    true, 
    false, 
    now(), 
    now()
) ON CONFLICT (id) DO NOTHING;

