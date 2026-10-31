package cl.ipt.siga.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Records that a student is matriculated in a period. The matriculation
 * process itself is outside SIGA IPT; the system only needs to know the result.
 */
@Entity
@Table(name = "matriculations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "period_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Matriculation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "period_id", nullable = false)
    private AcademicPeriod period;

    @Column(nullable = false)
    private LocalDate registeredAt;
}
