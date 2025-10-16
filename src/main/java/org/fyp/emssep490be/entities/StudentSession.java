package org.fyp.emssep490be.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.fyp.emssep490be.entities.enums.AttendanceStatus;
import org.fyp.emssep490be.entities.ids.StudentSessionId;

@Entity
@Table(name = "student_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentSession {

    @EmbeddedId
    private StudentSessionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("studentId")
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sessionId")
    @JoinColumn(name = "session_id", nullable = false)
    private SessionEntity session;

    @Column(name = "is_makeup")
    private Boolean isMakeup;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false)
    private AttendanceStatus attendanceStatus;

    private String note;

    @Column(name = "recorded_at")
    private OffsetDateTime recordedAt;
}
