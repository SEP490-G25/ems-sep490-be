package org.fyp.emssep490be.services.student.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.enrollment.CreateEnrollmentRequestDTO;
import org.fyp.emssep490be.dtos.enrollment.EnrollmentDTO;
import org.fyp.emssep490be.dtos.student.*;
import org.fyp.emssep490be.entities.Branch;
import org.fyp.emssep490be.entities.Student;
import org.fyp.emssep490be.entities.UserAccount;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.BranchRepository;
import org.fyp.emssep490be.repositories.StudentRepository;
import org.fyp.emssep490be.repositories.UserAccountRepository;
import org.fyp.emssep490be.services.student.StudentService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final UserAccountRepository userAccountRepository;
    private final BranchRepository branchRepository;
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
    public StudentProfileDTO getStudentProfile(Long id) {
        log.info("Getting student profile for ID: {}", id);
        
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));
        
        // TODO: Map to StudentProfileDTO with more details (enrollments, etc.)
        return new StudentProfileDTO();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getStudentEnrollments(Long id) {
        log.info("Getting enrollments for student ID: {}", id);
        
        // Verify student exists
        if (!studentRepository.existsById(id)) {
            throw new CustomException(ErrorCode.STUDENT_NOT_FOUND);
        }
        
        // TODO: Implement enrollment query
        return new ArrayList<>();
    }

    @Override
    public EnrollmentDTO enrollStudent(Long id, CreateEnrollmentRequestDTO request) {
        // TODO: Create Enrollment and auto-generate StudentSession records for all future sessions
        log.info("Enrolling student ID: {} in class ID: {}", id, request.getClassId());
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Object getStudentSchedule(Long id) {
        log.info("Getting schedule for student ID: {}", id);
        
        // Verify student exists
        if (!studentRepository.existsById(id)) {
            throw new CustomException(ErrorCode.STUDENT_NOT_FOUND);
        }
        
        // TODO: Implement schedule query
        return null;
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
}
