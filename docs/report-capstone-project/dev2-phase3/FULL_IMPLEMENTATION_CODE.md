# 📝 FULL IMPLEMENTATION CODE - PHASE 3

**Purpose**: Complete implementation code for all remaining Phase 3 files
**Status**: Ready to copy-paste
**Date**: 2025-10-23

---

## ✅ FILES ALREADY COMPLETED

1. ✓ `PloCloMappingRepository.java` - DONE
2. ✓ `CourseSessionCloMappingRepository.java` - DONE
3. ✓ `ErrorCode.java` - Error codes added (1310-1389)
4. ✓ `PloServiceImpl.java` - DONE

---

## 📦 FILE 1: CloServiceImpl.java

**Path**: `src/main/java/org/fyp/emssep490be/services/clo/impl/CloServiceImpl.java`

```java
package org.fyp.emssep490be.services.clo.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.clo.CloDTO;
import org.fyp.emssep490be.dtos.clo.CreateCloRequestDTO;
import org.fyp.emssep490be.dtos.clo.MappingRequestDTO;
import org.fyp.emssep490be.dtos.plo.PloDTO;
import org.fyp.emssep490be.entities.*;
import org.fyp.emssep490be.entities.ids.CourseSessionCloMappingId;
import org.fyp.emssep490be.entities.ids.PloCloMappingId;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.*;
import org.fyp.emssep490be.services.clo.CloService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of CloService for CLO (Course Learning Outcomes) management and mapping
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CloServiceImpl implements CloService {

    private final CloRepository cloRepository;
    private final PloRepository ploRepository;
    private final CourseRepository courseRepository;
    private final CourseSessionRepository courseSessionRepository;
    private final PloCloMappingRepository ploCloMappingRepository;
    private final CourseSessionCloMappingRepository courseSessionCloMappingRepository;

    /**
     * Get all CLOs for a course with mapped PLOs
     *
     * @param courseId Course ID
     * @return List of CLOs with mappings
     */
    @Override
    @Transactional(readOnly = true)
    public List<CloDTO> getClosByCourse(Long courseId) {
        log.info("Getting CLOs for course ID: {}", courseId);

        // Validate course exists
        if (!courseRepository.existsById(courseId)) {
            log.error("Course not found with ID: {}", courseId);
            throw new CustomException(ErrorCode.COURSE_NOT_FOUND);
        }

        // Get CLOs for course
        List<Clo> clos = cloRepository.findByCourseId(courseId);

        if (clos.isEmpty()) {
            log.warn("No CLOs found for course ID: {}", courseId);
        }

        // Convert to DTOs with mappings
        return clos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new CLO
     *
     * @param courseId Course ID
     * @param request CLO creation request
     * @return Created CLO DTO
     */
    @Override
    public CloDTO createClo(Long courseId, CreateCloRequestDTO request) {
        log.info("Creating CLO for course ID: {} with code: {}", courseId, request.getCode());

        // Validate course exists
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.error("Course not found with ID: {}", courseId);
                    return new CustomException(ErrorCode.COURSE_NOT_FOUND);
                });

        // Validate code unique for course
        if (cloRepository.existsByCodeAndCourseId(request.getCode(), courseId)) {
            log.error("CLO code '{}' already exists for course ID: {}", request.getCode(), courseId);
            throw new CustomException(ErrorCode.CLO_CODE_DUPLICATE);
        }

        // Validate code format (uppercase, numbers, dashes only)
        if (!request.getCode().matches("^[A-Z0-9-]+$")) {
            log.error("Invalid CLO code format: {}", request.getCode());
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        // Validate description not empty
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            log.error("CLO description cannot be empty");
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        // Create CLO entity
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Clo clo = new Clo();
        clo.setCourse(course);
        clo.setCode(request.getCode());
        clo.setDescription(request.getDescription());
        clo.setCreatedAt(now);
        clo.setUpdatedAt(now);

        // Save and return
        Clo savedClo = cloRepository.save(clo);
        log.info("CLO created successfully with ID: {}", savedClo.getId());

        return convertToDTO(savedClo);
    }

    /**
     * Map a PLO to a CLO - CRITICAL VALIDATION
     * PLO and CLO must belong to the same subject
     *
     * @param ploId PLO ID
     * @param cloId CLO ID
     * @param request Mapping request with status
     * @return Mapping confirmation
     */
    @Override
    public Map<String, Object> mapPloToClo(Long ploId, Long cloId, MappingRequestDTO request) {
        log.info("Mapping PLO ID: {} to CLO ID: {}", ploId, cloId);

        // Load PLO entity
        Plo plo = ploRepository.findById(ploId)
                .orElseThrow(() -> {
                    log.error("PLO not found with ID: {}", ploId);
                    return new CustomException(ErrorCode.PLO_NOT_FOUND);
                });

        // Load CLO entity
        Clo clo = cloRepository.findById(cloId)
                .orElseThrow(() -> {
                    log.error("CLO not found with ID: {}", cloId);
                    return new CustomException(ErrorCode.CLO_NOT_FOUND);
                });

        // CRITICAL VALIDATION: PLO and CLO must belong to the same subject
        Long ploSubjectId = plo.getSubject().getId();
        Long cloSubjectId = clo.getCourse().getSubject().getId();

        if (!ploSubjectId.equals(cloSubjectId)) {
            log.error("PLO subject ID {} does not match CLO subject ID {}",
                    ploSubjectId, cloSubjectId);
            throw new CustomException(ErrorCode.PLO_CLO_SUBJECT_MISMATCH);
        }

        // Check if mapping already exists
        if (ploCloMappingRepository.existsByPloIdAndCloId(ploId, cloId)) {
            log.error("Mapping already exists between PLO ID: {} and CLO ID: {}", ploId, cloId);
            throw new CustomException(ErrorCode.PLO_CLO_MAPPING_ALREADY_EXISTS);
        }

        // Create mapping
        PloCloMappingId mappingId = new PloCloMappingId(ploId, cloId);
        PloCloMapping mapping = new PloCloMapping();
        mapping.setId(mappingId);
        mapping.setPlo(plo);
        mapping.setClo(clo);
        mapping.setStatus(request.getStatus() != null ? request.getStatus() : "active");

        ploCloMappingRepository.save(mapping);
        log.info("PLO-CLO mapping created successfully");

        // Return response
        Map<String, Object> response = new HashMap<>();
        response.put("ploId", ploId);
        response.put("cloId", cloId);
        response.put("status", mapping.getStatus());
        response.put("message", "PLO-CLO mapping created successfully");

        return response;
    }

    /**
     * Map a CLO to a CourseSession - CRITICAL VALIDATION
     * CLO and CourseSession must belong to the same course
     *
     * @param sessionId CourseSession ID
     * @param cloId CLO ID
     * @param request Mapping request with status
     * @return Mapping confirmation
     */
    @Override
    public Map<String, Object> mapCloToSession(Long sessionId, Long cloId, MappingRequestDTO request) {
        log.info("Mapping CLO ID: {} to CourseSession ID: {}", cloId, sessionId);

        // Load CourseSession entity
        CourseSession courseSession = courseSessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    log.error("CourseSession not found with ID: {}", sessionId);
                    return new CustomException(ErrorCode.COURSE_SESSION_NOT_FOUND);
                });

        // Load CLO entity
        Clo clo = cloRepository.findById(cloId)
                .orElseThrow(() -> {
                    log.error("CLO not found with ID: {}", cloId);
                    return new CustomException(ErrorCode.CLO_NOT_FOUND);
                });

        // CRITICAL VALIDATION: CLO and CourseSession must belong to the same course
        Long sessionCourseId = courseSession.getPhase().getCourse().getId();
        Long cloCourseId = clo.getCourse().getId();

        if (!sessionCourseId.equals(cloCourseId)) {
            log.error("CourseSession course ID {} does not match CLO course ID {}",
                    sessionCourseId, cloCourseId);
            throw new CustomException(ErrorCode.CLO_SESSION_COURSE_MISMATCH);
        }

        // Check if mapping already exists
        if (courseSessionCloMappingRepository.existsByCourseSessionIdAndCloId(sessionId, cloId)) {
            log.error("Mapping already exists between CourseSession ID: {} and CLO ID: {}", sessionId, cloId);
            throw new CustomException(ErrorCode.CLO_SESSION_MAPPING_ALREADY_EXISTS);
        }

        // Create mapping
        CourseSessionCloMappingId mappingId = new CourseSessionCloMappingId(sessionId, cloId);
        CourseSessionCloMapping mapping = new CourseSessionCloMapping();
        mapping.setId(mappingId);
        mapping.setCourseSession(courseSession);
        mapping.setClo(clo);
        mapping.setStatus(request.getStatus() != null ? request.getStatus() : "active");

        courseSessionCloMappingRepository.save(mapping);
        log.info("CLO-Session mapping created successfully");

        // Return response
        Map<String, Object> response = new HashMap<>();
        response.put("courseSessionId", sessionId);
        response.put("cloId", cloId);
        response.put("status", mapping.getStatus());
        response.put("message", "CLO-Session mapping created successfully");

        return response;
    }

    /**
     * Delete a CLO
     *
     * @param courseId Course ID
     * @param id CLO ID
     */
    @Override
    public void deleteClo(Long courseId, Long id) {
        log.info("Deleting CLO ID: {} for course ID: {}", id, courseId);

        // Validate CLO exists and belongs to course
        Clo clo = cloRepository.findByIdAndCourseId(id, courseId)
                .orElseThrow(() -> {
                    log.error("CLO not found with ID: {} for course ID: {}", id, courseId);
                    return new CustomException(ErrorCode.CLO_NOT_FOUND);
                });

        // Check if CLO has PLO mappings
        long ploMappingCount = ploCloMappingRepository.countByCloId(id);
        if (ploMappingCount > 0) {
            log.error("Cannot delete CLO ID: {} - has {} PLO mappings", id, ploMappingCount);
            throw new CustomException(ErrorCode.CLO_HAS_MAPPINGS);
        }

        // Check if CLO has Session mappings
        long sessionMappingCount = courseSessionCloMappingRepository.countByCloId(id);
        if (sessionMappingCount > 0) {
            log.error("Cannot delete CLO ID: {} - has {} Session mappings", id, sessionMappingCount);
            throw new CustomException(ErrorCode.CLO_HAS_MAPPINGS);
        }

        // Delete CLO
        cloRepository.delete(clo);
        log.info("CLO deleted successfully with ID: {}", id);
    }

    /**
     * Convert CLO entity to DTO with mapped PLOs and session count
     *
     * @param clo CLO entity
     * @return CLO DTO with mappings
     */
    private CloDTO convertToDTO(Clo clo) {
        CloDTO dto = new CloDTO();
        dto.setId(clo.getId());
        dto.setCourseId(clo.getCourse().getId());
        dto.setCode(clo.getCode());
        dto.setDescription(clo.getDescription());

        // Get mapped PLOs
        List<PloCloMapping> ploMappings = ploCloMappingRepository.findByCloId(clo.getId());
        List<PloDTO> mappedPlos = ploMappings.stream()
                .map(mapping -> {
                    Plo plo = mapping.getPlo();
                    PloDTO ploDTO = new PloDTO();
                    ploDTO.setId(plo.getId());
                    ploDTO.setSubjectId(plo.getSubject().getId());
                    ploDTO.setCode(plo.getCode());
                    ploDTO.setDescription(plo.getDescription());
                    return ploDTO;
                })
                .collect(Collectors.toList());

        dto.setMappedPlos(mappedPlos);

        // Get mapped sessions count
        long mappedSessionsCount = courseSessionCloMappingRepository.countByCloId(clo.getId());
        dto.setMappedSessionsCount((int) mappedSessionsCount);

        return dto;
    }
}
```

