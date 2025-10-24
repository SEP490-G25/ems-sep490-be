# SYSTEM MAIN FLOWS - CÁC LUỒNG TỰ ĐỘNG CỦA HỆ THỐNG

## Tổng quan
File này mô tả các luồng tự động mà hệ thống EMS thực hiện trong background, không cần tương tác trực tiếp từ user. Đây là các flow quan trọng đảm bảo tính nhất quán dữ liệu và automation.

---

## FLOW 1: Auto-Generate Sessions Từ Course Template ⭐ CORE AUTOMATION

**Trigger:** Academic Staff tạo class mới  
**Description:** Hệ thống tự động sinh tất cả sessions dựa trên course template.

**Database Tables Involved:**
- `class` → `course` → `course_phase` → `course_session`
- `session` (insert nhiều records)
- `time_slot_template` (để lấy giờ học)

**Flow Steps:**

1. **Trigger: Academic Staff INSERT class mới**
   - Class có: course_id, start_date, schedule_days, time_slot mapping

2. **System đọc course structure**
   ```
   -- Lấy tất cả course_session theo thứ tự
   SELECT 
     cs.id AS course_session_id,
     cs.phase_id,
     cp.phase_number,
     cs.sequence_no,
     cs.topic,
     cs.student_task,
     cs.skill_set,
     ROW_NUMBER() OVER (ORDER BY cp.phase_number, cs.sequence_no) AS global_sequence
   FROM course_session cs
   JOIN course_phase cp ON cs.phase_id = cp.id
   WHERE cp.course_id = :course_id
   ORDER BY cp.phase_number, cs.sequence_no
   ```

3. **System tính toán ngày giờ cho từng session**
   - Logic:
   ```
   FOR each course_session (global_sequence = 1, 2, 3, ...):
     
     -- Tính tuần và ngày trong tuần
     week_index = FLOOR((global_sequence - 1) / array_length(schedule_days))
     day_index = ((global_sequence - 1) % array_length(schedule_days)) + 1
     
     -- Lấy schedule_day tương ứng
     schedule_day = schedule_days[day_index]
     
     -- Tính ngày cụ thể
     -- Bước 1: Tìm ngày đầu tiên khớp với schedule_day[0] (ngày đầu tiên trong tuần)
     first_day_dow = schedule_days[0]
     days_to_first = (first_day_dow - EXTRACT(DOW FROM start_date) + 7) % 7
     first_session_date = start_date + days_to_first
     
     -- Bước 2: Tính ngày của session này
     -- Từ first_session_date, cộng thêm số tuần * 7 ngày
     -- Và cộng thêm offset trong tuần
     offset_in_week = schedule_days[day_index] - schedule_days[0]
     IF offset_in_week < 0 THEN offset_in_week = offset_in_week + 7
     
     session_date = first_session_date + (week_index * 7) + offset_in_week
     
     -- Lấy time slot
     time_slot = time_slot_mapping[schedule_day]
     start_time = time_slot.start_time
     end_time = time_slot.end_time
   ```

4. **System INSERT sessions hàng loạt**
   ```
   INSERT INTO session (
     class_id,
     course_session_id,
     date,
     start_time,
     end_time,
     type,
     status
   ) 
   SELECT 
     :class_id,
     course_session_id,
     calculated_date,
     calculated_start_time,
     calculated_end_time,
     'CLASS',
     'planned'
   FROM (
     -- Generated data from step 3
   )
   ```

5. **System validation**
   - Đảm bảo:
     - Số lượng sessions = tổng số course_session
     - Tất cả sessions nằm đúng schedule_days
     - Không có session nào bị trùng ngày giờ (trong cùng class)

6. **System log**
   - Ghi log: "Generated X sessions for class [class_code]"

**Result:** 
- Tất cả sessions được tạo tự động
- Academic Staff chỉ cần phân công teacher và resource

---

## FLOW 2: Auto-Generate Student Schedule (student_session)

**Trigger:** Enrollment mới được tạo  
**Description:** Hệ thống tự động tạo lịch cá nhân (student_session) cho học viên khi ghi danh.

**Database Tables Involved:**
- `enrollment` → `class` → `session`
- `student_session` (insert nhiều records)

