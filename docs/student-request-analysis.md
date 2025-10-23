# Phân Tích Chi Tiết 3 Luồng Student Request

---

## 1. ABSENCE REQUEST (Xin phép nghỉ)

### 1.1. Mô tả nghiệp vụ

Học sinh biết trước mình sẽ vắng mặt một buổi học và muốn xin phép để được tính là **"excused"** (nghỉ có phép) thay vì **"absent"** (vắng không phép).

### 1.2. Điều kiện

- ✅ Phải submit trước buổi học ít nhất X ngày (`request_lead_time` - cấu hình bởi Admin)
- ✅ Buổi học phải còn `status = planned` (chưa diễn ra)
- ✅ Student phải đang enrolled trong class đó

### 1.3. Luồng xử lý

```
STUDENT → Submit Absence Request
  ↓
  target_session_id: Session cần nghỉ
  reason: "Có việc gia đình khẩn cấp"
  status: pending
  ↓
ACADEMIC STAFF → Review & Approve
  ↓
  Kiểm tra:
  - Lý do hợp lệ?
  - Có trong request_lead_time?
  - Student có lịch sử nghỉ nhiều không?
  ↓
  Approve → status: approved
  ↓
SYSTEM → Execute
  ↓
  UPDATE student_session
  SET attendance_status = 'excused',
      note = 'Approved absence: [reason]'
  WHERE student_id = X
    AND session_id = target_session_id
  ↓
RESULT: Học sinh nghỉ có phép, không bị ảnh hưởng điểm chuyên cần
```

### 1.4. Business Rules

- ⚠️ Nếu quá số ngày cho phép nghỉ (VD: max 3 buổi/khóa), Academic Staff có thể reject
- ⚠️ Emergency case (đột xuất): có thể submit trong ngày nhưng cần lý do đặc biệt
- 📊 Số buổi nghỉ có phép được tính vào báo cáo attendance rate riêng

---

## 2. MAKE-UP REQUEST (Học bù) ⭐ PHỨC TẠP NHẤT

### 2.1. Mô tả nghiệp vụ

Học sinh đã nghỉ một buổi học (hoặc sắp nghỉ) và muốn học bù bằng cách tham gia buổi học **cùng nội dung** (cùng `course_session_id`) của một lớp khác.

### 2.2. Điểm đặc biệt

🎯 **Học bù phải là cùng `course_session_id`** (cùng topic, cùng kỹ năng)

🎯 **Có thể học bù ở:**
- ✅ Lớp khác cùng branch
- ✅ Lớp khác branch khác
- ✅ Lớp OFFLINE → ONLINE hoặc ngược lại
- ✅ Giáo viên khác
- ✅ Thời gian khác

### 2.3. Luồng xử lý chi tiết

#### **BƯỚC 1: STUDENT chọn buổi đã nghỉ**

```sql
SELECT s.id, s.date, cs.topic, cs.skill_set
FROM session s
JOIN course_session cs ON s.course_session_id = cs.id
WHERE s.id IN (
  SELECT session_id 
  FROM student_session 
  WHERE student_id = :student_id 
    AND attendance_status IN ('absent', 'planned')
)
ORDER BY s.date DESC;
```

**Ví dụ:** Session 5 - "Listening Practice" - Feb 10, 2025

---

#### **BƯỚC 2: SYSTEM tìm available make-up sessions**

```sql
SELECT 
  s.id AS makeup_session_id,
  s.date,
  s.start_time,
  s.end_time,
  c.name AS class_name,
  b.name AS branch_name,
  c.modality,
  (c.max_capacity - COUNT(ss.student_id)) AS available_slots,
  cs.topic
FROM session s
JOIN class c ON s.class_id = c.id
JOIN branch b ON c.branch_id = b.id
JOIN course_session cs ON s.course_session_id = cs.id
LEFT JOIN student_session ss ON s.id = ss.session_id 
  AND ss.attendance_status != 'excused'
WHERE s.course_session_id = :missed_course_session_id -- ⭐ KEY: cùng nội dung
  AND s.status = 'planned'
  AND s.date >= CURRENT_DATE
  AND s.id NOT IN (
    -- Loại bỏ sessions mà student đã đăng ký (tránh trùng)
    SELECT session_id 
    FROM student_session 
    WHERE student_id = :student_id
  )
GROUP BY s.id, c.id, b.id, cs.id
HAVING COUNT(ss.student_id) < c.max_capacity -- Còn chỗ trống
ORDER BY 
  available_slots DESC, -- Ưu tiên lớp còn nhiều chỗ
  s.date ASC; -- Sớm nhất
```

**Kết quả ví dụ:**

| makeup_session_id | date   | time  | class_name    | branch    | modality | slots_avail |
|-------------------|--------|-------|---------------|-----------|----------|-------------|
| 245               | Feb 12 | 14:00 | A1-Evening-02 | Hoàn Kiếm | OFFLINE  | 5           |
| 298               | Feb 13 | 18:00 | A1-Night-03   | Cầu Giấy  | ONLINE   | 3           |
| 312               | Feb 15 | 08:00 | A1-Morning-01 | Đống Đa   | HYBRID   | 2           |