---

## 📦 FILE 2: CourseMaterialServiceImpl.java

**Path**: `src/main/java/org/fyp/emssep490be/services/coursematerial/impl/CourseMaterialServiceImpl.java`

```java
package org.fyp.emssep490be.services.coursematerial.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fyp.emssep490be.dtos.coursematerial.CourseMaterialDTO;
import org.fyp.emssep490be.dtos.coursematerial.UploadMaterialRequestDTO;
import org.fyp.emssep490be.entities.Course;
import org.fyp.emssep490be.entities.CourseMaterial;
import org.fyp.emssep490be.entities.CoursePhase;
import org.fyp.emssep490be.entities.CourseSession;
import org.fyp.emssep490be.entities.UserAccount;
import org.fyp.emssep490be.exceptions.CustomException;
import org.fyp.emssep490be.exceptions.ErrorCode;
import org.fyp.emssep490be.repositories.*;
import org.fyp.emssep490be.services.coursematerial.CourseMaterialService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Implementation of CourseMaterialService for course material management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CourseMaterialServiceImpl implements CourseMaterialService {

    private final CourseMaterialRepository courseMaterialRepository;
    private final CourseRepository courseRepository;
    private final CoursePhaseRepository coursePhaseRepository;
    private final CourseSessionRepository courseSessionRepository;
    private final UserAccountRepository userAccountRepository;

    /**
     * Upload a course material
     *
     * @param courseId Course ID
     * @param request Upload material request
     * @return Created material DTO
     */
    @Override
    public CourseMaterialDTO uploadMaterial(Long courseId, UploadMaterialRequestDTO request) {
        log.info("Uploading material for course ID: {}", courseId);

        // Validate course exists
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.error("Course not found with ID: {}", courseId);
                    return new CustomException(ErrorCode.COURSE_NOT_FOUND);
                });

        // Validate request
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            log.error("Material title cannot be empty");
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        if (request.getUrl() == null || request.getUrl().trim().isEmpty()) {
            log.error("Material URL cannot be empty");
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        // Business rule: Must have at least one context (course, phase, or session)
        // For MVP: courseId is always provided, phaseId and sessionId are optional

        // Validate optional phase reference
        CoursePhase phase = null;
        if (request.getPhaseId() != null) {
            phase = coursePhaseRepository.findById(request.getPhaseId())
                    .orElseThrow(() -> {
                        log.error("CoursePhase not found with ID: {}", request.getPhaseId());
                        return new CustomException(ErrorCode.PHASE_NOT_FOUND);
                    });

            // Validate phase belongs to course
            if (!phase.getCourse().getId().equals(courseId)) {
                log.error("CoursePhase ID {} does not belong to Course ID {}",
                         request.getPhaseId(), courseId);
                throw new CustomException(ErrorCode.INVALID_INPUT);
            }
        }

        // Validate optional session reference
        CourseSession session = null;
        if (request.getCourseSessionId() != null) {
            session = courseSessionRepository.findById(request.getCourseSessionId())
                    .orElseThrow(() -> {
                        log.error("CourseSession not found with ID: {}", request.getCourseSessionId());
                        return new CustomException(ErrorCode.COURSE_SESSION_NOT_FOUND);
                    });

            // Validate session belongs to course (through phase)
            if (!session.getPhase().getCourse().getId().equals(courseId)) {
                log.error("CourseSession ID {} does not belong to Course ID {}",
                         request.getCourseSessionId(), courseId);
                throw new CustomException(ErrorCode.INVALID_INPUT);
            }
        }

        // Get current user from SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        UserAccount user = userAccountRepository.findByEmail(username)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", username);
                    return new CustomException(ErrorCode.USER_NOT_FOUND);
                });

        // Create CourseMaterial entity
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        CourseMaterial material = new CourseMaterial();
        material.setCourse(course);
        material.setPhase(phase); // Can be null
        material.setCourseSession(session); // Can be null
        material.setTitle(request.getTitle());
        material.setUrl(request.getUrl());
        material.setUploadedBy(user);
        material.setCreatedAt(now);
        material.setUpdatedAt(now);

        // Save and return
        CourseMaterial savedMaterial = courseMaterialRepository.save(material);
        log.info("Course material uploaded successfully with ID: {}", savedMaterial.getId());

        return convertToDTO(savedMaterial);
    }

    /**
     * Delete a course material
     *
     * @param courseId Course ID
     * @param id Material ID
     */
    @Override
    public void deleteMaterial(Long courseId, Long id) {
        log.info("Deleting course material ID: {} for course ID: {}", id, courseId);

        // Validate material exists and belongs to course
        CourseMaterial material = courseMaterialRepository.findByIdAndCourseId(id, courseId)
                .orElseThrow(() -> {
                    log.error("Course material not found with ID: {} for course ID: {}", id, courseId);
                    return new CustomException(ErrorCode.COURSE_MATERIAL_NOT_FOUND);
                });

        // TODO: File cleanup
        // For MVP: URL is stored as-is, no actual file deletion needed
        // For Production with S3: Add file deletion logic here

        // Delete database record
        courseMaterialRepository.delete(material);
        log.info("Course material deleted successfully with ID: {}", id);
    }

    /**
     * Convert CourseMaterial entity to DTO
     *
     * @param material CourseMaterial entity
     * @return CourseMaterial DTO
     */
    private CourseMaterialDTO convertToDTO(CourseMaterial material) {
        CourseMaterialDTO dto = new CourseMaterialDTO();
        dto.setId(material.getId());
        dto.setCourseId(material.getCourse().getId());
        dto.setPhaseId(material.getPhase() != null ? material.getPhase().getId() : null);
        dto.setCourseSessionId(material.getCourseSession() != null ? material.getCourseSession().getId() : null);
        dto.setTitle(material.getTitle());
        dto.setUrl(material.getUrl());
        dto.setUploadedBy(material.getUploadedBy().getId());
        dto.setCreatedAt(material.getCreatedAt());

        return dto;
    }
}
```

