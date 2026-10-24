package com.clinica.medica.profesional.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ProfesionalResponse(
        Long id,
        String nombre,
        String apellido,
        String matricula,
        String telefono,
        String email,
        boolean activo,
        LocalDateTime fechaAlta,
        List<EspecialidadAsociada> especialidades
) {
    public record EspecialidadAsociada(Long id, String nombre, boolean activo) {}
}
