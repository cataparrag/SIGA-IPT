package cl.ipt.siga.repository;

import cl.ipt.siga.model.AcademicPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcademicPeriodRepository extends JpaRepository<AcademicPeriod, Long> {

    boolean existsByYearAndSemester(Integer year, Integer semester);

    List<AcademicPeriod> findAllByOrderByYearDescSemesterDesc();
}
