package cl.ipt.siga.repository;

import cl.ipt.siga.model.StudyPlan;
import cl.ipt.siga.model.StudyPlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {

    List<StudyPlan> findByCareerId(Long careerId);

    Optional<StudyPlan> findByCareerIdAndStatus(Long careerId, StudyPlanStatus status);

    boolean existsByCareerIdAndIdentifier(Long careerId, String identifier);

    boolean existsByCareerId(Long careerId);
}