package cl.ipt.siga.service;

import cl.ipt.siga.dto.MatriculationResponse;
import cl.ipt.siga.exception.ConflictException;
import cl.ipt.siga.exception.NotFoundException;
import cl.ipt.siga.model.AcademicPeriod;
import cl.ipt.siga.model.Matriculation;
import cl.ipt.siga.model.Student;
import cl.ipt.siga.repository.EnrollmentRepository;
import cl.ipt.siga.repository.MatriculationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Matriculation is managed outside SIGA IPT; the administrator only records
 * which students are matriculated in each period, because enrollment requires it.
 */
@Service
@RequiredArgsConstructor
public class MatriculationService {

    private final MatriculationRepository matriculationRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentService studentService;
    private final AcademicPeriodService periodService;

    @Transactional(readOnly = true)
    public List<MatriculationResponse> findByPeriod(Long periodId) {
        periodService.getPeriod(periodId);
        return matriculationRepository.findByPeriodId(periodId).stream()
                .map(MatriculationResponse::from)
                .toList();
    }

    @Transactional
    public MatriculationResponse create(Long periodId, Long studentId) {
        AcademicPeriod period = periodService.getPeriod(periodId);
        Student student = studentService.getStudent(studentId);
        if (!period.isOpen()) {
            throw new ConflictException("El período " + period.getCode() + " está cerrado");
        }
        if (matriculationRepository.existsByStudentIdAndPeriodId(studentId, periodId)) {
            throw new ConflictException("El estudiante ya está matriculado en el período " + period.getCode());
        }
        Matriculation matriculation = new Matriculation();
        matriculation.setStudent(student);
        matriculation.setPeriod(period);
        matriculation.setRegisteredAt(LocalDate.now());
        return MatriculationResponse.from(matriculationRepository.save(matriculation));
    }

    @Transactional
    public void delete(Long id) {
        Matriculation matriculation = matriculationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe la matrícula con id " + id));
        if (!enrollmentRepository.findByStudentIdAndSectionPeriodId(
                matriculation.getStudent().getId(), matriculation.getPeriod().getId()).isEmpty()) {
            throw new ConflictException("El estudiante tiene inscripciones en el período; no se puede eliminar su matrícula");
        }
        matriculationRepository.delete(matriculation);
    }
}