---

#### **BƯỚC 3: STUDENT chọn make-up session ưa thích**

Student chọn: **Session 245** (Feb 12, 14:00, Hoàn Kiếm OFFLINE)

Submit request:
```json
{
  "target_session_id": 123,        // Buổi gốc đã nghỉ
  "makeup_session_id": 245,        // Buổi muốn học bù
  "reason": "Muốn học bù buổi Listening đã nghỉ hôm 10/2"
}
```

---

#### **BƯỚC 4: ACADEMIC STAFF review**

**Kiểm tra:**

✅ **Cùng course_session_id?** → YES (cùng topic "Listening Practice")

✅ **Còn chỗ trống?** → YES (5 slots)

✅ **Student có conflict với schedule khác không?**
```sql
SELECT COUNT(*) 
FROM student_session ss
JOIN session s ON ss.session_id = s.id
WHERE ss.student_id = :student_id
  AND s.date = '2025-02-12'
  AND (
    (s.start_time, s.end_time) OVERLAPS ('14:00', '16:30')
  )
```
→ Nếu > 0 → **CONFLICT** → Reject

✅ **Lịch sử make-up:** Student đã học bù mấy lần? (VD: max 5 lần/khóa)

→ **Approve**

---

#### **BƯỚC 5: SYSTEM execute (TRANSACTION)**

```sql
BEGIN;

-- 1. Update original session → excused
UPDATE student_session
SET attendance_status = 'excused',
    note = 'Học bù tại Session 245 (Class A1-Evening-02, Feb 12)'
WHERE student_id = :student_id
  AND session_id = 123; -- target_session_id

-- 2. Create NEW student_session for make-up
INSERT INTO student_session (
  student_id,
  session_id,
  attendance_status,
  is_makeup,
  note
) VALUES (
  :student_id,
  245, -- makeup_session_id
  'planned',
  TRUE, -- ⭐ Đánh dấu đây là học bù
  'Học bù từ Session 123 (Class A1-Morning-01, Feb 10)'
);

-- 3. Update request status
UPDATE student_request
SET status = 'approved',
    decided_by = :staff_id,
    decided_at = NOW(),
    resolution = 'Học bù tại Session 245'
WHERE id = :request_id;

COMMIT;
```

---

#### **BƯỚC 6: TEACHER thấy student trong lớp học bù**

Giáo viên của Session 245 mở attendance list:

| Student Name | Status        | Notes        |
|--------------|---------------|--------------|
| Nguyễn Văn A | enrolled      | -            |
| Trần Thị B   | enrolled      | -            |
| Lê Văn C     | **MAKE-UP 🔄** | From Class X | ← ⭐ Hiển thị đặc biệt

→ Giáo viên điểm danh bình thường (present/absent/late)  
→ Attendance được ghi vào `student_session.attendance_status` của makeup session

---

### 2.4. Edge Cases

#### **Case 1: Không tìm thấy make-up session**

**Lý do:**
- Tất cả lớp khác đã học qua Session 5 rồi
- Hoặc các lớp chưa học đến Session 5
- Hoặc tất cả lớp đều full capacity

**Giải pháp:**
1. Student tự học qua tài liệu (materials từ `course_session`)
2. Academic Staff sắp xếp 1-on-1 với giáo viên (ngoài hệ thống)
3. Đợi đến khi có lớp mới mở (delay make-up)

#### **Case 2: Capacity overflow**

**Problem:**
10 students cùng muốn học bù Session 5 tại Class B → Class B chỉ còn 2 slots

**Giải pháp:**
1. Academic Staff override capacity (`session_resource.capacity_override = TRUE`)
   - Với approval từ Center Head
   - VD: ONLINE class có thể tăng từ 25 → 35 học sinh
2. Mở "Make-up Class" riêng (dedicated session chỉ cho học bù)
   - Create new session với `course_session_id = 5`
   - Assign teacher, room
   - Enroll tất cả students cần học bù

#### **Case 3: Cross-branch make-up**

**Scenario:** Student ở Branch Cầu Giấy muốn học bù ở Branch Hoàn Kiếm

**Business Rule:**
- ✅ Được phép (miễn cùng `course_session_id`)
- ⚠️ Academic Staff phải notify cả 2 branch
- ⚠️ Student tự chịu trách nhiệm di chuyển

**System check:**
- Verify student có quyền access branch khác không?
- Notify teacher ở branch Hoàn Kiếm về student "external"

---

### 2.5. Metrics cần tracking

