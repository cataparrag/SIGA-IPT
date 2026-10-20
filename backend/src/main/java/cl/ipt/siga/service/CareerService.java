package cl.ipt.siga.service;

import cl.ipt.siga.dto.CareerRequest;
import cl.ipt.siga.dto.CareerResponse;
import cl.ipt.siga.exception.ConflictException;
import cl.ipt.siga.exception.NotFoundException;
import cl.ipt.siga.model.Career;
import cl.ipt.siga.model.CareerStatus;
import cl.ipt.siga.repository.CareerRepository;
import cl.ipt.siga.repository.StudyPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CareerService {

    private final CareerRepository careerRepository;
    private final StudyPlanRepository studyPlanRepository;

    @Transactional(readOnly = true)
    public List<CareerResponse> findAll() {
        return careerRepository.findAll().stream()
                .map(CareerResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CareerResponse findById(Long id) {
        return CareerResponse.from(getCareer(id));
    }

    @Transactional
    public CareerResponse create(CareerRequest request) {
        // Rule: the career code must be unique in the system.
        if (careerRepository.existsByCode(request.code())) {
            throw new ConflictException("Ya existe una carrera con el código " + request.code());
        }
        Career career = new Career();
        career.setCode(request.code());
        career.setName(request.name());
        career.setDescription(request.description());
        career.setStatus(CareerStatus.ACTIVE);
        return CareerResponse.from(careerRepository.save(career));
    }

    @Transactional
    public CareerResponse update(Long id, CareerRequest request) {
        Career career = getCareer(id);
        if (careerRepository.existsByCodeAndIdNot(request.code(), id)) {
            throw new ConflictException("Ya existe una carrera con el código " + request.code());
        }
        career.setCode(request.code());
        career.setName(request.name());
        career.setDescription(request.description());
        return CareerResponse.from(careerRepository.save(career));
    }

    @Transactional
    public CareerResponse changeStatus(Long id, CareerStatus status) {
        Career career = getCareer(id);
        career.setStatus(status);
        return CareerResponse.from(careerRepository.save(career));
    }

    @Transactional
    public void delete(Long id) {
        Career career = getCareer(id);
        // Rule: a career with study plans (or students) cannot be physically deleted,
        // only its status can change.
        // TODO: also check for students once the Student entity exists.
        if (studyPlanRepository.existsByCareerId(id)) {
            throw new ConflictException(
                    "La carrera tiene planes de estudio asociados; solo se puede cambiar su estado");
        }
        careerRepository.delete(career);
    }

    /** Shared by other services that need a Career entity. */
    public Career getCareer(Long id) {
        return careerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe la carrera con id " + id));
    }
}