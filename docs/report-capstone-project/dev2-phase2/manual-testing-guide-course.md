# 🧪 MANUAL TESTING GUIDE - COURSE MANAGEMENT
## Phase 2: Course, CoursePhase, and CourseSession

**Purpose**: Step-by-step guide for manual testing via Swagger UI
**Developer**: DEV 2 - Academic Curriculum Lead
**Last Updated**: 2025-10-22

---

## 📋 PRE-REQUISITES

### 1. Start the Application
```bash
# Option 1: Using Docker
docker-compose up -d

# Option 2: Using Maven
./mvnw spring-boot:run

# Wait for application to start
# Check health: http://localhost:8080/actuator/health
```

### 2. Access Swagger UI
Open browser: `http://localhost:8080/swagger-ui.html`

### 3. Authenticate
You need a valid JWT token with appropriate roles:
- **SUBJECT_LEADER**: For course creation/editing
- **MANAGER** or **CENTER_HEAD**: For approval

**Get Token**:
```bash
POST /api/v1/auth/login
Body:
{
  "username": "subject_leader_user",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGc..."  # Copy this token
}
```

**Add Token to Swagger**:
1. Click "Authorize" button (top right)
2. Enter: `Bearer eyJhbGc...`
3. Click "Authorize"

### 4. Required Data
You need existing:
- **Subject** (e.g., id=1, code="ENG")
- **Level** (e.g., id=1, code="A1")

---

## 🎯 TEST SCENARIOS

## Scenario 1: Complete Course Creation Flow ✅

### Step 1.1: Create a Course
**Endpoint**: `POST /api/v1/courses`
**Role Required**: SUBJECT_LEADER

**Request Body**:
```json
{
  "subjectId": 1,
  "levelId": 1,
  "code": "ENG-A1-V1",
  "name": "English Level A1 - Beginner",
  "version": 1,
  "description": "Comprehensive English course for absolute beginners covering basic grammar, vocabulary, and conversation skills.",
  "totalHours": 120,
  "durationWeeks": 12,
  "sessionPerWeek": 3,
  "hoursPerSession": 3.33,
  "prerequisites": "No prerequisites required",
  "targetAudience": "Complete beginners with no prior English knowledge",
  "teachingMethods": "Interactive lessons, group activities, multimedia content",
  "status": "draft"
}
```

**Expected Response** (201 Created):
```json
{
  "status": 200,
  "message": "Course created successfully",
  "data": {
    "id": 1,
    "code": "ENG-A1-V1",
    "name": "English Level A1 - Beginner",
    "status": "draft",
    "approved": false,
    "phasesCount": 0,
    "createdAt": "2025-10-22T...",
    ...
  }
}
```

✅ **Verification**:
- Check `status = "draft"`
- Check `approved = false`
- Check `phasesCount = 0`
- Note the returned `id` (e.g., 1)

---

### Step 1.2: Try to Submit Without Phases (Should Fail)
**Endpoint**: `POST /api/v1/courses/{id}/submit`
**Course ID**: Use ID from Step 1.1

**Expected Response** (400 Bad Request):
```json
{
  "status": 1248,
  "message": "Course must have at least one phase before submission",
  "data": null
}
```

✅ **Verification**: Error code = 1248 (COURSE_NO_PHASES)

---

### Step 1.3: Add Phase 1
**Endpoint**: `POST /api/v1/courses/{courseId}/phases`
**Course ID**: 1

**Request Body**:
```json
{
  "phaseNumber": 1,
  "name": "Phase 1 - Foundation",
  "durationWeeks": 4,
  "learningFocus": "Basic greetings, alphabet, numbers 1-100, simple present tense",
  "sortOrder": 1
}
```

**Expected Response** (201 Created):
```json
{
  "status": 201,
  "message": "Phase created successfully",
  "data": {
    "id": 1,
    "courseId": 1,
    "phaseNumber": 1,
    "name": "Phase 1 - Foundation",
    "durationWeeks": 4,
    "sortOrder": 1,
    "sessionsCount": 0,
    ...
  }
}
```

✅ **Verification**: Phase created with sessionsCount = 0

---

### Step 1.4: Add Phase 2
**Endpoint**: `POST /api/v1/courses/{courseId}/phases`

**Request Body**:
```json
{
  "phaseNumber": 2,
  "name": "Phase 2 - Building Skills",
  "durationWeeks": 4,
  "learningFocus": "Past tense, daily routines, describing people and places",
  "sortOrder": 2
}
```

✅ **Verification**: Second phase created

---

### Step 1.5: Add Phase 3
**Request Body**:
```json
{
  "phaseNumber": 3,
  "name": "Phase 3 - Communication",
  "durationWeeks": 4,
  "learningFocus": "Future tense, making plans, asking for directions, shopping conversations",
  "sortOrder": 3
}
```

