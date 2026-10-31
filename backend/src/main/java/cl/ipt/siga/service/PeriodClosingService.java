package cl.ipt.siga.service;

import cl.ipt.siga.dto.PeriodClosingResponse;
import cl.ipt.siga.exception.ConflictException;
import cl.ipt.siga.model.*;
import cl.ipt.siga.repository.AcademicRecordRepository;
import cl.ipt.siga.repository.EnrollmentRepository;
import cl.ipt.siga.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Closes an academic period: moves grades into the academic record, updates
 * the students' status and marks the period as CLOSED. Runs in one transaction,
 * so if anything fails nothing is saved.
 */
@Service
@RequiredArgsConstructor
public class PeriodClosingService {

    static final int FAILURES_FOR_ELIMINATION = 2;

    private final EnrollmentRepository enrollmentRepository;
    private final AcademicRecordRepository academicRecordRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicPeriodService periodService;

    @Transactional
    public PeriodClosingResponse close(Long periodId) {
        AcademicPeriod period = periodService.getPeriod(periodId);

        // Rule: only an OPEN period can be closed (and only once).
        if (!period.isOpen()) {
            throw new ConflictException("El período " + period.getCode() + " ya está cerrado");
        }

        // Rule: every enrollment must have a final grade; otherwise report the incomplete sections.
        List<Enrollment> pending = enrollmentRepository.findBySectionPeriodIdAndFinalGradeIsNull(periodId);
        if (!pending.isEmpty()) {
            Map<String, Long> pendingBySection = pending.stream()
                    .collect(Collectors.groupingBy(e -> e.getSection().getSubject().getCode(),
                            TreeMap::new, Collectors.counting()));
            String detail = pendingBySection.entrySet().stream()
                    .map(entry -> entry.getKey() + " (" + entry.getValue() + " sin nota)")
                    .collect(Collectors.joining(", "));
            throw new ConflictException("No se puede cerrar el período: hay calificaciones pendientes en las secciones " + detail);
        }

        // Rule: grades and results become part of the academic record.
        List<Enrollment> enrollments = enrollmentRepository.findBySectionPeriodId(periodId);
        Map<Long, Student> students = new LinkedHashMap<>();
        Map<Long, List<Enrollment>> enrollmentsByStudent = new HashMap<>();
        for (Enrollment enrollment : enrollments) {
            academicRecordRepository.save(toRecord(enrollment, period));
            Student student = enrollment.getStudent();
            students.put(student.getId(), student);
            enrollmentsByStudent.computeIfAbsent(student.getId(), id -> new ArrayList<>()).add(enrollment);
        }

        // Rule: update each student's academic situation.
        List<String> eliminated = new ArrayList<>();
        List<String> graduated = new ArrayList<>();
        for (Student student : students.values()) {
            if (failedSameSubjectTwice(student, enrollmentsByStudent.get(student.getId()))) {
                student.setStatus(StudentStatus.ELIMINATED);
                eliminated.add(student.getRun());
            } else if (approvedWholePlan(student)) {
                student.setStatus(StudentStatus.GRADUATED);
                graduated.add(student.getRun());
            }
        }

        // Rule: finally, the period changes from OPEN to CLOSED.
        period.setStatus(PeriodStatus.CLOSED);
        return new PeriodClosingResponse(period.getCode(), period.getStatus(), enrollments.size(), eliminated, graduated);
    }

    private AcademicRecord toRecord(Enrollment enrollment, AcademicPeriod period) {
        Subject subject = enrollment.getSection().getSubject();
        AcademicRecord record = new AcademicRecord();
        record.setStudent(enrollment.getStudent());
        record.setSubject(subject);
        // A copy of the subject data, so later changes to the subject do not alter the record.
        record.setSubjectCode(subject.getCode());
        record.setSubjectName(subject.getName());
        record.setSubjectSemester(subject.getSemester());
        record.setSct(subject.getSct());
        record.setPeriodCode(period.getCode());
        record.setFinalGrade(enrollment.getFinalGrade());
        record.setResult(enrollment.getResult());
        return record;
    }

    /** Failing the same subject for the second time means ELIMINATED. */
    private boolean failedSameSubjectTwice(Student student, List<Enrollment> enrollmentsThisPeriod) {
        for (Enrollment enrollment : enrollmentsThisPeriod) {
            if (enrollment.getResult() == EnrollmentResult.FAILED) {
                long failures = academicRecordRepository.countByStudentIdAndSubjectIdAndResult(
                        student.getId(), enrollment.getSection().getSubject().getId(), EnrollmentResult.FAILED);
                if (failures >= FAILURES_FOR_ELIMINATION) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Approving every subject of the student's plan means GRADUATED. */
    private boolean approvedWholePlan(Student student) {
        Set<Long> approved = academicRecordRepository
                .findByStudentIdAndResult(student.getId(), EnrollmentResult.APPROVED).stream()
                .map(record -> record.getSubject().getId())
                .collect(Collectors.toSet());
        List<Subject> planSubjects = subjectRepository
                .findByStudyPlanIdOrderBySemesterAscCodeAsc(student.getStudyPlan().getId());
        return !planSubjects.isEmpty()
                && planSubjects.stream().allMatch(subject -> approved.contains(subject.getId()));
    }
}
