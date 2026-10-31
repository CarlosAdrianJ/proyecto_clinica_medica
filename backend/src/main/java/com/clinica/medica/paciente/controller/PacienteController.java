package com.clinica.medica.paciente.controller;

import com.clinica.medica.paciente.dto.PacienteRequest;
import com.clinica.medica.paciente.dto.PacienteResponse;
import com.clinica.medica.paciente.service.PacienteService;
import com.clinica.medica.usuario.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;
    private final UsuarioRepository usuarios;

    public PacienteController(
            PacienteService pacienteService,
            UsuarioRepository usuarios
    ) {
        this.pacienteService = pacienteService;
        this.usuarios = usuarios;
    }

    @GetMapping("/mi-perfil")
    public PacienteResponse miPerfil(Authentication authentication) {
        Long pacienteId = usuarios
                .findByNombreUsuario(authentication.getName())
                .map(usuario -> usuario.getPaciente() == null
                        ? null
                        : usuario.getPaciente().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Perfil de paciente no encontrado"
                ));

        return pacienteService.buscarPorId(pacienteId);
    }

    @GetMapping
    public List<PacienteResponse> listar() {
        return pacienteService.listar();
    }

    @GetMapping("/{id}")
    public PacienteResponse buscarPorId(@PathVariable Long id) {
        return pacienteService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<PacienteResponse> crear(
            @Valid @RequestBody PacienteRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(pacienteService.crear(request));
    }

    @PutMapping("/{id}")
    public PacienteResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PacienteRequest request
    ) {
        return pacienteService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pacienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}