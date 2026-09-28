package com.clinica.medica.horario.dto;

import com.clinica.medica.horario.model.DiaSemana;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalTime;

public record HorarioAtencionRequest(
        @NotNull(message = "El profesional es obligatorio")
        Long profesionalId,

        @NotNull(message = "El día es obligatorio")
        DiaSemana diaSemana,

        @NotNull(message = "La hora de inicio es obligatoria")
        LocalTime horaDesde,

        @NotNull(message = "La hora de finalización es obligatoria")
        LocalTime horaHasta,

        @NotNull(message = "La duración es obligatoria")
        @Positive(message = "La duración debe ser mayor que cero")
        Integer duracionTurno
) {
}