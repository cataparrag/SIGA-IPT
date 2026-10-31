package cl.ipt.siga.service;

import cl.ipt.siga.dto.EnrollmentResponse;
import cl.ipt.siga.exception.BadRequestException;
import cl.ipt.siga.exception.ConflictException;
import cl.ipt.siga.exception.ForbiddenException;
import cl.ipt.siga.model.Enrollment;
import cl.ipt.siga.model.EnrollmentResult;
import cl.ipt.siga.model.Section;
import cl.ipt.siga.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GradeService {

    static final double MIN_GRADE = 1.0;
    static final double MAX_GRADE = 7.0;

    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentService enrollmentService;

    /**
     * Registers or modifies the final grade of an enrollment. Each enrollment has
     * at most one final grade, so registering again replaces it.
     */
    @Transactional
    public EnrollmentResponse registerGrade(Long enrollmentId, Long teacherId, double grade) {
        // Rule: grades only for students enrolled in the section (the enrollment must exist).
        Enrollment enrollment = enrollmentService.getEnrollment(enrollmentId);
        Section section = enrollment.getSection();

        // Rule: grades can be registered or modified only while the period is OPEN.
        if (!section.getPeriod().isOpen()) {
            throw new ConflictException("El período " + section.getPeriod().getCode()
                    + " está cerrado; no se pueden registrar ni modificar calificaciones");
        }
        // Rule: only the teacher responsible for the section can register grades.
        if (!Objects.equals(section.getTeacher().getId(), teacherId)) {
            throw new ForbiddenException("Solo el docente responsable de la sección puede registrar sus calificaciones");
        }
        // Rule: the grade is between 1.0 and 7.0, with one decimal.
        if (grade < MIN_GRADE || grade > MAX_GRADE) {
            throw new BadRequestException("La calificación debe estar entre 1,0 y 7,0");
        }
        if (Math.abs(grade * 10 - Math.round(grade * 10)) > 1e-9) {
            throw new BadRequestException("La calificación debe tener como máximo un decimal");
        }

        enrollment.setFinalGrade(grade);
        // Rule: 4.0 or higher is APPROVED, lower is FAILED.
        enrollment.setResult(EnrollmentResult.fromGrade(grade));
        return EnrollmentResponse.from(enrollmentRepository.save(enrollment));
    }
}
