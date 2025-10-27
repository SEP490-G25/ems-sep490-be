-- =========================================
-- EMS-SEP490-BE: Seed Data Script
-- PostgreSQL 16 Test Data
-- =========================================
--
-- PURPOSE:
-- This script populates the EMS database with realistic test data for development and testing.
-- It creates a complete education center ecosystem including courses, classes, teachers, students,
-- sessions, enrollments, and various operational scenarios.
--
-- PREREQUISITES:
-- - Database schema must be created first (run database-schema.sql)
-- - All enum types must exist
-- - All tables must be created
--
-- EXECUTION:
-- psql -U postgres -d ems -f seed-data.sql
--
-- DATA OVERVIEW:
-- - 1 Center with 1 Branch (Main Center)
-- - Multiple subjects (English, Japanese)
-- - Multiple levels per subject (A1-C2, N5-N1)
-- - 10+ diverse courses (IELTS, TOEIC, JLPT, Business, Conversational)
-- - 15+ teachers with various skills
-- - 50+ students enrolled in different classes
-- - Multiple classes in different statuses (draft, scheduled, ongoing, completed)
-- - Realistic scenarios: attendance, absences, makeups, transfers, teacher substitutions
-- =========================================

-- =========================================
-- SECTION 1: ORGANIZATION & INFRASTRUCTURE
-- =========================================

-- 1.1 Center
INSERT INTO center (id, code, name, description, phone, email, created_at, updated_at)
VALUES
(1, 'ELC-HN', 'English Language Center Hanoi', 'Premier language training center in Hanoi offering English and Japanese courses', '+84-24-3123-4567', 'info@elc-hanoi.edu.vn', NOW(), NOW());

-- 1.2 Branch
INSERT INTO branch (id, center_id, code, name, address, location, phone, capacity, status, opening_date, created_at, updated_at)
VALUES
(1, 1, 'HN-MAIN', 'Main Campus', '123 Nguyen Trai Street, Thanh Xuan District', 'Hanoi', '+84-24-3123-4567', 500, 'active', '2020-01-15', NOW(), NOW());

-- Reset sequences
SELECT setval('center_id_seq', (SELECT MAX(id) FROM center));
SELECT setval('branch_id_seq', (SELECT MAX(id) FROM branch));

-- =========================================
-- SECTION 2: ROLES & USER ACCOUNTS
-- =========================================

-- 2.1 Roles (8 system roles)
INSERT INTO role (id, code, name)
VALUES
(1, 'ADMIN', 'System Administrator'),
(2, 'MANAGER', 'Operations Manager'),
(3, 'CENTER_HEAD', 'Branch Director'),
(4, 'ACADEMIC_STAFF', 'Academic Staff (Giáo vụ)'),
(5, 'SUBJECT_LEADER', 'Subject Leader'),
(6, 'TEACHER', 'Teacher'),
(7, 'STUDENT', 'Student'),
(8, 'QA', 'Quality Assurance');

