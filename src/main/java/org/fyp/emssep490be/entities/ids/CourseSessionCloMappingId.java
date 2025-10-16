package org.fyp.emssep490be.entities.ids;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CourseSessionCloMappingId implements Serializable {

    @Column(name = "course_session_id")
    private Long courseSessionId;

    @Column(name = "clo_id")
    private Long cloId;
}
