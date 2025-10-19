-- =========================================
-- Initial Data for EMS
-- This script inserts sample data for development and testing
-- =========================================

-- =========================================
-- 1. ROLES
-- =========================================
INSERT INTO role (code, name) VALUES
    ('ADMIN', 'System Administrator'),
    ('MANAGER', 'Operations Manager'),
    ('CENTER_HEAD', 'Branch Director'),
    ('ACADEMIC_STAFF', 'Academic Staff'),
    ('SUBJECT_LEADER', 'Subject Leader'),
    ('TEACHER', 'Teacher'),
    ('STUDENT', 'Student'),
    ('QA', 'Quality Assurance')
ON CONFLICT (code) DO NOTHING;

-- =========================================
-- 2. CENTERS
-- =========================================
INSERT INTO center (code, name, description, phone, email, created_at, updated_at) VALUES
    ('CTR001', 'English Center Vietnam', 'Main language training center', '0901234567', 'contact@englishcenter.vn', NOW(), NOW()),
    ('CTR002', 'Language Academy', 'Multi-language training center', '0907654321', 'info@langacademy.vn', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- =========================================
-- 3. BRANCHES
-- =========================================
INSERT INTO branch (center_id, code, name, address, location, phone, capacity, status, opening_date, created_at, updated_at)
SELECT
    c.id,
    b.code,
    b.name,
    b.address,
    b.location,
    b.phone,
    b.capacity,
    b.status::branch_status_enum,
    b.opening_date,
    NOW(),
    NOW()
FROM center c
CROSS JOIN (VALUES
    ('HN01', 'Ha Noi Branch 1', '123 Ba Dinh, Ha Noi', 'Ha Noi', '0901111111', 200, 'active', '2020-01-15'),
    ('HN02', 'Ha Noi Branch 2', '456 Dong Da, Ha Noi', 'Ha Noi', '0901111112', 150, 'active', '2021-03-20'),
    ('HCM01', 'Ho Chi Minh Branch 1', '789 District 1, HCM', 'Ho Chi Minh', '0902222221', 250, 'active', '2020-06-10'),
    ('DN01', 'Da Nang Branch', '321 Hai Chau, Da Nang', 'Da Nang', '0903333331', 100, 'active', '2022-01-05')
) AS b(code, name, address, location, phone, capacity, status, opening_date)
WHERE c.code = 'CTR001'
ON CONFLICT (center_id, code) DO NOTHING;

-- =========================================
-- 4. USER ACCOUNTS
-- Password for all users: "12345678" (hashed with BCrypt)
-- =========================================
INSERT INTO user_account (email, phone, full_name, password_hash, status, created_at, updated_at) VALUES
    -- Admin user
    ('admin@ems.vn', '0900000001', 'System Administrator', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),

    -- Manager users
    ('manager@ems.vn', '0900000002', 'Nguyen Van Manager', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),

    -- Center Head users (one per branch)
    ('centerhead.hn01@ems.vn', '0900000003', 'Tran Thi Head HN1', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('centerhead.hn02@ems.vn', '0900000004', 'Le Van Head HN2', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('centerhead.hcm01@ems.vn', '0900000005', 'Pham Thi Head HCM1', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),

    -- Academic Staff users
    ('staff.hn01@ems.vn', '0900000010', 'Nguyen Van Staff HN1', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('staff.hcm01@ems.vn', '0900000011', 'Tran Thi Staff HCM1', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),

    -- Subject Leader users
    ('subjectlead@ems.vn', '0900000020', 'Le Van Subject Leader', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),

    -- Teacher users
    ('teacher1@ems.vn', '0900000030', 'Pham Van Teacher 1', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('teacher2@ems.vn', '0900000031', 'Hoang Thi Teacher 2', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),

    -- Student users
    ('student1@ems.vn', '0900000040', 'Nguyen Van Student 1', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student2@ems.vn', '0900000041', 'Tran Thi Student 2', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),

    -- QA user
    ('qa@ems.vn', '0900000050', 'Le Van QA', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- =========================================
-- 5. USER ROLES ASSIGNMENT
-- =========================================
-- Admin role
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM user_account u, role r
WHERE u.email = 'admin@ems.vn' AND r.code = 'ADMIN'
ON CONFLICT DO NOTHING;

-- Manager role
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM user_account u, role r
WHERE u.email = 'manager@ems.vn' AND r.code = 'MANAGER'
ON CONFLICT DO NOTHING;

-- Center Head roles
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM user_account u, role r
WHERE u.email IN ('centerhead.hn01@ems.vn', 'centerhead.hn02@ems.vn', 'centerhead.hcm01@ems.vn')
  AND r.code = 'CENTER_HEAD'
ON CONFLICT DO NOTHING;

-- Academic Staff roles
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM user_account u, role r
WHERE u.email IN ('staff.hn01@ems.vn', 'staff.hcm01@ems.vn')
  AND r.code = 'ACADEMIC_STAFF'
ON CONFLICT DO NOTHING;

-- Subject Leader role
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM user_account u, role r
WHERE u.email = 'subjectlead@ems.vn' AND r.code = 'SUBJECT_LEADER'
ON CONFLICT DO NOTHING;

-- Teacher roles
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM user_account u, role r
WHERE u.email IN ('teacher1@ems.vn', 'teacher2@ems.vn')
  AND r.code = 'TEACHER'
ON CONFLICT DO NOTHING;

-- Student roles
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM user_account u, role r
WHERE u.email IN ('student1@ems.vn', 'student2@ems.vn')
  AND r.code = 'STUDENT'
ON CONFLICT DO NOTHING;

-- QA role
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM user_account u, role r
WHERE u.email = 'qa@ems.vn' AND r.code = 'QA'
ON CONFLICT DO NOTHING;

-- =========================================
-- 6. USER BRANCH ASSIGNMENTS
-- =========================================
-- Manager has access to all branches
INSERT INTO user_branch (user_id, branch_id)
SELECT u.id, b.id
FROM user_account u
CROSS JOIN branch b
WHERE u.email = 'manager@ems.vn'
ON CONFLICT DO NOTHING;

-- Center Head HN01
INSERT INTO user_branch (user_id, branch_id)
SELECT u.id, b.id
FROM user_account u, branch b
WHERE u.email = 'centerhead.hn01@ems.vn' AND b.code = 'HN01'
ON CONFLICT DO NOTHING;

-- Center Head HN02
INSERT INTO user_branch (user_id, branch_id)
SELECT u.id, b.id
FROM user_account u, branch b
WHERE u.email = 'centerhead.hn02@ems.vn' AND b.code = 'HN02'
ON CONFLICT DO NOTHING;

-- Center Head HCM01
INSERT INTO user_branch (user_id, branch_id)
SELECT u.id, b.id
FROM user_account u, branch b
WHERE u.email = 'centerhead.hcm01@ems.vn' AND b.code = 'HCM01'
ON CONFLICT DO NOTHING;

-- Academic Staff HN01
INSERT INTO user_branch (user_id, branch_id)
SELECT u.id, b.id
FROM user_account u, branch b
WHERE u.email = 'staff.hn01@ems.vn' AND b.code = 'HN01'
ON CONFLICT DO NOTHING;

-- Academic Staff HCM01
INSERT INTO user_branch (user_id, branch_id)
SELECT u.id, b.id
FROM user_account u, branch b
WHERE u.email = 'staff.hcm01@ems.vn' AND b.code = 'HCM01'
ON CONFLICT DO NOTHING;

-- QA has read-only access to all branches
INSERT INTO user_branch (user_id, branch_id)
SELECT u.id, b.id
FROM user_account u
CROSS JOIN branch b
WHERE u.email = 'qa@ems.vn'
ON CONFLICT DO NOTHING;