**Flow Steps:**

1. **Trigger: INSERT vào enrollment table**
   - enrollment.class_id, enrollment.student_id, enrollment.status='enrolled'

2. **System lấy tất cả sessions của class**
   ```
   SELECT id
   FROM session
   WHERE class_id = :class_id
     AND status = 'planned'
     AND date >= CURRENT_DATE
   ORDER BY date
   ```

3. **System INSERT student_session hàng loạt**
   ```
   INSERT INTO student_session (
     student_id,
     session_id,
     is_makeup,
     attendance_status
   )
   SELECT 
     :student_id,
     s.id,
     FALSE,
     'planned'
   FROM session s
   WHERE s.class_id = :class_id
     AND s.status = 'planned'
     AND s.date >= CURRENT_DATE
   ```

4. **System log**
   - "Generated X student_session records for student [student_code]"

**Result:** 
- Student có lịch học cá nhân ngay sau khi enrollment
- Teacher thấy student trong danh sách điểm danh

---

## FLOW 3: Auto-Sync Student Schedule Khi Session Thay Đổi

**Trigger:** Session bị reschedule/cancel  
**Description:** Khi session thay đổi (ngày giờ, hoặc bị hủy), hệ thống tự động cập nhật student_session của tất cả enrolled students.

**Database Tables Involved:**
- `session` (UPDATE)
- `student_session` (UPDATE/INSERT/DELETE logic)
- `enrollment` (để lấy danh sách students)

**Flow Steps:**

### Case A: Session Bị Cancel
1. **Trigger: UPDATE session SET status='cancelled'**

2. **System tự động update student_session**
   ```
   UPDATE student_session
   SET attendance_status = 'excused'
   WHERE session_id = :cancelled_session_id
     AND attendance_status = 'planned'
   ```

3. **System gửi notification hàng loạt**
   - Tới tất cả students: "Buổi học ngày [...] đã bị hủy"

### Case B: Session Bị Reschedule (Tạo Session Mới)
1. **Trigger: Academic Staff reschedule session**
   - Tạo new_session với ngày giờ mới
   - Cancel old_session

2. **System transfer student_session**
   ```
   -- Chuyển tất cả student_session từ old sang new
   UPDATE student_session
   SET session_id = :new_session_id
   WHERE session_id = :old_session_id
   ```

3. **System gửi notification**
   - "Buổi học ngày [...] đã được dời sang ngày [...]"

**Result:** 
- Lịch học của students luôn được sync tự động
- Không cần manual intervention

---

## FLOW 4: Conflict Detection (Teacher/Resource)

**Trigger:** Khi assign teacher hoặc resource  
**Description:** Hệ thống kiểm tra conflict trước khi cho phép assignment.

**Database Tables Involved:**
- `teaching_slot` (để check teacher conflict)
- `session_resource` (để check resource conflict)
- `session` (để lấy ngày giờ)

**Flow Steps:**

### 4A. Teacher Conflict Detection

1. **Trigger: Academic Staff chọn teacher cho session**

2. **System query để check conflict**
   ```
   SELECT COUNT(*) AS conflict_count
   FROM teaching_slot ts
   JOIN session s ON ts.session_id = s.id
   WHERE ts.teacher_id = :teacher_id
     AND s.date = :session_date
     AND s.status IN ('planned', 'ongoing')
     AND (s.start_time, s.end_time) OVERLAPS (:start_time, :end_time)
   ```

3. **System validation**
   - Nếu conflict_count > 0:
     - RETURN error: "Teacher đã có lịch dạy trùng giờ"
     - Hiển thị thông tin session bị conflict
   - Nếu conflict_count = 0:
     - Cho phép assign

### 4B. Resource Conflict Detection

1. **Trigger: Academic Staff chọn resource cho session**

2. **System query**
   ```
   SELECT COUNT(*) AS conflict_count
   FROM session_resource sr
   JOIN session s ON sr.session_id = s.id
   WHERE sr.resource_id = :resource_id
     AND s.date = :session_date
     AND s.status IN ('planned', 'ongoing')
     AND (s.start_time, s.end_time) OVERLAPS (:start_time, :end_time)
   ```

