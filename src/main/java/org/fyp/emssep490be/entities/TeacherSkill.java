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
import org.fyp.emssep490be.entities.ids.TeacherSkillId;

@Entity
@Table(name = "teacher_skill")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSkill {

    @EmbeddedId
    private TeacherSkillId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("teacherId")
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    private Short level;
}
