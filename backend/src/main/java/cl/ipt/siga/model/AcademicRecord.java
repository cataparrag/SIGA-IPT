package cl.ipt.siga.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One line of a student's academic record, created when a period is closed.
 * It keeps a copy of the subject data (code, name, semester, SCT) so that later
 * changes to the subject do not alter the record.
 */
@Entity
@Table(name = "academic_records")
@Getter
@Setter
@NoArgsConstructor
public class AcademicRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /** Kept to know which subject it was and to block its physical deletion. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false, length = 20)
    private String subjectCode;

    @Column(nullable = false)
    private String subjectName;

    @Column(nullable = false)
    private Integer subjectSemester;

    @Column(nullable = false)
    private Integer sct;

    @Column(nullable = false, length = 10)
    private String periodCode;

    @Column(nullable = false)
    private Double finalGrade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentResult result;
}
