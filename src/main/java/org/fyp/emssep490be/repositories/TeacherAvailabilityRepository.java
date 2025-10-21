package org.fyp.emssep490be.repositories;

import java.util.List;
import org.fyp.emssep490be.entities.TeacherAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherAvailabilityRepository extends JpaRepository<TeacherAvailability, Long> {
    List<TeacherAvailability> findByTeacherId(Long teacherId);
}
