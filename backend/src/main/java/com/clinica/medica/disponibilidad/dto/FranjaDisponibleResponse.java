package com.clinica.medica.disponibilidad.dto;

import java.time.LocalTime;

public record FranjaDisponibleResponse(
        LocalTime horaInicio,
        LocalTime horaFin
) {
}