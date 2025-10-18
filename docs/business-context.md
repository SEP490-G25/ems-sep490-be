# **EMS (Education Management System) - Business Context Summary**

## **1. System Overview**

### **1.1 Domain & Purpose**
EMS is a comprehensive **Education Management System** designed specifically for **multi-branch language training centers** (e.g., English, Japanese language schools). The system manages the complete lifecycle of educational operations from curriculum design to class delivery, attendance tracking, and quality assurance.

### **1.2 Core Business Problem**
Language training centers face complex operational challenges:
- **Multi-tenant operations**: Managing multiple centers with multiple branches, each requiring independent yet coordinated operations
- **Complex scheduling**: Coordinating teachers, rooms, online platforms (Zoom), and student schedules across different time slots and modalities (offline/online/hybrid)
- **Dynamic changes**: Handling frequent requests for schedule changes, teacher absences, student make-ups, and class transfers
- **Curriculum integrity**: Ensuring standardized curriculum delivery while allowing flexibility in execution
- **Quality tracking**: Monitoring learning outcomes (PLO/CLO), attendance rates, and teaching quality across all operations

### **1.3 Key Business Entities**
- **Organization**: Center → Branch → Resources (Rooms/Zoom) → Time Slots
- **Curriculum**: Subject → Level → Course → Phase → Course Session (template)
- **Operations**: Class → Session (actual meeting) → Teaching Slot → Session Resource
- **People**: Students, Teachers (with skills/availability), Academic Staff, Managers
- **Requests**: Student requests (absence/make-up/transfer), Teacher requests (leave/OT/reschedule)
- **Assessment**: Assessments, Scores, Feedback, QA Reports

---

## **2. Organizational Roles & Responsibilities**

### **2.1 Role Hierarchy**

#### **ADMIN** (System Level)
- Full system access
- Create/manage user accounts and assign roles
- Configure system-wide settings (capacity policies, attendance lock times, request lead times)

#### **MANAGER** (System/Multi-Branch Operations Manager)
- **Scope**: Entire system or multiple branches (strategic oversight)
- **Responsibilities**:
  - **Strategic Management**: Monitor KPIs across all branches, operational excellence, performance analytics
  - **High-Level Approvals**: Approve courses designed by Subject Leaders (curriculum standardization)
  - **Cross-Branch Coordination**: Oversee resource allocation, teacher assignments across multiple branches
  - **Executive Oversight**: Review executive dashboards (enrollment trends, branch performance, teacher workload)
  - **Strategic Decisions**: Opening new branches, hiring policies, system-wide policies
  - **Escalated Requests**: Handle complex cross-branch conflicts and escalated issues
- **Key Distinction**: Focuses on **system-wide operational excellence** and strategic decisions, not day-to-day branch operations

#### **CENTER HEAD** (Branch Director)
- **Scope**: ONE specific branch (direct branch management)
- **Responsibilities**:
  - **Branch Operations**: Direct management of daily activities within their branch
  - **Operational Approvals**: Approve classes created by Academic Staff for their branch
  - **Resource Management**: Manage rooms, Zoom accounts, and resources within the branch
  - **Branch Performance**: Monitor and improve branch-level KPIs and quality
  - **Request Handling**: Approve student/teacher requests within branch scope (absence, make-up, transfers)
  - **Staff Supervision**: Oversee Academic Staff, Teachers at the branch
- **Key Distinction**: Focuses on **direct branch operational execution** and tactical day-to-day management

#### **SUBJECT LEADER** (Curriculum Level)
- **Scope**: Subject-specific curriculum design
- **Responsibilities**:
  - Design subjects and levels (e.g., English A1, A2, B1)
  - Create courses with detailed curriculum (phases, sessions, topics, skills)
  - Define learning outcomes (PLO - Program Learning Outcomes, CLO - Course Learning Outcomes)
  - Map CLOs to course sessions for outcome tracking
  - Upload course materials (slides, worksheets, videos)
  - Submit courses for approval by Manager (strategic curriculum approval)
  - **Does NOT** handle day-to-day operations or class scheduling

#### **ACADEMIC STAFF (Giáo vụ)** (Branch Operations Level) ⭐ **KEY OPERATIONAL ROLE**
- **Scope**: Daily operations at assigned branch(es)
- **Responsibilities**:
  - **Class Creation**: Create classes based on approved courses, define schedule (days, time slots, start date)
  - **Resource Assignment**: Assign rooms/Zoom accounts to sessions, detect and resolve conflicts
  - **Teacher Assignment**: Assign teachers to sessions based on skills and availability
  - **Student Enrollment**: Import student lists (CSV), enroll students into classes, handle capacity limits
  - **Request Processing**:
    - Approve student requests (absence, make-up, transfer)
    - Process teacher requests (leave, find substitutes, reschedule sessions, approve OT)
  - **Conflict Resolution**: Handle schedule conflicts, room double-bookings, teacher unavailability
  - **Submit for Approval**: Submit completed class plans to Center Head/Manager for final approval
  - **Note**: Multiple Academic Staff can work at the same branch simultaneously, handling different classes or sharing workload

