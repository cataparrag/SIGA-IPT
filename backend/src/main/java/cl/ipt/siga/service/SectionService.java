package cl.ipt.siga.service;

import cl.ipt.siga.dto.SectionRequest;
import cl.ipt.siga.dto.SectionResponse;
import cl.ipt.siga.dto.SectionUpdateRequest;
import cl.ipt.siga.exception.BadRequestException;
import cl.ipt.siga.exception.ConflictException;
import cl.ipt.siga.exception.NotFoundException;
import cl.ipt.siga.model.AcademicPeriod;
import cl.ipt.siga.model.ScheduleBlocks;
import cl.ipt.siga.model.Section;
import cl.ipt.siga.model.Subject;
import cl.ipt.siga.model.Teacher;
import cl.ipt.siga.model.TeacherStatus;
import cl.ipt.siga.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SectionService {

    static final int MIN_CAPACITY = 1;
    static final int MAX_CAPACITY = 60;

    private final SectionRepository sectionRepository;
    private final SubjectService subjectService;
    private final AcademicPeriodService periodService;
    private final TeacherService teacherService;

    /** The academic offer of a period. */
    @Transactional(readOnly = true)
    public List<SectionResponse> findByPeriod(Long periodId) {
        periodService.getPeriod(periodId);
        return sectionRepository.findByPeriodId(periodId).stream()
                .map(SectionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SectionResponse> findByTeacher(Long teacherId) {
        teacherService.getTeacher(teacherId);
        return sectionRepository.findByTeacherId(teacherId).stream()
                .map(SectionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SectionResponse findById(Long id) {
        return SectionResponse.from(getSection(id));
    }

    @Transactional
    public SectionResponse create(SectionRequest request) {
        // Rule: a section belongs to an existing subject and period.
        Subject subject = subjectService.getSubject(request.subjectId());
        AcademicPeriod period = periodService.getPeriod(request.periodId());
        requireOpen(period);

        // Rule: a subject has a single section per period.
        if (sectionRepository.existsBySubjectIdAndPeriodId(subject.getId(), period.getId())) {
            throw new ConflictException("La asignatura " + subject.getCode()
                    + " ya tiene una sección en el período " + period.getCode());
        }

        Teacher teacher = teacherService.getTeacher(request.teacherId());
        validateCapacity(request.capacity());
        Set<String> blocks = validateBlocks(request.blocks(), subject);
        validateTeacher(teacher, period, blocks, null);

        Section section = new Section();
        section.setSubject(subject);
        section.setPeriod(period);
        section.setTeacher(teacher);
        section.setCapacity(request.capacity());
        section.setBlocks(blocks);
        return SectionResponse.from(sectionRepository.save(section));
    }

    @Transactional
    public SectionResponse update(Long id, SectionUpdateRequest request) {
        Section section = getSection(id);
        requireOpen(section.getPeriod());

        Teacher teacher = teacherService.getTeacher(request.teacherId());
        validateCapacity(request.capacity());
        // TODO (Epic 6): the capacity cannot be lower than the number of enrolled students.
        Set<String> blocks = validateBlocks(request.blocks(), section.getSubject());
        validateTeacher(teacher, section.getPeriod(), blocks, section.getId());

        section.setTeacher(teacher);
        section.setCapacity(request.capacity());
        section.setBlocks(blocks);
        return SectionResponse.from(sectionRepository.save(section));
    }

    @Transactional
    public void delete(Long id) {
        Section section = getSection(id);
        // Rule: a section with enrolled students cannot be physically deleted.
        // TODO (Epic 6): check enrollments.
        sectionRepository.delete(section);
    }

    public Section getSection(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe la sección con id " + id));
    }

    /** Sections can only be created or changed while their period is OPEN. */
    private void requireOpen(AcademicPeriod period) {
        if (!period.isOpen()) {
            throw new ConflictException("El período " + period.getCode() + " está cerrado");
        }
    }

    /** Rule: capacity greater than zero and at most 60 students. */
    private void validateCapacity(int capacity) {
        if (capacity < MIN_CAPACITY || capacity > MAX_CAPACITY) {
            throw new BadRequestException("La capacidad debe ser mayor que 0 y no superar los 60 estudiantes");
        }
    }

    /**
     * Rules on schedule blocks: at least one, valid day (L, M, W, J, V) and
     * module (1-6), no duplicates, and as many blocks as TEL hours / 2.
     */
    private Set<String> validateBlocks(List<String> rawBlocks, Subject subject) {
        if (rawBlocks == null || rawBlocks.isEmpty()) {
            throw new BadRequestException("La sección debe tener al menos un bloque horario");
        }
        Set<String> blocks = new HashSet<>();
        for (String raw : rawBlocks) {
            String block = ScheduleBlocks.normalize(raw);
            if (!ScheduleBlocks.isValid(block)) {
                throw new BadRequestException("El bloque '" + raw
                        + "' no es válido: use un día L, M, W, J o V y un módulo del 1 al 6 (ej. L2)");
            }
            if (!blocks.add(block)) {
                throw new BadRequestException("El bloque " + block + " está duplicado");
            }
        }
        int totalHours = subject.getTotalHours();
        if (totalHours % ScheduleBlocks.HOURS_PER_BLOCK != 0) {
            throw new BadRequestException("Las horas TEL de " + subject.getCode()
                    + " no se pueden dividir en bloques de 2 horas");
        }
        int requiredBlocks = totalHours / ScheduleBlocks.HOURS_PER_BLOCK;
        if (blocks.size() != requiredBlocks) {
            throw new BadRequestException("La asignatura " + subject.getCode() + " tiene " + totalHours
                    + " horas TEL, por lo que su sección necesita exactamente " + requiredBlocks + " bloques");
        }
        return blocks;
    }

    /**
     * Rules on the teacher: must be ACTIVE when assigned, and cannot have two
     * sections in the same period sharing a day and module.
     */
    private void validateTeacher(Teacher teacher, AcademicPeriod period, Set<String> blocks, Long currentSectionId) {
        if (teacher.getStatus() != TeacherStatus.ACTIVE) {
            throw new ConflictException("El docente " + teacher.getFullName() + " no está activo");
        }
        for (Section other : sectionRepository.findByTeacherIdAndPeriodId(teacher.getId(), period.getId())) {
            if (Objects.equals(other.getId(), currentSectionId)) {
                continue;
            }
            for (String block : blocks) {
                if (other.getBlocks().contains(block)) {
                    throw new ConflictException("El docente ya tiene la sección de "
                            + other.getSubject().getCode() + " en el bloque " + block);
                }
            }
        }
    }
}
