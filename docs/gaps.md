 ---
  ✅ CÁC PHẦN ĐÃ ĐẦY ĐỦ:

  1. Phân quyền Roles ✅

  - 8 roles được định nghĩa rõ ràng
  - RBAC với user_branches (multi-branch access control)
  - Phân biệt rõ Manager vs Center Head
  - Approval flows đã được map đúng người

  2. Core Operations ✅

  - ✅ Curriculum design (Subject → Level → Course → Phase → Session)
  - ✅ Class creation với auto-generate sessions
  - ✅ Student enrollment với auto-generate student_session
  - ✅ Attendance recording và session reporting
  - ✅ Teacher assignment với skill matching
  - ✅ Resource assignment (Room/Zoom) với conflict detection

  3. Request Workflows ✅

  Student Requests:
  - ✅ Absence request
  - ✅ Make-up request (với session matching theo course_session_id)
  - ✅ Transfer request (với content mapping)
  - ✅ Reschedule request

  Teacher Requests:
  - ✅ Leave request (với substitute finding logic)
  - ✅ OT registration và auto-assignment
  - ✅ Swap request
  - ✅ Reschedule request

  4. Assessment & Quality ✅

  - ✅ Assessment definition và score entry
  - ✅ Student feedback
  - ✅ QA reports
  - ✅ PLO-CLO mapping và tracking

  5. Reporting & Analytics ✅

  - ✅ Enrollment reports
  - ✅ Attendance reports
  - ✅ Teacher workload reports
  - ✅ Class progress reports
  - ✅ Executive dashboards (phân biệt Center Head vs Manager)

  ---
  ⚠️ CÁC GAPS VÀ EDGE CASES CẦN BỔ SUNG:

  A. Phân quyền & Access Control

  ❌ 1. Branch Assignment Logic chưa rõ:

  Vấn đề:
  - Manager quản lý "nhiều branches" → nhưng cơ chế gán như thế nào?
    - Manager có trong user_branches không? Hay Manager tự động có quyền ALL branches?
    - Nếu Manager có user_branches, thì scope là "tất cả branches trong user_branches" hay "tất cả branches trong hệ thống"?

  Giải pháp đề xuất:
  // Option 1: Manager có flag đặc biệt
  @Entity
  public class UserRole {
      @Id private UserRoleId id;
      private boolean isSystemWide; // Manager = true, Center Head = false
  }

  // Option 2: Manager không cần user_branches (implicit ALL access)
  // Center Head phải có user_branches với ĐÚNG 1 branch

  ---
  ❌ 2. Cross-branch operations chưa rõ ràng:

  Case chưa cover:
  Q: Manager có thể approve class của branch mà mình KHÔNG được assign trong user_branches không?
  Q: Center Head của Branch A có thể xem data của Branch B không? (để coordinate transfer)
  Q: Academic Staff được assign 2 branches → có thể tạo class cross-branch không?

  Missing business rules:
  - Ai có quyền MOVE teacher từ Branch A sang Branch B?
  - Ai có quyền MERGE classes từ 2 branches?
  - Transfer student giữa 2 branches khác Manager → ai approve?

  ---
  ❌ 3. Subject Leader scope không rõ:

  Vấn đề:
  - Subject Leader tạo Course → Course này áp dụng cho:
    - Tất cả branches toàn hệ thống? (centralized curriculum)
    - Chỉ branches được assign? (decentralized curriculum)

  - Nếu Subject Leader tạo Course cho "English General":
    - Branch A và Branch B có thể dùng chung Course này không?
    - Hay mỗi branch có Course riêng?

  Giải pháp đề xuất:
  Thêm field vào Course:
  - is_global: true → apply cho tất cả branches
  - is_global: false → chỉ áp dụng cho specific branches (cần bảng course_branches)

  ---
  B. Operational Gaps

  ❌ 4. Teacher cross-branch assignment:

  Case chưa cover:
  Scenario: Teacher Nguyễn Văn A làm việc tại Branch Cầu Giấy.
  - Branch Hoàn Kiếm thiếu teacher → có thể "mượn" teacher từ Branch Cầu Giấy không?
  - Ai có quyền approve việc này? (Center Head A? Center Head B? Manager?)
  - Teacher có bị trừ giờ teaching từ quota của Branch A không?

  Missing:
  - Teacher borrowing/lending workflow
  - Cross-branch teaching tracking (cho KPI và payroll)
  - Conflict resolution khi teacher dạy 2 branches cùng lúc

  ---
  ❌ 5. Resource sharing giữa branches:

  Case chưa cover:
  Scenario: Branch A có Zoom license dư, Branch B thiếu.
  - Có thể share Zoom account cross-branch không?
  - Resource.branch_id = ? → nếu share thì gán branch nào?
  - Conflict detection có hoạt động cross-branch không?

  Missing:
  - Resource pooling mechanism (shared resources)
  - Cross-branch resource utilization reports

  ---
  ❌ 6. Late enrollment edge cases:

  Case chưa đủ:
  feature-list.md nói:
  "6.5.2 Hệ thống sinh student_session chỉ cho các buổi tương lai"

  Nhưng:
  Q: Student vào muộn 10 buổi → 10 buổi đầu không có điểm attendance → ảnh hưởng attendance rate thế nào?
  Q: Late enrollment có deadline không? (ví dụ: không cho vào sau 20% khóa học)
  Q: Late student có được make-up 10 buổi đầu không? Hay bắt buộc self-study?

  Missing business rules:
  - Late enrollment cutoff point (ví dụ: max 5 sessions, hoặc max 20% course)
  - Late enrollment fee adjustment (học ít hơn → trả ít hơn?)
  - Late student catch-up mechanism (tự học? make-up mandatory?)

  ---
  ❌ 7. Transfer request content gap handling:

  business-context.md có đề cập nhưng chưa có solution:
  Dòng 347-355:
  "Edge Case: Lớp B đã dạy qua một số buổi mà A chưa học → buổi đó bị thiếu
  Solution:
    - Bỏ qua (student xem lại record) ← CHƯA CÓ MECHANISM
    - Hoặc chuyển muộn hơn ← AI ENFORCE?
  "

  Missing:
  - Content gap detection algorithm (tự động highlight missing sessions)
  - Make-up plan generation (suggest sessions student cần học bù)
  - Content gap approval flow (Academic Staff/Center Head must acknowledge gap before approve)

  ---
  ❌ 8. Teacher leave → cascade cancellation:

  Case chưa cover đủ:
  Scenario: Teacher xin nghỉ 5 buổi liên tục (tuần nghỉ phép).
  - Không tìm được substitute cho TẤT CẢ 5 buổi.
  - Không thể reschedule 5 buổi (lịch branch đã kín).

  Option hiện tại: Cancel 5 buổi → 5 buổi bị mất content.

  Missing:
  - Class extension mechanism: thêm 5 buổi vào cuối khóa để bù content
  - Automatic rescheduling suggestion: tìm slot trống trong 2 tuần tới
  - Notify mechanism: gửi email hàng loạt cho 25 students về 5 buổi cancel

  ---
  ❌ 9. Capacity override tracking:

  Đã có feature nhưng chưa có governance:
  feature-list.md:
  "6.2.1 Validate: Class chưa full (enrolled count < max_capacity) – có thể override nếu policy cho phép"

  Missing:
  Q: Ai có quyền override? (Academic Staff? Center Head? Manager?)
  Q: Override có limit không? (max thêm bao nhiêu students?)
  Q: Override có cần approval không?
  Q: Override có ảnh hưởng đến room capacity không? (physical room vs Zoom)

  Missing business rules:
  - Capacity override approval workflow
  - Physical room capacity hard limit (fire safety) vs Zoom flexible capacity
  - Override audit log (track ai override, lý do gì, bao nhiêu lần)

  ---
  C. Data Integrity & Edge Cases

  ❌ 10. Session cancellation → assessment impact:

  Case chưa cover:
  Scenario: Session 15 bị cancel (không có teacher, không reschedule được).
  - Session 15 gắn với Assessment "Midterm Exam".
  - Bây giờ Assessment này diễn ra ở đâu? Session nào?

  Missing:
  - Assessment rescheduling workflow khi session cancel
  - Assessment dependency check (không cho cancel session có assessment)

  ---
  ❌ 11. Class completion criteria:

  Chưa rõ:
  Q: Lớp kết thúc khi nào?
    - Khi hết 36 sessions theo template?
    - Khi hết planned_end_date?
    - Khi 80% students hoàn thành?

  Q: Nếu có 5 sessions bị cancel → class vẫn complete?
  Q: Nếu class kéo dài hơn dự kiến (do reschedule nhiều) → auto-extend hay cần approval?

  Missing:
  - Class completion criteria definition
  - Auto-completion trigger
  - Class extension workflow (nếu chưa đủ sessions)

  ---
  ❌ 12. Concurrent modification conflicts:

  Case chưa cover:
  Scenario:
  - Academic Staff A đang assign Teacher X vào Session 15 (10:00 AM).
  - Đồng thời Academic Staff B đang assign Teacher X vào Session 20 (10:00 AM cùng ngày).
  - Cả 2 đều pass conflict check (vì transaction chưa commit).
  - Cả 2 commit → conflict!

  Missing:
  - Optimistic locking (version field trong Session/TeachingSlot)
  - Retry mechanism khi conflict
  - User-friendly error message

  ---
  D. System Configuration & Policies

  ❌ 13. Policy configuration chưa đủ:

  feature-list.md đề cập nhưng chưa có implementation:
  11.1.1 Admin cấu hình các tham số toàn hệ thống:
  - Ngưỡng nghỉ tối đa cho học viên (số buổi) ← CHƯA CÓ ENFORCEMENT
  - Ngưỡng đổi lịch/nghỉ tối đa cho teacher (số lần/tháng) ← CHƯA CÓ VALIDATION
  - Thời gian lock điểm danh sau buổi học (T giờ) ← ĐÃ CÓ
  - Lead-time tối thiểu cho request (số ngày trước buổi học) ← CHƯA CÓ VALIDATION
  - Policy capacity override ← CHƯA CÓ

  Missing:
  - SystemConfig entity/table để lưu policies
  - Policy validation trong request workflows
  - Policy override mechanism (với approval)

  ---

  #### ❌ **14. Notification system chưa rõ:**
  **feature-list.md có đề cập (Module 11.2) nhưng thiếu details:**
  11.2.1 Hệ thống gửi notification qua nhiều kênh:
  - Email (class approved, schedule changed, request approved/rejected)
  - SMS (urgent: class cancelled, teacher change)
  - In-app notification (portal)

  Missing:
  - Notification template management (ai tạo template? format thế nào?)
  - Retry mechanism (nếu email fail thì sao?)
  - Notification log (track notification đã gửi chưa?)
  - Opt-out mechanism (user có thể tắt notification không?)

  ---

  ### **E. Reporting & Analytics Gaps**

  #### ❌ **15. Financial tracking hoàn toàn thiếu:**
  **Chưa có:**
  - Enrollment fee tracking
  - Payment status (paid/unpaid/partial)
  - Refund logic (student drop/transfer)
  - Teacher payroll calculation (base salary + OT hours)
  - Revenue reports by branch/class/course

  **Note:** Feature-list.md có đề cập "12.3 Payment & Billing Integration" nhưng trong "vNext / Optional" → chưa implement.

  ---

  #### ❌ **16. Attendance KPI enforcement:**
  **Có report nhưng chưa có action:**
  feature-list.md:
  "10.2.2 Top absences & cảnh báo học viên vượt ngưỡng nghỉ"

  Missing:
  Q: Khi student vượt ngưỡng nghỉ (ví dụ: nghỉ > 5 buổi) → hệ thống làm gì?
  - Tự động chuyển status enrollment sang "at_risk"?
  - Gửi warning email cho student?
  - Block student khỏi make-up requests?
  - Require Center Head review?

  ---

  #### ❌ **17. Teacher performance review:**
  **Có data nhưng chưa có workflow:**
  QA reports + Student feedback → teacher performance score

  Missing:
  - Teacher performance review workflow
  - Performance-based decisions (promote/warn/terminate)
  - Performance history tracking

  ---

  ## 📊 **TỔNG KẾT:**

  ### ✅ **Điểm mạnh:**
  - Core flows đã rất đầy đủ và chi tiết
  - Session-first design rất solid
  - Request workflows cover nhiều cases
  - Reporting khá comprehensive

  ### ⚠️ **Cần bổ sung NGAY (Critical):**

  1. **Phân quyền cross-branch rõ ràng** (Manager scope, Center Head scope)
  2. **Capacity override approval workflow**
  3. **Late enrollment cutoff rules**
  4. **Content gap detection cho transfer**
  5. **Policy configuration & validation**
  6. **Concurrent conflict handling** (optimistic locking)

  ### 🔮 **Cần bổ sung SAU (Important nhưng không urgent):**

  7. Teacher cross-branch assignment workflow
  8. Resource sharing mechanism
  9. Class completion criteria & auto-completion
  10. Assessment rescheduling khi session cancel
  11. Notification retry & logging
  12. Teacher leave cascade handling (class extension)