```sql
-- Số lượng make-up requests
SELECT COUNT(*) FROM student_request 
WHERE request_type = 'makeup' AND status = 'approved';

-- Top students có nhiều make-up nhất
SELECT s.full_name, COUNT(*) AS makeup_count
FROM student_session ss
JOIN student s ON ss.student_id = s.id
WHERE ss.is_makeup = TRUE
GROUP BY s.id
ORDER BY makeup_count DESC
LIMIT 10;

-- Make-up success rate (có tìm được session không)
SELECT 
  COUNT(CASE WHEN status = 'approved' THEN 1 END) * 100.0 / COUNT(*) AS success_rate
FROM student_request
WHERE request_type = 'makeup';
```

---

## 3. TRANSFER REQUEST (Chuyển lớp) ⭐⭐ CỰC KỲ PHỨC TẠP

### 3.1. Mô tả nghiệp vụ

Học sinh muốn chuyển từ lớp hiện tại sang lớp khác **HOÀN TOÀN** (không chỉ 1 buổi), tiếp tục học từ giữa chừng.

**Lý do thường gặp:**
- 🔄 Đổi lịch học (Morning → Evening, Weekday → Weekend)
- 🔄 Đổi modality (OFFLINE → ONLINE do chuyển nhà xa)
- 🔄 Đổi branch (chuyển công ty, chuyển nhà)
- 🔄 Đổi giáo viên (không hợp với teaching style)
- 🔄 Tốc độ học không phù hợp (lớp hiện tại quá nhanh/chậm)

### 3.2. Business Rules CỰC KỲ QUAN TRỌNG

✅ **Điều kiện BẮT BUỘC:**
1. Class A và Class B phải cùng `course_id` (cùng curriculum)
2. Class B phải có `status = 'scheduled'` hoặc `'ongoing'`
3. Class B phải còn available capacity
4. Student phải đang enrolled trong Class A (`status = 'enrolled'`)

⚠️ **Xử lý dữ liệu:**
- **KHÔNG XÓA** enrollment cũ → Chuyển status sang `'transferred'`
- **KHÔNG XÓA** student_session cũ → Preserve audit trail
- Map sessions dựa trên `course_session_id` (không phải sequence)

### 3.3. Luồng xử lý SIÊU CHI TIẾT

#### **BƯỚC 1: STUDENT submit transfer request**

**Student hiện đang học:**
- Class A: "English A1 Mon/Wed/Fri Morning - Cầu Giấy OFFLINE"
- Đã học đến Session 10 (Feb 10, 2025)
- Còn 26 sessions nữa (Session 11-36)

**Muốn chuyển sang:**
- Class B: "English A1 Tue/Thu/Sat Evening - Online"

**Submit request:**
```json
{
  "current_class_id": 101,     // Class A
  "target_class_id": 205,      // Class B  
  "effective_date": "2025-02-15",
  "reason": "Chuyển công ty, không thể học buổi sáng được nữa"
}
```

---

#### **BƯỚC 2: ACADEMIC STAFF validation**

**Check 1: Cùng course?**
```sql
SELECT 
  c1.course_id AS class_a_course,
  c2.course_id AS class_b_course,
  (c1.course_id = c2.course_id) AS same_course
FROM class c1, class c2
WHERE c1.id = 101 AND c2.id = 205;
```
→ Nếu `FALSE` → **REJECT** ngay lập tức  
→ "Cannot transfer: Class A uses Course X, Class B uses Course Y"

**Check 2: Class B có status hợp lệ?**
```sql
SELECT status FROM class WHERE id = 205;
```
→ Nếu `'draft'` hoặc `'completed'` → **REJECT**

**Check 3: Class B còn chỗ?**
```sql
SELECT 
  max_capacity,
  (SELECT COUNT(*) FROM enrollment WHERE class_id = 205 AND status = 'enrolled') AS enrolled
FROM class WHERE id = 205;
```
→ Nếu `enrolled >= max_capacity` → Cần approval từ Center Head để override

**Check 4: Xác định cutoff point** ("Học sinh đã học đến đâu, sẽ tiếp tục từ đâu")
```sql
SELECT MAX(s.id) AS left_session_id
FROM session s
JOIN student_session ss ON s.id = ss.session_id
WHERE ss.student_id = :student_id
  AND s.class_id = 101 -- Class A
  AND s.date < '2025-02-15' -- Trước effective_date
  AND ss.attendance_status IN ('present', 'late', 'remote'); -- Đã học thực sự
```
→ VD: `left_session_id = 1024` (Session 10, course_session_id = 10)

---

#### **BƯỚC 3: SYSTEM phân tích content mapping** ⭐ **QUAN TRỌNG**

