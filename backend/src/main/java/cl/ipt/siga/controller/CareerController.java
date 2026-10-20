package cl.ipt.siga.controller;

import cl.ipt.siga.dto.CareerRequest;
import cl.ipt.siga.dto.CareerResponse;
import cl.ipt.siga.dto.CareerStatusRequest;
import cl.ipt.siga.service.CareerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/careers")
@RequiredArgsConstructor
public class CareerController {

    private final CareerService careerService;

    @GetMapping
    public List<CareerResponse> findAll() {
        return careerService.findAll();
    }

    @GetMapping("/{id}")
    public CareerResponse findById(@PathVariable Long id) {
        return careerService.findById(id);
    }

    @PostMapping
    public ResponseEntity<CareerResponse> create(@Valid @RequestBody CareerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(careerService.create(request));
    }

    @PutMapping("/{id}")
    public CareerResponse update(@PathVariable Long id, @Valid @RequestBody CareerRequest request) {
        return careerService.update(id, request);
    }

    @PutMapping("/{id}/status")
    public CareerResponse changeStatus(@PathVariable Long id, @Valid @RequestBody CareerStatusRequest request) {
        return careerService.changeStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        careerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}