---

## 🎮 CONTROLLER IMPLEMENTATIONS

Controllers should be implemented by reading existing stub files and replacing method bodies. Here are the patterns:

### Pattern for all controllers:
```java
@RestController
@RequestMapping("/api/...")
@RequiredArgsConstructor
@Validated
public class XxxController {
    private final XxxService xxxService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUBJECT_LEADER', 'ACADEMIC_STAFF')")
    public ResponseEntity<ResponseObject<List<XxxDTO>>> getXxx(...) {
        List<XxxDTO> result = xxxService.getXxx(...);
        return ResponseEntity.ok(
            ResponseObject.<List<XxxDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Retrieved successfully")
                .data(result)
                .build()
        );
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUBJECT_LEADER')")
    public ResponseEntity<ResponseObject<XxxDTO>> createXxx(@Valid @RequestBody CreateXxxRequestDTO request) {
        XxxDTO created = xxxService.createXxx(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ResponseObject.<XxxDTO>builder()
                .status(HttpStatus.CREATED.value())
                .message("Created successfully")
                .data(created)
                .build()
            );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUBJECT_LEADER')")
    public ResponseEntity<ResponseObject<Void>> deleteXxx(@PathVariable Long id) {
        xxxService.deleteXxx(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .body(ResponseObject.<Void>builder()
                .status(HttpStatus.NO_CONTENT.value())
                .message("Deleted successfully")
                .build()
            );
    }
}
```