```sql
-- Lấy danh sách course_session còn lại của Class A
WITH remaining_class_a AS (
  SELECT DISTINCT s.course_session_id
  FROM session s
  WHERE s.class_id = 101
    AND s.date >= '2025-02-15' -- Từ effective_date
    AND s.status = 'planned'
  ORDER BY s.course_session_id
)
-- VD: [11, 12, 13, 14, 15, 16, 17, 18, ..., 36] = 26 sessions

-- Lấy danh sách course_session còn lại của Class B
WITH remaining_class_b AS (
  SELECT DISTINCT s.course_session_id, s.date
  FROM session s
  WHERE s.class_id = 205
    AND s.date >= '2025-02-15'
    AND s.status = 'planned'
  ORDER BY s.course_session_id
)
-- VD: [12, 13, 14, 16, 17, 18, 19, ..., 36] = 25 sessions
-- ⚠️ MISSING: course_session_id = 11, 15

-- Tìm content gaps
SELECT cs_id 
FROM remaining_class_a
WHERE cs_id NOT IN (SELECT course_session_id FROM remaining_class_b);
```

→ **Gap detected:** [11, 15]  
→ **Topics:** "Grammar - Present Perfect", "Vocabulary - Travel"

---

#### **BƯỚC 4: ACADEMIC STAFF handle gaps (Manual Decision)**

**Options:**

1. ✅ **Accept gap** → Student tự học materials:
   - System tự động gửi link materials của session 11, 15
   - Student review trước khi join Class B

2. ⚠️ **Delay transfer:**
   - Chuyển `effective_date` sang sau (VD: Feb 20)
   - Để Class B học đến session 15 rồi hẵng chuyển

3. 🔄 **Arrange 1-on-1 make-up:**
   - Academic Staff schedule riêng 2 buổi bù cho session 11, 15
   - (Ngoài hệ thống hoặc tạo dedicated sessions)

→ Academic Staff chọn **Option 1** → Continue with transfer

---

#### **BƯỚC 5: SYSTEM execute transfer (BIG TRANSACTION)**

```sql
BEGIN;

-- Step 1: Update enrollment in Class A
UPDATE enrollment
SET status = 'transferred',
    left_at = NOW(),
    left_session_id = 1024, -- Session 10
    note = 'Transferred to Class B (ID: 205) on 2025-02-15'
WHERE student_id = :student_id
  AND class_id = 101
  AND status = 'enrolled';

-- Step 2: Create new enrollment in Class B
INSERT INTO enrollment (
  student_id,
  class_id,
  status,
  enrolled_at,
  join_session_id,
  note
) VALUES (
  :student_id,
  205, -- Class B
  'enrolled',
  NOW(),
  (SELECT MIN(id) FROM session 
   WHERE class_id = 205 
     AND date >= '2025-02-15' 
     AND status = 'planned'), -- First session in Class B
  'Transferred from Class A (ID: 101) on 2025-02-15. Gaps: Session 11, 15'
);

-- Step 3: Mark future sessions in Class A as excused
UPDATE student_session ss
SET attendance_status = 'excused',
    note = 'Transferred to Class B on 2025-02-15'
WHERE ss.student_id = :student_id
  AND ss.session_id IN (
    SELECT s.id 
    FROM session s
    WHERE s.class_id = 101
      AND s.date >= '2025-02-15'
      AND s.status = 'planned'
  );

-- Step 4: Generate student_session for Class B
-- Map by course_session_id (not sequence!)
INSERT INTO student_session (
  student_id,
  session_id,
  attendance_status,
  is_makeup,
  note
)
SELECT 
  :student_id,
  s.id, -- session_id from Class B
  'planned',
  FALSE,
  'Transferred from Class A. Original progress: completed up to course_session 10'
FROM session s
WHERE s.class_id = 205
  AND s.date >= '2025-02-15'
  AND s.status = 'planned'
  AND s.course_session_id IN (
    -- Chỉ map những course_session mà Class B có
    SELECT DISTINCT course_session_id 
    FROM session 
    WHERE class_id = 205 
      AND date >= '2025-02-15'
  );
-- ⭐ Kết quả: 25 student_session records created (không bao gồm session 11, 15)

-- Step 5: Update request
UPDATE student_request
SET status = 'approved',
    decided_by = :staff_id,
    decided_at = NOW(),
    resolution = 'Transferred successfully. Content gaps: Session 11, 15 (self-study materials provided)'
WHERE id = :request_id;

-- Step 6: Send notification with materials
INSERT INTO notification (
  user_id,
  title,
  message,
  data
) VALUES (
  :student_id,
  'Transfer Approved - Action Required',
  'You have been transferred to Class B. Please review materials for Session 11 and 15 before Feb 15.',
  JSON_BUILD_OBJECT(
    'missing_sessions', ARRAY[11, 15],
    'materials_links', (
      SELECT JSON_AGG(JSON_BUILD_OBJECT('session', cs.sequence_no, 'topic', cs.topic, 'link', cs.materials_url))
      FROM course_session cs
      WHERE cs.id IN (11, 15)
    )
  )
);

COMMIT;
```

---

#### **BƯỚC 6: Result verification**

```sql
-- Student record in Class A
SELECT * FROM enrollment WHERE student_id = X AND class_id = 101;
```
→ `status: 'transferred', left_at: '2025-02-15', left_session_id: 1024`

```sql
-- Student record in Class B  
SELECT * FROM enrollment WHERE student_id = X AND class_id = 205;
```
→ `status: 'enrolled', enrolled_at: '2025-02-15', join_session_id: 2145`

