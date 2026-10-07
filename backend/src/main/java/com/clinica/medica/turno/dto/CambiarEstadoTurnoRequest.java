package com.clinica.medica.turno.dto;

import com.clinica.medica.turno.model.EstadoTurno;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoTurnoRequest(

        @NotNull(message = "El estado es obligatorio")
        EstadoTurno estado
) {
}