---

### Step 1.6: Add Sessions to Phase 1
**Endpoint**: `POST /api/v1/phases/{phaseId}/sessions`
**Phase ID**: 1

**Session 1**:
```json
{
  "sequenceNo": 1,
  "topic": "Introduction & Greetings",
  "studentTask": "Practice greetings with classmates, complete Workbook pages 1-5",
  "skillSet": ["GENERAL", "SPEAKING", "LISTENING"]
}
```

**Session 2**:
```json
{
  "sequenceNo": 2,
  "topic": "The Alphabet & Pronunciation",
  "studentTask": "Memorize alphabet, practice pronunciation exercises, record audio",
  "skillSet": ["SPEAKING", "LISTENING"]
}
```

**Session 3**:
```json
{
  "sequenceNo": 3,
  "topic": "Numbers 1-100",
  "studentTask": "Practice counting, complete number exercises pages 10-12",
  "skillSet": ["GENERAL", "LISTENING"]
}
```

**Session 4**:
```json
{
  "sequenceNo": 4,
  "topic": "Simple Present Tense - Be",
  "studentTask": "Complete grammar exercises, write 5 sentences using 'be' verb",
  "skillSet": ["WRITING", "GENERAL"]
}
```

✅ **Verification**: 4 sessions created for Phase 1

---

### Step 1.7: Add Sessions to Phase 2
**Phase ID**: 2

Add at least 2-3 sessions following the same pattern.

---

### Step 1.8: Verify Course Details
**Endpoint**: `GET /api/v1/courses/{id}`

**Expected Response**:
```json
{
  "status": 200,
  "message": "Course retrieved successfully",
  "data": {
    "id": 1,
    "code": "ENG-A1-V1",
    "name": "English Level A1 - Beginner",
    "status": "draft",
    "approved": false,
    "phases": [
      {
        "id": 1,
        "phaseNumber": 1,
        "name": "Phase 1 - Foundation",
        "sessionsCount": 4
      },
      {
        "id": 2,
        "phaseNumber": 2,
        "sessionsCount": 3  # If you added 3
      },
      {
        "id": 3,
        "phaseNumber": 3,
        "sessionsCount": 0
      }
    ],
    ...
  }
}
```

✅ **Verification**:
- 3 phases present
- Sessions count correct for each phase

---

### Step 1.9: Submit for Approval (Should Succeed Now)
**Endpoint**: `POST /api/v1/courses/{id}/submit`

**Expected Response** (200 OK):
```json
{
  "status": 200,
  "message": "Course submitted for approval successfully",
  "data": {
    "id": 1,
    "status": "draft",  # Still draft, waiting approval
    "submittedAt": "2025-10-22T...",  # Now has timestamp
    ...
  }
}
```

✅ **Verification**: `submittedAt` field is now populated

---

### Step 1.10: Logout and Login as Manager
Get new token with MANAGER or CENTER_HEAD role:
```bash
POST /api/v1/auth/login
Body:
{
  "username": "manager_user",
  "password": "password123"
}
```

Update authorization in Swagger with new token.

---

### Step 1.11: Approve the Course
**Endpoint**: `POST /api/v1/courses/{id}/approve`
**Role Required**: MANAGER or CENTER_HEAD

**Request Body**:
```json
{
  "action": "approve",
  "rejectionReason": null
}
```

**Expected Response** (200 OK):
```json
{
  "status": 200,
  "message": "Course approval processed successfully",
  "data": {
    "id": 1,
    "status": "active",  # Changed to active!
    "approved": true,
    "approvedByManager": {
      "id": 2,
      "username": "manager_user",
      ...
    },
    "approvedAt": "2025-10-22T...",
    "rejectionReason": null,
    ...
  }
}
```

✅ **Verification**:
- `status = "active"`
- `approved = true`
- `approvedByManager` is populated
- `approvedAt` has timestamp

---

## Scenario 2: Rejection and Resubmission Flow ✅

### Step 2.1: Create Another Course
Follow Step 1.1, use different code (e.g., "ENG-A2-V1")

### Step 2.2: Add Phases and Sessions
Add at least 1 phase with 1 session

### Step 2.3: Submit for Approval
As SUBJECT_LEADER

### Step 2.4: Reject the Course
As MANAGER:

**Endpoint**: `POST /api/v1/courses/{id}/approve`

**Request Body**:
```json
{
  "action": "reject",
  "rejectionReason": "The course content needs more detail in the learning objectives. Please expand Phase 1 description and add more sessions."
}
```