```sql
-- Future sessions in Class A
SELECT COUNT(*) FROM student_session ss
JOIN session s ON ss.session_id = s.id
WHERE ss.student_id = X 
  AND s.class_id = 101 
  AND s.date >= '2025-02-15';
```
→ 26 records, all with `attendance_status = 'excused'`

```sql
-- Sessions in Class B
SELECT COUNT(*) FROM student_session ss
JOIN session s ON ss.session_id = s.id
WHERE ss.student_id = X 
  AND s.class_id = 205 
  AND s.date >= '2025-02-15';
```
→ 25 records, all with `attendance_status = 'planned'`

```sql
-- Attendance history preserved
SELECT s.date, ss.attendance_status, c.name
FROM student_session ss
JOIN session s ON ss.session_id = s.id
JOIN class c ON s.class_id = c.id
WHERE ss.student_id = X
ORDER BY s.date;
```

**Shows complete timeline:**
- Feb 3 - Class A - present
- Feb 5 - Class A - present
- ... (all history in Class A)
- Feb 15 - Class B - planned (từ đây trở đi)

---

### 3.4. Edge Cases CỰC KỲ QUAN TRỌNG

#### **Case 1: Transfer sang course khác (different course_id)**

**VD:** Transfer từ English A1 sang Japanese N5

**Business Rule:**
- ❌ KHÔNG thể map sessions (khác curriculum)
- ✅ Xử lý như: "Drop Class A + Fresh Enroll Class B"

**System execute:**
1. Mark enrollment Class A: `status = 'dropped'` (not 'transferred')
2. Mark all future student_session in Class A: `'excused'`
3. Create fresh enrollment in Class B (như học viên mới)
4. Generate ALL student_session for Class B (từ đầu, không có cutoff)

⚠️ **Fees handling:**
- Academic Staff must manually calculate refund/additional payment
- (Outside EMS scope - finance module)

#### **Case 2: Class B đã học xa hơn Class A**

**Scenario:** 
- Class A đã học đến Session 10 (`course_session_id = 10`)
- Class B đã học đến Session 20 (`course_session_id = 20`)

**Problem:** Student sẽ miss Sessions 11-20 (10 sessions!)

**Academic Staff decision:**
1. ❌ **REJECT** transfer → "Too much content gap"
2. ✅ **APPROVE** with conditions:
   - Student must attend 10 make-up sessions first
   - Or student accepts skipping 10 sessions (sign waiver)
3. 🔄 Suggest alternative Class C (progress tương đương Class A)

#### **Case 3: Capacity overflow**

**Scenario:**
- Class B `max_capacity = 25`
- Currently `enrolled = 25`
- Student wants to transfer → 26 students

**Academic Staff options:**
1. Request Center Head approval for capacity override
2. Put student on waitlist (wait for someone to drop)
3. Suggest alternative class with available capacity

#### **Case 4: Mid-session transfer**

**Scenario:**
- Student submitted transfer request on Feb 12
- Effective date: Feb 15
- But Class B has session on Feb 14 (before effective date)

**Question:** Student học buổi Feb 14 của Class B không?

**Business Rule:**
- Nếu `effective_date = Feb 15` → Student KHÔNG học Feb 14
- Nếu muốn học Feb 14 → Change `effective_date` to Feb 14
- System strictly follows `effective_date` (không tự động adjust)

---

### 3.5. Reporting Requirements

```sql
-- Transfer history report
SELECT 
  s.full_name,
  ca.name AS from_class,
  cb.name AS to_class,
  e1.left_at AS transfer_date,
  e1.left_session_id,
  e2.join_session_id
FROM enrollment e1
JOIN enrollment e2 ON e1.student_id = e2.student_id
JOIN student s ON e1.student_id = s.id
JOIN class ca ON e1.class_id = ca.id
JOIN class cb ON e2.class_id = cb.id
WHERE e1.status = 'transferred'
  AND e2.status = 'enrolled'
ORDER BY e1.left_at DESC;

-- Transfer rate by class
SELECT 
  c.name,
  COUNT(CASE WHEN e.status = 'transferred' THEN 1 END) AS transfers_out,
  COUNT(CASE WHEN e.status = 'enrolled' AND e.join_session_id IS NOT NULL THEN 1 END) AS transfers_in
FROM class c
LEFT JOIN enrollment e ON c.id = e.class_id
GROUP BY c.id;

-- Content gap analysis
SELECT 
  sr.id AS request_id,
  s.full_name,
  sr.resolution -- Contains gap details
FROM student_request sr
JOIN student s ON sr.student_id = s.id
WHERE sr.request_type = 'transfer'
  AND sr.resolution LIKE '%gap%';
```

---

## 4. TÓM TẮT SO SÁNH 3 FLOWS