---

## 🧪 TESTING STRATEGY

### Run implementation test first:
```bash
# Build to check compilation errors
./mvnw clean compile

# If successful, run tests
./mvnw test

# Check specific service tests
./mvnw test -Dtest=PloServiceImplTest
./mvnw test -Dtest=CloServiceImplTest
```

### Unit tests to write (50+ tests total):
1. **PloServiceImplTest** (15-18 tests) - Follow existing patterns from Phase 1-2
2. **CloServiceImplTest** (20-25 tests) - Focus on CRITICAL validation tests
3. **CourseMaterialServiceImplTest** (12-15 tests) - Context validation tests

---

## 📋 COMPLETION CHECKLIST

After implementing all files above:

- [ ] Copy-paste CloServiceImpl.java
- [ ] Copy-paste CourseMaterialServiceImpl.java
- [ ] Implement PloController (3 endpoints)
- [ ] Implement CloController (5 endpoints)
- [ ] Implement CourseMaterialController (2 endpoints)
- [ ] Run `./mvnw clean compile` - fix any compilation errors
- [ ] Write unit tests (reference existing test patterns)
- [ ] Run `./mvnw test` - all tests should pass
- [ ] Generate coverage report: `./mvnw test jacoco:report`
- [ ] Check coverage: `target/site/jacoco/index.html` (target > 90%)
- [ ] Manual test via Swagger UI
- [ ] Update work-division-plan.md - mark Phase 3 COMPLETED