**Expected Response**:
```json
{
  "status": 200,
  "message": "Course approval processed successfully",
  "data": {
    "id": 2,
    "status": "draft",  # Back to draft!
    "approved": false,
    "rejectionReason": "The course content needs more detail...",
    "approvedByManager": null,  # Cleared
    "approvedAt": null,  # Cleared
    ...
  }
}
```

✅ **Verification**:
- Status reverted to "draft"
- Rejection reason saved
- Approval fields cleared

---

### Step 2.5: Edit the Rejected Course
Login as SUBJECT_LEADER again

**Endpoint**: `PUT /api/v1/courses/{id}`

**Request Body**:
```json
{
  "name": "English Level A2 - Elementary (Revised)",
  "description": "Updated description with more detailed learning objectives...",
  "status": "draft"
}
```

**Expected Response**: Course updated successfully

✅ **Verification**: Can edit draft course

---

### Step 2.6: Add More Content
Add another phase or more sessions

---

### Step 2.7: Resubmit
**Endpoint**: `POST /api/v1/courses/{id}/submit`

✅ **Verification**: Can resubmit after rejection and edits

---

### Step 2.8: Approve This Time
As MANAGER, approve with action="approve"

✅ **Verification**: Now approved and active

---

## Scenario 3: Validation Testing ❌ (Should Fail)

### Test 3.1: Duplicate Course Code
**Endpoint**: `POST /api/v1/courses`

Try to create course with existing code (e.g., "ENG-A1-V1")

**Expected Error** (400 Bad Request):
```json
{
  "status": 1242,
  "message": "Course code already exists",
  "data": null
}
```

✅ **Verification**: Error code = 1242

---

### Test 3.2: Invalid Total Hours
**Endpoint**: `POST /api/v1/courses`

**Request Body**:
```json
{
  "subjectId": 1,
  "levelId": 1,
  "code": "ENG-B1-V1",
  "totalHours": 200,  # Wrong!
  "durationWeeks": 12,
  "sessionPerWeek": 3,
  "hoursPerSession": 3.33  # Calculated: 12*3*3.33 = ~120
}
```

**Expected Error** (400 Bad Request):
```json
{
  "status": 1251,
  "message": "Total hours calculation is inconsistent (tolerance: 10%)",
  "data": null
}
```

✅ **Verification**: Error code = 1251

---

### Test 3.3: Update Approved Course (Should Fail)
Try to update the course from Step 1.11 (which is approved)

**Endpoint**: `PUT /api/v1/courses/{id}`

**Expected Error**:
```json
{
  "status": 1243,
  "message": "Course cannot be updated (must be in draft or rejected status)",
  "data": null
}
```

✅ **Verification**: Error code = 1243

---

### Test 3.4: Duplicate Phase Number
Add phase with existing phaseNumber

**Endpoint**: `POST /api/v1/courses/{courseId}/phases`

**Request Body**:
```json
{
  "phaseNumber": 1,  # Already exists!
  "name": "Duplicate Phase"
}
```

**Expected Error**:
```json
{
  "status": 1271,
  "message": "Phase number already exists for this course",
  "data": null
}
```

---

### Test 3.5: Invalid Skill Set
**Endpoint**: `POST /api/v1/phases/{phaseId}/sessions`

**Request Body**:
```json
{
  "sequenceNo": 10,
  "topic": "Test Session",
  "skillSet": ["INVALID_SKILL", "GENERAL"]  # Invalid!
}
```

**Expected Error**:
```json
{
  "status": 1293,
  "message": "Invalid skill set value(s)",
  "data": null
}
```

---

### Test 3.6: Delete Phase with Sessions
Try to delete Phase 1 (which has 4 sessions)

**Endpoint**: `DELETE /api/v1/phases/{id}`

**Expected Error**:
```json
{
  "status": 1272,
  "message": "Cannot delete phase that has course sessions",
  "data": null
}
```

✅ **Verification**: Must delete sessions first

---

## Scenario 4: CRUD Operations ✅

### Test 4.1: List All Courses
**Endpoint**: `GET /api/v1/courses`

**Query Parameters**:
- page=1
- limit=10

**Expected Response**:
```json
{
  "status": 200,
  "message": "Courses retrieved successfully",
  "data": {
    "data": [ /* array of courses */ ],
    "currentPage": 1,
    "totalItems": 2,
    "totalPages": 1
  }
}
```

---

### Test 4.2: Filter Courses
**Endpoint**: `GET /api/v1/courses?subjectId=1&status=active`

**Expected Response**: Only active courses for subject 1

---

### Test 4.3: Get Course Details
**Endpoint**: `GET /api/v1/courses/{id}`

Verify detailed response with phases

---

