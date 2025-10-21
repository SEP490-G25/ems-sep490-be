package org.fyp.emssep490be.services.teacher.impl;

import org.fyp.emssep490be.dtos.teacher.TeacherProfileDTO;
import org.fyp.emssep490be.entities.Teacher;
import org.fyp.emssep490be.entities.UserAccount;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.TeacherAvailabilityRepository;
import org.fyp.emssep490be.repositories.TeacherRepository;
import org.fyp.emssep490be.repositories.TeacherSkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherServiceImplTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private TeacherSkillRepository teacherSkillRepository;

    @Mock
    private TeacherAvailabilityRepository teacherAvailabilityRepository;

    @InjectMocks
    private TeacherServiceImpl teacherService;

    private Teacher teacher;
    private UserAccount userAccount;

    @BeforeEach
    void setUp() {
        userAccount = new UserAccount();
        userAccount.setId(1L);
        userAccount.setFullName("John Doe");
        userAccount.setEmail("john.doe@example.com");
        userAccount.setPhone("0123456789");
        userAccount.setStatus("ACTIVE");

        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setEmployeeCode("EMP001");
        teacher.setUserAccount(userAccount);
    }

    @Test
    void getTeacherProfile_ValidId_ReturnsTeacherProfile() {
        // Given
        when(teacherRepository.findByIdWithUserAccount(1L)).thenReturn(Optional.of(teacher));
        when(teacherSkillRepository.findByTeacherId(1L)).thenReturn(Collections.emptyList());
        when(teacherAvailabilityRepository.findByTeacherId(1L)).thenReturn(Collections.emptyList());

        // When
        TeacherProfileDTO result = teacherService.getTeacherProfile(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getUserAccountId());
        assertEquals("EMP001", result.getEmployeeCode());
        assertEquals("John Doe", result.getFullName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("0123456789", result.getPhone());
        assertEquals("ACTIVE", result.getStatus());
        assertTrue(result.getSkills().isEmpty());
        assertTrue(result.getAvailability().isEmpty());

        verify(teacherRepository).findByIdWithUserAccount(1L);
        verify(teacherSkillRepository).findByTeacherId(1L);
        verify(teacherAvailabilityRepository).findByTeacherId(1L);
    }

    @Test
    void getTeacherProfile_InvalidId_ThrowsException() {
        // Given
        when(teacherRepository.findByIdWithUserAccount(999L)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.getTeacherProfile(999L));
        
        assertEquals(ErrorCode.TEACHER_NOT_FOUND, exception.getErrorCode());
        verify(teacherRepository).findByIdWithUserAccount(999L);
        verify(teacherSkillRepository, never()).findByTeacherId(anyLong());
        verify(teacherAvailabilityRepository, never()).findByTeacherId(anyLong());
    }

    @Test
    void getTeacherProfile_NullId_ThrowsException() {
        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.getTeacherProfile(null));
        
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
        verify(teacherRepository, never()).findByIdWithUserAccount(anyLong());
    }

    @Test
    void getTeacherProfile_ZeroId_ThrowsException() {
        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.getTeacherProfile(0L));
        
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
        verify(teacherRepository, never()).findByIdWithUserAccount(anyLong());
    }

    @Test
    void getTeacherProfile_NegativeId_ThrowsException() {
        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.getTeacherProfile(-1L));
        
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
        verify(teacherRepository, never()).findByIdWithUserAccount(anyLong());
    }
}
