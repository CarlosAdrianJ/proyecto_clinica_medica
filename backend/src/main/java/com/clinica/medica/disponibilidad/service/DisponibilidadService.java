package com.clinica.medica.disponibilidad.service;

import com.clinica.medica.disponibilidad.dto.DisponibilidadResponse;
import com.clinica.medica.disponibilidad.dto.FranjaDisponibleResponse;
import com.clinica.medica.horario.model.DiaSemana;
import com.clinica.medica.horario.model.HorarioAtencion;
import com.clinica.medica.horario.repository.HorarioAtencionRepository;
import com.clinica.medica.profesional.model.Profesional;
import com.clinica.medica.profesional.repository.ProfesionalRepository;
import com.clinica.medica.turno.model.EstadoTurno;
import com.clinica.medica.turno.model.Turno;
import com.clinica.medica.turno.repository.TurnoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DisponibilidadService {

    private static final ZoneId ZONA_CLINICA =
            ZoneId.of("America/Argentina/Buenos_Aires");

    private static final LocalTime HORA_APERTURA =
            LocalTime.of(8, 0);

    private static final LocalTime CIERRE_LUNES_A_VIERNES =
            LocalTime.of(20, 0);

    private static final LocalTime CIERRE_SABADO =
            LocalTime.of(12, 0);

    private final ProfesionalRepository profesionalRepository;
    private final HorarioAtencionRepository horarioAtencionRepository;
    private final TurnoRepository turnoRepository;

    public DisponibilidadService(
            ProfesionalRepository profesionalRepository,
            HorarioAtencionRepository horarioAtencionRepository,
            TurnoRepository turnoRepository
    ) {
        this.profesionalRepository = profesionalRepository;
        this.horarioAtencionRepository = horarioAtencionRepository;
        this.turnoRepository = turnoRepository;
    }

    public DisponibilidadResponse consultar(
            Long profesionalId,
            LocalDate fecha
    ) {
        Profesional profesional =
                profesionalRepository.findById(profesionalId)
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

        LocalDate hoy = LocalDate.now(ZONA_CLINICA);

        if (fecha.isBefore(hoy)
                || fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return new DisponibilidadResponse(
                    profesionalId,
                    fecha,
                    List.of()
            );
        }

        DayOfWeek dia = fecha.getDayOfWeek();
        DiaSemana diaSemana = convertirDiaSemana(dia);
        LocalTime horaCierre = dia == DayOfWeek.SATURDAY
                ? CIERRE_SABADO
                : CIERRE_LUNES_A_VIERNES;

        LocalTime horaActual = fecha.equals(hoy)
                ? LocalTime.now(ZONA_CLINICA)
                : null;

        List<HorarioAtencion> horarios =
                horarioAtencionRepository
                        .findByProfesionalIdAndActivoTrue(profesionalId);

        List<Turno> turnosOcupados =
                turnoRepository
                        .findByProfesionalIdAndFechaAndEstadoNotOrderByHoraInicioAsc(
                                profesionalId,
                                fecha,
                                EstadoTurno.CANCELADO
                        );

        List<FranjaDisponibleResponse> franjas = new ArrayList<>();

        horarios.stream()
                .filter(horario -> horario.getDiaSemana() == diaSemana)
                .filter(horario -> horario.getDuracionTurno() == 30)
                .filter(horario ->
                        esBloqueDeTreintaMinutos(horario.getHoraDesde())
                )
                .filter(horario ->
                        esBloqueDeTreintaMinutos(horario.getHoraHasta())
                )
                .sorted(Comparator.comparing(HorarioAtencion::getHoraDesde))
                .forEach(horario ->
                        generarFranjas(
                                horario,
                                turnosOcupados,
                                franjas,
                                horaCierre,
                                horaActual
                        )
                );

        return new DisponibilidadResponse(
                profesionalId,
                fecha,
                franjas
        );
    }

    private void generarFranjas(
            HorarioAtencion horario,
            List<Turno> turnosOcupados,
            List<FranjaDisponibleResponse> franjas,
            LocalTime horaCierre,
            LocalTime horaActual
    ) {
        LocalTime horaInicio = horario.getHoraDesde();

        while (true) {
            LocalTime horaFin =
                    horaInicio.plusMinutes(horario.getDuracionTurno());

            if (horaFin.isAfter(horario.getHoraHasta())) {
                break;
            }

            boolean dentroDelHorarioClinica =
                    !horaInicio.isBefore(HORA_APERTURA)
                            && !horaFin.isAfter(horaCierre);

            boolean noPasoLaHora =
                    horaActual == null || horaInicio.isAfter(horaActual);

            if (dentroDelHorarioClinica
                    && noPasoLaHora
                    && !estaOcupada(horaInicio, horaFin, turnosOcupados)) {
                franjas.add(new FranjaDisponibleResponse(
                        horaInicio,
                        horaFin
                ));
            }

            horaInicio = horaFin;
        }
    }

    private boolean estaOcupada(
            LocalTime horaInicio,
            LocalTime horaFin,
            List<Turno> turnosOcupados
    ) {
        return turnosOcupados.stream()
                .anyMatch(turno ->
                        turno.getHoraInicio().isBefore(horaFin)
                                && turno.getHoraFin().isAfter(horaInicio)
                );
    }

    private boolean esBloqueDeTreintaMinutos(LocalTime hora) {
        return hora.getMinute() % 30 == 0
                && hora.getSecond() == 0
                && hora.getNano() == 0;
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