package org.fyp.emssep490be.services.teacher.impl;

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
        Teacher teacher = teacherRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

        // User info
        var user = teacher.getUserAccount();

        // Skills
        List<TeacherSkill> skills = teacherSkillRepository.findByTeacherId(id);
        List<TeacherSkillDTO> skillDTOs = skills.stream().map(ts -> {
            TeacherSkillDTO dto = new TeacherSkillDTO();
            dto.setId(null); // entity dùng composite key; để null hoặc bỏ field này khỏi DTO
            dto.setTeacherId(id);
            dto.setSkill(ts.getId().getSkill().name());
            dto.setProficiencyLevel(ts.getLevel() == null ? null : ts.getLevel().intValue());
            dto.setCertificationInfo(null);
            return dto;
        }).collect(Collectors.toList());

        // Weekly availability (đặt isAvailable = true cho khung rảnh)
        List<TeacherAvailability> weekly = teacherAvailabilityRepository.findByTeacherId(id);
        List<TeacherAvailabilityDTO> availabilityDTOs = weekly.stream().map(av -> {
            TeacherAvailabilityDTO dto = new TeacherAvailabilityDTO();
            dto.setId(av.getId());
            dto.setTeacherId(id);
            dto.setDayOfWeek(av.getDayOfWeek().intValue());
            dto.setStartTime(av.getStartTime());
            dto.setEndTime(av.getEndTime());
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
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Object getTeacherWorkload(Long id) {
        log.info("Getting teacher workload for ID: {}", id);
        return null;
    }
}
