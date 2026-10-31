package cl.ipt.siga.controller;

import cl.ipt.siga.dto.MatriculationRequest;
import cl.ipt.siga.dto.MatriculationResponse;
import cl.ipt.siga.service.MatriculationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MatriculationController {

    private final MatriculationService matriculationService;

    @GetMapping("/periods/{periodId}/matriculations")
    public List<MatriculationResponse> findByPeriod(@PathVariable Long periodId) {
        return matriculationService.findByPeriod(periodId);
    }

    @PostMapping("/periods/{periodId}/matriculations")
    public ResponseEntity<MatriculationResponse> create(@PathVariable Long periodId,
                                                        @Valid @RequestBody MatriculationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(matriculationService.create(periodId, request.studentId()));
    }

    @DeleteMapping("/matriculations/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        matriculationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
