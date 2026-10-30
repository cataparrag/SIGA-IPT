package cl.ipt.siga.service;

import cl.ipt.siga.dto.PeriodRequest;
import cl.ipt.siga.dto.PeriodResponse;
import cl.ipt.siga.exception.BadRequestException;
import cl.ipt.siga.exception.ConflictException;
import cl.ipt.siga.exception.NotFoundException;
import cl.ipt.siga.model.AcademicPeriod;
import cl.ipt.siga.model.PeriodStatus;
import cl.ipt.siga.repository.AcademicPeriodRepository;
import cl.ipt.siga.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicPeriodService {

    static final int MIN_YEAR = 2000;

    private final AcademicPeriodRepository periodRepository;
    private final SectionRepository sectionRepository;

    @Transactional(readOnly = true)
    public List<PeriodResponse> findAll() {
        return periodRepository.findAllByOrderByYearDescSemesterDesc().stream()
                .map(PeriodResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PeriodResponse findById(Long id) {
        return PeriodResponse.from(getPeriod(id));
    }

    @Transactional
    public PeriodResponse create(PeriodRequest request) {
        if (request.semester() != 1 && request.semester() != 2) {
            throw new BadRequestException("El semestre del período debe ser 1 o 2");
        }
        if (request.year() < MIN_YEAR) {
            throw new BadRequestException("El año del período no es válido");
        }
        if (periodRepository.existsByYearAndSemester(request.year(), request.semester())) {
            throw new ConflictException("Ya existe el período " + request.year() + "-" + request.semester());
        }
        AcademicPeriod period = new AcademicPeriod();
        period.setYear(request.year());
        period.setSemester(request.semester());
        // Rule: every new period starts OPEN.
        period.setStatus(PeriodStatus.OPEN);
        return PeriodResponse.from(periodRepository.save(period));
    }

    @Transactional
    public void delete(Long id) {
        AcademicPeriod period = getPeriod(id);
        if (sectionRepository.existsByPeriodId(id)) {
            throw new ConflictException("El período tiene secciones y no puede eliminarse");
        }
        periodRepository.delete(period);
    }

    public AcademicPeriod getPeriod(Long id) {
        return periodRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el período con id " + id));
    }
}
