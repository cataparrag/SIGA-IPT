package cl.ipt.siga.service;

import cl.ipt.siga.dto.EnrollmentRequest;
import cl.ipt.siga.dto.EnrollmentResponse;
import cl.ipt.siga.dto.SectionResponse;
import cl.ipt.siga.exception.BadRequestException;
import cl.ipt.siga.exception.ConflictException;
import cl.ipt.siga.exception.NotFoundException;
import cl.ipt.siga.model.*;
import cl.ipt.siga.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    static final int MIN_SUBJECTS_PER_PERIOD = 3;
    static final int MAX_SUBJECTS_PER_PERIOD = 6;

    private final EnrollmentRepository enrollmentRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final MatriculationRepository matriculationRepository;
    private final AcademicRecordRepository academicRecordRepository;
    private final StudentService studentService;
    private final AcademicPeriodService periodService;

    // ---------------------------------------------------------------- queries

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findByStudent(Long studentId, Long periodId) {
        studentService.getStudent(studentId);
        List<Enrollment> enrollments = periodId == null
                ? enrollmentRepository.findByStudentId(studentId)
                : enrollmentRepository.findByStudentIdAndSectionPeriodId(studentId, periodId);
        return enrollments.stream().map(EnrollmentResponse::from).toList();
    }

    /** Students enrolled in a section (used by its teacher). */
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findBySection(Long sectionId) {
        if (!sectionRepository.existsById(sectionId)) {
            throw new NotFoundException("No existe la sección con id " + sectionId);
        }
        return enrollmentRepository.findBySectionId(sectionId).stream().map(EnrollmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> findByPeriod(Long periodId) {
        periodService.getPeriod(periodId);
        return enrollmentRepository.findBySectionPeriodId(periodId).stream().map(EnrollmentResponse::from).toList();
    }

    /**
     * Sections the student could choose: subjects of their plan offered in the
     * period, not approved yet, with prerequisites approved and not already enrolled.
     */
    @Transactional(readOnly = true)
    public List<SectionResponse> findAvailableSections(Long studentId, Long periodId) {
        Student student = studentService.getStudent(studentId);
        periodService.getPeriod(periodId);
        Set<Long> approved = approvedSubjectIds(student);
        Set<Long> enrolled = enrolledSubjectIds(enrollmentRepository.findByStudentIdAndSectionPeriodId(studentId, periodId));
        return sectionRepository.findByPeriodId(periodId).stream()
                .filter(section -> belongsToPlan(section.getSubject(), student))
                .filter(section -> !approved.contains(section.getSubject().getId()))
                .filter(section -> !enrolled.contains(section.getSubject().getId()))
                .filter(section -> missingPrerequisites(section.getSubject(), approved).isEmpty())
                .sorted(Comparator.comparing((Section s) -> s.getSubject().getSemester())
                        .thenComparing(s -> s.getSubject().getCode()))
                .map(section -> SectionResponse.from(section, enrollmentRepository.countBySectionId(section.getId())))
                .toList();
    }

    // ---------------------------------------------------------------- enrollment

    @Transactional
    public List<EnrollmentResponse> enroll(Long studentId, EnrollmentRequest request) {
        Student student = studentService.getStudent(studentId);
        AcademicPeriod period = periodService.getPeriod(request.periodId());

        // Rule: the student must be REGULAR and matriculated in the period.
        if (student.getStatus() != StudentStatus.REGULAR) {
            throw new ConflictException("El estudiante está " + student.getStatus() + "; solo los estudiantes REGULAR pueden inscribir");
        }
        if (!matriculationRepository.existsByStudentIdAndPeriodId(student.getId(), period.getId())) {
            throw new ConflictException("El estudiante no está matriculado en el período " + period.getCode());
        }
        // Rule: enrollments only while the period is OPEN.
        requireOpen(period);

        Set<Long> sectionIds = new LinkedHashSet<>(request.sectionIds());
        if (sectionIds.size() != request.sectionIds().size()) {
            throw new BadRequestException("La selección tiene secciones repetidas");
        }

        List<Enrollment> current = enrollmentRepository.findByStudentIdAndSectionPeriodId(student.getId(), period.getId());
        Set<Long> currentSubjectIds = enrolledSubjectIds(current);
        Set<Long> approved = approvedSubjectIds(student);

        List<Section> selected = new ArrayList<>();
        Set<Long> selectedSubjectIds = new HashSet<>();
        for (Long sectionId : sectionIds) {
            Section section = sectionRepository.findById(sectionId)
                    .orElseThrow(() -> new NotFoundException("No existe la sección con id " + sectionId));
            Subject subject = section.getSubject();

            // Rule: only subjects of the student's plan offered in this period.
            if (!Objects.equals(section.getPeriod().getId(), period.getId())) {
                throw new BadRequestException("La sección de " + subject.getCode() + " no pertenece al período " + period.getCode());
            }
            if (!belongsToPlan(subject, student)) {
                throw new BadRequestException(subject.getCode() + " no pertenece al plan de estudios del estudiante");
            }
            // Rule: an approved subject cannot be taken again (a failed one can).
            if (approved.contains(subject.getId())) {
                throw new ConflictException("El estudiante ya aprobó " + subject.getCode());
            }
            // Rule: a subject only once per period.
            if (currentSubjectIds.contains(subject.getId()) || !selectedSubjectIds.add(subject.getId())) {
                throw new ConflictException(subject.getCode() + " ya está inscrita en el período " + period.getCode());
            }
            // Rule: all prerequisites approved.
            List<String> missing = missingPrerequisites(subject, approved);
            if (!missing.isEmpty()) {
                throw new BadRequestException("Para inscribir " + subject.getCode() + " debe aprobar antes: "
                        + String.join(", ", missing));
            }
            // Rule: the section must have seats available.
            if (!hasSeats(section)) {
                throw new ConflictException("La sección de " + subject.getCode() + " no tiene cupos disponibles");
            }
            selected.add(section);
        }

        // Rule: between 3 and 6 subjects per period (fewer if fewer remain in the plan).
        int total = current.size() + selected.size();
        if (total > MAX_SUBJECTS_PER_PERIOD) {
            throw new BadRequestException("Se pueden inscribir como máximo " + MAX_SUBJECTS_PER_PERIOD
                    + " asignaturas por período (quedaría con " + total + ")");
        }
        int minimum = Math.min(MIN_SUBJECTS_PER_PERIOD, pendingSubjectsInPlan(student, approved));
        if (total < minimum) {
            throw new BadRequestException("Debe inscribir al menos " + minimum
                    + " asignaturas en el período (quedaría con " + total + ")");
        }

        // Rule: no schedule conflicts among the sections (new and already enrolled).
        List<Section> allSections = new ArrayList<>(current.stream().map(Enrollment::getSection).toList());
        allSections.addAll(selected);
        checkScheduleConflicts(allSections);

        // Rule: no higher-semester subjects while lower-semester ones are pending and available.
        Set<Long> takenSubjectIds = new HashSet<>(currentSubjectIds);
        takenSubjectIds.addAll(selectedSubjectIds);
        checkSemesterOrder(student, period, approved, takenSubjectIds, selected);

        // Every enrollment records the date it was made.
        LocalDateTime now = LocalDateTime.now();
        List<EnrollmentResponse> result = new ArrayList<>();
        for (Section section : selected) {
            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setSection(section);
            enrollment.setEnrollmentDate(now);
            result.add(EnrollmentResponse.from(enrollmentRepository.save(enrollment)));
        }
        return result;
    }

    /** Cancelling an enrollment frees its seat. */
    @Transactional
    public void cancel(Long enrollmentId) {
        Enrollment enrollment = getEnrollment(enrollmentId);
        // Rule: cancellations only while the period is OPEN.
        requireOpen(enrollment.getSection().getPeriod());
        if (enrollment.hasGrade()) {
            throw new ConflictException("La inscripción ya tiene calificación final y no puede anularse");
        }
        enrollmentRepository.delete(enrollment);
    }

    public Enrollment getEnrollment(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe la inscripción con id " + id));
    }

    // ---------------------------------------------------------------- helpers

    private void requireOpen(AcademicPeriod period) {
        if (!period.isOpen()) {
            throw new ConflictException("El período " + period.getCode() + " está cerrado");
        }
    }

    private boolean belongsToPlan(Subject subject, Student student) {
        return Objects.equals(subject.getStudyPlan().getId(), student.getStudyPlan().getId());
    }

    private boolean hasSeats(Section section) {
        return enrollmentRepository.countBySectionId(section.getId()) < section.getCapacity();
    }

    private Set<Long> approvedSubjectIds(Student student) {
        return academicRecordRepository.findByStudentIdAndResult(student.getId(), EnrollmentResult.APPROVED).stream()
                .map(record -> record.getSubject().getId())
                .collect(Collectors.toSet());
    }

    private Set<Long> enrolledSubjectIds(List<Enrollment> enrollments) {
        return enrollments.stream()
                .map(enrollment -> enrollment.getSection().getSubject().getId())
                .collect(Collectors.toSet());
    }

    private List<String> missingPrerequisites(Subject subject, Set<Long> approved) {
        return subject.getPrerequisites().stream()
                .filter(prerequisite -> !approved.contains(prerequisite.getId()))
                .map(Subject::getCode)
                .sorted()
                .toList();
    }

    private int pendingSubjectsInPlan(Student student, Set<Long> approved) {
        return (int) subjectRepository.findByStudyPlanIdOrderBySemesterAscCodeAsc(student.getStudyPlan().getId()).stream()
                .filter(subject -> !approved.contains(subject.getId()))
                .count();
    }

    /** Two sections conflict when they share a day and module. */
    private void checkScheduleConflicts(List<Section> sections) {
        Map<String, String> usedBlocks = new HashMap<>();
        for (Section section : sections) {
            for (String block : section.getBlocks()) {
                String other = usedBlocks.putIfAbsent(block, section.getSubject().getCode());
                if (other != null) {
                    throw new ConflictException("Las secciones de " + other + " y " + section.getSubject().getCode()
                            + " tienen choque de horario en el bloque " + block);
                }
            }
        }
    }

    /**
     * A subject is "pending and available" when it belongs to the plan, is offered in
     * the period with free seats, is not approved, has its prerequisites approved and
     * is not being taken. The student cannot take a subject from a higher semester
     * while one of those exists in a lower semester.
     */
    private void checkSemesterOrder(Student student, AcademicPeriod period, Set<Long> approved,
                                    Set<Long> takenSubjectIds, List<Section> selected) {
        List<Subject> availablePending = sectionRepository.findByPeriodId(period.getId()).stream()
                .filter(section -> belongsToPlan(section.getSubject(), student))
                .filter(section -> !approved.contains(section.getSubject().getId()))
                .filter(section -> !takenSubjectIds.contains(section.getSubject().getId()))
                .filter(section -> missingPrerequisites(section.getSubject(), approved).isEmpty())
                .filter(this::hasSeats)
                .map(Section::getSubject)
                .toList();

        for (Section section : selected) {
            Subject subject = section.getSubject();
            for (Subject pending : availablePending) {
                if (pending.getSemester() < subject.getSemester()) {
                    throw new BadRequestException("No puede inscribir " + subject.getCode() + " (semestre "
                            + subject.getSemester() + ") mientras tenga pendiente " + pending.getCode()
                            + " (semestre " + pending.getSemester() + "), que está disponible en este período");
                }
            }
        }
    }
}