| Tiêu chí | Absence | Make-up | Transfer |
|---------|---------|---------|----------|
| **Phức tạp** | ⭐ Đơn giản | ⭐⭐⭐ Phức tạp | ⭐⭐⭐⭐⭐ Cực phức tạp |
| **Scope** | 1 session | 1 session | Toàn bộ class |
| **Điều kiện** | Lead time | Same course_session_id + capacity | Same course_id + content mapping |
| **Data changes** | 1 UPDATE | 1 UPDATE + 1 INSERT | Multiple UPDATE + Multiple INSERT |
| **Risk** | Thấp | Trung bình (capacity) | Cao (content gaps, data integrity) |
| **Rollback** | Dễ | Trung bình | Khó (phải revert nhiều bảng) |
| **Business impact** | Minimal | Medium | High (ảnh hưởng 2 classes) |

---

## 5. KIẾN NGHỊ IMPLEMENTATION

### Priority 1: Make-up Request
- ✅ Là flow phổ biến nhất
- ✅ Business value cao (student satisfaction)
- ✅ Technical risk vừa phải

### Priority 2: Absence Request
- ✅ Đơn giản nhất
- ✅ Foundation cho make-up flow

### Priority 3: Transfer Request
- ⚠️ Phức tạp nhất
- ⚠️ Cần test kỹ (nhiều edge cases)
- ⚠️ Nên implement sau khi 2 flows trên stable

## 📊 PHÂN TÍCH HIỆN TRẠNG CODEBASE

### ✅ ĐÃ CÓ (EXISTING)

#### 1. Database Schema & Entities

✅ **Entity: `StudentRequest.java`** - Đầy đủ các fields cần thiết:
- `student`, `currentClass`, `targetClass`
- `targetSession`, `makeupSession`, `effectiveSession`
- `requestType` (ABSENCE, MAKEUP, TRANSFER, RESCHEDULE)
- `status` (PENDING, APPROVED, REJECTED, CANCELLED)
- `submittedAt`, `decidedAt`, `submittedBy`, `decidedBy`
- `note`, `effectiveDate`

✅ **Related Entities:**
- `StudentSession` - có field `isMakeup` (hỗ trợ makeup flow)
- `Enrollment` - có `leftAt`, `leftSession`, `joinSession` (hỗ trợ transfer flow)
- `SessionEntity`, `ClassEntity` - đầy đủ

✅ **Enums:**
- `StudentRequestType`, `RequestStatus`, `AttendanceStatus`, `EnrollmentStatus`

#### 2. OpenAPI Documentation

✅ **Endpoints đã định nghĩa trong `openapi-student.yaml`:**
- `POST /students/{id}/requests/absence` - Create absence request
- `POST /students/{id}/requests/makeup` - Create makeup request
- `POST /students/{id}/requests/transfer` - Create transfer request
- `GET /students/{id}/requests` - List student requests (với filter)
- `POST /student-requests/{request_id}/approve` - Approve request

✅ **DTOs đã định nghĩa:**
- `StudentRequestDTO`
- `StudentRequestApprovalResponse`

### ❌ CHƯA CÓ (MISSING)

#### 1. Repository Layer
- ❌ `StudentRequestRepository.java` - **CHƯA TỒN TẠI**
- ❌ Custom queries để:
  - Tìm available makeup sessions (theo `course_session_id` + capacity)
  - Validate transfer (same `course_id`, content mapping)
  - List requests với filters (type, status, student, date range)

#### 2. Service Layer
- ❌ `StudentRequestService.java` - **CHƯA TỒN TẠI**
- ❌ Implementation cho 3 flows chính:
  - Absence request logic
  - Makeup request logic (phức tạp nhất)
  - Transfer request logic (cực kỳ phức tạp)
- ❌ Business validation logic
- ❌ Transaction handling cho transfer flow

#### 3. Controller Layer
- ❌ `StudentRequestController.java` - **CHƯA TỒN TẠI**
- ❌ Endpoints implementation

#### 4. DTOs

❌ **Request DTOs (input):**
- `CreateAbsenceRequestDTO`
- `CreateMakeupRequestDTO`
- `CreateTransferRequestDTO`
- `ApproveRequestDTO` (có thể cần `decision_notes`)
- `RejectRequestDTO` (cần `rejection_reason`)

❌ **Response DTOs (output):**
- `StudentRequestDetailDTO` (chi tiết hơn base DTO)
- `AvailableMakeupSessionDTO` (cho makeup search)
- `TransferValidationResultDTO` (content gap analysis)

#### 5. Helper/Utility Services
- ❌ **Makeup Session Finder** - Logic tìm available makeup sessions
- ❌ **Transfer Content Mapper** - Logic map `course_session_id` giữa 2 classes
- ❌ **Conflict Detector** - Kiểm tra student schedule conflicts

## 📋 DANH SÁCH ENDPOINTS CẦN IMPLEMENT

### GROUP 1: STUDENT SUBMISSION (Student Role) 🎓

#### 1.1. Absence Request

```http
POST /api/v1/students/{studentId}/requests/absence
```

