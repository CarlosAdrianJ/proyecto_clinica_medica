package com.clinica.medica.profesional.controller;

import com.clinica.medica.profesional.dto.ProfesionalRequest;
import com.clinica.medica.profesional.dto.ProfesionalResponse;
import com.clinica.medica.profesional.service.ProfesionalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profesionales")
public class ProfesionalController {

    private final ProfesionalService profesionalService;

    public ProfesionalController(ProfesionalService profesionalService) {
        this.profesionalService = profesionalService;
    }

    @GetMapping
    public List<ProfesionalResponse> listar() {
        return profesionalService.listar();
    }

    @GetMapping("/{id}")
    public ProfesionalResponse buscarPorId(@PathVariable Long id) {
        return profesionalService.buscarPorId(id);
    }

    @GetMapping("/matricula/{matricula}")
    public ProfesionalResponse buscarPorMatricula(@PathVariable String matricula) {
        return profesionalService.buscarPorMatricula(matricula);
    }

    @PostMapping
    public ResponseEntity<ProfesionalResponse> crear(
            @Valid @RequestBody ProfesionalRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(profesionalService.crear(request));
    }

    @PutMapping("/{id}")
    public ProfesionalResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProfesionalRequest request
    ) {
        return profesionalService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        profesionalService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
