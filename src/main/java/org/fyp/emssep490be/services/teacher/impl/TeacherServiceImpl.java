package org.fyp.emssep490be.services.teacher.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.teacher.TeacherAvailabilityDTO;
import org.fyp.emssep490be.dtos.teacher.TeacherProfileDTO;
import org.fyp.emssep490be.dtos.teacher.TeacherSkillDTO;
import org.fyp.emssep490be.entities.Teacher;
import org.fyp.emssep490be.entities.TeacherAvailability;
import org.fyp.emssep490be.entities.TeacherSkill;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.TeacherAvailabilityRepository;
import org.fyp.emssep490be.repositories.TeacherRepository;
import org.fyp.emssep490be.repositories.TeacherSkillRepository;
import org.fyp.emssep490be.services.teacher.TeacherService;
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
    @Transactional(readOnly = true)
    public Object getTeacherSchedule(Long id, String dateFrom, String dateTo) {
        log.info("Getting teacher schedule for ID: {}", id);
        
        // Validate input
        if (id == null || id <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        
        // Check if teacher exists
        if (!teacherRepository.existsById(id)) {
            throw new CustomException(ErrorCode.TEACHER_NOT_FOUND);
        }
        
        // TODO: Implement actual schedule logic based on dateFrom and dateTo
        // This would typically query CourseSession, ClassSession, etc.
        return Collections.emptyMap();
    }

    @Override
    @Transactional(readOnly = true)
    public Object getTeacherWorkload(Long id) {
        log.info("Getting teacher workload for ID: {}", id);
        
        // Validate input
        if (id == null || id <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        
        // Check if teacher exists
        if (!teacherRepository.existsById(id)) {
            throw new CustomException(ErrorCode.TEACHER_NOT_FOUND);
        }
        
        // TODO: Implement actual workload calculation
        // This would typically calculate total hours, classes, students, etc.
        return Collections.emptyMap();
    }
}