#### **TEACHER** (Instruction Delivery)
- **Scope**: Teaching sessions and student interaction
- **Responsibilities**:
  - Register skills and availability (regular schedule + OT availability)
  - Teach assigned sessions
  - Record attendance for each session
  - Submit session reports (what was taught, student performance notes)
  - Enter scores and feedback for assessments
  - Submit requests (leave, reschedule, swap sessions with other teachers)

#### **STUDENT** (Learner)
- **Scope**: Personal learning journey
- **Responsibilities**:
  - Attend scheduled sessions
  - Submit requests (absence notification, make-up session, class transfer)
  - Provide feedback on sessions/teachers
  - View personal schedule, grades, and attendance records

#### **QA (Quality Assurance)** (Optional/Monitoring Role)
- Monitor teaching quality
- Create QA reports on classes/sessions
- Track CLO achievement and syllabus adherence
- Provide feedback to management

---

### **2.2 Authority & Approval Flows**

| **Decision** | **Who Initiates** | **Who Approves** | **Impact** |
|-------------|------------------|-----------------|-----------|
| Create Subject/Level | Subject Leader | Auto-approved | Curriculum structure |
| Create Course | Subject Leader | **Manager** (strategic curriculum approval) | Must be approved before use across system |
| Create Class | Academic Staff | **Center Head** (branch) OR **Manager** (cross-branch) | Must be approved before enrollment |
| Enroll Students | Academic Staff | Auto (with capacity check) | Immediate enrollment |
| Assign Teacher | Academic Staff | Auto (with conflict check) | Immediate assignment |
| Student Absence | Student/Academic Staff | Academic Staff | Attendance status update |
| Student Make-up | Student | Academic Staff | New student_session created |
| Student Transfer | Student | Academic Staff OR Center Head | Enrollment + schedule sync |
| Teacher Leave | Teacher/Academic Staff | Academic Staff (must find solution) | Requires substitute/reschedule/cancel |
| Teacher OT | Teacher (registers availability) | Academic Staff (when assigned) | OT request auto-created |
| Reschedule Session | Academic Staff | Academic Staff OR Center Head | All parties notified |
| Cancel Session | Academic Staff | **Center Head** (branch) OR **Manager** (cross-branch) | All students marked excused |

---

## **3. Core Modules / Main Business Flows**

### **3.1 Curriculum Design Flow** (Subject Leader)
**Objective**: Build standardized, reusable curriculum templates

**Steps**:
1. **Create Subject** (e.g., "English General")
2. **Define Levels** (e.g., A1, A2, B1) with expected duration and prerequisites
3. **Create Course** for each level (e.g., "English A1 V1" - 60 hours, 12 weeks, 3 sessions/week)
4. **Break Course into Phases** (e.g., Foundation Phase - 4 weeks, Development Phase - 4 weeks)
5. **Define Course Sessions** (templates) for each phase:
   - Sequence number (order)
   - Topic (e.g., "Greetings & Introductions")
   - Student tasks (e.g., "Practice self-introduction with partners")
   - Skill set (general/reading/writing/speaking/listening)
6. **Define PLOs** (program-level outcomes for the subject)
7. **Define CLOs** (course-level outcomes) and **map to sessions** to track which sessions achieve which outcomes
8. **Upload Materials** (slides, worksheets) at course/phase/session level
9. **Submit for Approval** → **Manager** reviews and approves or rejects (strategic curriculum decision)

**Result**: Approved course templates ready for Academic Staff to use when creating classes

---

### **3.2 Class Creation & Session Generation Flow** (Academic Staff → Center Head/Manager Approval)
**Objective**: Convert a course template into a real, scheduled class with assigned resources and teachers

**Steps**:
1. **Academic Staff creates class**:
   - Select approved course
   - Choose branch and modality (OFFLINE/ONLINE/HYBRID)
   - Set start date and schedule days (e.g., Monday, Wednesday, Friday)
   - Map each schedule day to a time slot template (e.g., "Morning 1: 08:00-10:30")
   - Set max capacity (e.g., 25 students)
   - Status: `draft`

2. **System auto-generates sessions**:
   - Reads all course_session templates from the course (e.g., 36 sessions total)
   - Calculates actual dates based on:
     - Start date + schedule days + week offset
     - Example: If start date is Monday Feb 3, and schedule is Mon/Wed/Fri:
       - Session 1 → Mon Feb 3 (08:00-10:30)
       - Session 2 → Wed Feb 5 (08:00-10:30)
       - Session 3 → Fri Feb 7 (08:00-10:30)
       - Session 4 → Mon Feb 10 (08:00-10:30)
       - ... continues for 12 weeks = 36 sessions
   - Each session links to its course_session template (topic, skills, materials)
   - All sessions start with status: `planned`

