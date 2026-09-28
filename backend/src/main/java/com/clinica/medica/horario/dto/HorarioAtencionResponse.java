package com.clinica.medica.horario.dto;

import com.clinica.medica.horario.model.DiaSemana;

import java.time.LocalTime;

public record HorarioAtencionResponse(
        Long id,
        Long profesionalId,
        String profesionalNombre,
        String profesionalApellido,
        DiaSemana diaSemana,
        LocalTime horaDesde,
        LocalTime horaHasta,
        Integer duracionTurno,
        Boolean activo
) {
}