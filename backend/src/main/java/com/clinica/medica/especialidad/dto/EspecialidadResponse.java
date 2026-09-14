package com.clinica.medica.especialidad.dto;

public record EspecialidadResponse(
        Long id,
        String nombre,
        String descripcion,
        boolean activo
) {
}