3. **Academic Staff assigns resources**:
   - For OFFLINE: Assign physical room (e.g., "Room 101")
   - For ONLINE: Assign Zoom account (e.g., "Zoom Account 1")
   - For HYBRID: Assign both
   - System checks for conflicts (same resource, overlapping time)

4. **Academic Staff assigns teachers**:
   - For each session (or batch of sessions), assign teacher(s) based on:
     - Skill match (e.g., session requires "speaking" → find teacher with "speaking" skill)
     - Availability (teacher's regular schedule + availability overrides)
     - No conflicts (teacher not already teaching another session at the same time)
   - System suggests available teachers; Academic Staff confirms
   - One session can have multiple teachers for different skills (e.g., Speaking teacher + Listening teacher)

5. **System validation**:
   - Check for resource conflicts
   - Check for teacher conflicts
   - Check for schedule adherence to time slot templates

6. **Academic Staff submits class** → Status: `draft` → `submitted_at` timestamp

7. **Center Head (for their branch) OR Manager (cross-branch authority) reviews**:
   - If approved: Status → `scheduled`, can now enroll students
   - If rejected: Status → `draft`, Academic Staff must fix issues
   - **Note**: Center Head approves classes for their branch; Manager can approve across all branches

**Result**: Fully scheduled class with 36 sessions, all with assigned rooms/Zoom and teachers, ready for student enrollment

---

### **3.3 Student Enrollment & Schedule Sync Flow** (Academic Staff)
**Objective**: Register students into classes and auto-generate their personal schedules

**Business Context**:
A large corporate client (e.g., Viettel) sends 500 employees for training. **Manager** (with cross-branch oversight) divides them across branches by level:
- 100 Beginners → Cầu Giấy branch
- 150 Intermediate → Hoàn Kiếm branch
- 250 Advanced → distributed across 3 branches

Each branch's Academic Staff receives their student list and creates classes accordingly.

**Steps**:
1. **Import or Create Students**:
   - Academic Staff uploads CSV (full_name, email, phone, branch_id)
   - System checks if user_account exists (by email/phone)
   - If new: Create user_account + student record
   - If existing: Link to student record (user might already be a visitor or have inquired before)

2. **Enroll Students into Class**:
   - Academic Staff selects students (multi-select from list)
   - Clicks "Enroll into Class X"
   - System validates:
     - Class capacity (enrolled < max_capacity)
     - Schedule conflict check (student not already in another class at the same time)
   - System creates enrollment record (status: `enrolled`, enrolled_at: now())

3. **Auto-Generate Personal Schedule**:
   - For each session in the class (all 36 sessions), system creates `student_session` record:
     - student_id, session_id
     - attendance_status: `planned`
     - is_makeup: `false`
   - This is the student's personal schedule

4. **Late Enrollment** (mid-course):
   - If student joins after class has started (e.g., after 5 sessions already happened)
   - System only creates student_session for future sessions (session 6-36)
   - Records join_session_id in enrollment to track when student joined

**Result**: Student is officially enrolled, has personal schedule synced with class sessions, appears on teacher's attendance list

---

### **3.4 Attendance & Session Reporting Flow** (Teacher)
**Objective**: Track student attendance, record session outcomes, and provide basis for reporting

**Steps**:
1. **Before Session**:
   - Teacher sees list of enrolled students via student_session table
   - Each student has attendance_status: `planned`

2. **During/After Session**:
   - Teacher opens attendance list for the session
   - Marks each student: `present`, `absent`, `late`, `excused`, `remote`
   - System updates student_session.attendance_status
   - System records recorded_at timestamp

3. **Session Report**:
   - Teacher fills session report:
     - What was taught (matches course_session.topic)
     - Student performance notes
     - Any issues or highlights
   - System updates session.teacher_note
   - System updates session.status: `planned` → `done`

4. **Attendance Lock**:
   - After T hours (configured by Admin), system locks attendance
   - Teachers cannot modify attendance after lock (data integrity for reports)

5. **Scoring** (if assessment tied to session):
   - Teacher enters scores for assessments (quiz, midterm, final)
   - System stores in score table (assessment_id, student_id, score, feedback)

**Result**: Complete attendance record, session outcomes documented, data ready for reporting and analytics

---

### **3.5 Student Request Handling Flow** (Student → Academic Staff)

#### **3.5.1 Absence Request**
**Scenario**: Student knows in advance they will miss a session

**Steps**:
1. Student submits absence request (target_session_id, reason)
2. Academic Staff reviews and approves
3. System updates student_session.attendance_status: `planned` → `excused`
4. Student's absence is recorded as "excused" (not counted as unexcused absence)

---

#### **3.5.2 Make-up Request** ⭐ **COMPLEX FLOW**
**Scenario**: Student missed a session and wants to attend a different class with the same content

**Business Rule**: Students can make up missed sessions by attending another class's session **with the same course_session_id** (same topic/content), possibly different day/time/branch/modality.

**Steps**:
1. **Student selects missed session** (e.g., "Session 5: Listening Practice" in Class A)

2. **System finds available make-up sessions**:
   - Query all sessions with same course_session_id (same topic)
   - Filter: status = `planned`, date ≥ today
   - Filter: capacity check (current enrolled < max capacity, or allow override)
   - Sort by: capacity available (priority) → date (soonest first)
   - Example results:
     - Class B - Session 5 - Wed Feb 12, 14:00-16:30 (Branch: Hoàn Kiếm, 3 slots available)
     - Class C - Session 5 - Thu Feb 13, 18:00-20:30 (Branch: Cầu Giấy Online, 5 slots available)

3. **Student selects preferred make-up session** and submits request:
   - target_session_id: original missed session (Session 5 in Class A)
   - makeup_session_id: chosen session (Session 5 in Class B)

4. **Academic Staff reviews**:
   - Verifies same course_session_id (same content)
   - Verifies capacity available
   - Approves request

5. **System executes**:
   - Updates original student_session: attendance_status → `excused`
   - Inserts NEW student_session: (student_id, makeup_session_id, is_makeup=TRUE, attendance_status=`planned`)

6. **Teacher sees student in make-up class**:
   - Teacher of Class B Session 5 now sees this student in their attendance list
   - Student is marked with is_makeup=TRUE (visible to teacher)
   - Teacher marks attendance normally (present/absent/late)

**Result**: Student attended make-up session, original absence excused, no gap in learning

---

#### **3.5.3 Transfer Request** ⭐ **MOST COMPLEX FLOW**
**Scenario**: Student wants to change class mid-course (e.g., OFFLINE → ONLINE, or different branch/schedule)

**Business Rules**:
- Transfer only allowed if **both classes use the same course** (course_id must match)
- System maps remaining sessions by course_session_id to ensure content continuity
- Original class enrollment: status → `transferred` (not deleted, preserves audit trail)
- New class enrollment: created from transfer point onward
- Past sessions in original class: kept as-is (history preserved)
- Future sessions in original class: marked `excused` (not deleted)
- Future sessions in new class: new student_session records created, mapped by course_session_id

**Steps**:
1. **Student submits transfer request**:
   - current_class_id: Class A (e.g., "English A1 Mon/Wed/Fri Morning - Cầu Giấy OFFLINE")
   - target_class_id: Class B (e.g., "English A1 Tue/Thu/Sat Evening - Online")
   - effective_date: When to start in new class (e.g., Feb 15)

2. **Academic Staff validates**:
   - Checks both classes have same course_id (same curriculum)
   - Checks Class B status: `scheduled` or `ongoing`
   - Checks Class B capacity: has available slots
   - Identifies cutoff point:
     - left_session_id: Last session student attended in Class A (before effective_date)
     - join_session_id: First session student will attend in Class B (on/after effective_date)

3. **System checks for content gaps** (Edge Case):
   - Class A remaining sessions: course_session_id [15, 16, 17, 18, ... 36]
   - Class B future sessions: course_session_id [16, 18, 19, 20, ... 36]
   - **Gap detected**: course_session_id 15 and 17 not available in Class B (Class B already finished those)
   - **Solution**:
     - Option A: Student reviews materials for missed topics (15, 17) independently
     - Option B: Delay transfer effective_date to avoid gap

4. **Academic Staff approves** (assuming no critical gaps)

5. **System executes in ONE TRANSACTION**:
   ```
   Step 1: Update enrollment in Class A
     - status: `enrolled` → `transferred`
     - left_at: now()
     - left_session_id: last session attended

   Step 2: Create new enrollment in Class B
     - status: `enrolled`
     - enrolled_at: now()
     - join_session_id: first session in Class B

   Step 3: Mark future sessions in Class A as excused
     - All student_session records where session.date ≥ effective_date
     - attendance_status → `excused`
     - Note: "Transferred to Class B on [date]"
     - NOT DELETED (audit trail preserved)

   Step 4: Generate student_session for Class B
     - For each session in Class B where:
       - status = `planned`
       - date ≥ effective_date
       - course_session_id IN (remaining course_session_ids from Class A)
     - Insert student_session (student_id, session_id, is_makeup=FALSE, attendance_status=`planned`)
     - Note: "Transferred from Class A on [date]"
   ```

6. **Result**:
   - Student's history in Class A preserved (attendance, scores kept)
   - Student appears in Class B teacher's attendance lists from join_session_id onward
   - Reports show student in Class A until transfer date, then in Class B
   - Enrollment history traceable for auditing

**Edge Cases**:
- **Different course_id**: If Class A and B use different courses (e.g., transfer from English A1 to Japanese N5), system treats it as "drop from A + enroll fresh in B" (no content mapping)
- **Capacity full in Class B**: Academic Staff can override capacity limit (session_resource.capacity_override) if policy allows

---

### **3.6 Teacher Request Handling Flow** (Teacher → Academic Staff)

#### **3.6.1 Leave Request** ⭐ **CRITICAL FLOW**
**Scenario**: Teacher has urgent personal issue and cannot teach a session

**Business Rule**: Leave request only approved when a **solution** is confirmed:
- Option A: Find substitute teacher
- Option B: Reschedule session to different date/time
- Option C: Cancel session (last resort)

**Steps**:
1. **Teacher submits leave request**:
   - session_id: the session they cannot teach
   - request_type: `leave`
   - reason: "Family emergency"
   - Status: `pending`

2. **Academic Staff receives request** and **must find a solution**:

   **Option A: Find Substitute Teacher**

   a. System searches for available substitute teachers:
      - Skill match: teacher_skill matches session's required skills (from course_session.skill_set)
      - Availability:
        - Check teacher_availability_override (date-specific) **FIRST** (OT registrations)
        - If no override, check teacher_availability (regular weekly schedule)
      - No conflict: teacher not teaching another session at the same time
      - Same branch (or branches teacher is assigned to via user_branches)

   b. System returns ranked list:
      - Priority 1: Teachers who registered OT for that exact time slot
      - Priority 2: Teachers with regular availability and matching skills

   c. **Academic Staff contacts candidate teachers** (offline) to confirm willingness

   d. Once confirmed, Academic Staff selects substitute teacher and clicks "Approve & Assign Substitute"

   e. **System executes in ONE TRANSACTION**:
      ```
      Step 1: Approve original leave request
        - UPDATE teacher_request SET status='approved', decided_by=staff_id, resolution='Teacher X will substitute'

      Step 2: Create OT request for substitute teacher (for payroll tracking)
        - INSERT INTO teacher_request (teacher_id=substitute_id, session_id, request_type='ot', status='approved')

      Step 3: Update teaching assignment
        - UPDATE teaching_slot SET teacher_id=substitute_id WHERE session_id=X AND teacher_id=original_teacher_id
      ```

   f. **System notifies students** (optional): "Teacher change for Session 15 on Feb 20: Teacher Y will substitute"

   **Option B: Reschedule Session** (if no substitute found)

   a. Academic Staff chooses "Reschedule Session"

   b. Academic Staff selects new target date (e.g., Feb 22 instead of Feb 20)

   c. System finds available time slots on Feb 22:
      - Query time_slot_template for the branch
      - For each slot, check:
        - Original teacher is available (teacher_availability + override)
        - Resource (room/Zoom) is available (no session_resource conflict)
      - Return list of valid slots

   d. Academic Staff selects slot (e.g., "Morning 1: 08:00-10:30")

   e. **System executes in ONE TRANSACTION**:
      ```
      Step 1: Create NEW session
        - Clone all data from old session (class_id, course_session_id, teacher, resource)
        - Set new date (Feb 22) and new time (08:00-10:30 from chosen slot)
        - Status: `planned`

      Step 2: Transfer all links to new session
        - UPDATE student_session SET session_id=new_session_id WHERE session_id=old_session_id
        - UPDATE teaching_slot SET session_id=new_session_id WHERE session_id=old_session_id
        - UPDATE session_resource SET session_id=new_session_id WHERE session_id=old_session_id

      Step 3: Mark old session as cancelled
        - UPDATE session SET status='cancelled' WHERE id=old_session_id

      Step 4: Approve leave request
        - UPDATE teacher_request SET status='approved', resolution='Session rescheduled to Feb 22 08:00-10:30'
      ```

   f. **System notifies all students**: "Session 15 has been rescheduled from Feb 20 14:00 to Feb 22 08:00"

   **Option C: Cancel Session** (last resort, if no substitute and cannot reschedule)

   a. Academic Staff clicks "Cancel Session"

   b. **System executes**:
      ```
      Step 1: Cancel session
        - UPDATE session SET status='cancelled', teacher_note='Cancelled due to teacher unavailability' WHERE id=session_id

      Step 2: Mark all students excused
        - UPDATE student_session SET attendance_status='excused' WHERE session_id=session_id

      Step 3: Approve leave request
        - UPDATE teacher_request SET status='approved', resolution='Session cancelled due to no substitute available'
      ```

   c. **System notifies all students**: "Session 15 on Feb 20 has been cancelled. You are marked as excused."

**Critical Point**: Leave request **remains `pending`** until Academic Staff finds and executes a solution. System prevents approval without a solution.

---

#### **3.6.2 Overtime (OT) Registration**
**Scenario**: Teacher has extra free time and wants to earn additional income by taking extra sessions

**Steps**:
1. **Teacher registers OT availability**:
   - Opens personal calendar
   - Selects date + time range (e.g., Feb 20, 18:00-20:30)
   - System creates teacher_availability_override (date, start_time, end_time, is_available=TRUE)
   - Reason: "Available for OT"

2. **When substitute needed**:
   - System searches for substitute (as in 3.6.1)
   - Teachers with OT registrations appear at **top of list**
   - UI shows "(OT)" badge next to their name

3. **When assigned OT**:
   - System auto-creates teacher_request (teacher_id, session_id, request_type=`ot`, status=`approved`)
   - This record used for payroll calculation (OT hours × OT rate)

**Result**: Teachers incentivized to cover absences, Academic Staff has pool of willing substitutes, OT hours tracked for payroll

---

#### **3.6.3 Reschedule Request**
**Scenario**: Teacher requests to move a session to a different time (not due to leave, just preference)

**Process**: Same as "Option B" in Leave Request flow, but initiated by teacher request instead of as a solution to absence.

---

#### **3.6.4 Swap Request** (Future Feature)
**Scenario**: Two teachers agree to swap sessions

**Process**:
1. Teacher A and Teacher B both submit swap_request
2. Academic Staff verifies both teachers consent
3. System updates teaching_slot for both sessions simultaneously

---

### **3.7 Bulk Class Schedule Reschedule** (Academic Staff with Center Head approval)
**Scenario**: Entire class needs to change schedule (e.g., from morning to afternoon, or change one weekday)

**Example 1**: Change entire class from "Morning 1 (08:00-10:30)" to "Afternoon 2 (14:00-16:30)" starting Feb 15

**Steps**:
1. Academic Staff selects class and clicks "Reschedule Class"
2. Selects effective_from date (Feb 15)
3. Selects target day-of-week (NULL = all days, or specific day like "Monday only")
4. Selects new time slot from time_slot_template
5. System executes:
   ```SQL
   UPDATE session
   SET start_time = new_slot.start_time,
       end_time = new_slot.end_time,
       teacher_note = 'Rescheduled to [new time] effective Feb 15'
   WHERE class_id = :class_id
     AND status = 'planned'
     AND date >= :effective_from
     AND (target_dow IS NULL OR EXTRACT(DOW FROM date) = target_dow);
   ```
6. System validates:
   - Check resource conflicts with other classes
   - Check teacher conflicts with other classes
   - If conflicts found, suggest alternative resources/teachers
7. **System notifies all enrolled students** via email/SMS

**Example 2**: Change only "Monday sessions" from morning to evening (keep Wed/Fri as-is)

**Result**: All future sessions updated in bulk, conflicts detected automatically, students notified

---

### **3.8 Assessment & Feedback Flow**

#### **3.8.1 Assessment Definition** (Academic Staff/Teacher)
**Steps**:
1. Create assessment for a class:
   - name: "Progress Test 1", "Midterm Exam", "Final Project"
   - kind: quiz/midterm/final/assignment/project/oral
   - max_score: 100
   - weight: 20% (of total grade)
   - session_id: (optional) if assessment happens during a specific session
2. System validates: total weight of all assessments ≤ 100%

#### **3.8.2 Score Entry** (Teacher)
**Steps**:
1. **Option A: Manual Entry**
   - Teacher opens assessment, sees list of students
   - Enters score + feedback for each student
   - System saves to score table (assessment_id, student_id, score, feedback, graded_by=teacher_id)

2. **Option B: Import from Excel**
   - Teacher prepares CSV (student_code, score, feedback)
   - Uploads to system
   - System validates (student exists, score ≤ max_score)
   - System imports in bulk

3. **Lock Scores** (optional):
   - After deadline, Academic Staff can lock assessment
   - Teachers cannot modify scores after lock (data integrity)

#### **3.8.3 Student Feedback** (Student)
**Purpose**: Collect "voice of student" to improve teaching quality

**Steps**:
1. After session ends, student receives notification: "Rate your session"
2. Student provides:
   - rating: 1-5 stars
   - comment: free text (what went well, what needs improvement)
3. System stores in student_feedback (student_id, session_id, rating, comment)
4. QA team reviews aggregated feedback:
   - Average rating per class/teacher/phase
   - Identify low-rated sessions for investigation

#### **3.8.4 QA Report** (QA Staff)
**Purpose**: Formal quality assurance reporting

**Steps**:
1. QA staff observes class session or reviews data
2. Creates qa_report:
   - Scope: class_id, session_id, or phase_id
   - report_type: checklist/process/observation
   - findings: "Teacher did not follow syllabus, skipped exercises"
   - action_items: "Remind teacher to follow course materials"
   - status: open/in_progress/resolved/closed
3. QA team follows up until resolved
4. Manager (system-wide) and Center Head (branch-level) review QA reports in their respective dashboards

**Result**: Data-driven quality monitoring, issues tracked to resolution, teacher performance evaluated

---

## **4. Key Business Rules & Decision Points**

### **4.1 Capacity & Enrollment**
- **Rule**: enrolled students < class.max_capacity
- **Exception**: Academic Staff can override capacity (session_resource.capacity_override) with approval
- **For ONLINE classes**: Virtual rooms (Zoom) have flexible capacity (can "squeeze" more students)
- **For OFFLINE classes**: Physical room capacity is hard limit (fire safety regulations)

### **4.2 Curriculum Integrity**
- **Rule**: Only **approved courses** can be used to create classes
- **Rule**: Course structure must be complete before approval:
  - All phases defined
  - All course_sessions defined with sequence_no (no gaps)
  - All CLOs mapped to sessions (for outcome tracking)
- **Rule**: Once class starts, course template changes **do not** affect existing classes (version control)

### **4.3 Schedule Adherence**
- **Rule**: All sessions must use time_slot_template (no "free-form" times)
- **Rationale**: Standardize schedules, simplify reporting, prevent chaos
- **Rule**: Sessions must fall on class.schedule_days (no random days)

### **4.4 Conflict Detection**
- **Resource Conflict**: Same resource (room/Zoom) cannot be assigned to two sessions at overlapping times
- **Teacher Conflict**: Same teacher cannot teach two sessions at overlapping times
- **Student Conflict**: Student cannot be enrolled in two classes with overlapping session times
- **Detection**: System checks conflicts **before** assignment, blocks if conflict found

### **4.5 Approval Workflow**
- **Course Approval**: Subject Leader designs → **Manager** approves (strategic curriculum decision) → can be used across system
- **Class Approval**: Academic Staff creates → **Center Head** (branch) OR **Manager** (cross-branch) approves → can enroll students
- **Request Approval**: Student/Teacher submits → Academic Staff approves → system executes

### **4.6 Data Integrity (No Deletion)**
- **Philosophy**: Preserve audit trail, never delete operational data
- **Instead of DELETE**:
  - Enrollment: status `enrolled` → `transferred`/`dropped`/`completed`
  - Session: status `planned` → `cancelled`/`done`
  - Student_session: attendance_status `planned` → `excused`/`absent`/`present`
- **Rationale**: Enable historical reporting, QA audits, dispute resolution

### **4.7 Attendance Lock**
- **Rule**: After T hours (e.g., 24 hours) after session ends, attendance is locked
- **Rationale**: Prevent retroactive changes, ensure reports are stable
- **Exception**: Admin or Manager can unlock for valid reasons (with audit log)

### **4.8 Request Lead Time**
- **Rule**: Students must submit absence/transfer requests at least X days before session
- **Rationale**: Give Academic Staff time to arrange make-up/transfer
- **Exception**: Emergency requests can be submitted same-day (subject to stricter approval)

---

## **5. Pain Points & Challenges Noted in Business Flows**

### **5.1 Teacher Unavailability Crisis**
**Problem**: Teacher calls in sick 2 hours before class, no substitute available

**Impact**:
- 25 students show up, no one to teach
- Wasted resources (room booked, students traveled)
- Poor student experience

**Current Mitigation**:
- OT registration system → pool of willing substitutes
- System auto-suggests substitutes ranked by skill/availability
- If no substitute, quick reschedule via time_slot_template validation

**Remaining Challenge**: Teachers must confirm OT offline before assignment (system cannot force acceptance)

---

### **5.2 Make-up Session Capacity Overflow**
**Problem**: 10 students from Class A want to make up Session 5 in Class B, but Class B only has 2 free slots

**Impact**: Some students cannot make up, fall behind on curriculum

**Current Mitigation**:
- System shows capacity in make-up search results (students can see if full)
- Academic Staff can override capacity (session_resource.capacity_override) with approval
- Alternative: Schedule dedicated "make-up class" if demand is high

**Remaining Challenge**: Balancing flexibility (allow make-ups) vs. class size control (maintain quality)

---

### **5.3 Transfer Content Gap**
**Problem**: Student transfers from Class A to Class B (same course). Class A just finished Session 15 (Listening Practice), but Class B already finished Session 15 last week and is now on Session 16.

**Impact**: Student misses Session 15 content entirely

**Current Mitigation**:
- System detects gap during transfer validation
- Options:
  - Student reviews Session 15 materials independently (self-study)
  - Delay transfer effective_date to avoid gap
  - Academic Staff arranges 1-on-1 make-up with teacher

**Remaining Challenge**: No automated "fill the gap" solution; relies on manual intervention

---

### **5.4 Multi-Branch Coordination**
**Problem**: Center has 3 branches. Each branch creates schedules independently. Sometimes creates:
- Resource waste: Branch A has empty rooms while Branch B is overcrowded
- Inefficient teacher allocation: Teacher lives near Branch A but assigned to Branch B

**Impact**: Higher operational costs, teacher dissatisfaction

**Current Mitigation**:
- **Manager** has cross-branch visibility via executive dashboard
- Manager can reassign teachers across branches via user_branches
- Manager can coordinate student transfers to less crowded branches

**Remaining Challenge**: No automated load balancing; requires manual management oversight

---

### **5.5 Last-Minute Schedule Changes**
**Problem**: Room suddenly unavailable (AC broken, maintenance needed). Session is in 1 hour.

**Impact**: Chaos, students confused, quality issues

**Current Mitigation**:
- System allows quick resource reassignment if alternative room/Zoom available
- System sends bulk SMS/email notifications to students
- For OFFLINE→ONLINE pivot: Assign Zoom, notify students to join online

**Remaining Challenge**: Students may not see notification in time; need backup communication channel (e.g., WhatsApp group)

---

### **5.6 Attendance Lock Disputes**
**Problem**: Teacher forgets to mark attendance. System locks attendance 24 hours after session. Student complains "I was there!" 3 days later.

**Impact**: Data integrity vs. student satisfaction conflict

**Current Mitigation**:
- Audit trail: all attendance changes logged (who changed, when, from what to what)
- Admin can unlock attendance with justification
- Remind teachers to submit attendance within 24 hours (automated reminder)

**Remaining Challenge**: Balancing flexibility (fix mistakes) vs. rigidity (prevent fraud)

---

## **6. Reporting & Analytics** (High-Level)

### **6.1 Enrollment Dashboard**
- Total students by branch/level/course
- Fill rate by class (enrolled / max_capacity)
- Waitlist status
- Trial-to-enrollment conversion rate

### **6.2 Attendance Dashboard**
- Attendance rate by class/branch/teacher
- Top absences (students with most absences)
- Alert: students exceeding absence threshold

### **6.3 Teacher Workload Dashboard**
- Total teaching hours per teacher (by week/month)
- Number of classes per teacher
- OT hours per teacher (for payroll)
- Request statistics (leave/reschedule frequency)

### **6.4 Class Progress Dashboard**
- % syllabus completed vs. scheduled
- Session completion rate (done/planned/cancelled)
- Deviation from planned_end_date

### **6.5 Quality Dashboard**
- Average student feedback rating by class/teacher
- QA report summary (open issues by branch)
- CLO achievement rate (% of sessions achieving target CLOs)

### **6.6 Resource Utilization Dashboard**
- Room occupancy rate (used hours / available hours)
- Zoom license utilization (concurrent sessions / total licenses)
- Peak usage times (identify need for more resources)

---

## **7. Summary: What Problem Does EMS Solve?**

EMS solves the **operational complexity** of running multi-branch language training centers by:

1. **Standardizing Curriculum** while allowing flexible delivery (templates + instances)
2. **Automating Schedule Generation** from templates (reduces manual errors, saves time)
3. **Centralizing Resource Management** (rooms, Zoom, teachers) with conflict detection
4. **Enabling Dynamic Changes** (absences, transfers, reschedules) through structured request workflows
5. **Preserving Audit Trail** (no deletion, everything logged) for compliance and dispute resolution
6. **Providing Real-Time Visibility** (dashboards, reports) for data-driven management decisions
7. **Balancing Flexibility & Control** (overrides allowed but tracked, approvals required but fast)

**Core Value Proposition**:
- **For Academic Staff**: Turn weeks of manual scheduling into hours of automated setup
- **For Teachers**: Clear visibility of schedule, easy request submission, fair OT opportunities
- **For Students**: Flexible make-up/transfer options, transparent attendance/grades, quality assurance
- **For Managers**: Real-time operational intelligence, proactive issue detection, scalable multi-branch management

---

## **8. Critical Success Factors**

1. **Data Quality**: System only works if users input data accurately (teacher availability, student attendance)
2. **Change Management**: Training staff to use system (shift from Excel/WhatsApp to structured platform)
3. **Policy Enforcement**: Clear rules (capacity limits, request lead times, attendance locks) must be enforced consistently
4. **Communication Integration**: System must integrate with email/SMS for notifications (students don't check portal constantly)
5. **Stakeholder Buy-in**: Teachers must register OT availability, students must use make-up system (not informal arrangements)

---

**Document Purpose**: This business context summary provides a foundation for understanding EMS operations without technical details, enabling team members (developers, QA, product managers, stakeholders) to grasp the domain logic, user roles, and core workflows before diving into technical implementation.

**Related Documents**:
- `api-design.md` - Technical API specifications
- `feature-list.md` - Detailed feature requirements by module
- `mainflow-handles.md` - SQL queries and technical implementation details
- `CLAUDE.md` - Technical architecture and development guidelines