3. **System validation**
   - Nếu conflict_count > 0:
     - RETURN error: "Resource đã được sử dụng trong khung giờ này"
   - Nếu conflict_count = 0:
     - Cho phép assign

**Result:** 
- Ngăn chặn double-booking
- Đảm bảo data integrity

---

## FLOW 5: Attendance Lock (Tự Động Khóa Điểm Danh)

**Trigger:** Scheduled job (cron) chạy mỗi giờ  
**Description:** Tự động khóa điểm danh sau T giờ kể từ khi session kết thúc.

**Database Tables Involved:**
- `session` (thêm trường is_attendance_locked)
- `system_config` (lưu T = số giờ lock)

**Flow Steps:**

1. **Scheduled Job chạy định kỳ**
   - Mỗi giờ hoặc mỗi 30 phút

2. **System query các session cần lock**
   ```
   SELECT id
   FROM session
   WHERE status = 'done'
     AND is_attendance_locked = FALSE
     AND (
       date + end_time + INTERVAL ':T hours' <= NOW()
     )
   ```

3. **System UPDATE hàng loạt**
   ```
   UPDATE session
   SET is_attendance_locked = TRUE
   WHERE id IN (...)
   ```

4. **System log**
   - "Locked attendance for X sessions"

**Result:** 
- Attendance data được lock tự động
- Teacher không thể sửa điểm danh sau khi lock (trừ khi Admin unlock)

---

## FLOW 6: Auto-Notification System

**Trigger:** Các sự kiện trong hệ thống (enrollment, reschedule, approval, v.v.)  
**Description:** Hệ thống tự động gửi notification qua email/SMS/in-app.

**Database Tables Involved:**
- `notification` (lưu lịch sử notification)
- Event-driven triggers

**Flow Steps:**

### Event-Driven Architecture

1. **System lắng nghe events**
   - Event: `EnrollmentCreated`
   - Event: `SessionRescheduled`
   - Event: `RequestApproved`
   - Event: `ClassApproved`
   - v.v.

2. **Khi event trigger, System xác định recipients**
   - Ví dụ: Event `SessionRescheduled`
     - Recipients: Tất cả students của class + teacher của session

3. **System tạo notification content**
   - Template:
     ```
     Buổi học {class_name} ngày {old_date} {old_time}
     đã được dời sang ngày {new_date} {new_time}.
     Địa điểm/Link: {resource_info}
     ```

4. **System gửi qua nhiều kênh**
   - Email: Queue job gửi email hàng loạt
   - SMS: Queue job gửi SMS (chỉ urgent cases)
   - In-app: INSERT vào bảng notification

5. **System INSERT notification record**
   ```
   INSERT INTO notification (
     user_id,
     title,
     message,
     type,
     is_read,
     created_at
   ) VALUES (
     :user_id,
     :title,
     :message,
     :type,
     FALSE,
     NOW()
   )
   ```

**Result:** 
- Users nhận thông báo real-time
- Tất cả notifications được audit trail

---

## FLOW 7: Scheduled Reports (Báo Cáo Định Kỳ Tự Động)

**Trigger:** Scheduled job (cron) chạy theo lịch  
**Description:** Hệ thống tự động generate và gửi báo cáo định kỳ.

**Database Tables Involved:**
- Multiple tables (tùy loại báo cáo)
- `report_schedule` (cấu hình lịch gửi báo cáo)

**Flow Steps:**

1. **System có bảng report_schedule**
   ```
   report_schedule:
   - id
   - report_type (daily_attendance, weekly_enrollment, monthly_summary)
   - recipients (list of emails)
   - schedule (cron expression: "0 8 * * 1" = 8AM mỗi thứ 2)
   - last_run_at
   ```

2. **Cron job chạy định kỳ**
   - Kiểm tra các report_schedule cần chạy

3. **System generate report**
   - Ví dụ: Daily Attendance Report
   ```
   SELECT 
     c.name AS class_name,
     s.date,
     COUNT(CASE WHEN ss.attendance_status IN ('present', 'late') THEN 1 END) AS present_count,
     COUNT(CASE WHEN ss.attendance_status = 'absent' THEN 1 END) AS absent_count,
     COUNT(*) AS total_students
   FROM session s
   JOIN class c ON s.class_id = c.id
   JOIN student_session ss ON s.id = ss.session_id
   WHERE s.date = CURRENT_DATE - 1
     AND s.status = 'done'
   GROUP BY c.id, s.date
   ORDER BY c.name
   ```

