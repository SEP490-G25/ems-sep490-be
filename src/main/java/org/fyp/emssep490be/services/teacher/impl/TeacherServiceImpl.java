package org.fyp.emssep490be.services.teacher.impl;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.teacher.CreateTeacherRequestDTO;
import org.fyp.emssep490be.dtos.teacher.TeacherAvailabilityDTO;
import org.fyp.emssep490be.dtos.teacher.TeacherProfileDTO;
import org.fyp.emssep490be.dtos.teacher.TeacherSkillDTO;
import org.fyp.emssep490be.dtos.teacher.TeacherSkillsResponseDTO;
import org.fyp.emssep490be.dtos.teacher.UpdateTeacherRequestDTO;
import org.fyp.emssep490be.dtos.teacher.UpdateTeacherSkillsRequestDTO;
import org.fyp.emssep490be.entities.Teacher;
import org.fyp.emssep490be.entities.TeacherAvailability;
import org.fyp.emssep490be.entities.TeacherSkill;
import org.fyp.emssep490be.entities.UserAccount;
import org.fyp.emssep490be.entities.enums.Skill;
import org.fyp.emssep490be.entities.ids.TeacherSkillId;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.TeacherAvailabilityRepository;
import org.fyp.emssep490be.repositories.TeacherRepository;
import org.fyp.emssep490be.repositories.TeacherSkillRepository;
import org.fyp.emssep490be.repositories.UserAccountRepository;
import org.fyp.emssep490be.services.teacher.TeacherService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
public class TeacherServiceImpl implements TeacherService {

