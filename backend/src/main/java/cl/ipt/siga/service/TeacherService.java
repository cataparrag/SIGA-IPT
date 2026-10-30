package cl.ipt.siga.service;

import cl.ipt.siga.dto.TeacherRequest;
import cl.ipt.siga.dto.TeacherResponse;
import cl.ipt.siga.exception.ConflictException;
import cl.ipt.siga.exception.NotFoundException;
import cl.ipt.siga.model.PeriodStatus;
import cl.ipt.siga.model.Teacher;
import cl.ipt.siga.model.TeacherStatus;
import cl.ipt.siga.repository.SectionRepository;
import cl.ipt.siga.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final SectionRepository sectionRepository;

    @Transactional(readOnly = true)
    public List<TeacherResponse> findAll() {
        return teacherRepository.findAll().stream()
                .map(TeacherResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeacherResponse findById(Long id) {
        return TeacherResponse.from(getTeacher(id));
    }

    @Transactional
    public TeacherResponse create(TeacherRequest request) {
        // Rule: RUN and email are unique in the system.
        if (teacherRepository.existsByRun(request.run())) {
            throw new ConflictException("Ya existe un docente con el RUN " + request.run());
        }
        if (teacherRepository.existsByEmail(request.email())) {
            throw new ConflictException("Ya existe un docente con el correo " + request.email());
        }
        Teacher teacher = new Teacher();
        applyFields(teacher, request);
        // Rule: every new teacher starts as ACTIVE.
        teacher.setStatus(TeacherStatus.ACTIVE);
        return TeacherResponse.from(teacherRepository.save(teacher));
    }

    @Transactional
    public TeacherResponse update(Long id, TeacherRequest request) {
        Teacher teacher = getTeacher(id);
        if (teacherRepository.existsByRunAndIdNot(request.run(), id)) {
            throw new ConflictException("Ya existe un docente con el RUN " + request.run());
        }
        if (teacherRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new ConflictException("Ya existe un docente con el correo " + request.email());
        }
        applyFields(teacher, request);
        return TeacherResponse.from(teacherRepository.save(teacher));
    }

    @Transactional
    public TeacherResponse changeStatus(Long id, TeacherStatus newStatus) {
        Teacher teacher = getTeacher(id);
        // Rule: a teacher cannot become INACTIVE while assigned to sections of an OPEN period.
        if (newStatus == TeacherStatus.INACTIVE
                && sectionRepository.existsByTeacherIdAndPeriodStatus(id, PeriodStatus.OPEN)) {
            throw new ConflictException("El docente tiene secciones en un período abierto y no puede quedar inactivo");
        }
        teacher.setStatus(newStatus);
        return TeacherResponse.from(teacherRepository.save(teacher));
    }

    @Transactional
    public void delete(Long id) {
        Teacher teacher = getTeacher(id);
        // Rule: a teacher with sections cannot be physically deleted.
        if (sectionRepository.existsByTeacherId(id)) {
            throw new ConflictException("El docente tiene secciones asociadas y no puede eliminarse");
        }
        teacherRepository.delete(teacher);
    }

    public Teacher getTeacher(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el docente con id " + id));
    }

    private void applyFields(Teacher teacher, TeacherRequest request) {
        teacher.setRun(request.run());
        teacher.setFullName(request.fullName());
        teacher.setEmail(request.email());
        teacher.setProfessionalTitle(request.professionalTitle());
        teacher.setAcademicDegree(request.academicDegree());
    }
}
