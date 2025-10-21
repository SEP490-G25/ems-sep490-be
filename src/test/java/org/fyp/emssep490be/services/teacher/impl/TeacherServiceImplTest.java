package org.fyp.emssep490be.services.teacher.impl;

import org.fyp.emssep490be.dtos.teacher.CreateTeacherRequest;
import org.fyp.emssep490be.dtos.teacher.TeacherProfileDTO;
import org.fyp.emssep490be.entities.Teacher;
import org.fyp.emssep490be.entities.UserAccount;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.TeacherAvailabilityRepository;
import org.fyp.emssep490be.repositories.TeacherRepository;
import org.fyp.emssep490be.repositories.TeacherSkillRepository;
import org.fyp.emssep490be.repositories.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherServiceImplTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private TeacherSkillRepository teacherSkillRepository;

    @Mock
    private TeacherAvailabilityRepository teacherAvailabilityRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

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

    @Test
    void createTeacher_ValidRequest_ReturnsTeacherProfile() {
        // Given
        CreateTeacherRequest request = createCreateTeacherRequest();
        UserAccount savedUserAccount = createUserAccount();
        Teacher savedTeacher = createTeacher();
        
        when(teacherRepository.findByEmployeeCode(request.getEmployeeCode())).thenReturn(Optional.empty());
        when(userAccountRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userAccountRepository.existsByPhone(request.getPhone())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(savedUserAccount);
        when(teacherRepository.save(any(Teacher.class))).thenReturn(savedTeacher);

        // When
        TeacherProfileDTO result = teacherService.createTeacher(request);

        // Then
        assertNotNull(result);
        assertEquals(savedTeacher.getId(), result.getId());
        assertEquals(savedUserAccount.getId(), result.getUserAccountId());
        assertEquals(request.getEmployeeCode(), result.getEmployeeCode());
        assertEquals(request.getFullName(), result.getFullName());
        assertEquals(request.getEmail(), result.getEmail());
        assertEquals(request.getPhone(), result.getPhone());
        assertEquals(request.getStatus(), result.getStatus());
        assertTrue(result.getSkills().isEmpty());
        assertTrue(result.getAvailability().isEmpty());

        verify(teacherRepository).findByEmployeeCode(request.getEmployeeCode());
        verify(userAccountRepository).existsByEmail(request.getEmail());
        verify(userAccountRepository).existsByPhone(request.getPhone());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userAccountRepository).save(any(UserAccount.class));
        verify(teacherRepository).save(any(Teacher.class));
    }

    @Test
    void createTeacher_NullRequest_ThrowsException() {
        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.createTeacher(null));
        
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
        verify(teacherRepository, never()).findByEmployeeCode(anyString());
    }

    @Test
    void createTeacher_DuplicateEmployeeCode_ThrowsException() {
        // Given
        CreateTeacherRequest request = createCreateTeacherRequest();
        when(teacherRepository.findByEmployeeCode(request.getEmployeeCode()))
                .thenReturn(Optional.of(createTeacher()));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.createTeacher(request));
        
        assertEquals(ErrorCode.TEACHER_EMPLOYEE_CODE_ALREADY_EXISTS, exception.getErrorCode());
        verify(teacherRepository).findByEmployeeCode(request.getEmployeeCode());
        verify(userAccountRepository, never()).existsByEmail(anyString());
    }

    @Test
    void createTeacher_DuplicateEmail_ThrowsException() {
        // Given
        CreateTeacherRequest request = createCreateTeacherRequest();
        when(teacherRepository.findByEmployeeCode(request.getEmployeeCode())).thenReturn(Optional.empty());
        when(userAccountRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.createTeacher(request));
        
        assertEquals(ErrorCode.USER_EMAIL_ALREADY_EXISTS, exception.getErrorCode());
        verify(teacherRepository).findByEmployeeCode(request.getEmployeeCode());
        verify(userAccountRepository).existsByEmail(request.getEmail());
        verify(userAccountRepository, never()).existsByPhone(anyString());
    }

    @Test
    void createTeacher_DuplicatePhone_ThrowsException() {
        // Given
        CreateTeacherRequest request = createCreateTeacherRequest();
        when(teacherRepository.findByEmployeeCode(request.getEmployeeCode())).thenReturn(Optional.empty());
        when(userAccountRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userAccountRepository.existsByPhone(request.getPhone())).thenReturn(true);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.createTeacher(request));
        
        assertEquals(ErrorCode.USER_PHONE_ALREADY_EXISTS, exception.getErrorCode());
        verify(teacherRepository).findByEmployeeCode(request.getEmployeeCode());
        verify(userAccountRepository).existsByEmail(request.getEmail());
        verify(userAccountRepository).existsByPhone(request.getPhone());
        verify(passwordEncoder, never()).encode(anyString());
    }

    private CreateTeacherRequest createCreateTeacherRequest() {
        CreateTeacherRequest request = new CreateTeacherRequest();
        request.setEmployeeCode("EMP1234"); // 3 letters + 4 digits
        request.setFullName("John Doe");
        request.setEmail("john.doe@example.com");
        request.setPhone("0123456789");
        request.setPassword("Password123!");
        request.setStatus("ACTIVE");
        request.setNote("Test teacher");
        return request;
    }

    private UserAccount createUserAccount() {
        UserAccount userAccount = new UserAccount();
        userAccount.setId(1L);
        userAccount.setFullName("John Doe");
        userAccount.setEmail("john.doe@example.com");
        userAccount.setPhone("0123456789");
        userAccount.setStatus("ACTIVE");
        userAccount.setPasswordHash("encodedPassword");
        userAccount.setCreatedAt(OffsetDateTime.now());
        userAccount.setUpdatedAt(OffsetDateTime.now());
        return userAccount;
    }

    private Teacher createTeacher() {
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setEmployeeCode("EMP1234"); // 3 letters + 4 digits
        teacher.setNote("Test teacher");
        teacher.setCreatedAt(OffsetDateTime.now());
        teacher.setUpdatedAt(OffsetDateTime.now());
        teacher.setUserAccount(createUserAccount());
        return teacher;
    }
}
