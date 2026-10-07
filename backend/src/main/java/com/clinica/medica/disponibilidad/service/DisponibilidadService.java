package com.clinica.medica.disponibilidad.service;

import com.clinica.medica.disponibilidad.dto.DisponibilidadResponse;
import com.clinica.medica.disponibilidad.dto.FranjaDisponibleResponse;
import com.clinica.medica.horario.model.DiaSemana;
import com.clinica.medica.horario.model.HorarioAtencion;
import com.clinica.medica.horario.repository.HorarioAtencionRepository;
import com.clinica.medica.profesional.model.Profesional;
import com.clinica.medica.profesional.repository.ProfesionalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DisponibilidadService {

    private final ProfesionalRepository profesionalRepository;
    private final HorarioAtencionRepository horarioAtencionRepository;

    public DisponibilidadService(
            ProfesionalRepository profesionalRepository,
            HorarioAtencionRepository horarioAtencionRepository
    ) {
        this.profesionalRepository = profesionalRepository;
        this.horarioAtencionRepository = horarioAtencionRepository;
    }

    public DisponibilidadResponse consultar(Long profesionalId, LocalDate fecha) {
        Profesional profesional = profesionalRepository.findById(profesionalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Profesional no encontrado"
                ));

        if (!Boolean.TRUE.equals(profesional.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El profesional se encuentra inactivo"
            );
        }

        DiaSemana diaSemana = convertirDiaSemana(fecha.getDayOfWeek());

        List<HorarioAtencion> horarios =
                horarioAtencionRepository.findByProfesionalIdAndActivoTrue(profesionalId);

        List<FranjaDisponibleResponse> franjas = new ArrayList<>();

        horarios.stream()
                .filter(horario -> horario.getDiaSemana() == diaSemana)
                .sorted(Comparator.comparing(HorarioAtencion::getHoraDesde))
                .forEach(horario -> generarFranjas(horario, franjas));

        return new DisponibilidadResponse(
                profesionalId,
                fecha,
                franjas
        );
    }

    private void generarFranjas(
            HorarioAtencion horario,
            List<FranjaDisponibleResponse> franjas
    ) {
        LocalTime horaInicio = horario.getHoraDesde();

        while (!horaInicio
                .plusMinutes(horario.getDuracionTurno())
                .isAfter(horario.getHoraHasta())) {

            LocalTime horaFin =
                    horaInicio.plusMinutes(horario.getDuracionTurno());

            franjas.add(new FranjaDisponibleResponse(
                    horaInicio,
                    horaFin
            ));

            horaInicio = horaFin;
        }
    }

    private DiaSemana convertirDiaSemana(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> DiaSemana.LUNES;
            case TUESDAY -> DiaSemana.MARTES;
            case WEDNESDAY -> DiaSemana.MIERCOLES;
            case THURSDAY -> DiaSemana.JUEVES;
            case FRIDAY -> DiaSemana.VIERNES;
            case SATURDAY -> DiaSemana.SABADO;
            case SUNDAY -> DiaSemana.DOMINGO;
        };
    }
}