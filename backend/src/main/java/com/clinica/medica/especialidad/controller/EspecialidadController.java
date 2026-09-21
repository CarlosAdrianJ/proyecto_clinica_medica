package com.clinica.medica.especialidad.controller;

import java.util.List;

import com.clinica.medica.especialidad.dto.EspecialidadResponse;
import com.clinica.medica.especialidad.dto.EspecialidadRequest;
import com.clinica.medica.especialidad.model.Especialidad;
import com.clinica.medica.especialidad.service.EspecialidadService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/especialidades")
public class EspecialidadController {

    private final EspecialidadService especialidadService;

    public EspecialidadController(EspecialidadService especialidadService) {
        this.especialidadService = especialidadService;
    }

    @GetMapping
    public List<EspecialidadResponse> listar() {
        return especialidadService.listar()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public EspecialidadResponse buscarPorId(@PathVariable Long id) {
        return toResponse(especialidadService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<EspecialidadResponse> crear(@Valid @RequestBody EspecialidadRequest request) {
        Especialidad creada = especialidadService.crear(toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(creada));
    }

    @PutMapping("/{id}")
    public EspecialidadResponse actualizar(@PathVariable Long id, @Valid @RequestBody EspecialidadRequest request) {
        Especialidad actualizada = especialidadService.actualizar(id, toEntity(request));
        return toResponse(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        especialidadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private EspecialidadResponse toResponse(Especialidad especialidad) {
        return new EspecialidadResponse(
                especialidad.getId(),
                especialidad.getNombre(),
                especialidad.getDescripcion(),
                especialidad.isActivo()
        );
    }

    private Especialidad toEntity(EspecialidadRequest request) {
        Especialidad especialidad = new Especialidad();
        especialidad.setNombre(request.nombre());
        especialidad.setDescripcion(request.descripcion());
        return especialidad;
    }
}