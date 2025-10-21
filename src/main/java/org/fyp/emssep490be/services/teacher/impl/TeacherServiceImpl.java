package org.fyp.emssep490be.services.teacher.impl;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.teacher.CreateTeacherRequest;
import org.fyp.emssep490be.dtos.teacher.TeacherAvailabilityDTO;
import org.fyp.emssep490be.dtos.teacher.TeacherProfileDTO;
import org.fyp.emssep490be.dtos.teacher.TeacherSkillDTO;
import org.fyp.emssep490be.entities.Teacher;
import org.fyp.emssep490be.entities.TeacherAvailability;
import org.fyp.emssep490be.entities.TeacherSkill;
import org.fyp.emssep490be.entities.UserAccount;
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
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherSkillRepository teacherSkillRepository;
    private final TeacherAvailabilityRepository teacherAvailabilityRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

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
    public TeacherProfileDTO createTeacher(CreateTeacherRequest request) {
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
}
