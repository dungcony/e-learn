-- Tạo các tài khoản nhân viên mặc định (Mật khẩu: 123456)
-- 1. Tài khoản Bếp (CHEF)
INSERT INTO users (
    id, email, password_hash, full_name, role, status, email_verified, must_change_password, created_at, updated_at
) VALUES (
    '22222222-2222-2222-2222-222222222222', 
    'bep@gmail.com', 
    crypt('123456', gen_salt('bf', 10)), 
    'Bếp Trưởng', 
    'CHEF', 
    'ACTIVE', 
    true, 
    false, 
    now(), 
    now()
) ON CONFLICT (id) DO NOTHING;

-- 2. Tài khoản Thu ngân (CASHIER)
INSERT INTO users (
    id, email, password_hash, full_name, role, status, email_verified, must_change_password, created_at, updated_at
) VALUES (
    '33333333-3333-3333-3333-333333333333', 
    'thungan@gmail.com', 
    crypt('123456', gen_salt('bf', 10)), 
    'Thu Ngân 1', 
    'CASHIER', 
    'ACTIVE', 
    true, 
    false, 
    now(), 
    now()
) ON CONFLICT (id) DO NOTHING;

-- 3. Tài khoản Phục vụ (WAITER)
INSERT INTO users (
    id, email, password_hash, full_name, role, status, email_verified, must_change_password, created_at, updated_at
) VALUES (
    '44444444-4444-4444-4444-444444444444', 
    'phucvu@gmail.com', 
    crypt('123456', gen_salt('bf', 10)), 
    'Phục Vụ 1', 
    'WAITER', 
    'ACTIVE', 
    true, 
    false, 
    now(), 
    now()
) ON CONFLICT (id) DO NOTHING;

