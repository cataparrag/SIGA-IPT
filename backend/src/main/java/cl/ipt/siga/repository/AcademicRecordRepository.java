package cl.ipt.siga.repository;

import cl.ipt.siga.model.AcademicRecord;
import cl.ipt.siga.model.EnrollmentResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcademicRecordRepository extends JpaRepository<AcademicRecord, Long> {

    List<AcademicRecord> findByStudentId(Long studentId);

    List<AcademicRecord> findByStudentIdAndResult(Long studentId, EnrollmentResult result);

    long countByStudentIdAndSubjectIdAndResult(Long studentId, Long subjectId, EnrollmentResult result);

    boolean existsByStudentId(Long studentId);

    boolean existsBySubjectId(Long subjectId);
}
