package org.fyp.emssep490be.repositories;

import org.fyp.emssep490be.entities.*;
import org.fyp.emssep490be.entities.enums.*;
import org.fyp.emssep490be.entities.ids.StudentSessionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for StudentRequestRepository
 * Tests complex queries with real database operations
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("StudentRequest Repository Integration Tests")
class StudentRequestRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StudentRequestRepository studentRequestRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private StudentSessionRepository studentSessionRepository;

    @Autowired
    private StudentRepository studentRepository;

    private Student testStudent;
    private SessionEntity targetSession;
    private SessionEntity makeupSession;
    private ClassEntity testClass;
    private Branch testBranch;
    private Center testCenter;
    private CourseSession testCourseSession;
    private Course testCourse;
    private Level testLevel;
    private Subject testSubject;
    private UserAccount studentAccount;
    private UserAccount staffAccount;

    @BeforeEach
    void setUp() {
        // Create center
        testCenter = new Center();
        testCenter.setCode("CTR01");
        testCenter.setName("Test Center");
        testCenter.setPhone("0123456789");
        testCenter.setEmail("test@center.com");
        entityManager.persist(testCenter);

        // Create branch
        testBranch = new Branch();
        testBranch.setCode("BR01");
        testBranch.setName("Test Branch");
        testBranch.setAddress("456 Branch St");
        testBranch.setPhone("0987654321");
        testBranch.setStatus(BranchStatus.ACTIVE);
        testBranch.setCenter(testCenter);
        entityManager.persist(testBranch);

        // Create subject
        testSubject = new Subject();
        testSubject.setName("English");
        testSubject.setCode("ENG");
        testSubject.setDescription("English Language");
        testSubject.setStatus(SubjectStatus.ACTIVE);
        entityManager.persist(testSubject);

        // Create level
        testLevel = new Level();
        testLevel.setCode("A1");
        testLevel.setName("Beginner");
        testLevel.setDescription("Beginner Level");
        testLevel.setSubject(testSubject);
        testLevel.setCreatedAt(OffsetDateTime.now());
        testLevel.setUpdatedAt(OffsetDateTime.now());
        entityManager.persist(testLevel);

        // Create course
        testCourse = new Course();
        testCourse.setCode("ENG-A1-01");
        testCourse.setName("English A1 Course");
        testCourse.setDescription("Beginner English Course");
        testCourse.setDurationWeeks(12);
        testCourse.setSessionPerWeek(2);
        testCourse.setSubject(testSubject); // Set subject (required field)
        testCourse.setLevel(testLevel);
        // approvedByManager is UserAccount, not boolean - set it later if needed
        entityManager.persist(testCourse);

        // Create course session
        testCourseSession = new CourseSession();
        testCourseSession.setSequenceNumber(1);
        testCourseSession.setTopic("Introduction");
        testCourseSession.setStudentTask("Introduction to English");
        testCourseSession.setCreatedAt(OffsetDateTime.now());
        testCourseSession.setUpdatedAt(OffsetDateTime.now());
        
        // Create temporary course phase (required for course session)
        CoursePhase tempPhase = new CoursePhase();
        tempPhase.setPhaseNumber(1);
        tempPhase.setName("Phase 1");
        tempPhase.setCourse(testCourse);
        tempPhase.setCreatedAt(OffsetDateTime.now());
        tempPhase.setUpdatedAt(OffsetDateTime.now());
        entityManager.persist(tempPhase);
        
        testCourseSession.setPhase(tempPhase);
        entityManager.persist(testCourseSession);

        // Create class
        testClass = new ClassEntity();
        testClass.setName("ENG-A1-Morning");
        testClass.setCode("ENG-A1-M");
        testClass.setMaxCapacity(30);
        testClass.setStartDate(LocalDate.now().minusDays(10));
        testClass.setModality(Modality.OFFLINE);
        testClass.setStatus(ClassStatus.ONGOING);
        testClass.setCourse(testCourse);
        testClass.setBranch(testBranch);
        entityManager.persist(testClass);

        // Create student account
        studentAccount = new UserAccount();
        studentAccount.setEmail("student@test.com");
        studentAccount.setPasswordHash("hash");
        studentAccount.setFullName("Test Student");
        studentAccount.setPhone("0123456789");
        entityManager.persist(studentAccount);

        // Create student
        testStudent = new Student();
        testStudent.setStudentCode("STU001");
        testStudent.setUserAccount(studentAccount);
        testStudent.setCreatedAt(OffsetDateTime.now());
        testStudent.setUpdatedAt(OffsetDateTime.now());
        entityManager.persist(testStudent);

        // Create staff account
        staffAccount = new UserAccount();
        staffAccount.setEmail("staff@test.com");
        staffAccount.setPasswordHash("hash");
        staffAccount.setFullName("Test Staff");
        staffAccount.setPhone("0987654321");
        entityManager.persist(staffAccount);

        // Create target session (missed session)
        targetSession = new SessionEntity();
        targetSession.setDate(LocalDate.now().minusDays(3));
        targetSession.setStartTime(LocalTime.of(9, 0));
        targetSession.setEndTime(LocalTime.of(11, 0));
        targetSession.setStatus(SessionStatus.DONE);
        targetSession.setType(SessionType.CLASS);
        targetSession.setClazz(testClass);
        targetSession.setCourseSession(testCourseSession);
        targetSession.setCreatedAt(OffsetDateTime.now());
        targetSession.setUpdatedAt(OffsetDateTime.now());
        entityManager.persist(targetSession);

        // Create makeup session (same course session, different date)
        makeupSession = new SessionEntity();
        makeupSession.setDate(LocalDate.now().plusDays(7));
        makeupSession.setStartTime(LocalTime.of(14, 0));
        makeupSession.setEndTime(LocalTime.of(16, 0));
        makeupSession.setStatus(SessionStatus.PLANNED);
        makeupSession.setType(SessionType.CLASS);
        makeupSession.setClazz(testClass);
        makeupSession.setCourseSession(testCourseSession);
        makeupSession.setCreatedAt(OffsetDateTime.now());
        makeupSession.setUpdatedAt(OffsetDateTime.now());
        entityManager.persist(makeupSession);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should find available makeup sessions with correct filtering")
    void findAvailableMakeupSessions_WithFiltering() {
        // Given: Create student session for target (missed) session
        StudentSession missedSession = new StudentSession();
        missedSession.setId(new StudentSessionId(testStudent.getId(), targetSession.getId()));
        missedSession.setStudent(testStudent);
        missedSession.setSession(targetSession);
        missedSession.setAttendanceStatus(AttendanceStatus.ABSENT);
        studentSessionRepository.save(missedSession);

        entityManager.flush();
        entityManager.clear();

        // When: Search for available makeup sessions
        List<Object[]> results = sessionRepository.findAvailableMakeupSessions(
                testCourseSession.getId(),
                testStudent.getId(),
                null,
                null,
                null,
                null
        );

        // Then: Should find the makeup session
        assertThat(results).isNotEmpty();
        assertThat(results.size()).isGreaterThanOrEqualTo(1);
        
        Object[] firstResult = results.get(0);
        SessionEntity foundSession = (SessionEntity) firstResult[0];
        assertThat(foundSession.getId()).isEqualTo(makeupSession.getId());
        assertThat(foundSession.getCourseSession().getId()).isEqualTo(testCourseSession.getId());
    }

    @Test
    @DisplayName("Should exclude sessions student already enrolled in")
    void findAvailableMakeupSessions_ExcludesEnrolledSessions() {
        // Given: Student is already enrolled in makeup session
        StudentSession enrolledSession = new StudentSession();
        enrolledSession.setId(new StudentSessionId(testStudent.getId(), makeupSession.getId()));
        enrolledSession.setStudent(testStudent);
        enrolledSession.setSession(makeupSession);
        enrolledSession.setAttendanceStatus(AttendanceStatus.PLANNED);
        studentSessionRepository.save(enrolledSession);

        entityManager.flush();
        entityManager.clear();

        // When: Search for available makeup sessions
        List<Object[]> results = sessionRepository.findAvailableMakeupSessions(
                testCourseSession.getId(),
                testStudent.getId(),
                null,
                null,
                null,
                null
        );

        // Then: Should NOT find the makeup session (student already enrolled)
        assertThat(results).allMatch(result -> {
            SessionEntity session = (SessionEntity) result[0];
            return !session.getId().equals(makeupSession.getId());
        });
    }

    @Test
    @DisplayName("Should filter by date range")
    void findAvailableMakeupSessions_FilterByDateRange() {
        // Given: Date range that includes makeup session
        LocalDate dateFrom = LocalDate.now().plusDays(5);
        LocalDate dateTo = LocalDate.now().plusDays(10);

        // When: Search with date filter
        List<Object[]> results = sessionRepository.findAvailableMakeupSessions(
                testCourseSession.getId(),
                testStudent.getId(),
                dateFrom,
                dateTo,
                null,
                null
        );

        // Then: Should find sessions within date range
        assertThat(results).allMatch(result -> {
            SessionEntity session = (SessionEntity) result[0];
            return !session.getDate().isBefore(dateFrom) && !session.getDate().isAfter(dateTo);
        });
    }

    @Test
    @DisplayName("Should check if pending makeup request exists")
    void existsPendingMakeupRequest_FindsDuplicate() {
        // Given: Create pending makeup request
        StudentRequest request = new StudentRequest();
        request.setStudent(testStudent);
        request.setRequestType(StudentRequestType.MAKEUP);
        request.setStatus(RequestStatus.PENDING);
        request.setTargetSession(targetSession);
        request.setMakeupSession(makeupSession);
        request.setNote("Need to makeup missed class");
        request.setSubmittedBy(studentAccount);
        request.setSubmittedAt(OffsetDateTime.now());
        request.setCreatedAt(OffsetDateTime.now());
        request.setUpdatedAt(OffsetDateTime.now());
        studentRequestRepository.save(request);

        entityManager.flush();
        entityManager.clear();

        // When: Check for duplicate
        boolean exists = studentRequestRepository.existsPendingMakeupRequest(
                testStudent.getId(),
                targetSession.getId(),
                makeupSession.getId()
        );

        // Then: Should find the pending request
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should not find duplicate if request is approved")
    void existsPendingMakeupRequest_IgnoresApprovedRequests() {
        // Given: Create approved makeup request
        StudentRequest request = new StudentRequest();
        request.setStudent(testStudent);
        request.setRequestType(StudentRequestType.MAKEUP);
        request.setStatus(RequestStatus.APPROVED); // Not pending
        request.setTargetSession(targetSession);
        request.setMakeupSession(makeupSession);
        request.setNote("Need to makeup missed class");
        request.setSubmittedBy(studentAccount);
        request.setSubmittedAt(OffsetDateTime.now());
        request.setDecidedBy(staffAccount);
        request.setDecidedAt(OffsetDateTime.now());
        request.setCreatedAt(OffsetDateTime.now());
        request.setUpdatedAt(OffsetDateTime.now());
        studentRequestRepository.save(request);

        entityManager.flush();
        entityManager.clear();

        // When: Check for duplicate
        boolean exists = studentRequestRepository.existsPendingMakeupRequest(
                testStudent.getId(),
                targetSession.getId(),
                makeupSession.getId()
        );

        // Then: Should NOT find (approved, not pending)
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should check if pending absence request exists")
    void existsPendingAbsenceRequestForSession_FindsDuplicate() {
        // Given: Create pending absence request
        StudentRequest request = new StudentRequest();
        request.setStudent(testStudent);
        request.setRequestType(StudentRequestType.ABSENCE);
        request.setStatus(RequestStatus.PENDING);
        request.setTargetSession(targetSession);
        request.setNote("I am sick");
        request.setSubmittedBy(studentAccount);
        request.setSubmittedAt(OffsetDateTime.now());
        request.setCreatedAt(OffsetDateTime.now());
        request.setUpdatedAt(OffsetDateTime.now());
        studentRequestRepository.save(request);

        entityManager.flush();
        entityManager.clear();

        // When: Check for duplicate
        boolean exists = studentRequestRepository.existsPendingAbsenceRequestForSession(
                testStudent.getId(),
                targetSession.getId()
        );

        // Then: Should find the pending request
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should count approved absence requests in class")
    void countApprovedAbsenceRequestsInClass_ReturnsCorrectCount() {
        // Given: Create 2 approved absence requests for different sessions in same class
        SessionEntity session2 = new SessionEntity();
        session2.setDate(LocalDate.now().plusDays(1));
        session2.setStartTime(LocalTime.of(9, 0));
        session2.setEndTime(LocalTime.of(11, 0));
        session2.setStatus(SessionStatus.PLANNED);
        session2.setType(SessionType.CLASS);
        session2.setClazz(testClass);
        session2.setCourseSession(testCourseSession);
        session2.setCreatedAt(OffsetDateTime.now());
        session2.setUpdatedAt(OffsetDateTime.now());
        entityManager.persist(session2);

        StudentRequest request1 = new StudentRequest();
        request1.setStudent(testStudent);
        request1.setRequestType(StudentRequestType.ABSENCE);
        request1.setStatus(RequestStatus.APPROVED);
        request1.setTargetSession(targetSession);
        request1.setNote("Sick");
        request1.setSubmittedBy(studentAccount);
        request1.setSubmittedAt(OffsetDateTime.now());
        request1.setDecidedBy(staffAccount);
        request1.setDecidedAt(OffsetDateTime.now());
        request1.setCreatedAt(OffsetDateTime.now());
        request1.setUpdatedAt(OffsetDateTime.now());
        studentRequestRepository.save(request1);

        StudentRequest request2 = new StudentRequest();
        request2.setStudent(testStudent);
        request2.setRequestType(StudentRequestType.ABSENCE);
        request2.setStatus(RequestStatus.APPROVED);
        request2.setTargetSession(session2);
        request2.setNote("Emergency");
        request2.setSubmittedBy(studentAccount);
        request2.setSubmittedAt(OffsetDateTime.now());
        request2.setDecidedBy(staffAccount);
        request2.setDecidedAt(OffsetDateTime.now());
        request2.setCreatedAt(OffsetDateTime.now());
        request2.setUpdatedAt(OffsetDateTime.now());
        studentRequestRepository.save(request2);

        entityManager.flush();
        entityManager.clear();

        // When: Count approved requests
        long count = studentRequestRepository.countApprovedAbsenceRequestsInClass(
                testStudent.getId(),
                testClass.getId()
        );

        // Then: Should return 2
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should only count approved requests, not pending or rejected")
    void countApprovedAbsenceRequestsInClass_OnlyCountsApproved() {
        // Given: Create approved, pending, and rejected requests
        StudentRequest approved = new StudentRequest();
        approved.setStudent(testStudent);
        approved.setRequestType(StudentRequestType.ABSENCE);
        approved.setStatus(RequestStatus.APPROVED);
        approved.setTargetSession(targetSession);
        approved.setNote("Sick");
        approved.setSubmittedBy(studentAccount);
        approved.setSubmittedAt(OffsetDateTime.now());
        approved.setDecidedBy(staffAccount);
        approved.setDecidedAt(OffsetDateTime.now());
        approved.setCreatedAt(OffsetDateTime.now());
        approved.setUpdatedAt(OffsetDateTime.now());
        studentRequestRepository.save(approved);

        SessionEntity session2 = new SessionEntity();
        session2.setDate(LocalDate.now().plusDays(2));
        session2.setStartTime(LocalTime.of(9, 0));
        session2.setEndTime(LocalTime.of(11, 0));
        session2.setStatus(SessionStatus.PLANNED);
        session2.setType(SessionType.CLASS);
        session2.setClazz(testClass);
        session2.setCourseSession(testCourseSession);
        session2.setCreatedAt(OffsetDateTime.now());
        session2.setUpdatedAt(OffsetDateTime.now());
        entityManager.persist(session2);

        StudentRequest pending = new StudentRequest();
        pending.setStudent(testStudent);
        pending.setRequestType(StudentRequestType.ABSENCE);
        pending.setStatus(RequestStatus.PENDING);
        pending.setTargetSession(session2);
        pending.setNote("Pending reason");
        pending.setSubmittedBy(studentAccount);
        pending.setSubmittedAt(OffsetDateTime.now());
        pending.setCreatedAt(OffsetDateTime.now());
        pending.setUpdatedAt(OffsetDateTime.now());
        studentRequestRepository.save(pending);

        entityManager.flush();
        entityManager.clear();

        // When: Count approved requests
        long count = studentRequestRepository.countApprovedAbsenceRequestsInClass(
                testStudent.getId(),
                testClass.getId()
        );

        // Then: Should return 1 (only approved)
        assertThat(count).isEqualTo(1);
    }
}