**Request Body:**
```json
{
  "targetSessionId": 123,
  "reason": "Family emergency"
}
```

**Response:** `StudentRequestDTO`

---

#### 1.2. Makeup Request

```http
POST /api/v1/students/{studentId}/requests/makeup
```

**Request Body:**
```json
{
  "targetSessionId": 123,      // Buổi đã nghỉ
  "makeupSessionId": 245,      // Buổi muốn học bù
  "reason": "Make up for missed session"
}
```

**Response:** `StudentRequestDTO`

---

#### 1.2.1. Helper endpoint cho makeup

```http
GET /api/v1/students/{studentId}/sessions/{sessionId}/available-makeups
```

**Query params:**
- `date_from` (optional): earliest date
- `date_to` (optional): latest date
- `branch_id` (optional): prefer branch
- `modality` (optional): OFFLINE/ONLINE/HYBRID

**Response:**
```json
{
  "total": 5,
  "makeupSessions": [
    {
      "sessionId": 245,
      "classId": 102,
      "className": "English A1 Evening",
      "branchName": "Hoàn Kiếm",
      "modality": "OFFLINE",
      "date": "2025-02-12",
      "startTime": "14:00",
      "endTime": "16:30",
      "courseSessionId": 5,
      "topic": "Listening Practice",
      "availableSlots": 5,
      "maxCapacity": 25
    }
  ]
}
```

---

#### 1.3. Transfer Request

```http
POST /api/v1/students/{studentId}/requests/transfer
```

**Request Body:**
```json
{
  "currentClassId": 101,
  "targetClassId": 205,
  "effectiveDate": "2025-02-15",
  "reason": "Schedule conflict with work"
}
```

**Response:** `StudentRequestDTO`

---

#### 1.3.1. Helper endpoint cho transfer validation

```http
POST /api/v1/students/{studentId}/transfer-validation
```

**Request Body:**
```json
{
  "currentClassId": 101,
  "targetClassId": 205,
  "effectiveDate": "2025-02-15"
}
```

**Response:**
```json
{
  "isValid": true,
  "sameCourse": true,
  "hasCapacity": true,
  "contentGaps": [
    {
      "courseSessionId": 11,
      "sequenceNo": 11,
      "topic": "Grammar - Present Perfect"
    },
    {
      "courseSessionId": 15,
      "sequenceNo": 15,
      "topic": "Vocabulary - Travel"
    }
  ],
  "warnings": [
    "You will miss 2 sessions (11, 15). Materials will be provided for self-study."
  ],
  "currentProgress": {
    "lastAttendedSessionId": 1024,
    "sessionSequence": 10,
    "completedSessions": 10,
    "remainingSessions": 26
  },
  "targetClassInfo": {
    "currentSessionSequence": 12,
    "availableSessions": 25,
    "availableCapacity": 3
  }
}
```

---

### GROUP 2: REQUEST MANAGEMENT (Student View) 👀

#### 2.1. List My Requests

```http
GET /api/v1/students/{studentId}/requests
```

**Query params:**
- `type`: ABSENCE | MAKEUP | TRANSFER
- `status`: PENDING | APPROVED | REJECTED | CANCELLED
- `date_from`, `date_to`
- `page`, `size`, `sort`

**Response:** `PagedResponse<StudentRequestDTO>`

---

#### 2.2. Get Request Detail

```http
GET /api/v1/students/{studentId}/requests/{requestId}
```

**Response:**
```json
{
  "id": 1,
  "requestType": "MAKEUP",
  "status": "PENDING",
  "student": {...},
  "targetSession": {
    "id": 123,
    "date": "2025-02-10",
    "topic": "Listening Practice"
  },
  "makeupSession": {
    "id": 245,
    "date": "2025-02-12",
    "className": "A1-Evening-02"
  },
  "submittedAt": "2025-02-11T10:00:00Z",
  "submittedBy": {...},
  "decidedBy": null,
  "decidedAt": null,
  "note": "Make up for missed session",
  "decisionNotes": null
}
```

---

#### 2.3. Cancel My Request

```http
POST /api/v1/students/{studentId}/requests/{requestId}/cancel
```

**Response:** `StudentRequestDTO` (status = CANCELLED)

---

### GROUP 3: ACADEMIC STAFF REVIEW (Academic Staff Role) 👨‍💼

#### 3.1. List All Pending Requests (Dashboard)

```http
GET /api/v1/student-requests
```

**Query params:**
- `status`: PENDING | APPROVED | REJECTED (default: PENDING)
- `type`: ABSENCE | MAKEUP | TRANSFER
- `branch_id`: filter by branch
- `student_id`: filter by student
- `submitted_from`, `submitted_to`
- `page`, `size`, `sort`

**Response:** `PagedResponse<StudentRequestDTO>`

---

#### 3.2. Get Request Detail (for review)

```http
GET /api/v1/student-requests/{requestId}
```

**Response:** `StudentRequestDetailDTO` (giống student view nhưng có thêm fields cho review)

