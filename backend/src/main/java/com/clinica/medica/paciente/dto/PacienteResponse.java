package com.clinica.medica.paciente.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PacienteResponse(
        Long id,
        String nombre,
        String apellido,
        String dni,
        LocalDate fechaNacimiento,
        String telefono,
        String email,
        String direccion,
        boolean activo,
        LocalDateTime fechaAlta
) {
}