4. **System export to file**
   - Format: CSV/Excel/PDF
   - Upload to S3/Cloud Storage
   - Generate presigned URL

5. **System gửi email với attachment**
   - To: recipients từ report_schedule
   - Subject: "Daily Attendance Report - [Date]"
   - Body: Summary + link download

6. **System UPDATE last_run_at**
   ```
   UPDATE report_schedule
   SET last_run_at = NOW()
   WHERE id = :schedule_id
   ```

**Result:** 
- Manager/Center Head nhận báo cáo tự động
- Không cần manual export mỗi ngày

---

## FLOW 8: Data Consistency Checker (Background Job)

**Trigger:** Scheduled job chạy hàng đêm  
**Description:** Kiểm tra và sửa chữa các inconsistency trong dữ liệu.

**Database Tables Involved:**
- Multiple tables

**Flow Steps:**

1. **Job chạy vào 2AM hàng ngày**

2. **System chạy các validation queries**

### Check 1: Student_session Orphans
   ```
   -- Tìm student_session không có enrollment tương ứng
   SELECT ss.id
   FROM student_session ss
   WHERE NOT EXISTS (
     SELECT 1 
     FROM enrollment e
     JOIN session s ON e.class_id = s.class_id
     WHERE e.student_id = ss.student_id
       AND s.id = ss.session_id
       AND e.status = 'enrolled'
   )
   ```

### Check 2: Teaching_slot Orphans
   ```
   -- Tìm teaching_slot không có session
   SELECT ts.id
   FROM teaching_slot ts
   WHERE NOT EXISTS (
     SELECT 1 FROM session s WHERE s.id = ts.session_id
   )
   ```

### Check 3: CLO Coverage
   ```
   -- Tìm course có CLO chưa được map vào session nào
   SELECT 
     clo.id,
     clo.code,
     clo.description
   FROM clo
   WHERE clo.course_id = :course_id
     AND NOT EXISTS (
       SELECT 1 
       FROM course_session_clo_mapping cscm 
       WHERE cscm.clo_id = clo.id
     )
   ```

3. **System log issues**
   - Ghi vào bảng `data_consistency_log`
   - Hoặc gửi alert tới Admin

4. **System tự động fix (nếu có thể)**
   - Ví dụ: Xóa orphan records
   - Hoặc đánh dấu để manual review

**Result:** 
- Dữ liệu luôn consistent
- Phát hiện sớm lỗi logic

---

## FLOW 9: Capacity Warning System

**Trigger:** Scheduled job hoặc real-time check  
**Description:** Cảnh báo khi class/resource sắp full hoặc quá tải.

**Database Tables Involved:**
- `class`, `enrollment`, `session_resource`

**Flow Steps:**

1. **System check capacity định kỳ**

### Check A: Class Fill Rate
   ```
   SELECT 
     c.id,
     c.name,
     c.max_capacity,
     COUNT(e.id) AS enrolled_count,
     (COUNT(e.id)::float / c.max_capacity * 100) AS fill_rate
   FROM class c
   LEFT JOIN enrollment e ON (e.class_id = c.id AND e.status = 'enrolled')
   WHERE c.status IN ('scheduled', 'ongoing')
   GROUP BY c.id
   HAVING (COUNT(e.id)::float / c.max_capacity * 100) >= 90
   ```

2. **System gửi alert nếu fill_rate >= 90%**
   - Tới Academic Staff: "Lớp [Tên] sắp đầy (90% capacity)"
   - Tới Center Head: Dashboard hiển thị warning

### Check B: Resource Overload
   ```
   SELECT 
     r.id,
     r.name,
     COUNT(sr.session_id) AS usage_count
   FROM resource r
   LEFT JOIN session_resource sr ON r.id = sr.resource_id
   LEFT JOIN session s ON (sr.session_id = s.id AND s.status = 'planned')
   WHERE s.date BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '7 days'
   GROUP BY r.id
   HAVING COUNT(sr.session_id) > 20  -- threshold
   ```

