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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for StudentSessionRepository
 * Tests schedule conflict detection and makeup session counting
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("StudentSession Repository Integration Tests")
class StudentSessionRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StudentSessionRepository studentSessionRepository;

    private Student testStudent;
    private SessionEntity conflictingSession;
    private SessionEntity nonConflictingSession;
    private SessionEntity makeupSession1;
    private SessionEntity makeupSession2;
    private ClassEntity testClass;
    private Branch testBranch;
    private Center testCenter;
    private CourseSession testCourseSession;
    private Course testCourse;
    private Level testLevel;
    private Subject testSubject;
    private UserAccount studentAccount;

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

        // Create sessions
        LocalDate targetDate = LocalDate.now().plusDays(7);

        // Conflicting session: 9:00-11:00
        conflictingSession = new SessionEntity();
        conflictingSession.setDate(targetDate);
        conflictingSession.setStartTime(LocalTime.of(9, 0));
        conflictingSession.setEndTime(LocalTime.of(11, 0));
        conflictingSession.setStatus(SessionStatus.PLANNED);
        conflictingSession.setType(SessionType.CLASS);
        conflictingSession.setClazz(testClass);
        conflictingSession.setCourseSession(testCourseSession);
        conflictingSession.setCreatedAt(OffsetDateTime.now());
        conflictingSession.setUpdatedAt(OffsetDateTime.now());
        entityManager.persist(conflictingSession);

        // Non-conflicting session: 14:00-16:00 (same day, no overlap)
        nonConflictingSession = new SessionEntity();
        nonConflictingSession.setDate(targetDate);
        nonConflictingSession.setStartTime(LocalTime.of(14, 0));
        nonConflictingSession.setEndTime(LocalTime.of(16, 0));
        nonConflictingSession.setStatus(SessionStatus.PLANNED);
        nonConflictingSession.setType(SessionType.CLASS);
        nonConflictingSession.setClazz(testClass);
        nonConflictingSession.setCourseSession(testCourseSession);
        nonConflictingSession.setCreatedAt(OffsetDateTime.now());
        nonConflictingSession.setUpdatedAt(OffsetDateTime.now());
        entityManager.persist(nonConflictingSession);

        // Makeup sessions
        makeupSession1 = new SessionEntity();
        makeupSession1.setDate(LocalDate.now().plusDays(10));
        makeupSession1.setStartTime(LocalTime.of(10, 0));
        makeupSession1.setEndTime(LocalTime.of(12, 0));
        makeupSession1.setStatus(SessionStatus.PLANNED);
        makeupSession1.setType(SessionType.MAKEUP);
        makeupSession1.setClazz(testClass);
        makeupSession1.setCourseSession(testCourseSession);
        makeupSession1.setCreatedAt(OffsetDateTime.now());
        makeupSession1.setUpdatedAt(OffsetDateTime.now());
        entityManager.persist(makeupSession1);

        makeupSession2 = new SessionEntity();
        makeupSession2.setDate(LocalDate.now().plusDays(15));
        makeupSession2.setStartTime(LocalTime.of(10, 0));
        makeupSession2.setEndTime(LocalTime.of(12, 0));
        makeupSession2.setStatus(SessionStatus.PLANNED);
        makeupSession2.setType(SessionType.MAKEUP);
        makeupSession2.setClazz(testClass);
        makeupSession2.setCourseSession(testCourseSession);
        makeupSession2.setCreatedAt(OffsetDateTime.now());
        makeupSession2.setUpdatedAt(OffsetDateTime.now());
        entityManager.persist(makeupSession2);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should detect overlapping time schedule conflict")
    void countScheduleConflicts_DetectsOverlap() {
        // Given: Student enrolled in session 9:00-11:00
        StudentSession existingSession = new StudentSession();
        existingSession.setId(new StudentSessionId(testStudent.getId(), conflictingSession.getId()));
        existingSession.setStudent(testStudent);
        existingSession.setSession(conflictingSession);
        existingSession.setAttendanceStatus(AttendanceStatus.PLANNED);
        studentSessionRepository.save(existingSession);

        entityManager.flush();
        entityManager.clear();

        // When: Check for conflict with 10:00-12:00 (overlaps with 9:00-11:00)
        long conflicts = studentSessionRepository.countScheduleConflicts(
                testStudent.getId(),
                conflictingSession.getDate(),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0)
        );

        // Then: Should detect conflict
        assertThat(conflicts).isEqualTo(1);
    }

    @Test
    @DisplayName("Should NOT detect conflict for non-overlapping times on same day")
    void countScheduleConflicts_NoConflictDifferentTime() {
        // Given: Student enrolled in session 9:00-11:00
        StudentSession existingSession = new StudentSession();
        existingSession.setId(new StudentSessionId(testStudent.getId(), conflictingSession.getId()));
        existingSession.setStudent(testStudent);
        existingSession.setSession(conflictingSession);
        existingSession.setAttendanceStatus(AttendanceStatus.PLANNED);
        studentSessionRepository.save(existingSession);

        entityManager.flush();
        entityManager.clear();

        // When: Check for conflict with 14:00-16:00 (no overlap)
        long conflicts = studentSessionRepository.countScheduleConflicts(
                testStudent.getId(),
                conflictingSession.getDate(),
                LocalTime.of(14, 0),
                LocalTime.of(16, 0)
        );

        // Then: Should NOT detect conflict
        assertThat(conflicts).isZero();
    }

    @Test
    @DisplayName("Should NOT count EXCUSED sessions in conflict check")
    void countScheduleConflicts_ExcludesExcusedSessions() {
        // Given: Student enrolled in session but EXCUSED
        StudentSession excusedSession = new StudentSession();
        excusedSession.setId(new StudentSessionId(testStudent.getId(), conflictingSession.getId()));
        excusedSession.setStudent(testStudent);
        excusedSession.setSession(conflictingSession);
        excusedSession.setAttendanceStatus(AttendanceStatus.EXCUSED);
        studentSessionRepository.save(excusedSession);

        entityManager.flush();
        entityManager.clear();

        // When: Check for conflict with overlapping time
        long conflicts = studentSessionRepository.countScheduleConflicts(
                testStudent.getId(),
                conflictingSession.getDate(),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0)
        );

        // Then: Should NOT detect conflict (excused sessions don't count)
        assertThat(conflicts).isZero();
    }

    @Test
    @DisplayName("Should detect edge case - same start time")
    void countScheduleConflicts_SameStartTime() {
        // Given: Student enrolled in session 9:00-11:00
        StudentSession existingSession = new StudentSession();
        existingSession.setId(new StudentSessionId(testStudent.getId(), conflictingSession.getId()));
        existingSession.setStudent(testStudent);
        existingSession.setSession(conflictingSession);
        existingSession.setAttendanceStatus(AttendanceStatus.PLANNED);
        studentSessionRepository.save(existingSession);

        entityManager.flush();
        entityManager.clear();

        // When: Check for conflict with 9:00-10:00 (same start time)
        long conflicts = studentSessionRepository.countScheduleConflicts(
                testStudent.getId(),
                conflictingSession.getDate(),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0)
        );

        // Then: Should detect conflict
        assertThat(conflicts).isEqualTo(1);
    }

    @Test
    @DisplayName("Should detect edge case - same end time")
    void countScheduleConflicts_SameEndTime() {
        // Given: Student enrolled in session 9:00-11:00
        StudentSession existingSession = new StudentSession();
        existingSession.setId(new StudentSessionId(testStudent.getId(), conflictingSession.getId()));
        existingSession.setStudent(testStudent);
        existingSession.setSession(conflictingSession);
        existingSession.setAttendanceStatus(AttendanceStatus.PLANNED);
        studentSessionRepository.save(existingSession);

        entityManager.flush();
        entityManager.clear();

        // When: Check for conflict with 10:00-11:00 (same end time)
        long conflicts = studentSessionRepository.countScheduleConflicts(
                testStudent.getId(),
                conflictingSession.getDate(),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        // Then: Should detect conflict
        assertThat(conflicts).isEqualTo(1);
    }

    @Test
    @DisplayName("Should count makeup sessions correctly")
    void countMakeupSessions_ReturnsCorrectCount() {
        // Given: Student has 2 makeup sessions
        StudentSession makeup1 = new StudentSession();
        makeup1.setId(new StudentSessionId(testStudent.getId(), makeupSession1.getId()));
        makeup1.setStudent(testStudent);
        makeup1.setSession(makeupSession1);
        makeup1.setAttendanceStatus(AttendanceStatus.PLANNED);
        makeup1.setIsMakeup(true); // MAKEUP FLAG
        studentSessionRepository.save(makeup1);

        StudentSession makeup2 = new StudentSession();
        makeup2.setId(new StudentSessionId(testStudent.getId(), makeupSession2.getId()));
        makeup2.setStudent(testStudent);
        makeup2.setSession(makeupSession2);
        makeup2.setAttendanceStatus(AttendanceStatus.PLANNED);
        makeup2.setIsMakeup(true); // MAKEUP FLAG
        studentSessionRepository.save(makeup2);

        // Regular session (not makeup)
        StudentSession regular = new StudentSession();
        regular.setId(new StudentSessionId(testStudent.getId(), conflictingSession.getId()));
        regular.setStudent(testStudent);
        regular.setSession(conflictingSession);
        regular.setAttendanceStatus(AttendanceStatus.PLANNED);
        regular.setIsMakeup(false);
        studentSessionRepository.save(regular);

        entityManager.flush();
        entityManager.clear();

        // When: Count makeup sessions
        long count = studentSessionRepository.countMakeupSessions(testStudent.getId(), null);

        // Then: Should return 2
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should filter makeup sessions by class")
    void countMakeupSessions_FilterByClass() {
        // Given: Student has makeup sessions in testClass
        StudentSession makeup1 = new StudentSession();
        makeup1.setId(new StudentSessionId(testStudent.getId(), makeupSession1.getId()));
        makeup1.setStudent(testStudent);
        makeup1.setSession(makeupSession1);
        makeup1.setAttendanceStatus(AttendanceStatus.PLANNED);
        makeup1.setIsMakeup(true);
        studentSessionRepository.save(makeup1);

        entityManager.flush();
        entityManager.clear();

        // When: Count makeup sessions for specific class
        long count = studentSessionRepository.countMakeupSessions(testStudent.getId(), testClass.getId());

        // Then: Should return 1
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Should NOT count non-makeup sessions")
    void countMakeupSessions_OnlyCountsMakeups() {
        // Given: Student has only regular sessions (isMakeup = false)
        StudentSession regular = new StudentSession();
        regular.setId(new StudentSessionId(testStudent.getId(), conflictingSession.getId()));
        regular.setStudent(testStudent);
        regular.setSession(conflictingSession);
        regular.setAttendanceStatus(AttendanceStatus.PLANNED);
        regular.setIsMakeup(false); // NOT a makeup
        studentSessionRepository.save(regular);

        entityManager.flush();
        entityManager.clear();

        // When: Count makeup sessions
        long count = studentSessionRepository.countMakeupSessions(testStudent.getId(), null);

        // Then: Should return 0
        assertThat(count).isZero();
    }
}
