package cl.ipt.siga.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A student's enrollment in a section. The subject and period are reached
 * through the section (section -> subject, section -> period).
 */
@Entity
@Table(name = "enrollments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "section_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Column(nullable = false)
    private LocalDateTime enrollmentDate;

    /** Null until the teacher registers it. */
    private Double finalGrade;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EnrollmentResult result;

    public boolean hasGrade() {
        return finalGrade != null;
    }
}
