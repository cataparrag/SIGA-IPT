package cl.ipt.siga.service;

import cl.ipt.siga.dto.StudyPlanRequest;
import cl.ipt.siga.dto.StudyPlanResponse;
import cl.ipt.siga.exception.ConflictException;
import cl.ipt.siga.exception.NotFoundException;
import cl.ipt.siga.model.Career;
import cl.ipt.siga.model.StudyPlan;
import cl.ipt.siga.model.StudyPlanStatus;
import cl.ipt.siga.repository.StudyPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;
    private final CareerService careerService;

    @Transactional(readOnly = true)
    public List<StudyPlanResponse> findByCareer(Long careerId) {
        careerService.getCareer(careerId);
        return studyPlanRepository.findByCareerId(careerId).stream()
                .map(StudyPlanResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudyPlanResponse findById(Long id) {
        return StudyPlanResponse.from(getStudyPlan(id));
    }

    @Transactional
    public StudyPlanResponse create(Long careerId, StudyPlanRequest request) {
        // Rule: every plan belongs to an existing career.
        Career career = careerService.getCareer(careerId);

        // Rule: the identifier distinguishes the plan from the other plans of the same career.
        if (studyPlanRepository.existsByCareerIdAndIdentifier(careerId, request.identifier())) {
            throw new ConflictException("La carrera ya tiene un plan con el identificador " + request.identifier());
        }

        StudyPlan plan = new StudyPlan();
        plan.setIdentifier(request.identifier());
        plan.setCareer(career);
        plan.setStatus(StudyPlanStatus.NOT_CURRENT);
        if (Boolean.TRUE.equals(request.current())) {
            makeCurrent(plan);
        }
        return StudyPlanResponse.from(studyPlanRepository.save(plan));
    }

    @Transactional
    public StudyPlanResponse setCurrent(Long id) {
        StudyPlan plan = getStudyPlan(id);
        makeCurrent(plan);
        return StudyPlanResponse.from(studyPlanRepository.save(plan));
    }

    @Transactional
    public void delete(Long id) {
        StudyPlan plan = getStudyPlan(id);
        // TODO: forbid deleting plans with subjects or students.
        studyPlanRepository.delete(plan);
    }

    /** Shared by other services that need a StudyPlan entity. */
    public StudyPlan getStudyPlan(Long id) {
        return studyPlanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el plan de estudios con id " + id));
    }

    /**
     * Rule: a career has only one current plan. Making a plan current turns
     * the previously current plan of the same career into NOT_CURRENT.
     * Students keep the plan they already had, because they point to their own plan.
     */
    private void makeCurrent(StudyPlan plan) {
        studyPlanRepository
                .findByCareerIdAndStatus(plan.getCareer().getId(), StudyPlanStatus.CURRENT)
                .filter(previous -> !Objects.equals(previous.getId(), plan.getId()))
                .ifPresent(previous -> {
                    previous.setStatus(StudyPlanStatus.NOT_CURRENT);
                    studyPlanRepository.save(previous);
                });
        plan.setStatus(StudyPlanStatus.CURRENT);
    }
}
