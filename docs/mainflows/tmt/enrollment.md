
STUDENT ENROLLMENT
GIAI ĐOẠN 1: KHỞI TẠO & XEM DANH SÁCH (Initialization)
Step 1: [Academic Affairs] Vào Chi Tiết Lớp
Giáo vụ truy cập vào trang chi tiết lớp học (Class Detail page)
Tại đây hiển thị thông tin lớp và danh sách học viên đã được ghi danh

Step 2: [Academic Affairs] Click "Ghi danh học viên"
Giáo vụ nhấn nút "Ghi danh học viên" để bắt đầu quy trình enrollment

Step 3: [System] Kiểm tra class status (scheduled/ongoing)
Hệ thống kiểm tra trạng thái lớp học
Chỉ cho phép ghi danh nếu lớp ở trạng thái "scheduled" hoặc "ongoing"
Không cho phép ghi danh nếu lớp đã "completed" hoặc "cancelled"

Step 4: [System] Load danh sách students khả dụng (chưa enroll trong lớp)
Hệ thống load danh sách tất cả học viên thuộc chi nhánh (branch)
Lọc ra những học viên chưa được ghi danh vào lớp này
Danh sách này sẽ là nguồn để giáo vụ chọn

WITH class_info AS (
    SELECT branch_id FROM class WHERE id = 2
),
enrolled_students AS (
    -- Lấy danh sách students đã enroll vào lớp này
    SELECT student_id
    FROM enrollment
    WHERE class_id = 2
      AND status NOT IN ('dropped', 'transferred')
)
SELECT DISTINCT
    s.id AS student_id,
    u.full_name,
    u.email,
    u.phone,
    s.student_code,
    s.education_level,
    s.created_at
FROM student s
INNER JOIN user_account u ON s.user_id = u.id
-- Kiểm tra student thuộc branch của class (qua user_branches)
INNER JOIN user_branches ub ON u.id = ub.user_id
INNER JOIN class_info ci ON ub.branch_id = ci.branch_id
-- Loại trừ students đã enroll
WHERE s.id NOT IN (SELECT student_id FROM enrolled_students)
  AND u.status = 'active'  -- Chỉ lấy user active
ORDER BY u.full_name;


-- Kết quả: Danh sách students khả dụng để chọn


Step 5: [Academic Affairs] Xem danh sách students khả dụng
Giáo vụ xem danh sách học viên có thể ghi danh
Danh sách hiển thị thông tin: họ tên, email, số điện thoại, v.v.

Step 6: [System] Hiển thị 3 options
Hệ thống hiển thị 3 lựa chọn cho giáo vụ:
Option A: Chọn từ danh sách có sẵn
Option B: Thêm học viên mới (thêm lẻ)
Option C: Import CSV

Step 7: [Academic Affairs] Chọn hành động
Giáo vụ quyết định chọn một trong 3 phương thức

GIAI ĐOẠN 2A: OPTION A - CHỌN HỌC VIÊN CÓ SẴN
Step 8A: [Academic Affairs] OPTION A: Chọn student có sẵn từ danh sách
Giáo vụ chọn phương thức "Chọn từ danh sách có sẵn"



Step 9A: [System] Enable checkboxes để chọn students
Hệ thống hiển thị checkbox bên cạnh mỗi học viên trong danh sách
Giáo vụ có thể chọn một hoặc nhiều học viên

Step 10A: Nhảy đến Step 20 (Chọn students từ DS đã update)
Giáo vụ tiến hành chọn các học viên cần ghi danh

GIAI ĐOẠN 2B: OPTION B - THÊM HỌC VIÊN MỚI (LẺ)
Step 8B: [Academic Affairs] OPTION B: Click "Thêm học viên mới"
Giáo vụ chọn phương thức thêm học viên mới từng người

Step 9B: [System] Hiển thị form tạo student mới
Hệ thống mở form nhập thông tin học viên mới
Form bao gồm: Họ tên, email, số điện thoại, ngày sinh, địa chỉ, v.v.

Step 10B: [Academic Affairs] Điền form thông tin student
Giáo vụ điền đầy đủ thông tin học viên mới vào form

Step 11B: [Academic Affairs] Click "Lưu và Thêm vào DS"
Giáo vụ nhấn nút "Lưu và Thêm vào Danh Sách"

Step 12B: [System] Validate input
Hệ thống validate thông tin:
Email unique (không trùng trong hệ thống)
Phone format (đúng định dạng số điện thoại)
Required fields (các trường bắt buộc phải có giá trị)

