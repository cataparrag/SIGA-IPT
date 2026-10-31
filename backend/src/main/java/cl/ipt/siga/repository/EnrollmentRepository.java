package cl.ipt.siga.repository;

import cl.ipt.siga.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    long countBySectionId(Long sectionId);

    boolean existsBySectionId(Long sectionId);

    boolean existsByStudentId(Long studentId);

    List<Enrollment> findBySectionId(Long sectionId);

    List<Enrollment> findBySectionPeriodId(Long periodId);

    List<Enrollment> findByStudentId(Long studentId);

    List<Enrollment> findByStudentIdAndSectionPeriodId(Long studentId, Long periodId);

    List<Enrollment> findBySectionPeriodIdAndFinalGradeIsNull(Long periodId);
}