    private final EntityManager entityManager;
    private final TeacherRepository teacherRepository;
    private final TeacherSkillRepository teacherSkillRepository;
    private final TeacherAvailabilityRepository teacherAvailabilityRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public TeacherServiceImpl(EntityManager entityManager,
                             TeacherRepository teacherRepository,
                             TeacherSkillRepository teacherSkillRepository,
                             TeacherAvailabilityRepository teacherAvailabilityRepository,
                             UserAccountRepository userAccountRepository,
                             PasswordEncoder passwordEncoder) {
        this.entityManager = entityManager;
        this.teacherRepository = teacherRepository;
        this.teacherSkillRepository = teacherSkillRepository;
        this.teacherAvailabilityRepository = teacherAvailabilityRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherProfileDTO getTeacherProfile(Long id) {
        log.info("Getting teacher profile for ID: {}", id);
        
        // Validate input
        if (id == null || id <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        
        // Use optimized query to avoid N+1 problem
        Teacher teacher = teacherRepository.findByIdWithUserAccount(id)
            .orElseThrow(() -> new CustomException(ErrorCode.TEACHER_NOT_FOUND));

        // User info (already fetched with JOIN FETCH)
        var user = teacher.getUserAccount();

        // Skills - handle edge case where teacher has no skills
        List<TeacherSkill> skills = teacherSkillRepository.findByTeacherId(id);
        List<TeacherSkillDTO> skillDTOs = skills.stream().map(ts -> {
            TeacherSkillDTO dto = new TeacherSkillDTO();
            dto.setTeacherId(id);
            dto.setSkill(ts.getId().getSkill().name());
            dto.setProficiencyLevel(ts.getLevel() == null ? null : ts.getLevel().intValue());
            return dto;
        }).collect(Collectors.toList());

        // Weekly availability - handle edge case where teacher has no availability
        List<TeacherAvailability> weekly = teacherAvailabilityRepository.findByTeacherId(id);
        List<TeacherAvailabilityDTO> availabilityDTOs = weekly.stream().map(av -> {
            TeacherAvailabilityDTO dto = new TeacherAvailabilityDTO();
            dto.setId(av.getId());
            dto.setTeacherId(id);
            dto.setDayOfWeek(av.getDayOfWeek().intValue());
            dto.setStartTime(av.getStartTime());
            dto.setEndTime(av.getEndTime());
            // Since TeacherAvailability represents available time slots, isAvailable should be true
            // This is not hardcoded but represents the business logic: if record exists, teacher is available
            dto.setIsAvailable(true);
            return dto;
        }).collect(Collectors.toList());

        return new TeacherProfileDTO(
            teacher.getId(),
            user.getId(),
            teacher.getEmployeeCode(),
            user.getFullName(),
            user.getEmail(),
            user.getPhone(),
            user.getStatus(),
            skillDTOs,
            availabilityDTOs
        );
    }

    @Override
    @Transactional
    public TeacherProfileDTO createTeacher(CreateTeacherRequestDTO request) {
        // Validate input first
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        
        log.info("Creating teacher with employee code: {}", request.getEmployeeCode());

        // Check if employee code already exists
        if (teacherRepository.findByEmployeeCode(request.getEmployeeCode()).isPresent()) {
            throw new CustomException(ErrorCode.TEACHER_EMPLOYEE_CODE_ALREADY_EXISTS);
        }

        // Check if email already exists
        if (userAccountRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        // Check if phone already exists
        if (userAccountRepository.existsByPhone(request.getPhone())) {
            throw new CustomException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
        }

        // Create UserAccount first
        UserAccount userAccount = new UserAccount();
        userAccount.setEmail(request.getEmail());
        userAccount.setPhone(request.getPhone());
        userAccount.setFullName(request.getFullName());
        userAccount.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userAccount.setStatus(request.getStatus());
        userAccount.setCreatedAt(OffsetDateTime.now());
        userAccount.setUpdatedAt(OffsetDateTime.now());

        UserAccount savedUserAccount = userAccountRepository.save(userAccount);

        // Create Teacher
        Teacher teacher = new Teacher();
        teacher.setUserAccount(savedUserAccount);
        teacher.setEmployeeCode(request.getEmployeeCode());
        teacher.setNote(request.getNote());
        teacher.setCreatedAt(OffsetDateTime.now());
        teacher.setUpdatedAt(OffsetDateTime.now());

        Teacher savedTeacher = teacherRepository.save(teacher);

        // Return teacher profile (no skills/availability for new teacher)
        return new TeacherProfileDTO(
                savedTeacher.getId(),
                savedTeacher.getUserAccount().getId(),
                savedTeacher.getEmployeeCode(),
                savedTeacher.getUserAccount().getFullName(),
                savedTeacher.getUserAccount().getEmail(),
                savedTeacher.getUserAccount().getPhone(),
                savedTeacher.getUserAccount().getStatus(),
                Collections.emptyList(), // No skills initially
                Collections.emptyList()  // No availability initially
        );
    }

    @Override
    @Transactional
    public TeacherProfileDTO updateTeacher(Long id, UpdateTeacherRequestDTO request) {
        // Validate input
        if (id == null || id <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        
        if (request == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        
        log.info("Updating teacher with ID: {}", id);

        // Find teacher with user account
        Teacher teacher = teacherRepository.findByIdWithUserAccount(id)
                .orElseThrow(() -> new CustomException(ErrorCode.TEACHER_NOT_FOUND));

        UserAccount userAccount = teacher.getUserAccount();

        // Check for duplicate email (if email is being updated)
        if (request.getEmail() != null && !request.getEmail().equals(userAccount.getEmail())) {
            if (userAccountRepository.existsByEmail(request.getEmail())) {
                throw new CustomException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
            }
        }

        // Check for duplicate phone (if phone is being updated)
        if (request.getPhone() != null && !request.getPhone().equals(userAccount.getPhone())) {
            if (userAccountRepository.existsByPhone(request.getPhone())) {
                throw new CustomException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
            }
        }

        // Update UserAccount fields (only if provided)
        if (request.getFullName() != null) {
            userAccount.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            userAccount.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            userAccount.setPhone(request.getPhone());
        }
        if (request.getStatus() != null) {
            userAccount.setStatus(request.getStatus());
        }
        userAccount.setUpdatedAt(OffsetDateTime.now());

        // Update Teacher fields (only if provided)
        if (request.getNote() != null) {
            teacher.setNote(request.getNote());
        }
        teacher.setUpdatedAt(OffsetDateTime.now());

        // Save both entities
        UserAccount savedUserAccount = userAccountRepository.save(userAccount);
        Teacher savedTeacher = teacherRepository.save(teacher);

        // Get skills and availability for response
        List<TeacherSkill> skills = teacherSkillRepository.findByTeacherId(id);
        List<TeacherSkillDTO> skillDTOs = skills.stream().map(ts -> {
            TeacherSkillDTO dto = new TeacherSkillDTO();
            dto.setTeacherId(id);
            dto.setSkill(ts.getId().getSkill().name());
            dto.setProficiencyLevel(ts.getLevel() == null ? null : ts.getLevel().intValue());
            return dto;
        }).collect(Collectors.toList());

        List<TeacherAvailability> weekly = teacherAvailabilityRepository.findByTeacherId(id);
        List<TeacherAvailabilityDTO> availabilityDTOs = weekly.stream().map(av -> {
            TeacherAvailabilityDTO dto = new TeacherAvailabilityDTO();
            dto.setId(av.getId());
            dto.setTeacherId(id);
            dto.setDayOfWeek(av.getDayOfWeek().intValue());
            dto.setStartTime(av.getStartTime());
            dto.setEndTime(av.getEndTime());
            return dto;
        }).collect(Collectors.toList());

        return new TeacherProfileDTO(
                savedTeacher.getId(),
                savedUserAccount.getId(),
                savedTeacher.getEmployeeCode(),
                savedUserAccount.getFullName(),
                savedUserAccount.getEmail(),
                savedUserAccount.getPhone(),
                savedUserAccount.getStatus(),
                skillDTOs,
                availabilityDTOs
        );
    }

    @Override
    @Transactional
    public void deleteTeacher(Long id) {
        // Validate input
        if (id == null || id <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        log.info("Deleting teacher with ID: {}", id);

        // Find teacher
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.TEACHER_NOT_FOUND));

        // Check if teacher has any active sessions or assignments
        // For now, we'll do a soft delete by updating the status
        // In a real system, you might want to check for dependencies first
        
        // Soft delete: Update teacher status to INACTIVE
        UserAccount userAccount = teacher.getUserAccount();
        if (userAccount != null) {
            userAccount.setStatus("INACTIVE");
            userAccount.setUpdatedAt(OffsetDateTime.now());
            userAccountRepository.save(userAccount);
        }

        // Update teacher record
        teacher.setUpdatedAt(OffsetDateTime.now());
        teacherRepository.save(teacher);

        log.info("Teacher with ID {} has been deactivated", id);
    }

    @Override
    @Transactional
    public TeacherSkillsResponseDTO updateTeacherSkills(Long id, UpdateTeacherSkillsRequestDTO request) {
        log.info("Updating teacher skills for ID: {}", id);

        // Validate input
        if (id == null || id <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        if (request == null || request.getSkills() == null || request.getSkills().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        // Find teacher
        Teacher teacher = teacherRepository.findByIdWithUserAccount(id)
                .orElseThrow(() -> new CustomException(ErrorCode.TEACHER_NOT_FOUND));

        // Validate skills data
        for (UpdateTeacherSkillsRequestDTO.TeacherSkillDTO skillDTO : request.getSkills()) {
            if (skillDTO.getSkill() == null || skillDTO.getSkill().trim().isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_INPUT);
            }
            if (skillDTO.getLevel() == null || skillDTO.getLevel() < 1 || skillDTO.getLevel() > 5) {
                throw new CustomException(ErrorCode.INVALID_INPUT);
            }
            
            // Validate skill enum (case-insensitive)
            String skillInput = skillDTO.getSkill().trim().toLowerCase();
            if (!skillInput.matches("general|reading|writing|speaking|listening")) {
                throw new CustomException(ErrorCode.INVALID_INPUT);
            }
            
            // Convert to uppercase for enum
            skillDTO.setSkill(skillInput.toUpperCase());
        }

        // Delete existing skills
        teacherSkillRepository.deleteByTeacherId(id);
        entityManager.flush(); // Force flush DELETE to database before INSERT

        // Insert with native SQL casting to skill_enum (no entity/DB changes)
        for (UpdateTeacherSkillsRequestDTO.TeacherSkillDTO skillDTO : request.getSkills()) {
            String skillLower = skillDTO.getSkill().trim().toLowerCase();
            teacherSkillRepository.insertTeacherSkill(id, skillLower, skillDTO.getLevel().shortValue());
        }

        // Update teacher timestamp
        teacher.setUpdatedAt(OffsetDateTime.now());
        teacherRepository.save(teacher);

        // Read back for response using native query to avoid enum issues
        List<Object[]> skillRows = teacherSkillRepository.findSkillsByTeacherIdNative(id);
        
        // Build response from native query results
        List<TeacherSkillsResponseDTO.TeacherSkillDTO> responseSkills = skillRows.stream()
                .map(row -> {
                    String skillName = (String) row[1]; // skill column
                    Integer level = ((Number) row[2]).intValue(); // level column
                    return new TeacherSkillsResponseDTO.TeacherSkillDTO(skillName, level);
                })
                .collect(Collectors.toList());

        log.info("Teacher skills updated successfully for ID: {}", id);
        return new TeacherSkillsResponseDTO(id, responseSkills, teacher.getUpdatedAt());
    }
}
