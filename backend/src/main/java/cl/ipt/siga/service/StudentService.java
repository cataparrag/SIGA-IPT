package cl.ipt.siga.service;

import cl.ipt.siga.dto.StudentRequest;
import cl.ipt.siga.dto.StudentResponse;
import cl.ipt.siga.exception.BadRequestException;
import cl.ipt.siga.exception.ConflictException;
import cl.ipt.siga.exception.NotFoundException;
import cl.ipt.siga.model.Career;
import cl.ipt.siga.model.CareerStatus;
import cl.ipt.siga.model.Student;
import cl.ipt.siga.model.StudentStatus;
import cl.ipt.siga.model.StudyPlan;
import cl.ipt.siga.model.StudyPlanStatus;
import cl.ipt.siga.repository.AcademicRecordRepository;
import cl.ipt.siga.repository.EnrollmentRepository;
import cl.ipt.siga.repository.StudentRepository;
import cl.ipt.siga.repository.StudyPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AcademicRecordRepository academicRecordRepository;
    private final CareerService careerService;
    private final StudyPlanService studyPlanService;

    @Transactional(readOnly = true)
    public List<StudentResponse> findAll() {
        return studentRepository.findAll().stream()
                .map(StudentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse findById(Long id) {
        return StudentResponse.from(getStudent(id));
    }

    @Transactional
    public StudentResponse create(StudentRequest request) {
        // Rule: RUN and email are unique in the system.
        if (studentRepository.existsByRun(request.run())) {
            throw new ConflictException("Ya existe un estudiante con el RUN " + request.run());
        }
        if (studentRepository.existsByEmail(request.email())) {
            throw new ConflictException("Ya existe un estudiante con el correo " + request.email());
        }

        // Rule: the student belongs to an existing career, which must be ACTIVE.
        Career career = careerService.getCareer(request.careerId());
        if (career.getStatus() == CareerStatus.INACTIVE) {
            throw new ConflictException("La carrera " + career.getCode() + " está inactiva y no recibe nuevos estudiantes");
        }

        Student student = new Student();
        student.setRun(request.run());
        student.setFullName(request.fullName());
        student.setEmail(request.email());
        student.setCareer(career);
        // Rule: the student is assigned the current plan of the career.
        student.setStudyPlan(getCurrentPlan(career));
        // Rule: every new student starts as REGULAR.
        student.setStatus(StudentStatus.REGULAR);
        return StudentResponse.from(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = getStudent(id);
        if (studentRepository.existsByRunAndIdNot(request.run(), id)) {
            throw new ConflictException("Ya existe un estudiante con el RUN " + request.run());
        }
        if (studentRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new ConflictException("Ya existe un estudiante con el correo " + request.email());
        }

        boolean careerChanges = !Objects.equals(student.getCareer().getId(), request.careerId());
        boolean planChanges = request.studyPlanId() != null
                && !Objects.equals(student.getStudyPlan().getId(), request.studyPlanId());

        if (careerChanges || planChanges) {
            // Rule: career and plan can only change while the student has no enrollments or record.
            if (hasAcademicActivity(student)) {
                throw new ConflictException(
                        "El estudiante tiene inscripciones o historial; no se puede cambiar su carrera ni su plan");
            }
            Career career = careerService.getCareer(request.careerId());
            StudyPlan plan;
            if (request.studyPlanId() != null) {
                plan = studyPlanService.getStudyPlan(request.studyPlanId());
                if (!Objects.equals(plan.getCareer().getId(), career.getId())) {
                    throw new BadRequestException("El plan de estudios no pertenece a la carrera indicada");
                }
            } else {
                plan = getCurrentPlan(career);
            }
            student.setCareer(career);
            student.setStudyPlan(plan);
        }

        student.setRun(request.run());
        student.setFullName(request.fullName());
        student.setEmail(request.email());
        return StudentResponse.from(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse changeStatus(Long id, StudentStatus newStatus) {
        Student student = getStudent(id);
        // Rule: GRADUATED and ELIMINATED are set only by the period closing process.
        if (!newStatus.isManuallyAssignable()) {
            throw new BadRequestException("El estado " + newStatus + " solo lo asigna el sistema al cerrar el período");
        }
        if (!student.getStatus().isManuallyAssignable()) {
            throw new ConflictException("El estudiante está " + student.getStatus() + " y su estado no puede modificarse");
        }
        student.setStatus(newStatus);
        return StudentResponse.from(studentRepository.save(student));
    }

    @Transactional
    public void delete(Long id) {
        Student student = getStudent(id);
        // Rule: a student with enrollments or academic record cannot be physically deleted.
        if (hasAcademicActivity(student)) {
            throw new ConflictException("El estudiante tiene inscripciones o historial y no puede eliminarse");
        }
        studentRepository.delete(student);
    }

    public Student getStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el estudiante con id " + id));
    }

    private StudyPlan getCurrentPlan(Career career) {
        return studyPlanRepository.findByCareerIdAndStatus(career.getId(), StudyPlanStatus.CURRENT)
                .orElseThrow(() -> new ConflictException(
                        "La carrera " + career.getCode() + " no tiene un plan de estudios vigente"));
    }

    /** Whether the student has enrollments or academic record entries. */
    private boolean hasAcademicActivity(Student student) {
        return enrollmentRepository.existsByStudentId(student.getId())
                || academicRecordRepository.existsByStudentId(student.getId());
    }
}
