package org.fyp.emssep490be.entities.ids;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.fyp.emssep490be.entities.enums.Skill;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TeacherSkillId implements Serializable {

    @Column(name = "teacher_id")
    private Long teacherId;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill", columnDefinition = "skill_enum")
    private Skill skill;
}
