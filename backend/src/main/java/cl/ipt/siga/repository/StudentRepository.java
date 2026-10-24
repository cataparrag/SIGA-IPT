package cl.ipt.siga.repository;

import cl.ipt.siga.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByRun(String run);

    boolean existsByEmail(String email);

    boolean existsByRunAndIdNot(String run, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByCareerId(Long careerId);

    boolean existsByStudyPlanId(Long studyPlanId);

    Optional<Student> findByEmail(String email);
}
