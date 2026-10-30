package cl.ipt.siga.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "sections",
        uniqueConstraints = @UniqueConstraint(columnNames = {"subject_id", "period_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(optional = false)
    @JoinColumn(name = "period_id", nullable = false)
    private AcademicPeriod period;

    @ManyToOne(optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(nullable = false)
    private Integer capacity;

    /** Multivalued attribute: schedule blocks such as "L2", "W3". */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "section_blocks", joinColumns = @JoinColumn(name = "section_id"))
    @Column(name = "block", nullable = false, length = 2)
    private Set<String> blocks = new HashSet<>();
}
