package cl.ipt.siga.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "study_plans",
        uniqueConstraints = @UniqueConstraint(columnNames = {"career_id", "identifier"}))
@Getter
@Setter
@NoArgsConstructor
public class StudyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identifier such as "2021.3", unique within its career. */
    @Column(nullable = false, length = 20)
    private String identifier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudyPlanStatus status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "career_id", nullable = false)
    private Career career;
}