-- Validate 1: Kiểm tra email đã tồn tại chưa
SELECT COUNT(*) AS email_exists
FROM user_account
WHERE LOWER(email) = LOWER(:email);

-- Nếu email_exists > 0 → Email đã tồn tại, không cho phép tạo mới
-- Nếu email_exists = 0 → Email hợp lệ, tiếp tục

-- Validate 2: Kiểm tra phone đã tồn tại chưa (optional, nếu yêu cầu unique)
SELECT COUNT(*) AS phone_exists
FROM user_account
WHERE phone = :phone;

-- Validate 3: Kiểm tra student_code đã tồn tại chưa (nếu có)
SELECT COUNT(*) AS code_exists
FROM student
WHERE student_code = :studentCode;


Step 13B: [System] CREATE: 1. user_account, 2. student record
Hệ thống tạo tài khoản user_account (nếu email chưa tồn tại)
Tạo bản ghi student record liên kết với user_account
Gán role STUDENT cho user

INSERT INTO user_account (
    email,
    phone,
    full_name,
    password_hash,
    status,
    created_at,
    updated_at
) VALUES (
    'testuser001@gmail.com',
    '+84-925-111-111',
    'Nguyen Van Test 1',
    '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6',  -- password: "password123"
    'active',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
RETURNING id;
-- Expected: Returns user_id (e.g., 96)


-- Step 2: Assign STUDENT role (giả sử user_id = 96)
INSERT INTO user_role (user_id, role_id)
VALUES (
    96,  -- user_id từ bước 1
    (SELECT id FROM role WHERE code = 'STUDENT')
);


-- Step 3: Assign to Branch 1 (Main Campus)
INSERT INTO user_branches (user_id, branch_id, assigned_at, assigned_by)
VALUES (
    96,  -- user_id từ bước 1
    1,   -- Branch 1 (Main Campus)
    CURRENT_TIMESTAMP,
    4    -- Assigned by Academic Affairs 1 (user_id = 4)
);


-- Step 4: Create student record
INSERT INTO student (
    user_id,
    student_code,
    education_level,
    address,
    created_at,
    updated_at
) VALUES (
    96,     -- user_id từ bước 1
    'S071',  -- Student code (tiếp theo từ S070)
    'Undergraduate',
    'Hanoi, Vietnam',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
RETURNING id;


Step 14B: [System] Thêm student mới vào DS khả dụng
Hệ thống thêm học viên mới vào danh sách khả dụng
Refresh danh sách để hiển thị học viên vừa thêm

Step 15B: Chuyển đến Step 20 (Chọn students từ DS đã update)
Giáo vụ tiếp tục chọn học viên (bao gồm cả học viên vừa thêm) để ghi danh

GIAI ĐOẠN 2C: OPTION C - IMPORT CSV
Step 8C: [Academic Affairs] OPTION C: Click "Import CSV"
Giáo vụ chọn phương thức import hàng loạt từ file CSV

Step 9C: [Academic Affairs] Upload file CSV

Giáo vụ chọn và upload file CSV chứa thông tin học viên
File CSV phải tuân theo template chuẩn của hệ thống

Step 10C: [System] Parse và validate CSV
Hệ thống đọc và phân tích file CSV
Validate từng dòng:
Format đúng (số cột, định dạng dữ liệu)
Required fields có đủ không
Email/phone đã tồn tại hay chưa

-- Validate từng dòng CSV (trong code, không phải SQL thuần)
-- Nhưng cần check email/phone duplicates batch:

-- Query để check multiple emails một lúc
SELECT email
FROM user_account
WHERE email = ANY(:emailArray);  -- PostgreSQL array parameter

-- Query để check multiple phones một lúc
SELECT phone
FROM user_account
WHERE phone = ANY(:phoneArray);

-- Query để check multiple student codes
SELECT student_code
FROM student
WHERE student_code = ANY(:studentCodeArray);

-- Kết quả: Danh sách emails/phones/codes đã tồn tại
-- Code logic sẽ so sánh với input để đánh dấu warning/error


Step 11C: [System] Hiển thị preview
Hệ thống hiển thị preview kết quả validation:
Valid: Các bản ghi hợp lệ, sẵn sàng import
Warning: Các bản ghi có cảnh báo (ví dụ: email đã tồn tại, sẽ skip)
Error: Các bản ghi lỗi, không thể import (ví dụ: thiếu thông tin bắt buộc)

Step 12C: [Academic Affairs] Review preview data
Giáo vụ xem xét kết quả preview
Quyết định có tiếp tục import hay không

Step 13C: [Academic Affairs] Click "Import vào DS"
Giáo vụ xác nhận import các bản ghi valid vào hệ thống

Step 14C: [System] Batch CREATE: user_account + student cho valid records
Hệ thống thực hiện tạo hàng loạt:
Tạo user_account cho các email mới
Tạo student record cho từng học viên valid
Gán role STUDENT

-- Batch insert user_accounts (sử dụng VALUES multiple rows)
WITH new_users AS (
    INSERT INTO user_account (email, phone, full_name, password_hash, status, created_at, updated_at)
    VALUES 
        (:email1, :phone1, :fullName1, :passwordHash1, 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
        (:email2, :phone2, :fullName2, :passwordHash2, 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
        (:email3, :phone3, :fullName3, :passwordHash3, 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        -- ... more rows
    RETURNING id, email
)
SELECT * FROM new_users;

-- Sau đó batch insert user_role
INSERT INTO user_role (user_id, role_id)
SELECT nu.id, r.id
FROM new_users nu
CROSS JOIN (SELECT id FROM role WHERE code = 'STUDENT') r;

-- Batch insert user_branches
INSERT INTO user_branches (user_id, branch_id, assigned_at, assigned_by)
SELECT nu.id, :branchId, CURRENT_TIMESTAMP, :currentUserId
FROM new_users nu;

-- Batch insert students
WITH user_mapping AS (
    SELECT id, email FROM user_account WHERE email = ANY(:emailArray)
)
INSERT INTO student (user_id, student_code, education_level, address, created_at, updated_at)
SELECT 
    um.id,
    csv.student_code,
    csv.education_level,
    csv.address,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM (VALUES 
    (:email1, :studentCode1, :educationLevel1, :address1),
    (:email2, :studentCode2, :educationLevel2, :address2)
    -- ... more rows
) AS csv(email, student_code, education_level, address)
INNER JOIN user_mapping um ON csv.email = um.email
RETURNING id AS student_id;


Step 15C: [System] Update DS khả dụng với students mới
Hệ thống cập nhật danh sách khả dụng với các học viên vừa import
Refresh danh sách để hiển thị đầy đủ

Step 16C: Chuyển đến Step 20 (Chọn students từ DS đã update)
Giáo vụ tiếp tục chọn học viên để ghi danh vào lớp

GIAI ĐOẠN 3: GHI DANH VÀO LỚP (Enrollment Process)
Step 20: [Academic Affairs] Chọn students từ DS đã update
Giáo vụ chọn (tick checkbox) các học viên cần ghi danh vào lớp
Có thể chọn một hoặc nhiều học viên

Step 21: [Academic Affairs] Click "Ghi danh vào lớp"
Giáo vụ nhấn nút "Ghi danh vào lớp" để xác nhận

Step 22: [System] Lấy danh sách students được chọn
Hệ thống lấy danh sách tất cả học viên đã được tick checkbox
WITH class_info AS (
    SELECT branch_id FROM class WHERE id = 2
),
enrolled_students AS (
    -- Lấy danh sách students đã enroll vào lớp này
    SELECT student_id
    FROM enrollment
    WHERE class_id = 2
      AND status NOT IN ('dropped', 'transferred')
)
SELECT DISTINCT
    s.id AS student_id,
    u.full_name,
    u.email,
    u.phone,
    s.student_code,
    s.education_level,
    s.created_at
FROM student s
INNER JOIN user_account u ON s.user_id = u.id
-- Kiểm tra student thuộc branch của class (qua user_branches)
INNER JOIN user_branches ub ON u.id = ub.user_id
INNER JOIN class_info ci ON ub.branch_id = ci.branch_id
-- Loại trừ students đã enroll
WHERE s.id NOT IN (SELECT student_id FROM enrolled_students)
  AND u.status = 'active'  -- Chỉ lấy user active
ORDER BY u.full_name;


-- Kết quả: Danh sách students khả dụng để chọn


Step 23: [System] Kiểm tra capacity
Hệ thống kiểm tra sức chứa lớp học:
Tính toán: enrolled_count (đã ghi danh) + selected (đang chọn)
So sánh với max_capacity (sức chứa tối đa)
Nếu (enrolled_count + selected) < max_capacity → OK
Nếu (enrolled_count + selected) ≥ max_capacity → Warning

-- Query để check capacity
SELECT 
    c.id,
    c.max_capacity,
    c.enrolled_count,
    c.max_capacity - c.enrolled_count AS remaining_capacity,
    :selectedCount AS selected_count,
    (c.enrolled_count + :selectedCount) AS total_after_enrollment,
    CASE 
        WHEN (c.enrolled_count + :selectedCount) <= c.max_capacity THEN true
        ELSE false
    END AS capacity_ok
FROM class c
WHERE c.id = :classId;

-- Nếu capacity_ok = false → Hiển thị warning
-- Nếu capacity_ok = true → Tiếp tục


Step 24: [Academic Affairs] Capacity OK?
Giáo vụ quyết định dựa trên kết quả kiểm tra capacity

Step 25a: [YES] Capacity OK → Chuyển đến Step 28

Nếu còn chỗ trống, tiếp tục quy trình enrollment

Step 25b: [NO] Capacity vượt mức → Step 26

Step 26: [System]  Hiển thị cảnh báo vượt capacity
Hệ thống hiển thị thông báo cảnh báo
Thông tin: Lớp sẽ vượt sức chứa tối đa X học viên
Yêu cầu giáo vụ xác nhận override

Step 27: [Academic Affairs] Override với lý do
Giáo vụ nhập lý do vượt capacity (ví dụ: "Học viên VIP", "Yêu cầu từ ban giám đốc")
Xác nhận override để tiếp tục

Step 28: [System] Kiểm tra schedule conflict
Hệ thống kiểm tra xung đột lịch học:
Xem học viên đã có lớp nào trùng lịch không
So sánh schedule_days và time_slot của lớp hiện tại với các lớp khác mà học viên đã đăng ký
Nếu có conflict → Warning (nhưng vẫn cho phép ghi danh nếu giáo vụ xác nhận)

-- Query để check conflict lịch học cho từng student
-- Kiểm tra xem student đã enroll vào lớp nào trùng lịch không

WITH target_class AS (
    SELECT 
        id,
        schedule_days,  -- Array of smallint (2=Mon, 3=Tue, etc.)
        time_slot_id
    FROM class
    WHERE id = :classId
),
student_classes AS (
    -- Lấy tất cả lớp mà các students đang enroll
    SELECT DISTINCT
        e.student_id,
        c.id AS class_id,
        c.name AS class_name,
        c.schedule_days,
        c.time_slot_id
    FROM enrollment e
    INNER JOIN class c ON e.class_id = c.id
    WHERE e.student_id = ANY(:selectedStudentIds)
      AND e.status = 'enrolled'
      AND c.status IN ('scheduled', 'ongoing')
),
conflicts AS (
    SELECT 
        sc.student_id,
        sc.class_name,
        s.full_name AS student_name
    FROM student_classes sc
    CROSS JOIN target_class tc
    INNER JOIN student s ON sc.student_id = s.id
    INNER JOIN user_account u ON s.user_id = u.id
    WHERE sc.time_slot_id = tc.time_slot_id  -- Cùng time slot
      AND sc.schedule_days && tc.schedule_days  -- Array overlap (PostgreSQL operator)
)
SELECT * FROM conflicts;

-- Nếu có kết quả → Warning về conflict (nhưng vẫn cho phép override)
-- Nếu không có kết quả → OK, không có conflict


GIAI ĐOẠN 4: XỬ LÝ TRANSACTION (Database Transaction)

Step 29: [System] BEGIN TRANSACTION
Hệ thống bắt đầu database transaction
Đảm bảo tính toàn vẹn dữ liệu (atomicity)

Step 30: [System] CREATE enrollments cho các students được chọn
Hệ thống tạo bản ghi enrollment cho từng học viên:
enrollment (class_id, student_id, enrolled_at, status='enrolled')
Cập nhật enrolled_count của lớp học

-- Batch insert enrollments
INSERT INTO enrollment (
    class_id,
    student_id,
    status,
    enrolled_at,
    created_at,
    updated_at
)
SELECT 
    :classId,
    student_id,
    'enrolled',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM UNNEST(:selectedStudentIds) AS student_id
RETURNING id AS enrollment_id, student_id;

-- Update enrolled_count của class
UPDATE class
SET 
    enrolled_count = enrolled_count + :selectedCount,
    updated_at = CURRENT_TIMESTAMP
WHERE id = :classId;


Step 31: [System] GENERATE student_session (cho tất cả future sessions của từng student)
Hệ thống sinh các bản ghi student_session:
Lấy tất cả session của lớp có session_date >= today (future sessions)
Với mỗi student được ghi danh:
Tạo student_session (student_id, session_id, attendance_status='planned')
Nếu học viên ghi danh muộn (mid-course), chỉ sinh student_session cho các buổi còn lại

-- Query để lấy tất cả future sessions của class
WITH future_sessions AS (
    SELECT id AS session_id
    FROM session
    WHERE class_id = :classId
      AND session_date >= CURRENT_DATE  -- Chỉ lấy sessions tương lai
      AND status = 'planned'
    ORDER BY session_date, time_slot_id
)
-- Batch insert student_session cho từng student + từng session
INSERT INTO student_session (
    student_id,
    session_id,
    attendance_status,
    is_makeup,
    created_at,
    updated_at
)
SELECT 
    s.student_id,
    fs.session_id,
    'planned',  -- attendance_status_enum
    false,      -- không phải makeup session
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM UNNEST(:selectedStudentIds) AS s(student_id)
CROSS JOIN future_sessions fs;

-- Nếu lớp có 20 sessions và enroll 5 students
-- → Tạo 5 × 20 = 100 student_session records

-- Query để verify số lượng student_session đã tạo:
SELECT 
    COUNT(*) AS total_student_sessions,
    COUNT(DISTINCT student_id) AS students_count,
    COUNT(DISTINCT session_id) AS sessions_count
FROM student_session
WHERE session_id IN (
    SELECT id FROM session WHERE class_id = :classId AND session_date >= CURRENT_DATE
)
AND student_id = ANY(:selectedStudentIds);


Step 32: [System] COMMIT TRANSACTION
Hệ thống commit transaction
Lưu tất cả thay đổi vào database
Nếu có lỗi ở bất kỳ bước nào → ROLLBACK toàn bộ

GIAI ĐOẠN 5: THÔNG BÁO & HOÀN TẤT (Notification & Completion)

Step 33: [System] Gửi email welcome cho từng student (async)
Hệ thống gửi email thông báo cho từng học viên (chạy background job):
Thông tin lớp học: Tên lớp, mã lớp, giáo viên, phòng học
Lịch học: Ngày bắt đầu, thời gian, địa điểm
Link login hệ thống
Thông tin tài khoản: Username (email), mật khẩu mặc định (yêu cầu đổi khi đăng nhập đầu tiên)

Step 34: [System] Hiển thị success

Step 35: [Academic Affairs] Xem thông báo thành công

Step 36: [System] Update danh sách học viên đã enroll trên UI (refresh)

Step 37: [Academic Affairs] Xem danh sách học viên đã enroll

GIAI ĐOẠN 6: HỌC VIÊN NHẬN THÔNG BÁO (Student Perspective)

Step 38: [Student] Học viên nhận email thông báo ghi danh thành công từ hệ thống

Step 39: [Student] Đọc thông tin
Học viên đọc thông tin trong email:
Thông tin lớp học
Lịch học chi tiết
Link đăng nhập hệ thống

Step 40: [Student] Login hệ thống và đổi password
Học viên truy cập link đăng nhập
Sử dụng email và mật khẩu mặc định để đăng nhập lần đầu
Hệ thống yêu cầu đổi mật khẩu mới

Step 41: [Student] Xem lịch học cá nhân
Học viên xem lịch học của mình trong hệ thống
Danh sách các buổi học (sessions) được tự động sinh sẵn
Kết thúc luồng từ phía học viên

-- Input parameters:
-- :studentId - ID của học viên
-- :weekStartDate - Ngày bắt đầu tuần (Monday), format: 'YYYY-MM-DD'
-- :weekEndDate - Ngày kết thúc tuần (Sunday), format: 'YYYY-MM-DD'

WITH student_classes AS (
    -- Lấy tất cả lớp mà student đang enroll
    SELECT DISTINCT
        c.id AS class_id,
        c.name AS class_name,
        c.class_code,
        c.status AS class_status,
        c.modality,
        e.status AS enrollment_status,
        e.enrolled_at,
        co.name AS course_name,
        co.code AS course_code,
        b.name AS branch_name
    FROM enrollment e
    INNER JOIN class c ON e.class_id = c.id
    INNER JOIN course co ON c.course_id = co.id
    INNER JOIN branch b ON c.branch_id = b.id
    WHERE e.student_id = :studentId
      AND e.status = 'enrolled'
      AND c.status IN ('scheduled', 'ongoing')
),
student_sessions AS (
    -- Lấy tất cả sessions trong tuần cho student này
    SELECT 
        ss.student_id,
        ss.session_id,
        ss.attendance_status,
        ss.is_makeup,
        ss.homework_status,
        s.date AS session_date,
        EXTRACT(ISODOW FROM s.date) AS day_of_week,  -- 1=Monday, 7=Sunday
        s.type AS session_type,
        s.status AS session_status,
        s.teacher_note,
        cs.topic AS session_topic,
        cs.sequence_no AS session_sequence,
        cs.student_task,
        cs.skill_set,
        tst.name AS time_slot_name,
        tst.start_time,
        tst.end_time,
        tst.duration_min,
        -- Class info
        sc.class_id,
        sc.class_name,
        sc.class_code,
        sc.class_status,
        sc.modality,
        sc.course_name,
        sc.course_code,
        sc.branch_name,
        -- Resource info (room or virtual)
        r.name AS resource_name,
        r.resource_type,
        r.location AS room_location,
        r.meeting_url,
        r.meeting_id,
        -- Teacher info (primary teacher)
        (
            SELECT u.full_name
            FROM teaching_slot ts
            INNER JOIN teacher t ON ts.teacher_id = t.id
            INNER JOIN user_account u ON t.user_account_id = u.id
            WHERE ts.session_id = s.id
              AND ts.role = 'primary'
              AND ts.status = 'scheduled'
            LIMIT 1
        ) AS teacher_name,
        -- Phase info
        cp.name AS phase_name,
        cp.phase_number
    FROM student_session ss
    INNER JOIN session s ON ss.session_id = s.id
    INNER JOIN student_classes sc ON s.class_id = sc.class_id
    LEFT JOIN course_session cs ON s.course_session_id = cs.id
    LEFT JOIN time_slot_template tst ON s.time_slot_template_id = tst.id
    LEFT JOIN course_phase cp ON cs.phase_id = cp.id
    -- Get resource (room or zoom)
    LEFT JOIN session_resource sr ON s.id = sr.session_id
    LEFT JOIN resource r ON sr.resource_id = r.id
    WHERE ss.student_id = :studentId
      AND s.date BETWEEN :weekStartDate AND :weekEndDate
)
SELECT 
    session_id,
    session_date,
    day_of_week,
    CASE day_of_week
        WHEN 1 THEN 'Thứ 2'
        WHEN 2 THEN 'Thứ 3'
        WHEN 3 THEN 'Thứ 4'
        WHEN 4 THEN 'Thứ 5'
        WHEN 5 THEN 'Thứ 6'
        WHEN 6 THEN 'Thứ 7'
        WHEN 7 THEN 'Chủ nhật'
    END AS day_name,
    time_slot_name,
    start_time,
    end_time,
    duration_min,
    -- Session details
    session_topic,
    session_sequence,
    session_type,
    session_status,
    student_task,
    skill_set,
    teacher_note,
    -- Class info
    class_id,
    class_name,
    class_code,
    class_status,
    modality,
    course_name,
    course_code,
    branch_name,
    -- Attendance info
    attendance_status,
    is_makeup,
    homework_status,
    -- Location/Resource info
    CASE 
        WHEN resource_type = 'room' THEN 'Phòng: ' || resource_name || ' (' || room_location || ')'
        WHEN resource_type = 'virtual' THEN 'Online: ' || resource_name
        ELSE 'Chưa xác định'
    END AS location_display,
    resource_type,
    meeting_url,
    meeting_id,
    -- Teacher info
    teacher_name,
    -- Phase info
    phase_name,
    phase_number,
    -- Display badges
    CASE attendance_status
        WHEN 'planned' THEN 'Chưa diễn ra'
        WHEN 'present' THEN '✓ Có mặt'
        WHEN 'absent' THEN '✗ Vắng'
        WHEN 'late' THEN '⏰ Muộn'
        WHEN 'excused' THEN '⚠ Có phép'
        WHEN 'remote' THEN '🌐 Học online'
    END AS attendance_badge,
    CASE session_status
        WHEN 'planned' THEN 'badge-primary'
        WHEN 'done' THEN 'badge-success'
        WHEN 'cancelled' THEN 'badge-danger'
    END AS status_badge_class,
    CASE 
        WHEN is_makeup = true THEN 'Buổi bù'
        ELSE ''
    END AS makeup_badge
FROM student_sessions
ORDER BY day_of_week, start_time, session_sequence;

-- Kết quả: Danh sách sessions theo tuần, có đủ thông tin để render calendar view
-- Frontend sẽ group by day_of_week để hiển thị theo từng cột (Thứ 2 → Chủ nhật)


