package cl.ipt.siga.repository;

import cl.ipt.siga.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findByStudyPlanIdOrderBySemesterAscCodeAsc(Long studyPlanId);

    boolean existsByStudyPlanIdAndCode(Long studyPlanId, String code);

    boolean existsByStudyPlanIdAndCodeAndIdNot(Long studyPlanId, String code, Long id);

    boolean existsByStudyPlanId(Long studyPlanId);

    /** Subjects that have the given subject as one of their prerequisites. */
    List<Subject> findByPrerequisitesId(Long prerequisiteId);
}