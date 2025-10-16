package org.fyp.emssep490be.entities;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.fyp.emssep490be.entities.ids.CourseSessionCloMappingId;

@Entity
@Table(name = "course_session_clo_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseSessionCloMapping {

    @EmbeddedId
    private CourseSessionCloMappingId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("courseSessionId")
    @JoinColumn(name = "course_session_id", nullable = false)
    private CourseSession courseSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("cloId")
    @JoinColumn(name = "clo_id", nullable = false)
    private Clo clo;

    private String status;
}
