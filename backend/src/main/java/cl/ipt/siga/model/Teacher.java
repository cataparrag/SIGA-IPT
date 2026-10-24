package cl.ipt.siga.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "teachers")
@Getter
@Setter
@NoArgsConstructor
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    private String run;

    @Column(nullable = false)
    private String fullName;

    /** Also links the teacher with their Keycloak user. */
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String professionalTitle;

    @Column(nullable = false)
    private String academicDegree;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeacherStatus status;
}
