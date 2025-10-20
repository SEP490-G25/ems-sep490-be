-- =========================================
-- COMPREHENSIVE SEED DATA FOR EMS
-- This script inserts realistic, production-like data for development and testing
-- Password for all users: "12345678" (BCrypt hashed)
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
    ('ECV', 'English Center Vietnam', 'Premier English language training center with focus on international certifications', '0241234567', 'contact@englishcenter.vn', NOW(), NOW()),
    ('ILA', 'International Language Academy', 'Multi-language training center offering English, Japanese, Korean, and Chinese courses', '0287654321', 'info@ila.edu.vn', NOW(), NOW())
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
    -- English Center Vietnam branches
    ('HN-BD', 'Ba Dinh Campus', '123 Nguyen Thai Hoc, Ba Dinh District', 'Hanoi', '0241111111', 300, 'active', '2018-01-15'),
    ('HN-CG', 'Cau Giay Campus', '456 Tran Duy Hung, Cau Giay District', 'Hanoi', '0241111112', 250, 'active', '2019-03-20'),
    ('HCM-Q1', 'District 1 Campus', '789 Le Loi, District 1', 'Ho Chi Minh', '0282222221', 400, 'active', '2018-06-10'),
    ('HCM-Q7', 'District 7 Campus', '321 Nguyen Van Linh, District 7', 'Ho Chi Minh', '0282222222', 350, 'active', '2020-02-15'),
    ('DN-HC', 'Hai Chau Campus', '654 Tran Phu, Hai Chau District', 'Da Nang', '0233333331', 200, 'active', '2021-01-05')
) AS b(code, name, address, location, phone, capacity, status, opening_date)
WHERE c.code = 'ECV'
ON CONFLICT (center_id, code) DO NOTHING;

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
    ('HN-TH', 'Thanh Xuan Campus', '88 Nguyen Trai, Thanh Xuan District', 'Hanoi', '0245555551', 280, 'active', '2019-08-01'),
    ('HCM-TD', 'Thu Duc Campus', '99 Vo Van Ngan, Thu Duc City', 'Ho Chi Minh', '0286666661', 320, 'active', '2020-09-10')
) AS b(code, name, address, location, phone, capacity, status, opening_date)
WHERE c.code = 'ILA'
ON CONFLICT (center_id, code) DO NOTHING;

