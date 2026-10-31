package cl.ipt.siga.service;

import cl.ipt.siga.dto.AcademicRecordResponse;
import cl.ipt.siga.model.AcademicRecord;
import cl.ipt.siga.repository.AcademicRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicRecordService {

    private final AcademicRecordRepository academicRecordRepository;
    private final StudentService studentService;

    @Transactional(readOnly = true)
    public List<AcademicRecordResponse> findByStudent(Long studentId) {
        studentService.getStudent(studentId);
        return academicRecordRepository.findByStudentId(studentId).stream()
                .sorted(Comparator.comparing(AcademicRecord::getPeriodCode)
                        .thenComparing(AcademicRecord::getSubjectSemester)
                        .thenComparing(AcademicRecord::getSubjectCode))
                .map(AcademicRecordResponse::from)
                .toList();
    }
}