---

#### 3.3. Approve Request

```http
POST /api/v1/student-requests/{requestId}/approve
```

**Request Body:**
```json
{
  "decisionNotes": "Approved. Student will attend Class B from Feb 15."
}
```

**Response:**
```json
{
  "id": 1,
  "status": "APPROVED",
  "decidedAt": "2025-02-11T15:30:00Z",
  "decidedBy": {...},
  "decisionNotes": "...",
  "executionResult": {
    "success": true,
    "affectedRecords": {
      "enrollmentsUpdated": 2,
      "studentSessionsCreated": 25,
      "studentSessionsMarkedExcused": 26
    }
  }
}
```

---

#### 3.4. Reject Request

```http
POST /api/v1/student-requests/{requestId}/reject
```

**Request Body:**
```json
{
  "rejectionReason": "Target class is full. Please choose another class."
}
```

**Response:** `StudentRequestDTO` (status = REJECTED)

---

### GROUP 4: BULK OPERATIONS (Academic Staff) 📦

#### 4.1. Approve Multiple Requests

```http
POST /api/v1/student-requests/bulk-approve
```

**Request Body:**
```json
{
  "requestIds": [1, 2, 3, 4],
  "decisionNotes": "Batch approved"
}
```

**Response:**
```json
{
  "total": 4,
  "successful": 3,
  "failed": 1,
  "results": [
    {"requestId": 1, "status": "success"},
    {"requestId": 2, "status": "success"},
    {"requestId": 3, "status": "success"},
    {"requestId": 4, "status": "failed", "error": "Capacity full"}
  ]
}
```

---

#### 4.2. Auto-approve Absence Requests (Optional)

```http
POST /api/v1/student-requests/auto-approve-absence
```

**Request Body:**
```json
{
  "criteria": {
    "submittedBefore": "2025-02-10",
    "branchId": 1
  }
}
```

**Response:** `BulkOperationResult`

---

### GROUP 5: STATISTICS & ANALYTICS 📊

#### 5.1. Request Statistics

```http
GET /api/v1/student-requests/statistics
```

**Query params:**
- `date_from`, `date_to`
- `branch_id`

**Response:**
```json
{
  "totalRequests": 150,
  "byType": {
    "ABSENCE": 80,
    "MAKEUP": 50,
    "TRANSFER": 20
  },
  "byStatus": {
    "PENDING": 30,
    "APPROVED": 100,
    "REJECTED": 15,
    "CANCELLED": 5
  },
  "averageProcessingTime": "2.5 hours",
  "approvalRate": 86.96
}
```

---

#### 5.2. Student Request History

```http
GET /api/v1/students/{studentId}/request-history
```

**Response:**
```json
{
  "studentId": 1,
  "totalRequests": 5,
  "approvedCount": 4,
  "rejectedCount": 1,
  "history": [...]
}
```

---

## 🎯 TỔNG KẾT ENDPOINTS

| Category | Endpoint Count | Priority |
|----------|----------------|----------|
| **Student Submission** | 3 create + 2 helpers = **5** | ⭐⭐⭐⭐⭐ CRITICAL |
| **Student View** | **3** (list, detail, cancel) | ⭐⭐⭐⭐ HIGH |
| **Academic Staff Review** | **4** (list, detail, approve, reject) | ⭐⭐⭐⭐⭐ CRITICAL |
| **Bulk Operations** | **2** | ⭐⭐⭐ MEDIUM |
| **Statistics** | **2** | ⭐⭐ LOW |
| **TOTAL** | **16 endpoints** | |

## 🚀 IMPLEMENTATION ROADMAP

### Phase 1: Foundation (Priority 1)
1. ✅ Create `StudentRequestRepository`
2. ✅ Create `StudentRequestService` interface
3. ✅ Create DTOs (Request + Response)
4. ✅ Create `StudentRequestController` skeleton

### Phase 2: Absence Flow (Easiest)
5. ✅ Implement absence request submission
6. ✅ Implement absence request approval
7. ✅ Unit tests + Integration tests

### Phase 3: Makeup Flow (Complex)
8. ✅ Implement available makeup session finder
9. ✅ Implement makeup request submission (with validation)
10. ✅ Implement makeup request approval (update student_session)
11. ✅ Unit tests + Integration tests

### Phase 4: Transfer Flow (Most Complex)
12. ✅ Implement transfer validation helper
13. ✅ Implement content gap analyzer
14. ✅ Implement transfer request submission
15. ✅ Implement transfer request approval (BIG TRANSACTION)
16. ✅ Edge case handling
17. ✅ Unit tests + Integration tests

### Phase 5: Management Features
18. ✅ List/filter/search endpoints
19. ✅ Reject functionality
20. ✅ Cancel functionality
21. ✅ Bulk operations (optional)

### Phase 6: Analytics (Optional)
22. ✅ Statistics endpoints
23. ✅ Reports

---

**END OF DOCUMENT**