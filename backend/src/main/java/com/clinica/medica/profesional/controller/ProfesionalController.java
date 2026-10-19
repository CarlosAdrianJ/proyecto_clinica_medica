package com.clinica.medica.profesional.controller;

import com.clinica.medica.profesional.dto.ProfesionalRequest;
import com.clinica.medica.profesional.dto.ProfesionalResponse;
import com.clinica.medica.profesional.service.ProfesionalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/profesionales")
public class ProfesionalController {

    private final ProfesionalService profesionalService;

    public ProfesionalController(ProfesionalService profesionalService) {
        this.profesionalService = profesionalService;
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA')")
    @GetMapping
    public List<ProfesionalResponse> listar() {
        return profesionalService.listar();
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA') or @accesoProfesional.puedeConsultar(authentication, #id)")
    @GetMapping("/{id}")
    public ProfesionalResponse buscarPorId(@PathVariable Long id) {
        return profesionalService.buscarPorId(id);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCIONISTA')")
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

    @PostMapping("/{profesionalId}/especialidades/{especialidadId}")
    public ResponseEntity<Void> asociarEspecialidad(
            @PathVariable Long profesionalId,
            @PathVariable Long especialidadId
    ) {
        profesionalService.asociarEspecialidad(
                profesionalId,
                especialidadId
        );

        return ResponseEntity.noContent().build();
    }
}