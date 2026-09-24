package com.clinica.medica.profesional.dto;

import java.time.LocalDateTime;

public record ProfesionalResponse(
        Long id,
        String nombre,
        String apellido,
        String matricula,
        String telefono,
        String email,
        boolean activo,
        LocalDateTime fechaAlta
) {
}