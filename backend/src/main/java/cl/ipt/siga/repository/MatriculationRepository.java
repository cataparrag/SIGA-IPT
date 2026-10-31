package cl.ipt.siga.repository;

import cl.ipt.siga.model.Matriculation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatriculationRepository extends JpaRepository<Matriculation, Long> {

    boolean existsByStudentIdAndPeriodId(Long studentId, Long periodId);

    List<Matriculation> findByPeriodId(Long periodId);

    boolean existsByStudentId(Long studentId);
}
