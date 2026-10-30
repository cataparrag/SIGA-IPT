package cl.ipt.siga.controller;

import cl.ipt.siga.dto.PeriodRequest;
import cl.ipt.siga.dto.PeriodResponse;
import cl.ipt.siga.service.AcademicPeriodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/periods")
@RequiredArgsConstructor
public class AcademicPeriodController {

    private final AcademicPeriodService periodService;

    @GetMapping
    public List<PeriodResponse> findAll() {
        return periodService.findAll();
    }

    @GetMapping("/{id}")
    public PeriodResponse findById(@PathVariable Long id) {
        return periodService.findById(id);
    }

    @PostMapping
    public ResponseEntity<PeriodResponse> create(@Valid @RequestBody PeriodRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(periodService.create(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        periodService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