---

## ⚠️ CRITICAL VALIDATIONS TO VERIFY

1. **PLO-CLO Subject Match**:
   - Test: Create Subject1, PLO1, Course1(Subject1), CLO1
   - Map PLO1 to CLO1 → SUCCESS ✓
   - Create Subject2, Course2(Subject2), CLO2
   - Map PLO1 to CLO2 → EXCEPTION (PLO_CLO_SUBJECT_MISMATCH) ✓

2. **CLO-Session Course Match**:
   - Test: Create Course1, Phase1, Session1, CLO1
   - Map CLO1 to Session1 → SUCCESS ✓
   - Create Course2, Phase2, Session2
   - Map CLO1 to Session2 → EXCEPTION (CLO_SESSION_COURSE_MISMATCH) ✓

3. **Cannot Delete with Mappings**:
   - Create PLO1 with CLO1 mapping → Delete PLO1 → EXCEPTION ✓
   - Create CLO1 with Session mapping → Delete CLO1 → EXCEPTION ✓

---

## 🚀 ESTIMATED COMPLETION TIME

- CloServiceImpl: Already provided above (copy-paste 5 min)
- CourseMaterialServiceImpl: Already provided above (copy-paste 5 min)
- Controllers (3 files): 1-2 hours (follow patterns)
- Unit Tests: 3-4 hours (most time-consuming)
- Test & Fix: 1-2 hours
- **TOTAL**: 5-8 hours remaining

---

**Document Status**: READY TO USE
**Last Updated**: 2025-10-23
**Next Action**: Copy-paste service implementations and start controller implementations