3. **System gửi alert**
   - "Resource [Tên] đang quá tải (20+ sessions tuần này)"

**Result:** 
- Phát hiện sớm bottleneck
- Center Head có thể can thiệp kịp thời

---

## FLOW 10: Automatic Class Status Transition

**Trigger:** Scheduled job hoặc event-driven  
**Description:** Tự động chuyển status của class (scheduled → ongoing → completed).

**Database Tables Involved:**
- `class`, `session`

**Flow Steps:**

1. **Job chạy mỗi ngày**

### Transition 1: scheduled → ongoing
   ```
   -- Class có buổi học đầu tiên đã qua
   UPDATE class
   SET status = 'ongoing'
   WHERE status = 'scheduled'
     AND EXISTS (
       SELECT 1 
       FROM session 
       WHERE session.class_id = class.id 
         AND session.date <= CURRENT_DATE
     )
   ```

### Transition 2: ongoing → completed
   ```
   -- Class có tất cả sessions đã done
   UPDATE class
   SET status = 'completed'
   WHERE status = 'ongoing'
     AND NOT EXISTS (
       SELECT 1 
       FROM session 
       WHERE session.class_id = class.id 
         AND session.status != 'done'
     )
   ```

2. **System gửi notification**
   - Khi class completed: Gửi certificate tới students (nếu có)

**Result:** 
- Class status luôn chính xác
- Không cần manual update

---

## Tóm Tắt Các System Flow

| Flow | Trigger | Mô Tả | Bảng Chính |
|------|---------|-------|-----------|
| 1. Auto-Generate Sessions | Class created | Sinh sessions từ course template | course_session → session |
| 2. Auto-Generate Student Schedule | Enrollment created | Sinh student_session | enrollment → student_session |
| 3. Auto-Sync Student Schedule | Session reschedule/cancel | Update student_session | session → student_session |
| 4. Conflict Detection | Assign teacher/resource | Kiểm tra trùng lịch | teaching_slot, session_resource |
| 5. Attendance Lock | Scheduled job | Lock điểm danh sau T giờ | session.is_attendance_locked |
| 6. Auto-Notification | Event-driven | Gửi email/SMS/in-app | notification |
| 7. Scheduled Reports | Cron job | Generate & gửi báo cáo định kỳ | Multiple tables |
| 8. Data Consistency Checker | Nightly job | Kiểm tra & fix data issues | All tables |
| 9. Capacity Warning | Real-time/scheduled | Cảnh báo sắp full | class, resource |
| 10. Class Status Transition | Daily job | Chuyển status tự động | class, session |

---

## Kiến Trúc Tổng Quan

```
┌─────────────────────────────────────────────────────┐
│              USER ACTIONS (Manual Flows)            │
│  Teacher, Student, Academic Staff, Manager, etc.    │
└────────────────────┬────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────┐
│              API LAYER                               │
│  REST APIs, Validation, Business Logic              │
└────────────────────┬────────────────────────────────┘
                     │
         ┌───────────┴───────────┐
         ▼                       ▼
┌──────────────────┐    ┌──────────────────┐
│  DATABASE        │    │  EVENT QUEUE     │
│  (PostgreSQL)    │    │  (RabbitMQ/      │
│                  │    │   Kafka)         │
└────────┬─────────┘    └────────┬─────────┘
         │                       │
         │         ┌─────────────┘
         │         ▼
         │  ┌─────────────────────────────────────┐
         │  │    BACKGROUND WORKERS                │
         │  │  - Auto-generation                   │
         │  │  - Sync operations                   │
         └─>│  - Scheduled jobs (cron)             │
            │  - Notification service              │
            │  - Report generation                 │
            └─────────────────────────────────────┘
```

---

**Lưu Ý Quan Trọng:**
- System flows đảm bảo automation và consistency
- Sử dụng TRANSACTION cho các operation phức tạp
- Event-driven architecture cho real-time sync
- Scheduled jobs cho batch processing (reports, cleanup)
- Luôn có logging và monitoring cho mọi background job
- Có retry mechanism cho các job thất bại
- Notification có queue để không block main flow