-- 2.2 User Accounts
-- Note: Password hash is bcrypt for "password123" - in production use secure passwords!
-- Admin & Management (IDs: 1-5)
INSERT INTO user_account (id, email, phone, facebook_url, full_name, dob, address, password_hash, status, last_login_at, created_at, updated_at)
VALUES
(1, 'admin@elc-hanoi.edu.vn', '+84-901-111-111', NULL, 'Nguyen Van Admin', '1985-03-15', '10 Tran Hung Dao, Hoan Kiem, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '365 days', NOW()),
(2, 'manager@elc-hanoi.edu.vn', '+84-901-222-222', NULL, 'Tran Thi Manager', '1982-07-22', '25 Hang Bong, Hoan Kiem, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '5 hours', NOW() - INTERVAL '300 days', NOW()),
(3, 'centerhead@elc-hanoi.edu.vn', '+84-901-333-333', NULL, 'Le Van Center Head', '1980-11-08', '50 Nguyen Chi Thanh, Dong Da, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '1 hour', NOW() - INTERVAL '200 days', NOW()),
(4, 'academic1@elc-hanoi.edu.vn', '+84-901-444-444', NULL, 'Pham Thi Academic', '1988-05-12', '88 Lang Ha, Ba Dinh, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '3 hours', NOW() - INTERVAL '180 days', NOW()),
(5, 'academic2@elc-hanoi.edu.vn', '+84-901-555-555', NULL, 'Hoang Van Academic', '1990-09-25', '123 Giang Vo, Ba Dinh, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '4 hours', NOW() - INTERVAL '180 days', NOW());

-- Subject Leaders (IDs: 6-8)
INSERT INTO user_account (id, email, phone, facebook_url, full_name, dob, address, password_hash, status, last_login_at, created_at, updated_at)
VALUES
(6, 'english.leader@elc-hanoi.edu.vn', '+84-902-111-111', NULL, 'Nguyen Thi English Leader', '1983-04-18', '45 Tran Phu, Ba Dinh, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '6 hours', NOW() - INTERVAL '250 days', NOW()),
(7, 'japanese.leader@elc-hanoi.edu.vn', '+84-902-222-222', NULL, 'Tanaka Yuki', '1981-12-05', '77 Kim Ma, Ba Dinh, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '8 hours', NOW() - INTERVAL '220 days', NOW()),
(8, 'qa.staff@elc-hanoi.edu.vn', '+84-902-333-333', NULL, 'Vo Thi QA', '1987-06-30', '99 Nguyen Thai Hoc, Ba Dinh, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '10 hours', NOW() - INTERVAL '150 days', NOW());

-- Teachers (IDs: 9-25) - 17 teachers with diverse names
INSERT INTO user_account (id, email, phone, facebook_url, full_name, dob, address, password_hash, status, last_login_at, created_at, updated_at)
VALUES
-- English Teachers
(9, 'teacher.john@elc-hanoi.edu.vn', '+84-903-111-111', 'https://facebook.com/john.smith', 'John Smith', '1985-08-14', '12 Tay Ho, Tay Ho, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '1 day', NOW() - INTERVAL '200 days', NOW()),
(10, 'teacher.sarah@elc-hanoi.edu.vn', '+84-903-222-222', 'https://facebook.com/sarah.johnson', 'Sarah Johnson', '1988-02-28', '34 Xuan Dieu, Tay Ho, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '12 hours', NOW() - INTERVAL '190 days', NOW()),
(11, 'teacher.michael@elc-hanoi.edu.vn', '+84-903-333-333', NULL, 'Michael Brown', '1982-11-19', '56 Au Co, Tay Ho, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '8 hours', NOW() - INTERVAL '185 days', NOW()),
(12, 'teacher.emily@elc-hanoi.edu.vn', '+84-903-444-444', NULL, 'Emily Davis', '1990-05-07', '78 Yen Phu, Tay Ho, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '2 days', NOW() - INTERVAL '180 days', NOW()),
(13, 'teacher.linh@elc-hanoi.edu.vn', '+84-903-555-555', NULL, 'Nguyen Thi Linh', '1991-09-13', '11 Cau Giay, Cau Giay, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '5 hours', NOW() - INTERVAL '175 days', NOW()),
(14, 'teacher.huy@elc-hanoi.edu.vn', '+84-903-666-666', NULL, 'Tran Van Huy', '1986-03-21', '22 Duy Tan, Cau Giay, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '15 hours', NOW() - INTERVAL '170 days', NOW()),
(15, 'teacher.mai@elc-hanoi.edu.vn', '+84-903-777-777', NULL, 'Le Thi Mai', '1989-07-16', '33 Tran Thai Tong, Cau Giay, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '10 hours', NOW() - INTERVAL '165 days', NOW()),
(16, 'teacher.david@elc-hanoi.edu.vn', '+84-903-888-888', NULL, 'David Wilson', '1984-12-03', '44 Xuan Thuy, Cau Giay, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '1 day', NOW() - INTERVAL '160 days', NOW()),
-- Japanese Teachers
(17, 'teacher.yuki@elc-hanoi.edu.vn', '+84-904-111-111', NULL, 'Yamamoto Yuki', '1987-04-25', '55 Linh Lang, Ba Dinh, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '6 hours', NOW() - INTERVAL '155 days', NOW()),
(18, 'teacher.sakura@elc-hanoi.edu.vn', '+84-904-222-222', NULL, 'Sato Sakura', '1990-10-11', '66 Lieu Giai, Ba Dinh, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '18 hours', NOW() - INTERVAL '150 days', NOW()),
(19, 'teacher.kenji@elc-hanoi.edu.vn', '+84-904-333-333', NULL, 'Suzuki Kenji', '1983-06-08', '77 Dao Tan, Ba Dinh, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '12 hours', NOW() - INTERVAL '145 days', NOW()),
(20, 'teacher.anh@elc-hanoi.edu.vn', '+84-904-444-444', NULL, 'Pham Thi Anh', '1992-01-17', '88 Hoang Hoa Tham, Ba Dinh, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '9 hours', NOW() - INTERVAL '140 days', NOW()),
-- Multi-skilled Teachers
(21, 'teacher.james@elc-hanoi.edu.vn', '+84-905-111-111', NULL, 'James Anderson', '1981-09-22', '99 Nguyen Khang, Cau Giay, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '7 hours', NOW() - INTERVAL '135 days', NOW()),
(22, 'teacher.linda@elc-hanoi.edu.vn', '+84-905-222-222', NULL, 'Linda Martinez', '1986-05-14', '111 Hoang Quoc Viet, Cau Giay, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '14 hours', NOW() - INTERVAL '130 days', NOW()),
(23, 'teacher.robert@elc-hanoi.edu.vn', '+84-905-333-333', NULL, 'Robert Taylor', '1979-08-30', '222 Pham Van Dong, Cau Giay, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '20 hours', NOW() - INTERVAL '125 days', NOW()),
(24, 'teacher.thu@elc-hanoi.edu.vn', '+84-905-444-444', NULL, 'Nguyen Thu Ha', '1993-02-26', '333 Tran Duy Hung, Cau Giay, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '11 hours', NOW() - INTERVAL '120 days', NOW()),
(25, 'teacher.nam@elc-hanoi.edu.vn', '+84-905-555-555', NULL, 'Hoang Van Nam', '1988-11-09', '444 Nguyen Van Huyen, Cau Giay, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '16 hours', NOW() - INTERVAL '115 days', NOW());

-- Students (IDs: 26-80) - 55 students with diverse Vietnamese names
INSERT INTO user_account (id, email, phone, facebook_url, full_name, dob, address, password_hash, status, last_login_at, created_at, updated_at)
VALUES
(26, 'student001@gmail.com', '+84-911-111-111', NULL, 'Nguyen Van An', '1998-03-15', '15 Le Thanh Nghi, Hai Ba Trung, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '100 days', NOW()),
(27, 'student002@gmail.com', '+84-911-222-222', NULL, 'Tran Thi Binh', '2000-07-22', '20 Bach Mai, Hai Ba Trung, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '5 hours', NOW() - INTERVAL '95 days', NOW()),
(28, 'student003@gmail.com', '+84-911-333-333', NULL, 'Le Van Cuong', '1997-11-08', '25 Minh Khai, Hai Ba Trung, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '1 day', NOW() - INTERVAL '90 days', NOW()),
(29, 'student004@gmail.com', '+84-911-444-444', NULL, 'Pham Thi Dung', '2001-05-12', '30 Tran Khanh Du, Hai Ba Trung, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '3 hours', NOW() - INTERVAL '85 days', NOW()),
(30, 'student005@gmail.com', '+84-911-555-555', NULL, 'Hoang Van Duy', '1999-09-25', '35 Truong Dinh, Hai Ba Trung, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '8 hours', NOW() - INTERVAL '80 days', NOW()),
(31, 'student006@gmail.com', '+84-912-111-111', NULL, 'Vo Thi Em', '2002-04-18', '40 Nguyen Du, Hai Ba Trung, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '4 hours', NOW() - INTERVAL '75 days', NOW()),
(32, 'student007@gmail.com', '+84-912-222-222', NULL, 'Dang Van Giang', '1996-12-05', '45 Le Dai Hanh, Hai Ba Trung, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '6 hours', NOW() - INTERVAL '70 days', NOW()),
(33, 'student008@gmail.com', '+84-912-333-333', NULL, 'Bui Thi Ha', '2000-06-30', '50 Tran Nhan Tong, Hai Ba Trung, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '12 hours', NOW() - INTERVAL '65 days', NOW()),
(34, 'student009@gmail.com', '+84-912-444-444', NULL, 'Do Van Hieu', '2003-08-14', '55 Pho Hue, Hai Ba Trung, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '2 days', NOW() - INTERVAL '60 days', NOW()),
(35, 'student010@gmail.com', '+84-912-555-555', NULL, 'Ngo Thi Hong', '1998-02-28', '60 Dai Co Viet, Hai Ba Trung, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '10 hours', NOW() - INTERVAL '55 days', NOW()),
(36, 'student011@gmail.com', '+84-913-111-111', NULL, 'Truong Van Huy', '1997-10-19', '65 Nguyen Luong Bang, Dong Da, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '7 hours', NOW() - INTERVAL '50 days', NOW()),
(37, 'student012@gmail.com', '+84-913-222-222', NULL, 'Duong Thi Khanh', '2001-05-07', '70 Ton Duc Thang, Dong Da, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '15 hours', NOW() - INTERVAL '45 days', NOW()),
(38, 'student013@gmail.com', '+84-913-333-333', NULL, 'Ly Van Kien', '1999-09-13', '75 Kham Thien, Dong Da, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '1 day', NOW() - INTERVAL '40 days', NOW()),
(39, 'student014@gmail.com', '+84-913-444-444', NULL, 'Mac Thi Lan', '2002-03-21', '80 Tay Son, Dong Da, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '9 hours', NOW() - INTERVAL '35 days', NOW()),
(40, 'student015@gmail.com', '+84-913-555-555', NULL, 'Vu Van Linh', '1996-07-16', '85 Giai Phong, Dong Da, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '5 hours', NOW() - INTERVAL '30 days', NOW()),
(41, 'student016@gmail.com', '+84-914-111-111', NULL, 'Trinh Thi Mai', '2000-12-03', '90 La Thanh, Dong Da, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '11 hours', NOW() - INTERVAL '28 days', NOW()),
(42, 'student017@gmail.com', '+84-914-222-222', NULL, 'Quach Van Minh', '2003-04-25', '95 O Cho Dua, Dong Da, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '14 hours', NOW() - INTERVAL '26 days', NOW()),
(43, 'student018@gmail.com', '+84-914-333-333', NULL, 'Dinh Thi Nga', '1998-10-11', '100 Lang, Dong Da, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '3 hours', NOW() - INTERVAL '24 days', NOW()),
(44, 'student019@gmail.com', '+84-914-444-444', NULL, 'Ta Van Phong', '2001-06-08', '105 Huynh Thuc Khang, Dong Da, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '18 hours', NOW() - INTERVAL '22 days', NOW()),
(45, 'student020@gmail.com', '+84-914-555-555', NULL, 'Ha Thi Phuong', '1999-01-17', '110 Nguyen Phuc Lai, Dong Da, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '6 hours', NOW() - INTERVAL '20 days', NOW()),
(46, 'student021@gmail.com', '+84-915-111-111', NULL, 'Cao Van Quang', '1997-09-22', '115 Thai Ha, Dong Da, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '8 hours', NOW() - INTERVAL '18 days', NOW()),
(47, 'student022@gmail.com', '+84-915-222-222', NULL, 'Tong Thi Quynh', '2002-05-14', '120 Chua Lang, Dong Da, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '13 hours', NOW() - INTERVAL '16 days', NOW()),
(48, 'student023@gmail.com', '+84-915-333-333', NULL, 'Lam Van Son', '1998-08-30', '125 Nguyen Khuyen, Dong Da, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '14 days', NOW()),
(49, 'student024@gmail.com', '+84-915-444-444', NULL, 'Ong Thi Thao', '2000-02-26', '130 Khuat Duy Tien, Thanh Xuan, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '19 hours', NOW() - INTERVAL '12 days', NOW()),
(50, 'student025@gmail.com', '+84-915-555-555', NULL, 'Nghiem Van Tuan', '1999-11-09', '135 Nguyen Tuan, Thanh Xuan, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '4 hours', NOW() - INTERVAL '10 days', NOW()),
(51, 'student026@gmail.com', '+84-916-111-111', NULL, 'Bach Thi Uyen', '2001-07-15', '140 Khuc Thua Du, Thanh Xuan, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '7 hours', NOW() - INTERVAL '9 days', NOW()),
(52, 'student027@gmail.com', '+84-916-222-222', NULL, 'Chu Van Vinh', '1996-03-22', '145 Nguy Nhu Kon Tum, Thanh Xuan, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '16 hours', NOW() - INTERVAL '8 days', NOW()),
(53, 'student028@gmail.com', '+84-916-333-333', NULL, 'Doan Thi Xuan', '2003-11-18', '150 Kim Dong, Thanh Xuan, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '10 hours', NOW() - INTERVAL '7 days', NOW()),
(54, 'student029@gmail.com', '+84-916-444-444', NULL, 'Dao Van Yen', '1998-06-25', '155 Trung Kinh, Thanh Xuan, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '12 hours', NOW() - INTERVAL '6 days', NOW()),
(55, 'student030@gmail.com', '+84-916-555-555', NULL, 'Phung Thi Anh', '2000-10-12', '160 Ton That Tung, Dong Da, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '5 hours', NOW() - INTERVAL '5 days', NOW()),
-- Additional students for diverse class enrollments
(56, 'student031@gmail.com', '+84-917-111-111', NULL, 'Phan Van Bao', '1997-04-08', '165 Nguyen Trai, Thanh Xuan, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '3 hours', NOW() - INTERVAL '50 days', NOW()),
(57, 'student032@gmail.com', '+84-917-222-222', NULL, 'Luong Thi Cam', '2001-08-19', '170 Le Van Luong, Thanh Xuan, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '9 hours', NOW() - INTERVAL '48 days', NOW()),
(58, 'student033@gmail.com', '+84-917-333-333', NULL, 'Huynh Van Dat', '1999-12-27', '175 Nguyen Xien, Thanh Xuan, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '11 hours', NOW() - INTERVAL '46 days', NOW()),
(59, 'student034@gmail.com', '+84-917-444-444', NULL, 'Khuu Thi Dieu', '2002-06-14', '180 Khuong Dinh, Thanh Xuan, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '15 hours', NOW() - INTERVAL '44 days', NOW()),
(60, 'student035@gmail.com', '+84-917-555-555', NULL, 'To Van Duc', '1998-02-03', '185 Nguyen Luong Bang, Thanh Xuan, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '6 hours', NOW() - INTERVAL '42 days', NOW()),
(61, 'student036@gmail.com', '+84-918-111-111', NULL, 'Si Thi Eo', '2000-09-28', '190 Hoang Dao Thuy, Thanh Xuan, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '8 hours', NOW() - INTERVAL '40 days', NOW()),
(62, 'student037@gmail.com', '+84-918-222-222', NULL, 'Ung Van Phuc', '1997-05-16', '195 Vu Trong Phung, Thanh Xuan, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '13 hours', NOW() - INTERVAL '38 days', NOW()),
(63, 'student038@gmail.com', '+84-918-333-333', NULL, 'Tang Thi Gia', '2003-01-24', '200 Nguyen Huu Tho, Thanh Xuan, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '17 hours', NOW() - INTERVAL '36 days', NOW()),
(64, 'student039@gmail.com', '+84-918-444-444', NULL, 'Vuong Van Hai', '1996-07-11', '205 Khuc Hao, Thanh Xuan, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '4 hours', NOW() - INTERVAL '34 days', NOW()),
(65, 'student040@gmail.com', '+84-918-555-555', NULL, 'Kieu Thi Hang', '2001-11-05', '210 Ton Duc Thang, Thanh Xuan, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '14 hours', NOW() - INTERVAL '32 days', NOW()),
(66, 'student041@gmail.com', '+84-919-111-111', NULL, 'Nham Van Khanh', '1999-03-19', '215 Khuat Duy Tien, Cau Giay, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '7 hours', NOW() - INTERVAL '30 days', NOW()),
(67, 'student042@gmail.com', '+84-919-222-222', NULL, 'Uong Thi Kim', '2002-10-26', '220 Nguyen Phong Sac, Cau Giay, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '12 hours', NOW() - INTERVAL '28 days', NOW()),
(68, 'student043@gmail.com', '+84-919-333-333', NULL, 'Thach Van Long', '1998-06-13', '225 Tran Thai Tong, Cau Giay, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '26 days', NOW()),
(69, 'student044@gmail.com', '+84-919-444-444', NULL, 'Khong Thi My', '2000-12-08', '230 Hoang Minh Giam, Cau Giay, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '18 hours', NOW() - INTERVAL '24 days', NOW()),
(70, 'student045@gmail.com', '+84-919-555-555', NULL, 'Tieu Van Nam', '1997-08-23', '235 Duong Lang, Dong Da, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '10 hours', NOW() - INTERVAL '22 days', NOW()),
(71, 'student046@gmail.com', '+84-920-111-111', NULL, 'Quan Thi Oanh', '2001-04-17', '240 Pham Ngoc Thach, Dong Da, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '5 hours', NOW() - INTERVAL '20 days', NOW()),
(72, 'student047@gmail.com', '+84-920-222-222', NULL, 'Luu Van Phuc', '1996-11-30', '245 Xa Dan, Dong Da, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '16 hours', NOW() - INTERVAL '18 days', NOW()),
(73, 'student048@gmail.com', '+84-920-333-333', NULL, 'An Thi Quyen', '2003-07-21', '250 Nguyen Cong Tru, Hai Ba Trung, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '9 hours', NOW() - INTERVAL '16 days', NOW()),
(74, 'student049@gmail.com', '+84-920-444-444', NULL, 'Ninh Van Sang', '1999-02-14', '255 Pho Vong, Hai Ba Trung, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '1 day', NOW() - INTERVAL '14 days', NOW()),
(75, 'student050@gmail.com', '+84-920-555-555', NULL, 'Nghia Thi Tam', '2002-09-05', '260 Tran Khat Chan, Hai Ba Trung, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '3 hours', NOW() - INTERVAL '12 days', NOW()),
(76, 'student051@gmail.com', '+84-921-111-111', NULL, 'Thai Van Tung', '1998-05-28', '265 Hai Ba Trung, Hai Ba Trung, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '11 hours', NOW() - INTERVAL '10 days', NOW()),
(77, 'student052@gmail.com', '+84-921-222-222', NULL, 'Thi Thi Uyen', '2000-01-10', '270 Bui Thi Xuan, Hai Ba Trung, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '6 hours', NOW() - INTERVAL '8 days', NOW()),
(78, 'student053@gmail.com', '+84-921-333-333', NULL, 'Kim Van Vu', '1997-10-02', '275 Ly Thuong Kiet, Hoan Kiem, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '15 hours', NOW() - INTERVAL '6 days', NOW()),
(79, 'student054@gmail.com', '+84-921-444-444', NULL, 'Tay Thi Xuan', '2001-06-19', '280 Trang Tien, Hoan Kiem, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '8 hours', NOW() - INTERVAL '4 days', NOW()),
(80, 'student055@gmail.com', '+84-921-555-555', NULL, 'Y Van Yen', '1999-12-25', '285 Hang Bai, Hoan Kiem, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 days', NOW());

-- Reset sequence
SELECT setval('user_account_id_seq', (SELECT MAX(id) FROM user_account));

-- =========================================
-- SECTION 3: USER ROLE & BRANCH MAPPINGS
-- =========================================

-- 3.1 User-Role Mappings
INSERT INTO user_role (user_id, role_id)
VALUES
-- Admin
(1, 1), -- Admin → ADMIN role
-- Manager
(2, 2), -- Manager → MANAGER role
-- Center Head
(3, 3), -- Center Head → CENTER_HEAD role
-- Academic Staff
(4, 4), -- Academic 1 → ACADEMIC_STAFF role
(5, 4), -- Academic 2 → ACADEMIC_STAFF role
-- Subject Leaders
(6, 5), -- English Leader → SUBJECT_LEADER role
(7, 5), -- Japanese Leader → SUBJECT_LEADER role
-- QA
(8, 8), -- QA Staff → QA role
-- Teachers (IDs 9-25 → TEACHER role)
(9, 6), (10, 6), (11, 6), (12, 6), (13, 6), (14, 6), (15, 6), (16, 6),
(17, 6), (18, 6), (19, 6), (20, 6), (21, 6), (22, 6), (23, 6), (24, 6), (25, 6),
-- Students (IDs 26-80 → STUDENT role)
(26, 7), (27, 7), (28, 7), (29, 7), (30, 7), (31, 7), (32, 7), (33, 7), (34, 7), (35, 7),
(36, 7), (37, 7), (38, 7), (39, 7), (40, 7), (41, 7), (42, 7), (43, 7), (44, 7), (45, 7),
(46, 7), (47, 7), (48, 7), (49, 7), (50, 7), (51, 7), (52, 7), (53, 7), (54, 7), (55, 7),
(56, 7), (57, 7), (58, 7), (59, 7), (60, 7), (61, 7), (62, 7), (63, 7), (64, 7), (65, 7),
(66, 7), (67, 7), (68, 7), (69, 7), (70, 7), (71, 7), (72, 7), (73, 7), (74, 7), (75, 7),
(76, 7), (77, 7), (78, 7), (79, 7), (80, 7);

-- 3.2 User-Branch Mappings (assign users to Main Campus)
INSERT INTO user_branches (user_id, branch_id, assigned_at, assigned_by)
VALUES
-- Management & Staff assigned to Main Campus
(1, 1, NOW() - INTERVAL '365 days', NULL), -- Admin
(2, 1, NOW() - INTERVAL '300 days', 1),    -- Manager (assigned by Admin)
(3, 1, NOW() - INTERVAL '200 days', 1),    -- Center Head (assigned by Admin)
(4, 1, NOW() - INTERVAL '180 days', 2),    -- Academic Staff 1 (assigned by Manager)
(5, 1, NOW() - INTERVAL '180 days', 2),    -- Academic Staff 2 (assigned by Manager)
(6, 1, NOW() - INTERVAL '250 days', 2),    -- English Leader (assigned by Manager)
(7, 1, NOW() - INTERVAL '220 days', 2),    -- Japanese Leader (assigned by Manager)
(8, 1, NOW() - INTERVAL '150 days', 2),    -- QA Staff (assigned by Manager)
-- All Teachers assigned to Main Campus
(9, 1, NOW() - INTERVAL '200 days', 4),    -- John Smith
(10, 1, NOW() - INTERVAL '190 days', 4),   -- Sarah Johnson
(11, 1, NOW() - INTERVAL '185 days', 4),   -- Michael Brown
(12, 1, NOW() - INTERVAL '180 days', 4),   -- Emily Davis
(13, 1, NOW() - INTERVAL '175 days', 4),   -- Nguyen Thi Linh
(14, 1, NOW() - INTERVAL '170 days', 4),   -- Tran Van Huy
(15, 1, NOW() - INTERVAL '165 days', 4),   -- Le Thi Mai
(16, 1, NOW() - INTERVAL '160 days', 4),   -- David Wilson
(17, 1, NOW() - INTERVAL '155 days', 4),   -- Yamamoto Yuki
(18, 1, NOW() - INTERVAL '150 days', 4),   -- Sato Sakura
(19, 1, NOW() - INTERVAL '145 days', 4),   -- Suzuki Kenji
(20, 1, NOW() - INTERVAL '140 days', 4),   -- Pham Thi Anh
(21, 1, NOW() - INTERVAL '135 days', 5),   -- James Anderson
(22, 1, NOW() - INTERVAL '130 days', 5),   -- Linda Martinez
(23, 1, NOW() - INTERVAL '125 days', 5),   -- Robert Taylor
(24, 1, NOW() - INTERVAL '120 days', 5),   -- Nguyen Thu Ha
(25, 1, NOW() - INTERVAL '115 days', 5);   -- Hoang Van Nam
-- Note: Students are assigned to branches via enrollment, not directly via user_branches

-- =========================================
-- SECTION 4: TEACHERS & STUDENTS
-- =========================================

-- 4.1 Teachers (IDs 1-17)
INSERT INTO teacher (id, user_account_id, employee_code, note, created_at, updated_at)
VALUES
-- English Teachers
(1, 9, 'T001', 'Native speaker, specialized in IELTS Speaking & Writing', NOW() - INTERVAL '200 days', NOW()),
(2, 10, 'T002', 'CELTA certified, experienced in General English', NOW() - INTERVAL '190 days', NOW()),
(3, 11, 'T003', 'TESOL certified, focus on Business English', NOW() - INTERVAL '185 days', NOW()),
(4, 12, 'T004', 'Cambridge examiner, IELTS & TOEIC specialist', NOW() - INTERVAL '180 days', NOW()),
(5, 13, 'T005', 'Experienced Vietnamese teacher, strong grammar focus', NOW() - INTERVAL '175 days', NOW()),
(6, 14, 'T006', 'Conversational English specialist', NOW() - INTERVAL '170 days', NOW()),
(7, 15, 'T007', 'IELTS Reading & Listening expert', NOW() - INTERVAL '165 days', NOW()),
(8, 16, 'T008', 'Business English and presentation skills', NOW() - INTERVAL '160 days', NOW()),
-- Japanese Teachers
(9, 17, 'T009', 'Native Japanese speaker, JLPT N1-N3 specialist', NOW() - INTERVAL '155 days', NOW()),
(10, 18, 'T010', 'Japanese native, focus on conversation and culture', NOW() - INTERVAL '150 days', NOW()),
(11, 19, 'T011', 'JLPT certified teacher, grammar and kanji expert', NOW() - INTERVAL '145 days', NOW()),
(12, 20, 'T012', 'Vietnamese teacher with N1 certification', NOW() - INTERVAL '140 days', NOW()),
-- Multi-skilled Teachers
(13, 21, 'T013', 'English & Japanese bilingual teacher', NOW() - INTERVAL '135 days', NOW()),
(14, 22, 'T014', 'IELTS Speaking examiner, pronunciation specialist', NOW() - INTERVAL '130 days', NOW()),
(15, 23, 'T015', 'TOEIC expert, corporate training experience', NOW() - INTERVAL '125 days', NOW()),
(16, 24, 'T016', 'Young learners specialist, interactive teaching', NOW() - INTERVAL '120 days', NOW()),
(17, 25, 'T017', 'Test preparation specialist for all levels', NOW() - INTERVAL '115 days', NOW());

-- 4.2 Teacher Skills (diverse skill combinations)
INSERT INTO teacher_skill (teacher_id, skill, level)
VALUES
-- John Smith (T001) - IELTS Speaking & Writing specialist
(1, 'speaking', 5), (1, 'writing', 5), (1, 'general', 4),
-- Sarah Johnson (T002) - General English
(2, 'general', 5), (2, 'reading', 4), (2, 'listening', 4),
-- Michael Brown (T003) - Business English
(3, 'general', 5), (3, 'speaking', 4), (3, 'writing', 4),
-- Emily Davis (T004) - IELTS & TOEIC
(4, 'reading', 5), (4, 'listening', 5), (4, 'writing', 4), (4, 'speaking', 4),
-- Nguyen Thi Linh (T005) - Grammar focus
(5, 'general', 5), (5, 'writing', 4), (5, 'reading', 4),
-- Tran Van Huy (T006) - Conversational
(6, 'speaking', 5), (6, 'listening', 5), (6, 'general', 4),
-- Le Thi Mai (T007) - IELTS Reading & Listening
(7, 'reading', 5), (7, 'listening', 5), (7, 'general', 3),
-- David Wilson (T008) - Business English
(8, 'speaking', 5), (8, 'general', 4), (8, 'writing', 4),
-- Yamamoto Yuki (T009) - Japanese all skills
(9, 'general', 5), (9, 'reading', 5), (9, 'speaking', 5), (9, 'listening', 5),
-- Sato Sakura (T010) - Japanese conversation
(10, 'speaking', 5), (10, 'listening', 5), (10, 'general', 4),
-- Suzuki Kenji (T011) - Japanese grammar
(11, 'general', 5), (11, 'reading', 5), (11, 'writing', 5),
-- Pham Thi Anh (T012) - Japanese certified
(12, 'general', 5), (12, 'reading', 4), (12, 'writing', 4), (12, 'listening', 4),
-- James Anderson (T013) - Bilingual
(13, 'general', 5), (13, 'speaking', 5), (13, 'listening', 4),
-- Linda Martinez (T014) - IELTS Speaking
(14, 'speaking', 5), (14, 'listening', 5), (14, 'general', 4),
-- Robert Taylor (T015) - TOEIC
(15, 'reading', 5), (15, 'listening', 5), (15, 'general', 4),
-- Nguyen Thu Ha (T016) - Young learners
(16, 'general', 5), (16, 'speaking', 4), (16, 'listening', 4),
-- Hoang Van Nam (T017) - Test prep all-rounder
(17, 'general', 5), (17, 'reading', 4), (17, 'writing', 4), (17, 'listening', 4), (17, 'speaking', 4);

-- 4.3 Students (IDs 1-55, user_account_id 26-80)
INSERT INTO student (id, user_id, student_code, level, created_at, updated_at)
VALUES
(1, 26, 'S001', 'Intermediate', NOW() - INTERVAL '100 days', NOW()),
(2, 27, 'S002', 'Beginner', NOW() - INTERVAL '95 days', NOW()),
(3, 28, 'S003', 'Intermediate', NOW() - INTERVAL '90 days', NOW()),
(4, 29, 'S004', 'Beginner', NOW() - INTERVAL '85 days', NOW()),
(5, 30, 'S005', 'Advanced', NOW() - INTERVAL '80 days', NOW()),
(6, 31, 'S006', 'Beginner', NOW() - INTERVAL '75 days', NOW()),
(7, 32, 'S007', 'Intermediate', NOW() - INTERVAL '70 days', NOW()),
(8, 33, 'S008', 'Beginner', NOW() - INTERVAL '65 days', NOW()),
(9, 34, 'S009', 'Beginner', NOW() - INTERVAL '60 days', NOW()),
(10, 35, 'S010', 'Advanced', NOW() - INTERVAL '55 days', NOW()),
(11, 36, 'S011', 'Intermediate', NOW() - INTERVAL '50 days', NOW()),
(12, 37, 'S012', 'Beginner', NOW() - INTERVAL '45 days', NOW()),
(13, 38, 'S013', 'Beginner', NOW() - INTERVAL '40 days', NOW()),
(14, 39, 'S014', 'Intermediate', NOW() - INTERVAL '35 days', NOW()),
(15, 40, 'S015', 'Advanced', NOW() - INTERVAL '30 days', NOW()),
(16, 41, 'S016', 'Beginner', NOW() - INTERVAL '28 days', NOW()),
(17, 42, 'S017', 'Beginner', NOW() - INTERVAL '26 days', NOW()),
(18, 43, 'S018', 'Intermediate', NOW() - INTERVAL '24 days', NOW()),
(19, 44, 'S019', 'Beginner', NOW() - INTERVAL '22 days', NOW()),
(20, 45, 'S020', 'Advanced', NOW() - INTERVAL '20 days', NOW()),
(21, 46, 'S021', 'Intermediate', NOW() - INTERVAL '18 days', NOW()),
(22, 47, 'S022', 'Beginner', NOW() - INTERVAL '16 days', NOW()),
(23, 48, 'S023', 'Beginner', NOW() - INTERVAL '14 days', NOW()),
(24, 49, 'S024', 'Advanced', NOW() - INTERVAL '12 days', NOW()),
(25, 50, 'S025', 'Intermediate', NOW() - INTERVAL '10 days', NOW()),
(26, 51, 'S026', 'Beginner', NOW() - INTERVAL '9 days', NOW()),
(27, 52, 'S027', 'Beginner', NOW() - INTERVAL '8 days', NOW()),
(28, 53, 'S028', 'Intermediate', NOW() - INTERVAL '7 days', NOW()),
(29, 54, 'S029', 'Advanced', NOW() - INTERVAL '6 days', NOW()),
(30, 55, 'S030', 'Beginner', NOW() - INTERVAL '5 days', NOW()),
(31, 56, 'S031', 'Beginner', NOW() - INTERVAL '50 days', NOW()),
(32, 57, 'S032', 'Intermediate', NOW() - INTERVAL '48 days', NOW()),
(33, 58, 'S033', 'Beginner', NOW() - INTERVAL '46 days', NOW()),
(34, 59, 'S034', 'Advanced', NOW() - INTERVAL '44 days', NOW()),
(35, 60, 'S035', 'Intermediate', NOW() - INTERVAL '42 days', NOW()),
(36, 61, 'S036', 'Beginner', NOW() - INTERVAL '40 days', NOW()),
(37, 62, 'S037', 'Beginner', NOW() - INTERVAL '38 days', NOW()),
(38, 63, 'S038', 'Intermediate', NOW() - INTERVAL '36 days', NOW()),
(39, 64, 'S039', 'Advanced', NOW() - INTERVAL '34 days', NOW()),
(40, 65, 'S040', 'Beginner', NOW() - INTERVAL '32 days', NOW()),
(41, 66, 'S041', 'Beginner', NOW() - INTERVAL '30 days', NOW()),
(42, 67, 'S042', 'Intermediate', NOW() - INTERVAL '28 days', NOW()),
(43, 68, 'S043', 'Advanced', NOW() - INTERVAL '26 days', NOW()),
(44, 69, 'S044', 'Beginner', NOW() - INTERVAL '24 days', NOW()),
(45, 70, 'S045', 'Intermediate', NOW() - INTERVAL '22 days', NOW()),
(46, 71, 'S046', 'Beginner', NOW() - INTERVAL '20 days', NOW()),
(47, 72, 'S047', 'Beginner', NOW() - INTERVAL '18 days', NOW()),
(48, 73, 'S048', 'Advanced', NOW() - INTERVAL '16 days', NOW()),
(49, 74, 'S049', 'Intermediate', NOW() - INTERVAL '14 days', NOW()),
(50, 75, 'S050', 'Beginner', NOW() - INTERVAL '12 days', NOW()),
(51, 76, 'S051', 'Beginner', NOW() - INTERVAL '10 days', NOW()),
(52, 77, 'S052', 'Advanced', NOW() - INTERVAL '8 days', NOW()),
(53, 78, 'S053', 'Intermediate', NOW() - INTERVAL '6 days', NOW()),
(54, 79, 'S054', 'Beginner', NOW() - INTERVAL '4 days', NOW()),
(55, 80, 'S055', 'Beginner', NOW() - INTERVAL '2 days', NOW());

-- Reset sequences
SELECT setval('teacher_id_seq', (SELECT MAX(id) FROM teacher));
SELECT setval('student_id_seq', (SELECT MAX(id) FROM student));

-- =========================================
-- SECTION 4.5: STUDENT SKILL ASSESSMENTS
-- =========================================
-- Note: Cần chạy sau khi có subject và level data. 
-- Tạm thời comment out phần này, sẽ thêm sau khi có subject và level IDs.
-- Sẽ uncomment và điền data sau SECTION 6 & 7 (sau khi có subject_id và level_id)

-- =========================================
-- SECTION 5: RESOURCES & TIME SLOTS
-- =========================================

-- 5.1 Time Slot Templates (Morning, Afternoon, Evening shifts)
INSERT INTO time_slot_template (id, branch_id, name, start_time, end_time, duration_min, created_at, updated_at)
VALUES
-- Morning Slots
(1, 1, 'Morning Slot 1', '07:00:00', '08:30:00', 90, NOW() - INTERVAL '200 days', NOW()),
(2, 1, 'Morning Slot 2', '08:45:00', '10:15:00', 90, NOW() - INTERVAL '200 days', NOW()),
(3, 1, 'Morning Slot 3', '10:30:00', '12:00:00', 90, NOW() - INTERVAL '200 days', NOW()),
-- Afternoon Slots
(4, 1, 'Afternoon Slot 1', '13:00:00', '14:30:00', 90, NOW() - INTERVAL '200 days', NOW()),
(5, 1, 'Afternoon Slot 2', '14:45:00', '16:15:00', 90, NOW() - INTERVAL '200 days', NOW()),
(6, 1, 'Afternoon Slot 3', '16:30:00', '18:00:00', 90, NOW() - INTERVAL '200 days', NOW()),
-- Evening Slots
(7, 1, 'Evening Slot 1', '18:15:00', '19:45:00', 90, NOW() - INTERVAL '200 days', NOW()),
(8, 1, 'Evening Slot 2', '20:00:00', '21:30:00', 90, NOW() - INTERVAL '200 days', NOW()),
-- Weekend Slots
(9, 1, 'Weekend Morning', '08:00:00', '10:00:00', 120, NOW() - INTERVAL '200 days', NOW()),
(10, 1, 'Weekend Afternoon', '14:00:00', '16:00:00', 120, NOW() - INTERVAL '200 days', NOW());

-- 5.2 Physical Rooms
INSERT INTO resource (id, branch_id, resource_type, name, location, capacity, description, equipment, created_by, created_at, updated_at)
VALUES
-- Small classrooms (capacity 8-12)
(1, 1, 'room', 'Room 101', 'Floor 1', 10, 'Small classroom for intensive courses', 'Whiteboard, Projector, Air conditioning', 4, NOW() - INTERVAL '200 days', NOW()),
(2, 1, 'room', 'Room 102', 'Floor 1', 12, 'Small classroom with audio equipment', 'Whiteboard, Audio system, TV, Air conditioning', 4, NOW() - INTERVAL '200 days', NOW()),
(3, 1, 'room', 'Room 103', 'Floor 1', 8, 'VIP room for 1-on-1 or small groups', 'Whiteboard, Projector, Sofa, Coffee table', 4, NOW() - INTERVAL '200 days', NOW()),
-- Medium classrooms (capacity 15-20)
(4, 1, 'room', 'Room 201', 'Floor 2', 15, 'Standard classroom', 'Whiteboard, Projector, Sound system, Air conditioning', 4, NOW() - INTERVAL '200 days', NOW()),
(5, 1, 'room', 'Room 202', 'Floor 2', 18, 'Multimedia classroom', 'Interactive whiteboard, Projector, Computer, Speakers', 4, NOW() - INTERVAL '200 days', NOW()),
(6, 1, 'room', 'Room 203', 'Floor 2', 20, 'Large standard classroom', 'Whiteboard, Projector, Audio system, Air conditioning', 4, NOW() - INTERVAL '200 days', NOW()),
-- Large classrooms (capacity 25-30)
(7, 1, 'room', 'Room 301', 'Floor 3', 25, 'Large classroom for test preparation', 'Whiteboards (2), Projector, PA system, Air conditioning', 4, NOW() - INTERVAL '200 days', NOW()),
(8, 1, 'room', 'Room 302', 'Floor 3', 30, 'Auditorium-style classroom', 'Stage, Projector, Sound system, Microphones, Air conditioning', 4, NOW() - INTERVAL '200 days', NOW()),
-- Specialized rooms
(9, 1, 'room', 'Lab 401', 'Floor 4', 16, 'Computer lab for online practice', '16 computers, Headphones, Language software, Projector', 4, NOW() - INTERVAL '200 days', NOW()),
(10, 1, 'room', 'Speaking Room 402', 'Floor 4', 6, 'Speaking practice room with recording', 'Recording equipment, Mirrors, Comfortable seating', 4, NOW() - INTERVAL '200 days', NOW());

-- 5.3 Virtual Resources (Zoom accounts)
INSERT INTO resource (id, branch_id, resource_type, name, meeting_url, meeting_id, account_email, license_type, expiry_date, renewal_date, capacity, description, created_by, created_at, updated_at)
VALUES
(11, 1, 'virtual', 'Zoom Account 1', 'https://zoom.us/j/11111111111', '111-1111-1111', 'zoom1@elc-hanoi.edu.vn', 'Pro', CURRENT_DATE + INTERVAL '300 days', CURRENT_DATE + INTERVAL '270 days', 100, 'Primary Zoom account for large classes', 4, NOW() - INTERVAL '180 days', NOW()),
(12, 1, 'virtual', 'Zoom Account 2', 'https://zoom.us/j/22222222222', '222-2222-2222', 'zoom2@elc-hanoi.edu.vn', 'Pro', CURRENT_DATE + INTERVAL '320 days', CURRENT_DATE + INTERVAL '290 days', 100, 'Secondary Zoom account for online classes', 4, NOW() - INTERVAL '180 days', NOW()),
(13, 1, 'virtual', 'Zoom Account 3', 'https://zoom.us/j/33333333333', '333-3333-3333', 'zoom3@elc-hanoi.edu.vn', 'Business', CURRENT_DATE + INTERVAL '350 days', CURRENT_DATE + INTERVAL '320 days', 300, 'Business account for webinars and large events', 4, NOW() - INTERVAL '180 days', NOW()),
(14, 1, 'virtual', 'Zoom Account 4', 'https://zoom.us/j/44444444444', '444-4444-4444', 'zoom4@elc-hanoi.edu.vn', 'Pro', CURRENT_DATE + INTERVAL '310 days', CURRENT_DATE + INTERVAL '280 days', 100, 'Backup Zoom account', 4, NOW() - INTERVAL '180 days', NOW()),
(15, 1, 'virtual', 'Zoom Account 5', 'https://zoom.us/j/55555555555', '555-5555-5555', 'zoom5@elc-hanoi.edu.vn', 'Pro', CURRENT_DATE + INTERVAL '330 days', CURRENT_DATE + INTERVAL '300 days', 100, 'Dedicated for evening classes', 4, NOW() - INTERVAL '180 days', NOW());

-- Reset sequences
SELECT setval('time_slot_template_id_seq', (SELECT MAX(id) FROM time_slot_template));
SELECT setval('resource_id_seq', (SELECT MAX(id) FROM resource));

-- =========================================
-- SECTION 6: ACADEMIC CURRICULUM
-- =========================================

-- 6.1 Subjects
INSERT INTO subject (id, code, name, description, status, created_by, created_at, updated_at)
VALUES
(1, 'ENG', 'English', 'English language training programs including IELTS, TOEIC, Business English, and General English', 'active', 6, NOW() - INTERVAL '250 days', NOW()),
(2, 'JPN', 'Japanese', 'Japanese language training programs including JLPT preparation and conversational Japanese', 'active', 7, NOW() - INTERVAL '220 days', NOW());

-- 6.2 Levels for English (CEFR framework)
INSERT INTO level (id, subject_id, code, name, standard_type, expected_duration_hours, sort_order, description, created_at, updated_at)
VALUES
(1, 1, 'A1', 'Beginner (A1)', 'CEFR', 80, 1, 'Basic user - Can understand and use familiar everyday expressions', NOW() - INTERVAL '250 days', NOW()),
(2, 1, 'A2', 'Elementary (A2)', 'CEFR', 100, 2, 'Basic user - Can communicate in simple routine tasks', NOW() - INTERVAL '250 days', NOW()),
(3, 1, 'B1', 'Intermediate (B1)', 'CEFR', 120, 3, 'Independent user - Can deal with most situations while traveling', NOW() - INTERVAL '250 days', NOW()),
(4, 1, 'B2', 'Upper-Intermediate (B2)', 'CEFR', 140, 4, 'Independent user - Can interact with fluency and spontaneity', NOW() - INTERVAL '250 days', NOW()),
(5, 1, 'C1', 'Advanced (C1)', 'CEFR', 160, 5, 'Proficient user - Can express ideas fluently and spontaneously', NOW() - INTERVAL '250 days', NOW()),
(6, 1, 'C2', 'Proficiency (C2)', 'CEFR', 180, 6, 'Proficient user - Can understand virtually everything heard or read', NOW() - INTERVAL '250 days', NOW());

-- 6.3 Levels for Japanese (JLPT framework)
INSERT INTO level (id, subject_id, code, name, standard_type, expected_duration_hours, sort_order, description, created_at, updated_at)
VALUES
(7, 2, 'N5', 'JLPT N5', 'JLPT', 80, 1, 'Basic kanji and vocabulary, simple conversations', NOW() - INTERVAL '220 days', NOW()),
(8, 2, 'N4', 'JLPT N4', 'JLPT', 100, 2, 'Basic kanji and vocabulary, everyday conversations', NOW() - INTERVAL '220 days', NOW()),
(9, 2, 'N3', 'JLPT N3', 'JLPT', 120, 3, 'Intermediate kanji and vocabulary, everyday situations', NOW() - INTERVAL '220 days', NOW()),
(10, 2, 'N2', 'JLPT N2', 'JLPT', 150, 4, 'Advanced vocabulary, newspapers and general topics', NOW() - INTERVAL '220 days', NOW()),
(11, 2, 'N1', 'JLPT N1', 'JLPT', 180, 5, 'Advanced vocabulary, complex texts and abstract topics', NOW() - INTERVAL '220 days', NOW());

-- 6.4 Courses (diverse English and Japanese courses)
INSERT INTO course (id, subject_id, level_id, logical_course_code, version, code, name, description, total_hours, duration_weeks, session_per_week, hours_per_session, prerequisites, target_audience, teaching_methods, effective_date, status, approved_by_manager, approved_at, created_by, created_at, updated_at)
VALUES
-- English Courses
(1, 1, 1, 'ENG-A1-GEN', 1, 'ENG-A1-GEN-V1', 'General English A1', 'Foundation course for complete beginners focusing on basic grammar, vocabulary, and everyday communication', 80, 12, 3, 2.0, NULL, 'Complete beginners with no prior English knowledge', 'Communicative approach, task-based learning, interactive activities', '2024-01-01', 'active', 2, NOW() - INTERVAL '240 days', 6, NOW() - INTERVAL '245 days', NOW()),

(2, 1, 3, 'ENG-B1-IELTS', 1, 'ENG-B1-IELTS-V1', 'IELTS Foundation (B1)', 'IELTS preparation for intermediate learners targeting band 5.0-6.0', 120, 16, 3, 2.5, 'Completed A2 or equivalent (IELTS 4.0-4.5)', 'Students preparing for IELTS exam, university applicants', 'IELTS-focused tasks, mock tests, skill-building exercises', '2024-01-01', 'active', 2, NOW() - INTERVAL '235 days', 6, NOW() - INTERVAL '240 days', NOW()),

(3, 1, 4, 'ENG-B2-IELTS', 1, 'ENG-B2-IELTS-V1', 'IELTS Intermediate (B2)', 'IELTS preparation for upper-intermediate learners targeting band 6.5-7.5', 140, 18, 3, 2.5, 'Completed B1 or equivalent (IELTS 5.0-5.5)', 'Students aiming for university admission or immigration', 'Intensive practice, timed tests, examiner feedback', '2024-01-01', 'active', 2, NOW() - INTERVAL '230 days', 6, NOW() - INTERVAL '235 days', NOW()),

(4, 1, 4, 'ENG-B2-BUS', 1, 'ENG-B2-BUS-V1', 'Business English (B2)', 'Professional English for business communication, presentations, and negotiations', 100, 14, 2, 3.0, 'Intermediate English (B1 minimum)', 'Working professionals, business students, managers', 'Case studies, role plays, business simulations, presentation practice', '2024-02-01', 'active', 2, NOW() - INTERVAL '225 days', 6, NOW() - INTERVAL '230 days', NOW()),

(5, 1, 2, 'ENG-A2-CONV', 1, 'ENG-A2-CONV-V1', 'Conversational English (A2)', 'Focus on speaking and listening skills for everyday communication', 60, 10, 3, 2.0, 'Basic English (A1 minimum)', 'Students wanting to improve speaking confidence', 'Speaking clubs, pair work, group discussions, real-life scenarios', '2024-01-15', 'active', 2, NOW() - INTERVAL '220 days', 6, NOW() - INTERVAL '225 days', NOW()),

(6, 1, 4, 'ENG-B2-TOEIC', 1, 'ENG-B2-TOEIC-V1', 'TOEIC Preparation (B2)', 'Comprehensive TOEIC test preparation targeting 750+ score', 80, 12, 2, 3.0, 'Intermediate English (B1 minimum)', 'Job seekers, corporate employees, university students', 'TOEIC strategies, practice tests, time management techniques', '2024-02-15', 'active', 2, NOW() - INTERVAL '215 days', 6, NOW() - INTERVAL '220 days', NOW()),

-- Japanese Courses
(7, 2, 7, 'JPN-N5-GEN', 1, 'JPN-N5-GEN-V1', 'Japanese for Beginners (N5)', 'Introduction to Japanese language covering hiragana, katakana, basic kanji, and simple conversations', 80, 12, 3, 2.0, NULL, 'Complete beginners with no Japanese knowledge', 'Structured grammar lessons, writing practice, listening exercises', '2024-01-01', 'active', 2, NOW() - INTERVAL '210 days', 7, NOW() - INTERVAL '215 days', NOW()),

(8, 2, 8, 'JPN-N4-GEN', 1, 'JPN-N4-GEN-V1', 'Japanese Elementary (N4)', 'Building on N5 foundations with expanded kanji, grammar, and conversation skills', 100, 14, 3, 2.5, 'Completed N5 or equivalent', 'Students who completed N5 level', 'Grammar drills, kanji practice, conversation activities', '2024-01-01', 'active', 2, NOW() - INTERVAL '205 days', 7, NOW() - INTERVAL '210 days', NOW()),

(9, 2, 9, 'JPN-N3-PREP', 1, 'JPN-N3-PREP-V1', 'JLPT N3 Preparation', 'Comprehensive N3 exam preparation with focus on intermediate grammar and vocabulary', 120, 16, 3, 2.5, 'Completed N4 or equivalent', 'N3 exam takers, students planning to study in Japan', 'JLPT practice tests, grammar review, reading comprehension', '2024-02-01', 'active', 2, NOW() - INTERVAL '200 days', 7, NOW() - INTERVAL '205 days', NOW()),

(10, 2, 10, 'JPN-N2-PREP', 1, 'JPN-N2-PREP-V1', 'JLPT N2 Preparation', 'Advanced N2 exam preparation focusing on complex grammar and kanji', 150, 20, 3, 2.5, 'Completed N3 or equivalent', 'Advanced learners preparing for N2 exam', 'Intensive grammar, advanced reading, listening practice', '2024-02-15', 'active', 2, NOW() - INTERVAL '195 days', 7, NOW() - INTERVAL '200 days', NOW()),

(11, 2, 9, 'JPN-N3-CONV', 1, 'JPN-N3-CONV-V1', 'Japanese Conversation (N3)', 'Intermediate conversation course focusing on practical speaking skills', 90, 12, 3, 2.5, 'Completed N4 or equivalent', 'Students wanting to improve speaking fluency', 'Role plays, discussion topics, pronunciation practice', '2024-03-01', 'active', 2, NOW() - INTERVAL '190 days', 7, NOW() - INTERVAL '195 days', NOW()),

(12, 2, 10, 'JPN-N2-BUS', 1, 'JPN-N2-BUS-V1', 'Business Japanese (N2)', 'Professional Japanese for business contexts and keigo (honorific language)', 100, 14, 2, 3.0, 'N3 level or equivalent', 'Working professionals, students planning business careers in Japan', 'Business scenarios, keigo practice, email writing, meeting simulations', '2024-03-15', 'active', 2, NOW() - INTERVAL '185 days', 7, NOW() - INTERVAL '190 days', NOW());

-- Reset sequences
SELECT setval('subject_id_seq', (SELECT MAX(id) FROM subject));
SELECT setval('level_id_seq', (SELECT MAX(id) FROM level));
SELECT setval('course_id_seq', (SELECT MAX(id) FROM course));

-- =========================================
-- SECTION 7: COURSE STRUCTURE (PHASES & SESSIONS)
-- =========================================
-- Note: Creating detailed structure for courses 1, 2, 7 as examples
-- Other courses follow similar patterns

-- 7.1 Course Phases for Course 1 (General English A1)
INSERT INTO course_phase (id, course_id, phase_number, name, duration_weeks, learning_focus, sort_order, created_at, updated_at)
VALUES
(1, 1, 1, 'Foundation Phase', 4, 'Basic greetings, alphabet, numbers, simple present tense', 1, NOW() - INTERVAL '240 days', NOW()),
(2, 1, 2, 'Building Phase', 4, 'Family, daily routines, food, simple past tense', 2, NOW() - INTERVAL '240 days', NOW()),
(3, 1, 3, 'Development Phase', 4, 'Shopping, directions, hobbies, future tense', 3, NOW() - INTERVAL '240 days', NOW());

-- 7.2 Course Sessions for Course 1 Phase 1 (12 sessions: 4 weeks × 3 sessions/week)
INSERT INTO course_session (id, phase_id, sequence_no, topic, student_task, skill_set, created_at, updated_at)
VALUES
-- Foundation Phase (Phase 1)
(1, 1, 1, 'Greetings and Introductions', 'Practice self-introduction, learn basic greetings', '{general,speaking,listening}', NOW() - INTERVAL '240 days', NOW()),
(2, 1, 2, 'Alphabet and Pronunciation', 'Learn English alphabet, practice pronunciation', '{general,speaking,listening}', NOW() - INTERVAL '240 days', NOW()),
(3, 1, 3, 'Numbers 1-100', 'Learn numbers, practice counting', '{general,listening}', NOW() - INTERVAL '240 days', NOW()),
(4, 1, 4, 'Personal Information', 'Fill personal information forms, ask/answer personal questions', '{general,speaking,writing}', NOW() - INTERVAL '240 days', NOW()),
(5, 1, 5, 'Simple Present Tense - To Be', 'Learn verb to be, describe people and places', '{general,writing}', NOW() - INTERVAL '240 days', NOW()),
(6, 1, 6, 'Countries and Nationalities', 'Learn countries, talk about nationalities', '{general,speaking}', NOW() - INTERVAL '240 days', NOW()),
(7, 1, 7, 'Jobs and Occupations', 'Learn job vocabulary, describe occupations', '{general,speaking,writing}', NOW() - INTERVAL '240 days', NOW()),
(8, 1, 8, 'Simple Present Tense - Regular Verbs', 'Learn present simple, describe habits', '{general,writing}', NOW() - INTERVAL '240 days', NOW()),
(9, 1, 9, 'Daily Activities', 'Talk about daily routines and schedules', '{general,speaking}', NOW() - INTERVAL '240 days', NOW()),
(10, 1, 10, 'Telling Time', 'Learn to tell time, make appointments', '{general,speaking,listening}', NOW() - INTERVAL '240 days', NOW()),
(11, 1, 11, 'Review and Practice', 'Review all topics from Phase 1', '{general,reading,writing,speaking,listening}', NOW() - INTERVAL '240 days', NOW()),
(12, 1, 12, 'Phase 1 Assessment', 'Written and oral assessment', '{general,reading,writing,speaking,listening}', NOW() - INTERVAL '240 days', NOW());

-- 7.3 Course Phases for Course 2 (IELTS Foundation B1)
INSERT INTO course_phase (id, course_id, phase_number, name, duration_weeks, learning_focus, sort_order, created_at, updated_at)
VALUES
(4, 2, 1, 'IELTS Introduction & Listening', 5, 'IELTS format, Listening strategies, note-taking skills', 1, NOW() - INTERVAL '235 days', NOW()),
(5, 2, 2, 'Reading & Vocabulary Building', 6, 'Reading techniques, skimming, scanning, academic vocabulary', 2, NOW() - INTERVAL '235 days', NOW()),
(6, 2, 3, 'Speaking & Writing Foundation', 5, 'Speaking parts 1-3, Task 1&2 introduction, essay structure', 3, NOW() - INTERVAL '235 days', NOW());

-- 7.4 Sample Course Sessions for Course 2 Phase 1 (15 sessions: 5 weeks × 3 sessions/week)
INSERT INTO course_session (id, phase_id, sequence_no, topic, student_task, skill_set, created_at, updated_at)
VALUES
(13, 4, 1, 'IELTS Overview & Test Format', 'Understand IELTS structure, scoring system', '{general}', NOW() - INTERVAL '235 days', NOW()),
(14, 4, 2, 'Listening Section 1 - Forms & Details', 'Practice form completion, note-taking', '{listening}', NOW() - INTERVAL '235 days', NOW()),
(15, 4, 3, 'Listening Section 2 - Monologues', 'Practice with guided tours, presentations', '{listening}', NOW() - INTERVAL '235 days', NOW()),
(16, 4, 4, 'Listening Section 3 - Conversations', 'Practice academic discussions', '{listening}', NOW() - INTERVAL '235 days', NOW()),
(17, 4, 5, 'Listening Section 4 - Lectures', 'Practice with academic lectures', '{listening}', NOW() - INTERVAL '235 days', NOW()),
(18, 4, 6, 'Listening Practice Test 1', 'Full listening test with timing', '{listening}', NOW() - INTERVAL '235 days', NOW()),
(19, 4, 7, 'Listening Strategies - Prediction', 'Learn to predict answers', '{listening}', NOW() - INTERVAL '235 days', NOW()),
(20, 4, 8, 'Listening Strategies - Keywords', 'Identify keywords and paraphrasing', '{listening}', NOW() - INTERVAL '235 days', NOW()),
(21, 4, 9, 'Numbers, Dates, Spellings', 'Master number dictation', '{listening}', NOW() - INTERVAL '235 days', NOW()),
(22, 4, 10, 'Multiple Choice Strategies', 'Practice multiple choice questions', '{listening}', NOW() - INTERVAL '235 days', NOW()),
(23, 4, 11, 'Matching and Labeling', 'Practice diagram/map labeling', '{listening}', NOW() - INTERVAL '235 days', NOW()),
(24, 4, 12, 'Sentence Completion', 'Practice sentence/summary completion', '{listening}', NOW() - INTERVAL '235 days', NOW()),
(25, 4, 13, 'Listening Practice Test 2', 'Full listening mock test', '{listening}', NOW() - INTERVAL '235 days', NOW()),
(26, 4, 14, 'Review Common Mistakes', 'Analyze errors, improvement strategies', '{listening}', NOW() - INTERVAL '235 days', NOW()),
(27, 4, 15, 'Phase 1 Listening Assessment', 'Phase assessment and feedback', '{listening}', NOW() - INTERVAL '235 days', NOW());

-- 7.5 Course Phases for Course 7 (Japanese N5)
INSERT INTO course_phase (id, course_id, phase_number, name, duration_weeks, learning_focus, sort_order, created_at, updated_at)
VALUES
(7, 7, 1, 'Hiragana & Basic Grammar', 4, 'Master hiragana, basic particles, simple sentences', 1, NOW() - INTERVAL '210 days', NOW()),
(8, 7, 2, 'Katakana & Everyday Conversations', 4, 'Master katakana, numbers, time, daily conversations', 2, NOW() - INTERVAL '210 days', NOW()),
(9, 7, 3, 'Basic Kanji & Sentence Patterns', 4, 'Learn 50 basic kanji, verb conjugations, sentence patterns', 3, NOW() - INTERVAL '210 days', NOW());

-- 7.6 Sample Course Sessions for Course 7 Phase 1 (12 sessions)
INSERT INTO course_session (id, phase_id, sequence_no, topic, student_task, skill_set, created_at, updated_at)
VALUES
(28, 7, 1, 'Hiragana あ-そ', 'Learn first 25 hiragana characters', '{general,reading,writing}', NOW() - INTERVAL '210 days', NOW()),
(29, 7, 2, 'Hiragana た-ほ', 'Learn next 25 hiragana characters', '{general,reading,writing}', NOW() - INTERVAL '210 days', NOW()),
(30, 7, 3, 'Hiragana ま-ん & Combinations', 'Complete hiragana chart, practice combinations', '{general,reading,writing}', NOW() - INTERVAL '210 days', NOW()),
(31, 7, 4, 'Self-Introduction in Japanese', 'Introduce yourself using hiragana', '{general,speaking,writing}', NOW() - INTERVAL '210 days', NOW()),
(32, 7, 5, 'Particles は・を・に・で', 'Learn basic particles and usage', '{general,writing}', NOW() - INTERVAL '210 days', NOW()),
(33, 7, 6, 'This/That/Which (これ・それ・あれ)', 'Demonstrate objects and ask questions', '{general,speaking}', NOW() - INTERVAL '210 days', NOW()),
(34, 7, 7, 'Location Words and ありま す/います', 'Describe locations and existence', '{general,speaking,writing}', NOW() - INTERVAL '210 days', NOW()),
(35, 7, 8, 'Adjectives - い-adjectives', 'Describe things using い-adjectives', '{general,speaking,writing}', NOW() - INTERVAL '210 days', NOW()),
(36, 7, 9, 'Adjectives - な-adjectives', 'Describe things using な-adjectives', '{general,speaking,writing}', NOW() - INTERVAL '210 days', NOW()),
(37, 7, 10, 'Verb Introduction - Present Tense', 'Learn basic verb conjugation', '{general,writing}', NOW() - INTERVAL '210 days', NOW()),
(38, 7, 11, 'Daily Conversations Practice', 'Practice everyday dialogues', '{general,speaking,listening}', NOW() - INTERVAL '210 days', NOW()),
(39, 7, 12, 'Phase 1 Assessment', 'Hiragana test, grammar quiz, oral test', '{general,reading,writing,speaking,listening}', NOW() - INTERVAL '210 days', NOW());

-- Reset sequences
SELECT setval('course_phase_id_seq', (SELECT MAX(id) FROM course_phase));
SELECT setval('course_session_id_seq', (SELECT MAX(id) FROM course_session));

-- =========================================
-- SECTION 7B: PLO, CLO, MAPPINGS & MATERIALS
-- =========================================

-- 7B.1 Program Learning Outcomes (PLO) for English Subject
INSERT INTO plo (id, subject_id, code, description, created_at, updated_at)
VALUES
(1, 1, 'PLO-ENG-01', 'Demonstrate proficiency in English communication across listening, speaking, reading, and writing skills', NOW() - INTERVAL '250 days', NOW()),
(2, 1, 'PLO-ENG-02', 'Apply critical thinking and analytical skills to interpret and evaluate various types of texts', NOW() - INTERVAL '250 days', NOW()),
(3, 1, 'PLO-ENG-03', 'Communicate effectively in professional and academic contexts using appropriate language and conventions', NOW() - INTERVAL '250 days', NOW()),
(4, 1, 'PLO-ENG-04', 'Demonstrate cultural awareness and sensitivity in cross-cultural communication', NOW() - INTERVAL '250 days', NOW()),
(5, 1, 'PLO-ENG-05', 'Apply English language skills to achieve personal, academic, and professional goals', NOW() - INTERVAL '250 days', NOW());

-- 7B.2 Program Learning Outcomes (PLO) for Japanese Subject
INSERT INTO plo (id, subject_id, code, description, created_at, updated_at)
VALUES
(6, 2, 'PLO-JPN-01', 'Demonstrate proficiency in Japanese language skills including reading, writing, speaking, and listening', NOW() - INTERVAL '220 days', NOW()),
(7, 2, 'PLO-JPN-02', 'Understand and apply Japanese grammar structures and kanji characters appropriately', NOW() - INTERVAL '220 days', NOW()),
(8, 2, 'PLO-JPN-03', 'Communicate effectively in Japanese in daily life and professional situations', NOW() - INTERVAL '220 days', NOW()),
(9, 2, 'PLO-JPN-04', 'Demonstrate understanding of Japanese culture, customs, and social etiquette', NOW() - INTERVAL '220 days', NOW()),
(10, 2, 'PLO-JPN-05', 'Apply Japanese language skills to achieve JLPT certification and career objectives', NOW() - INTERVAL '220 days', NOW());

-- 7B.3 Course Learning Outcomes (CLO) for Course 1 (General English A1)
INSERT INTO clo (id, course_id, code, description, created_at, updated_at)
VALUES
(1, 1, 'CLO-A1-01', 'Use basic greetings and introduce themselves in English', NOW() - INTERVAL '240 days', NOW()),
(2, 1, 'CLO-A1-02', 'Understand and use simple present tense to describe daily routines', NOW() - INTERVAL '240 days', NOW()),
(3, 1, 'CLO-A1-03', 'Ask and answer basic personal questions about name, age, nationality, and occupation', NOW() - INTERVAL '240 days', NOW()),
(4, 1, 'CLO-A1-04', 'Comprehend simple spoken and written texts about familiar topics', NOW() - INTERVAL '240 days', NOW()),
(5, 1, 'CLO-A1-05', 'Write simple sentences about themselves and their surroundings', NOW() - INTERVAL '240 days', NOW());

-- 7B.4 Course Learning Outcomes (CLO) for Course 2 (IELTS Foundation B1)
INSERT INTO clo (id, course_id, code, description, created_at, updated_at)
VALUES
(6, 2, 'CLO-IELTS-B1-01', 'Apply effective listening strategies to understand IELTS listening passages', NOW() - INTERVAL '235 days', NOW()),
(7, 2, 'CLO-IELTS-B1-02', 'Use reading techniques (skimming, scanning) to comprehend IELTS reading passages', NOW() - INTERVAL '235 days', NOW()),
(8, 2, 'CLO-IELTS-B1-03', 'Speak fluently and coherently on familiar topics in IELTS speaking test format', NOW() - INTERVAL '235 days', NOW()),
(9, 2, 'CLO-IELTS-B1-04', 'Write well-structured essays and reports for IELTS Task 1 and Task 2', NOW() - INTERVAL '235 days', NOW()),
(10, 2, 'CLO-IELTS-B1-05', 'Expand academic vocabulary relevant to common IELTS topics', NOW() - INTERVAL '235 days', NOW()),
(11, 2, 'CLO-IELTS-B1-06', 'Manage time effectively during IELTS test sections', NOW() - INTERVAL '235 days', NOW());

-- 7B.5 Course Learning Outcomes (CLO) for Course 7 (Japanese N5)
INSERT INTO clo (id, course_id, code, description, created_at, updated_at)
VALUES
(12, 7, 'CLO-N5-01', 'Read and write hiragana and katakana characters fluently', NOW() - INTERVAL '210 days', NOW()),
(13, 7, 'CLO-N5-02', 'Recognize and use approximately 80 basic kanji characters', NOW() - INTERVAL '210 days', NOW()),
(14, 7, 'CLO-N5-03', 'Understand and use basic Japanese grammar patterns (particles, verb conjugations)', NOW() - INTERVAL '210 days', NOW()),
(15, 7, 'CLO-N5-04', 'Conduct simple conversations about daily life topics in Japanese', NOW() - INTERVAL '210 days', NOW()),
(16, 7, 'CLO-N5-05', 'Comprehend basic Japanese sentences and passages at N5 level', NOW() - INTERVAL '210 days', NOW());

-- 7B.6 PLO-CLO Mappings for English Courses
INSERT INTO plo_clo_mapping (plo_id, clo_id, status)
VALUES
-- General English A1 CLOs → English PLOs
(1, 1, 'active'), (1, 2, 'active'), (1, 3, 'active'), (1, 4, 'active'), (1, 5, 'active'),
(3, 1, 'active'), (3, 3, 'active'),
(5, 2, 'active'), (5, 4, 'active'), (5, 5, 'active'),
-- IELTS B1 CLOs → English PLOs
(1, 6, 'active'), (1, 7, 'active'), (1, 8, 'active'), (1, 9, 'active'),
(2, 7, 'active'), (2, 9, 'active'),
(3, 8, 'active'), (3, 9, 'active'),
(5, 6, 'active'), (5, 10, 'active'), (5, 11, 'active');

-- 7B.7 PLO-CLO Mappings for Japanese Courses
INSERT INTO plo_clo_mapping (plo_id, clo_id, status)
VALUES
-- Japanese N5 CLOs → Japanese PLOs
(6, 12, 'active'), (6, 13, 'active'), (6, 14, 'active'), (6, 15, 'active'), (6, 16, 'active'),
(7, 13, 'active'), (7, 14, 'active'),
(8, 14, 'active'), (8, 15, 'active'),
(10, 12, 'active'), (10, 16, 'active');

-- 7B.8 Course Session CLO Mappings (linking sessions to CLOs)
INSERT INTO course_session_clo_mapping (course_session_id, clo_id, status)
VALUES
-- Course 1 (General English A1) Phase 1 Sessions
(1, 1, 'active'), (1, 3, 'active'),  -- Greetings session
(2, 1, 'active'), (2, 4, 'active'),  -- Alphabet session
(3, 3, 'active'), (3, 4, 'active'),  -- Numbers session
(4, 3, 'active'), (4, 5, 'active'),  -- Personal info session
(5, 2, 'active'), (5, 5, 'active'),  -- Simple present session
(6, 3, 'active'), (6, 4, 'active'),  -- Countries session
(7, 3, 'active'), (7, 5, 'active'),  -- Jobs session
(8, 2, 'active'), (8, 5, 'active'),  -- Regular verbs session
(9, 2, 'active'), (9, 3, 'active'),  -- Daily activities session
(10, 2, 'active'), (10, 3, 'active'), -- Telling time session
-- Course 2 (IELTS B1) Phase 1 Sessions (Listening)
(13, 6, 'active'), (13, 11, 'active'), -- IELTS Overview
(14, 6, 'active'), -- Listening Section 1
(15, 6, 'active'), -- Listening Section 2
(16, 6, 'active'), -- Listening Section 3
(17, 6, 'active'), -- Listening Section 4
(18, 6, 'active'), (18, 11, 'active'), -- Practice Test 1
-- Course 7 (Japanese N5) Phase 1 Sessions
(28, 12, 'active'), (28, 16, 'active'), -- Hiragana 1
(29, 12, 'active'), (29, 16, 'active'), -- Hiragana 2
(30, 12, 'active'), (30, 16, 'active'), -- Hiragana 3
(31, 12, 'active'), (31, 15, 'active'), -- Self-intro
(32, 14, 'active'), -- Particles
(33, 14, 'active'), (33, 15, 'active'), -- This/That/Which
(34, 14, 'active'), (34, 15, 'active'), -- Location words
(35, 14, 'active'), (35, 15, 'active'), -- い-adjectives
(36, 14, 'active'), (36, 15, 'active'), -- な-adjectives
(37, 14, 'active'), (37, 15, 'active'); -- Verb intro

-- 7B.9 Course Materials
INSERT INTO course_material (id, course_id, phase_id, course_session_id, title, url, uploaded_by, created_at, updated_at)
VALUES
-- Materials for Course 1 (General English A1)
(1, 1, 1, NULL, 'General English A1 Course Syllabus', 'https://drive.google.com/file/course1-syllabus.pdf', 6, NOW() - INTERVAL '240 days', NOW()),
(2, 1, 1, NULL, 'Phase 1 Learning Guide', 'https://drive.google.com/file/phase1-guide.pdf', 6, NOW() - INTERVAL '240 days', NOW()),
(3, 1, 1, 1, 'Greetings Vocabulary List', 'https://drive.google.com/file/greetings-vocab.pdf', 6, NOW() - INTERVAL '240 days', NOW()),
(4, 1, 1, 1, 'Self-Introduction Practice Worksheet', 'https://drive.google.com/file/self-intro-worksheet.pdf', 6, NOW() - INTERVAL '240 days', NOW()),
(5, 1, 1, 5, 'Simple Present Tense Grammar Guide', 'https://drive.google.com/file/present-tense-guide.pdf', 6, NOW() - INTERVAL '238 days', NOW()),
(6, 1, 1, 10, 'Telling Time Practice Exercises', 'https://drive.google.com/file/time-exercises.pdf', 6, NOW() - INTERVAL '235 days', NOW()),
-- Materials for Course 2 (IELTS Foundation B1)
(7, 2, 4, NULL, 'IELTS Foundation B1 Course Overview', 'https://drive.google.com/file/ielts-b1-overview.pdf', 6, NOW() - INTERVAL '235 days', NOW()),
(8, 2, 4, 13, 'IELTS Test Format Guide', 'https://drive.google.com/file/ielts-format.pdf', 6, NOW() - INTERVAL '235 days', NOW()),
(9, 2, 4, 14, 'Listening Section 1 Practice Questions', 'https://drive.google.com/file/listening-s1-practice.pdf', 6, NOW() - INTERVAL '234 days', NOW()),
(10, 2, 4, 18, 'Listening Practice Test 1 Audio & Questions', 'https://drive.google.com/file/listening-test1.zip', 6, NOW() - INTERVAL '232 days', NOW()),
(11, 2, 4, NULL, 'IELTS Vocabulary Bank - Common Topics', 'https://drive.google.com/file/ielts-vocab.xlsx', 6, NOW() - INTERVAL '230 days', NOW()),
-- Materials for Course 7 (Japanese N5)
(12, 7, 7, NULL, 'Japanese N5 Course Textbook PDF', 'https://drive.google.com/file/n5-textbook.pdf', 7, NOW() - INTERVAL '210 days', NOW()),
(13, 7, 7, 28, 'Hiragana Chart and Writing Practice', 'https://drive.google.com/file/hiragana-chart.pdf', 7, NOW() - INTERVAL '210 days', NOW()),
(14, 7, 7, 29, 'Hiragana Stroke Order Animation', 'https://youtube.com/watch?v=hiragana-strokes', 7, NOW() - INTERVAL '209 days', NOW()),
(15, 7, 7, 32, 'Japanese Particles Usage Guide', 'https://drive.google.com/file/particles-guide.pdf', 7, NOW() - INTERVAL '208 days', NOW()),
(16, 7, 7, 37, 'N5 Verb Conjugation Tables', 'https://drive.google.com/file/n5-verbs.pdf', 7, NOW() - INTERVAL '205 days', NOW()),
(17, 7, 7, NULL, 'N5 Kanji List with Examples', 'https://drive.google.com/file/n5-kanji-list.xlsx', 7, NOW() - INTERVAL '205 days', NOW());

-- 7B.10 Course Assessments (Templates at course level)
INSERT INTO course_assessment (id, course_id, name, kind, skills, max_score, description, created_at, updated_at)
VALUES
-- Course 1 (General English A1) Assessments
(1, 1, 'Vocabulary Quiz 1', 'quiz', '{general,reading}', 20.00, 'Basic vocabulary test covering greetings and personal information', NOW() - INTERVAL '240 days', NOW()),
(2, 1, 'Grammar Quiz - Simple Present', 'quiz', '{general,writing}', 25.00, 'Quiz on simple present tense usage', NOW() - INTERVAL '240 days', NOW()),
(3, 1, 'Speaking Assessment', 'oral', '{speaking}', 30.00, 'Oral examination on self-introduction and daily conversations', NOW() - INTERVAL '240 days', NOW()),
(4, 1, 'Midterm Written Exam', 'midterm', '{general,reading,writing}', 50.00, 'Comprehensive midterm covering all Phase 1 & 2 materials', NOW() - INTERVAL '240 days', NOW()),
(5, 1, 'Final Written Exam', 'final', '{general,reading,writing,listening}', 100.00, 'Final examination covering entire course content', NOW() - INTERVAL '240 days', NOW()),
(6, 1, 'Class Participation', 'other', '{general}', 20.00, 'Active participation and homework completion', NOW() - INTERVAL '240 days', NOW()),
-- Course 2 (IELTS Foundation B1) Assessments
(7, 2, 'Listening Practice Test 1', 'practice', '{listening}', 40.00, 'IELTS Listening full practice test', NOW() - INTERVAL '235 days', NOW()),
(8, 2, 'Reading Practice Test 1', 'practice', '{reading}', 40.00, 'IELTS Reading full practice test', NOW() - INTERVAL '235 days', NOW()),
(9, 2, 'Speaking Mock Test', 'oral', '{speaking}', 9.00, 'IELTS Speaking test simulation (all 3 parts)', NOW() - INTERVAL '235 days', NOW()),
(10, 2, 'Writing Task 1 Assessment', 'assignment', '{writing}', 25.00, 'IELTS Writing Task 1 assessment', NOW() - INTERVAL '235 days', NOW()),
(11, 2, 'Writing Task 2 Assessment', 'assignment', '{writing}', 25.00, 'IELTS Writing Task 2 essay', NOW() - INTERVAL '235 days', NOW()),
(12, 2, 'Full IELTS Mock Exam', 'final', '{reading,writing,listening,speaking}', 100.00, 'Complete IELTS simulation test (all 4 skills)', NOW() - INTERVAL '235 days', NOW()),
-- Course 7 (Japanese N5) Assessments
(13, 7, 'Hiragana Test', 'quiz', '{reading,writing}', 50.00, 'Reading and writing all hiragana characters', NOW() - INTERVAL '210 days', NOW()),
(14, 7, 'Katakana Test', 'quiz', '{reading,writing}', 50.00, 'Reading and writing all katakana characters', NOW() - INTERVAL '210 days', NOW()),
(15, 7, 'Basic Kanji Quiz', 'quiz', '{reading,writing}', 30.00, 'Recognition and usage of 50 basic kanji', NOW() - INTERVAL '210 days', NOW()),
(16, 7, 'Grammar Test', 'midterm', '{general,reading,writing}', 60.00, 'Comprehensive grammar test on N5 grammar patterns', NOW() - INTERVAL '210 days', NOW()),
(17, 7, 'Speaking Test', 'oral', '{speaking,listening}', 40.00, 'Oral examination on daily conversations', NOW() - INTERVAL '210 days', NOW()),
(18, 7, 'N5 Mock Exam', 'final', '{general,reading,writing,listening}', 100.00, 'Full JLPT N5 simulation exam', NOW() - INTERVAL '210 days', NOW());

-- 7B.11 Course Assessment CLO Mappings
INSERT INTO course_assessment_clo_mapping (course_assessment_id, clo_id, status)
VALUES
-- Course 1 Assessments
(1, 1, 'active'), (1, 3, 'active'), -- Vocab Quiz 1
(2, 2, 'active'), (2, 5, 'active'), -- Grammar Quiz
(3, 1, 'active'), (3, 3, 'active'), -- Speaking
(4, 2, 'active'), (4, 3, 'active'), (4, 4, 'active'), (4, 5, 'active'), -- Midterm
(5, 1, 'active'), (5, 2, 'active'), (5, 3, 'active'), (5, 4, 'active'), (5, 5, 'active'), -- Final
-- Course 2 Assessments
(7, 6, 'active'), (7, 11, 'active'), -- Listening Test
(8, 7, 'active'), (8, 10, 'active'), -- Reading Test
(9, 8, 'active'), -- Speaking Mock
(10, 9, 'active'), (10, 10, 'active'), -- Writing Task 1
(11, 9, 'active'), (11, 10, 'active'), -- Writing Task 2
(12, 6, 'active'), (12, 7, 'active'), (12, 8, 'active'), (12, 9, 'active'), -- Full Mock
-- Course 7 Assessments
(13, 12, 'active'), (13, 16, 'active'), -- Hiragana Test
(14, 12, 'active'), (14, 16, 'active'), -- Katakana Test
(15, 13, 'active'), (15, 16, 'active'), -- Kanji Quiz
(16, 14, 'active'), -- Grammar Test
(17, 14, 'active'), (17, 15, 'active'), -- Speaking Test
(18, 12, 'active'), (18, 13, 'active'), (18, 14, 'active'), (18, 15, 'active'), (18, 16, 'active'); -- N5 Mock

-- Reset sequences
SELECT setval('plo_id_seq', (SELECT MAX(id) FROM plo));
SELECT setval('clo_id_seq', (SELECT MAX(id) FROM clo));
SELECT setval('course_material_id_seq', (SELECT MAX(id) FROM course_material));
SELECT setval('course_assessment_id_seq', (SELECT MAX(id) FROM course_assessment));

-- =========================================
-- SECTION 7C: STUDENT SKILL ASSESSMENTS
-- =========================================
-- Placement tests and skill assessments for students before enrollment
-- This helps determine which class/level is suitable for each student
-- Note: level_id links to level table which already contains subject_id

INSERT INTO replacement_skill_assessment (id, student_id, skill, level_id, score, assessment_date, assessment_type, note, assessed_by, created_at, updated_at)
VALUES
-- English Placement Tests (level_id 1-6 for English A1-C2)
-- Student 1 (S001) - Intermediate level, tested for General English
(1, 1, 'general', 3, 60, CURRENT_DATE - INTERVAL '105 days', 'placement_test', 'Good foundation, recommended for B1 level', 4, NOW() - INTERVAL '105 days', NOW()),
(2, 1, 'reading', 3, 65, CURRENT_DATE - INTERVAL '105 days', 'placement_test', 'Strong reading comprehension', 4, NOW() - INTERVAL '105 days', NOW()),
(3, 1, 'listening', 3, 58, CURRENT_DATE - INTERVAL '105 days', 'placement_test', 'Needs improvement in listening', 4, NOW() - INTERVAL '105 days', NOW()),

-- Student 2 (S002) - Beginner, starting from A1
(4, 2, 'general', 1, 35, CURRENT_DATE - INTERVAL '100 days', 'placement_test', 'Complete beginner, start from A1', 4, NOW() - INTERVAL '100 days', NOW()),

-- Student 3 (S003) - Intermediate, tested for IELTS
(5, 3, 'general', 3, 62, CURRENT_DATE - INTERVAL '98 days', 'placement_test', 'Ready for IELTS B1 preparation', 6, NOW() - INTERVAL '98 days', NOW()),
(6, 3, 'speaking', 3, 55, CURRENT_DATE - INTERVAL '98 days', 'placement_test', 'Speaking needs more practice', 6, NOW() - INTERVAL '98 days', NOW()),
(7, 3, 'writing', 3, 60, CURRENT_DATE - INTERVAL '98 days', 'placement_test', 'Good writing structure', 6, NOW() - INTERVAL '98 days', NOW()),

-- Student 5 (S005) - Advanced level
(8, 5, 'general', 4, 78, CURRENT_DATE - INTERVAL '85 days', 'placement_test', 'Advanced level, suitable for B2/C1 courses', 6, NOW() - INTERVAL '85 days', NOW()),
(9, 5, 'reading', 5, 82, CURRENT_DATE - INTERVAL '85 days', 'placement_test', 'Excellent reading comprehension', 6, NOW() - INTERVAL '85 days', NOW()),
(10, 5, 'listening', 4, 75, CURRENT_DATE - INTERVAL '85 days', 'placement_test', 'Very good listening skills', 6, NOW() - INTERVAL '85 days', NOW()),

-- Student 7 (S007) - Has IELTS certificate
(11, 7, 'general', 3, 55, CURRENT_DATE - INTERVAL '75 days', 'ielts', 'IELTS overall 5.5', 4, NOW() - INTERVAL '75 days', NOW()),
(12, 7, 'reading', 3, 60, CURRENT_DATE - INTERVAL '75 days', 'ielts', 'IELTS Reading 6.0', 4, NOW() - INTERVAL '75 days', NOW()),
(13, 7, 'listening', 3, 55, CURRENT_DATE - INTERVAL '75 days', 'ielts', 'IELTS Listening 5.5', 4, NOW() - INTERVAL '75 days', NOW()),
(14, 7, 'writing', 3, 50, CURRENT_DATE - INTERVAL '75 days', 'ielts', 'IELTS Writing 5.0', 4, NOW() - INTERVAL '75 days', NOW()),
(15, 7, 'speaking', 3, 55, CURRENT_DATE - INTERVAL '75 days', 'ielts', 'IELTS Speaking 5.5', 4, NOW() - INTERVAL '75 days', NOW()),

-- Student 10 (S010) - Advanced with TOEIC
(16, 10, 'general', 4, 80, CURRENT_DATE - INTERVAL '60 days', 'toeic', 'TOEIC 800/990', 6, NOW() - INTERVAL '60 days', NOW()),
(17, 10, 'reading', 4, 85, CURRENT_DATE - INTERVAL '60 days', 'toeic', 'TOEIC Reading 425/495', 6, NOW() - INTERVAL '60 days', NOW()),
(18, 10, 'listening', 4, 75, CURRENT_DATE - INTERVAL '60 days', 'toeic', 'TOEIC Listening 375/495', 6, NOW() - INTERVAL '60 days', NOW()),

-- Student 15 (S015) - Advanced learner
(19, 15, 'general', 5, 85, CURRENT_DATE - INTERVAL '35 days', 'internal_exam', 'Excellent performance in internal test', 6, NOW() - INTERVAL '35 days', NOW()),
(20, 15, 'speaking', 4, 80, CURRENT_DATE - INTERVAL '35 days', 'internal_exam', 'Fluent speaker', 6, NOW() - INTERVAL '35 days', NOW()),

-- Japanese Placement Tests (level_id 7-11 for Japanese N5-N1)
-- Student 20 (S020) - Complete beginner in Japanese
(21, 20, 'general', 7, 20, CURRENT_DATE - INTERVAL '145 days', 'placement_test', 'No prior knowledge, start from N5', 7, NOW() - INTERVAL '145 days', NOW()),

-- Student 24 (S024) - Some Japanese knowledge
(22, 24, 'general', 7, 45, CURRENT_DATE - INTERVAL '140 days', 'placement_test', 'Knows hiragana, can start N5 course', 7, NOW() - INTERVAL '140 days', NOW()),
(23, 24, 'reading', 7, 40, CURRENT_DATE - INTERVAL '140 days', 'placement_test', 'Can read hiragana partially', 7, NOW() - INTERVAL '140 days', NOW()),

-- Student 29 (S029) - Has N5 certificate, ready for N4
(24, 29, 'general', 8, 65, CURRENT_DATE - INTERVAL '50 days', 'jlpt', 'Passed JLPT N5, ready for N4', 7, NOW() - INTERVAL '50 days', NOW()),
(25, 29, 'reading', 8, 70, CURRENT_DATE - INTERVAL '50 days', 'jlpt', 'Good kanji recognition', 7, NOW() - INTERVAL '50 days', NOW()),
(26, 29, 'listening', 8, 60, CURRENT_DATE - INTERVAL '50 days', 'jlpt', 'Listening comprehension adequate', 7, NOW() - INTERVAL '50 days', NOW()),

-- Student 34 (S034) - Advanced Japanese learner
(27, 34, 'general', 9, 72, CURRENT_DATE - INTERVAL '48 days', 'placement_test', 'N3 level, can proceed to N3 prep', 7, NOW() - INTERVAL '48 days', NOW()),
(28, 34, 'speaking', 9, 68, CURRENT_DATE - INTERVAL '48 days', 'placement_test', 'Good conversational skills', 7, NOW() - INTERVAL '48 days', NOW()),
(29, 34, 'writing', 9, 70, CURRENT_DATE - INTERVAL '48 days', 'placement_test', 'Can write intermediate kanji', 7, NOW() - INTERVAL '48 days', NOW()),

-- Student 39 (S039) - High level Japanese
(30, 39, 'general', 10, 78, CURRENT_DATE - INTERVAL '38 days', 'internal_exam', 'Near N2 level', 7, NOW() - INTERVAL '38 days', NOW()),
(31, 39, 'reading', 10, 80, CURRENT_DATE - INTERVAL '38 days', 'internal_exam', 'Strong reading comprehension', 7, NOW() - INTERVAL '38 days', NOW()),

-- Student 43 (S043) - Self-assessed for conversation course
(32, 43, 'general', 9, 65, CURRENT_DATE - INTERVAL '30 days', 'self_assessment', 'Student wants to improve speaking', 7, NOW() - INTERVAL '30 days', NOW()),
(33, 43, 'speaking', 9, 60, CURRENT_DATE - INTERVAL '30 days', 'self_assessment', 'Self-reported intermediate speaking', 7, NOW() - INTERVAL '30 days', NOW()),

-- Additional assessments for other students
-- Student 11 (S011) - Intermediate English
(34, 11, 'general', 3, 58, CURRENT_DATE - INTERVAL '55 days', 'placement_test', 'Intermediate level confirmed', 4, NOW() - INTERVAL '55 days', NOW()),

-- Student 14 (S014) - Intermediate with focus on speaking
(35, 14, 'general', 3, 62, CURRENT_DATE - INTERVAL '40 days', 'placement_test', 'Good overall, weak in speaking', 6, NOW() - INTERVAL '40 days', NOW()),
(36, 14, 'speaking', 2, 45, CURRENT_DATE - INTERVAL '40 days', 'placement_test', 'Speaking at A2 level, needs improvement', 6, NOW() - INTERVAL '40 days', NOW()),

-- Student 18 (S018) - Retake assessment after initial course
(37, 18, 'general', 3, 55, CURRENT_DATE - INTERVAL '28 days', 'placement_test', 'Initial assessment B1 level', 4, NOW() - INTERVAL '28 days', NOW()),
(38, 18, 'general', 3, 68, CURRENT_DATE - INTERVAL '5 days', 'internal_exam', 'Progress check after 3 weeks - improving', 6, NOW() - INTERVAL '5 days', NOW()),

-- Student 21 (S021) - Business English candidate
(39, 21, 'general', 4, 70, CURRENT_DATE - INTERVAL '22 days', 'placement_test', 'Suitable for Business English B2', 6, NOW() - INTERVAL '22 days', NOW()),
(40, 21, 'speaking', 4, 72, CURRENT_DATE - INTERVAL '22 days', 'placement_test', 'Good business communication skills', 6, NOW() - INTERVAL '22 days', NOW()),

-- Student 25 (S025) - Conversational English
(41, 25, 'general', 2, 48, CURRENT_DATE - INTERVAL '15 days', 'placement_test', 'A2 level, suitable for conversation class', 4, NOW() - INTERVAL '15 days', NOW()),
(42, 25, 'speaking', 2, 42, CURRENT_DATE - INTERVAL '15 days', 'placement_test', 'Needs confidence in speaking', 4, NOW() - INTERVAL '15 days', NOW()),

-- Student 35 (S035) - Japanese N4 level
(43, 35, 'general', 8, 62, CURRENT_DATE - INTERVAL '47 days', 'placement_test', 'Ready for N4 course', 7, NOW() - INTERVAL '47 days', NOW()),
(44, 35, 'listening', 8, 58, CURRENT_DATE - INTERVAL '47 days', 'placement_test', 'Listening skills adequate', 7, NOW() - INTERVAL '47 days', NOW());

-- Reset sequence
SELECT setval('replacement_skill_assessment_id_seq', (SELECT MAX(id) FROM replacement_skill_assessment));

-- =========================================
-- SECTION 8: CLASSES & SESSIONS
-- =========================================
-- Creating diverse classes in different statuses with realistic schedules
-- schedule_days: 2=Monday, 3=Tuesday, 4=Wednesday, 5=Thursday, 6=Friday, 7=Saturday, 1=Sunday

-- 8.1 Classes
INSERT INTO "class" (id, branch_id, course_id, code, name, modality, start_date, planned_end_date, actual_end_date, schedule_days, max_capacity, status, created_by, submitted_at, approved_by, approved_at, created_at, updated_at)
VALUES
-- COMPLETED Classes (started and finished in the past)
(1, 1, 1, 'A1-GEN-001', 'General English A1 - Morning Class', 'offline', CURRENT_DATE - INTERVAL '150 days', CURRENT_DATE - INTERVAL '65 days', CURRENT_DATE - INTERVAL '65 days', '{2,4,6}', 15, 'completed', 4, NOW() - INTERVAL '155 days', 3, NOW() - INTERVAL '154 days', NOW() - INTERVAL '156 days', CURRENT_DATE - INTERVAL '65 days'),

(2, 1, 7, 'N5-GEN-001', 'Japanese Beginners N5 - Evening', 'offline', CURRENT_DATE - INTERVAL '140 days', CURRENT_DATE - INTERVAL '55 days', CURRENT_DATE - INTERVAL '56 days', '{2,4,6}', 12, 'completed', 4, NOW() - INTERVAL '145 days', 3, NOW() - INTERVAL '144 days', NOW() - INTERVAL '146 days', CURRENT_DATE - INTERVAL '56 days'),

-- ONGOING Classes (currently in progress)
(3, 1, 2, 'B1-IELTS-001', 'IELTS Foundation B1 - Afternoon', 'offline', CURRENT_DATE - INTERVAL '50 days', CURRENT_DATE + INTERVAL '62 days', NULL, '{2,4,6}', 18, 'ongoing', 4, NOW() - INTERVAL '55 days', 3, NOW() - INTERVAL '54 days', NOW() - INTERVAL '56 days', NOW()),

(4, 1, 3, 'B2-IELTS-001', 'IELTS Intermediate B2 - Evening', 'hybrid', CURRENT_DATE - INTERVAL '60 days', CURRENT_DATE + INTERVAL '66 days', NULL, '{3,5}', 20, 'ongoing', 4, NOW() - INTERVAL '65 days', 3, NOW() - INTERVAL '64 days', NOW() - INTERVAL '66 days', NOW()),

(5, 1, 5, 'A2-CONV-001', 'Conversational English A2 - Weekend', 'offline', CURRENT_DATE - INTERVAL '35 days', CURRENT_DATE + INTERVAL '35 days', NULL, '{7,1}', 10, 'ongoing', 5, NOW() - INTERVAL '40 days', 3, NOW() - INTERVAL '39 days', NOW() - INTERVAL '41 days', NOW()),

(6, 1, 8, 'N4-GEN-001', 'Japanese Elementary N4 - Morning', 'offline', CURRENT_DATE - INTERVAL '45 days', CURRENT_DATE + INTERVAL '53 days', NULL, '{2,4,6}', 15, 'ongoing', 4, NOW() - INTERVAL '50 days', 3, NOW() - INTERVAL '49 days', NOW() - INTERVAL '51 days', NOW()),

-- SCHEDULED Classes (approved but not started yet)
(7, 1, 4, 'B2-BUS-001', 'Business English B2 - Evening', 'offline', CURRENT_DATE + INTERVAL '14 days', CURRENT_DATE + INTERVAL '112 days', NULL, '{3,5}', 16, 'scheduled', 5, NOW() - INTERVAL '10 days', 3, NOW() - INTERVAL '9 days', NOW() - INTERVAL '11 days', NOW()),

(8, 1, 6, 'B2-TOEIC-001', 'TOEIC Preparation - Weekend', 'offline', CURRENT_DATE + INTERVAL '21 days', CURRENT_DATE + INTERVAL '105 days', NULL, '{7}', 20, 'scheduled', 4, NOW() - INTERVAL '7 days', 3, NOW() - INTERVAL '6 days', NOW() - INTERVAL '8 days', NOW()),

(9, 1, 1, 'A1-GEN-002', 'General English A1 - Afternoon', 'online', CURRENT_DATE + INTERVAL '7 days', CURRENT_DATE + INTERVAL '91 days', NULL, '{2,4,6}', 25, 'scheduled', 4, NOW() - INTERVAL '5 days', 3, NOW() - INTERVAL '4 days', NOW() - INTERVAL '6 days', NOW()),

(10, 1, 9, 'N3-PREP-001', 'JLPT N3 Preparation - Evening', 'hybrid', CURRENT_DATE + INTERVAL '28 days', CURRENT_DATE + INTERVAL '140 days', NULL, '{3,5,7}', 18, 'scheduled', 5, NOW() - INTERVAL '3 days', 3, NOW() - INTERVAL '2 days', NOW() - INTERVAL '4 days', NOW()),

-- DRAFT Classes (created but not yet submitted/approved)
(11, 1, 11, 'N3-CONV-001', 'Japanese Conversation N3 - Morning', 'offline', CURRENT_DATE + INTERVAL '35 days', CURRENT_DATE + INTERVAL '119 days', NULL, '{2,4,6}', 12, 'draft', 5, NULL, NULL, NULL, NOW() - INTERVAL '2 days', NOW()),

(12, 1, 12, 'N2-BUS-001', 'Business Japanese N2 - Evening', 'offline', CURRENT_DATE + INTERVAL '42 days', CURRENT_DATE + INTERVAL '140 days', NULL, '{3,5}', 14, 'draft', 4, NULL, NULL, NULL, NOW() - INTERVAL '1 day', NOW()),

-- ADDITIONAL CLASSES FOR TRANSFER TESTING
-- More General English A1 classes (course_id = 1) - different modalities
(13, 1, 1, 'A1-GEN-003', 'General English A1 - Evening Online', 'online', CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE + INTERVAL '54 days', NULL, '{2,4,6}', 20, 'ongoing', 4, NOW() - INTERVAL '35 days', 3, NOW() - INTERVAL '34 days', NOW() - INTERVAL '36 days', NOW()),

(14, 1, 1, 'A1-GEN-004', 'General English A1 - Morning Offline', 'offline', CURRENT_DATE + INTERVAL '14 days', CURRENT_DATE + INTERVAL '98 days', NULL, '{3,5}', 15, 'scheduled', 4, NOW() - INTERVAL '8 days', 3, NOW() - INTERVAL '7 days', NOW() - INTERVAL '9 days', NOW()),

(15, 1, 1, 'A1-GEN-005', 'General English A1 - Weekend Online', 'online', CURRENT_DATE + INTERVAL '21 days', CURRENT_DATE + INTERVAL '105 days', NULL, '{7,1}', 25, 'scheduled', 5, NOW() - INTERVAL '6 days', 3, NOW() - INTERVAL '5 days', NOW() - INTERVAL '7 days', NOW()),

-- More IELTS Foundation B1 classes (course_id = 2) - for same course transfer
(16, 1, 2, 'B1-IELTS-002', 'IELTS Foundation B1 - Morning Online', 'online', CURRENT_DATE - INTERVAL '40 days', CURRENT_DATE + INTERVAL '72 days', NULL, '{2,4,6}', 20, 'ongoing', 4, NOW() - INTERVAL '45 days', 3, NOW() - INTERVAL '44 days', NOW() - INTERVAL '46 days', NOW()),

(17, 1, 2, 'B1-IELTS-003', 'IELTS Foundation B1 - Evening Offline', 'offline', CURRENT_DATE + INTERVAL '10 days', CURRENT_DATE + INTERVAL '122 days', NULL, '{3,5}', 18, 'scheduled', 4, NOW() - INTERVAL '5 days', 3, NOW() - INTERVAL '4 days', NOW() - INTERVAL '6 days', NOW()),

(18, 1, 2, 'B1-IELTS-004', 'IELTS Foundation B1 - Weekend Hybrid', 'hybrid', CURRENT_DATE + INTERVAL '28 days', CURRENT_DATE + INTERVAL '140 days', NULL, '{7}', 16, 'scheduled', 5, NOW() - INTERVAL '3 days', 3, NOW() - INTERVAL '2 days', NOW() - INTERVAL '4 days', NOW()),

-- More IELTS B2 classes (course_id = 3) - for different course transfer (progression)
(19, 1, 3, 'B2-IELTS-002', 'IELTS Intermediate B2 - Morning Online', 'online', CURRENT_DATE - INTERVAL '45 days', CURRENT_DATE + INTERVAL '81 days', NULL, '{2,4,6}', 20, 'ongoing', 4, NOW() - INTERVAL '50 days', 3, NOW() - INTERVAL '49 days', NOW() - INTERVAL '51 days', NOW()),

(20, 1, 3, 'B2-IELTS-003', 'IELTS Intermediate B2 - Afternoon Offline', 'offline', CURRENT_DATE + INTERVAL '14 days', CURRENT_DATE + INTERVAL '140 days', NULL, '{3,5}', 18, 'scheduled', 4, NOW() - INTERVAL '7 days', 3, NOW() - INTERVAL '6 days', NOW() - INTERVAL '8 days', NOW()),

-- More Conversational A2 classes (course_id = 5) - for different course transfer
(21, 1, 5, 'A2-CONV-002', 'Conversational English A2 - Evening Online', 'online', CURRENT_DATE - INTERVAL '25 days', CURRENT_DATE + INTERVAL '45 days', NULL, '{2,4,6}', 15, 'ongoing', 5, NOW() - INTERVAL '30 days', 3, NOW() - INTERVAL '29 days', NOW() - INTERVAL '31 days', NOW()),

(22, 1, 5, 'A2-CONV-003', 'Conversational English A2 - Morning Offline', 'offline', CURRENT_DATE + INTERVAL '20 days', CURRENT_DATE + INTERVAL '90 days', NULL, '{3,5}', 12, 'scheduled', 5, NOW() - INTERVAL '5 days', 3, NOW() - INTERVAL '4 days', NOW() - INTERVAL '6 days', NOW());

-- 8.2 Sessions for COMPLETED Class 1 (General English A1 - completed 12 weeks ago)
-- Schedule: Monday, Wednesday, Friday (days 2,4,6), Total: 36 sessions (12 weeks × 3/week)
INSERT INTO session (id, class_id, course_session_id, time_slot_template_id, date, type, status, teacher_note, created_at, updated_at)
VALUES
-- Week 1 of Class 1 (Phase 1 - Foundation)
(1, 1, 1, 1, CURRENT_DATE - INTERVAL '150 days', 'class', 'done', 'Great start, students are engaged', NOW() - INTERVAL '150 days', CURRENT_DATE - INTERVAL '149 days'),
(2, 1, 2, 1, CURRENT_DATE - INTERVAL '148 days', 'class', 'done', 'Pronunciation practice went well', NOW() - INTERVAL '148 days', CURRENT_DATE - INTERVAL '147 days'),
(3, 1, 3, 1, CURRENT_DATE - INTERVAL '146 days', 'class', 'done', NULL, NOW() - INTERVAL '146 days', CURRENT_DATE - INTERVAL '145 days'),
-- Week 2
(4, 1, 4, 1, CURRENT_DATE - INTERVAL '143 days', 'class', 'done', NULL, NOW() - INTERVAL '143 days', CURRENT_DATE - INTERVAL '142 days'),
(5, 1, 5, 1, CURRENT_DATE - INTERVAL '141 days', 'class', 'done', 'Need more practice on verb to be', NOW() - INTERVAL '141 days', CURRENT_DATE - INTERVAL '140 days'),
(6, 1, 6, 1, CURRENT_DATE - INTERVAL '139 days', 'class', 'done', NULL, NOW() - INTERVAL '139 days', CURRENT_DATE - INTERVAL '138 days'),
-- Week 3
(7, 1, 7, 1, CURRENT_DATE - INTERVAL '136 days', 'class', 'done', NULL, NOW() - INTERVAL '136 days', CURRENT_DATE - INTERVAL '135 days'),
(8, 1, 8, 1, CURRENT_DATE - INTERVAL '134 days', 'class', 'done', NULL, NOW() - INTERVAL '134 days', CURRENT_DATE - INTERVAL '133 days'),
(9, 1, 9, 1, CURRENT_DATE - INTERVAL '132 days', 'class', 'done', NULL, NOW() - INTERVAL '132 days', CURRENT_DATE - INTERVAL '131 days'),
-- Week 4
(10, 1, 10, 1, CURRENT_DATE - INTERVAL '129 days', 'class', 'done', NULL, NOW() - INTERVAL '129 days', CURRENT_DATE - INTERVAL '128 days'),
(11, 1, 11, 1, CURRENT_DATE - INTERVAL '127 days', 'class', 'done', NULL, NOW() - INTERVAL '127 days', CURRENT_DATE - INTERVAL '126 days'),
(12, 1, 12, 1, CURRENT_DATE - INTERVAL '125 days', 'class', 'done', NULL, NOW() - INTERVAL '125 days', CURRENT_DATE - INTERVAL '124 days'),
-- Week 5
(13, 1, 1, 1, CURRENT_DATE - INTERVAL '122 days', 'class', 'done', NULL, NOW() - INTERVAL '122 days', CURRENT_DATE - INTERVAL '121 days'),
(14, 1, 2, 1, CURRENT_DATE - INTERVAL '120 days', 'class', 'done', NULL, NOW() - INTERVAL '120 days', CURRENT_DATE - INTERVAL '119 days'),
(15, 1, 3, 1, CURRENT_DATE - INTERVAL '118 days', 'class', 'done', NULL, NOW() - INTERVAL '118 days', CURRENT_DATE - INTERVAL '117 days'),
-- Week 6
(16, 1, 4, 1, CURRENT_DATE - INTERVAL '115 days', 'class', 'done', NULL, NOW() - INTERVAL '115 days', CURRENT_DATE - INTERVAL '114 days'),
(17, 1, 5, 1, CURRENT_DATE - INTERVAL '113 days', 'class', 'done', NULL, NOW() - INTERVAL '113 days', CURRENT_DATE - INTERVAL '112 days'),
(18, 1, 6, 1, CURRENT_DATE - INTERVAL '111 days', 'class', 'done', NULL, NOW() - INTERVAL '111 days', CURRENT_DATE - INTERVAL '110 days'),
-- Week 7
(19, 1, 7, 1, CURRENT_DATE - INTERVAL '108 days', 'class', 'done', NULL, NOW() - INTERVAL '108 days', CURRENT_DATE - INTERVAL '107 days'),
(20, 1, 8, 1, CURRENT_DATE - INTERVAL '106 days', 'class', 'done', NULL, NOW() - INTERVAL '106 days', CURRENT_DATE - INTERVAL '105 days'),
(21, 1, 9, 1, CURRENT_DATE - INTERVAL '104 days', 'class', 'done', NULL, NOW() - INTERVAL '104 days', CURRENT_DATE - INTERVAL '103 days'),
-- Week 8
(22, 1, 10, 1, CURRENT_DATE - INTERVAL '101 days', 'class', 'done', NULL, NOW() - INTERVAL '101 days', CURRENT_DATE - INTERVAL '100 days'),
(23, 1, 11, 1, CURRENT_DATE - INTERVAL '99 days', 'class', 'done', NULL, NOW() - INTERVAL '99 days', CURRENT_DATE - INTERVAL '98 days'),
(24, 1, 12, 1, CURRENT_DATE - INTERVAL '97 days', 'class', 'done', NULL, NOW() - INTERVAL '97 days', CURRENT_DATE - INTERVAL '96 days'),
-- Week 9
(25, 1, 1, 1, CURRENT_DATE - INTERVAL '94 days', 'class', 'done', NULL, NOW() - INTERVAL '94 days', CURRENT_DATE - INTERVAL '93 days'),
(26, 1, 2, 1, CURRENT_DATE - INTERVAL '92 days', 'class', 'done', NULL, NOW() - INTERVAL '92 days', CURRENT_DATE - INTERVAL '91 days'),
(27, 1, 3, 1, CURRENT_DATE - INTERVAL '90 days', 'class', 'done', NULL, NOW() - INTERVAL '90 days', CURRENT_DATE - INTERVAL '89 days'),
-- Week 10
(28, 1, 4, 1, CURRENT_DATE - INTERVAL '87 days', 'class', 'done', NULL, NOW() - INTERVAL '87 days', CURRENT_DATE - INTERVAL '86 days'),
(29, 1, 5, 1, CURRENT_DATE - INTERVAL '85 days', 'class', 'done', NULL, NOW() - INTERVAL '85 days', CURRENT_DATE - INTERVAL '84 days'),
(30, 1, 6, 1, CURRENT_DATE - INTERVAL '83 days', 'class', 'done', NULL, NOW() - INTERVAL '83 days', CURRENT_DATE - INTERVAL '82 days'),
-- Week 11
(31, 1, 7, 1, CURRENT_DATE - INTERVAL '80 days', 'class', 'done', NULL, NOW() - INTERVAL '80 days', CURRENT_DATE - INTERVAL '79 days'),
(32, 1, 8, 1, CURRENT_DATE - INTERVAL '78 days', 'class', 'done', NULL, NOW() - INTERVAL '78 days', CURRENT_DATE - INTERVAL '77 days'),
(33, 1, 9, 1, CURRENT_DATE - INTERVAL '76 days', 'class', 'done', NULL, NOW() - INTERVAL '76 days', CURRENT_DATE - INTERVAL '75 days'),
-- Week 12 (final week of course) - Sessions 34-36
(34, 1, 10, 1, CURRENT_DATE - INTERVAL '73 days', 'class', 'done', NULL, NOW() - INTERVAL '73 days', CURRENT_DATE - INTERVAL '72 days'),
(35, 1, 11, 1, CURRENT_DATE - INTERVAL '71 days', 'class', 'done', 'Good review session, students ready for assessment', NOW() - INTERVAL '71 days', CURRENT_DATE - INTERVAL '70 days'),
(36, 1, 12, 1, CURRENT_DATE - INTERVAL '69 days', 'class', 'done', 'Final assessment completed successfully', NOW() - INTERVAL '69 days', CURRENT_DATE - INTERVAL '68 days');

-- 8.3 Sessions for ONGOING Class 3 (IELTS Foundation B1)
-- Schedule: Monday, Wednesday, Friday, Total: 48 sessions (16 weeks × 3/week)
-- Started 50 days ago, currently at session ~18-20 (about week 7)
INSERT INTO session (id, class_id, course_session_id, time_slot_template_id, date, type, status, teacher_note, created_at, updated_at)
VALUES
-- Past sessions (done)
(37, 3, 13, 4, CURRENT_DATE - INTERVAL '50 days', 'class', 'done', 'Students enthusiastic about IELTS', NOW() - INTERVAL '50 days', CURRENT_DATE - INTERVAL '49 days'),
(38, 3, 14, 4, CURRENT_DATE - INTERVAL '48 days', 'class', 'done', NULL, NOW() - INTERVAL '48 days', CURRENT_DATE - INTERVAL '47 days'),
(39, 3, 15, 4, CURRENT_DATE - INTERVAL '46 days', 'class', 'done', NULL, NOW() - INTERVAL '46 days', CURRENT_DATE - INTERVAL '45 days'),
(40, 3, 16, 4, CURRENT_DATE - INTERVAL '43 days', 'class', 'done', NULL, NOW() - INTERVAL '43 days', CURRENT_DATE - INTERVAL '42 days'),
(41, 3, 17, 4, CURRENT_DATE - INTERVAL '41 days', 'class', 'done', NULL, NOW() - INTERVAL '41 days', CURRENT_DATE - INTERVAL '40 days'),
(42, 3, 18, 4, CURRENT_DATE - INTERVAL '39 days', 'class', 'done', 'First mock test - good progress', NOW() - INTERVAL '39 days', CURRENT_DATE - INTERVAL '38 days'),
(43, 3, 19, 4, CURRENT_DATE - INTERVAL '36 days', 'class', 'done', NULL, NOW() - INTERVAL '36 days', CURRENT_DATE - INTERVAL '35 days'),
(44, 3, 20, 4, CURRENT_DATE - INTERVAL '34 days', 'class', 'done', NULL, NOW() - INTERVAL '34 days', CURRENT_DATE - INTERVAL '33 days'),
(45, 3, 21, 4, CURRENT_DATE - INTERVAL '32 days', 'class', 'done', NULL, NOW() - INTERVAL '32 days', CURRENT_DATE - INTERVAL '31 days'),
(46, 3, 22, 4, CURRENT_DATE - INTERVAL '29 days', 'class', 'done', NULL, NOW() - INTERVAL '29 days', CURRENT_DATE - INTERVAL '28 days'),
(47, 3, 23, 4, CURRENT_DATE - INTERVAL '27 days', 'class', 'done', NULL, NOW() - INTERVAL '27 days', CURRENT_DATE - INTERVAL '26 days'),
(48, 3, 24, 4, CURRENT_DATE - INTERVAL '25 days', 'class', 'done', NULL, NOW() - INTERVAL '25 days', CURRENT_DATE - INTERVAL '24 days'),
(49, 3, 25, 4, CURRENT_DATE - INTERVAL '22 days', 'class', 'done', NULL, NOW() - INTERVAL '22 days', CURRENT_DATE - INTERVAL '21 days'),
(50, 3, 26, 4, CURRENT_DATE - INTERVAL '20 days', 'class', 'done', NULL, NOW() - INTERVAL '20 days', CURRENT_DATE - INTERVAL '19 days'),
(51, 3, 27, 4, CURRENT_DATE - INTERVAL '18 days', 'class', 'done', 'Phase 1 completed well', NOW() - INTERVAL '18 days', CURRENT_DATE - INTERVAL '17 days'),
-- Recent/today (some done, one in progress)
(52, 3, 13, 4, CURRENT_DATE - INTERVAL '15 days', 'class', 'done', 'Starting Phase 2 - Reading focus', NOW() - INTERVAL '15 days', CURRENT_DATE - INTERVAL '14 days'),
(53, 3, 14, 4, CURRENT_DATE - INTERVAL '13 days', 'class', 'done', NULL, NOW() - INTERVAL '13 days', CURRENT_DATE - INTERVAL '12 days'),
(54, 3, 15, 4, CURRENT_DATE - INTERVAL '11 days', 'class', 'done', NULL, NOW() - INTERVAL '11 days', CURRENT_DATE - INTERVAL '10 days'),
(55, 3, 16, 4, CURRENT_DATE - INTERVAL '8 days', 'class', 'done', NULL, NOW() - INTERVAL '8 days', CURRENT_DATE - INTERVAL '7 days'),
(56, 3, 17, 4, CURRENT_DATE - INTERVAL '6 days', 'class', 'done', NULL, NOW() - INTERVAL '6 days', CURRENT_DATE - INTERVAL '5 days'),
(57, 3, 18, 4, CURRENT_DATE - INTERVAL '4 days', 'class', 'done', NULL, NOW() - INTERVAL '4 days', CURRENT_DATE - INTERVAL '3 days'),
(58, 3, 19, 4, CURRENT_DATE - INTERVAL '1 day', 'class', 'done', NULL, NOW() - INTERVAL '1 day', NOW()),
-- Future sessions (planned)
(59, 3, 20, 4, CURRENT_DATE + INTERVAL '1 day', 'class', 'planned', NULL, NOW() - INTERVAL '50 days', NOW()),
(60, 3, 21, 4, CURRENT_DATE + INTERVAL '3 days', 'class', 'planned', NULL, NOW() - INTERVAL '50 days', NOW()),
(61, 3, 22, 4, CURRENT_DATE + INTERVAL '6 days', 'class', 'planned', NULL, NOW() - INTERVAL '50 days', NOW());

-- 8.4 Sessions for ONGOING Class 4 (IELTS B2 - Hybrid with Tuesday/Thursday schedule)
-- Schedule: Tuesday, Thursday (days 3,5), Total: 36 sessions (18 weeks × 2/week)
-- Started 60 days ago, currently at session ~17 (about week 9)
INSERT INTO session (id, class_id, course_session_id, time_slot_template_id, date, type, status, teacher_note, created_at, updated_at)
VALUES
-- Past sessions (sample - showing recent history)
(62, 4, 13, 7, CURRENT_DATE - INTERVAL '21 days', 'class', 'done', NULL, NOW() - INTERVAL '60 days', CURRENT_DATE - INTERVAL '20 days'),
(63, 4, 14, 7, CURRENT_DATE - INTERVAL '19 days', 'class', 'done', NULL, NOW() - INTERVAL '60 days', CURRENT_DATE - INTERVAL '18 days'),
(64, 4, 15, 7, CURRENT_DATE - INTERVAL '14 days', 'class', 'done', NULL, NOW() - INTERVAL '60 days', CURRENT_DATE - INTERVAL '13 days'),
(65, 4, 16, 7, CURRENT_DATE - INTERVAL '12 days', 'class', 'done', NULL, NOW() - INTERVAL '60 days', CURRENT_DATE - INTERVAL '11 days'),
(66, 4, 17, 7, CURRENT_DATE - INTERVAL '7 days', 'class', 'done', NULL, NOW() - INTERVAL '60 days', CURRENT_DATE - INTERVAL '6 days'),
(67, 4, 18, 7, CURRENT_DATE - INTERVAL '5 days', 'class', 'done', 'Hybrid session worked well', NOW() - INTERVAL '60 days', CURRENT_DATE - INTERVAL '4 days'),
-- Current week
(68, 4, 19, 7, CURRENT_DATE, 'class', 'planned', NULL, NOW() - INTERVAL '60 days', NOW()),
(69, 4, 20, 7, CURRENT_DATE + INTERVAL '2 days', 'class', 'planned', NULL, NOW() - INTERVAL '60 days', NOW()),
-- Future sessions
(70, 4, 21, 7, CURRENT_DATE + INTERVAL '7 days', 'class', 'planned', NULL, NOW() - INTERVAL '60 days', NOW()),
(71, 4, 22, 7, CURRENT_DATE + INTERVAL '9 days', 'class', 'planned', NULL, NOW() - INTERVAL '60 days', NOW());

-- 8.5 Sessions for ONGOING Class 5 (Conversational A2 - Weekend class)
-- Schedule: Saturday, Sunday (days 7,1), Total: 30 sessions (10 weeks × 3/week - but weekends so ~15 weeks)
-- Started 35 days ago, currently at session ~7
INSERT INTO session (id, class_id, course_session_id, time_slot_template_id, date, type, status, teacher_note, created_at, updated_at)
VALUES
(72, 5, 1, 9, CURRENT_DATE - INTERVAL '35 days', 'class', 'done', 'Weekend students very motivated', NOW() - INTERVAL '35 days', CURRENT_DATE - INTERVAL '34 days'),
(73, 5, 2, 9, CURRENT_DATE - INTERVAL '34 days', 'class', 'done', NULL, NOW() - INTERVAL '35 days', CURRENT_DATE - INTERVAL '33 days'),
(74, 5, 3, 9, CURRENT_DATE - INTERVAL '28 days', 'class', 'done', NULL, NOW() - INTERVAL '35 days', CURRENT_DATE - INTERVAL '27 days'),
(75, 5, 4, 9, CURRENT_DATE - INTERVAL '27 days', 'class', 'done', NULL, NOW() - INTERVAL '35 days', CURRENT_DATE - INTERVAL '26 days'),
(76, 5, 5, 9, CURRENT_DATE - INTERVAL '21 days', 'class', 'done', NULL, NOW() - INTERVAL '35 days', CURRENT_DATE - INTERVAL '20 days'),
(77, 5, 6, 9, CURRENT_DATE - INTERVAL '20 days', 'class', 'done', NULL, NOW() - INTERVAL '35 days', CURRENT_DATE - INTERVAL '19 days'),
(78, 5, 7, 9, CURRENT_DATE - INTERVAL '14 days', 'class', 'done', NULL, NOW() - INTERVAL '35 days', CURRENT_DATE - INTERVAL '13 days'),
(79, 5, 8, 9, CURRENT_DATE - INTERVAL '13 days', 'class', 'done', NULL, NOW() - INTERVAL '35 days', CURRENT_DATE - INTERVAL '12 days'),
(80, 5, 9, 9, CURRENT_DATE - INTERVAL '7 days', 'class', 'done', NULL, NOW() - INTERVAL '35 days', CURRENT_DATE - INTERVAL '6 days'),
(81, 5, 10, 9, CURRENT_DATE - INTERVAL '6 days', 'class', 'done', NULL, NOW() - INTERVAL '35 days', CURRENT_DATE - INTERVAL '5 days'),
-- This weekend
(82, 5, 11, 9, CURRENT_DATE, 'class', 'planned', NULL, NOW() - INTERVAL '35 days', NOW()),
(83, 5, 1, 9, CURRENT_DATE + INTERVAL '1 day', 'class', 'planned', NULL, NOW() - INTERVAL '35 days', NOW());

-- Reset sequences
SELECT setval('class_id_seq', (SELECT MAX(id) FROM "class"));
SELECT setval('session_id_seq', (SELECT MAX(id) FROM session));

-- =========================================
-- SECTION 9: ENROLLMENTS & STUDENT SESSIONS
-- =========================================

-- 9.1 Enrollments for COMPLETED Class 1 (12 students completed)
INSERT INTO enrollment (id, class_id, student_id, status, enrolled_at, left_at, join_session_id, left_session_id, created_at, updated_at)
VALUES
(1, 1, 1, 'completed', NOW() - INTERVAL '155 days', CURRENT_DATE - INTERVAL '65 days', 1, 36, NOW() - INTERVAL '155 days', CURRENT_DATE - INTERVAL '65 days'),
(2, 1, 2, 'completed', NOW() - INTERVAL '155 days', CURRENT_DATE - INTERVAL '65 days', 1, 36, NOW() - INTERVAL '155 days', CURRENT_DATE - INTERVAL '65 days'),
(3, 1, 3, 'completed', NOW() - INTERVAL '155 days', CURRENT_DATE - INTERVAL '65 days', 1, 36, NOW() - INTERVAL '155 days', CURRENT_DATE - INTERVAL '65 days'),
(4, 1, 4, 'dropped', NOW() - INTERVAL '155 days', CURRENT_DATE - INTERVAL '120 days', 1, 15, NOW() - INTERVAL '155 days', CURRENT_DATE - INTERVAL '120 days'),  -- Dropped mid-course
(5, 1, 5, 'completed', NOW() - INTERVAL '155 days', CURRENT_DATE - INTERVAL '65 days', 1, 36, NOW() - INTERVAL '155 days', CURRENT_DATE - INTERVAL '65 days'),
(6, 1, 6, 'completed', NOW() - INTERVAL '155 days', CURRENT_DATE - INTERVAL '65 days', 1, 36, NOW() - INTERVAL '155 days', CURRENT_DATE - INTERVAL '65 days'),
(7, 1, 7, 'completed', NOW() - INTERVAL '150 days', CURRENT_DATE - INTERVAL '65 days', 3, 36, NOW() - INTERVAL '150 days', CURRENT_DATE - INTERVAL '65 days'), -- Late enrollment
(8, 1, 8, 'completed', NOW() - INTERVAL '155 days', CURRENT_DATE - INTERVAL '65 days', 1, 36, NOW() - INTERVAL '155 days', CURRENT_DATE - INTERVAL '65 days'),
(9, 1, 9, 'completed', NOW() - INTERVAL '155 days', CURRENT_DATE - INTERVAL '65 days', 1, 36, NOW() - INTERVAL '155 days', CURRENT_DATE - INTERVAL '65 days'),
(10, 1, 10, 'completed', NOW() - INTERVAL '155 days', CURRENT_DATE - INTERVAL '65 days', 1, 36, NOW() - INTERVAL '155 days', CURRENT_DATE - INTERVAL '65 days'),
(11, 1, 11, 'completed', NOW() - INTERVAL '155 days', CURRENT_DATE - INTERVAL '65 days', 1, 36, NOW() - INTERVAL '155 days', CURRENT_DATE - INTERVAL '65 days'),
(12, 1, 12, 'completed', NOW() - INTERVAL '155 days', CURRENT_DATE - INTERVAL '65 days', 1, 36, NOW() - INTERVAL '155 days', CURRENT_DATE - INTERVAL '65 days');

-- 9.2 Enrollments for ONGOING Class 3 (IELTS B1 - 16 students)
INSERT INTO enrollment (id, class_id, student_id, status, enrolled_at, join_session_id, created_at, updated_at)
VALUES
(13, 3, 13, 'enrolled', NOW() - INTERVAL '55 days', 37, NOW() - INTERVAL '55 days', NOW()),
(14, 3, 14, 'enrolled', NOW() - INTERVAL '55 days', 37, NOW() - INTERVAL '55 days', NOW()),
(15, 3, 15, 'enrolled', NOW() - INTERVAL '55 days', 37, NOW() - INTERVAL '55 days', NOW()),
(16, 3, 16, 'enrolled', NOW() - INTERVAL '55 days', 37, NOW() - INTERVAL '55 days', NOW()),
(17, 3, 17, 'enrolled', NOW() - INTERVAL '55 days', 37, NOW() - INTERVAL '55 days', NOW()),
(18, 3, 18, 'enrolled', NOW() - INTERVAL '55 days', 37, NOW() - INTERVAL '55 days', NOW()),
(19, 3, 19, 'enrolled', NOW() - INTERVAL '55 days', 37, NOW() - INTERVAL '55 days', NOW()),
(20, 3, 20, 'enrolled', NOW() - INTERVAL '55 days', 37, NOW() - INTERVAL '55 days', NOW()),
(21, 3, 21, 'enrolled', NOW() - INTERVAL '45 days', 40, NOW() - INTERVAL '45 days', NOW()), -- Late enrollment
(22, 3, 22, 'enrolled', NOW() - INTERVAL '55 days', 37, NOW() - INTERVAL '55 days', NOW()),
(23, 3, 23, 'enrolled', NOW() - INTERVAL '55 days', 37, NOW() - INTERVAL '55 days', NOW()),
(24, 3, 24, 'enrolled', NOW() - INTERVAL '55 days', 37, NOW() - INTERVAL '55 days', NOW()),
(25, 3, 25, 'enrolled', NOW() - INTERVAL '55 days', 37, NOW() - INTERVAL '55 days', NOW()),
(26, 3, 26, 'enrolled', NOW() - INTERVAL '55 days', 37, NOW() - INTERVAL '55 days', NOW()),
(27, 3, 27, 'enrolled', NOW() - INTERVAL '55 days', 37, NOW() - INTERVAL '55 days', NOW()),
(28, 3, 28, 'enrolled', NOW() - INTERVAL '55 days', 37, NOW() - INTERVAL '55 days', NOW());

-- 9.3 Enrollments for ONGOING Class 4 (IELTS B2 - 14 students)
INSERT INTO enrollment (id, class_id, student_id, status, enrolled_at, join_session_id, created_at, updated_at)
VALUES
(29, 4, 29, 'enrolled', NOW() - INTERVAL '65 days', 62, NOW() - INTERVAL '65 days', NOW()),
(30, 4, 30, 'enrolled', NOW() - INTERVAL '65 days', 62, NOW() - INTERVAL '65 days', NOW()),
(31, 4, 31, 'enrolled', NOW() - INTERVAL '65 days', 62, NOW() - INTERVAL '65 days', NOW()),
(32, 4, 32, 'enrolled', NOW() - INTERVAL '65 days', 62, NOW() - INTERVAL '65 days', NOW()),
(33, 4, 33, 'enrolled', NOW() - INTERVAL '65 days', 62, NOW() - INTERVAL '65 days', NOW()),
(34, 4, 34, 'enrolled', NOW() - INTERVAL '65 days', 62, NOW() - INTERVAL '65 days', NOW()),
(35, 4, 35, 'enrolled', NOW() - INTERVAL '65 days', 62, NOW() - INTERVAL '65 days', NOW()),
(36, 4, 36, 'enrolled', NOW() - INTERVAL '65 days', 62, NOW() - INTERVAL '65 days', NOW()),
(37, 4, 37, 'enrolled', NOW() - INTERVAL '65 days', 62, NOW() - INTERVAL '65 days', NOW()),
(38, 4, 38, 'enrolled', NOW() - INTERVAL '65 days', 62, NOW() - INTERVAL '65 days', NOW()),
(39, 4, 39, 'enrolled', NOW() - INTERVAL '65 days', 62, NOW() - INTERVAL '65 days', NOW()),
(40, 4, 40, 'enrolled', NOW() - INTERVAL '65 days', 62, NOW() - INTERVAL '65 days', NOW()),
(41, 4, 41, 'enrolled', NOW() - INTERVAL '65 days', 62, NOW() - INTERVAL '65 days', NOW()),
(42, 4, 42, 'enrolled', NOW() - INTERVAL '65 days', 62, NOW() - INTERVAL '65 days', NOW());

-- 9.4 Enrollments for ONGOING Class 5 (Conversational A2 - 8 students)
INSERT INTO enrollment (id, class_id, student_id, status, enrolled_at, join_session_id, created_at, updated_at)
VALUES
(43, 5, 43, 'enrolled', NOW() - INTERVAL '40 days', 72, NOW() - INTERVAL '40 days', NOW()),
(44, 5, 44, 'enrolled', NOW() - INTERVAL '40 days', 72, NOW() - INTERVAL '40 days', NOW()),
(45, 5, 45, 'enrolled', NOW() - INTERVAL '40 days', 72, NOW() - INTERVAL '40 days', NOW()),
(46, 5, 46, 'enrolled', NOW() - INTERVAL '40 days', 72, NOW() - INTERVAL '40 days', NOW()),
(47, 5, 47, 'enrolled', NOW() - INTERVAL '40 days', 72, NOW() - INTERVAL '40 days', NOW()),
(48, 5, 48, 'enrolled', NOW() - INTERVAL '40 days', 72, NOW() - INTERVAL '40 days', NOW()),
(49, 5, 49, 'enrolled', NOW() - INTERVAL '40 days', 72, NOW() - INTERVAL '40 days', NOW()),
(50, 5, 50, 'enrolled', NOW() - INTERVAL '40 days', 72, NOW() - INTERVAL '40 days', NOW());

-- 9.5 Enrollments for NEW CLASSES (for transfer testing)
-- Class 13 (A1-GEN-003 - Online, ongoing) - 15 students
INSERT INTO enrollment (id, class_id, student_id, status, enrolled_at, created_at, updated_at)
VALUES
(51, 13, 1, 'enrolled', NOW() - INTERVAL '35 days', NOW() - INTERVAL '35 days', NOW()),
(52, 13, 2, 'enrolled', NOW() - INTERVAL '35 days', NOW() - INTERVAL '35 days', NOW()),
(53, 13, 3, 'enrolled', NOW() - INTERVAL '35 days', NOW() - INTERVAL '35 days', NOW()),
(54, 13, 4, 'enrolled', NOW() - INTERVAL '35 days', NOW() - INTERVAL '35 days', NOW()),
(55, 13, 5, 'enrolled', NOW() - INTERVAL '35 days', NOW() - INTERVAL '35 days', NOW()),
(56, 13, 6, 'enrolled', NOW() - INTERVAL '35 days', NOW() - INTERVAL '35 days', NOW()),
(57, 13, 7, 'enrolled', NOW() - INTERVAL '35 days', NOW() - INTERVAL '35 days', NOW()),
(58, 13, 8, 'enrolled', NOW() - INTERVAL '35 days', NOW() - INTERVAL '35 days', NOW()),
(59, 13, 9, 'enrolled', NOW() - INTERVAL '35 days', NOW() - INTERVAL '35 days', NOW()),
(60, 13, 10, 'enrolled', NOW() - INTERVAL '35 days', NOW() - INTERVAL '35 days', NOW()),
(61, 13, 11, 'enrolled', NOW() - INTERVAL '35 days', NOW() - INTERVAL '35 days', NOW()),
(62, 13, 12, 'enrolled', NOW() - INTERVAL '35 days', NOW() - INTERVAL '35 days', NOW()),
(63, 13, 51, 'enrolled', NOW() - INTERVAL '35 days', NOW() - INTERVAL '35 days', NOW()),
(64, 13, 52, 'enrolled', NOW() - INTERVAL '35 days', NOW() - INTERVAL '35 days', NOW()),
(65, 13, 53, 'enrolled', NOW() - INTERVAL '35 days', NOW() - INTERVAL '35 days', NOW());

-- Class 16 (B1-IELTS-002 - Online, ongoing) - 12 students (has capacity for more)
INSERT INTO enrollment (id, class_id, student_id, status, enrolled_at, created_at, updated_at)
VALUES
(66, 16, 29, 'enrolled', NOW() - INTERVAL '45 days', NOW() - INTERVAL '45 days', NOW()),
(67, 16, 30, 'enrolled', NOW() - INTERVAL '45 days', NOW() - INTERVAL '45 days', NOW()),
(68, 16, 31, 'enrolled', NOW() - INTERVAL '45 days', NOW() - INTERVAL '45 days', NOW()),
(69, 16, 32, 'enrolled', NOW() - INTERVAL '45 days', NOW() - INTERVAL '45 days', NOW()),
(70, 16, 33, 'enrolled', NOW() - INTERVAL '45 days', NOW() - INTERVAL '45 days', NOW()),
(71, 16, 34, 'enrolled', NOW() - INTERVAL '45 days', NOW() - INTERVAL '45 days', NOW()),
(72, 16, 35, 'enrolled', NOW() - INTERVAL '45 days', NOW() - INTERVAL '45 days', NOW()),
(73, 16, 36, 'enrolled', NOW() - INTERVAL '45 days', NOW() - INTERVAL '45 days', NOW()),
(74, 16, 37, 'enrolled', NOW() - INTERVAL '45 days', NOW() - INTERVAL '45 days', NOW()),
(75, 16, 38, 'enrolled', NOW() - INTERVAL '45 days', NOW() - INTERVAL '45 days', NOW()),
(76, 16, 39, 'enrolled', NOW() - INTERVAL '45 days', NOW() - INTERVAL '45 days', NOW()),
(77, 16, 40, 'enrolled', NOW() - INTERVAL '45 days', NOW() - INTERVAL '45 days', NOW());

-- Class 19 (B2-IELTS-002 - Online, ongoing) - 10 students (has good capacity)
INSERT INTO enrollment (id, class_id, student_id, status, enrolled_at, created_at, updated_at)
VALUES
(78, 19, 41, 'enrolled', NOW() - INTERVAL '50 days', NOW() - INTERVAL '50 days', NOW()),
(79, 19, 42, 'enrolled', NOW() - INTERVAL '50 days', NOW() - INTERVAL '50 days', NOW()),
(80, 19, 43, 'enrolled', NOW() - INTERVAL '50 days', NOW() - INTERVAL '50 days', NOW()),
(81, 19, 44, 'enrolled', NOW() - INTERVAL '50 days', NOW() - INTERVAL '50 days', NOW()),
(82, 19, 45, 'enrolled', NOW() - INTERVAL '50 days', NOW() - INTERVAL '50 days', NOW()),
(83, 19, 46, 'enrolled', NOW() - INTERVAL '50 days', NOW() - INTERVAL '50 days', NOW()),
(84, 19, 47, 'enrolled', NOW() - INTERVAL '50 days', NOW() - INTERVAL '50 days', NOW()),
(85, 19, 48, 'enrolled', NOW() - INTERVAL '50 days', NOW() - INTERVAL '50 days', NOW()),
(86, 19, 49, 'enrolled', NOW() - INTERVAL '50 days', NOW() - INTERVAL '50 days', NOW()),
(87, 19, 50, 'enrolled', NOW() - INTERVAL '50 days', NOW() - INTERVAL '50 days', NOW());

-- Class 21 (A2-CONV-002 - Online, ongoing) - 8 students  
INSERT INTO enrollment (id, class_id, student_id, status, enrolled_at, created_at, updated_at)
VALUES
(88, 21, 51, 'enrolled', NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days', NOW()),
(89, 21, 52, 'enrolled', NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days', NOW()),
(90, 21, 53, 'enrolled', NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days', NOW()),
(91, 21, 54, 'enrolled', NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days', NOW()),
(92, 21, 55, 'enrolled', NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days', NOW()),
(93, 21, 1, 'enrolled', NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days', NOW()),
(94, 21, 2, 'enrolled', NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days', NOW()),
(95, 21, 3, 'enrolled', NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days', NOW());

-- 9.6 Sample Student Sessions for Class 1, Student 1 (showing varied attendance)
INSERT INTO student_session (student_id, session_id, is_makeup, attendance_status, homework_status, note, recorded_at)
VALUES
-- First week - all present
(1, 1, false, 'present', 'completed', 'Active participation', CURRENT_DATE - INTERVAL '149 days'),
(1, 2, false, 'present', 'completed', NULL, CURRENT_DATE - INTERVAL '147 days'),
(1, 3, false, 'present', 'completed', NULL, CURRENT_DATE - INTERVAL '145 days'),
-- Week 2
(1, 4, false, 'present', 'completed', NULL, CURRENT_DATE - INTERVAL '142 days'),
(1, 5, false, 'absent', NULL, 'Sick leave', CURRENT_DATE - INTERVAL '140 days'),
(1, 6, false, 'present', 'incomplete', 'Need to finish homework', CURRENT_DATE - INTERVAL '138 days'),
-- Final sessions
(1, 34, false, 'present', 'completed', NULL, CURRENT_DATE - INTERVAL '72 days'),
(1, 35, false, 'present', 'no_homework', 'Review session', CURRENT_DATE - INTERVAL '70 days'),
(1, 36, false, 'present', 'no_homework', 'Passed final assessment', CURRENT_DATE - INTERVAL '68 days');

-- 9.7 Sample Student Sessions for Class 3, Students 13-15 (recent sessions)
INSERT INTO student_session (student_id, session_id, is_makeup, attendance_status, homework_status, note, recorded_at)
VALUES
-- Student 13 - good attendance
(13, 52, false, 'present', 'completed', NULL, CURRENT_DATE - INTERVAL '14 days'),
(13, 53, false, 'present', 'completed', NULL, CURRENT_DATE - INTERVAL '12 days'),
(13, 54, false, 'present', 'completed', NULL, CURRENT_DATE - INTERVAL '10 days'),
(13, 55, false, 'present', 'completed', NULL, CURRENT_DATE - INTERVAL '7 days'),
(13, 56, false, 'present', 'completed', NULL, CURRENT_DATE - INTERVAL '5 days'),
(13, 57, false, 'present', 'completed', NULL, CURRENT_DATE - INTERVAL '3 days'),
(13, 58, false, 'present', 'incomplete', NULL, NOW()),
-- Student 14 - one absence
(14, 52, false, 'present', 'completed', NULL, CURRENT_DATE - INTERVAL '14 days'),
(14, 53, false, 'absent', NULL, 'Personal matter', CURRENT_DATE - INTERVAL '12 days'),
(14, 54, false, 'present', 'completed', NULL, CURRENT_DATE - INTERVAL '10 days'),
(14, 55, false, 'present', 'completed', NULL, CURRENT_DATE - INTERVAL '7 days'),
(14, 56, false, 'present', 'completed', NULL, CURRENT_DATE - INTERVAL '5 days'),
(14, 57, false, 'present', 'completed', NULL, CURRENT_DATE - INTERVAL '3 days'),
(14, 58, false, 'present', 'completed', NULL, NOW()),
-- Student 15 - late once
(15, 52, false, 'present', 'completed', NULL, CURRENT_DATE - INTERVAL '14 days'),
(15, 53, false, 'present', 'completed', NULL, CURRENT_DATE - INTERVAL '12 days'),
(15, 54, false, 'late', 'completed', 'Traffic jam', CURRENT_DATE - INTERVAL '10 days'),
(15, 55, false, 'present', 'completed', NULL, CURRENT_DATE - INTERVAL '7 days'),
(15, 56, false, 'present', 'completed', NULL, CURRENT_DATE - INTERVAL '5 days'),
(15, 57, false, 'present', 'completed', NULL, CURRENT_DATE - INTERVAL '3 days'),
(15, 58, false, 'present', 'incomplete', NULL, NOW());

-- Reset sequences
SELECT setval('enrollment_id_seq', (SELECT MAX(id) FROM enrollment));

-- =========================================
-- SECTION 10: TEACHING SLOTS & RESOURCES
-- =========================================

-- 10.1 Teaching Slots for Class 1 (Completed) - Teacher 1 (John Smith) - All 36 sessions
INSERT INTO teaching_slot (session_id, teacher_id, skill, role, status)
VALUES
(1, 1, 'general', 'primary', 'completed'),
(2, 1, 'speaking', 'primary', 'completed'),
(3, 1, 'general', 'primary', 'completed'),
(4, 1, 'general', 'primary', 'completed'),
(5, 1, 'general', 'primary', 'completed'),
(6, 1, 'general', 'primary', 'completed'),
(7, 1, 'general', 'primary', 'completed'),
(8, 1, 'general', 'primary', 'completed'),
(9, 1, 'general', 'primary', 'completed'),
(10, 1, 'general', 'primary', 'completed'),
(11, 1, 'general', 'primary', 'completed'),
(12, 1, 'general', 'primary', 'completed'),
(13, 1, 'general', 'primary', 'completed'),
(14, 1, 'general', 'primary', 'completed'),
(15, 1, 'general', 'primary', 'completed'),
(16, 1, 'general', 'primary', 'completed'),
(17, 1, 'general', 'primary', 'completed'),
(18, 1, 'general', 'primary', 'completed'),
(19, 1, 'general', 'primary', 'completed'),
(20, 1, 'general', 'primary', 'completed'),
(21, 1, 'general', 'primary', 'completed'),
(22, 1, 'general', 'primary', 'completed'),
(23, 1, 'general', 'primary', 'completed'),
(24, 1, 'general', 'primary', 'completed'),
(25, 1, 'general', 'primary', 'completed'),
(26, 1, 'general', 'primary', 'completed'),
(27, 1, 'general', 'primary', 'completed'),
(28, 1, 'general', 'primary', 'completed'),
(29, 1, 'general', 'primary', 'completed'),
(30, 1, 'general', 'primary', 'completed'),
(31, 1, 'general', 'primary', 'completed'),
(32, 1, 'general', 'primary', 'completed'),
(33, 1, 'general', 'primary', 'completed'),
(34, 1, 'general', 'primary', 'completed'),
(35, 1, 'general', 'primary', 'completed'),
(36, 1, 'general', 'primary', 'completed');

-- 10.2 Teaching Slots for Class 3 (IELTS B1) - Primary Teacher 4 (Emily) with Assistant 7 (Mai)
INSERT INTO teaching_slot (session_id, teacher_id, skill, role, status)
VALUES
-- Primary teacher
(37, 4, 'listening', 'primary', 'completed'),
(38, 4, 'listening', 'primary', 'completed'),
(39, 4, 'listening', 'primary', 'completed'),
(52, 4, 'reading', 'primary', 'completed'),
(53, 4, 'reading', 'primary', 'completed'),
(54, 4, 'reading', 'primary', 'completed'),
(55, 4, 'reading', 'primary', 'completed'),
(56, 4, 'reading', 'primary', 'completed'),
(57, 4, 'reading', 'primary', 'completed'),
(58, 4, 'reading', 'primary', 'completed'),
(59, 4, 'reading', 'primary', 'scheduled'),
(60, 4, 'reading', 'primary', 'scheduled'),
(61, 4, 'reading', 'primary', 'scheduled'),
-- Assistant teacher for some sessions
(37, 7, 'general', 'assistant', 'completed'),
(52, 7, 'general', 'assistant', 'completed'),
(59, 7, 'general', 'assistant', 'scheduled');

-- 10.3 Teaching Slots for Class 4 (IELTS B2 Hybrid) - Teacher 1 (John) as primary
INSERT INTO teaching_slot (session_id, teacher_id, skill, role, status)
VALUES
(62, 1, 'speaking', 'primary', 'completed'),
(63, 1, 'writing', 'primary', 'completed'),
(64, 1, 'speaking', 'primary', 'completed'),
(65, 1, 'writing', 'primary', 'completed'),
(66, 1, 'speaking', 'primary', 'completed'),
(67, 1, 'writing', 'primary', 'completed'),
(68, 1, 'speaking', 'primary', 'scheduled'),
(69, 1, 'writing', 'primary', 'scheduled'),
(70, 1, 'speaking', 'primary', 'scheduled'),
(71, 1, 'writing', 'primary', 'scheduled');

-- 10.4 Teaching Slots for Class 5 (Conversational A2 Weekend) - Teacher 6 (Tran Van Huy)
INSERT INTO teaching_slot (session_id, teacher_id, skill, role, status)
VALUES
(72, 6, 'speaking', 'primary', 'completed'),
(73, 6, 'speaking', 'primary', 'completed'),
(74, 6, 'speaking', 'primary', 'completed'),
(75, 6, 'speaking', 'primary', 'completed'),
(76, 6, 'speaking', 'primary', 'completed'),
(77, 6, 'speaking', 'primary', 'completed'),
(78, 6, 'speaking', 'primary', 'completed'),
(79, 6, 'speaking', 'primary', 'completed'),
(80, 6, 'speaking', 'primary', 'completed'),
(81, 6, 'speaking', 'primary', 'completed'),
(82, 6, 'speaking', 'primary', 'scheduled'),
(83, 6, 'speaking', 'primary', 'scheduled');

-- 10.5 Session Resources for Class 1 (Offline - using Room 101) - All 36 sessions
INSERT INTO session_resource (session_id, resource_type, resource_id, capacity_override)
VALUES
(1, 'room', 1, NULL), (2, 'room', 1, NULL), (3, 'room', 1, NULL),
(4, 'room', 1, NULL), (5, 'room', 1, NULL), (6, 'room', 1, NULL),
(7, 'room', 1, NULL), (8, 'room', 1, NULL), (9, 'room', 1, NULL),
(10, 'room', 1, NULL), (11, 'room', 1, NULL), (12, 'room', 1, NULL),
(13, 'room', 1, NULL), (14, 'room', 1, NULL), (15, 'room', 1, NULL),
(16, 'room', 1, NULL), (17, 'room', 1, NULL), (18, 'room', 1, NULL),
(19, 'room', 1, NULL), (20, 'room', 1, NULL), (21, 'room', 1, NULL),
(22, 'room', 1, NULL), (23, 'room', 1, NULL), (24, 'room', 1, NULL),
(25, 'room', 1, NULL), (26, 'room', 1, NULL), (27, 'room', 1, NULL),
(28, 'room', 1, NULL), (29, 'room', 1, NULL), (30, 'room', 1, NULL),
(31, 'room', 1, NULL), (32, 'room', 1, NULL), (33, 'room', 1, NULL),
(34, 'room', 1, NULL), (35, 'room', 1, NULL), (36, 'room', 1, NULL);

-- 10.6 Session Resources for Class 3 (Offline - using Room 201)
INSERT INTO session_resource (session_id, resource_type, resource_id, capacity_override)
VALUES
(37, 'room', 4, NULL),
(38, 'room', 4, NULL),
(39, 'room', 4, NULL),
(52, 'room', 4, NULL),
(53, 'room', 4, NULL),
(54, 'room', 4, NULL),
(55, 'room', 4, NULL),
(56, 'room', 4, NULL),
(57, 'room', 4, NULL),
(58, 'room', 4, NULL),
(59, 'room', 4, NULL),
(60, 'room', 4, NULL),
(61, 'room', 4, NULL);

-- 10.7 Session Resources for Class 4 (Hybrid - Room 301 + Zoom Account 1)
INSERT INTO session_resource (session_id, resource_type, resource_id, capacity_override)
VALUES
-- Physical room
(62, 'room', 7, NULL),
(63, 'room', 7, NULL),
(64, 'room', 7, NULL),
(65, 'room', 7, NULL),
(66, 'room', 7, NULL),
(67, 'room', 7, NULL),
(68, 'room', 7, NULL),
(69, 'room', 7, NULL),
-- Virtual resource
(62, 'virtual', 11, NULL),
(63, 'virtual', 11, NULL),
(64, 'virtual', 11, NULL),
(65, 'virtual', 11, NULL),
(66, 'virtual', 11, NULL),
(67, 'virtual', 11, NULL),
(68, 'virtual', 11, NULL),
(69, 'virtual', 11, NULL);

-- 10.8 Session Resources for Class 5 (Offline weekend - Room 202)
INSERT INTO session_resource (session_id, resource_type, resource_id, capacity_override)
VALUES
(72, 'room', 5, NULL),
(73, 'room', 5, NULL),
(74, 'room', 5, NULL),
(75, 'room', 5, NULL),
(76, 'room', 5, NULL),
(77, 'room', 5, NULL),
(78, 'room', 5, NULL),
(79, 'room', 5, NULL),
(80, 'room', 5, NULL),
(81, 'room', 5, NULL),
(82, 'room', 5, NULL),
(83, 'room', 5, NULL);

-- =========================================
-- SECTION 11: SAMPLE REQUESTS
-- =========================================

-- 11.1 Student Requests (absence, makeup, transfer)
INSERT INTO student_request (id, student_id, current_class_id, request_type, target_session_id, effective_date, status, submitted_at, submitted_by, decided_by, decided_at, note, created_at, updated_at)
VALUES
-- Approved absence request
(1, 14, 3, 'absence', 53, CURRENT_DATE - INTERVAL '13 days', 'approved', NOW() - INTERVAL '15 days', 14, 4, NOW() - INTERVAL '14 days', 'Family emergency', NOW() - INTERVAL '16 days', NOW() - INTERVAL '14 days'),

-- Pending makeup request
(2, 14, 3, 'makeup', NULL, CURRENT_DATE + INTERVAL '7 days', 'pending', NOW() - INTERVAL '2 days', 14, NULL, NULL, 'Request makeup for missed session 53', NOW() - INTERVAL '2 days', NOW()),

-- Approved absence with excused status
(3, 15, 3, 'absence', 54, CURRENT_DATE - INTERVAL '11 days', 'approved', NOW() - INTERVAL '13 days', 15, 4, NOW() - INTERVAL '12 days', 'Medical appointment', NOW() - INTERVAL '14 days', NOW() - INTERVAL '12 days');

-- 11.2 Teacher Requests (leave, swap, reschedule)
INSERT INTO teacher_request (id, teacher_id, session_id, request_type, replacement_teacher_id, reason, status, submitted_at, submitted_by, decided_by, decided_at, note, created_at, updated_at)
VALUES
-- Approved leave with replacement
(1, 4, 59, 'leave', 7, 'Personal day off', 'approved', NOW() - INTERVAL '5 days', 4, 3, NOW() - INTERVAL '4 days', 'Teacher 7 will substitute', NOW() - INTERVAL '6 days', NOW() - INTERVAL '4 days'),

-- Pending reschedule request
(2, 6, 82, 'reschedule', NULL, 'Conflict with another commitment', 'pending', NOW() - INTERVAL '1 day', 6, NULL, NULL, 'Request to reschedule weekend session', NOW() - INTERVAL '1 day', NOW());

-- Reset sequences
SELECT setval('student_request_id_seq', (SELECT MAX(id) FROM student_request));
SELECT setval('teacher_request_id_seq', (SELECT MAX(id) FROM teacher_request));

-- =========================================
-- SECTION 12: CLASS ASSESSMENTS (INSTANCES)
-- =========================================
-- These are actual assessments for classes (linked to course_assessment templates via course_assessment_id)
-- Teacher can customize name/description but maintain link to template for CLO tracking

-- Class 1 (Completed): General English A1 - All assessments completed
INSERT INTO assessment (id, class_id, course_assessment_id, name, kind, max_score, description, created_by, created_at, updated_at)
VALUES
-- Vocabulary Quizzes (linked to course_assessment_id = 1)
(1, 1, 1, 'Vocabulary Quiz 1', 'quiz', 20.00, 'Basic vocabulary test covering greetings and introductions', 1, CURRENT_DATE - INTERVAL '145 days', CURRENT_DATE - INTERVAL '138 days'),
(2, 1, 1, 'Vocabulary Quiz 2 - Daily Life', 'quiz', 20.00, 'Vocabulary test on daily activities and routines', 1, CURRENT_DATE - INTERVAL '125 days', CURRENT_DATE - INTERVAL '118 days'),
(3, 1, 1, 'Vocabulary Quiz 3 - Food & Shopping', 'quiz', 20.00, 'Vocabulary test covering shopping and food', 1, CURRENT_DATE - INTERVAL '105 days', CURRENT_DATE - INTERVAL '98 days'),

-- Grammar Tests (linked to course_assessment_id = 2)
(4, 1, 2, 'Grammar Test: Present Simple & Present Continuous', 'quiz', 30.00, 'Test on present tense forms', 1, CURRENT_DATE - INTERVAL '135 days', CURRENT_DATE - INTERVAL '128 days'),
(5, 1, 2, 'Grammar Test: Past Tense', 'quiz', 30.00, 'Test on past simple and past continuous', 1, CURRENT_DATE - INTERVAL '115 days', CURRENT_DATE - INTERVAL '108 days'),

-- Speaking Tests (linked to course_assessment_id = 3)
(6, 1, 3, 'Speaking Test: Self Introduction', 'oral', 25.00, 'Oral assessment on self-introduction', 1, CURRENT_DATE - INTERVAL '140 days', CURRENT_DATE - INTERVAL '133 days'),
(7, 1, 3, 'Speaking Test: Daily Conversation', 'oral', 25.00, 'Conversational assessment on daily topics', 1, CURRENT_DATE - INTERVAL '100 days', CURRENT_DATE - INTERVAL '93 days'),

-- Midterm & Final (linked to course_assessment_id = 4, 5)
(8, 1, 4, 'Midterm Exam', 'midterm', 100.00, 'Comprehensive midterm covering weeks 1-6', 1, CURRENT_DATE - INTERVAL '120 days', CURRENT_DATE - INTERVAL '113 days'),
(9, 1, 5, 'Final Exam', 'final', 100.00, 'Comprehensive final exam covering all material', 1, CURRENT_DATE - INTERVAL '75 days', CURRENT_DATE - INTERVAL '68 days'),

-- Custom Assignments (no course_assessment link - teacher created)
(10, 1, NULL, 'Writing Assignment: My Family', 'assignment', 20.00, 'Short essay about family members', 1, CURRENT_DATE - INTERVAL '130 days', CURRENT_DATE - INTERVAL '123 days'),
(11, 1, NULL, 'Writing Assignment: My Daily Routine', 'assignment', 20.00, 'Essay describing daily routine', 1, CURRENT_DATE - INTERVAL '95 days', CURRENT_DATE - INTERVAL '88 days'),

-- Class 3 (Ongoing): IELTS Foundation B1 - Some assessments completed, some pending
-- Linked to Course 2 assessments (IDs 7-12)
(12, 3, 7, 'IELTS Listening Practice Test 1', 'practice', 40.00, 'First listening practice test', 2, CURRENT_DATE - INTERVAL '55 days', CURRENT_DATE - INTERVAL '48 days'),
(13, 3, 8, 'IELTS Reading Practice Test 1', 'practice', 40.00, 'First reading practice test', 2, CURRENT_DATE - INTERVAL '50 days', CURRENT_DATE - INTERVAL '43 days'),
(14, 3, 10, 'Writing Task 1 Assignment', 'assignment', 25.00, 'Describe a chart/graph', 2, CURRENT_DATE - INTERVAL '40 days', CURRENT_DATE - INTERVAL '33 days'),
(15, 3, 9, 'Speaking Part 1 Mock Test', 'oral', 25.00, 'IELTS Speaking Part 1 practice', 2, CURRENT_DATE - INTERVAL '35 days', CURRENT_DATE - INTERVAL '28 days'),
(16, 3, NULL, 'Midterm Exam', 'midterm', 100.00, 'IELTS midterm covering all skills', 2, CURRENT_DATE - INTERVAL '25 days', CURRENT_DATE - INTERVAL '18 days'),
-- Upcoming assessments (created but not graded yet)
(17, 3, 12, 'IELTS Full Mock Test', 'practice', 100.00, 'Complete IELTS mock exam', 2, CURRENT_DATE - INTERVAL '10 days', CURRENT_DATE - INTERVAL '10 days'),
(18, 3, 12, 'Final Exam', 'final', 100.00, 'Comprehensive IELTS final exam', 2, CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE - INTERVAL '5 days'),

-- Class 4 (Ongoing): Business English B2 - Some completed (No course_assessment template for this course, all custom)
(19, 4, NULL, 'Business Vocabulary Quiz', 'quiz', 25.00, 'Business terminology test', 3, CURRENT_DATE - INTERVAL '47 days', CURRENT_DATE - INTERVAL '40 days'),
(20, 4, NULL, 'Presentation Project', 'project', 50.00, 'Business presentation on company analysis', 3, CURRENT_DATE - INTERVAL '50 days', CURRENT_DATE - INTERVAL '23 days'),
(21, 4, NULL, 'Email Writing Assignment', 'assignment', 30.00, 'Professional email correspondence', 3, CURRENT_DATE - INTERVAL '35 days', CURRENT_DATE - INTERVAL '28 days'),
(22, 4, NULL, 'Midterm Exam', 'midterm', 100.00, 'Business English midterm', 3, CURRENT_DATE - INTERVAL '20 days', CURRENT_DATE - INTERVAL '13 days'),
-- Upcoming
(23, 4, NULL, 'Negotiation Role-play', 'oral', 40.00, 'Business negotiation simulation', 3, CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE - INTERVAL '5 days'),
(24, 4, NULL, 'Final Exam', 'final', 100.00, 'Business English final exam', 3, CURRENT_DATE - INTERVAL '3 days', CURRENT_DATE - INTERVAL '3 days'),

-- Class 5 (Ongoing): Japanese N5 - Early stage, few assessments
-- Linked to Course 7 assessments (IDs 13-18)
(25, 5, 13, 'Hiragana Quiz', 'quiz', 30.00, 'Hiragana reading and writing test', 10, CURRENT_DATE - INTERVAL '60 days', CURRENT_DATE - INTERVAL '53 days'),
(26, 5, 14, 'Katakana Quiz', 'quiz', 30.00, 'Katakana reading and writing test', 10, CURRENT_DATE - INTERVAL '45 days', CURRENT_DATE - INTERVAL '38 days'),
(27, 5, 15, 'Basic Kanji Test', 'quiz', 25.00, 'First 50 kanji characters', 10, CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE - INTERVAL '23 days'),
(28, 5, 17, 'Speaking Test: Self Introduction', 'oral', 25.00, 'Japanese self-introduction', 10, CURRENT_DATE - INTERVAL '40 days', CURRENT_DATE - INTERVAL '33 days'),
-- Upcoming
(29, 5, 16, 'Midterm Exam', 'midterm', 100.00, 'N5 midterm covering grammar and vocabulary', 10, CURRENT_DATE - INTERVAL '7 days', CURRENT_DATE - INTERVAL '7 days'),
(30, 5, 18, 'Final Exam', 'final', 100.00, 'JLPT N5 mock exam', 10, CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE - INTERVAL '5 days');

-- Reset sequence
SELECT setval('assessment_id_seq', (SELECT MAX(id) FROM assessment));

-- =========================================
-- SECTION 13: STUDENT SCORES
-- =========================================
-- Scores for completed assessments

-- Class 1 (Completed) - Student 1 (Excellent student - scores 85-95)
INSERT INTO score (id, assessment_id, student_id, score, feedback, graded_by, graded_at)
VALUES
-- Student 1 (ID: 1) - Nguyen Van An
(1, 1, 1, 18.5, 'Excellent vocabulary knowledge', 1, CURRENT_DATE - INTERVAL '138 days'),
(2, 2, 1, 19.0, 'Perfect understanding of daily routines', 1, CURRENT_DATE - INTERVAL '118 days'),
(3, 3, 1, 18.0, 'Good work on food vocabulary', 1, CURRENT_DATE - INTERVAL '98 days'),
(4, 4, 1, 27.5, 'Strong grasp of present tenses', 1, CURRENT_DATE - INTERVAL '128 days'),
(5, 5, 1, 28.0, 'Excellent past tense usage', 1, CURRENT_DATE - INTERVAL '108 days'),
(6, 6, 1, 23.0, 'Fluent and confident self-introduction', 1, CURRENT_DATE - INTERVAL '133 days'),
(7, 7, 1, 22.5, 'Natural conversational skills', 1, CURRENT_DATE - INTERVAL '93 days'),
(8, 8, 1, 89.0, 'Outstanding midterm performance', 1, CURRENT_DATE - INTERVAL '113 days'),
(9, 9, 1, 92.0, 'Excellent final exam - top of class', 1, CURRENT_DATE - INTERVAL '68 days'),
(10, 10, 1, 19.0, 'Well-written family essay', 1, CURRENT_DATE - INTERVAL '123 days'),
(11, 11, 1, 18.5, 'Clear and organized daily routine essay', 1, CURRENT_DATE - INTERVAL '88 days'),

-- Student 2 (ID: 2) - Tran Thi Binh (Good student - scores 75-85)
(12, 1, 2, 16.0, 'Good vocabulary knowledge', 1, CURRENT_DATE - INTERVAL '138 days'),
(13, 2, 2, 17.0, 'Well done on daily activities', 1, CURRENT_DATE - INTERVAL '118 days'),
(14, 3, 2, 15.5, 'Good effort on vocabulary', 1, CURRENT_DATE - INTERVAL '98 days'),
(15, 4, 2, 24.0, 'Good understanding of tenses', 1, CURRENT_DATE - INTERVAL '128 days'),
(16, 5, 2, 25.0, 'Solid past tense knowledge', 1, CURRENT_DATE - INTERVAL '108 days'),
(17, 6, 2, 20.0, 'Clear self-introduction', 1, CURRENT_DATE - INTERVAL '133 days'),
(18, 7, 2, 19.5, 'Good conversational ability', 1, CURRENT_DATE - INTERVAL '93 days'),
(19, 8, 2, 78.0, 'Good midterm performance', 1, CURRENT_DATE - INTERVAL '113 days'),
(20, 9, 2, 81.0, 'Good final exam result', 1, CURRENT_DATE - INTERVAL '68 days'),
(21, 10, 2, 16.5, 'Nice family description', 1, CURRENT_DATE - INTERVAL '123 days'),
(22, 11, 2, 17.0, 'Well-structured essay', 1, CURRENT_DATE - INTERVAL '88 days'),

-- Student 3 (ID: 3) - Le Van Cuong (Average student - scores 65-75)
(23, 1, 3, 14.0, 'Fair vocabulary knowledge', 1, CURRENT_DATE - INTERVAL '138 days'),
(24, 2, 3, 15.0, 'Adequate understanding', 1, CURRENT_DATE - INTERVAL '118 days'),
(25, 3, 3, 13.5, 'Needs more vocabulary practice', 1, CURRENT_DATE - INTERVAL '98 days'),
(26, 4, 3, 21.0, 'Fair grasp of tenses', 1, CURRENT_DATE - INTERVAL '128 days'),
(27, 5, 3, 22.0, 'Satisfactory past tense usage', 1, CURRENT_DATE - INTERVAL '108 days'),
(28, 6, 3, 17.0, 'Acceptable self-introduction', 1, CURRENT_DATE - INTERVAL '133 days'),
(29, 7, 3, 16.5, 'Needs more speaking practice', 1, CURRENT_DATE - INTERVAL '93 days'),
(30, 8, 3, 68.0, 'Satisfactory midterm', 1, CURRENT_DATE - INTERVAL '113 days'),
(31, 9, 3, 71.0, 'Fair final exam performance', 1, CURRENT_DATE - INTERVAL '68 days'),
(32, 10, 3, 14.0, 'Basic essay structure', 1, CURRENT_DATE - INTERVAL '123 days'),
(33, 11, 3, 15.0, 'Adequate essay', 1, CURRENT_DATE - INTERVAL '88 days'),

-- Student 4 (ID: 4) - Dropped student - only early scores before dropping
(34, 1, 4, 12.0, 'Basic vocabulary knowledge', 1, CURRENT_DATE - INTERVAL '138 days'),
(35, 2, 4, 13.0, 'Fair attempt', 1, CURRENT_DATE - INTERVAL '118 days'),
(36, 4, 4, 18.0, 'Needs improvement', 1, CURRENT_DATE - INTERVAL '128 days'),
(37, 6, 4, 14.0, 'Limited speaking ability', 1, CURRENT_DATE - INTERVAL '133 days'),
-- Student dropped after session 15, no later scores

-- Student 5 (ID: 5) - Pham Thi Dung (Strong student - scores 80-90)
(38, 1, 5, 17.0, 'Very good vocabulary', 1, CURRENT_DATE - INTERVAL '138 days'),
(39, 2, 5, 18.0, 'Excellent comprehension', 1, CURRENT_DATE - INTERVAL '118 days'),
(40, 3, 5, 17.5, 'Strong vocabulary knowledge', 1, CURRENT_DATE - INTERVAL '98 days'),
(41, 4, 5, 26.0, 'Excellent tense usage', 1, CURRENT_DATE - INTERVAL '128 days'),
(42, 5, 5, 27.0, 'Very strong grammar', 1, CURRENT_DATE - INTERVAL '108 days'),
(43, 6, 5, 21.5, 'Confident speaker', 1, CURRENT_DATE - INTERVAL '133 days'),
(44, 7, 5, 21.0, 'Good conversational skills', 1, CURRENT_DATE - INTERVAL '93 days'),
(45, 8, 5, 83.0, 'Strong midterm performance', 1, CURRENT_DATE - INTERVAL '113 days'),
(46, 9, 5, 87.0, 'Excellent final exam', 1, CURRENT_DATE - INTERVAL '68 days'),
(47, 10, 5, 18.0, 'Well-written essay', 1, CURRENT_DATE - INTERVAL '123 days'),
(48, 11, 5, 17.5, 'Clear and detailed writing', 1, CURRENT_DATE - INTERVAL '88 days'),

-- Class 3 (Ongoing) - IELTS Foundation B1 - Students 11-20
-- Only scores for completed assessments (12-16)

-- Student 11 (ID: 11) - Strong IELTS student
(49, 12, 11, 34.0, 'Excellent listening skills', 2, CURRENT_DATE - INTERVAL '48 days'),
(50, 13, 11, 35.0, 'Outstanding reading comprehension', 2, CURRENT_DATE - INTERVAL '43 days'),
(51, 14, 11, 22.0, 'Well-structured Task 1 response', 2, CURRENT_DATE - INTERVAL '33 days'),
(52, 15, 11, 21.5, 'Fluent and natural speaking', 2, CURRENT_DATE - INTERVAL '28 days'),
(53, 16, 11, 88.0, 'Excellent midterm result', 2, CURRENT_DATE - INTERVAL '18 days'),

-- Student 12 (ID: 12) - Good IELTS student
(54, 12, 12, 30.0, 'Good listening comprehension', 2, CURRENT_DATE - INTERVAL '48 days'),
(55, 13, 12, 31.0, 'Strong reading skills', 2, CURRENT_DATE - INTERVAL '43 days'),
(56, 14, 12, 19.5, 'Good Task 1 description', 2, CURRENT_DATE - INTERVAL '33 days'),
(57, 15, 12, 19.0, 'Clear speaking responses', 2, CURRENT_DATE - INTERVAL '28 days'),
(58, 16, 12, 79.0, 'Good midterm performance', 2, CURRENT_DATE - INTERVAL '18 days'),

-- Student 13 (ID: 13) - Average IELTS student
(59, 12, 13, 26.0, 'Fair listening ability', 2, CURRENT_DATE - INTERVAL '48 days'),
(60, 13, 13, 27.0, 'Adequate reading comprehension', 2, CURRENT_DATE - INTERVAL '43 days'),
(61, 14, 13, 16.0, 'Basic Task 1 structure', 2, CURRENT_DATE - INTERVAL '33 days'),
(62, 15, 13, 16.5, 'Needs more speaking practice', 2, CURRENT_DATE - INTERVAL '28 days'),
(63, 16, 13, 68.0, 'Satisfactory midterm', 2, CURRENT_DATE - INTERVAL '18 days'),

-- Class 4 (Ongoing) - Business English B2 - Students 21-30
-- Scores for assessments 19-22

-- Student 21 (ID: 21) - Excellent business student
(64, 19, 21, 23.0, 'Outstanding business vocabulary', 3, CURRENT_DATE - INTERVAL '40 days'),
(65, 20, 21, 46.0, 'Excellent presentation with clear analysis', 3, CURRENT_DATE - INTERVAL '23 days'),
(66, 21, 21, 28.0, 'Professional and well-structured emails', 3, CURRENT_DATE - INTERVAL '28 days'),
(67, 22, 21, 90.0, 'Excellent midterm - strong business knowledge', 3, CURRENT_DATE - INTERVAL '13 days'),

-- Student 22 (ID: 22) - Good business student
(68, 19, 22, 20.0, 'Good business terminology', 3, CURRENT_DATE - INTERVAL '40 days'),
(69, 20, 22, 41.0, 'Well-structured presentation', 3, CURRENT_DATE - INTERVAL '23 days'),
(70, 21, 22, 25.0, 'Professional email style', 3, CURRENT_DATE - INTERVAL '28 days'),
(71, 22, 22, 81.0, 'Good midterm result', 3, CURRENT_DATE - INTERVAL '13 days'),

-- Student 23 (ID: 23) - Average business student
(72, 19, 23, 17.0, 'Fair business vocabulary', 3, CURRENT_DATE - INTERVAL '40 days'),
(73, 20, 23, 35.0, 'Adequate presentation', 3, CURRENT_DATE - INTERVAL '23 days'),
(74, 21, 23, 21.0, 'Basic email structure', 3, CURRENT_DATE - INTERVAL '28 days'),
(75, 22, 23, 71.0, 'Satisfactory midterm', 3, CURRENT_DATE - INTERVAL '13 days'),

-- Class 5 (Ongoing) - Japanese N5 - Students 31-40
-- Scores for assessments 25-28

-- Student 31 (ID: 31) - Excellent Japanese student
(76, 25, 31, 28.0, 'Perfect hiragana mastery', 10, CURRENT_DATE - INTERVAL '53 days'),
(77, 26, 31, 29.0, 'Excellent katakana knowledge', 10, CURRENT_DATE - INTERVAL '38 days'),
(78, 27, 31, 23.0, 'Outstanding kanji memorization', 10, CURRENT_DATE - INTERVAL '23 days'),
(79, 28, 31, 23.5, 'Natural Japanese self-introduction', 10, CURRENT_DATE - INTERVAL '33 days'),

-- Student 32 (ID: 32) - Good Japanese student
(80, 25, 32, 25.0, 'Good hiragana skills', 10, CURRENT_DATE - INTERVAL '53 days'),
(81, 26, 32, 26.0, 'Strong katakana knowledge', 10, CURRENT_DATE - INTERVAL '38 days'),
(82, 27, 32, 20.0, 'Good kanji memorization', 10, CURRENT_DATE - INTERVAL '23 days'),
(83, 28, 32, 20.5, 'Clear self-introduction', 10, CURRENT_DATE - INTERVAL '33 days'),

-- Student 33 (ID: 33) - Average Japanese student
(84, 25, 33, 21.0, 'Fair hiragana knowledge', 10, CURRENT_DATE - INTERVAL '53 days'),
(85, 26, 33, 22.0, 'Adequate katakana skills', 10, CURRENT_DATE - INTERVAL '38 days'),
(86, 27, 33, 17.0, 'Needs more kanji practice', 10, CURRENT_DATE - INTERVAL '23 days'),
(87, 28, 33, 17.5, 'Basic self-introduction', 10, CURRENT_DATE - INTERVAL '33 days');

-- Reset sequence
SELECT setval('score_id_seq', (SELECT MAX(id) FROM score));

-- =========================================
-- COMPLETION MESSAGE
-- =========================================

DO $$
BEGIN
    RAISE NOTICE '=========================================';
    RAISE NOTICE 'EMS Seed Data Inserted Successfully!';
    RAISE NOTICE '=========================================';
    RAISE NOTICE '';
    RAISE NOTICE 'Summary:';
    RAISE NOTICE '  - 1 Center, 1 Branch';
    RAISE NOTICE '  - 8 Roles, 80 User Accounts';
    RAISE NOTICE '  - 17 Teachers with skills, 55 Students';
    RAISE NOTICE '  - 15 Resources (10 rooms + 5 Zoom accounts)';
    RAISE NOTICE '  - 10 Time Slot Templates';
    RAISE NOTICE '  - 2 Subjects (English, Japanese)';
    RAISE NOTICE '  - 11 Levels (6 CEFR + 5 JLPT)';
    RAISE NOTICE '  - 12 Courses (6 English + 6 Japanese)';
    RAISE NOTICE '  - 10 PLOs (Program Learning Outcomes)';
    RAISE NOTICE '  - 16 CLOs (Course Learning Outcomes)';
    RAISE NOTICE '  - PLO-CLO mappings and Course Session-CLO mappings';
    RAISE NOTICE '  - 17 Course Materials (PDFs, worksheets, etc.)';
    RAISE NOTICE '  - 18 Course Assessment Templates';
    RAISE NOTICE '  - 9 Course Phases, 39 Course Sessions';
    RAISE NOTICE '  - 12 Classes (2 completed, 4 ongoing, 4 scheduled, 2 draft)';
    RAISE NOTICE '  - 83+ Sessions with realistic status';
    RAISE NOTICE '  - 50 Enrollments with varied statuses';
    RAISE NOTICE '  - Student Sessions with diverse attendance patterns';
    RAISE NOTICE '  - Teaching Slots and Session Resources assigned';
    RAISE NOTICE '  - Sample Student & Teacher Requests';
    RAISE NOTICE '  - 30 Class Assessment Instances';
    RAISE NOTICE '  - 87 Student Score Records';
    RAISE NOTICE '  - Additional 15 Students NOT ENROLLED (for testing enrollment queries)';
    RAISE NOTICE '';
    RAISE NOTICE 'Database ready for testing!';
    RAISE NOTICE '=========================================';
END $$;

-- =========================================
-- ADDITIONAL TEST DATA: STUDENTS NOT ENROLLED IN ANY CLASS
-- Purpose: Test query for available students to enroll
-- These students are assigned to Branch 1 via user_branches
-- But have NO enrollments yet
-- =========================================

-- Additional User Accounts for Students (IDs: 81-95)
INSERT INTO user_account (id, email, phone, facebook_url, full_name, dob, address, password_hash, status, last_login_at, created_at, updated_at)
VALUES
(81, 'student056@gmail.com', '+84-922-111-111', NULL, 'Nguyen Van Hung', '1998-03-15', '290 Trang Thi, Hoan Kiem, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '1 hour', NOW() - INTERVAL '5 days', NOW()),
(82, 'student057@gmail.com', '+84-922-222-222', NULL, 'Tran Thi Lan', '2001-07-22', '295 Hang Bac, Hoan Kiem, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '5 days', NOW()),
(83, 'student058@gmail.com', '+84-922-333-333', NULL, 'Le Van Minh', '1999-11-08', '300 Hang Gai, Hoan Kiem, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '3 hours', NOW() - INTERVAL '5 days', NOW()),
(84, 'student059@gmail.com', '+84-922-444-444', NULL, 'Pham Thi Nga', '2002-04-30', '305 Cau Go, Hoan Kiem, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '4 hours', NOW() - INTERVAL '4 days', NOW()),
(85, 'student060@gmail.com', '+84-922-555-555', NULL, 'Hoang Van Phong', '1997-09-17', '310 Hang Bong, Hoan Kiem, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '5 hours', NOW() - INTERVAL '4 days', NOW()),
(86, 'student061@gmail.com', '+84-923-111-111', NULL, 'Vo Thi Quynh', '2000-12-03', '315 Hang Dao, Hoan Kiem, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '6 hours', NOW() - INTERVAL '4 days', NOW()),
(87, 'student062@gmail.com', '+84-923-222-222', NULL, 'Dang Van Son', '1996-06-25', '320 Hang Ngang, Hoan Kiem, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '7 hours', NOW() - INTERVAL '3 days', NOW()),
(88, 'student063@gmail.com', '+84-923-333-333', NULL, 'Bui Thi Thao', '2003-02-11', '325 Hang Duong, Hoan Kiem, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '8 hours', NOW() - INTERVAL '3 days', NOW()),
(89, 'student064@gmail.com', '+84-923-444-444', NULL, 'Do Van Tuan', '1998-10-19', '330 Hang Ma, Hoan Kiem, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '9 hours', NOW() - INTERVAL '3 days', NOW()),
(90, 'student065@gmail.com', '+84-923-555-555', NULL, 'Ngo Thi Uyen', '2001-05-07', '335 Hang Buom, Hoan Kiem, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '10 hours', NOW() - INTERVAL '2 days', NOW()),
(91, 'student066@gmail.com', '+84-924-111-111', NULL, 'Truong Van Vinh', '1999-08-14', '340 Hang Chieu, Hoan Kiem, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '11 hours', NOW() - INTERVAL '2 days', NOW()),
(92, 'student067@gmail.com', '+84-924-222-222', NULL, 'Duong Thi Xuan', '2002-11-29', '345 Hang Giay, Hoan Kiem, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '12 hours', NOW() - INTERVAL '2 days', NOW()),
(93, 'student068@gmail.com', '+84-924-333-333', NULL, 'Ly Van Yen', '1997-04-06', '350 Hang Thiec, Hoan Kiem, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NOW()),
(94, 'student069@gmail.com', '+84-924-444-444', NULL, 'Mac Thi Anh', '2000-09-23', '355 Hang Bac, Hoan Kiem, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NOW()),
(95, 'student070@gmail.com', '+84-924-555-555', NULL, 'Vu Van Binh', '2003-01-18', '360 Hang Quat, Hoan Kiem, Hanoi', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'active', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', NOW());

-- Assign STUDENT role to new users
INSERT INTO user_role (user_id, role_id)
VALUES
(81, 7), (82, 7), (83, 7), (84, 7), (85, 7),
(86, 7), (87, 7), (88, 7), (89, 7), (90, 7),
(91, 7), (92, 7), (93, 7), (94, 7), (95, 7);

-- Assign students to Branch 1 (Main Campus) via user_branches
-- This makes them eligible to enroll in classes at Branch 1
INSERT INTO user_branches (user_id, branch_id, assigned_at, assigned_by)
VALUES
(81, 1, NOW() - INTERVAL '5 days', 4),  -- Assigned by Academic Staff 1
(82, 1, NOW() - INTERVAL '5 days', 4),
(83, 1, NOW() - INTERVAL '5 days', 4),
(84, 1, NOW() - INTERVAL '4 days', 4),
(85, 1, NOW() - INTERVAL '4 days', 4),
(86, 1, NOW() - INTERVAL '4 days', 5),  -- Assigned by Academic Staff 2
(87, 1, NOW() - INTERVAL '3 days', 5),
(88, 1, NOW() - INTERVAL '3 days', 5),
(89, 1, NOW() - INTERVAL '3 days', 5),
(90, 1, NOW() - INTERVAL '2 days', 5),
(91, 1, NOW() - INTERVAL '2 days', 5),
(92, 1, NOW() - INTERVAL '2 days', 4),
(93, 1, NOW() - INTERVAL '1 day', 4),
(94, 1, NOW() - INTERVAL '1 day', 4),
(95, 1, NOW() - INTERVAL '1 day', 4);

-- Create Student records for these new users
INSERT INTO student (id, user_id, student_code, level, created_at, updated_at)
VALUES
(56, 81, 'S056', 'Beginner', NOW() - INTERVAL '5 days', NOW()),
(57, 82, 'S057', 'Beginner', NOW() - INTERVAL '5 days', NOW()),
(58, 83, 'S058', 'Intermediate', NOW() - INTERVAL '5 days', NOW()),
(59, 84, 'S059', 'Advanced', NOW() - INTERVAL '4 days', NOW()),
(60, 85, 'S060', 'Beginner', NOW() - INTERVAL '4 days', NOW()),
(61, 86, 'S061', 'Beginner', NOW() - INTERVAL '4 days', NOW()),
(62, 87, 'S062', 'Intermediate', NOW() - INTERVAL '3 days', NOW()),
(63, 88, 'S063', 'Advanced', NOW() - INTERVAL '3 days', NOW()),
(64, 89, 'S064', 'Beginner', NOW() - INTERVAL '3 days', NOW()),
(65, 90, 'S065', 'Intermediate', NOW() - INTERVAL '2 days', NOW()),
(66, 91, 'S066', 'Beginner', NOW() - INTERVAL '2 days', NOW()),
(67, 92, 'S067', 'Advanced', NOW() - INTERVAL '2 days', NOW()),
(68, 93, 'S068', 'Beginner', NOW() - INTERVAL '1 day', NOW()),
(69, 94, 'S069', 'Intermediate', NOW() - INTERVAL '1 day', NOW()),
(70, 95, 'S070', 'Beginner', NOW() - INTERVAL '1 day', NOW());

-- Note: Students 51-55 (from existing data) also have NO enrollments
-- So now you have 20 students total (51-70) without any enrollments for testing

COMMENT ON TABLE student IS 'Students 1-50: Enrolled in various classes. Students 51-70: NOT ENROLLED (available for testing enrollment queries).';

-- =========================================
-- ADDITIONAL SKILL ASSESSMENTS FOR REMAINING STUDENTS
-- =========================================
-- Adding assessments for students who don't have any yet

INSERT INTO replacement_skill_assessment (student_id, skill, level_id, score, assessment_date, assessment_type, note, assessed_by, created_at, updated_at)
VALUES
-- Student 4 (S004) - Beginner English
(4, 'general', 1, 30, CURRENT_DATE - INTERVAL '80 days', 'placement_test', 'Complete beginner in English', 4, NOW() - INTERVAL '80 days', NOW()),
(4, 'listening', 1, 28, CURRENT_DATE - INTERVAL '80 days', 'placement_test', 'Weak listening skills', 4, NOW() - INTERVAL '80 days', NOW()),

-- Student 6 (S006) - Beginner English
(6, 'general', 1, 32, CURRENT_DATE - INTERVAL '70 days', 'placement_test', 'Starting from basics', 4, NOW() - INTERVAL '70 days', NOW()),

-- Student 8 (S008) - Beginner English
(8, 'general', 1, 35, CURRENT_DATE - INTERVAL '60 days', 'placement_test', 'Some basic knowledge', 4, NOW() - INTERVAL '60 days', NOW()),
(8, 'reading', 1, 38, CURRENT_DATE - INTERVAL '60 days', 'placement_test', 'Can read simple texts', 4, NOW() - INTERVAL '60 days', NOW()),

-- Student 9 (S009) - Beginner English
(9, 'general', 1, 28, CURRENT_DATE - INTERVAL '55 days', 'placement_test', 'Elementary level', 4, NOW() - INTERVAL '55 days', NOW()),

-- Student 12 (S012) - Beginner English
(12, 'general', 2, 45, CURRENT_DATE - INTERVAL '42 days', 'placement_test', 'Pre-intermediate level', 4, NOW() - INTERVAL '42 days', NOW()),
(12, 'speaking', 2, 42, CURRENT_DATE - INTERVAL '42 days', 'placement_test', 'Basic conversation skills', 4, NOW() - INTERVAL '42 days', NOW()),

-- Student 13 (S013) - Beginner English
(13, 'general', 1, 33, CURRENT_DATE - INTERVAL '38 days', 'placement_test', 'Elementary English', 4, NOW() - INTERVAL '38 days', NOW()),

-- Student 16 (S016) - Beginner English
(16, 'general', 1, 30, CURRENT_DATE - INTERVAL '26 days', 'placement_test', 'Basic English skills', 4, NOW() - INTERVAL '26 days', NOW()),

-- Student 17 (S017) - Beginner English
(17, 'general', 1, 35, CURRENT_DATE - INTERVAL '24 days', 'placement_test', 'Elementary level confirmed', 4, NOW() - INTERVAL '24 days', NOW()),
(17, 'writing', 1, 32, CURRENT_DATE - INTERVAL '24 days', 'placement_test', 'Weak writing skills', 4, NOW() - INTERVAL '24 days', NOW()),

-- Student 19 (S019) - Beginner English
(19, 'general', 1, 28, CURRENT_DATE - INTERVAL '20 days', 'placement_test', 'True beginner', 4, NOW() - INTERVAL '20 days', NOW()),

-- Student 22 (S022) - Beginner English
(22, 'general', 2, 48, CURRENT_DATE - INTERVAL '14 days', 'placement_test', 'A2 level, ready for elementary course', 4, NOW() - INTERVAL '14 days', NOW()),
(22, 'reading', 2, 50, CURRENT_DATE - INTERVAL '14 days', 'placement_test', 'Reading comprehension adequate', 4, NOW() - INTERVAL '14 days', NOW()),

-- Student 23 (S023) - Beginner English
(23, 'general', 1, 32, CURRENT_DATE - INTERVAL '12 days', 'placement_test', 'Starting English journey', 4, NOW() - INTERVAL '12 days', NOW()),

-- Student 26 (S026) - Beginner English
(26, 'general', 1, 30, CURRENT_DATE - INTERVAL '9 days', 'placement_test', 'Basic level', 4, NOW() - INTERVAL '9 days', NOW()),

-- Student 27 (S027) - Beginner English
(27, 'general', 1, 35, CURRENT_DATE - INTERVAL '8 days', 'placement_test', 'Elementary English', 4, NOW() - INTERVAL '8 days', NOW()),

-- Student 28 (S028) - Intermediate English
(28, 'general', 3, 60, CURRENT_DATE - INTERVAL '7 days', 'placement_test', 'B1 level confirmed', 6, NOW() - INTERVAL '7 days', NOW()),
(28, 'speaking', 3, 58, CURRENT_DATE - INTERVAL '7 days', 'placement_test', 'Good speaking ability', 6, NOW() - INTERVAL '7 days', NOW()),

-- Student 30 (S030) - Beginner English
(30, 'general', 1, 28, CURRENT_DATE - INTERVAL '5 days', 'placement_test', 'Complete beginner', 4, NOW() - INTERVAL '5 days', NOW()),

-- Student 31 (S031) - Beginner English
(31, 'general', 1, 32, CURRENT_DATE - INTERVAL '48 days', 'placement_test', 'Elementary level', 4, NOW() - INTERVAL '48 days', NOW()),

-- Student 32 (S032) - Intermediate English
(32, 'general', 3, 62, CURRENT_DATE - INTERVAL '46 days', 'placement_test', 'Solid B1 level', 6, NOW() - INTERVAL '46 days', NOW()),
(32, 'listening', 3, 60, CURRENT_DATE - INTERVAL '46 days', 'placement_test', 'Good listening comprehension', 6, NOW() - INTERVAL '46 days', NOW()),

-- Student 33 (S033) - Beginner English
(33, 'general', 1, 30, CURRENT_DATE - INTERVAL '44 days', 'placement_test', 'True beginner', 4, NOW() - INTERVAL '44 days', NOW()),

-- Student 36 (S036) - Beginner English
(36, 'general', 1, 35, CURRENT_DATE - INTERVAL '38 days', 'placement_test', 'Basic English', 4, NOW() - INTERVAL '38 days', NOW()),

-- Student 37 (S037) - Beginner English
(37, 'general', 1, 28, CURRENT_DATE - INTERVAL '36 days', 'placement_test', 'Elementary level', 4, NOW() - INTERVAL '36 days', NOW()),

-- Student 38 (S038) - Intermediate English
(38, 'general', 3, 58, CURRENT_DATE - INTERVAL '34 days', 'placement_test', 'Intermediate level', 6, NOW() - INTERVAL '34 days', NOW()),
(38, 'writing', 3, 55, CURRENT_DATE - INTERVAL '34 days', 'placement_test', 'Writing needs improvement', 6, NOW() - INTERVAL '34 days', NOW()),

-- Student 40 (S040) - Beginner English
(40, 'general', 1, 30, CURRENT_DATE - INTERVAL '30 days', 'placement_test', 'Complete beginner', 4, NOW() - INTERVAL '30 days', NOW()),

-- Student 41 (S041) - Beginner English
(41, 'general', 1, 32, CURRENT_DATE - INTERVAL '28 days', 'placement_test', 'Elementary English', 4, NOW() - INTERVAL '28 days', NOW()),

-- Student 42 (S042) - Intermediate English
(42, 'general', 3, 60, CURRENT_DATE - INTERVAL '26 days', 'placement_test', 'B1 level', 6, NOW() - INTERVAL '26 days', NOW()),
(42, 'speaking', 3, 62, CURRENT_DATE - INTERVAL '26 days', 'placement_test', 'Strong speaking skills', 6, NOW() - INTERVAL '26 days', NOW()),

-- Student 44 (S044) - Beginner English
(44, 'general', 1, 28, CURRENT_DATE - INTERVAL '22 days', 'placement_test', 'True beginner', 4, NOW() - INTERVAL '22 days', NOW()),

-- Student 45 (S045) - Intermediate English
(45, 'general', 3, 58, CURRENT_DATE - INTERVAL '20 days', 'placement_test', 'Intermediate level', 6, NOW() - INTERVAL '20 days', NOW()),

-- Student 46 (S046) - Beginner English
(46, 'general', 1, 30, CURRENT_DATE - INTERVAL '18 days', 'placement_test', 'Basic English', 4, NOW() - INTERVAL '18 days', NOW()),

-- Student 47 (S047) - Beginner English
(47, 'general', 1, 35, CURRENT_DATE - INTERVAL '16 days', 'placement_test', 'Elementary level', 4, NOW() - INTERVAL '16 days', NOW()),

-- Student 48 (S048) - Advanced English
(48, 'general', 4, 75, CURRENT_DATE - INTERVAL '14 days', 'placement_test', 'Advanced level, B2', 6, NOW() - INTERVAL '14 days', NOW()),
(48, 'reading', 4, 78, CURRENT_DATE - INTERVAL '14 days', 'placement_test', 'Excellent reading', 6, NOW() - INTERVAL '14 days', NOW()),

-- Student 49 (S049) - Intermediate English
(49, 'general', 3, 60, CURRENT_DATE - INTERVAL '12 days', 'placement_test', 'Solid B1', 6, NOW() - INTERVAL '12 days', NOW()),

-- Student 50 (S050) - Beginner English
(50, 'general', 1, 32, CURRENT_DATE - INTERVAL '10 days', 'placement_test', 'Elementary English', 4, NOW() - INTERVAL '10 days', NOW()),

-- Student 51 (S051) - Beginner English
(51, 'general', 1, 28, CURRENT_DATE - INTERVAL '8 days', 'placement_test', 'True beginner', 4, NOW() - INTERVAL '8 days', NOW()),

-- Student 52 (S052) - Advanced English
(52, 'general', 4, 80, CURRENT_DATE - INTERVAL '6 days', 'placement_test', 'Advanced level confirmed', 6, NOW() - INTERVAL '6 days', NOW()),
(52, 'speaking', 4, 82, CURRENT_DATE - INTERVAL '6 days', 'placement_test', 'Fluent speaker', 6, NOW() - INTERVAL '6 days', NOW()),

-- Student 53 (S053) - Intermediate English
(53, 'general', 3, 58, CURRENT_DATE - INTERVAL '4 days', 'placement_test', 'B1 level', 6, NOW() - INTERVAL '4 days', NOW()),

-- Student 54 (S054) - Beginner English
(54, 'general', 1, 30, CURRENT_DATE - INTERVAL '2 days', 'placement_test', 'Basic English skills', 4, NOW() - INTERVAL '2 days', NOW()),

-- Student 55 (S055) - Beginner English
(55, 'general', 1, 32, CURRENT_DATE - INTERVAL '1 day', 'placement_test', 'Elementary level', 4, NOW() - INTERVAL '1 day', NOW()),

-- Students 56-70 (newly added students)
-- Student 56 (S056) - Intermediate English
(56, 'general', 3, 60, CURRENT_DATE - INTERVAL '5 days', 'placement_test', 'B1 level', 6, NOW() - INTERVAL '5 days', NOW()),

-- Student 57 (S057) - Beginner English
(57, 'general', 1, 28, CURRENT_DATE - INTERVAL '5 days', 'placement_test', 'True beginner', 4, NOW() - INTERVAL '5 days', NOW()),

-- Student 58 (S058) - Intermediate English
(58, 'general', 3, 62, CURRENT_DATE - INTERVAL '5 days', 'placement_test', 'Solid B1', 6, NOW() - INTERVAL '5 days', NOW()),
(58, 'listening', 3, 60, CURRENT_DATE - INTERVAL '5 days', 'placement_test', 'Good listening', 6, NOW() - INTERVAL '5 days', NOW()),

-- Student 59 (S059) - Advanced English
(59, 'general', 4, 78, CURRENT_DATE - INTERVAL '4 days', 'placement_test', 'Advanced B2', 6, NOW() - INTERVAL '4 days', NOW()),

-- Student 60 (S060) - Beginner English
(60, 'general', 1, 30, CURRENT_DATE - INTERVAL '4 days', 'placement_test', 'Basic level', 4, NOW() - INTERVAL '4 days', NOW()),

-- Student 61 (S061) - Beginner English
(61, 'general', 1, 32, CURRENT_DATE - INTERVAL '4 days', 'placement_test', 'Elementary English', 4, NOW() - INTERVAL '4 days', NOW()),

-- Student 62 (S062) - Intermediate English
(62, 'general', 3, 58, CURRENT_DATE - INTERVAL '3 days', 'placement_test', 'B1 level', 6, NOW() - INTERVAL '3 days', NOW()),

-- Student 63 (S063) - Advanced English
(63, 'general', 4, 75, CURRENT_DATE - INTERVAL '3 days', 'placement_test', 'Advanced level', 6, NOW() - INTERVAL '3 days', NOW()),
(63, 'speaking', 4, 78, CURRENT_DATE - INTERVAL '3 days', 'placement_test', 'Excellent speaking', 6, NOW() - INTERVAL '3 days', NOW()),

-- Student 64 (S064) - Beginner English
(64, 'general', 1, 28, CURRENT_DATE - INTERVAL '3 days', 'placement_test', 'True beginner', 4, NOW() - INTERVAL '3 days', NOW()),

-- Student 65 (S065) - Intermediate English
(65, 'general', 3, 60, CURRENT_DATE - INTERVAL '2 days', 'placement_test', 'Solid B1', 6, NOW() - INTERVAL '2 days', NOW()),

-- Student 66 (S066) - Beginner English
(66, 'general', 1, 30, CURRENT_DATE - INTERVAL '2 days', 'placement_test', 'Basic English', 4, NOW() - INTERVAL '2 days', NOW()),

-- Student 67 (S067) - Advanced English
(67, 'general', 4, 80, CURRENT_DATE - INTERVAL '2 days', 'placement_test', 'Advanced B2/C1', 6, NOW() - INTERVAL '2 days', NOW()),
(67, 'reading', 4, 82, CURRENT_DATE - INTERVAL '2 days', 'placement_test', 'Strong reading skills', 6, NOW() - INTERVAL '2 days', NOW()),

-- Student 68 (S068) - Beginner English
(68, 'general', 1, 32, CURRENT_DATE - INTERVAL '1 day', 'placement_test', 'Elementary level', 4, NOW() - INTERVAL '1 day', NOW()),

-- Student 69 (S069) - Intermediate English
(69, 'general', 3, 58, CURRENT_DATE - INTERVAL '1 day', 'placement_test', 'B1 level', 6, NOW() - INTERVAL '1 day', NOW()),

-- Student 70 (S070) - Beginner English
(70, 'general', 1, 28, CURRENT_DATE - INTERVAL '1 day', 'placement_test', 'True beginner', 4, NOW() - INTERVAL '1 day', NOW());

-- Reset sequence for replacement_skill_assessment
SELECT setval('replacement_skill_assessment_id_seq', (SELECT MAX(id) FROM replacement_skill_assessment));

-- Update sequences to reflect new data
SELECT setval('user_account_id_seq', (SELECT MAX(id) FROM user_account));
SELECT setval('student_id_seq', (SELECT MAX(id) FROM student));
