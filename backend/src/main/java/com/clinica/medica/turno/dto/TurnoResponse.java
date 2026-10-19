package com.clinica.medica.turno.dto;

import com.clinica.medica.turno.model.EstadoTurno;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record TurnoResponse(
        Long id,
        Long pacienteId,
        String pacienteNombre,
        Long profesionalId,
        String profesionalNombre,
        Long especialidadId,
        String especialidadNombre,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        EstadoTurno estado,
        String observaciones,
        LocalDateTime fechaCreacion
) {
}