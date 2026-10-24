package cl.ipt.siga.service;

import cl.ipt.siga.dto.SubjectRequest;
import cl.ipt.siga.dto.SubjectResponse;
import cl.ipt.siga.exception.BadRequestException;
import cl.ipt.siga.exception.ConflictException;
import cl.ipt.siga.exception.NotFoundException;
import cl.ipt.siga.model.StudyPlan;
import cl.ipt.siga.model.Subject;
import cl.ipt.siga.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SubjectService {

    static final int MIN_SEMESTER = 1;
    static final int MAX_SEMESTER = 4;
    static final int MIN_SCT = 1;
    static final int MAX_SCT = 7;
    static final int MAX_WEEKLY_HOURS_EXCLUSIVE = 8;
    static final int MAX_PREREQUISITES = 3;

    private final SubjectRepository subjectRepository;
    private final StudyPlanService studyPlanService;

    @Transactional(readOnly = true)
    public List<SubjectResponse> findByStudyPlan(Long studyPlanId) {
        studyPlanService.getStudyPlan(studyPlanId);
        return subjectRepository.findByStudyPlanIdOrderBySemesterAscCodeAsc(studyPlanId).stream()
                .map(SubjectResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubjectResponse findById(Long id) {
        return SubjectResponse.from(getSubject(id));
    }

    @Transactional
    public SubjectResponse create(Long studyPlanId, SubjectRequest request) {
        // Rule: every subject belongs to an existing study plan.
        StudyPlan plan = studyPlanService.getStudyPlan(studyPlanId);

        // Rule: the code is unique within its study plan.
        if (subjectRepository.existsByStudyPlanIdAndCode(studyPlanId, request.code())) {
            throw new ConflictException("El plan ya tiene una asignatura con el código " + request.code());
        }
        validateNumbers(request);

        Subject subject = new Subject();
        subject.setStudyPlan(plan);
        applyFields(subject, request);
        subject.setPrerequisites(resolvePrerequisites(request.prerequisiteIds(), plan, request.semester(), null));
        return SubjectResponse.from(subjectRepository.save(subject));
    }

    @Transactional
    public SubjectResponse update(Long id, SubjectRequest request) {
        Subject subject = getSubject(id);
        StudyPlan plan = subject.getStudyPlan();

        if (subjectRepository.existsByStudyPlanIdAndCodeAndIdNot(plan.getId(), request.code(), id)) {
            throw new ConflictException("El plan ya tiene una asignatura con el código " + request.code());
        }
        validateNumbers(request);

        // If the semester changes, the subjects that depend on this one must still be in later semesters.
        for (Subject dependent : subjectRepository.findByPrerequisitesId(id)) {
            if (dependent.getSemester() <= request.semester()) {
                throw new BadRequestException("La asignatura " + dependent.getCode()
                        + " la tiene como prerrequisito y no es de un semestre posterior");
            }
        }

        applyFields(subject, request);
        subject.setPrerequisites(resolvePrerequisites(request.prerequisiteIds(), plan, request.semester(), id));
        return SubjectResponse.from(subjectRepository.save(subject));
    }

    @Transactional
    public void delete(Long id) {
        Subject subject = getSubject(id);
        // Rule: a subject in a student's academic record cannot be physically deleted.
        // TODO: check the academic record once that entity exists.
        if (!subjectRepository.findByPrerequisitesId(id).isEmpty()) {
            throw new ConflictException("La asignatura es prerrequisito de otras y no puede eliminarse");
        }
        subjectRepository.delete(subject);
    }

    public Subject getSubject(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe la asignatura con id " + id));
    }

    /** Rules on semester, SCT and TEL hours. */
    private void validateNumbers(SubjectRequest request) {
        if (request.semester() < MIN_SEMESTER || request.semester() > MAX_SEMESTER) {
            throw new BadRequestException("El semestre debe estar entre 1 y 4");
        }
        if (request.sct() < MIN_SCT || request.sct() > MAX_SCT) {
            throw new BadRequestException("El SCT debe ser mayor que 0 y menor o igual a 7");
        }
        if (request.theoryHours() < 0 || request.exerciseHours() < 0 || request.labHours() < 0) {
            throw new BadRequestException("Las horas de teoría, ejercicios y laboratorio no pueden ser negativas");
        }
        int total = request.theoryHours() + request.exerciseHours() + request.labHours();
        if (total >= MAX_WEEKLY_HOURS_EXCLUSIVE) {
            throw new BadRequestException("La suma de horas TEL debe ser inferior a 8 horas semanales");
        }
    }

    /**
     * Rules on prerequisites: between 0 and 3, existing subjects of the same
     * plan, and always from earlier semesters.
     */
    private Set<Subject> resolvePrerequisites(List<Long> ids, StudyPlan plan, int semester, Long selfId) {
        Set<Subject> prerequisites = new HashSet<>();
        if (ids == null || ids.isEmpty()) {
            return prerequisites;
        }
        Set<Long> uniqueIds = new HashSet<>(ids);
        if (uniqueIds.size() > MAX_PREREQUISITES) {
            throw new BadRequestException("Una asignatura puede tener como máximo 3 prerrequisitos");
        }
        for (Long prerequisiteId : uniqueIds) {
            if (Objects.equals(prerequisiteId, selfId)) {
                throw new BadRequestException("Una asignatura no puede ser prerrequisito de sí misma");
            }
            Subject prerequisite = getSubject(prerequisiteId);
            if (!Objects.equals(prerequisite.getStudyPlan().getId(), plan.getId())) {
                throw new BadRequestException("El prerrequisito " + prerequisite.getCode()
                        + " no pertenece al mismo plan de estudios");
            }
            if (prerequisite.getSemester() >= semester) {
                throw new BadRequestException("El prerrequisito " + prerequisite.getCode()
                        + " debe ser de un semestre anterior");
            }
            prerequisites.add(prerequisite);
        }
        return prerequisites;
    }

    private void applyFields(Subject subject, SubjectRequest request) {
        subject.setCode(request.code());
        subject.setName(request.name());
        subject.setSemester(request.semester());
        subject.setTheoryHours(request.theoryHours());
        subject.setExerciseHours(request.exerciseHours());
        subject.setLabHours(request.labHours());
        subject.setSct(request.sct());
    }
}