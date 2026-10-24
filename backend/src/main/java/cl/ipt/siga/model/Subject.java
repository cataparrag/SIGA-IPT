package cl.ipt.siga.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "subjects",
        uniqueConstraints = @UniqueConstraint(columnNames = {"study_plan_id", "code"}))
@Getter
@Setter
@NoArgsConstructor
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false)
    private String name;

    /** Semester of the study plan, from 1 to 4. */
    @Column(nullable = false)
    private Integer semester;

    /** TEL: weekly hours of Theory, Exercises and Laboratory. */
    @Column(nullable = false)
    private Integer theoryHours;

    @Column(nullable = false)
    private Integer exerciseHours;

    @Column(nullable = false)
    private Integer labHours;

    @Column(nullable = false)
    private Integer sct;

    @ManyToOne(optional = false)
    @JoinColumn(name = "study_plan_id", nullable = false)
    private StudyPlan studyPlan;

    @ManyToMany
    @JoinTable(name = "subject_prerequisites",
            joinColumns = @JoinColumn(name = "subject_id"),
            inverseJoinColumns = @JoinColumn(name = "prerequisite_id"))
    private Set<Subject> prerequisites = new HashSet<>();

    public int getTotalHours() {
        return theoryHours + exerciseHours + labHours;
    }
}