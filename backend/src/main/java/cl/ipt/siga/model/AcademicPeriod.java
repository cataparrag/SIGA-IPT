package cl.ipt.siga.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "academic_periods",
        uniqueConstraints = @UniqueConstraint(columnNames = {"period_year", "semester"}))
@Getter
@Setter
@NoArgsConstructor
public class AcademicPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** "year" is a reserved word in some databases, so the column is period_year. */
    @Column(name = "period_year", nullable = false)
    private Integer year;

    /** 1 or 2. */
    @Column(nullable = false)
    private Integer semester;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PeriodStatus status;

    /** Code such as "2027-1". */
    public String getCode() {
        return year + "-" + semester;
    }

    public boolean isOpen() {
        return status == PeriodStatus.OPEN;
    }
}
