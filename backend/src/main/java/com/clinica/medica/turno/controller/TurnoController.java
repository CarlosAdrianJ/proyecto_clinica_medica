package com.clinica.medica.turno.controller;

import com.clinica.medica.security.AccesoProfesional;
import com.clinica.medica.turno.dto.CambiarEstadoTurnoRequest;
import com.clinica.medica.turno.dto.ReprogramarTurnoRequest;
import com.clinica.medica.turno.dto.TurnoRequest;
import com.clinica.medica.turno.dto.TurnoResponse;
import com.clinica.medica.turno.model.EstadoTurno;
import com.clinica.medica.turno.service.TurnoService;
import com.clinica.medica.usuario.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/turnos")
public class TurnoController {

    private final TurnoService turnoService;
    private final AccesoProfesional accesoProfesional;
    private final UsuarioRepository usuarios;

    public TurnoController(
            TurnoService turnoService,
            AccesoProfesional accesoProfesional,
            UsuarioRepository usuarios
    ) {
        this.turnoService = turnoService;
        this.accesoProfesional = accesoProfesional;
        this.usuarios = usuarios;
    }

    @GetMapping("/mi-historial")
    public List<TurnoResponse> consultarMiHistorial(
            Authentication authentication
    ) {
        Long pacienteId = usuarios
                .findByNombreUsuario(authentication.getName())
                .map(usuario -> usuario.getPaciente() == null
                        ? null
                        : usuario.getPaciente().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Perfil de paciente no encontrado"
                ));

        return turnoService.buscar(pacienteId, null, null, null);
    }

    @GetMapping("/mi-agenda")
    public List<TurnoResponse> consultarMiAgenda(
            Authentication authentication,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,
            @RequestParam(required = false)
            EstadoTurno estado
    ) {
        Long profesionalId =
                accesoProfesional.obtenerProfesionalId(authentication);

        return turnoService.buscar(
                null,
                profesionalId,
                fecha,
                estado
        );
    }

    @GetMapping
    public List<TurnoResponse> listar(
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) Long profesionalId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,
            @RequestParam(required = false)
            EstadoTurno estado
    ) {
        if (pacienteId == null
                && profesionalId == null
                && fecha == null
                && estado == null) {
            return turnoService.listar();
        }

        return turnoService.buscar(
                pacienteId,
                profesionalId,
                fecha,
                estado
        );
    }

    @GetMapping("/{id}")
    public TurnoResponse buscarPorId(@PathVariable Long id) {
        return turnoService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<TurnoResponse> crear(
            @Valid @RequestBody TurnoRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(turnoService.crear(request));
    }

    @PutMapping("/{id}/reprogramar")
    public TurnoResponse reprogramar(
            @PathVariable Long id,
            @Valid @RequestBody ReprogramarTurnoRequest request
    ) {
        return turnoService.reprogramar(id, request);
    }

    @PatchMapping("/{id}/estado")
    public TurnoResponse cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoTurnoRequest request
    ) {
        return turnoService.cambiarEstado(id, request.estado());
    }

    @PatchMapping("/{id}/cancelar")
    public TurnoResponse cancelar(@PathVariable Long id) {
        return turnoService.cancelar(id);
    }
}