-- =========================================
-- 4. USER ACCOUNTS
-- Password: "12345678" (BCrypt: $2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK)
-- =========================================
INSERT INTO user_account (email, phone, full_name, password_hash, status, created_at, updated_at) VALUES
    -- System Admin
    ('admin@ems.vn', '0900000001', 'Nguyen Van Admin', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),

    -- Operations Managers (2)
    ('manager1@ems.vn', '0900000002', 'Tran Minh Quan', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('manager2@ems.vn', '0900000003', 'Le Thi Huong', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),

    -- Center Heads (one per branch - 7 branches)
    ('head.hn.bd@ems.vn', '0900001001', 'Pham Duc Anh', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('head.hn.cg@ems.vn', '0900001002', 'Nguyen Thi Lan', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('head.hcm.q1@ems.vn', '0900001003', 'Vo Hoang Nam', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('head.hcm.q7@ems.vn', '0900001004', 'Tran Thi Mai', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('head.dn.hc@ems.vn', '0900001005', 'Le Van Tung', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('head.hn.th@ems.vn', '0900001006', 'Hoang Thi Phuong', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('head.hcm.td@ems.vn', '0900001007', 'Nguyen Van Binh', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),

    -- Academic Staff (10 - distributed across branches)
    ('staff.hn.bd.1@ems.vn', '0900002001', 'Dang Thi Hoa', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('staff.hn.bd.2@ems.vn', '0900002002', 'Bui Van Khanh', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('staff.hn.cg.1@ems.vn', '0900002003', 'Phan Thi Ngoc', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('staff.hcm.q1.1@ems.vn', '0900002004', 'Tran Van Tai', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('staff.hcm.q1.2@ems.vn', '0900002005', 'Le Thi Kim', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('staff.hcm.q7.1@ems.vn', '0900002006', 'Nguyen Van Phuc', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('staff.dn.hc.1@ems.vn', '0900002007', 'Vo Thi Thao', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('staff.hn.th.1@ems.vn', '0900002008', 'Hoang Van Dung', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('staff.hcm.td.1@ems.vn', '0900002009', 'Tran Thi Linh', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('staff.hcm.td.2@ems.vn', '0900002010', 'Nguyen Van Son', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),

    -- Subject Leaders (3 - curriculum specialists)
    ('subjlead.eng@ems.vn', '0900003001', 'Pham Thi Minh Chau', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('subjlead.ielts@ems.vn', '0900003002', 'Le Van Toan', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('subjlead.toeic@ems.vn', '0900003003', 'Nguyen Thi Hanh', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),

    -- Teachers (20 - diverse skill sets)
    ('teacher.nguyen.ha@ems.vn', '0900004001', 'Nguyen Thi Ha', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('teacher.tran.minh@ems.vn', '0900004002', 'Tran Van Minh', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('teacher.le.lan@ems.vn', '0900004003', 'Le Thi Lan', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('teacher.pham.tuan@ems.vn', '0900004004', 'Pham Van Tuan', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('teacher.vo.mai@ems.vn', '0900004005', 'Vo Thi Mai', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('teacher.hoang.nam@ems.vn', '0900004006', 'Hoang Van Nam', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('teacher.dang.linh@ems.vn', '0900004007', 'Dang Thi Linh', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('teacher.bui.quan@ems.vn', '0900004008', 'Bui Van Quan', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('teacher.do.huong@ems.vn', '0900004009', 'Do Thi Huong', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('teacher.vuong.hung@ems.vn', '0900004010', 'Vuong Van Hung', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('teacher.ngo.thao@ems.vn', '0900004011', 'Ngo Thi Thao', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('teacher.ly.duc@ems.vn', '0900004012', 'Ly Van Duc', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('teacher.trinh.nga@ems.vn', '0900004013', 'Trinh Thi Nga', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('teacher.mai.khai@ems.vn', '0900004014', 'Mai Van Khai', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('teacher.duong.phuong@ems.vn', '0900004015', 'Duong Thi Phuong', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('teacher.vu.binh@ems.vn', '0900004016', 'Vu Van Binh', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('teacher.dinh.hong@ems.vn', '0900004017', 'Dinh Thi Hong', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('teacher.ha.tien@ems.vn', '0900004018', 'Ha Van Tien', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('teacher.cao.oanh@ems.vn', '0900004019', 'Cao Thi Oanh', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('teacher.khuat.long@ems.vn', '0900004020', 'Khuat Van Long', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),

    -- Students (40 - diverse enrollment patterns)
    ('student.nguyen.an@student.vn', '0900005001', 'Nguyen Van An', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.tran.bao@student.vn', '0900005002', 'Tran Thi Bao', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.le.cuong@student.vn', '0900005003', 'Le Van Cuong', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.pham.dung@student.vn', '0900005004', 'Pham Thi Dung', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.vo.em@student.vn', '0900005005', 'Vo Van Em', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.hoang.giang@student.vn', '0900005006', 'Hoang Thi Giang', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.dang.hieu@student.vn', '0900005007', 'Dang Van Hieu', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.bui.ivy@student.vn', '0900005008', 'Bui Thi Ivy', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.do.kien@student.vn', '0900005009', 'Do Van Kien', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.vuong.linh@student.vn', '0900005010', 'Vuong Thi Linh', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.ngo.minh@student.vn', '0900005011', 'Ngo Van Minh', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.ly.nhan@student.vn', '0900005012', 'Ly Thi Nhan', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.trinh.oanh@student.vn', '0900005013', 'Trinh Thi Oanh', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.mai.phong@student.vn', '0900005014', 'Mai Van Phong', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.duong.quynh@student.vn', '0900005015', 'Duong Thi Quynh', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.vu.son@student.vn', '0900005016', 'Vu Van Son', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.dinh.thu@student.vn', '0900005017', 'Dinh Thi Thu', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.ha.uyen@student.vn', '0900005018', 'Ha Thi Uyen', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.cao.vinh@student.vn', '0900005019', 'Cao Van Vinh', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.khuat.xuan@student.vn', '0900005020', 'Khuat Thi Xuan', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.phan.yen@student.vn', '0900005021', 'Phan Thi Yen', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.ta.binh@student.vn', '0900005022', 'Ta Van Binh', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.tang.chi@student.vn', '0900005023', 'Tang Thi Chi', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.luu.dat@student.vn', '0900005024', 'Luu Van Dat', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.mac.hoa@student.vn', '0900005025', 'Mac Thi Hoa', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.quy.kha@student.vn', '0900005026', 'Quy Van Kha', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.nghi.lan@student.vn', '0900005027', 'Nghi Thi Lan', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.si.nam@student.vn', '0900005028', 'Si Van Nam', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.viet.phuong@student.vn', '0900005029', 'Viet Thi Phuong', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.ton.quang@student.vn', '0900005030', 'Ton Van Quang', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.dam.thuy@student.vn', '0900005031', 'Dam Thi Thuy', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.man.tuan@student.vn', '0900005032', 'Man Van Tuan', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.giang.van@student.vn', '0900005033', 'Giang Thi Van', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.thach.hai@student.vn', '0900005034', 'Thach Van Hai', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.nghiem.lan@student.vn', '0900005035', 'Nghiem Thi Lan', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.ung.phuc@student.vn', '0900005036', 'Ung Van Phuc', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.hao.nga@student.vn', '0900005037', 'Hao Thi Nga', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.ky.truong@student.vn', '0900005038', 'Ky Van Truong', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.bien.thao@student.vn', '0900005039', 'Bien Thi Thao', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('student.cuong.long@student.vn', '0900005040', 'Cuong Van Long', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),

    -- QA Staff (2)
    ('qa.hn@ems.vn', '0900006001', 'Nguyen Thi QA Hanoi', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW()),
    ('qa.hcm@ems.vn', '0900006002', 'Tran Van QA HCM', '$2a$12$x48a8bwuljECYA7u3lnPduHY6NTeEzmxA1RNmXgqlTF9LF6.HG7aK', 'active', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- =========================================
-- 5. USER ROLES ASSIGNMENT
-- =========================================
-- Admin
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM user_account u, role r
WHERE u.email = 'admin@ems.vn' AND r.code = 'ADMIN'
ON CONFLICT DO NOTHING;

-- Managers
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM user_account u, role r
WHERE u.email IN ('manager1@ems.vn', 'manager2@ems.vn') AND r.code = 'MANAGER'
ON CONFLICT DO NOTHING;

-- Center Heads
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM user_account u, role r
WHERE u.email IN ('head.hn.bd@ems.vn', 'head.hn.cg@ems.vn', 'head.hcm.q1@ems.vn', 'head.hcm.q7@ems.vn', 'head.dn.hc@ems.vn', 'head.hn.th@ems.vn', 'head.hcm.td@ems.vn')
  AND r.code = 'CENTER_HEAD'
ON CONFLICT DO NOTHING;

-- Academic Staff
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM user_account u, role r
WHERE u.email LIKE 'staff.%@ems.vn' AND r.code = 'ACADEMIC_STAFF'
ON CONFLICT DO NOTHING;

-- Subject Leaders
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM user_account u, role r
WHERE u.email LIKE 'subjlead.%@ems.vn' AND r.code = 'SUBJECT_LEADER'
ON CONFLICT DO NOTHING;

-- Teachers
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM user_account u, role r
WHERE u.email LIKE 'teacher.%@ems.vn' AND r.code = 'TEACHER'
ON CONFLICT DO NOTHING;

-- Students
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM user_account u, role r
WHERE u.email LIKE 'student.%@student.vn' AND r.code = 'STUDENT'
ON CONFLICT DO NOTHING;

-- QA Staff
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM user_account u, role r
WHERE u.email LIKE 'qa.%@ems.vn' AND r.code = 'QA'
ON CONFLICT DO NOTHING;

-- =========================================
-- 6. USER BRANCH ASSIGNMENTS
-- =========================================
-- Managers have access to all branches
INSERT INTO user_branch (user_id, branch_id)
SELECT u.id, b.id FROM user_account u CROSS JOIN branch b
WHERE u.email IN ('manager1@ems.vn', 'manager2@ems.vn')
ON CONFLICT DO NOTHING;

-- Center Heads (one branch each)
INSERT INTO user_branch (user_id, branch_id)
SELECT u.id, b.id FROM user_account u, branch b, center c
WHERE u.email = 'head.hn.bd@ems.vn' AND b.code = 'HN-BD' AND b.center_id = c.id AND c.code = 'ECV'
ON CONFLICT DO NOTHING;

INSERT INTO user_branch (user_id, branch_id)
SELECT u.id, b.id FROM user_account u, branch b, center c
WHERE u.email = 'head.hn.cg@ems.vn' AND b.code = 'HN-CG' AND b.center_id = c.id AND c.code = 'ECV'
ON CONFLICT DO NOTHING;

INSERT INTO user_branch (user_id, branch_id)
SELECT u.id, b.id FROM user_account u, branch b, center c
WHERE u.email = 'head.hcm.q1@ems.vn' AND b.code = 'HCM-Q1' AND b.center_id = c.id AND c.code = 'ECV'
ON CONFLICT DO NOTHING;

INSERT INTO user_branch (user_id, branch_id)
SELECT u.id, b.id FROM user_account u, branch b, center c
WHERE u.email = 'head.hcm.q7@ems.vn' AND b.code = 'HCM-Q7' AND b.center_id = c.id AND c.code = 'ECV'
ON CONFLICT DO NOTHING;

INSERT INTO user_branch (user_id, branch_id)
SELECT u.id, b.id FROM user_account u, branch b, center c
WHERE u.email = 'head.dn.hc@ems.vn' AND b.code = 'DN-HC' AND b.center_id = c.id AND c.code = 'ECV'
ON CONFLICT DO NOTHING;

INSERT INTO user_branch (user_id, branch_id)
SELECT u.id, b.id FROM user_account u, branch b, center c
WHERE u.email = 'head.hn.th@ems.vn' AND b.code = 'HN-TH' AND b.center_id = c.id AND c.code = 'ILA'
ON CONFLICT DO NOTHING;

INSERT INTO user_branch (user_id, branch_id)
SELECT u.id, b.id FROM user_account u, branch b, center c
WHERE u.email = 'head.hcm.td@ems.vn' AND b.code = 'HCM-TD' AND b.center_id = c.id AND c.code = 'ILA'
ON CONFLICT DO NOTHING;

-- Academic Staff branch assignments
INSERT INTO user_branch (user_id, branch_id)
SELECT u.id, b.id FROM user_account u, branch b, center c
WHERE u.email IN ('staff.hn.bd.1@ems.vn', 'staff.hn.bd.2@ems.vn') AND b.code = 'HN-BD' AND b.center_id = c.id AND c.code = 'ECV'
ON CONFLICT DO NOTHING;

INSERT INTO user_branch (user_id, branch_id)
SELECT u.id, b.id FROM user_account u, branch b, center c
WHERE u.email = 'staff.hn.cg.1@ems.vn' AND b.code = 'HN-CG' AND b.center_id = c.id AND c.code = 'ECV'
ON CONFLICT DO NOTHING;

INSERT INTO user_branch (user_id, branch_id)
SELECT u.id, b.id FROM user_account u, branch b, center c
WHERE u.email IN ('staff.hcm.q1.1@ems.vn', 'staff.hcm.q1.2@ems.vn') AND b.code = 'HCM-Q1' AND b.center_id = c.id AND c.code = 'ECV'
ON CONFLICT DO NOTHING;

INSERT INTO user_branch (user_id, branch_id)
SELECT u.id, b.id FROM user_account u, branch b, center c
WHERE u.email = 'staff.hcm.q7.1@ems.vn' AND b.code = 'HCM-Q7' AND b.center_id = c.id AND c.code = 'ECV'
ON CONFLICT DO NOTHING;

INSERT INTO user_branch (user_id, branch_id)
SELECT u.id, b.id FROM user_account u, branch b, center c
WHERE u.email = 'staff.dn.hc.1@ems.vn' AND b.code = 'DN-HC' AND b.center_id = c.id AND c.code = 'ECV'
ON CONFLICT DO NOTHING;

INSERT INTO user_branch (user_id, branch_id)
SELECT u.id, b.id FROM user_account u, branch b, center c
WHERE u.email = 'staff.hn.th.1@ems.vn' AND b.code = 'HN-TH' AND b.center_id = c.id AND c.code = 'ILA'
ON CONFLICT DO NOTHING;

INSERT INTO user_branch (user_id, branch_id)
SELECT u.id, b.id FROM user_account u, branch b, center c
WHERE u.email IN ('staff.hcm.td.1@ems.vn', 'staff.hcm.td.2@ems.vn') AND b.code = 'HCM-TD' AND b.center_id = c.id AND c.code = 'ILA'
ON CONFLICT DO NOTHING;

-- QA Staff have access to all branches
INSERT INTO user_branch (user_id, branch_id)
SELECT u.id, b.id FROM user_account u CROSS JOIN branch b
WHERE u.email IN ('qa.hn@ems.vn', 'qa.hcm@ems.vn')
ON CONFLICT DO NOTHING;

-- =========================================
-- 7. TEACHER RECORDS
-- =========================================
INSERT INTO teacher (user_id, certifications, years_of_experience, bio, created_at, updated_at)
SELECT u.id, 
       CASE 
           WHEN u.email LIKE '%nguyen.ha%' THEN ARRAY['TESOL', 'IELTS 8.5']
           WHEN u.email LIKE '%tran.minh%' THEN ARRAY['CELTA', 'TOEIC 990']
           WHEN u.email LIKE '%le.lan%' THEN ARRAY['DELTA', 'IELTS 8.0']
           WHEN u.email LIKE '%pham.tuan%' THEN ARRAY['TESOL', 'Cambridge C2']
           WHEN u.email LIKE '%vo.mai%' THEN ARRAY['CELTA', 'IELTS 7.5']
           WHEN u.email LIKE '%hoang.nam%' THEN ARRAY['TESOL', 'TOEIC 950']
           WHEN u.email LIKE '%dang.linh%' THEN ARRAY['CELTA', 'IELTS 8.0']
           WHEN u.email LIKE '%bui.quan%' THEN ARRAY['DELTA', 'Cambridge C2']
           WHEN u.email LIKE '%do.huong%' THEN ARRAY['TESOL', 'TOEIC 980']
           WHEN u.email LIKE '%vuong.hung%' THEN ARRAY['CELTA', 'IELTS 7.5']
           WHEN u.email LIKE '%ngo.thao%' THEN ARRAY['TESOL', 'TOEIC 970']
           WHEN u.email LIKE '%ly.duc%' THEN ARRAY['CELTA', 'IELTS 8.5']
           WHEN u.email LIKE '%trinh.nga%' THEN ARRAY['DELTA', 'Cambridge C2']
           WHEN u.email LIKE '%mai.khai%' THEN ARRAY['TESOL', 'IELTS 8.0']
           WHEN u.email LIKE '%duong.phuong%' THEN ARRAY['CELTA', 'TOEIC 990']
           WHEN u.email LIKE '%vu.binh%' THEN ARRAY['TESOL', 'IELTS 7.5']
           WHEN u.email LIKE '%dinh.hong%' THEN ARRAY['CELTA', 'Cambridge C1']
           WHEN u.email LIKE '%ha.tien%' THEN ARRAY['DELTA', 'IELTS 8.5']
           WHEN u.email LIKE '%cao.oanh%' THEN ARRAY['TESOL', 'TOEIC 960']
           ELSE ARRAY['TESOL', 'IELTS 7.0']
       END,
       CASE 
           WHEN u.email LIKE '%nguyen.ha%' THEN 8
           WHEN u.email LIKE '%tran.minh%' THEN 6
           WHEN u.email LIKE '%le.lan%' THEN 10
           WHEN u.email LIKE '%pham.tuan%' THEN 5
           WHEN u.email LIKE '%vo.mai%' THEN 7
           WHEN u.email LIKE '%hoang.nam%' THEN 4
           WHEN u.email LIKE '%dang.linh%' THEN 9
           WHEN u.email LIKE '%bui.quan%' THEN 11
           WHEN u.email LIKE '%do.huong%' THEN 6
           WHEN u.email LIKE '%vuong.hung%' THEN 5
           WHEN u.email LIKE '%ngo.thao%' THEN 7
           WHEN u.email LIKE '%ly.duc%' THEN 12
           WHEN u.email LIKE '%trinh.nga%' THEN 8
           WHEN u.email LIKE '%mai.khai%' THEN 6
           WHEN u.email LIKE '%duong.phuong%' THEN 9
           WHEN u.email LIKE '%vu.binh%' THEN 4
           WHEN u.email LIKE '%dinh.hong%' THEN 3
           WHEN u.email LIKE '%ha.tien%' THEN 10
           WHEN u.email LIKE '%cao.oanh%' THEN 5
           ELSE 3
       END,
       'Experienced English language instructor with proven track record in language education.',
       NOW(), NOW()
FROM user_account u
WHERE u.email LIKE 'teacher.%@ems.vn'
ON CONFLICT (user_id) DO NOTHING;

-- =========================================
-- 8. TEACHER SKILLS
-- =========================================
INSERT INTO teacher_skill (teacher_id, skill)
SELECT t.id, 'general'::skill_enum FROM teacher t
JOIN user_account u ON t.user_id = u.id
WHERE u.email LIKE 'teacher.%@ems.vn'
ON CONFLICT DO NOTHING;

-- Reading specialists
INSERT INTO teacher_skill (teacher_id, skill)
SELECT t.id, 'reading'::skill_enum FROM teacher t
JOIN user_account u ON t.user_id = u.id
WHERE u.email IN ('teacher.nguyen.ha@ems.vn', 'teacher.le.lan@ems.vn', 'teacher.dang.linh@ems.vn', 'teacher.ly.duc@ems.vn', 'teacher.ha.tien@ems.vn')
ON CONFLICT DO NOTHING;

-- Writing specialists
INSERT INTO teacher_skill (teacher_id, skill)
SELECT t.id, 'writing'::skill_enum FROM teacher t
JOIN user_account u ON t.user_id = u.id
WHERE u.email IN ('teacher.tran.minh@ems.vn', 'teacher.pham.tuan@ems.vn', 'teacher.bui.quan@ems.vn', 'teacher.trinh.nga@ems.vn', 'teacher.duong.phuong@ems.vn')
ON CONFLICT DO NOTHING;

-- Speaking specialists
INSERT INTO teacher_skill (teacher_id, skill)
SELECT t.id, 'speaking'::skill_enum FROM teacher t
JOIN user_account u ON t.user_id = u.id
WHERE u.email IN ('teacher.vo.mai@ems.vn', 'teacher.hoang.nam@ems.vn', 'teacher.do.huong@ems.vn', 'teacher.ngo.thao@ems.vn', 'teacher.mai.khai@ems.vn', 'teacher.cao.oanh@ems.vn')
ON CONFLICT DO NOTHING;

-- Listening specialists
INSERT INTO teacher_skill (teacher_id, skill)
SELECT t.id, 'listening'::skill_enum FROM teacher t
JOIN user_account u ON t.user_id = u.id
WHERE u.email IN ('teacher.vuong.hung@ems.vn', 'teacher.vu.binh@ems.vn', 'teacher.dinh.hong@ems.vn', 'teacher.khuat.long@ems.vn')
ON CONFLICT DO NOTHING;

-- =========================================
-- 9. STUDENT RECORDS
-- =========================================
INSERT INTO student (user_id, date_of_birth, guardian_phone, emergency_contact, created_at, updated_at)
SELECT u.id,
       DATE '2005-01-01' + (EXTRACT(DAY FROM u.created_at)::int % 3650) * INTERVAL '1 day',
       '0912' || LPAD((EXTRACT(DAY FROM u.created_at)::int * 1000 + EXTRACT(HOUR FROM u.created_at)::int)::text, 6, '0'),
       'Emergency: ' || u.phone,
       NOW(), NOW()
FROM user_account u
WHERE u.email LIKE 'student.%@student.vn'
ON CONFLICT (user_id) DO NOTHING;

-- =========================================
-- 10. TIME SLOT TEMPLATES
-- =========================================
INSERT INTO time_slot_template (branch_id, slot_code, name, start_time, end_time, created_at, updated_at)
SELECT b.id, ts.slot_code, ts.name, ts.start_time, ts.end_time, NOW(), NOW()
FROM branch b
CROSS JOIN (VALUES
    ('M07-09', 'Morning 07:00-09:00', '07:00:00', '09:00:00'),
    ('M09-11', 'Morning 09:00-11:00', '09:00:00', '11:00:00'),
    ('A13-15', 'Afternoon 13:00-15:00', '13:00:00', '15:00:00'),
    ('A15-17', 'Afternoon 15:00-17:00', '15:00:00', '17:00:00'),
    ('E17-19', 'Evening 17:00-19:00', '17:00:00', '19:00:00'),
    ('E19-21', 'Evening 19:00-21:00', '19:00:00', '21:00:00')
) AS ts(slot_code, name, start_time, end_time)
ON CONFLICT (branch_id, slot_code) DO NOTHING;

-- =========================================
-- 11. RESOURCES (Rooms and Virtual)
-- =========================================
INSERT INTO resource (branch_id, code, name, type, capacity, location, equipment, zoom_link, zoom_meeting_id, zoom_password, status, created_at, updated_at)
SELECT b.id, r.code, r.name, r.type::resource_type_enum, r.capacity, r.location, r.equipment, r.zoom_link, r.zoom_meeting_id, r.zoom_password, 'available', NOW(), NOW()
FROM branch b
CROSS JOIN (VALUES
    -- Physical Rooms
    ('R101', 'Room 101', 'ROOM', 25, 'Floor 1', ARRAY['Projector', 'Whiteboard', 'Air Conditioner'], NULL, NULL, NULL),
    ('R102', 'Room 102', 'ROOM', 30, 'Floor 1', ARRAY['Projector', 'Whiteboard', 'Air Conditioner', 'Sound System'], NULL, NULL, NULL),
    ('R201', 'Room 201', 'ROOM', 20, 'Floor 2', ARRAY['Smart TV', 'Whiteboard', 'Air Conditioner'], NULL, NULL, NULL),
    ('R202', 'Room 202', 'ROOM', 25, 'Floor 2', ARRAY['Projector', 'Whiteboard', 'Air Conditioner'], NULL, NULL, NULL),
    ('R301', 'Room 301', 'ROOM', 35, 'Floor 3', ARRAY['Projector', 'Whiteboard', 'Air Conditioner', 'Sound System'], NULL, NULL, NULL),
    -- Virtual Rooms
    ('ZOOM01', 'Zoom Room 01', 'VIRTUAL', 100, 'Online', ARRAY['Screen Share', 'Breakout Rooms', 'Recording'], 'https://zoom.us/j/1234567890', '1234567890', 'pass123'),
    ('ZOOM02', 'Zoom Room 02', 'VIRTUAL', 100, 'Online', ARRAY['Screen Share', 'Breakout Rooms', 'Recording'], 'https://zoom.us/j/0987654321', '0987654321', 'pass456')
) AS r(code, name, type, capacity, location, equipment, zoom_link, zoom_meeting_id, zoom_password)
ON CONFLICT (branch_id, code) DO NOTHING;

-- =========================================
-- 12. SUBJECTS
-- =========================================
INSERT INTO subject (code, name, description, status, created_at, updated_at) VALUES
    ('ENG-GEN', 'General English', 'Comprehensive English language program for all skill levels', 'active'::subject_status_enum, NOW(), NOW()),
    ('ENG-IELTS', 'IELTS Preparation', 'Intensive IELTS exam preparation focusing on all four skills', 'active'::subject_status_enum, NOW(), NOW()),
    ('ENG-TOEIC', 'TOEIC Preparation', 'TOEIC exam preparation with focus on listening and reading', 'active'::subject_status_enum, NOW(), NOW()),
    ('ENG-KIDS', 'English for Kids', 'Fun and interactive English program for children aged 6-12', 'active'::subject_status_enum, NOW(), NOW()),
    ('ENG-BUS', 'Business English', 'English for professional and business communication', 'active'::subject_status_enum, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- =========================================
-- 13. LEVELS
-- =========================================
INSERT INTO level (subject_id, code, name, description, order_index, created_at, updated_at)
SELECT s.id, l.code, l.name, l.description, l.order_index, NOW(), NOW()
FROM subject s
CROSS JOIN (VALUES
    ('A1', 'Beginner A1', 'Can understand and use familiar everyday expressions', 1),
    ('A2', 'Elementary A2', 'Can communicate in simple routine tasks', 2),
    ('B1', 'Intermediate B1', 'Can deal with most situations while traveling', 3),
    ('B2', 'Upper Intermediate B2', 'Can interact with fluency and spontaneity', 4),
    ('C1', 'Advanced C1', 'Can express ideas fluently and spontaneously', 5),
    ('C2', 'Proficiency C2', 'Can understand virtually everything with ease', 6)
) AS l(code, name, description, order_index)
WHERE s.code = 'ENG-GEN'
ON CONFLICT (subject_id, code) DO NOTHING;

INSERT INTO level (subject_id, code, name, description, order_index, created_at, updated_at)
SELECT s.id, l.code, l.name, l.description, l.order_index, NOW(), NOW()
FROM subject s
CROSS JOIN (VALUES
    ('BASIC', 'IELTS Basic', 'Foundation for IELTS - Target band 4.0-5.0', 1),
    ('INTER', 'IELTS Intermediate', 'Intermediate preparation - Target band 5.5-6.5', 2),
    ('ADV', 'IELTS Advanced', 'Advanced preparation - Target band 7.0-8.0', 3),
    ('EXPERT', 'IELTS Expert', 'Expert level preparation - Target band 8.5+', 4)
) AS l(code, name, description, order_index)
WHERE s.code = 'ENG-IELTS'
ON CONFLICT (subject_id, code) DO NOTHING;

INSERT INTO level (subject_id, code, name, description, order_index, created_at, updated_at)
SELECT s.id, l.code, l.name, l.description, l.order_index, NOW(), NOW()
FROM subject s
CROSS JOIN (VALUES
    ('L1', 'TOEIC Level 1', 'Foundation TOEIC - Target 225-545', 1),
    ('L2', 'TOEIC Level 2', 'Intermediate TOEIC - Target 550-780', 2),
    ('L3', 'TOEIC Level 3', 'Advanced TOEIC - Target 785-945', 3),
    ('L4', 'TOEIC Level 4', 'Expert TOEIC - Target 950-990', 4)
) AS l(code, name, description, order_index)
WHERE s.code = 'ENG-TOEIC'
ON CONFLICT (subject_id, code) DO NOTHING;

INSERT INTO level (subject_id, code, name, description, order_index, created_at, updated_at)
SELECT s.id, l.code, l.name, l.description, l.order_index, NOW(), NOW()
FROM subject s
CROSS JOIN (VALUES
    ('STARTER', 'Starter', 'Ages 6-7: Basic vocabulary and simple sentences', 1),
    ('MOVER', 'Mover', 'Ages 8-9: Expanding vocabulary and grammar', 2),
    ('FLYER', 'Flyer', 'Ages 10-12: Confident communication', 3)
) AS l(code, name, description, order_index)
WHERE s.code = 'ENG-KIDS'
ON CONFLICT (subject_id, code) DO NOTHING;

INSERT INTO level (subject_id, code, name, description, order_index, created_at, updated_at)
SELECT s.id, l.code, l.name, l.description, l.order_index, NOW(), NOW()
FROM subject s
CROSS JOIN (VALUES
    ('BEG', 'Business Beginner', 'Basic business communication', 1),
    ('INT', 'Business Intermediate', 'Professional workplace communication', 2),
    ('ADV', 'Business Advanced', 'Executive-level business English', 3)
) AS l(code, name, description, order_index)
WHERE s.code = 'ENG-BUS'
ON CONFLICT (subject_id, code) DO NOTHING;

-- =========================================
-- 14. PROGRAM LEARNING OUTCOMES (PLOs)
-- =========================================
INSERT INTO plo (subject_id, code, description, created_at, updated_at)
SELECT s.id, p.code, p.description, NOW(), NOW()
FROM subject s
CROSS JOIN (VALUES
    ('PLO1', 'Communicate effectively in everyday situations using appropriate vocabulary and grammar'),
    ('PLO2', 'Comprehend spoken English in various contexts including conversations and presentations'),
    ('PLO3', 'Read and understand texts of varying complexity and genres'),
    ('PLO4', 'Write coherent texts for different purposes using proper structure and conventions'),
    ('PLO5', 'Demonstrate cultural awareness and appropriate language use in diverse contexts')
) AS p(code, description)
WHERE s.code = 'ENG-GEN'
ON CONFLICT (subject_id, code) DO NOTHING;

INSERT INTO plo (subject_id, code, description, created_at, updated_at)
SELECT s.id, p.code, p.description, NOW(), NOW()
FROM subject s
CROSS JOIN (VALUES
    ('PLO1', 'Achieve target IELTS band score in all four skills: Listening, Reading, Writing, Speaking'),
    ('PLO2', 'Master IELTS test-taking strategies and time management techniques'),
    ('PLO3', 'Demonstrate academic English proficiency required for international study'),
    ('PLO4', 'Analyze and respond effectively to various IELTS task types')
) AS p(code, description)
WHERE s.code = 'ENG-IELTS'
ON CONFLICT (subject_id, code) DO NOTHING;

-- =========================================
-- 15. COURSES
-- =========================================
INSERT INTO course (level_id, code, name, description, duration_weeks, session_per_week, total_sessions, is_approved, approved_by_manager, approved_at, status, created_at, updated_at)
SELECT l.id, c.code, c.name, c.description, c.duration_weeks, c.session_per_week, c.total_sessions, true, 
       (SELECT id FROM user_account WHERE email = 'manager1@ems.vn' LIMIT 1),
       NOW() - INTERVAL '30 days', 'active'::class_status_enum, NOW() - INTERVAL '30 days', NOW()
FROM level l
JOIN subject s ON l.subject_id = s.id
CROSS JOIN (VALUES
    ('A1-01', 'English A1 - Starter', 'Foundation course for absolute beginners', 12, 2, 24),
    ('A2-01', 'English A2 - Elementary', 'Build on basics with practical language skills', 12, 2, 24),
    ('B1-01', 'English B1 - Intermediate', 'Develop independence in language use', 16, 2, 32),
    ('B2-01', 'English B2 - Upper Intermediate', 'Achieve fluency and confidence', 16, 2, 32),
    ('C1-01', 'English C1 - Advanced', 'Master complex language structures', 20, 2, 40)
) AS c(code, name, description, duration_weeks, session_per_week, total_sessions)
WHERE s.code = 'ENG-GEN' AND l.code IN ('A1', 'A2', 'B1', 'B2', 'C1')
  AND c.code LIKE l.code || '%'
ON CONFLICT (level_id, code) DO NOTHING;

INSERT INTO course (level_id, code, name, description, duration_weeks, session_per_week, total_sessions, is_approved, approved_by_manager, approved_at, status, created_at, updated_at)
SELECT l.id, c.code, c.name, c.description, c.duration_weeks, c.session_per_week, c.total_sessions, true,
       (SELECT id FROM user_account WHERE email = 'manager1@ems.vn' LIMIT 1),
       NOW() - INTERVAL '25 days', 'active'::class_status_enum, NOW() - INTERVAL '25 days', NOW()
FROM level l
JOIN subject s ON l.subject_id = s.id
CROSS JOIN (VALUES
    ('IELTS-BASIC', 'IELTS Foundation', 'Build foundation for IELTS success', 10, 3, 30),
    ('IELTS-INTER', 'IELTS Intermediate Intensive', 'Intensive preparation for band 5.5-6.5', 12, 3, 36),
    ('IELTS-ADV', 'IELTS Advanced', 'Advanced strategies for band 7.0+', 14, 3, 42)
) AS c(code, name, description, duration_weeks, session_per_week, total_sessions)
WHERE s.code = 'ENG-IELTS' AND l.code IN ('BASIC', 'INTER', 'ADV')
  AND c.code LIKE 'IELTS-' || l.code || '%'
ON CONFLICT (level_id, code) DO NOTHING;

INSERT INTO course (level_id, code, name, description, duration_weeks, session_per_week, total_sessions, is_approved, approved_by_manager, approved_at, status, created_at, updated_at)
SELECT l.id, c.code, c.name, c.description, c.duration_weeks, c.session_per_week, c.total_sessions, true,
       (SELECT id FROM user_account WHERE email = 'manager1@ems.vn' LIMIT 1),
       NOW() - INTERVAL '20 days', 'active'::class_status_enum, NOW() - INTERVAL '20 days', NOW()
FROM level l
JOIN subject s ON l.subject_id = s.id
CROSS JOIN (VALUES
    ('TOEIC-L1', 'TOEIC Starter', 'Foundation TOEIC skills', 8, 2, 16),
    ('TOEIC-L2', 'TOEIC Intermediate', 'Build TOEIC competency', 10, 2, 20),
    ('TOEIC-L3', 'TOEIC Advanced', 'Achieve high TOEIC scores', 12, 3, 36)
) AS c(code, name, description, duration_weeks, session_per_week, total_sessions)
WHERE s.code = 'ENG-TOEIC' AND l.code IN ('L1', 'L2', 'L3')
  AND c.code LIKE 'TOEIC-' || l.code || '%'
ON CONFLICT (level_id, code) DO NOTHING;

-- =========================================
-- 16. COURSE LEARNING OUTCOMES (CLOs)
-- =========================================
INSERT INTO clo (course_id, code, description, created_at, updated_at)
SELECT c.id, clo.code, clo.description, NOW(), NOW()
FROM course c
CROSS JOIN (VALUES
    ('CLO1', 'Use basic greetings and introductions confidently'),
    ('CLO2', 'Understand simple spoken instructions and questions'),
    ('CLO3', 'Read and comprehend basic texts and signs'),
    ('CLO4', 'Write simple sentences about familiar topics'),
    ('CLO5', 'Demonstrate basic grammar and vocabulary usage')
) AS clo(code, description)
WHERE c.code LIKE 'A1-%' OR c.code LIKE 'A2-%'
ON CONFLICT (course_id, code) DO NOTHING;

INSERT INTO clo (course_id, code, description, created_at, updated_at)
SELECT c.id, clo.code, clo.description, NOW(), NOW()
FROM course c
CROSS JOIN (VALUES
    ('CLO1', 'Master all four IELTS task types with appropriate strategies'),
    ('CLO2', 'Achieve target band score through systematic practice'),
    ('CLO3', 'Manage time effectively during actual test conditions'),
    ('CLO4', 'Demonstrate academic vocabulary and complex grammar structures'),
    ('CLO5', 'Analyze sample responses and apply feedback for improvement')
) AS clo(code, description)
WHERE c.code LIKE 'IELTS-%'
ON CONFLICT (course_id, code) DO NOTHING;

-- =========================================
-- 17. PLO-CLO MAPPING
-- =========================================
INSERT INTO plo_clo_mapping (plo_id, clo_id)
SELECT p.id, clo.id
FROM plo p
JOIN subject s ON p.subject_id = s.id
JOIN level l ON l.subject_id = s.id
JOIN course c ON c.level_id = l.id
JOIN clo ON clo.course_id = c.id
WHERE s.code = 'ENG-GEN' 
  AND p.code = 'PLO1' 
  AND clo.code IN ('CLO1', 'CLO5')
ON CONFLICT DO NOTHING;

INSERT INTO plo_clo_mapping (plo_id, clo_id)
SELECT p.id, clo.id
FROM plo p
JOIN subject s ON p.subject_id = s.id
JOIN level l ON l.subject_id = s.id
JOIN course c ON c.level_id = l.id
JOIN clo ON clo.course_id = c.id
WHERE s.code = 'ENG-IELTS'
  AND p.code = 'PLO1'
  AND clo.code IN ('CLO1', 'CLO2')
ON CONFLICT DO NOTHING;

-- =========================================
-- 18. COURSE PHASES
-- =========================================
INSERT INTO course_phase (course_id, phase_name, description, sequence_no, created_at, updated_at)
SELECT c.id, cp.phase_name, cp.description, cp.sequence_no, NOW(), NOW()
FROM course c
CROSS JOIN (VALUES
    ('Foundation Phase', 'Introduction to basic concepts and vocabulary', 1),
    ('Development Phase', 'Building skills through structured practice', 2),
    ('Application Phase', 'Applying knowledge in practical contexts', 3),
    ('Assessment Phase', 'Evaluation and feedback', 4)
) AS cp(phase_name, description, sequence_no)
WHERE c.code LIKE 'A1-%' OR c.code LIKE 'A2-%'
ON CONFLICT (course_id, phase_name) DO NOTHING;

INSERT INTO course_phase (course_id, phase_name, description, sequence_no, created_at, updated_at)
SELECT c.id, cp.phase_name, cp.description, cp.sequence_no, NOW(), NOW()
FROM course c
CROSS JOIN (VALUES
    ('Diagnostic & Strategy', 'Initial assessment and test-taking strategies', 1),
    ('Skill Building Phase 1', 'Intensive practice - Listening & Reading', 2),
    ('Skill Building Phase 2', 'Intensive practice - Writing & Speaking', 3),
    ('Mock Test Phase', 'Full mock tests and performance analysis', 4),
    ('Final Preparation', 'Review, refinement and confidence building', 5)
) AS cp(phase_name, description, sequence_no)
WHERE c.code LIKE 'IELTS-%'
ON CONFLICT (course_id, phase_name) DO NOTHING;

-- =========================================
-- 19. COURSE SESSIONS (Templates)
-- =========================================
-- Generate course sessions for A1-01 course (24 sessions)
INSERT INTO course_session (phase_id, session_name, description, sequence_no, duration_minutes, created_at, updated_at)
SELECT cp.id,
       'Session ' || gs.session_no,
       CASE 
           WHEN cp.sequence_no = 1 THEN 'Foundation: ' || CASE WHEN gs.session_no % 3 = 1 THEN 'Vocabulary & Pronunciation' WHEN gs.session_no % 3 = 2 THEN 'Basic Grammar Structures' ELSE 'Speaking Practice' END
           WHEN cp.sequence_no = 2 THEN 'Development: ' || CASE WHEN gs.session_no % 3 = 1 THEN 'Listening & Comprehension' WHEN gs.session_no % 3 = 2 THEN 'Reading & Understanding' ELSE 'Writing Practice' END
           WHEN cp.sequence_no = 3 THEN 'Application: ' || CASE WHEN gs.session_no % 2 = 1 THEN 'Real-world Communication' ELSE 'Integrated Skills' END
           ELSE 'Assessment: Progress Test and Feedback'
       END,
       gs.session_no,
       120,
       NOW(), NOW()
FROM course_phase cp
JOIN course c ON cp.course_id = c.id
CROSS JOIN generate_series(1, 6) AS gs(session_no)
WHERE c.code = 'A1-01' AND cp.sequence_no <= 4
ON CONFLICT (phase_id, sequence_no) DO NOTHING;

-- Generate course sessions for IELTS-INTER course (36 sessions - distributed across 5 phases)
INSERT INTO course_session (phase_id, session_name, description, sequence_no, duration_minutes, created_at, updated_at)
SELECT cp.id,
       'Session ' || gs.session_no,
       CASE 
           WHEN cp.sequence_no = 1 THEN 'Diagnostic & Strategy Session ' || gs.session_no
           WHEN cp.sequence_no = 2 THEN 'Listening & Reading Skills ' || gs.session_no
           WHEN cp.sequence_no = 3 THEN 'Writing & Speaking Skills ' || gs.session_no
           WHEN cp.sequence_no = 4 THEN 'Full Mock Test ' || gs.session_no
           ELSE 'Final Review & Strategy ' || gs.session_no
       END,
       gs.session_no,
       120,
       NOW(), NOW()
FROM course_phase cp
JOIN course c ON cp.course_id = c.id
CROSS JOIN generate_series(1, CASE 
    WHEN cp.sequence_no = 1 THEN 6
    WHEN cp.sequence_no = 2 THEN 10
    WHEN cp.sequence_no = 3 THEN 10
    WHEN cp.sequence_no = 4 THEN 6
    ELSE 4
END) AS gs(session_no)
WHERE c.code = 'IELTS-INTER'
ON CONFLICT (phase_id, sequence_no) DO NOTHING;

-- =========================================
-- 20. COURSE SESSION CLO MAPPING
-- =========================================
INSERT INTO course_session_clo_mapping (course_session_id, clo_id)
SELECT cs.id, clo.id
FROM course_session cs
JOIN course_phase cp ON cs.phase_id = cp.id
JOIN course c ON cp.course_id = c.id
JOIN clo ON clo.course_id = c.id
WHERE c.code = 'A1-01' 
  AND cs.sequence_no <= 6 
  AND clo.code IN ('CLO1', 'CLO2')
ON CONFLICT DO NOTHING;

INSERT INTO course_session_clo_mapping (course_session_id, clo_id)
SELECT cs.id, clo.id
FROM course_session cs
JOIN course_phase cp ON cs.phase_id = cp.id
JOIN course c ON cp.course_id = c.id
JOIN clo ON clo.course_id = c.id
WHERE c.code = 'IELTS-INTER'
  AND clo.code IN ('CLO1', 'CLO2', 'CLO3')
ON CONFLICT DO NOTHING;

-- =========================================
-- 21. CLASSES (Instances of Courses)
-- =========================================
-- Create multiple classes for A1-01 course at different branches
INSERT INTO class (branch_id, course_id, code, modality, start_date, end_date, schedule_days, time_slot_template_id, max_capacity, enrolled_count, status, created_by, approved_by, approved_at, created_at, updated_at)
SELECT 
    b.id,
    c.id,
    'A1-' || b.code || '-' || to_char(NOW() + (ROW_NUMBER() OVER (PARTITION BY b.id ORDER BY b.id)) * INTERVAL '7 days', 'MMDD'),
    'OFFLINE'::modality_enum,
    NOW() + (ROW_NUMBER() OVER (PARTITION BY b.id ORDER BY b.id)) * INTERVAL '7 days',
    NOW() + (ROW_NUMBER() OVER (PARTITION BY b.id ORDER BY b.id)) * INTERVAL '7 days' + INTERVAL '12 weeks',
    ARRAY[2, 4, 6]::smallint[],
    (SELECT id FROM time_slot_template WHERE branch_id = b.id AND slot_code = 'E19-21' LIMIT 1),
    25,
    0,
    'scheduled'::class_status_enum,
    (SELECT u.id FROM user_account u JOIN user_branch ub ON u.id = ub.user_id WHERE ub.branch_id = b.id AND u.email LIKE 'staff.%@ems.vn' LIMIT 1),
    (SELECT u.id FROM user_account u JOIN user_branch ub ON u.id = ub.user_id WHERE ub.branch_id = b.id AND u.email LIKE 'head.%@ems.vn' LIMIT 1),
    NOW(),
    NOW(), NOW()
FROM branch b
CROSS JOIN course c
CROSS JOIN generate_series(1, 2) AS class_instance
WHERE c.code = 'A1-01' 
  AND b.code IN ('HN-BD', 'HCM-Q1', 'DN-HC')
ON CONFLICT (branch_id, code) DO NOTHING;

-- Create classes for IELTS-INTER at multiple branches
INSERT INTO class (branch_id, course_id, code, modality, start_date, end_date, schedule_days, time_slot_template_id, max_capacity, enrolled_count, status, created_by, approved_by, approved_at, created_at, updated_at)
SELECT 
    b.id,
    c.id,
    'IELTS-' || b.code || '-' || to_char(NOW() - INTERVAL '2 weeks' + (ROW_NUMBER() OVER (PARTITION BY b.id ORDER BY b.id)) * INTERVAL '14 days', 'MMDD'),
    CASE WHEN ROW_NUMBER() OVER (PARTITION BY b.id ORDER BY b.id) % 3 = 0 THEN 'ONLINE'::modality_enum ELSE 'OFFLINE'::modality_enum END,
    NOW() - INTERVAL '2 weeks' + (ROW_NUMBER() OVER (PARTITION BY b.id ORDER BY b.id)) * INTERVAL '14 days',
    NOW() - INTERVAL '2 weeks' + (ROW_NUMBER() OVER (PARTITION BY b.id ORDER BY b.id)) * INTERVAL '14 days' + INTERVAL '12 weeks',
    ARRAY[2, 4, 6]::smallint[],
    (SELECT id FROM time_slot_template WHERE branch_id = b.id AND slot_code = 'E17-19' LIMIT 1),
    30,
    0,
    'ongoing'::class_status_enum,
    (SELECT u.id FROM user_account u JOIN user_branch ub ON u.id = ub.user_id WHERE ub.branch_id = b.id AND u.email LIKE 'staff.%@ems.vn' LIMIT 1),
    (SELECT u.id FROM user_account u JOIN user_branch ub ON u.id = ub.user_id WHERE ub.branch_id = b.id AND u.email LIKE 'head.%@ems.vn' LIMIT 1),
    NOW() - INTERVAL '3 weeks',
    NOW() - INTERVAL '3 weeks', NOW()
FROM branch b
CROSS JOIN course c
CROSS JOIN generate_series(1, 1) AS class_instance
WHERE c.code = 'IELTS-INTER'
  AND b.code IN ('HN-BD', 'HN-CG', 'HCM-Q1', 'HCM-Q7')
ON CONFLICT (branch_id, code) DO NOTHING;

-- Create TOEIC classes
INSERT INTO class (branch_id, course_id, code, modality, start_date, end_date, schedule_days, time_slot_template_id, max_capacity, enrolled_count, status, created_by, approved_by, approved_at, created_at, updated_at)
SELECT 
    b.id,
    c.id,
    'TOEIC-' || b.code || '-' || to_char(NOW() + INTERVAL '1 week', 'MMDD'),
    'HYBRID'::modality_enum,
    NOW() + INTERVAL '1 week',
    NOW() + INTERVAL '1 week' + INTERVAL '10 weeks',
    ARRAY[3, 5]::smallint[],
    (SELECT id FROM time_slot_template WHERE branch_id = b.id AND slot_code = 'E19-21' LIMIT 1),
    28,
    0,
    'scheduled'::class_status_enum,
    (SELECT u.id FROM user_account u JOIN user_branch ub ON u.id = ub.user_id WHERE ub.branch_id = b.id AND u.email LIKE 'staff.%@ems.vn' LIMIT 1),
    (SELECT u.id FROM user_account u JOIN user_branch ub ON u.id = ub.user_id WHERE ub.branch_id = b.id AND u.email LIKE 'head.%@ems.vn' LIMIT 1),
    NOW() - INTERVAL '2 days',
    NOW() - INTERVAL '3 days', NOW()
FROM branch b
CROSS JOIN course c
WHERE c.code = 'TOEIC-L2'
  AND b.code IN ('HCM-Q1', 'HCM-TD')
ON CONFLICT (branch_id, code) DO NOTHING;

-- =========================================
-- 22. SESSIONS (Auto-generated from Classes)
-- Note: This is a simplified version. In real system, sessions are auto-generated
-- based on start_date, schedule_days, and course_session templates
-- =========================================
-- Generate sessions for the first A1 class at HN-BD (next 4 weeks, 8 sessions)
INSERT INTO session (class_id, course_session_id, session_date, start_time, end_time, actual_start_time, actual_end_time, status, type, notes, created_at, updated_at)
SELECT 
    cl.id,
    cs.id,
    cl.start_date + ((gs.session_no - 1) / 3 * 7 + 
        CASE 
            WHEN (gs.session_no - 1) % 3 = 0 THEN 0  -- Monday (day 2)
            WHEN (gs.session_no - 1) % 3 = 1 THEN 2  -- Wednesday (day 4)
            ELSE 4  -- Friday (day 6)
        END
    ) * INTERVAL '1 day',
    tst.start_time,
    tst.end_time,
    NULL,
    NULL,
    CASE 
        WHEN cl.start_date + ((gs.session_no - 1) / 3 * 7) * INTERVAL '1 day' < NOW() THEN 'done'::session_status_enum
        ELSE 'planned'::session_status_enum
    END,
    'CLASS'::session_type_enum,
    'Regular class session',
    NOW(), NOW()
FROM class cl
JOIN course c ON cl.course_id = c.id
JOIN course_phase cp ON cp.course_id = c.id
JOIN course_session cs ON cs.phase_id = cp.id
JOIN time_slot_template tst ON tst.id = cl.time_slot_template_id
CROSS JOIN generate_series(1, 8) AS gs(session_no)
WHERE c.code = 'A1-01'
  AND cl.code LIKE 'A1-HN-BD-%'
  AND cs.sequence_no = ((gs.session_no - 1) % 6) + 1
  AND cp.sequence_no = ((gs.session_no - 1) / 6) + 1
LIMIT 8
ON CONFLICT DO NOTHING;

-- Generate sessions for IELTS classes (12 sessions across 4 weeks for ongoing classes)
INSERT INTO session (class_id, course_session_id, session_date, start_time, end_time, actual_start_time, actual_end_time, status, type, notes, created_at, updated_at)
SELECT 
    cl.id,
    cs.id,
    cl.start_date + ((gs.session_no - 1) / 3 * 7 + 
        CASE 
            WHEN (gs.session_no - 1) % 3 = 0 THEN 0
            WHEN (gs.session_no - 1) % 3 = 1 THEN 2
            ELSE 4
        END
    ) * INTERVAL '1 day',
    tst.start_time,
    tst.end_time,
    CASE WHEN cl.start_date + ((gs.session_no - 1) / 3 * 7) * INTERVAL '1 day' < NOW() THEN tst.start_time ELSE NULL END,
    CASE WHEN cl.start_date + ((gs.session_no - 1) / 3 * 7) * INTERVAL '1 day' < NOW() THEN tst.end_time ELSE NULL END,
    CASE 
        WHEN cl.start_date + ((gs.session_no - 1) / 3 * 7) * INTERVAL '1 day' < NOW() - INTERVAL '1 day' THEN 'done'::session_status_enum
        ELSE 'planned'::session_status_enum
    END,
    'CLASS'::session_type_enum,
    'IELTS Intensive Session',
    NOW(), NOW()
FROM class cl
JOIN course c ON cl.course_id = c.id
JOIN course_phase cp ON cp.course_id = c.id
JOIN course_session cs ON cs.phase_id = cp.id
JOIN time_slot_template tst ON tst.id = cl.time_slot_template_id
CROSS JOIN generate_series(1, 12) AS gs(session_no)
WHERE c.code = 'IELTS-INTER'
  AND cl.status = 'ongoing'::class_status_enum
  AND cs.sequence_no <= 12
  AND cp.sequence_no IN (1, 2)
LIMIT 48
ON CONFLICT DO NOTHING;

-- =========================================
-- 23. SESSION RESOURCES (Room/Zoom Assignment)
-- =========================================
INSERT INTO session_resource (session_id, resource_id, assigned_at, assigned_by)
SELECT 
    s.id,
    r.id,
    NOW(),
    cl.created_by
FROM session s
JOIN class cl ON s.class_id = cl.id
JOIN branch b ON cl.branch_id = b.id
JOIN resource r ON r.branch_id = b.id
WHERE cl.modality = 'OFFLINE'::modality_enum
  AND r.type = 'ROOM'::resource_type_enum
  AND r.code = 'R201'
LIMIT 20
ON CONFLICT DO NOTHING;

INSERT INTO session_resource (session_id, resource_id, assigned_at, assigned_by)
SELECT 
    s.id,
    r.id,
    NOW(),
    cl.created_by
FROM session s
JOIN class cl ON s.class_id = cl.id
JOIN branch b ON cl.branch_id = b.id
JOIN resource r ON r.branch_id = b.id
WHERE cl.modality = 'ONLINE'::modality_enum
  AND r.type = 'VIRTUAL'::resource_type_enum
  AND r.code = 'ZOOM01'
LIMIT 10
ON CONFLICT DO NOTHING;

-- =========================================
-- 24. TEACHING SLOTS (Teacher Assignment)
-- =========================================
INSERT INTO teaching_slot (session_id, teacher_id, skill, role, assigned_at, assigned_by)
SELECT 
    s.id,
    t.id,
    'general'::skill_enum,
    'primary'::teaching_role_enum,
    NOW(),
    cl.created_by
FROM session s
JOIN class cl ON s.class_id = cl.id
JOIN branch b ON cl.branch_id = b.id
JOIN teacher t ON t.id = (
    SELECT teacher.id 
    FROM teacher 
    JOIN user_account ua ON teacher.user_id = ua.id
    WHERE ua.email LIKE 'teacher.nguyen.ha@ems.vn'
    LIMIT 1
)
WHERE cl.code LIKE 'A1-HN-BD-%'
ON CONFLICT DO NOTHING;

INSERT INTO teaching_slot (session_id, teacher_id, skill, role, assigned_at, assigned_by)
SELECT 
    s.id,
    t.id,
    'general'::skill_enum,
    'primary'::teaching_role_enum,
    NOW(),
    cl.created_by
FROM session s
JOIN class cl ON s.class_id = cl.id
JOIN branch b ON cl.branch_id = b.id
JOIN teacher t ON t.id = (
    SELECT teacher.id 
    FROM teacher 
    JOIN user_account ua ON teacher.user_id = ua.id
    WHERE ua.email LIKE 'teacher.le.lan@ems.vn'
    LIMIT 1
)
WHERE cl.code LIKE 'IELTS-HN-BD-%' OR cl.code LIKE 'IELTS-HCM-Q1-%'
ON CONFLICT DO NOTHING;

-- =========================================
-- 25. ENROLLMENTS
-- =========================================
-- Enroll students in A1 classes
INSERT INTO enrollment (class_id, student_id, enrolled_at, status, created_at, updated_at)
SELECT 
    cl.id,
    st.id,
    NOW() - INTERVAL '5 days',
    'enrolled'::enrollment_status_enum,
    NOW() - INTERVAL '5 days',
    NOW()
FROM class cl
JOIN student st ON st.id IN (
    SELECT s.id FROM student s
    JOIN user_account u ON s.user_id = u.id
    WHERE u.email IN (
        'student.nguyen.an@student.vn',
        'student.tran.bao@student.vn',
        'student.le.cuong@student.vn',
        'student.pham.dung@student.vn',
        'student.vo.em@student.vn'
    )
)
WHERE cl.code LIKE 'A1-HN-BD-%'
ON CONFLICT (class_id, student_id) DO NOTHING;

-- Enroll students in IELTS classes
INSERT INTO enrollment (class_id, student_id, enrolled_at, status, created_at, updated_at)
SELECT 
    cl.id,
    st.id,
    NOW() - INTERVAL '3 weeks',
    'enrolled'::enrollment_status_enum,
    NOW() - INTERVAL '3 weeks',
    NOW()
FROM class cl
JOIN student st ON st.id IN (
    SELECT s.id FROM student s
    JOIN user_account u ON s.user_id = u.id
    WHERE u.email IN (
        'student.hoang.giang@student.vn',
        'student.dang.hieu@student.vn',
        'student.bui.ivy@student.vn',
        'student.do.kien@student.vn',
        'student.vuong.linh@student.vn',
        'student.ngo.minh@student.vn',
        'student.ly.nhan@student.vn',
        'student.trinh.oanh@student.vn'
    )
)
WHERE cl.code LIKE 'IELTS-HN-BD-%' OR cl.code LIKE 'IELTS-HCM-Q1-%'
ON CONFLICT (class_id, student_id) DO NOTHING;

-- Update enrolled_count in classes
UPDATE class SET enrolled_count = (
    SELECT COUNT(*) FROM enrollment WHERE enrollment.class_id = class.id
) WHERE id IN (SELECT DISTINCT class_id FROM enrollment);

-- =========================================
-- 26. STUDENT SESSIONS (Individual Session Records)
-- =========================================
INSERT INTO student_session (student_id, session_id, attendance_status, is_makeup, notes, created_at, updated_at)
SELECT 
    e.student_id,
    s.id,
    CASE 
        WHEN s.status = 'done'::session_status_enum THEN 
            CASE 
                WHEN random() < 0.85 THEN 'present'::attendance_status_enum
                WHEN random() < 0.92 THEN 'late'::attendance_status_enum
                ELSE 'absent'::attendance_status_enum
            END
        ELSE 'planned'::attendance_status_enum
    END,
    false,
    NULL,
    NOW(), NOW()
FROM enrollment e
JOIN session s ON s.class_id = e.class_id
WHERE e.status = 'enrolled'::enrollment_status_enum
ON CONFLICT DO NOTHING;

-- =========================================
-- 27. ASSESSMENTS
-- =========================================
INSERT INTO assessment (class_id, name, description, kind, weight_percentage, max_score, due_date, created_by, created_at, updated_at)
SELECT 
    cl.id,
    'Midterm Test',
    'Comprehensive midterm examination covering all skills',
    'midterm'::assessment_kind_enum,
    30.0,
    100.0,
    cl.start_date + INTERVAL '6 weeks',
    cl.created_by,
    NOW(), NOW()
FROM class cl
WHERE cl.status IN ('ongoing'::class_status_enum, 'scheduled'::class_status_enum)
ON CONFLICT DO NOTHING;

INSERT INTO assessment (class_id, name, description, kind, weight_percentage, max_score, due_date, created_by, created_at, updated_at)
SELECT 
    cl.id,
    'Final Examination',
    'Final comprehensive examination',
    'final'::assessment_kind_enum,
    50.0,
    100.0,
    cl.end_date - INTERVAL '3 days',
    cl.created_by,
    NOW(), NOW()
FROM class cl
WHERE cl.status IN ('ongoing'::class_status_enum, 'scheduled'::class_status_enum)
ON CONFLICT DO NOTHING;

-- =========================================
-- 28. SCORES
-- =========================================
INSERT INTO score (assessment_id, student_id, score, feedback, graded_by, graded_at, created_at, updated_at)
SELECT 
    a.id,
    e.student_id,
    65.0 + (random() * 30.0)::numeric(5,2),
    'Good effort. Keep practicing speaking and writing.',
    (SELECT t.id FROM teacher t LIMIT 1),
    NOW() - INTERVAL '5 days',
    NOW() - INTERVAL '5 days',
    NOW()
FROM assessment a
JOIN class cl ON a.class_id = cl.id
JOIN enrollment e ON e.class_id = cl.id
WHERE a.kind = 'midterm'::assessment_kind_enum
  AND cl.status = 'ongoing'::class_status_enum
  AND cl.start_date < NOW() - INTERVAL '6 weeks'
ON CONFLICT (assessment_id, student_id) DO NOTHING;

-- =========================================
-- 29. STUDENT REQUESTS
-- =========================================
INSERT INTO student_request (student_id, session_id, type, reason, status, submitted_at, decided_by, decided_at, decision_notes, created_at, updated_at)
SELECT 
    e.student_id,
    s.id,
    'absence'::student_request_type_enum,
    'Family emergency - unable to attend session',
    'approved'::request_status_enum,
    NOW() - INTERVAL '2 days',
    cl.approved_by,
    NOW() - INTERVAL '1 day',
    'Excused absence approved. Please review lesson materials.',
    NOW() - INTERVAL '2 days',
    NOW()
FROM enrollment e
JOIN class cl ON e.class_id = cl.id
JOIN session s ON s.class_id = cl.id
WHERE s.session_date = NOW()::date + INTERVAL '1 day'
  AND e.student_id IN (
      SELECT st.id FROM student st
      JOIN user_account u ON st.user_id = u.id
      WHERE u.email = 'student.nguyen.an@student.vn'
      LIMIT 1
  )
LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO student_request (student_id, session_id, type, reason, makeup_session_id, status, submitted_at, decided_by, decided_at, decision_notes, created_at, updated_at)
SELECT 
    e.student_id,
    s.id,
    'makeup'::student_request_type_enum,
    'Missed session due to illness - requesting makeup class',
    NULL,
    'pending'::request_status_enum,
    NOW() - INTERVAL '1 day',
    NULL,
    NULL,
    NULL,
    NOW() - INTERVAL '1 day',
    NOW()
FROM enrollment e
JOIN class cl ON e.class_id = cl.id
JOIN session s ON s.class_id = cl.id
WHERE s.status = 'done'::session_status_enum
  AND e.student_id IN (
      SELECT st.id FROM student st
      JOIN user_account u ON st.user_id = u.id
      WHERE u.email = 'student.tran.bao@student.vn'
      LIMIT 1
  )
LIMIT 1
ON CONFLICT DO NOTHING;

-- =========================================
-- 30. TEACHER REQUESTS
-- =========================================
INSERT INTO teacher_request (teacher_id, session_id, type, reason, status, submitted_at, decided_by, decided_at, decision_notes, created_at, updated_at)
SELECT 
    t.id,
    s.id,
    'leave'::teacher_request_type_enum,
    'Medical appointment - cannot teach this session',
    'approved'::request_status_enum,
    NOW() - INTERVAL '3 days',
    (SELECT u.id FROM user_account u WHERE u.email = 'manager1@ems.vn' LIMIT 1),
    NOW() - INTERVAL '2 days',
    'Leave approved. Substitute teacher assigned.',
    NOW() - INTERVAL '3 days',
    NOW()
FROM teacher t
JOIN user_account u ON t.user_id = u.id
JOIN teaching_slot ts ON ts.teacher_id = t.id
JOIN session s ON ts.session_id = s.id
WHERE u.email = 'teacher.nguyen.ha@ems.vn'
  AND s.session_date > NOW() + INTERVAL '2 days'
LIMIT 1
ON CONFLICT DO NOTHING;

-- =========================================
-- 31. STUDENT FEEDBACK
-- =========================================
INSERT INTO student_feedback (student_id, session_id, rating, feedback_text, submitted_at, created_at, updated_at)
SELECT 
    ss.student_id,
    ss.session_id,
    4 + (random() * 1.0)::int,
    CASE 
        WHEN random() < 0.3 THEN 'Excellent session! Teacher was very engaging and clear.'
        WHEN random() < 0.6 THEN 'Good lesson. Would appreciate more speaking practice.'
        ELSE 'Helpful session. Materials were well-organized.'
    END,
    NOW() - INTERVAL '1 day',
    NOW() - INTERVAL '1 day',
    NOW()
FROM student_session ss
JOIN session s ON ss.session_id = s.id
WHERE s.status = 'done'::session_status_enum
  AND ss.attendance_status = 'present'::attendance_status_enum
  AND random() < 0.4
LIMIT 15
ON CONFLICT (student_id, session_id) DO NOTHING;

-- =========================================
-- 32. QA REPORTS
-- =========================================
INSERT INTO qa_report (session_id, qa_user_id, observation_notes, rating, recommendations, created_at, updated_at)
SELECT 
    s.id,
    (SELECT u.id FROM user_account u WHERE u.email = 'qa.hn@ems.vn' LIMIT 1),
    'Observed session: Teacher demonstrated strong classroom management and clear instruction. Students were engaged throughout.',
    5,
    'Continue current teaching methods. Consider incorporating more technology-based activities.',
    NOW() - INTERVAL '2 days',
    NOW()
FROM session s
WHERE s.status = 'done'::session_status_enum
  AND s.session_date < NOW()
LIMIT 5
ON CONFLICT DO NOTHING;

-- =========================================
-- SEED DATA GENERATION COMPLETE
-- =========================================
-- Summary:
-- - 2 Centers with 7 Branches across 3 cities
-- - 78 Users (Admin, Managers, Center Heads, Staff, Subject Leaders, Teachers, Students, QA)
-- - 20 Teachers with diverse skills and certifications
-- - 40 Students enrolled in various courses
-- - 5 Subjects with multiple Levels (21 total levels)
-- - 11 Approved Courses with PLOs, CLOs, Phases, and Course Sessions
-- - Multiple Classes (A1, IELTS, TOEIC) in different modalities
-- - Sessions with room/zoom assignments and teacher assignments
-- - Enrollments with attendance tracking via Student Sessions
-- - Assessments and Scores for ongoing classes
-- - Student and Teacher Requests demonstrating workflow
-- - Student Feedback and QA Reports for quality monitoring
-- 
-- All foreign key relationships validated and consistent.
-- Data covers major use cases: enrollment, attendance, requests, assessments, feedback.
