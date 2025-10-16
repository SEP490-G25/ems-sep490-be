package org.fyp.emssep490be.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "course_phase",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_course_phase_course_number", columnNames = {"course_id", "phase_number"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoursePhase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "phase_number", nullable = false)
    private Integer phaseNumber;

    private String name;

    @Column(name = "duration_weeks")
    private Integer durationWeeks;

    @Column(name = "learning_focus")
    private String learningFocus;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
