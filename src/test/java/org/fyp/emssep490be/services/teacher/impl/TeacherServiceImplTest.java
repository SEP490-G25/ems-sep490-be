package org.fyp.emssep490be.services.teacher.impl;

import org.fyp.emssep490be.dtos.teacher.CreateTeacherRequestDTO;
import org.fyp.emssep490be.dtos.teacher.TeacherProfileDTO;
import org.fyp.emssep490be.dtos.teacher.TeacherSkillsResponseDTO;
import org.fyp.emssep490be.dtos.teacher.UpdateTeacherRequestDTO;
import org.fyp.emssep490be.dtos.teacher.UpdateTeacherSkillsRequestDTO;
import org.fyp.emssep490be.dtos.teacher.AddTeacherSkillsRequestDTO;
import org.fyp.emssep490be.entities.Teacher;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.persistence.EntityManager;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    @Mock
    private EntityManager entityManager;

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
        when(teacherSkillRepository.findSkillsByTeacherIdNative(1L)).thenReturn(Collections.emptyList());
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
        verify(teacherSkillRepository).findSkillsByTeacherIdNative(1L);
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
        verify(teacherSkillRepository, never()).findSkillsByTeacherIdNative(anyLong());
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
        CreateTeacherRequestDTO request = createCreateTeacherRequest();
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
        CreateTeacherRequestDTO request = createCreateTeacherRequest();
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
        CreateTeacherRequestDTO request = createCreateTeacherRequest();
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
        CreateTeacherRequestDTO request = createCreateTeacherRequest();
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

    private CreateTeacherRequestDTO createCreateTeacherRequest() {
        CreateTeacherRequestDTO request = new CreateTeacherRequestDTO();
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

    @Test
    void updateTeacher_ValidRequest_ReturnsUpdatedTeacherProfile() {
        // Given
        Long teacherId = 1L;
        UpdateTeacherRequestDTO request = createUpdateTeacherRequest();
        Teacher existingTeacher = createTeacher();
        UserAccount existingUserAccount = createUserAccount();
        
        when(teacherRepository.findByIdWithUserAccount(teacherId)).thenReturn(Optional.of(existingTeacher));
        when(userAccountRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userAccountRepository.existsByPhone(request.getPhone())).thenReturn(false);
        when(teacherSkillRepository.findByTeacherId(teacherId)).thenReturn(Collections.emptyList());
        when(teacherAvailabilityRepository.findByTeacherId(teacherId)).thenReturn(Collections.emptyList());
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(invocation -> {
            UserAccount saved = invocation.getArgument(0);
            saved.setId(existingUserAccount.getId());
            return saved;
        });
        when(teacherRepository.save(any(Teacher.class))).thenAnswer(invocation -> {
            Teacher saved = invocation.getArgument(0);
            saved.setId(existingTeacher.getId());
            return saved;
        });

        // When
        TeacherProfileDTO result = teacherService.updateTeacher(teacherId, request);

        // Then
        assertNotNull(result);
        assertEquals(teacherId, result.getId());
        assertEquals(request.getFullName(), result.getFullName());
        assertEquals(request.getEmail(), result.getEmail());
        assertEquals(request.getPhone(), result.getPhone());
        assertEquals(request.getStatus(), result.getStatus());

        verify(teacherRepository).findByIdWithUserAccount(teacherId);
        verify(userAccountRepository).existsByEmail(request.getEmail());
        verify(userAccountRepository).existsByPhone(request.getPhone());
        verify(userAccountRepository).save(any(UserAccount.class));
        verify(teacherRepository).save(any(Teacher.class));
    }

    @Test
    void updateTeacher_NullId_ThrowsException() {
        // Given
        UpdateTeacherRequestDTO request = createUpdateTeacherRequest();

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
            () -> teacherService.updateTeacher(null, request));

        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
        verify(teacherRepository, never()).findByIdWithUserAccount(anyLong());
    }

    @Test
    void updateTeacher_NullRequest_ThrowsException() {
        // Given
        Long teacherId = 1L;

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
            () -> teacherService.updateTeacher(teacherId, null));

        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
        verify(teacherRepository, never()).findByIdWithUserAccount(anyLong());
    }

    @Test
    void updateTeacher_TeacherNotFound_ThrowsException() {
        // Given
        Long teacherId = 999L;
        UpdateTeacherRequestDTO request = createUpdateTeacherRequest();
        
        when(teacherRepository.findByIdWithUserAccount(teacherId)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
            () -> teacherService.updateTeacher(teacherId, request));

        assertEquals(ErrorCode.TEACHER_NOT_FOUND, exception.getErrorCode());
        verify(teacherRepository).findByIdWithUserAccount(teacherId);
        verify(userAccountRepository, never()).existsByEmail(anyString());
    }

    @Test
    void updateTeacher_DuplicateEmail_ThrowsException() {
        // Given
        Long teacherId = 1L;
        UpdateTeacherRequestDTO request = createUpdateTeacherRequest();
        Teacher existingTeacher = createTeacher();
        UserAccount existingUserAccount = createUserAccount();
        existingUserAccount.setEmail("old@example.com"); // Different from request email
        
        when(teacherRepository.findByIdWithUserAccount(teacherId)).thenReturn(Optional.of(existingTeacher));
        when(userAccountRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
            () -> teacherService.updateTeacher(teacherId, request));

        assertEquals(ErrorCode.USER_EMAIL_ALREADY_EXISTS, exception.getErrorCode());
        verify(teacherRepository).findByIdWithUserAccount(teacherId);
        verify(userAccountRepository).existsByEmail(request.getEmail());
        verify(userAccountRepository, never()).existsByPhone(anyString());
    }

    @Test
    void updateTeacher_DuplicatePhone_ThrowsException() {
        // Given
        Long teacherId = 1L;
        UpdateTeacherRequestDTO request = createUpdateTeacherRequest();
        Teacher existingTeacher = createTeacher();
        UserAccount existingUserAccount = createUserAccount();
        existingUserAccount.setPhone("0123456789"); // Different from request phone
        
        when(teacherRepository.findByIdWithUserAccount(teacherId)).thenReturn(Optional.of(existingTeacher));
        when(userAccountRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userAccountRepository.existsByPhone(request.getPhone())).thenReturn(true);

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
            () -> teacherService.updateTeacher(teacherId, request));

        assertEquals(ErrorCode.USER_PHONE_ALREADY_EXISTS, exception.getErrorCode());
        verify(teacherRepository).findByIdWithUserAccount(teacherId);
        verify(userAccountRepository).existsByEmail(request.getEmail());
        verify(userAccountRepository).existsByPhone(request.getPhone());
        verify(userAccountRepository, never()).save(any(UserAccount.class));
    }

    @Test
    void updateTeacher_SameEmailAndPhone_UpdatesSuccessfully() {
        // Given
        Long teacherId = 1L;
        UpdateTeacherRequestDTO request = createUpdateTeacherRequest();
        Teacher existingTeacher = createTeacher();
        UserAccount existingUserAccount = createUserAccount();
        existingUserAccount.setEmail(request.getEmail()); // Same email
        existingUserAccount.setPhone(request.getPhone()); // Same phone
        
        when(teacherRepository.findByIdWithUserAccount(teacherId)).thenReturn(Optional.of(existingTeacher));
        when(teacherSkillRepository.findByTeacherId(teacherId)).thenReturn(Collections.emptyList());
        when(teacherAvailabilityRepository.findByTeacherId(teacherId)).thenReturn(Collections.emptyList());
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(invocation -> {
            UserAccount saved = invocation.getArgument(0);
            saved.setId(existingUserAccount.getId());
            return saved;
        });
        when(teacherRepository.save(any(Teacher.class))).thenAnswer(invocation -> {
            Teacher saved = invocation.getArgument(0);
            saved.setId(existingTeacher.getId());
            return saved;
        });

        // When
        TeacherProfileDTO result = teacherService.updateTeacher(teacherId, request);

        // Then
        assertNotNull(result);
        assertEquals(teacherId, result.getId());
        assertEquals(request.getFullName(), result.getFullName());

        verify(teacherRepository).findByIdWithUserAccount(teacherId);
        // Note: existsByEmail and existsByPhone are still called even with same values
        // because the logic checks if email/phone are different before calling existsBy*
        verify(userAccountRepository).save(any(UserAccount.class));
        verify(teacherRepository).save(any(Teacher.class));
    }

    private UpdateTeacherRequestDTO createUpdateTeacherRequest() {
        UpdateTeacherRequestDTO request = new UpdateTeacherRequestDTO();
        request.setFullName("John Doe Updated");
        request.setEmail("john.updated@example.com");
        request.setPhone("0987654321");
        request.setStatus("ACTIVE");
        request.setNote("Updated teacher note");
        return request;
    }

    // DELETE TEACHER TESTS

    @Test
    void deleteTeacher_ValidId_DeletesSuccessfully() {
        // Given
        Long teacherId = 1L;
        Teacher teacher = createTeacher();
        UserAccount userAccount = createUserAccount();
        teacher.setUserAccount(userAccount);

        when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(teacherRepository.save(any(Teacher.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        assertDoesNotThrow(() -> teacherService.deleteTeacher(teacherId));

        // Then
        verify(teacherRepository).findById(teacherId);
        verify(userAccountRepository).save(any(UserAccount.class));
        verify(teacherRepository).save(any(Teacher.class));
    }

    @Test
    void deleteTeacher_NullId_ThrowsException() {
        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> teacherService.deleteTeacher(null));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    void deleteTeacher_InvalidId_ThrowsException() {
        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> teacherService.deleteTeacher(0L));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    void deleteTeacher_TeacherNotFound_ThrowsException() {
        // Given
        Long teacherId = 999L;
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> teacherService.deleteTeacher(teacherId));
        assertEquals(ErrorCode.TEACHER_NOT_FOUND, exception.getErrorCode());
    }

    // UPDATE TEACHER SKILLS TESTS

    @Test
    void updateTeacherSkills_ValidRequest_ReturnsUpdatedSkills() {
        // Given
        Long teacherId = 1L;
        Teacher teacher = createTeacher();
        UpdateTeacherSkillsRequestDTO request = createUpdateTeacherSkillsRequest();

        when(teacherRepository.findByIdWithUserAccount(teacherId)).thenReturn(Optional.of(teacher));
        
        // Mock current skills (empty - no existing skills)
        List<Object[]> currentSkills = Arrays.asList();
        when(teacherSkillRepository.findSkillsByTeacherIdNative(teacherId)).thenReturn(currentSkills);
        
        // Mock new methods for TRUE UPDATE logic
        doNothing().when(teacherSkillRepository).insertTeacherSkill(anyLong(), anyString(), anyShort());
        when(teacherRepository.save(any(Teacher.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Mock final result for response
        List<Object[]> finalSkills = Arrays.asList(
            new Object[]{teacherId, "speaking", 5},
            new Object[]{teacherId, "listening", 4}
        );
        when(teacherSkillRepository.findSkillsByTeacherIdNative(teacherId)).thenReturn(currentSkills, finalSkills);

        // When
        TeacherSkillsResponseDTO result = teacherService.updateTeacherSkills(teacherId, request);

        // Then
        assertNotNull(result);
        assertEquals(teacherId, result.getTeacherId());
        assertEquals(2, result.getSkills().size());
        assertEquals("speaking", result.getSkills().get(0).getSkill());
        assertEquals(5, result.getSkills().get(0).getLevel());
        assertEquals("listening", result.getSkills().get(1).getSkill());
        assertEquals(4, result.getSkills().get(1).getLevel());

        verify(teacherRepository).findByIdWithUserAccount(teacherId);
        verify(teacherSkillRepository, times(2)).findSkillsByTeacherIdNative(teacherId); // Called twice: current + final
        verify(teacherSkillRepository, times(2)).insertTeacherSkill(anyLong(), anyString(), anyShort()); // Insert 2 new skills
        verify(teacherRepository).save(any(Teacher.class));
    }

    @Test
    void updateTeacherSkills_NullId_ThrowsException() {
        // Given
        UpdateTeacherSkillsRequestDTO request = createUpdateTeacherSkillsRequest();

        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.updateTeacherSkills(null, request));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    void updateTeacherSkills_InvalidId_ThrowsException() {
        // Given
        UpdateTeacherSkillsRequestDTO request = createUpdateTeacherSkillsRequest();

        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.updateTeacherSkills(0L, request));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    void updateTeacherSkills_NullRequest_ThrowsException() {
        // Given
        Long teacherId = 1L;

        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.updateTeacherSkills(teacherId, null));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    void updateTeacherSkills_EmptySkills_ThrowsException() {
        // Given
        Long teacherId = 1L;
        UpdateTeacherSkillsRequestDTO request = new UpdateTeacherSkillsRequestDTO();
        request.setSkills(Collections.emptyList());

        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.updateTeacherSkills(teacherId, request));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    void updateTeacherSkills_TeacherNotFound_ThrowsException() {
        // Given
        Long teacherId = 999L;
        UpdateTeacherSkillsRequestDTO request = createUpdateTeacherSkillsRequest();

        when(teacherRepository.findByIdWithUserAccount(teacherId)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.updateTeacherSkills(teacherId, request));
        assertEquals(ErrorCode.TEACHER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void updateTeacherSkills_InvalidSkillName_ThrowsException() {
        // Given
        Long teacherId = 1L;
        Teacher teacher = createTeacher();
        UpdateTeacherSkillsRequestDTO request = createUpdateTeacherSkillsRequest();
        request.getSkills().get(0).setSkill("INVALID_SKILL");

        when(teacherRepository.findByIdWithUserAccount(teacherId)).thenReturn(Optional.of(teacher));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.updateTeacherSkills(teacherId, request));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    void updateTeacherSkills_InvalidLevel_ThrowsException() {
        // Given
        Long teacherId = 1L;
        Teacher teacher = createTeacher();
        UpdateTeacherSkillsRequestDTO request = createUpdateTeacherSkillsRequest();
        request.getSkills().get(0).setLevel(6); // Invalid level

        when(teacherRepository.findByIdWithUserAccount(teacherId)).thenReturn(Optional.of(teacher));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.updateTeacherSkills(teacherId, request));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    private UpdateTeacherSkillsRequestDTO createUpdateTeacherSkillsRequest() {
        UpdateTeacherSkillsRequestDTO request = new UpdateTeacherSkillsRequestDTO();
        
        UpdateTeacherSkillsRequestDTO.TeacherSkillDTO skill1 = new UpdateTeacherSkillsRequestDTO.TeacherSkillDTO();
        skill1.setSkill("speaking");
        skill1.setLevel(5);
        
        UpdateTeacherSkillsRequestDTO.TeacherSkillDTO skill2 = new UpdateTeacherSkillsRequestDTO.TeacherSkillDTO();
        skill2.setSkill("listening");
        skill2.setLevel(4);
        
        request.setSkills(List.of(skill1, skill2));
        return request;
    }

    // ADD TEACHER SKILLS TESTS

    @Test
    void addTeacherSkills_ValidRequest_ReturnsUpdatedSkills() {
        // Given
        Long teacherId = 1L;
        Teacher teacher = createTeacher();
        AddTeacherSkillsRequestDTO request = createAddTeacherSkillsRequest();

        when(teacherRepository.findByIdWithUserAccount(teacherId)).thenReturn(Optional.of(teacher));
        
        // Mock current skills (empty - no existing skills)
        List<Object[]> currentSkills = Arrays.asList();
        when(teacherSkillRepository.findSkillsByTeacherIdNative(teacherId)).thenReturn(currentSkills);
        
        // Mock insert method
        doNothing().when(teacherSkillRepository).insertTeacherSkill(anyLong(), anyString(), anyShort());
        when(teacherRepository.save(any(Teacher.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Mock final result for response
        List<Object[]> finalSkills = Arrays.asList(
            new Object[]{teacherId, "speaking", 5},
            new Object[]{teacherId, "listening", 4}
        );
        when(teacherSkillRepository.findSkillsByTeacherIdNative(teacherId)).thenReturn(currentSkills, finalSkills);

        // When
        TeacherSkillsResponseDTO result = teacherService.addTeacherSkills(teacherId, request);

        // Then
        assertNotNull(result);
        assertEquals(teacherId, result.getTeacherId());
        assertEquals(2, result.getSkills().size());
        assertEquals("speaking", result.getSkills().get(0).getSkill());
        assertEquals(5, result.getSkills().get(0).getLevel());
        assertEquals("listening", result.getSkills().get(1).getSkill());
        assertEquals(4, result.getSkills().get(1).getLevel());

        verify(teacherRepository).findByIdWithUserAccount(teacherId);
        verify(teacherSkillRepository, times(2)).findSkillsByTeacherIdNative(teacherId); // Called twice: current + final
        verify(teacherSkillRepository, times(2)).insertTeacherSkill(anyLong(), anyString(), anyShort()); // Insert 2 new skills
        verify(teacherRepository).save(any(Teacher.class));
    }

    @Test
    void addTeacherSkills_DuplicateSkill_ThrowsException() {
        // Given
        Long teacherId = 1L;
        Teacher teacher = createTeacher();
        AddTeacherSkillsRequestDTO request = createAddTeacherSkillsRequest();

        when(teacherRepository.findByIdWithUserAccount(teacherId)).thenReturn(Optional.of(teacher));
        
        // Mock current skills (teacher already has speaking skill)
        List<Object[]> currentSkills = new ArrayList<>();
        currentSkills.add(new Object[]{teacherId, "speaking", 3});
        when(teacherSkillRepository.findSkillsByTeacherIdNative(teacherId)).thenReturn(currentSkills);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.addTeacherSkills(teacherId, request));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());

        verify(teacherRepository).findByIdWithUserAccount(teacherId);
        verify(teacherSkillRepository).findSkillsByTeacherIdNative(teacherId);
        verify(teacherSkillRepository, never()).insertTeacherSkill(anyLong(), anyString(), anyShort());
    }

    @Test
    void addTeacherSkills_NullId_ThrowsException() {
        // Given
        AddTeacherSkillsRequestDTO request = createAddTeacherSkillsRequest();

        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.addTeacherSkills(null, request));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    void addTeacherSkills_NullRequest_ThrowsException() {
        // Given
        Long teacherId = 1L;

        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.addTeacherSkills(teacherId, null));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    void addTeacherSkills_EmptySkills_ThrowsException() {
        // Given
        Long teacherId = 1L;
        AddTeacherSkillsRequestDTO request = new AddTeacherSkillsRequestDTO();
        request.setSkills(Collections.emptyList());

        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.addTeacherSkills(teacherId, request));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    void addTeacherSkills_TeacherNotFound_ThrowsException() {
        // Given
        Long teacherId = 999L;
        AddTeacherSkillsRequestDTO request = createAddTeacherSkillsRequest();

        when(teacherRepository.findByIdWithUserAccount(teacherId)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.addTeacherSkills(teacherId, request));
        assertEquals(ErrorCode.TEACHER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void addTeacherSkills_InvalidSkillName_ThrowsException() {
        // Given
        Long teacherId = 1L;
        Teacher teacher = createTeacher();
        AddTeacherSkillsRequestDTO request = createAddTeacherSkillsRequest();
        request.getSkills().get(0).setSkill("INVALID_SKILL");

        when(teacherRepository.findByIdWithUserAccount(teacherId)).thenReturn(Optional.of(teacher));
        when(teacherSkillRepository.findSkillsByTeacherIdNative(teacherId)).thenReturn(Collections.emptyList());

        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.addTeacherSkills(teacherId, request));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    void addTeacherSkills_InvalidLevel_ThrowsException() {
        // Given
        Long teacherId = 1L;
        Teacher teacher = createTeacher();
        AddTeacherSkillsRequestDTO request = createAddTeacherSkillsRequest();
        request.getSkills().get(0).setLevel(6); // Invalid level

        when(teacherRepository.findByIdWithUserAccount(teacherId)).thenReturn(Optional.of(teacher));
        when(teacherSkillRepository.findSkillsByTeacherIdNative(teacherId)).thenReturn(Collections.emptyList());

        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.addTeacherSkills(teacherId, request));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    private AddTeacherSkillsRequestDTO createAddTeacherSkillsRequest() {
        AddTeacherSkillsRequestDTO request = new AddTeacherSkillsRequestDTO();
        
        AddTeacherSkillsRequestDTO.TeacherSkillDTO skill1 = new AddTeacherSkillsRequestDTO.TeacherSkillDTO();
        skill1.setSkill("speaking");
        skill1.setLevel(5);
        
        AddTeacherSkillsRequestDTO.TeacherSkillDTO skill2 = new AddTeacherSkillsRequestDTO.TeacherSkillDTO();
        skill2.setSkill("listening");
        skill2.setLevel(4);
        
        request.setSkills(List.of(skill1, skill2));
        return request;
    }

    // GET TEACHER SKILLS TESTS

    @Test
    void getTeacherSkills_ValidId_ReturnsTeacherSkills() {
        // Given
        Long teacherId = 1L;
        Teacher teacher = createTeacher();
        
        List<Object[]> skillRows = List.of(
            new Object[]{teacherId, "speaking", 5},
            new Object[]{teacherId, "listening", 4},
            new Object[]{teacherId, "writing", 3}
        );

        when(teacherRepository.findByIdWithUserAccount(teacherId)).thenReturn(Optional.of(teacher));
        when(teacherSkillRepository.findSkillsByTeacherIdNative(teacherId)).thenReturn(skillRows);

        // When
        TeacherSkillsResponseDTO result = teacherService.getTeacherSkills(teacherId);

        // Then
        assertNotNull(result);
        assertEquals(teacherId, result.getTeacherId());
        assertEquals(3, result.getSkills().size());
        assertEquals("speaking", result.getSkills().get(0).getSkill());
        assertEquals(5, result.getSkills().get(0).getLevel());
        assertEquals("listening", result.getSkills().get(1).getSkill());
        assertEquals(4, result.getSkills().get(1).getLevel());
        assertEquals("writing", result.getSkills().get(2).getSkill());
        assertEquals(3, result.getSkills().get(2).getLevel());

        verify(teacherRepository).findByIdWithUserAccount(teacherId);
        verify(teacherSkillRepository).findSkillsByTeacherIdNative(teacherId);
    }

    @Test
    void getTeacherSkills_EmptySkills_ReturnsEmptyList() {
        // Given
        Long teacherId = 1L;
        Teacher teacher = createTeacher();
        
        List<Object[]> skillRows = Collections.emptyList();

        when(teacherRepository.findByIdWithUserAccount(teacherId)).thenReturn(Optional.of(teacher));
        when(teacherSkillRepository.findSkillsByTeacherIdNative(teacherId)).thenReturn(skillRows);

        // When
        TeacherSkillsResponseDTO result = teacherService.getTeacherSkills(teacherId);

        // Then
        assertNotNull(result);
        assertEquals(teacherId, result.getTeacherId());
        assertTrue(result.getSkills().isEmpty());

        verify(teacherRepository).findByIdWithUserAccount(teacherId);
        verify(teacherSkillRepository).findSkillsByTeacherIdNative(teacherId);
    }

    @Test
    void getTeacherSkills_NullId_ThrowsException() {
        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.getTeacherSkills(null));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    void getTeacherSkills_InvalidId_ThrowsException() {
        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.getTeacherSkills(0L));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    void getTeacherSkills_TeacherNotFound_ThrowsException() {
        // Given
        Long teacherId = 999L;
        when(teacherRepository.findByIdWithUserAccount(teacherId)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.getTeacherSkills(teacherId));
        assertEquals(ErrorCode.TEACHER_NOT_FOUND, exception.getErrorCode());
    }

    // REMOVE TEACHER SKILL TESTS

    @Test
    void removeTeacherSkill_ValidRequest_RemovesSuccessfully() {
        // Given
        Long teacherId = 1L;
        String skill = "speaking";
        Teacher teacher = createTeacher();

        when(teacherRepository.findByIdWithUserAccount(teacherId)).thenReturn(Optional.of(teacher));
        
        // Mock current skills to include the skill we want to remove
        List<Object[]> currentSkills = new ArrayList<>();
        currentSkills.add(new Object[]{teacherId, "speaking", 5});
        when(teacherSkillRepository.findSkillsByTeacherIdNative(teacherId)).thenReturn(currentSkills);
        
        doNothing().when(teacherSkillRepository).deleteByTeacherIdAndSkill(anyLong(), anyString());
        when(teacherRepository.save(any(Teacher.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        assertDoesNotThrow(() -> teacherService.removeTeacherSkill(teacherId, skill));

        // Then
        verify(teacherRepository).findByIdWithUserAccount(teacherId);
        verify(teacherSkillRepository).deleteByTeacherIdAndSkill(teacherId, skill);
        verify(teacherRepository).save(any(Teacher.class));
    }

    @Test
    void removeTeacherSkill_NullTeacherId_ThrowsException() {
        // Given
        String skill = "speaking";

        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.removeTeacherSkill(null, skill));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    void removeTeacherSkill_InvalidTeacherId_ThrowsException() {
        // Given
        String skill = "speaking";

        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.removeTeacherSkill(0L, skill));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    void removeTeacherSkill_NullSkill_ThrowsException() {
        // Given
        Long teacherId = 1L;

        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.removeTeacherSkill(teacherId, null));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    void removeTeacherSkill_EmptySkill_ThrowsException() {
        // Given
        Long teacherId = 1L;
        String skill = "";

        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.removeTeacherSkill(teacherId, skill));
        assertEquals(ErrorCode.INVALID_INPUT, exception.getErrorCode());
    }

    @Test
    void removeTeacherSkill_TeacherNotFound_ThrowsException() {
        // Given
        Long teacherId = 999L;
        String skill = "speaking";
        when(teacherRepository.findByIdWithUserAccount(teacherId)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class, 
            () -> teacherService.removeTeacherSkill(teacherId, skill));
        assertEquals(ErrorCode.TEACHER_NOT_FOUND, exception.getErrorCode());
    }
}
