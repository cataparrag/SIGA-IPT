package cl.ipt.siga.repository;

import cl.ipt.siga.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    boolean existsByRun(String run);

    boolean existsByEmail(String email);

    boolean existsByRunAndIdNot(String run, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<Teacher> findByEmail(String email);
}
