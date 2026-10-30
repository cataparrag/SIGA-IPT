package cl.ipt.siga.repository;

import cl.ipt.siga.model.PeriodStatus;
import cl.ipt.siga.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {

    List<Section> findByPeriodId(Long periodId);

    List<Section> findByTeacherId(Long teacherId);

    List<Section> findByTeacherIdAndPeriodId(Long teacherId, Long periodId);

    boolean existsBySubjectIdAndPeriodId(Long subjectId, Long periodId);

    boolean existsBySubjectId(Long subjectId);

    boolean existsByPeriodId(Long periodId);

    boolean existsByTeacherId(Long teacherId);

    boolean existsByTeacherIdAndPeriodStatus(Long teacherId, PeriodStatus status);
}