### Test 4.4: List Phases for Course
**Endpoint**: `GET /api/v1/courses/{courseId}/phases`

**Expected Response**: Array of phases ordered by sortOrder

---

### Test 4.5: List Sessions for Phase
**Endpoint**: `GET /api/v1/phases/{phaseId}/sessions`

**Expected Response**: Array of sessions ordered by sequenceNo

---

### Test 4.6: Update Phase
**Endpoint**: `PUT /api/v1/phases/{id}`

**Request Body**:
```json
{
  "name": "Phase 1 - Foundation (Updated)",
  "learningFocus": "Updated learning focus description"
}
```

✅ **Verification**: Only works on draft courses

---

### Test 4.7: Update Session
**Endpoint**: `PUT /api/v1/sessions/{id}`

**Request Body**:
```json
{
  "topic": "Updated Topic",
  "skillSet": ["READING", "WRITING"]
}
```

✅ **Verification**: Skill set updated

---

### Test 4.8: Delete Session
**Endpoint**: `DELETE /api/v1/sessions/{id}`

Delete one session (that's not in use)

**Expected Response**:
```json
{
  "status": 200,
  "message": "Session deleted successfully",
  "data": null
}
```

---

### Test 4.9: Delete Phase
First delete all sessions in the phase, then:

**Endpoint**: `DELETE /api/v1/phases/{id}`

**Expected Response**: Phase deleted

---

### Test 4.10: Soft Delete Course
**Endpoint**: `DELETE /api/v1/courses/{id}`

**Expected Response**:
```json
{
  "status": 200,
  "message": "Course deleted successfully",
  "data": null
}
```

✅ **Verification**:
- Course status set to "inactive"
- Still in database (soft delete)

---

## Scenario 5: Authorization Testing 🔐

### Test 5.1: Create Course as Non-Subject Leader
Login as TEACHER or STUDENT

Try: `POST /api/v1/courses`

**Expected Error** (403 Forbidden):
```json
{
  "error": "Forbidden",
  "message": "Access Denied"
}
```

---

### Test 5.2: Approve as Non-Manager
Login as SUBJECT_LEADER

Try: `POST /api/v1/courses/{id}/approve`

**Expected Error** (403 Forbidden)

---

### Test 5.3: Verify Read Access
Any authenticated user with appropriate roles can read:
- GET /api/v1/courses ✅
- GET /api/v1/courses/{id} ✅

---

## 📊 TEST COVERAGE CHECKLIST

### Course Management
- [x] Create course with valid data
- [x] Create course with duplicate code (fail)
- [x] Create course with invalid total hours (fail)
- [x] Update draft course
- [x] Update approved course (fail)
- [x] Submit course without phases (fail)
- [x] Submit course with phases (success)
- [x] Approve course
- [x] Reject course with reason
- [x] Resubmit after rejection
- [x] Delete course
- [x] List courses with pagination
- [x] Filter courses by subject/level/status

### Phase Management
- [x] Create phase for draft course
- [x] Create phase with duplicate number (fail)
- [x] Update phase
- [x] Delete phase without sessions
- [x] Delete phase with sessions (fail)
- [x] List phases for course

### Session Management
- [x] Create session with valid skills
- [x] Create session with invalid skills (fail)
- [x] Create session with duplicate sequence (fail)
- [x] Update session
- [x] Delete session
- [x] List sessions for phase

### Authorization
- [x] SUBJECT_LEADER can create/edit
- [x] MANAGER can approve/reject
- [x] Others cannot create (403)
- [x] Others cannot approve (403)

---

## 🐛 TROUBLESHOOTING

### Issue: 401 Unauthorized
**Solution**: Token expired or invalid. Get new token and re-authorize.

### Issue: 403 Forbidden
**Solution**: User doesn't have required role. Check token payload.

### Issue: 404 Not Found
**Solution**: Entity doesn't exist. Check ID is correct.

### Issue: 400 Bad Request with ErrorCode
**Solution**: Check error message for specific validation failure.

### Issue: 500 Internal Server Error
**Solution**: Check application logs. Likely database connection issue.

---

## 📋 QUICK REFERENCE

### Valid Skill Set Values
- GENERAL
- READING
- WRITING
- SPEAKING
- LISTENING

### Course Status Values
- draft
- active
- inactive

### Approval Actions
- approve
- reject (requires rejectionReason)

### Required Roles
- Create/Update/Delete: SUBJECT_LEADER
- Approve/Reject: MANAGER, CENTER_HEAD
- Read: All authenticated users

---

**Testing Checklist Complete**: All scenarios covered ✅

**Document Prepared By**: DEV 2 - Academic Curriculum Lead
**Last Updated**: 2025-10-22
**Version**: 1.0
