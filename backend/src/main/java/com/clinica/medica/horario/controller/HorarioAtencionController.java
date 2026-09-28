package com.clinica.medica.horario.controller;

import com.clinica.medica.horario.dto.HorarioAtencionRequest;
import com.clinica.medica.horario.dto.HorarioAtencionResponse;
import com.clinica.medica.horario.service.HorarioAtencionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios-atencion")
public class HorarioAtencionController {

    private final HorarioAtencionService horarioService;

    public HorarioAtencionController(
            HorarioAtencionService horarioService
    ) {
        this.horarioService = horarioService;
    }

    @GetMapping
    public List<HorarioAtencionResponse> listar() {
        return horarioService.listar();
    }

    @GetMapping("/{id}")
    public HorarioAtencionResponse buscarPorId(
            @PathVariable Long id
    ) {
        return horarioService.buscarPorId(id);
    }

    @GetMapping("/profesional/{profesionalId}")
    public List<HorarioAtencionResponse> listarPorProfesional(
            @PathVariable Long profesionalId
    ) {
        return horarioService.listarPorProfesional(profesionalId);
    }

    @PostMapping
    public ResponseEntity<HorarioAtencionResponse> crear(
            @Valid @RequestBody HorarioAtencionRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(horarioService.crear(request));
    }

    @PutMapping("/{id}")
    public HorarioAtencionResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody HorarioAtencionRequest request
    ) {
        return horarioService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {
        horarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}