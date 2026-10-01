package com.clinica.medica.disponibilidad.dto;

import java.time.LocalDate;
import java.util.List;

public record DisponibilidadResponse(
        Long profesionalId,
        LocalDate fecha,
        List<FranjaDisponibleResponse> franjas
) {
}