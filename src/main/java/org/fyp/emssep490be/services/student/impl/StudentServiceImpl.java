package org.fyp.emssep490be.services.student.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.student.*;
import org.fyp.emssep490be.entities.*;
import org.fyp.emssep490be.entities.enums.AttendanceStatus;
import org.fyp.emssep490be.entities.enums.EnrollmentStatus;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.*;
import org.fyp.emssep490be.services.student.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final UserAccountRepository userAccountRepository;
    private final BranchRepository branchRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentSessionRepository studentSessionRepository;
    private final TeachingSlotRepository teachingSlotRepository;
    private final SessionResourceRepository sessionResourceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public StudentDTO createStudent(CreateStudentRequestDTO request) {
        log.info("Creating student with email: {}", request.getEmail());

        // Validate branch exists
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new CustomException(ErrorCode.BRANCH_NOT_FOUND));

        // Check if email already exists
        if (userAccountRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // Check if phone already exists
        if (userAccountRepository.existsByPhone(request.getPhone())) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // Create UserAccount
        UserAccount userAccount = new UserAccount();
        userAccount.setEmail(request.getEmail());
        userAccount.setPhone(request.getPhone());
        userAccount.setFullName(request.getFullName());
        userAccount.setPasswordHash(passwordEncoder.encode("123456")); // Default password
        userAccount.setStatus("active");
        userAccount.setCreatedAt(OffsetDateTime.now());
        userAccount.setUpdatedAt(OffsetDateTime.now());
        
        UserAccount savedUser = userAccountRepository.save(userAccount);
        log.info("Created user account with ID: {}", savedUser.getId());

        // Create Student
        Student student = new Student();
        student.setUserAccount(savedUser);
        student.setStudentCode(generateStudentCode(branch));
        student.setBranch(branch);
        student.setCreatedAt(OffsetDateTime.now());
        student.setUpdatedAt(OffsetDateTime.now());
        
        Student savedStudent = studentRepository.save(student);
        log.info("Created student with ID: {} and code: {}", savedStudent.getId(), savedStudent.getStudentCode());

        return mapToStudentDTO(savedStudent);
    }

    @Override
    public StudentDTO updateStudent(Long id, UpdateStudentRequestDTO request) {
        log.info("Updating student ID: {}", id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        UserAccount userAccount = student.getUserAccount();
        boolean updated = false;

        // Update full name if provided
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            userAccount.setFullName(request.getFullName());
            updated = true;
        }

        // Update email if provided and different
        if (request.getEmail() != null && !request.getEmail().equals(userAccount.getEmail())) {
            if (userAccountRepository.existsByEmail(request.getEmail())) {
                throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
            }
            userAccount.setEmail(request.getEmail());
            updated = true;
        }

        // Update phone if provided and different
        if (request.getPhone() != null && !request.getPhone().equals(userAccount.getPhone())) {
            if (userAccountRepository.existsByPhone(request.getPhone())) {
                throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
            }
            userAccount.setPhone(request.getPhone());
            updated = true;
        }

        // Update branch if provided
        if (request.getBranchId() != null && !request.getBranchId().equals(student.getBranch().getId())) {
            Branch newBranch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new CustomException(ErrorCode.BRANCH_NOT_FOUND));
            student.setBranch(newBranch);
            updated = true;
        }

        if (updated) {
            userAccount.setUpdatedAt(OffsetDateTime.now());
            student.setUpdatedAt(OffsetDateTime.now());
            userAccountRepository.save(userAccount);
            Student updatedStudent = studentRepository.save(student);
            log.info("Updated student ID: {}", id);
            return mapToStudentDTO(updatedStudent);
        }

        log.info("No changes detected for student ID: {}", id);
        return mapToStudentDTO(student);
    }

    @Override
    public BulkImportStudentResponseDTO bulkImportStudents(MultipartFile file, Long branchId) {
        log.info("Bulk importing students for branch ID: {}", branchId);

        // Validate branch exists
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new CustomException(ErrorCode.BRANCH_NOT_FOUND));

        BulkImportStudentResponseDTO response = new BulkImportStudentResponseDTO();
        List<StudentDTO> studentsCreated = new ArrayList<>();
        List<BulkImportStudentResponseDTO.ImportError> errors = new ArrayList<>();
        int totalRows = 0;
        int successful = 0;
        int failed = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            int rowNumber = 0;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                rowNumber++;
                
                // Skip header row
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                totalRows++;
                String[] fields = line.split(",");
                
                // Validate CSV format
                if (fields.length < 3) {
                    errors.add(new BulkImportStudentResponseDTO.ImportError(
                            rowNumber, "", "", "Invalid CSV format: expected 3 columns (full_name,email,phone)"));
                    failed++;
                    continue;
                }

                String fullName = fields[0].trim();
                String email = fields[1].trim();
                String phone = fields[2].trim();

                try {
                    // Validate fields
                    if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                        throw new IllegalArgumentException("All fields are required");
                    }

                    // Check if email already exists
                    if (userAccountRepository.existsByEmail(email)) {
                        throw new IllegalArgumentException("Email already exists");
                    }

                    // Check if phone already exists
                    if (userAccountRepository.existsByPhone(phone)) {
                        throw new IllegalArgumentException("Phone already exists");
                    }

                    // Create UserAccount
                    UserAccount userAccount = new UserAccount();
                    userAccount.setEmail(email);
                    userAccount.setPhone(phone);
                    userAccount.setFullName(fullName);
                    userAccount.setPasswordHash(passwordEncoder.encode("123456")); // Default password
                    userAccount.setStatus("active");
                    userAccount.setCreatedAt(OffsetDateTime.now());
                    userAccount.setUpdatedAt(OffsetDateTime.now());
                    
                    UserAccount savedUser = userAccountRepository.save(userAccount);

                    // Create Student
                    Student student = new Student();
                    student.setUserAccount(savedUser);
                    student.setStudentCode(generateStudentCode(branch));
                    student.setBranch(branch);
                    student.setCreatedAt(OffsetDateTime.now());
                    student.setUpdatedAt(OffsetDateTime.now());
                    
                    Student savedStudent = studentRepository.save(student);
                    studentsCreated.add(mapToStudentDTO(savedStudent));
                    successful++;
                    
                    log.debug("Successfully imported student: {} (row {})", email, rowNumber);
                    
                } catch (Exception e) {
                    errors.add(new BulkImportStudentResponseDTO.ImportError(
                            rowNumber, fullName, email, e.getMessage()));
                    failed++;
                    log.warn("Failed to import student at row {}: {}", rowNumber, e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Error reading CSV file", e);
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        response.setTotalRows(totalRows);
        response.setSuccessful(successful);
        response.setFailed(failed);
        response.setStudentsCreated(studentsCreated);
        response.setErrors(errors);

        log.info("Bulk import completed: {} successful, {} failed out of {} total rows", 
                successful, failed, totalRows);
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentListDTO> getAllStudents(String search, Long branchId, Pageable pageable) {
        log.info("Getting all students with search: {}, branchId: {}", search, branchId);

        // Build specification for dynamic query
        Page<Student> students = studentRepository.findAll(
                (root, query, cb) -> {
                    List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

                    // Search filter (name, code, email)
                    if (search != null && !search.isEmpty()) {
                        String searchPattern = "%" + search.toLowerCase() + "%";
                        predicates.add(cb.or(
                                cb.like(cb.lower(root.get("studentCode")), searchPattern),
                                cb.like(cb.lower(root.get("userAccount").get("fullName")), searchPattern),
                                cb.like(cb.lower(root.get("userAccount").get("email")), searchPattern)
                        ));
                    }

                    // Branch filter
                    if (branchId != null) {
                        predicates.add(cb.equal(root.get("branch").get("id"), branchId));
                    }

                    return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
                },
                pageable
        );

        // Map to DTOs
        List<StudentListDTO> dtos = students.getContent().stream()
                .map(this::mapToStudentListDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, students.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public StudentProfileDTO getStudentProfile(Long id) {
        log.info("Getting student profile for ID: {}", id);
        
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));
        
        UserAccount userAccount = student.getUserAccount();
        
        // Get current enrollments (enrolled or ongoing)
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(id).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ENROLLED || 
                            e.getStatus() == EnrollmentStatus.WAITLISTED)
                .collect(Collectors.toList());
        
        // Build current classes with attendance summary
        List<CurrentClassDTO> currentClasses = enrollments.stream()
                .map(enrollment -> {
                    ClassEntity clazz = enrollment.getClazz();
                    
                    // Calculate attendance summary for this enrollment
                    List<StudentSession> studentSessions = studentSessionRepository.findAll(
                            (root, query, cb) -> cb.and(
                                    cb.equal(root.get("id").get("studentId"), id),
                                    cb.equal(root.get("session").get("clazz").get("id"), clazz.getId())
                            )
                    );
                    
                    int total = studentSessions.size();
                    long attended = studentSessions.stream()
                            .filter(ss -> ss.getAttendanceStatus() == AttendanceStatus.PRESENT)
                            .count();
                    long absent = studentSessions.stream()
                            .filter(ss -> ss.getAttendanceStatus() == AttendanceStatus.ABSENT)
                            .count();
                    long excused = studentSessions.stream()
                            .filter(ss -> ss.getAttendanceStatus() == AttendanceStatus.EXCUSED)
                            .count();
                    
                    double rate = total > 0 ? (double) attended / total : 0.0;
                    
                    AttendanceSummaryDTO summary = AttendanceSummaryDTO.builder()
                            .totalSessions(total)
                            .attended((int) attended)
                            .absent((int) absent)
                            .excused((int) excused)
                            .rate(rate)
                            .build();
                    
                    return CurrentClassDTO.builder()
                            .classId(clazz.getId())
                            .classCode(clazz.getCode())
                            .className(clazz.getName())
                            .enrollmentStatus(enrollment.getStatus().name())
                            .enrolledAt(enrollment.getEnrolledAt() != null ? enrollment.getEnrolledAt().toLocalDateTime() : null)
                            .attendanceSummary(summary)
                            .build();
                })
                .collect(Collectors.toList());
        
        return StudentProfileDTO.builder()
                .id(student.getId())
                .userId(userAccount.getId())
                .studentCode(student.getStudentCode())
                .fullName(userAccount.getFullName())
                .email(userAccount.getEmail())
                .phone(userAccount.getPhone())
                .dateOfBirth(null) // Student entity doesn't have dateOfBirth field
                .guardianName(null) // Student entity doesn't have guardianName field
                .guardianPhone(null) // Student entity doesn't have guardianPhone field
                .branchId(student.getBranch().getId())
                .branchName(student.getBranch().getName())
                .currentClasses(currentClasses)
                .createdAt(student.getCreatedAt() != null ? student.getCreatedAt().toLocalDateTime() : null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentScheduleDTO getStudentSchedule(Long id, LocalDate dateFrom, LocalDate dateTo, Long classId) {
        log.info("Getting schedule for student ID: {} from {} to {}, classId: {}", id, dateFrom, dateTo, classId);
        
        // Verify student exists
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));
        
        // Query StudentSession with filters
        List<StudentSession> studentSessions = studentSessionRepository.findAll(
                (root, query, cb) -> {
                    List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
                    
                    // Student filter
                    predicates.add(cb.equal(root.get("id").get("studentId"), id));
                    
                    // Date range filter
                    if (dateFrom != null) {
                        predicates.add(cb.greaterThanOrEqualTo(root.get("session").get("date"), dateFrom));
                    }
                    if (dateTo != null) {
                        predicates.add(cb.lessThanOrEqualTo(root.get("session").get("date"), dateTo));
                    }
                    
                    // Class filter
                    if (classId != null) {
                        predicates.add(cb.equal(root.get("session").get("clazz").get("id"), classId));
                    }
                    
                    return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
                }
        );
        
        // Map to DTOs with full details
        List<StudentScheduleSessionDTO> sessionDTOs = studentSessions.stream()
                .map(ss -> {
                    SessionEntity session = ss.getSession();
                    ClassEntity clazz = session.getClazz();
                    
                    // Get teacher info and resources for this session
                    List<TeachingSlot> teachingSlots = teachingSlotRepository.findByIdSessionId(session.getId());
                    List<SessionResource> sessionResources = sessionResourceRepository.findByIdSessionId(session.getId());
                    
                    // Get teacher (primary role)
                    String teacherName = teachingSlots.stream()
                            .filter(ts -> ts.getRole().name().equals("PRIMARY"))
                            .findFirst()
                            .map(ts -> ts.getTeacher().getUserAccount().getFullName())
                            .orElse(null);
                    
                    // Get resource
                    String resourceType = sessionResources.stream()
                            .findFirst()
                            .map(sr -> sr.getResource().getResourceType().name())
                            .orElse(null);
                    
                    String resourceName = sessionResources.stream()
                            .findFirst()
                            .map(sr -> sr.getResource().getName())
                            .orElse(null);
                    
                    return StudentScheduleSessionDTO.builder()
                            .attendanceStatus(ss.getAttendanceStatus().name())
                            .isMakeup(ss.getIsMakeup())
                            .note(ss.getNote())
                            .sessionId(session.getId())
                            .date(session.getDate())
                            .startTime(session.getStartTime())
                            .endTime(session.getEndTime())
                            .classId(clazz.getId())
                            .classCode(clazz.getCode())
                            .className(clazz.getName())
                            .sequenceNo(session.getCourseSession() != null ? session.getCourseSession().getSequenceNumber() : null)
                            .topic(session.getCourseSession() != null ? session.getCourseSession().getTopic() : null)
                            .teacherName(teacherName)
                            .resourceType(resourceType)
                            .resourceName(resourceName)
                            .build();
                })
                .sorted(Comparator.comparing(StudentScheduleSessionDTO::getDate)
                        .thenComparing(StudentScheduleSessionDTO::getStartTime))
                .collect(Collectors.toList());
        
        // Build summary
        Map<String, Integer> statusCounts = new HashMap<>();
        for (StudentScheduleSessionDTO dto : sessionDTOs) {
            String status = dto.getAttendanceStatus();
            statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
        }
        
        StudentScheduleSummaryDTO summary = StudentScheduleSummaryDTO.builder()
                .totalSessions(sessionDTOs.size())
                .byStatus(statusCounts)
                .build();
        
        return StudentScheduleDTO.builder()
                .studentId(id)
                .studentName(student.getUserAccount().getFullName())
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .sessions(sessionDTOs)
                .summary(summary)
                .build();
    }

    // Helper methods
    private String generateStudentCode(Branch branch) {
        int currentYear = Year.now().getValue();
        String yearSuffix = String.valueOf(currentYear).substring(2); // Last 2 digits of year
        
        // Find the highest student code for current year in this branch
        String prefix = "HV-" + yearSuffix + "-";
        
        // Count students in this branch to generate sequential number
        long count = studentRepository.count();
        String sequential = String.format("%03d", count + 1);
        
        return prefix + sequential;
    }

    private StudentDTO mapToStudentDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setUserId(student.getUserAccount().getId());
        dto.setStudentCode(student.getStudentCode());
        dto.setFullName(student.getUserAccount().getFullName());
        dto.setEmail(student.getUserAccount().getEmail());
        dto.setPhone(student.getUserAccount().getPhone());
        dto.setBranchId(student.getBranch().getId());
        dto.setBranchName(student.getBranch().getName());
        dto.setCreatedAt(student.getCreatedAt());
        return dto;
    }

    private StudentListDTO mapToStudentListDTO(Student student) {
        return StudentListDTO.builder()
                .id(student.getId())
                .studentCode(student.getStudentCode())
                .fullName(student.getUserAccount().getFullName())
                .email(student.getUserAccount().getEmail())
                .phone(student.getUserAccount().getPhone())
                .branchId(student.getBranch().getId())
                .branchName(student.getBranch().getName())
                .createdAt(student.getCreatedAt() != null ? student.getCreatedAt().toLocalDateTime() : null)
                .build();
    }
}
